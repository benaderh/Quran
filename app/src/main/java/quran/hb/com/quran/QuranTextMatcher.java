package quran.hb.com.quran;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * Utilitaire de recherche et récupération de textes coraniques dans la base SQLite.
 *
 * Tables utilisées :
 *   - tb_tafcir  (Hafs) : colonnes id(0), sora(1), aya(2), aya_text(3), tefcir(4), p_sora(5), line, point
 *   - tb_tafcirW (Warsh): même structure
 *   - tb_q               : id_q (page), sora_p (sourate), hizb, aya_t, detail
 */
public class QuranTextMatcher {

    private static final String TAG = "QuranTextMatcher";

    private static final String TABLE_HAFS  = "tb_tafcir";
    private static final String TABLE_WARSH = "tb_tafcirW";
    private static final String TABLE_PAGE  = "tb_q";

    // Colonnes de tb_tafcir / tb_tafcirW
    private static final String COL_SORA     = "sora";
    private static final String COL_AYA      = "aya";
    private static final String COL_AYA_TEXT = "aya_text";
    private static final String COL_P_SORA   = "p_sora";

    // Colonnes de tb_q
    private static final String COL_PAGE     = "id_q";
    private static final String COL_PAGE_SORA = "sora_p";
    private static final String COL_AYA_T    = "aya_t";

    // ------------------------------------------------------------------ //
    //  Normalisation du texte arabe
    // ------------------------------------------------------------------ //

    /**
     * Normalise un texte arabe :
     *  - Supprime toutes les harakât (tashkil) : َ ُ ِ ً ٌ ٍ ّ ْ
     *  - Supprime le tatwil ـ
     *  - Normalise les variantes de Alef (أ إ آ ٱ → ا)
     *  - Normalise Hamza seule (ء → ا)
     *  - Supprime les espaces superflus
     */
    public static String normalize(String text) {
        if (text == null) return "";

        // Supprimer les harakât et signes de tajwid Unicode
        // U+064B = tanwin fath, U+064C = tanwin damm, U+064D = tanwin kasr
        // U+064E = fatha, U+064F = damma, U+0650 = kasra
        // U+0651 = shadda, U+0652 = sukun
        // U+0653..U+065F = autres signes
        // U+06D6..U+06DC = signes coraniques de récitation
        // U+06DF..U+06E4 = signes coraniques
        // U+06E7..U+06E8
        // U+06EA..U+06ED
        text = text.replaceAll("[\\u064B-\\u065F\\u0670\\u06D6-\\u06DC\\u06DF-\\u06E4\\u06E7\\u06E8\\u06EA-\\u06ED]", "");

        // Supprimer le tatwil (kashida)
        text = text.replace("\u0640", "");

        // Normaliser les formes de Alef
        text = text.replaceAll("[\u0622\u0623\u0625\u0671]", "\u0627"); // آ أ إ ٱ → ا

        // Hamza seule → Alef (approximation pour matching)
        text = text.replace("\u0621", "\u0627"); // ء → ا

        // Ya sans points (ى) → ي
        text = text.replace("\u0649", "\u064A");

        // Supprimer espaces multiples
        text = text.replaceAll("\\s+", " ").trim();

        return text;
    }

    // ------------------------------------------------------------------ //
    //  findAyaByText
    // ------------------------------------------------------------------ //

    /**
     * Recherche l'aya qui correspond le mieux au texte reconnu (par la voix).
     *
     * @param db     La base de données SQLite ouverte.
     * @param text   Le texte reconnu (peut contenir des harakât ou non).
     * @param isHafs true = riwaya Hafs, false = Warsh.
     * @return int[]{soraNum, ayaNum, pageNum} ou null si aucun résultat.
     */
    public static int[] findAyaByText(SQLiteDatabase db, String text, boolean isHafs) {
        if (db == null || text == null || text.isEmpty()) return null;

        String table = isHafs ? TABLE_HAFS : TABLE_WARSH;
        String normalizedInput = normalize(text);

        // Stratégie 1 : correspondance exacte après normalisation
        int[] result = searchExact(db, table, normalizedInput);
        if (result != null) return result;

        // Stratégie 2 : correspondance partielle — l'aya_text contient au moins
        // les 5 premiers mots significatifs du texte saisi
        result = searchPartial(db, table, normalizedInput);
        return result; // peut être null
    }

