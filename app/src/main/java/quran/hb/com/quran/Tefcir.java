package quran.hb.com.quran;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.IOException;

public class Tefcir extends Activity {

    DbHelper db;
    SQLiteDatabase SQLdb;
    Button btnAN, btnAP, btnM, btnP;
    TextView ayaT, soraT, tefcirT;
    //LinearLayout ll21, ll22;
    ScrollView scrollV;
    int soraI,ayaI,pageS, lastR;
    Cursor curT;
    int pro;
    String fontW;
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.tefcir);

        btnAP = (Button) findViewById(R.id.btnAP);
        btnAN = (Button) findViewById(R.id.btnAN);
        btnM = (Button) findViewById(R.id.btnM);
        btnP = (Button) findViewById(R.id.btnP);
        ayaT = (TextView) findViewById(R.id.tv_aya);
        soraT = (TextView) findViewById(R.id.tv_sora);
        tefcirT = (TextView) findViewById(R.id.tv_tefcir);
        scrollV = (ScrollView) findViewById(R.id.scrollV);
        //ayaT.setTextSize(pro*15/60);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

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

        db.opendatabase();

        Cursor curI;
        curI = db.inrawQuery("SELECT * FROM tb_indice", null);
        curI.moveToFirst();
        lastR = curI.getInt(1);
        soraI = curI.getInt(10);
        ayaI = curI.getInt(11);
        pro = curI.getInt(12);
        curI.close();
        //ayaT.setTextSize(pro*15/60);
        ayaT.setTextSize(pro);
        //db.opendatabase();
        if (lastR ==1) {
            curT = db.tfrawQuery("SELECT * FROM tb_tafcir", null);
            curT.moveToFirst();
            int j = 1;
            do {
                if (curT.getInt(1) == soraI && curT.getInt(2) == ayaI) {
                    //i = 366;
                    break;
                }
                curT.moveToNext();
                j++;
            } while (j < 6237);
            //Toast.makeText(getApplicationContext(), "s: "+soraI+"   a: "+ayaI+"   t: "+curT.getString(3) , Toast.LENGTH_SHORT).show();

            ayaT.setText(curT.getString(3));
            soraT.setText("ط§ظ„ط¢ظٹط© " + curT.getString(2) + "   ط³ظˆط±ط© " + curT.getString(7));
            tefcirT.setText(curT.getString(4));

            Typeface tf = Typeface.createFromAsset(getAssets(), "font/UthmanicHafs1 Ver09.otf");
            //Typeface tf = Typeface.createFromAsset(getAssets(), "font/UthmanicWarsh1 Ver05.otf");
            ayaT.setTypeface(tf);
        }else{
            curT = db.tfwrawQuery("SELECT * FROM tb_tafcirW", null);
            curT.moveToFirst();
            int j = 1;
            do {
                if (curT.getInt(1) == soraI && curT.getInt(2) == ayaI) {
                    //i = 366;
                    break;
                }
                curT.moveToNext();
                j++;
            } while (j < 6215);
            //Toast.makeText(getApplicationContext(), "s: "+soraI+"   a: "+ayaI+"   t: "+curT.getString(3) , Toast.LENGTH_SHORT).show();

            ayaT.setText(curT.getString(3));
            soraT.setText("ط§ظ„ط¢ظٹط© " + curT.getString(2) + "   ط³ظˆط±ط© " + curT.getString(7));
            tefcirT.setText(curT.getString(4));
            //fontW=curT.getString(6);
            pageS = Integer.valueOf(curT.getString(6));
            if (pageS<10) {fontW = "P00"+pageS;
            }else{ if (pageS<100) {fontW = "P0"+pageS;
            }else{ fontW = "P"+pageS;
            }}
            fontW="font/"+fontW+".otf";
            //fontW = ""+pageS;
            //Typeface tf = Typeface.createFromAsset(getAssets(), "font/UthmanicWarsh1 Ver05.otf");
            //Typeface tf = Typeface.createFromAsset(getAssets(), "font/P200.otf");
            Typeface tf = Typeface.createFromAsset(getAssets(), fontW);///"+fontW+".otf");
            ayaT.setTypeface(tf);
            //ayaT.setText(fontW);
        }

        btnAP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (curT.getInt(0)==1) curT.moveToLast(); else curT.moveToPrevious();
                ayaT.setText(curT.getString(3));
                soraT.setText("ط§ظ„ط¢ظٹط© " + curT.getString(2) + "   ط³ظˆط±ط© " + curT.getString(7));
                tefcirT.setText(curT.getString(4));

                if (lastR ==2) {
                    pageS = Integer.valueOf(curT.getString(6));
                    if (pageS<10) {fontW = "P00"+pageS;
                    }else{ if (pageS<100) {fontW = "P0"+pageS;
                    }else{ fontW = "P"+pageS;
                    }}
                    fontW="font/"+fontW+".otf";
                    //fontW = ""+pageS;
                    //Typeface tf = Typeface.createFromAsset(getAssets(), "font/UthmanicWarsh1 Ver05.otf");
                    //Typeface tf = Typeface.createFromAsset(getAssets(), "font/P200.otf");
                    Typeface tf = Typeface.createFromAsset(getAssets(), fontW);///"+fontW+".otf");
                    ayaT.setTypeface(tf);
                }
                //ayaB.setText(curT.getString(3));
                //tefcirB.setText(curT.getString(4));
                scrollV.smoothScrollTo(0,0);
                //scrollVb.smoothScrollTo(0,0);


            }
        });

        btnAN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (lastR ==1 && curT.getInt(0)==6236)
                    curT.moveToFirst();
                else if (lastR ==2 && curT.getInt(0)==6214)
                    curT.moveToFirst();
                else curT.moveToNext();

                ayaT.setText(curT.getString(3));
                soraT.setText("ط§ظ„ط¢ظٹط© " + curT.getString(2) + "   ط³ظˆط±ط© " + curT.getString(7));
                tefcirT.setText(curT.getString(4));
                //ayaB.setText(curT.getString(3));
                //tefcirB.setText(curT.getString(4));
                if (lastR ==2) {
                    pageS = Integer.valueOf(curT.getString(6));
                    if (pageS<10) {fontW = "P00"+pageS;
                    }else{ if (pageS<100) {fontW = "P0"+pageS;
                    }else{ fontW = "P"+pageS;
                    }}
                    fontW="font/"+fontW+".otf";
                    //fontW = ""+pageS;
                    //Typeface tf = Typeface.createFromAsset(getAssets(), "font/UthmanicWarsh1 Ver05.otf");
                    //Typeface tf = Typeface.createFromAsset(getAssets(), "font/P200.otf");
                    Typeface tf = Typeface.createFromAsset(getAssets(), fontW);///"+fontW+".otf");
                    ayaT.setTypeface(tf);
                }

                scrollV.smoothScrollTo(0,0);
                //scrollVb.smoothScrollTo(0,0);

            }
        });

        btnM.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                pro = pro - 5;
                if (pro==15) {pro=20;}
                ayaT.setTextSize(pro);
                SQLdb=openOrCreateDatabase("db_quran.sqlite", Context.MODE_PRIVATE, null);
                SQLdb.execSQL("UPDATE tb_indice SET sizei = '" + pro  + "'");
            }
        });

        btnP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                pro = pro + 5;
                if (pro==75) {pro=70;}
                ayaT.setTextSize(pro);
                SQLdb=openOrCreateDatabase("db_quran.sqlite", Context.MODE_PRIVATE, null);
                SQLdb.execSQL("UPDATE tb_indice SET sizei = '" + pro  + "'");
            }
        });

    }


    @Override
    public boolean onKeyUp( int keyCode, KeyEvent event )
    {
        if( keyCode == KeyEvent.KEYCODE_BACK )
        {
            //SQLdb = mHelper.openWritableDb();
            //SQLdb.execSQL("UPDATE tb_indice SET last_page = " + lastP + "");
            this.finish();
            startActivity(new Intent(this, QMain.class));
            return true;
        }
        //this.finish();
        return super.onKeyUp( keyCode, event );
    }



}


