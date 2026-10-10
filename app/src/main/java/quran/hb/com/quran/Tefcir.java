package quran.hb.com.quran;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Environment;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;

/**
 * Tefcir (Tafsir) Activity
 *
 * Table structure (SELECT * columns):
 *   tb_tafcir  : [0]tefcir_id [1]sora [2]aya [3]aya_q [4]aya_t [5]aya_text [6]p_sora [7]t_sora [8]line [9]point
 *   tb_tafcirW : [0]tefcir_id [1]sora [2]aya [3]aya_q [4]aya_t [5]aya_text [6]p_sora [7]t_sora [8]line [9]point [10]ayaW [11]eff
 *
 * Font files (external storage /QuranHW/):
 *   Hafs  : UthmanicHafs1 Ver09.otf
 *   Warsh : p001.otf … p604.otf  (p_sora = column 6, values 1-604)
 */
public class Tefcir extends Activity {

    DbHelper db;
    SQLiteDatabase SQLdb;
    Button btnAN, btnAP, btnM, btnP;
    TextView ayaT, soraT, tefcirT;
    ScrollView scrollV;

    int soraI, ayaI, lastR;
    int pro;
    Cursor curT;

    // ---------------------------------------------------------------
    // Font helpers
    // ---------------------------------------------------------------

    /** Build /QuranHW/p001.otf path for a given page (1-604) */
    private String warshFontPath(int page) {
        String name;
        if      (page < 10)  name = "p00" + page;
        else if (page < 100) name = "p0"  + page;
        else                 name = "p"   + page;
        return Environment.getExternalStorageDirectory().getAbsolutePath()
                + "/QuranHW/" + name + ".otf";
    }

    private void applyWarshFont(int page) {
        if (page < 1 || page > 604) return;
        try {
            File f = new File(warshFontPath(page));
            if (f.exists()) ayaT.setTypeface(Typeface.createFromFile(f));
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void applyHafsFont() {
        try {
            File f = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
                    + "/QuranHW/UthmanicHafs1 Ver09.otf");
            if (f.exists()) ayaT.setTypeface(Typeface.createFromFile(f));
        } catch (Exception e) { e.printStackTrace(); }
    }

    // ---------------------------------------------------------------
    // Display the current cursor row
    // ---------------------------------------------------------------
    private void displayCurrentRow() {
        if (curT == null || curT.isClosed()) return;
        try {
            if (curT.isBeforeFirst() || curT.isAfterLast()) return;

            // col[3]=aya_q (arabic text), col[4]=aya_t (tafsir), col[7]=t_sora (sora name)
            String ayaText   = curT.getString(3);
            String tafsirText = curT.getString(4);
            String soraName  = curT.getString(7);
            String ayaNum    = curT.getString(2);

            ayaT.setText(ayaText    != null ? ayaText    : "");
            tefcirT.setText(tafsirText != null ? tafsirText : "");
            soraT.setText("الآية " + (ayaNum != null ? ayaNum : "") + "   سورة " + (soraName != null ? soraName : ""));

            // Apply font - col[6] = p_sora (use getInt, not getString)
            if (lastR == 1) {
                applyHafsFont();
            } else {
                int page = curT.getInt(6); // p_sora column
                applyWarshFont(page);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ---------------------------------------------------------------
    // Find the cursor row matching soraI/ayaI
    // ---------------------------------------------------------------
    private void seekToAya(int targetSora, int targetAya) {
        if (curT == null || curT.isClosed()) return;
        try {
            curT.moveToFirst();
            do {
                if (curT.getInt(1) == targetSora && curT.getInt(2) == targetAya) return;
            } while (curT.moveToNext());
            // Not found: stay on first row
            curT.moveToFirst();
        } catch (Exception e) {
            e.printStackTrace();
            try { curT.moveToFirst(); } catch (Exception ignored) {}
        }
    }

    // ---------------------------------------------------------------
    // onCreate
    // ---------------------------------------------------------------
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tefcir);

        btnAP   = (Button)     findViewById(R.id.btnAP);
        btnAN   = (Button)     findViewById(R.id.btnAN);
        btnM    = (Button)     findViewById(R.id.btnM);
        btnP    = (Button)     findViewById(R.id.btnP);
        ayaT    = (TextView)   findViewById(R.id.tv_aya);
        soraT   = (TextView)   findViewById(R.id.tv_sora);
        tefcirT = (TextView)   findViewById(R.id.tv_tefcir);
        scrollV = (ScrollView) findViewById(R.id.scrollV);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // --- Open DB -----------------------------------------------
        try { db = new DbHelper(this); } catch (IOException e) { e.printStackTrace(); }
        try { db.createdatabase(); }    catch (IOException e) { e.printStackTrace(); }
        db.opendatabase();

        // --- Read saved position from tb_indice --------------------
        // col[1]=last_riwaya, col[10]=soraIndex, col[11]=ayaIndex, col[12]=sizei
        try {
            Cursor curI = db.inrawQuery("SELECT * FROM tb_indice", null);
            curI.moveToFirst();
            lastR = curI.getInt(1);
            soraI = curI.getInt(10);
            ayaI  = curI.getInt(11);
            pro   = curI.getInt(12);
            curI.close();
        } catch (Exception e) {
            e.printStackTrace();
            lastR = 1; soraI = 1; ayaI = 1; pro = 30;
        }

        ayaT.setTextSize(pro);

        // --- Load tafcir table and seek to aya ---------------------
        try {
            if (lastR == 1) {
                curT = db.tfrawQuery("SELECT * FROM tb_tafcir", null);
            } else {
                curT = db.tfwrawQuery("SELECT * FROM tb_tafcirW", null);
            }
            seekToAya(soraI, ayaI);
            displayCurrentRow();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // ---------------------------------------------------------------
        // Previous aya button
        // ---------------------------------------------------------------
        btnAP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (curT.isFirst()) curT.moveToLast();
                    else curT.moveToPrevious();
                    displayCurrentRow();
                    scrollV.smoothScrollTo(0, 0);
                } catch (Exception e) { e.printStackTrace(); }
            }
        });

        // ---------------------------------------------------------------
        // Next aya button
        // ---------------------------------------------------------------
        btnAN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (curT.isLast()) curT.moveToFirst();
                    else curT.moveToNext();
                    displayCurrentRow();
                    scrollV.smoothScrollTo(0, 0);
                } catch (Exception e) { e.printStackTrace(); }
            }
        });

        // ---------------------------------------------------------------
        // Decrease font size
        // ---------------------------------------------------------------
        btnM.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    pro = Math.max(20, pro - 5);
                    ayaT.setTextSize(pro);
                    SQLdb = openOrCreateDatabase("db_quran.sqlite", Context.MODE_PRIVATE, null);
                    SQLdb.execSQL("UPDATE tb_indice SET sizei = '" + pro + "'");
                } catch (Exception e) { e.printStackTrace(); }
            }
        });

        // ---------------------------------------------------------------
        // Increase font size
        // ---------------------------------------------------------------
        btnP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    pro = Math.min(70, pro + 5);
                    ayaT.setTextSize(pro);
                    SQLdb = openOrCreateDatabase("db_quran.sqlite", Context.MODE_PRIVATE, null);
                    SQLdb.execSQL("UPDATE tb_indice SET sizei = '" + pro + "'");
                } catch (Exception e) { e.printStackTrace(); }
            }
        });
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            this.finish();
            startActivity(new Intent(this, QMain.class));
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }
}
