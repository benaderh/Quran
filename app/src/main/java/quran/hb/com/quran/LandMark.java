package quran.hb.com.quran;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;

public class LandMark extends Activity {
    DbHelper db;
    SQLiteDatabase SQLdb;
    int lastP, ri1, ri2, ri3;
    String lastPt, rt1, rt2, rt3;
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.landmark);
        TextView tv_pc = (TextView) findViewById(R.id.tv_pc);

        Button btn_O1 = (Button) findViewById(R.id.btn_O1);
        TextView tv_p1 = (TextView) findViewById(R.id.tv_p1);
        Button btn_C1 = (Button) findViewById(R.id.btn_C1);

        Button btn_O2 = (Button) findViewById(R.id.btn_O2);
        TextView tv_p2 = (TextView) findViewById(R.id.tv_p2);
        Button btn_C2 = (Button) findViewById(R.id.btn_C2);

        Button btn_O3 = (Button) findViewById(R.id.btn_O3);
        TextView tv_p3 = (TextView) findViewById(R.id.tv_p3);
        Button btn_C3 = (Button) findViewById(R.id.btn_C3);

        try {
            db = new DbHelper(this);
        } catch (IOException e2) {
            e2.printStackTrace();
        }

        try {
            db.createdatabase();
        } catch (IOException e) {
            e.printStackTrace();
        }


        Cursor curI;
        db.opendatabase();
        curI = db.inrawQuery("SELECT * FROM tb_indice", null);
        curI.moveToFirst();
        lastP = curI.getInt(0);
        ri1=curI.getInt(2);
        rt1=curI.getString(3);
        ri2=curI.getInt(4);
        rt2=curI.getString(5);
        ri3=curI.getInt(6);
        rt3=curI.getString(7);
        curI.close();

        Cursor curQ;
        curQ = db.qrrawQuery("SELECT * FROM tb_q", null);
        curQ.moveToFirst();

        int j = 0;
        do {
            if (curQ.getInt(0) == lastP) {
                //lastPt = curQ.getString(3);
                break;
            }
            curQ.moveToNext();
            j++;
        } while (j < 609);
        lastPt = curQ.getString(3);

        tv_pc.setText("ط§ظ„طµظپط­ط© ط§ظ„ط­ط§ظ„ظٹط©\nط³ظˆط±ط© " + lastPt + "\nط§ظ„طµظپط­ط© " + lastP);
        tv_p1.setText("ط³ظˆط±ط© " + rt1 + "\nط§ظ„طµظپط­ط© " + ri1);
        tv_p2.setText("ط³ظˆط±ط© " + rt2 + "\nط§ظ„طµظپط­ط© " + ri2);
        tv_p3.setText("ط³ظˆط±ط© " + rt3 + "\nط§ظ„طµظپط­ط© " + ri3);

        btn_O1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                //Toast.makeText(getApplicationContext(), "ط§ظ„ظ‚ط§ط¦ظ…ط©", Toast.LENGTH_SHORT).show();
                SQLdb = db.openWritableDb();
                SQLdb.execSQL("UPDATE tb_indice SET last_page = " + ri1 + "");
                finish();
                Intent intent = new Intent(LandMark.this, QMain.class);
                startActivity(intent);
            }
        });

        btn_O2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                //Toast.makeText(getApplicationContext(), "ط§ظ„ظ‚ط§ط¦ظ…ط©", Toast.LENGTH_SHORT).show();
                SQLdb = db.openWritableDb();
                SQLdb.execSQL("UPDATE tb_indice SET last_page = " + ri2 + "");
                finish();
                Intent intent = new Intent(LandMark.this, QMain.class);
                startActivity(intent);
            }
        });

        btn_O3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                //Toast.makeText(getApplicationContext(), "ط§ظ„ظ‚ط§ط¦ظ…ط©", Toast.LENGTH_SHORT).show();
                SQLdb = db.openWritableDb();
                SQLdb.execSQL("UPDATE tb_indice SET last_page = " + ri3 + "");
                finish();
                Intent intent = new Intent(LandMark.this, QMain.class);
                startActivity(intent);
            }
        });

        btn_C1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                //Toast.makeText(getApplicationContext(), ""+lastPt, Toast.LENGTH_SHORT).show();
                SQLdb = db.openWritableDb();
                SQLdb.execSQL("UPDATE tb_indice SET ri1 = " + lastP + ", rt1 = '" + lastPt + "'");
                finish();
                Intent intent = new Intent(LandMark.this, QMain.class);
                startActivity(intent);
            }
        });

        btn_C2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                //Toast.makeText(getApplicationContext(), ""+lastPt, Toast.LENGTH_SHORT).show();
                SQLdb = db.openWritableDb();
                SQLdb.execSQL("UPDATE tb_indice SET ri2 = " + lastP + ", rt2 = '" + lastPt + "'");
                finish();
                Intent intent = new Intent(LandMark.this, QMain.class);
                startActivity(intent);
            }
        });

        btn_C3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                //Toast.makeText(getApplicationContext(), ""+lastPt, Toast.LENGTH_SHORT).show();
                SQLdb = db.openWritableDb();
                SQLdb.execSQL("UPDATE tb_indice SET ri3 = " + lastP + ", rt3 = '" + lastPt + "'");
                finish();
                Intent intent = new Intent(LandMark.this, QMain.class);
                startActivity(intent);
            }
        });

    }


    @Override
    public boolean onKeyUp( int keyCode, KeyEvent event )
    {
        if( keyCode == KeyEvent.KEYCODE_BACK )
        {
            //SQLdb = db.openWritableDb();
            //SQLdb.execSQL("UPDATE tb_indice SET last_page = " + lastP + "");
            this.finish();
            startActivity(new Intent(this, QMain.class));
            return true;
        }
        //this.finish();
        return super.onKeyUp( keyCode, event );
    }



}

