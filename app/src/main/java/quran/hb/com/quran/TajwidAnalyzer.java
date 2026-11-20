package quran.hb.com.quran;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Moteur d'analyse des règles de Tajwid.
 *
 * Prend le texte reconnu (voix) et le texte de référence (aya), retourne la liste
 * des observations Tajwid avec leur sévérité, et calcule un score /10.
 *
 * Règles implémentées :
 *  1. Noon Sakin / Tanwin : Izhar, Idgham bighunna, Idgham bila ghunna, Iqlab, Ikhfa
 *  2. Mim Sakin           : Idgham Mithlayn, Ikhfa Shafawi, Izhar Shafawi
 *  3. Madd                : Tabi'i, Wajib Muttasil, Ja'iz Munfasil, 'Arid lissukun
 *  4. Qalqala             : Sughra (وسط) et Kubra (وقفاً)
 *  5. Ghunna              : Noon/Mim Moshaddada
 *  6. Tafkhim / Tarqiq    : lettres épaisses (خ غ ص ض ط ظ ق)
 */
public class TajwidAnalyzer {

    private static final String TAG = "TajwidAnalyzer";

    // ------------------------------------------------------------------ //
    //  Constantes Unicode des lettres arabes
    // ------------------------------------------------------------------ //

    // Harakât et signes diacritiques
    private static final char FATHA      = '\u064E'; // َ
    private static final char DAMMA      = '\u064F'; // ُ
    private static final char KASRA      = '\u0650'; // ِ
    private static final char TANWIN_F   = '\u064B'; // ً
    private static final char TANWIN_D   = '\u064C'; // ٌ
    private static final char TANWIN_K   = '\u064D'; // ٍ
    private static final char SHADDA     = '\u0651'; // ّ
    private static final char SUKUN      = '\u0652'; // ْ
    private static final char MADDAH     = '\u0653'; // ٓ

    // Lettres arabes
    private static final char ALEF       = '\u0627'; // ا
    private static final char WAW        = '\u0648'; // و
    private static final char YA         = '\u064A'; // ي
    private static final char NOON       = '\u0646'; // ن
    private static final char MIM        = '\u0645'; // م
    private static final char BA         = '\u0628'; // ب
    private static final char LAM        = '\u0644'; // ل
    private static final char RA         = '\u0631'; // ر
    private static final char HAMZA      = '\u0621'; // ء
    private static final char HEH        = '\u0647'; // ه
    private static final char AYN        = '\u0639'; // ع
    private static final char HEH_HUTA   = '\u062D'; // ح
    private static final char GHAIN      = '\u063A'; // غ
    private static final char KHA        = '\u062E'; // خ
    private static final char QAF        = '\u0642'; // ق
    private static final char THA_HEAVY  = '\u0637'; // ط
    private static final char THA2       = '\u0638'; // ظ
    private static final char SAD        = '\u0635'; // ص
    private static final char DAD        = '\u0636'; // ض
    private static final char QAF2       = '\u0642'; // ق (same as QAF)
    private static final char TAA        = '\u062A'; // ت — pour Qalqala (non incluse dans قطبجد mais ajoutée dans certaines écoles)
    private static final char JEEM       = '\u062C'; // ج
    private static final char DAL        = '\u062F'; // د

    // Lettres Izhar halqi (حلقية)
    private static final String IZHAR_LETTERS   = "\u0621\u0647\u0639\u062D\u063A\u062E"; // ء ه ع ح غ خ

    // Lettres Idgham bighunna (يومن)
    private static final String IDGHAM_B_LETTERS = "\u064A\u0646\u0645\u0648"; // ي ن م و

    // Lettres Idgham bila ghunna (لر)
    private static final String IDGHAM_BL_LETTERS = "\u0644\u0631"; // ل ر

    // Lettre Iqlab
    private static final char IQLAB_LETTER = '\u0628'; // ب

    // Lettres Qalqala : ق ط ب ج د
    private static final String QALQALA_LETTERS = "\u0642\u0637\u0628\u062C\u062F";

    // Lettres Tafkhim (emphatiques)
    private static final String TAFKHIM_LETTERS = "\u062E\u063A\u0635\u0636\u0637\u0638\u0642"; // خ غ ص ض ط ظ ق

