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

public class Tefcir extends Activity {

    DbHelper db;
    SQLiteDatabase SQLdb;
    Button btnAN, btnAP, btnM, btnP;
    TextView ayaT, soraT, tefcirT;
    ScrollView scrollV;
    int soraI, ayaI, pageS, lastR;
    Cursor curT;
    int pro;
    String fontW;

    // ---------------------------------------------------------------
    // Helper : build font path for Warsh from page number
    // Files: /storage/emulated/0/QuranHW/p001.otf … p604.otf
    // ---------------------------------------------------------------
    private String buildWarshFontPath(int page) {
        String name;
        if (page < 10)        name = "p00" + page;
        else if (page < 100)  name = "p0"  + page;
        else                  name = "p"   + page;
        return Environment.getExternalStorageDirectory()
                .getAbsolutePath() + "/QuranHW/" + name + ".otf";
    }

    // ---------------------------------------------------------------
    // Helper : apply Warsh Typeface safely
    // ---------------------------------------------------------------
    private void applyWarshFont(int page) {
        if (page <= 0 || page > 604) return;
        try {
            File f = new File(buildWarshFontPath(page));
            if (f.exists()) {
                Typeface tf = Typeface.createFromFile(f);
                ayaT.setTypeface(tf);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ---------------------------------------------------------------
    // Helper : apply Hafs Typeface safely
    // ---------------------------------------------------------------
    private void applyHafsFont() {
        try {
            File f = new File(Environment.getExternalStorageDirectory()
                    .getAbsolutePath() + "/QuranHW/UthmanicHafs1 Ver09.otf");
            if (f.exists()) {
                Typeface tf = Typeface.createFromFile(f);
                ayaT.setTypeface(tf);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ---------------------------------------------------------------
    // Helper : display current row of curT
    // ---------------------------------------------------------------
    private void displayCurrentRow() {
        try {
            if (curT == null || curT.isBeforeFirst() || curT.isAfterLast()) return;
            ayaT.setText(curT.getString(3));
            soraT.setText("الآية " + curT.getString(2) + "   سورة " + curT.getString(7));
            String tefcir = curT.getString(4);
            tefcirT.setText(tefcir != null ? tefcir : "");

            if (lastR == 2) {
                String colVal = curT.getString(6);
                if (colVal != null && !colVal.isEmpty()) {
                    pageS = Integer.parseInt(colVal.trim());
                    applyWarshFont(pageS);
                }
            } else {
                applyHafsFont();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ---------------------------------------------------------------
    // Find cursor position matching soraI / ayaI
    // Returns true if found, false otherwise (cursor left at first row)
    // ---------------------------------------------------------------
    private boolean seekToAya(int sora, int aya, int maxRows) {
        try {
            curT.moveToFirst();
            int j = 0;
            do {
                if (curT.getInt(1) == sora && curT.getInt(2) == aya) return true;
                j++;
            } while (curT.moveToNext() && j < maxRows);
            // Not found – go back to first row so something is shown
            curT.moveToFirst();
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tefcir);

        btnAP   = (Button)   findViewById(R.id.btnAP);
        btnAN   = (Button)   findViewById(R.id.btnAN);
        btnM    = (Button)   findViewById(R.id.btnM);
        btnP    = (Button)   findViewById(R.id.btnP);
        ayaT    = (TextView) findViewById(R.id.tv_aya);
        soraT   = (TextView) findViewById(R.id.tv_sora);
        tefcirT = (TextView) findViewById(R.id.tv_tefcir);
        scrollV = (ScrollView) findViewById(R.id.scrollV);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // --- Open database ----------------------------------------
        try { db = new DbHelper(this); } catch (IOException e) { e.printStackTrace(); }
        try { db.createdatabase(); }     catch (IOException e) { e.printStackTrace(); }
        db.opendatabase();

        // --- Read index -------------------------------------------
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

        // --- Load tafcir table ------------------------------------
        try {
            if (lastR == 1) {
                curT = db.tfrawQuery("SELECT * FROM tb_tafcir", null);
                seekToAya(soraI, ayaI, 6236);
            } else {
                curT = db.tfwrawQuery("SELECT * FROM tb_tafcirW", null);
                seekToAya(soraI, ayaI, 6215);
            }
            displayCurrentRow();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // ---------------------------------------------------------------
        // Button : Previous aya
        // ---------------------------------------------------------------
        btnAP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (curT.isFirst()) {
                        curT.moveToLast();
                    } else {
                        curT.moveToPrevious();
                    }
                    displayCurrentRow();
                    scrollV.smoothScrollTo(0, 0);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        // ---------------------------------------------------------------
        // Button : Next aya
        // ---------------------------------------------------------------
        btnAN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (curT.isLast()) {
                        curT.moveToFirst();
                    } else {
                        curT.moveToNext();
                    }
                    displayCurrentRow();
                    scrollV.smoothScrollTo(0, 0);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        // ---------------------------------------------------------------
        // Button : Decrease font size
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
        // Button : Increase font size
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
