package quran.hb.com.quran;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.view.WindowManager;

public class SplashActivity extends Activity {

    private static final int REQ_STORAGE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Plein écran sans barre de titre
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        );

        // Fond blanc pendant le démarrage (évite l'écran noir)
        getWindow().getDecorView().setBackgroundColor(0xFFFFFFFF);

        if (Build.VERSION.SDK_INT >= 23 && Build.VERSION.SDK_INT < 30) {
            // Android 6-9 : demander WRITE_EXTERNAL_STORAGE
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                }, REQ_STORAGE);
                return;
            }
        }
        // Android 10+ : WRITE_EXTERNAL_STORAGE ignoré, on continue directement
        launchMain();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
            String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_STORAGE) {
            launchMain();
        }
    }

    private void launchMain() {
        startActivity(new Intent(this, QMain.class));
        finish();
    }
}