    // ------------------------------------------------------------------ //
    //  API publique
    // ------------------------------------------------------------------ //

    /**
     * Analyse le texte reconnu par rapport au texte de référence.
     *
     * @param recognizedText  Texte issu de la reconnaissance vocale.
     * @param referenceText   Texte de l'aya de référence (avec harakât idéalement).
     * @param isHafs          true = riwaya Hafs, false = Warsh.
     * @return Liste de TajwidRule observées.
     */
    public List<TajwidRule> analyze(String recognizedText, String referenceText, boolean isHafs) {
        List<TajwidRule> results = new ArrayList<>();

        if (referenceText == null || referenceText.isEmpty()) {
            return results;
        }

        String ref = referenceText.trim();

        // --- Analyses sur le texte de RÉFÉRENCE (toujours) ---
        analyzeNoonSakin(ref, results);
        analyzeMimSakin(ref, results);
        analyzeMadd(ref, results);
        analyzeQalqala(ref, results);
        analyzeGhunna(ref, results);
        analyzeTafkhim(ref, results);

        // --- Si texte reconnu fourni : comparer mot à mot ---
        if (recognizedText != null && !recognizedText.trim().isEmpty()) {
            compareWords(recognizedText.trim(), ref, results);
        }

        return results;
    }

    /**
     * Calcule un score /10 basé sur la liste de règles observées.
     *
     * Chaque erreur (severity=2) enlève 1 point, chaque avertissement (severity=1) enlève 0.5.
     * Le score minimum est 0.
     */
    public int calculateScore(List<TajwidRule> rules) {
        if (rules == null || rules.isEmpty()) return 10;

        double score = 10.0;
        for (TajwidRule rule : rules) {
            switch (rule.getSeverity()) {
                case TajwidRule.SEVERITY_ERROR:
                    score -= 1.0;
                    break;
                case TajwidRule.SEVERITY_WARNING:
                    score -= 0.5;
                    break;
                default:
                    break;
            }
        }
        return (int) Math.max(0, Math.round(score));
    }

    // ------------------------------------------------------------------ //
    //  1. Noon Sakin / Tanwin
    // ------------------------------------------------------------------ //

