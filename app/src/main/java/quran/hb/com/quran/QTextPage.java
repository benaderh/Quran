package quran.hb.com.quran;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
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
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Affiche une page du Coran en mode texte (police Hafs ou Warsh) avec zoom.
 *
 * Extras Intent :
 *   "page"   (int)  : numéro de page 1-604
 *   "riwaya" (int)  : 1 = Hafs, 2 = Warsh
 */
public class QTextPage extends Activity {

    // ── Constantes ──────────────────────────────────────────────────────────
    public static final String EXTRA_PAGE   = "page";
    public static final String EXTRA_RIWAYA = "riwaya";

    private static final int PAGE_MIN = 1;
    private static final int PAGE_MAX = 604;
    private static final int FONT_SIZE_DEFAULT = 28;
    private static final int FONT_SIZE_MIN     = 16;
    private static final int FONT_SIZE_MAX     = 60;

    // ── Vues ────────────────────────────────────────────────────────────────
    private WebView  webView;
    private TextView tvTitle;
    private TextView tvRiwaya;
    private Button   btnPrevPage, btnNextPage, btnZoomIn, btnZoomOut, btnClose;

    // ── État ────────────────────────────────────────────────────────────────
    private int page    = 1;
    private int riwaya  = 1;   // 1=Hafs, 2=Warsh
    private int fontSize = FONT_SIZE_DEFAULT;

    // ── Base de données ─────────────────────────────────────────────────────
    private DbHelper db;

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

        // WebView settings : zoom natif activé
        WebSettings ws = webView.getSettings();
        ws.setJavaScriptEnabled(false);
        ws.setBuiltInZoomControls(true);
        ws.setDisplayZoomControls(false); // masquer les +/- natifs (on a les nôtres)
        ws.setUseWideViewPort(true);
        ws.setLoadWithOverviewMode(false);
        // Autoriser l'accès aux fichiers locaux (police sur stockage externe)
        ws.setAllowFileAccess(true);
        ws.setAllowUniversalAccessFromFileURLs(true); // nécessaire pour @font-face file://
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

        // Afficher la page initiale
        loadPage();

        // ── Boutons navigation ───────────────────────────────────────────────
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

        // ── Zoom ────────────────────────────────────────────────────────────
        btnZoomIn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                fontSize = Math.min(FONT_SIZE_MAX, fontSize + 4);
                loadPage();
            }
        });

        btnZoomOut.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                fontSize = Math.max(FONT_SIZE_MIN, fontSize - 4);
                loadPage();
            }
        });

        // ── Fermer ──────────────────────────────────────────────────────────
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                goBack();
            }
        });
    }

    // ════════════════════════════════════════════════════════════════════════
    // Chargement du contenu HTML de la page
    // ════════════════════════════════════════════════════════════════════════

    private void loadPage() {
        tvTitle.setText("صفحة " + page + "  (" + (riwaya == 1 ? "حفص" : "ورش") + ")");
        btnPrevPage.setEnabled(page > PAGE_MIN);
        btnNextPage.setEnabled(page < PAGE_MAX);

        // Chemin de la police
        String fontPath = fontPathForPage(page);
        boolean fontExists = (fontPath != null);

        // Construire le texte HTML des versets
        StringBuilder ayat = new StringBuilder();
        Cursor cur = null;
        String soraName = "";
        try {
            if (riwaya == 1) {
                cur = db.tfPageQuery(page);
            } else {
                cur = db.tfwPageQuery(page);
            }
            if (cur != null && cur.moveToFirst()) {
                // col[3]=aya_q, col[2]=aya, col[7]=t_sora
                soraName = cur.getString(7) != null ? cur.getString(7) : "";
                do {
                    String ayaQ  = cur.getString(3);
                    String ayaNo = cur.getString(2);
                    if (ayaQ == null) ayaQ = "";
                    // Numéro d'ayat en EnclosedNumbers Unicode si disponible
                    String numBadge = " \uFD3F" + toArabicNum(ayaNo) + "\uFD3E ";
                    ayat.append(escapeHtml(ayaQ)).append(numBadge);
                } while (cur.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cur != null) try { cur.close(); } catch (Exception ignored) {}
        }

        if (ayat.length() == 0) {
            ayat.append("<span style='color:#888'>لا توجد بيانات لهذه الصفحة</span>");
        }

        // Construire l'URL de police pour @font-face
        String fontFace = "";
        if (fontExists) {
            // Utiliser file:// URI encodé
            String fontUri = "file://" + fontPath.replace(" ", "%20");
            fontFace = "@font-face { font-family: 'QuranFont'; src: url('" + fontUri + "'); }\n"
                     + ".quran { font-family: 'QuranFont', serif; }\n";
        } else {
            fontFace = ".quran { font-family: serif; }\n";
        }

        String html =
            "<!DOCTYPE html><html dir='rtl'><head><meta charset='UTF-8'>" +
            "<meta name='viewport' content='width=device-width, initial-scale=1.0, " +
            "minimum-scale=0.5, maximum-scale=5.0, user-scalable=yes'>" +
            "<style>" +
            "body { background:#FFF8E7; margin:0; padding:16px; direction:rtl; }" +
            fontFace +
            ".sora-title { color:#8B4513; font-size:18px; text-align:center; margin-bottom:10px; font-weight:bold; }" +
            ".quran { font-size:" + fontSize + "px; line-height:2.2; color:#1a1a1a; " +
            "text-align:justify; word-spacing:4px; }" +
            ".num { color:#8B4513; font-size:" + (fontSize - 6) + "px; }" +
            "</style></head><body>" +
            "<div class='sora-title'>" + escapeHtml(soraName) + "</div>" +
            "<div class='quran'>" + ayat + "</div>" +
            "</body></html>";

        webView.loadDataWithBaseURL("file:///", html, "text/html", "UTF-8", null);
    }

    // ════════════════════════════════════════════════════════════════════════
    // Helpers polices
    // ════════════════════════════════════════════════════════════════════════

    /** Retourne le chemin absolu du fichier de police pour la page donnée, ou null si absent. */
    private String fontPathForPage(int p) {
        String root = Environment.getExternalStorageDirectory().getAbsolutePath() + "/QuranHW/";
        String[] fontDirs = { root + "font/", root + "Font/", root };

        if (riwaya == 1) {
            // Hafs : une seule police
            String[] names = { "UthmanicHafs1 Ver09.otf", "UthmanicHafs1 Ver09.ttf",
                                "UthmanicHafs1Ver09.otf", "uthmanichafs1 ver09.otf" };
            return findFile(fontDirs, names);
        } else {
            // Warsh : police par page P001.otf … P604.otf
            String num = (p < 10) ? "00" + p : (p < 100) ? "0" + p : "" + p;
            String[] names = { "P" + num + ".otf", "p" + num + ".otf",
                                "P" + num + ".OTF", "p" + num + ".OTF" };
            return findFile(fontDirs, names);
        }
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
    // Helpers HTML
    // ════════════════════════════════════════════════════════════════════════

    private static String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    /** Convertit un numéro arabe (string) en chiffres arabes (٠١٢...) */
    private static String toArabicNum(String num) {
        if (num == null) return "";
        StringBuilder sb = new StringBuilder();
        for (char c : num.toCharArray()) {
            if (c >= '0' && c <= '9') sb.append((char)('\u0660' + (c - '0')));
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
