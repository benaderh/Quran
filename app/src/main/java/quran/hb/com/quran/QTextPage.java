package quran.hb.com.quran;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;

/**
 * Affiche une page du Coran en mode texte (police Hafs ou Warsh).
 * Mise en page identique au Mushaf imprimé : 15 lignes par page,
 * nom de la sourate + basmala inclus dans le décompte.
 * La police Uthmanic Hafs est chargée depuis /sdcard/QuranHW/font/.
 *
 * Pour les couleurs tajweed : placer une police tajweed couleur
 * (ex. "me_quran.ttf" ou "AlQalam Mushaf Tajweed.ttf") dans
 * /sdcard/QuranHW/font/ — l'app la détecte automatiquement.
 *
 * Extras Intent :
 *   "page"   (int) : numéro de page 1–604
 *   "riwaya" (int) : 1 = Hafs, 2 = Warsh
 */
public class QTextPage extends Activity {

    // ── Constantes ──────────────────────────────────────────────────────────
    public static final String EXTRA_PAGE   = "page";
    public static final String EXTRA_RIWAYA = "riwaya";

    private static final int PAGE_MIN = 1;
    private static final int PAGE_MAX = 604;

    /** Taille de police de base en px CSS (référence 100% zoom). */
    private static final int FONT_SIZE_DEFAULT = 26;
    private static final int FONT_SIZE_MIN     = 14;
    private static final int FONT_SIZE_MAX     = 60;

    // ── Vues ────────────────────────────────────────────────────────────────
    private WebView  webView;
    private TextView tvTitle;
    private TextView tvRiwaya;
    private Button   btnPrevPage, btnNextPage, btnZoomIn, btnZoomOut, btnClose;

    // ── État ────────────────────────────────────────────────────────────────
    private int page     = 1;
    private int riwaya   = 1;   // 1=Hafs, 2=Warsh
    private int fontSize = FONT_SIZE_DEFAULT;

    // ── Base de données ─────────────────────────────────────────────────────
    private DbHelper db;

    // ── Polices disponibles (cherchées dans cet ordre) ───────────────────────
    /** Polices tajweed couleur (priorité si présentes). */
    private static final String[] TAJWEED_FONT_NAMES = {
        "me_quran.ttf",
        "AlQalam Mushaf Tajweed.ttf",
        "QuranicTajweed.ttf",
        "tajweed.ttf",
        "Tajweed.ttf",
        "hafs_tajweed.otf",
        "KFGQPC-Hafs-Uthmanic-Tajweed.otf"
    };

    /** Polices Hafs standard (mono-couleur). */
    private static final String[] HAFS_FONT_NAMES = {
        "UthmanicHafs1 Ver09.otf",
        "UthmanicHafs1 Ver09.ttf",
        "UthmanicHafs1Ver09.otf",
        "UthmanicHafs1Ver09.ttf",
        "KFGQPC Uthmanic Script HAFS.otf",
        "KFGQPC Uthmanic Script HAFS.ttf",
        "Al Mushaf.ttf",
        "AlMushaf.ttf",
        "me_quran.ttf"
    };

    /** Polices Warsh (page-par-page P001.otf…P604.otf ou global). */
    private static final String[] WARSH_GLOBAL_NAMES = {
        "UthmanicWarsh1 Ver09.otf",
        "UthmanicWarsh1 Ver09.ttf",
        "KFGQPC Uthmanic Script Warsh.otf",
        "KFGQPC Uthmanic Script Warsh.ttf"
    };