    private void analyzeNoonSakin(String text, List<TajwidRule> out) {
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            boolean isNoonSakin = (c == NOON && i + 1 < text.length() && text.charAt(i + 1) == SUKUN);
            boolean isTanwin    = (c == TANWIN_F || c == TANWIN_D || c == TANWIN_K);

            if (!isNoonSakin && !isTanwin) continue;

            // Chercher la prochaine lettre significative
            int nextIdx = i + (isNoonSakin ? 2 : 1);
            // Sauter les harakât
            while (nextIdx < text.length() && isDiacritic(text.charAt(nextIdx))) nextIdx++;

            if (nextIdx >= text.length()) continue;
            char next = text.charAt(nextIdx);

            String context = String.valueOf(c);

            if (IZHAR_LETTERS.indexOf(next) >= 0) {
                // Izhar halqi : ء ه ع ح غ خ
                out.add(new TajwidRule(
                        "إظهار حلقي",
                        "Izhar Halqi – prononciation distincte",
                        "النون الساكنة أو التنوين قبل حرف حلقي تُظهر بدون غنة",
                        i, TajwidRule.SEVERITY_OK, null, null));
            } else if (IDGHAM_B_LETTERS.indexOf(next) >= 0) {
                // Idgham bighunna : ي ن م و
                out.add(new TajwidRule(
                        "إدغام بغنة",
                        "Idgham bighunna – assimilation avec nasalisation",
                        "النون الساكنة أو التنوين تُدغم في الحرف التالي مع إبقاء الغنة",
                        i, TajwidRule.SEVERITY_OK, null, null));
            } else if (IDGHAM_BL_LETTERS.indexOf(next) >= 0) {
                // Idgham bila ghunna : ل ر
                out.add(new TajwidRule(
                        "إدغام بلا غنة",
                        "Idgham bila ghunna – assimilation sans nasalisation",
                        "النون الساكنة أو التنوين تُدغم في الحرف التالي بدون غنة",
                        i, TajwidRule.SEVERITY_OK, null, null));
            } else if (next == IQLAB_LETTER) {
                // Iqlab : ب
                out.add(new TajwidRule(
                        "إقلاب",
                        "Iqlab – conversion en Mim",
                        "النون الساكنة أو التنوين تُقلب ميماً مخفاة عند حرف الباء",
                        i, TajwidRule.SEVERITY_OK, null, null));
            } else {
                // Ikhfa : 15 lettres restantes
                out.add(new TajwidRule(
                        "إخفاء حقيقي",
                        "Ikhfa – prononciation cachée",
                        "النون الساكنة أو التنوين تُخفى مع الغنة عند باقي الحروف",
                        i, TajwidRule.SEVERITY_OK, null, null));
            }
        }
    }

    // ------------------------------------------------------------------ //
    //  2. Mim Sakin
    // ------------------------------------------------------------------ //

    private void analyzeMimSakin(String text, List<TajwidRule> out) {
        for (int i = 0; i < text.length() - 1; i++) {
            char c = text.charAt(i);
            if (c != MIM) continue;

            // Vérifier que c'est un Mim sakin (suivi de sukun ou fin de mot)
            boolean mimSakin = false;
            int afterMim = i + 1;
            if (afterMim < text.length() && text.charAt(afterMim) == SUKUN) {
                mimSakin = true;
                afterMim++;
            } else {
                // Fin de mot (espace ou début de nouveau mot)
                if (afterMim < text.length() && text.charAt(afterMim) == ' ') {
                    mimSakin = true;
                }
            }
            if (!mimSakin) continue;

            // Chercher la prochaine lettre
            while (afterMim < text.length() && (isDiacritic(text.charAt(afterMim)) || text.charAt(afterMim) == ' '))
                afterMim++;

            if (afterMim >= text.length()) continue;
            char next = text.charAt(afterMim);

            if (next == MIM) {
                out.add(new TajwidRule(
                        "إدغام مثلين صغير",
                        "Idgham Mithlayn – assimilation de deux Mim",
                        "الميم الساكنة تُدغم في الميم التالية مع الغنة",
                        i, TajwidRule.SEVERITY_OK, null, null));
            } else if (next == BA) {
                out.add(new TajwidRule(
                        "إخفاء شفوي",
                        "Ikhfa Shafawi – occultation labiale",
                        "الميم الساكنة تُخفى عند الباء مع الغنة",
                        i, TajwidRule.SEVERITY_OK, null, null));
            } else {
                out.add(new TajwidRule(
                        "إظهار شفوي",
                        "Izhar Shafawi – prononciation labiale distincte",
                        "الميم الساكنة تُظهر عند باقي الحروف بدون غنة",
                        i, TajwidRule.SEVERITY_OK, null, null));
            }
        }
    }

    // ------------------------------------------------------------------ //
    //  3. Madd
    // ------------------------------------------------------------------ //

    private void analyzeMadd(String text, List<TajwidRule> out) {
        for (int i = 0; i < text.length() - 1; i++) {
            char c = text.charAt(i);

            // Madd tabi'i de base : alef après fatha, waw avec damma, ya avec kasra
            boolean isMaddAlef = (c == ALEF && i > 0 && text.charAt(i - 1) == FATHA);
            boolean isMaddWaw  = (c == WAW  && i > 0 && text.charAt(i - 1) == DAMMA);
            boolean isMaddYa   = (c == YA   && i > 0 && text.charAt(i - 1) == KASRA);

            if (!isMaddAlef && !isMaddWaw && !isMaddYa) continue;

            // Regarder ce qui suit pour catégoriser le Madd
            int next = i + 1;
            while (next < text.length() && isDiacritic(text.charAt(next))) next++;

            if (next >= text.length()) {
                // Fin du texte → 'Arid lissukun (2-6 comptes)
                out.add(new TajwidRule(
                        "مد عارض للسكون",
                        "Madd 'Arid lissukun (2-6 comptes)",
                        "حرف المد في آخر الكلمة عند الوقف يمد 2 أو 4 أو 6 حركات",
                        i, TajwidRule.SEVERITY_OK, null, null));
                continue;
            }

            char nextChar = text.charAt(next);

            // Hamza dans le même mot → Wajib Muttasil (4-5 comptes)
            if (nextChar == HAMZA && !isStartOfWord(text, i)) {
                out.add(new TajwidRule(
                        "مد واجب متصل",
                        "Madd Wajib Muttasil (4-5 comptes)",
                        "حرف المد تلاه همزة في نفس الكلمة، يجب المد 4 أو 5 حركات",
                        i, TajwidRule.SEVERITY_OK, null, null));
            } else if (nextChar == HAMZA && isStartOfWord(text, i)) {
                // Hamza dans le mot suivant → Ja'iz Munfasil (2-5 comptes)
                out.add(new TajwidRule(
                        "مد جائز منفصل",
                        "Madd Ja'iz Munfasil (2-5 comptes)",
                        "حرف المد تلاه همزة في الكلمة التالية، يجوز المد من 2 إلى 5 حركات",
                        i, TajwidRule.SEVERITY_OK, null, null));
            } else {
                // Madd tabi'i (2 comptes)
                out.add(new TajwidRule(
                        "مد طبيعي",
                        "Madd Tabi'i (2 comptes)",
                        "المد الأصلي لحرف المد بمقدار حركتين",
                        i, TajwidRule.SEVERITY_OK, null, null));
            }
        }
    }

    private boolean isStartOfWord(String text, int pos) {
        for (int i = pos - 1; i >= 0; i--) {
            if (text.charAt(i) == ' ') return true;
            if (!isDiacritic(text.charAt(i))) return false;
        }
        return true;
    }

    // ------------------------------------------------------------------ //
    //  4. Qalqala
    // ------------------------------------------------------------------ //

    private void analyzeQalqala(String text, List<TajwidRule> out) {
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (QALQALA_LETTERS.indexOf(c) < 0) continue;

            // Qalqala si la lettre porte un sukun ou est en fin de mot
            boolean hasSukun = (i + 1 < text.length() && text.charAt(i + 1) == SUKUN);
            boolean endOfWord = (i + 1 >= text.length() || text.charAt(i + 1) == ' ' || text.charAt(i + 1) == '\n');

            if (!hasSukun && !endOfWord) continue;

            // Kubra si en fin de phrase / pause ; Sughra si milieu de phrase
            boolean isKubra = (i + 1 >= text.length() || endOfWord);
            String nameAr   = isKubra ? "قلقلة كبرى" : "قلقلة صغرى";
            String nameFr   = isKubra ? "Qalqala Kubra (pause)" : "Qalqala Sughra (milieu)";
            String desc     = isKubra
                    ? "حرف قلقلة ساكن في آخر الكلمة عند الوقف، يُقلقل بشدة"
                    : "حرف قلقلة ساكن في وسط الكلمة، يُقلقل خفيفاً";

            out.add(new TajwidRule(nameAr, nameFr, desc, i, TajwidRule.SEVERITY_OK, null, null));
        }
    }

    // ------------------------------------------------------------------ //
    //  5. Ghunna (noon / mim moshaddada)
    // ------------------------------------------------------------------ //

    private void analyzeGhunna(String text, List<TajwidRule> out) {
        for (int i = 0; i < text.length() - 1; i++) {
            char c = text.charAt(i);
            if (c != NOON && c != MIM) continue;
            // Chercher shadda sur cette lettre
            if (i + 1 < text.length() && text.charAt(i + 1) == SHADDA) {
                String nameAr = (c == NOON) ? "غنة النون المشددة" : "غنة الميم المشددة";
                String nameFr = (c == NOON) ? "Ghunna – Noon Moshaddada (2 comptes)" : "Ghunna – Mim Moshaddada (2 comptes)";
                out.add(new TajwidRule(
                        nameAr, nameFr,
                        "الحرف المشدد يُغن بمقدار حركتين",
                        i, TajwidRule.SEVERITY_OK, null, null));
            }
        }
    }

    // ------------------------------------------------------------------ //
    //  6. Tafkhim / Tarqiq
    // ------------------------------------------------------------------ //

    private void analyzeTafkhim(String text, List<TajwidRule> out) {
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (TAFKHIM_LETTERS.indexOf(c) < 0) continue;

            String letter = String.valueOf(c);
            String nameAr = "تفخيم " + letter;
            String nameFr = "Tafkhim – prononciation emphatique de " + letter;
            String desc   = "حرف " + letter + " يُفخَّم دائماً";

            out.add(new TajwidRule(nameAr, nameFr, desc, i, TajwidRule.SEVERITY_OK, null, null));
        }

        // Ra' (ر) : tafkhim ou tarqiq selon le contexte
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) != RA) continue;

            // Ra' tafkhim si précédé de fatha, damma, ou sukun après lettre épaisse
            boolean tafkhim = false;
            if (i > 0) {
                char prev = text.charAt(i - 1);
                if (prev == FATHA || prev == DAMMA || prev == TANWIN_F || prev == TANWIN_D) {
                    tafkhim = true;
                }
            }

            String nameAr = tafkhim ? "تفخيم الراء" : "ترقيق الراء";
            String nameFr = tafkhim ? "Tafkhim du Ra'" : "Tarqiq du Ra'";
            String desc   = tafkhim
                    ? "الراء مفخمة في هذا الموضع"
                    : "الراء مرققة في هذا الموضع";

            out.add(new TajwidRule(nameAr, nameFr, desc, i, TajwidRule.SEVERITY_OK, null, null));
        }
    }

    // ------------------------------------------------------------------ //
    //  Comparaison mot à mot (texte reconnu vs référence)
    // ------------------------------------------------------------------ //

    /**
     * Compare le texte reconnu mot à mot avec la référence et génère des règles
     * d'erreur/avertissement pour les divergences.
     */
    private void compareWords(String recognized, String reference, List<TajwidRule> out) {
        String normRec = QuranTextMatcher.normalize(recognized);
        String normRef = QuranTextMatcher.normalize(reference);

        String[] recWords = normRec.split("\\s+");
        String[] refWords = normRef.split("\\s+");

        int len = Math.min(recWords.length, refWords.length);
        for (int i = 0; i < len; i++) {
            if (!recWords[i].equals(refWords[i])) {
                // Calculer la distance de Levenshtein pour décider warning vs error
                int dist = levenshtein(recWords[i], refWords[i]);
                int sev = (dist <= 1) ? TajwidRule.SEVERITY_WARNING : TajwidRule.SEVERITY_ERROR;
                String nameAr = sev == TajwidRule.SEVERITY_WARNING ? "خطأ طفيف" : "خطأ في التلاوة";
                String nameFr = sev == TajwidRule.SEVERITY_WARNING ? "Légère divergence" : "Erreur de récitation";
                out.add(new TajwidRule(
                        nameAr, nameFr,
                        "الكلمة المنطوقة تختلف عن النص الصحيح",
                        i, sev, recWords[i], refWords[i]));
            }
        }

        // Mots manquants
        if (recWords.length < refWords.length) {
            out.add(new TajwidRule(
                    "نقص في التلاوة",
                    "Récitation incomplète",
                    "بعض الكلمات لم تُتلَ أو لم تُتعرَّف عليها",
                    len, TajwidRule.SEVERITY_WARNING, null, null));
        }
    }

    // ------------------------------------------------------------------ //
    //  Utilitaires
    // ------------------------------------------------------------------ //

    private boolean isDiacritic(char c) {
        return (c >= '\u064B' && c <= '\u065F') || c == '\u0670'
                || (c >= '\u06D6' && c <= '\u06DC')
                || (c >= '\u06DF' && c <= '\u06E4')
                || c == '\u06E7' || c == '\u06E8'
                || (c >= '\u06EA' && c <= '\u06ED');
    }

    /** Distance de Levenshtein entre deux chaînes courtes. */
    private int levenshtein(String a, String b) {
        int la = a.length(), lb = b.length();
        int[][] dp = new int[la + 1][lb + 1];
        for (int i = 0; i <= la; i++) dp[i][0] = i;
        for (int j = 0; j <= lb; j++) dp[0][j] = j;
        for (int i = 1; i <= la; i++) {
            for (int j = 1; j <= lb; j++) {
                int cost = (a.charAt(i - 1) == b.charAt(j - 1)) ? 0 : 1;
                dp[i][j] = Math.min(Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1), dp[i - 1][j - 1] + cost);
            }
        }
        return dp[la][lb];
    }
}
