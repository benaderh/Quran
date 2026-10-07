package quran.hb.com.quran;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;

import java.io.File;
import java.io.IOException;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;

public class QMain extends Activity {

    private Utils utils;
    private QImage adapter;
    private ViewPager viewPager;
    private MediaPlayer ringTone;

    SQLiteDatabase SQLdb;
    Button btnM, btnA, btnT, btnH, btnW, btnF, btnJ, btnP, btnS, btnAbout, btnAP, btnAN, btnAPb, btnANb, btnRetour;
    TextView ayaT, tefcirT, ayaB, tefcirB;
    TextView cadre;
    LinearLayout ll21, ll22;
    ScrollView scrollV, scrollVb;
    DbHelper db;
    int lastP, lastR, pos, ayaI, lineI, soraI, line, point;
    int bar = 0;
    int ctlAudio = 0;
    String sora, Riwaya;
    int x1, y1;
    private int screenWidth;
    private int screenHeight;
    Cursor curQ, curT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.q_main);

        viewPager  = findViewById(R.id.vPager);
        utils      = new Utils(getApplicationContext());

        btnJ       = findViewById(R.id.btnJ);
        btnP       = findViewById(R.id.btnP);
        btnS       = findViewById(R.id.btnS);
        btnM       = findViewById(R.id.btnM);
        btnA       = findViewById(R.id.btnA);
        btnT       = findViewById(R.id.btnT);
        btnH       = findViewById(R.id.btnH);
        btnW       = findViewById(R.id.btnW);
        btnF       = findViewById(R.id.btnF);
        btnAbout   = findViewById(R.id.btnAbout);
        btnAP      = findViewById(R.id.btnAP);
        btnAN      = findViewById(R.id.btnAN);
        btnAPb     = findViewById(R.id.btnAPb);
        btnANb     = findViewById(R.id.btnANb);
        btnRetour  = findViewById(R.id.btnRetour);
        ayaT       = findViewById(R.id.tv_aya);
        tefcirT    = findViewById(R.id.tv_tefcir);
        ll21       = findViewById(R.id.ll21);
        ayaB       = findViewById(R.id.tv_ayab);
        tefcirB    = findViewById(R.id.tv_tefcirb);
        ll22       = findViewById(R.id.ll22);
        cadre      = findViewById(R.id.cadre);
        scrollV    = findViewById(R.id.scrollV);
        scrollVb   = findViewById(R.id.scrollVb);

        // Initialiser la base de données (petite, rapide)
        try {
            db = new DbHelper(this);
            db.createdatabase();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Demander la permission de lire le stockage externe si nécessaire
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11+ : besoin de MANAGE_EXTERNAL_STORAGE
            if (!Environment.isExternalStorageManager()) {
                try {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                    intent.addCategory("android.intent.category.DEFAULT");
                    intent.setData(Uri.parse(String.format("package:%s", getApplicationContext().getPackageName())));
                    startActivityForResult(intent, 2229);
                } catch (Exception e) {
                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                    startActivityForResult(intent, 2229);
                }
                return; // initPostLoad() sera appelé dans onActivityResult
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android 6-10 : besoin de READ_EXTERNAL_STORAGE
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                return; // initPostLoad() sera appelé dans onRequestPermissionsResult
            }
        }

        // Cacher l'écran de chargement et démarrer
        findViewById(R.id.loadingLayout).setVisibility(View.GONE);
        initPostLoad();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                findViewById(R.id.loadingLayout).setVisibility(View.GONE);
                initPostLoad();
            } else {
                Toast.makeText(this,
                        "Permission refusée. L'application nécessite l'accès au stockage.",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 2229) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager()) {
                    findViewById(R.id.loadingLayout).setVisibility(View.GONE);
                    initPostLoad();
                } else {
                    Toast.makeText(this,
                            "Permission 'Tous les fichiers' refusée.",
                            Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private void initPostLoad() {

        // === Vérifier que les fichiers images existent ===
        File imgDir = utils.getHafsImgDir(); // /storage/emulated/0/QuranHW/HI/
        File testImg = new File(imgDir, "p1.jpg");
        if (!testImg.exists()) {
            // Afficher un message d'erreur clair
            Toast.makeText(this,
                "Fichiers manquants !\n" +
                "Copiez vos images dans :\n" +
                imgDir.getAbsolutePath(),
                Toast.LENGTH_LONG).show();
            // Montrer aussi un TextView dans l'UI
            showMissingFilesMessage(imgDir.getAbsolutePath());
            return;
        }

        // Read indice from DB
        try {
            db.opendatabase();
        } catch (Exception e) {
            Log.e("QMain", "opendatabase error: " + e.getMessage());
        }
        Cursor curI = db.inrawQuery("SELECT * FROM tb_indice", null);
        curI.moveToFirst();
        lastP  = curI.getInt(0);
        lastR  = curI.getInt(1);
        line   = curI.getInt(8);
        point  = curI.getInt(9);
        curI.close();

        if (point == 4) point = 0;
        else if (point == 3) point = 1;
        else if (point == 2) point = 2;
        else point = 3;

        // Setup riwaya paths using app-private external storage
        if (lastR == 1) {
            Riwaya = utils.getHafsAudDir().getAbsolutePath() + "/";
            btnH.setTypeface(null, Typeface.BOLD);
            btnH.setTextColor(Color.parseColor("#ffffff"));
            btnW.setTypeface(null, Typeface.NORMAL);
            btnW.setTextColor(Color.parseColor("#aea7a7"));
            adapter = new QImage(QMain.this, utils.getFilePaths());
        } else {
            Riwaya = utils.getWarshAudDir().getAbsolutePath() + "/";
            btnH.setTypeface(null, Typeface.NORMAL);
            btnH.setTextColor(Color.parseColor("#aea7a7"));
            btnW.setTypeface(null, Typeface.BOLD);
            btnW.setTextColor(Color.parseColor("#ffffff"));
            adapter = new QImage(QMain.this, utils.getFilePathsW());
        }

        db.opendatabase();
        curQ = db.qrrawQuery("SELECT * FROM tb_q", null);
        curQ.moveToFirst();

        int j = 0;
        do {
            if (curQ.getInt(0) == lastP) break;
            curQ.moveToNext();
            j++;
        } while (j < 609);

        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(608 - lastP);

        btnP.setText("" + curQ.getInt(0));
        btnS.setText(curQ.getString(1));
        btnJ.setText(curQ.getString(2));

        // Get screen dimensions (modern API)
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        screenWidth  = metrics.widthPixels;
        screenHeight = metrics.heightPixels;

        if (line > 0) {
            cadre.setVisibility(View.VISIBLE);
            cadre.setGravity(Gravity.LEFT);
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.setMargins(point * (screenWidth / 4), (line - 1) * (screenHeight / 15), 0, 0);
            layoutParams.width  = screenWidth / 4;
            layoutParams.height = screenHeight / 15;
            cadre.setLayoutParams(layoutParams);

            SQLdb = db.openWritableDb();
            SQLdb.execSQL("UPDATE tb_indice SET line = 0");
            SQLdb.close();
        }

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        final GestureDetector gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            public void onLongPress(MotionEvent e) {
                if (lastP > 0 && lastP < 605) {
                    finish();
                    Intent intent = new Intent(QMain.this, LandMark.class);
                    startActivity(intent);
                }
            }
        });

        viewPager.setOnTouchListener(new View.OnTouchListener() {
            private final int CLICK_ACTION_THRESHOLD = 200;
            private float startX;
            private float startY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startX = event.getX();
                        startY = event.getY();
                        break;

                    case MotionEvent.ACTION_UP:
                        float endX = event.getX();
                        float endY = event.getY();
                        if (isAClick(startX, endX, startY, endY)) {
                            cadre.setVisibility(View.GONE);
                            if (ctlAudio == 1) {
                                ringTone.stop();
                                ctlAudio = 0;
                            }

                            if (bar == 0 && curQ.getInt(0) > 0 && curQ.getInt(0) < 605) {
                                btnJ.setVisibility(View.VISIBLE);
                                btnP.setVisibility(View.VISIBLE);
                                btnS.setVisibility(View.VISIBLE);
                                btnM.setVisibility(View.VISIBLE);
                                btnA.setVisibility(View.VISIBLE);
                                btnT.setVisibility(View.VISIBLE);
                                btnH.setVisibility(View.VISIBLE);
                                btnW.setVisibility(View.VISIBLE);
                                btnF.setVisibility(View.VISIBLE);
                                bar = 1;

                                float x = startX;
                                float y = startY;

                                if (0 < y && y < ((screenHeight / 15) * 1) + 1) y1 = 1;
                                else if ((screenHeight / 15) < y && y < ((screenHeight / 15) * 2) + 1) y1 = 2;
                                else if ((screenHeight / 15) * 2 < y && y < ((screenHeight / 15) * 3) + 1) y1 = 3;
                                else if ((screenHeight / 15) * 3 < y && y < ((screenHeight / 15) * 4) + 1) y1 = 4;
                                else if ((screenHeight / 15) * 4 < y && y < ((screenHeight / 15) * 5) + 1) y1 = 5;
                                else if ((screenHeight / 15) * 5 < y && y < ((screenHeight / 15) * 6) + 1) y1 = 6;
                                else if ((screenHeight / 15) * 6 < y && y < ((screenHeight / 15) * 7) + 1) y1 = 7;
                                else if ((screenHeight / 15) * 7 < y && y < ((screenHeight / 15) * 8) + 1) y1 = 8;
                                else if ((screenHeight / 15) * 8 < y && y < ((screenHeight / 15) * 9) + 1) y1 = 9;
                                else if ((screenHeight / 15) * 9 < y && y < ((screenHeight / 15) * 10) + 1) y1 = 10;
                                else if ((screenHeight / 15) * 10 < y && y < ((screenHeight / 15) * 11) + 1) y1 = 11;
                                else if ((screenHeight / 15) * 11 < y && y < ((screenHeight / 15) * 12) + 1) y1 = 12;
                                else if ((screenHeight / 15) * 12 < y && y < ((screenHeight / 15) * 13) + 1) y1 = 13;
                                else if ((screenHeight / 15) * 13 < y && y < ((screenHeight / 15) * 14) + 1) y1 = 14;
                                else if ((screenHeight / 15) * 14 < y && y < ((screenHeight / 15) * 15) + 1) y1 = 15;

                                if (0 < x && x < ((screenWidth / 4) * 1) + 1) x1 = 4;
                                else if ((screenWidth / 4) < x && x < ((screenWidth / 4) * 2) + 1) x1 = 3;
                                else if ((screenWidth / 4) * 2 < x && x < ((screenWidth / 4) * 3) + 1) x1 = 2;
                                else if ((screenWidth / 4) * 3 < x && x < ((screenWidth / 4) * 4) + 1) x1 = 1;

                                int click_page = curQ.getInt(0);
                                int click_line = y1;
                                int click_point = x1;

                                x1 = (curQ.getInt(0) - 1) * 60 + (y1 - 1) * 4 + x1 + 1;

                                Cursor curS;
                                db.opendatabase();
                                curS = db.srrawQuery("SELECT * FROM tb_sora", null);
                                curS.moveToFirst();
                                int k = 1;
                                do {
                                    if (curS.getInt(3) > x1) break;
                                    curS.moveToNext();
                                    k++;
                                } while (k < 115);
                                curS.moveToPrevious();
                                x1 = x1 - curS.getInt(3);
                                sora = "s" + (k - 1);
                                curS.close();

                                // Calculate soraI and ayaI from SQLite (tb_tafcir or tb_tafcirW) based on click position
                                int clickPosIndex = click_line * 4 + click_point;
                                int bestSora = 1;
                                int bestAya = 1;
                                
                                String tableName = (lastR == 1) ? "tb_tafcir" : "tb_tafcirW";
                                Cursor c = db.tfrawQuery("SELECT sora, aya, p_sora, line, point FROM " + tableName + " WHERE p_sora = " + click_page + " OR p_sora = " + (click_page - 1) + " ORDER BY p_sora ASC, line ASC, point ASC", null);
                                if (c != null && c.moveToFirst()) {
                                    do {
                                        int p_sora = c.getInt(2);
                                        int r_line = c.getInt(3);
                                        int r_point = c.getInt(4);
                                        int pos_index = r_line * 4 + r_point;
                                        
                                        if (p_sora < click_page || (p_sora == click_page && pos_index <= clickPosIndex)) {
                                            bestSora = c.getInt(0);
                                            bestAya = c.getInt(1);
                                        } else {
                                            break;
                                        }
                                    } while (c.moveToNext());
                                    c.close();
                                }
                                soraI = bestSora;
                                ayaI = bestAya;

                                // Read audio index from XLS file
                                try {
                                    File xlsFile = new File(Riwaya + sora + ".xls");
                                    if (xlsFile.exists()) {
                                        Workbook wb = Workbook.getWorkbook(xlsFile);
                                        Sheet s = wb.getSheet(0);
                                        Cell z = s.getCell(4, x1);
                                        pos = new Integer(z.getContents().toString());
                                    }
                                } catch (Exception e) {
                                    Log.e("QMain", "XLS read error: " + e.getMessage());
                                }

                            } else {
                                btnJ.setVisibility(View.GONE);
                                btnP.setVisibility(View.GONE);
                                btnS.setVisibility(View.GONE);
                                btnM.setVisibility(View.GONE);
                                btnAbout.setVisibility(View.GONE);
                                btnA.setVisibility(View.GONE);
                                btnT.setVisibility(View.GONE);
                                btnH.setVisibility(View.GONE);
                                btnW.setVisibility(View.GONE);
                                btnF.setVisibility(View.GONE);
                                bar = 0;
                            }
                            ll21.setVisibility(View.GONE);
                            ll22.setVisibility(View.GONE);
                        }
                        break;
                }
                v.getParent().requestDisallowInterceptTouchEvent(true);
                return gestureDetector.onTouchEvent(event);
            }

            private boolean isAClick(float startX, float endX, float startY, float endY) {
                float differenceX = Math.abs(startX - endX);
                float differenceY = Math.abs(startY - endY);
                return !(differenceX > CLICK_ACTION_THRESHOLD || differenceY > CLICK_ACTION_THRESHOLD);
            }
        });

        viewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                lastP = 608 - position;
                int k = 0;
                curQ.moveToFirst();
                do {
                    if (curQ.getInt(0) == lastP) break;
                    curQ.moveToNext();
                    k++;
                } while (k < 609);
                if (curQ.getString(2).length() > 8)
                    Toast.makeText(QMain.this, curQ.getString(2), Toast.LENGTH_SHORT).show();
                SQLdb = db.openWritableDb();
                SQLdb.execSQL("UPDATE tb_indice SET last_page = " + curQ.getInt(0));
                SQLdb.close();
                btnP.setText("" + curQ.getInt(0));
                btnS.setText(curQ.getString(1));
                btnJ.setText(curQ.getString(2));
                cadre.setVisibility(View.GONE);
                if (curQ.getInt(0) == 0 || curQ.getInt(0) > 604) {
                    btnJ.setVisibility(View.GONE);
                    btnP.setVisibility(View.GONE);
                    btnS.setVisibility(View.GONE);
                    btnM.setVisibility(View.GONE);
                    btnAbout.setVisibility(View.GONE);
                    btnA.setVisibility(View.GONE);
                    btnT.setVisibility(View.GONE);
                    btnH.setVisibility(View.GONE);
                    btnW.setVisibility(View.GONE);
                    btnF.setVisibility(View.GONE);
                    bar = 0;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                if (state == ViewPager.SCROLL_STATE_IDLE) {
                    int index = viewPager.getCurrentItem();
                    if (index == 0) viewPager.setCurrentItem(adapter.getCount() - 1, false);
                    else if (index == adapter.getCount() - 1) viewPager.setCurrentItem(0, false);
                }
            }
        });

        // ---- Buttons ----

        btnW.setOnClickListener(v -> {
            adapter = new QImage(QMain.this, utils.getFilePathsW());
            viewPager.setAdapter(adapter);
            viewPager.setCurrentItem(608 - lastP);
            SQLdb = db.openWritableDb();
            SQLdb.execSQL("UPDATE tb_indice SET last_riwaya = 2");
            SQLdb.close();
            btnW.setTypeface(null, Typeface.BOLD);
            btnW.setTextColor(Color.parseColor("#ffffff"));
            btnH.setTypeface(null, Typeface.NORMAL);
            btnH.setTextColor(Color.parseColor("#aea7a7"));
            Riwaya = utils.getWarshAudDir().getAbsolutePath() + "/";
            hideBar();
        });

        btnH.setOnClickListener(v -> {
            adapter = new QImage(QMain.this, utils.getFilePaths());
            viewPager.setAdapter(adapter);
            viewPager.setCurrentItem(608 - lastP);
            SQLdb = db.openWritableDb();
            SQLdb.execSQL("UPDATE tb_indice SET last_riwaya = 1");
            SQLdb.close();
            btnH.setTypeface(null, Typeface.BOLD);
            btnH.setTextColor(Color.parseColor("#ffffff"));
            btnW.setTypeface(null, Typeface.NORMAL);
            btnW.setTextColor(Color.parseColor("#aea7a7"));
            Riwaya = utils.getHafsAudDir().getAbsolutePath() + "/";
            hideBar();
        });

        btnT.setOnClickListener(v -> {
            SQLdb = db.openWritableDb();
            SQLdb.execSQL("UPDATE tb_indice SET soraIndex = " + soraI + ", ayaIndex = " + ayaI);
            SQLdb.close();
            finish();
            startActivity(new Intent(QMain.this, Tefcir.class));
        });

        btnA.setOnClickListener(v -> {
            if (ctlAudio == 1) {
                ringTone.stop();
                ctlAudio = 0;
            }
            String filePath = Riwaya + sora + ".wav";
            ringTone = new MediaPlayer();
            try {
                ringTone.setDataSource(filePath);
                ringTone.prepare();
                ringTone.seekTo(pos);
                ringTone.start();
                ctlAudio = 1;
            } catch (IOException e) {
                e.printStackTrace();
            }
            hideBar();
        });

        btnM.setOnClickListener(v -> btnAbout.setVisibility(View.VISIBLE));

        btnAbout.setOnClickListener(v -> {
            finish();
            startActivity(new Intent(QMain.this, About.class));
        });

        btnS.setOnClickListener(v -> {
            finish();
            startActivity(new Intent(QMain.this, IndexS.class));
        });

        btnP.setOnClickListener(v -> {
            finish();
            startActivity(new Intent(QMain.this, IndexP.class));
        });

        btnJ.setOnClickListener(v -> {
            finish();
            startActivity(new Intent(QMain.this, IndexJ.class));
        });

        btnF.setOnClickListener(v -> {
            finish();
            startActivity(new Intent(QMain.this, IndexF.class));
        });

        btnAP.setOnClickListener(v -> {
            if (curT.getInt(0) == 1) curT.moveToLast(); else curT.moveToPrevious();
            ayaT.setText(curT.getString(3));
            tefcirT.setText(curT.getString(4));
            scrollV.smoothScrollTo(0, 0);
        });

        btnAN.setOnClickListener(v -> {
            if (curT.getInt(0) == 6236) curT.moveToFirst(); else curT.moveToNext();
            ayaT.setText(curT.getString(3));
            tefcirT.setText(curT.getString(4));
            scrollV.smoothScrollTo(0, 0);
        });

        btnAPb.setOnClickListener(v -> {
            if (curT.getInt(0) == 1) curT.moveToLast(); else curT.moveToPrevious();
            ayaB.setText(curT.getString(3));
            tefcirB.setText(curT.getString(4));
            scrollVb.smoothScrollTo(0, 0);
        });

        btnANb.setOnClickListener(v -> {
            if (curT.getInt(0) == 6236) curT.moveToFirst(); else curT.moveToNext();
            ayaB.setText(curT.getString(3));
            tefcirB.setText(curT.getString(4));
            scrollVb.smoothScrollTo(0, 0);
        });

        btnRetour.setOnClickListener(v -> ll21.setVisibility(View.GONE));
    }

    private void hideBar() {
        btnJ.setVisibility(View.GONE);
        btnP.setVisibility(View.GONE);
        btnS.setVisibility(View.GONE);
        btnM.setVisibility(View.GONE);
        btnAbout.setVisibility(View.GONE);
        btnA.setVisibility(View.GONE);
        btnT.setVisibility(View.GONE);
        btnH.setVisibility(View.GONE);
        btnW.setVisibility(View.GONE);
        btnF.setVisibility(View.GONE);
        bar = 0;
    }

    // Les fichiers sont déposés manuellement par l'utilisateur dans /storage/emulated/0/QuranHW/
    // Pas de copie d'assets nécessaire.

    /** Affiche un message d'erreur dans l'UI quand les fichiers images sont absents */
    private void showMissingFilesMessage(String path) {
        // Réutilise le loadingLayout pour afficher le message
        View loading = findViewById(R.id.loadingLayout);
        loading.setVisibility(View.VISIBLE);
        // Trouver le TextView dans le layout de chargement
        TextView tv = loading.findViewById(R.id.tvLoadingMsg);
        if (tv != null) {
            tv.setText(
                "\u26a0 Fichiers manquants !\n\n" +
                "Copiez vos images JPG dans :\n\n" +
                path + "\n\n" +
                "(p0.jpg, p1.jpg ... p608.jpg)\n\n" +
                "Puis relancez l'application."
            );
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_HOME) {
            if (ctlAudio == 1) {
                ringTone.stop();
                ctlAudio = 0;
            }
        }
        return super.onKeyUp(keyCode, event);
    }
}