    private static int[] searchExact(SQLiteDatabase db, String table, String normalizedInput) {
        Cursor cursor = null;
        try {
            String query = "SELECT " + COL_SORA + ", " + COL_AYA + ", " + COL_P_SORA
                    + " FROM " + table;
            cursor = db.rawQuery(query, null);
            if (cursor == null) return null;

            while (cursor.moveToNext()) {
                String dbText = cursor.getString(2); // aya_text est col index 2 dans ce SELECT
                // Reselect with explicit columns
                // We'll compare after normalizing db text
                break;
            }
            cursor.close();

            // Proper query with explicit column selection
            String q2 = "SELECT " + COL_SORA + ", " + COL_AYA + ", " + COL_P_SORA
                    + ", " + COL_AYA_TEXT
                    + " FROM " + table;
            cursor = db.rawQuery(q2, null);
            if (cursor == null) return null;

            while (cursor.moveToNext()) {
                int soraIdx  = cursor.getColumnIndex(COL_SORA);
                int ayaIdx   = cursor.getColumnIndex(COL_AYA);
                int pSoraIdx = cursor.getColumnIndex(COL_P_SORA);
                int textIdx  = cursor.getColumnIndex(COL_AYA_TEXT);

                String dbAyaText = cursor.getString(textIdx);
                String normalizedDb = normalize(dbAyaText);

                if (normalizedDb.equals(normalizedInput)) {
                    int sora = cursor.getInt(soraIdx);
                    int aya  = cursor.getInt(ayaIdx);
                    int page = cursor.getInt(pSoraIdx);
                    cursor.close();
                    return new int[]{sora, aya, page};
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "searchExact error", e);
        } finally {
            if (cursor != null && !cursor.isClosed()) cursor.close();
        }
        return null;
    }

    private static int[] searchPartial(SQLiteDatabase db, String table, String normalizedInput) {
        // Prend les 4 premiers mots du texte normalisé comme "clé de recherche"
        String[] words = normalizedInput.split("\\s+");
        if (words.length == 0) return null;

        // Construire un fragment pour la recherche LIKE
        int keyWordCount = Math.min(4, words.length);
        StringBuilder fragment = new StringBuilder();
        for (int i = 0; i < keyWordCount; i++) {
            if (words[i].length() > 1) {
                fragment.append(words[i]);
                if (i < keyWordCount - 1) fragment.append(" ");
            }
        }
        String key = fragment.toString().trim();
        if (key.isEmpty()) return null;

        Cursor cursor = null;
        int bestSora = -1, bestAya = -1, bestPage = -1;
        int bestScore = 0;

        try {
            String q = "SELECT " + COL_SORA + ", " + COL_AYA + ", " + COL_P_SORA
                    + ", " + COL_AYA_TEXT
                    + " FROM " + table;
            cursor = db.rawQuery(q, null);
            if (cursor == null) return null;

            int soraIdx  = cursor.getColumnIndex(COL_SORA);
            int ayaIdx   = cursor.getColumnIndex(COL_AYA);
            int pSoraIdx = cursor.getColumnIndex(COL_P_SORA);
            int textIdx  = cursor.getColumnIndex(COL_AYA_TEXT);

            while (cursor.moveToNext()) {
                String dbAyaText = cursor.getString(textIdx);
                String normalizedDb = normalize(dbAyaText);

                // Score = nombre de mots du texte reconnu présents dans l'aya
                int score = countCommonWords(normalizedInput, normalizedDb);
                if (score > bestScore) {
                    bestScore = score;
                    bestSora  = cursor.getInt(soraIdx);
                    bestAya   = cursor.getInt(ayaIdx);
                    bestPage  = cursor.getInt(pSoraIdx);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "searchPartial error", e);
        } finally {
            if (cursor != null && !cursor.isClosed()) cursor.close();
        }

        if (bestSora > 0 && bestScore >= Math.min(3, words.length)) {
            return new int[]{bestSora, bestAya, bestPage};
        }
        return null;
    }

    /** Compte le nombre de mots de {@code input} présents dans {@code reference}. */
    private static int countCommonWords(String input, String reference) {
        String[] inputWords = input.split("\\s+");
        int count = 0;
        for (String w : inputWords) {
            if (w.length() > 1 && reference.contains(w)) {
                count++;
            }
        }
        return count;
    }

    // ------------------------------------------------------------------ //
    //  getAyaText
    // ------------------------------------------------------------------ //

    /**
     * Retourne le texte brut (avec harakât) d'une aya donnée.
     *
     * @param db     La base ouverte.
     * @param sora   Numéro de sourate (1-114).
     * @param aya    Numéro d'aya.
     * @param isHafs true = Hafs, false = Warsh.
     * @return Texte de l'aya, ou null si introuvable.
     */
    public static String getAyaText(SQLiteDatabase db, int sora, int aya, boolean isHafs) {
        if (db == null) return null;
        String table = isHafs ? TABLE_HAFS : TABLE_WARSH;

        Cursor cursor = null;
        try {
            String q = "SELECT " + COL_AYA_TEXT
                    + " FROM " + table
                    + " WHERE " + COL_SORA + "=? AND " + COL_AYA + "=?"
                    + " LIMIT 1";
            cursor = db.rawQuery(q, new String[]{String.valueOf(sora), String.valueOf(aya)});
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getString(0);
            }
        } catch (Exception e) {
            Log.e(TAG, "getAyaText error", e);
        } finally {
            if (cursor != null && !cursor.isClosed()) cursor.close();
        }
        return null;
    }

    // ------------------------------------------------------------------ //
    //  getAyaPage
    // ------------------------------------------------------------------ //

    /**
     * Retourne le numéro de page (mushaf) d'une aya.
     * Utilise la colonne p_sora de tb_tafcir (Hafs par défaut).
     *
     * @param db   La base ouverte.
     * @param sora Numéro de sourate.
     * @param aya  Numéro d'aya.
     * @return Numéro de page, ou -1 si introuvable.
     */
    public static int getAyaPage(SQLiteDatabase db, int sora, int aya) {
        if (db == null) return -1;

        Cursor cursor = null;
        try {
            String q = "SELECT " + COL_P_SORA
                    + " FROM " + TABLE_HAFS
                    + " WHERE " + COL_SORA + "=? AND " + COL_AYA + "=?"
                    + " LIMIT 1";
            cursor = db.rawQuery(q, new String[]{String.valueOf(sora), String.valueOf(aya)});
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getInt(0);
            }
        } catch (Exception e) {
            Log.e(TAG, "getAyaPage error", e);
        } finally {
            if (cursor != null && !cursor.isClosed()) cursor.close();
        }
        return -1;
    }