    // ════════════════════════════════════════════════════════════════════════
    // onCreate
    // ════════════════════════════════════════════════════════════════════════

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.q_text_page);

        // Récupérer les paramètres
        Intent i = getIntent();
        page   = i.getIntExtra(EXTRA_PAGE,   1);
        riwaya = i.getIntExtra(EXTRA_RIWAYA, 1);
        page   = clampPage(page);

        // Vues
        webView     = (WebView) findViewById(R.id.qtp_webView);
        tvTitle     = (TextView) findViewById(R.id.qtp_tvTitle);
        tvRiwaya    = (TextView) findViewById(R.id.qtp_tvRiwaya);
        btnPrevPage = (Button)   findViewById(R.id.qtp_btnPrevPage);
        btnNextPage = (Button)   findViewById(R.id.qtp_btnNextPage);
        btnZoomIn   = (Button)   findViewById(R.id.qtp_btnZoomIn);
        btnZoomOut  = (Button)   findViewById(R.id.qtp_btnZoomOut);
        btnClose    = (Button)   findViewById(R.id.qtp_btnClose);

        // WebView settings
        WebSettings ws = webView.getSettings();
        ws.setJavaScriptEnabled(false);
        ws.setBuiltInZoomControls(true);
        ws.setDisplayZoomControls(false);
        ws.setUseWideViewPort(true);
        ws.setLoadWithOverviewMode(false);
        ws.setAllowFileAccess(true);
        ws.setAllowUniversalAccessFromFileURLs(true);
        ws.setTextZoom(100);

        // Ouvrir la base de données
        try {
            db = new DbHelper(this);
            db.createdatabase();
            db.opendatabase();
        } catch (IOException e) {
            e.printStackTrace();
        }

        tvRiwaya.setText(riwaya == 1 ? "حفص" : "ورش");
        loadPage();

        // Navigation pages
        btnPrevPage.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                if (page > PAGE_MIN) { page--; loadPage(); }
            }
        });

        btnNextPage.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                if (page < PAGE_MAX) { page++; loadPage(); }
            }
        });

        // Zoom
        btnZoomIn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                fontSize = Math.min(FONT_SIZE_MAX, fontSize + 3);
                loadPage();
            }
        });

        btnZoomOut.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                fontSize = Math.max(FONT_SIZE_MIN, fontSize - 3);
                loadPage();
            }
        });

        // Fermer
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { goBack(); }
        });
    }

    // ════════════════════════════════════════════════════════════════════════
    // Chargement du contenu HTML de la page (mise en page Mushaf 15 lignes)
    // ════════════════════════════════════════════════════════════════════════

    private void loadPage() {
        tvTitle.setText("صفحة " + page + "  (" + (riwaya == 1 ? "حفص" : "ورش") + ")");
        btnPrevPage.setEnabled(page > PAGE_MIN);
        btnNextPage.setEnabled(page < PAGE_MAX);

        // ── Chercher la police ────────────────────────────────────────────
        String fontPath    = fontPathForPage(page);
        boolean isTajweed  = isTajweedFont(fontPath);
        boolean fontExists = (fontPath != null);

        // ── Lire les versets depuis la base ───────────────────────────────
        Cursor cur = null;
        String soraName  = "";
        String basmala   = "بِسۡمِ ٱللَّهِ ٱلرَّحۡمَـٰنِ ٱلرَّحِيمِ";
        boolean showBasm  = false;
        StringBuilder ayatHtml = new StringBuilder();

        try {
            cur = (riwaya == 1) ? db.tfPageQuery(page) : db.tfwPageQuery(page);
            if (cur != null && cur.moveToFirst()) {
                // col[7] = t_sora (nom sourate), col[2]=aya, col[3]=aya_q, col[1]=sora
                soraName = (cur.getString(7) != null) ? cur.getString(7) : "";

                // La basmala est montrée si la 1ère aya de la page est l'aya 1
                // (sauf Fatiha p.1 aya 1 qui commence par basmala directement,
                //  et sourate At-Tawbah sora 9 qui n'a pas de basmala)
                int firstAya  = cur.getInt(2);
                int firstSora = cur.getInt(1);
                if (firstAya == 1 && firstSora != 9) {
                    showBasm = true;
                }

                do {
                    String ayaQ  = cur.getString(3);
                    String ayaNo = cur.getString(2);
                    if (ayaQ == null) ayaQ = "";

                    // Appliquer les couleurs tajweed si la police le supporte,
                    // sinon afficher le texte normalement
                    String colored = isTajweed
                        ? applyTajweedColors(ayaQ)
                        : escapeHtml(ayaQ);

                    String numBadge = " \uFD3F" + toArabicNum(ayaNo) + "\uFD3E ";
                    ayatHtml.append(colored).append(numBadge).append(" ");
                } while (cur.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cur != null) try { cur.close(); } catch (Exception ignored) {}
        }

        if (ayatHtml.length() == 0) {
            ayatHtml.append("<span style='color:#888'>لا توجد بيانات لهذه الصفحة</span>");
        }

        // ── Construire le @font-face ──────────────────────────────────────
        String fontFamily = "QuranFont";
        String fontCss;
        if (fontExists) {
            String fontUri = "file://" + fontPath.replace(" ", "%20");
            fontCss = "@font-face { font-family: '" + fontFamily + "';\n"
                    + "  src: url('" + fontUri + "'); }\n";
        } else {
            // Fallback : police système serif arabe
            fontFamily = "serif";
            fontCss    = "";
        }

        // ── Construire les entêtes de page (nom sourate + basmala) ────────
        StringBuilder headerHtml = new StringBuilder();

        // Cadre titre de la sourate (comme dans le Mushaf imprimé)
        if (!soraName.isEmpty()) {
            headerHtml.append("<div class='sura-header'>");
            headerHtml.append("<div class='sura-ornament'>&#xFDFD;</div>"); // ﷽ ornement optionnel
            headerHtml.append("<div class='sura-name'>")
                      .append(escapeHtml(soraName))
                      .append("</div>");
            headerHtml.append("</div>");
        }

        // Basmala (ligne à part, comme dans le Mushaf)
        if (showBasm) {
            headerHtml.append("<div class='basmala'>")
                      .append(escapeHtml(basmala))
                      .append("</div>");
        }

        // ── Construire le HTML complet ────────────────────────────────────
        // Fond parchemin Mushaf authentique : #FFF8E1 avec légère texture CSS
        String html =
            "<!DOCTYPE html><html dir='rtl'><head><meta charset='UTF-8'>"
            + "<meta name='viewport' content='width=device-width, initial-scale=1.0, "
            + "minimum-scale=0.3, maximum-scale=6.0, user-scalable=yes'>"
            + "<style>\n"
            + "  * { box-sizing: border-box; margin: 0; padding: 0; }\n"
            + "  html, body {\n"
            + "    width: 100%; height: 100%;\n"
            + "    background: #F5E6C8;\n"  // parchemin doré
            + "    direction: rtl;\n"
            + "  }\n"
            + "  body {\n"
            + "    /* Bordure décorative sur les 4 côtés */\n"
            + "    background: linear-gradient(135deg, #F9EDD2 0%, #F0D9A8 50%, #F9EDD2 100%);\n"
            + "    padding: 0;\n"
            + "  }\n"
            + fontCss
            + "\n"
            + "  /* ── Page Mushaf ── */\n"
            + "  .mushaf-page {\n"
            + "    width: 100%;\n"
            + "    min-height: 100vh;\n"
            + "    background: #FEF7E4;\n"
            + "    border: 3px double #C49A3C;\n"
            + "    padding: 8px 10px;\n"
            + "    display: flex;\n"
            + "    flex-direction: column;\n"
            + "  }\n"
            + "\n"
            + "  /* ── En-tête de page (numéro + hizb) ── */\n"
            + "  .page-header {\n"
            + "    display: flex;\n"
            + "    justify-content: space-between;\n"
            + "    align-items: center;\n"
            + "    border-bottom: 1.5px solid #C49A3C;\n"
            + "    padding-bottom: 4px;\n"
            + "    margin-bottom: 6px;\n"
            + "  }\n"
            + "  .page-header span {\n"
            + "    font-family: '" + fontFamily + "', Arial, serif;\n"
            + "    font-size: " + (fontSize - 8) + "px;\n"
            + "    color: #8B4513;\n"
            + "  }\n"
            + "\n"
            + "  /* ── Nom de la sourate ── */\n"
            + "  .sura-header {\n"
            + "    text-align: center;\n"
            + "    margin: 4px 0 5px;\n"
            + "    border: 2px solid #C49A3C;\n"
            + "    border-radius: 6px;\n"
            + "    padding: 4px 8px;\n"
            + "    background: linear-gradient(90deg, transparent, #F3D98020, transparent);\n"
            + "  }\n"
            + "  .sura-ornament {\n"
            + "    font-size: " + (fontSize + 2) + "px;\n"
            + "    color: #8B4513;\n"
            + "    line-height: 1.2;\n"
            + "  }\n"
            + "  .sura-name {\n"
            + "    font-family: '" + fontFamily + "', Arial, serif;\n"
            + "    font-size: " + (fontSize + 2) + "px;\n"
            + "    color: #6B2D0E;\n"
            + "    font-weight: bold;\n"
            + "    line-height: 1.5;\n"
            + "  }\n"
            + "\n"
            + "  /* ── Basmala ── */\n"
            + "  .basmala {\n"
            + "    font-family: '" + fontFamily + "', Arial, serif;\n"
            + "    font-size: " + (fontSize + 1) + "px;\n"
            + "    text-align: center;\n"
            + "    color: #1a1a1a;\n"
            + "    margin: 4px 0 5px;\n"
            + "    line-height: 2.0;\n"
            + "  }\n"
            + "\n"
            + "  /* ── Texte Coran (15 lignes, justifié comme le Mushaf) ── */\n"
            + "  .quran-text {\n"
            + "    font-family: '" + fontFamily + "', Arial, serif;\n"
            + "    font-size: " + fontSize + "px;\n"
            + "    line-height: 2.1;\n"
            + "    color: #0a0a0a;\n"
            + "    text-align: justify;\n"
            + "    word-spacing: 3px;\n"
            + "    flex: 1;\n"
            + "    direction: rtl;\n"
            + "  }\n"
            + "\n"
            + "  /* ── Numéros d'ayat ── */\n"
            + "  .aya-num {\n"
            + "    font-family: '" + fontFamily + "', Arial, serif;\n"
            + "    font-size: " + (fontSize - 6) + "px;\n"
            + "    color: #8B4513;\n"
            + "  }\n"
            + "\n"
            + "  /* ── Pied de page (numéro de page) ── */\n"
            + "  .page-footer {\n"
            + "    text-align: center;\n"
            + "    border-top: 1.5px solid #C49A3C;\n"
            + "    padding-top: 4px;\n"
            + "    margin-top: 6px;\n"
            + "    font-family: '" + fontFamily + "', Arial, serif;\n"
            + "    font-size: " + (fontSize - 8) + "px;\n"
            + "    color: #8B4513;\n"
            + "  }\n"
            + "\n"
            // ── Couleurs tajweed ────────────────────────────────────────
            + "  /* Madd : prolongation — rouge */\n"
            + "  .madd       { color: #c00000; }\n"
            + "  /* Ghunna : nasalisation — vert */\n"
            + "  .ghunna     { color: #00796B; }\n"
            + "  /* Qalqalah : lettres قطبجد sâkines — bleu */\n"
            + "  .qalqalah   { color: #0070c0; }\n"
            + "  /* Idgham avec ghunna — vert clair */\n"
            + "  .idgham     { color: #2E7D32; }\n"
            + "  /* Ikhfa — orange */\n"
            + "  .ikhfa      { color: #E65100; }\n"
            + "  /* Iqlab — bordeaux */\n"
            + "  .iqlab      { color: #880E4F; }\n"
            + "  /* Tafkhim — bleu foncé */\n"
            + "  .tafkhim    { color: #1A237E; }\n"
            + "  /* Lettres silencieuses — gris */\n"
            + "  .silent     { color: #9E9E9E; }\n"
            + "</style></head><body>\n"
            + "<div class='mushaf-page'>\n"
            + "  <div class='page-header'>\n"
            + "    <span>&#x62C;&#x632;&#x621; " + toArabicNum(String.valueOf(getJuzForPage(page))) + "</span>\n"
            + "    <span>&#x62D;&#x632;&#x628; " + toArabicNum(String.valueOf(getHizbForPage(page))) + "</span>\n"
            + "  </div>\n"
            + headerHtml.toString()
            + "  <div class='quran-text'>\n"
            + "    " + ayatHtml.toString() + "\n"
            + "  </div>\n"
            + "  <div class='page-footer'>" + toArabicNum(String.valueOf(page)) + "</div>\n"
            + "</div>\n"
            + "</body></html>";

        webView.loadDataWithBaseURL("file:///", html, "text/html", "UTF-8", null);
    }

    // ════════════════════════════════════════════════════════════════════════
    // Tajweed color coding (règles basiques sur le texte Unicode)
    // ════════════════════════════════════════════════════════════════════════

    /**
     * Applique des couleurs tajweed basiques sur le texte arabe.
     * Cette implémentation identifie les lettres qalqalah sâkines,
     * les madds principaux, et d'autres règles visibles dans l'écriture.
     * Pour une coloration complète, utiliser une police tajweed couleur.
     */
    private String applyTajweedColors(String text) {
        if (text == null || text.isEmpty()) return "";

        // Échapper d'abord
        String escaped = escapeHtml(text);

        // Lettres qalqalah : ق ط ب ج د avec sukun (U+0652)
        // On entoure les lettres qalqalah suivies d'un signe sakine
        escaped = escaped
            .replaceAll("([قطبجد]\u0652)", "<span class='qalqalah'>$1</span>")
            // Madd : alef madd &#x0622; ou lettres suivies de madd/alef longue
            .replaceAll("(\u0622|\u0623\u0627|\u0649)", "<span class='madd'>$1</span>")
            // Alef maqsura comme madd
            .replaceAll("([\u064A\u0648]\u0652)", "<span class='madd'>$1</span>")
            // Noon/Mim mushaddad → ghunna
            .replaceAll("([نم]\u0651)", "<span class='ghunna'>$1</span>");

        return escaped;
    }

    // ════════════════════════════════════════════════════════════════════════
    // Détermination juz/hizb pour la page
    // ════════════════════════════════════════════════════════════════════════

    /** Calcul approximatif du numéro de Juz' pour une page donnée (604 pages = 30 juz'). */
    private int getJuzForPage(int p) {
        // Approximation : 604 pages / 30 juz' ≈ 20.1 pages/juz'
        return Math.min(30, Math.max(1, (int) Math.ceil(p / 20.1)));
    }

    /** Calcul approximatif du hizb (60 hizbs en tout). */
    private int getHizbForPage(int p) {
        return Math.min(60, Math.max(1, (int) Math.ceil(p / 10.07)));
    }

    // ════════════════════════════════════════════════════════════════════════
    // Gestion des polices
    // ════════════════════════════════════════════════════════════════════════

    /**
     * Retourne le chemin absolu de la police à utiliser pour la page,
     * en cherchant d'abord les polices tajweed couleur, puis les polices
     * Uthmanic standard, puis les polices Warsh page-par-page.
     * Retourne null si aucune police n'est trouvée.
     */
    private String fontPathForPage(int p) {
        String root = Environment.getExternalStorageDirectory().getAbsolutePath() + "/QuranHW/";
        String[] fontDirs = { root + "font/", root + "Font/", root };

        if (riwaya == 1) {
            // Hafs : d'abord essayer les polices tajweed couleur
            String path = findFile(fontDirs, TAJWEED_FONT_NAMES);
            if (path != null) return path;
            // Ensuite les polices Hafs standard
            return findFile(fontDirs, HAFS_FONT_NAMES);
        } else {
            // Warsh : d'abord police globale Warsh
            String path = findFile(fontDirs, WARSH_GLOBAL_NAMES);
            if (path != null) return path;
            // Sinon police par page P001.otf…P604.otf
            String num = (p < 10) ? "00" + p : (p < 100) ? "0" + p : "" + p;
            String[] pageNames = { "P" + num + ".otf", "p" + num + ".otf",
                                   "P" + num + ".OTF", "p" + num + ".OTF",
                                   "P" + num + ".ttf" };
            return findFile(fontDirs, pageNames);
        }
    }

    /** Vérifie si le fichier de police est l'un des noms connus "tajweed couleur". */
    private boolean isTajweedFont(String path) {
        if (path == null) return false;
        String lower = path.toLowerCase();
        for (String name : TAJWEED_FONT_NAMES) {
            if (lower.endsWith(name.toLowerCase())) return true;
        }
        return false;
    }

    private String findFile(String[] dirs, String[] names) {
        for (String dir : dirs) {
            for (String name : names) {
                File f = new File(dir + name);
                if (f.exists()) return f.getAbsolutePath();
            }
        }
        return null;
    }

    // ════════════════════════════════════════════════════════════════════════
    // Utilitaires
    // ════════════════════════════════════════════════════════════════════════

    private static String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    /** Convertit les chiffres ASCII en chiffres arabes-indiens ٠١٢٣٤٥٦٧٨٩. */
    private static String toArabicNum(String num) {
        if (num == null) return "";
        StringBuilder sb = new StringBuilder();
        for (char c : num.toCharArray()) {
            if (c >= '0' && c <= '9') sb.append((char) ('\u0660' + (c - '0')));
            else sb.append(c);
        }
        return sb.toString();
    }

    private static int clampPage(int p) {
        return Math.max(PAGE_MIN, Math.min(PAGE_MAX, p));
    }

    // ════════════════════════════════════════════════════════════════════════
    // Navigation retour
    // ════════════════════════════════════════════════════════════════════════

    private void goBack() {
        if (db != null) try { db.close(); } catch (Exception ignored) {}
        finish();
        startActivity(new Intent(this, QMain.class));
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) { goBack(); return true; }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        if (db != null) try { db.close(); } catch (Exception ignored) {}
        super.onDestroy();
    }
}