    // ------------------------------------------------------------------ //
    //  Utilitaire : nom de sourate (depuis tb_sora)
    // ------------------------------------------------------------------ //

    /**
     * Retourne le nom arabe de la sourate depuis tb_sora.
     *
     * @param db   La base ouverte.
     * @param sora Numéro de sourate (1-114).
     * @return Nom de la sourate ou chaîne vide si introuvable.
     */
    public static String getSoraName(SQLiteDatabase db, int sora) {
        if (db == null) return "";
        Cursor cursor = null;
        try {
            // t_sora = nom arabe de la sourate
            String q = "SELECT t_sora FROM tb_sora WHERE id_sora=? LIMIT 1";
            cursor = db.rawQuery(q, new String[]{String.valueOf(sora)});
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getString(0);
            }
        } catch (Exception e) {
            Log.e(TAG, "getSoraName error", e);
        } finally {
            if (cursor != null && !cursor.isClosed()) cursor.close();
        }
        return "";
    }

    /**
     * Retourne le nombre d'ayat d'une sourate (en comptant les lignes dans tb_tafcir).
     */
    public static int getAyaCount(SQLiteDatabase db, int sora, boolean isHafs) {
        if (db == null) return 0;
        String table = isHafs ? TABLE_HAFS : TABLE_WARSH;
        Cursor cursor = null;
        try {
            String q = "SELECT COUNT(*) FROM " + table + " WHERE " + COL_SORA + "=?";
            cursor = db.rawQuery(q, new String[]{String.valueOf(sora)});
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getInt(0);
            }
        } catch (Exception e) {
            Log.e(TAG, "getAyaCount error", e);
        } finally {
            if (cursor != null && !cursor.isClosed()) cursor.close();
        }
        return 0;
    }
}
