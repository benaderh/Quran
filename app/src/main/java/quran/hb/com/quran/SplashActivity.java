package quran.hb.com.quran;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.widget.Toast;

public class SplashActivity extends Activity {

    private static final int REQ_PERM = 1;
    private boolean waitingForSettings = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkAndRequestPermissions();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Verifier si les permissions sont accordees apres retour des parametres systeme
        if (waitingForSettings) {
            waitingForSettings = false;
            if (hasRequiredPermissions()) {
                launchMain();
            } else {
                // Toujours pas de permission : lancer quand meme (pour eviter le blocage)
                Toast.makeText(this,
                    "Accordez l'acces au stockage pour afficher les pages du Coran",
                    Toast.LENGTH_LONG).show();
                launchMain();
            }
        }
    }

    private void checkAndRequestPermissions() {
        // Android 11+ (API 30+) : MANAGE_EXTERNAL_STORAGE obligatoire pour /QuranHW/
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                waitingForSettings = true;
                try {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                    intent.setData(Uri.parse("package:" + getPackageName()));
                    startActivity(intent);
                } catch (Exception e) {
                    // Fallback: ouvrir les settings generaux
                    Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                    startActivity(intent);
                }
                return;
            }
            // Permission accordee
            launchMain();
            return;
        }

        // Android 13+ (API 33+) : READ_MEDIA_IMAGES
        if (Build.VERSION.SDK_INT >= 33) {
            if (checkSelfPermission(android.Manifest.permission.READ_MEDIA_IMAGES)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{
                    android.Manifest.permission.READ_MEDIA_IMAGES,
                    android.Manifest.permission.RECORD_AUDIO
                }, REQ_PERM);
                return;
            }
            launchMain();
            return;
        }

        // Android 6-12 : READ_EXTERNAL_STORAGE
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{
                    android.Manifest.permission.READ_EXTERNAL_STORAGE,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    android.Manifest.permission.RECORD_AUDIO
                }, REQ_PERM);
                return;
            }
        }

        launchMain();
    }

    private boolean hasRequiredPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return Environment.isExternalStorageManager();
        }
        if (Build.VERSION.SDK_INT >= 33) {
            return checkSelfPermission(android.Manifest.permission.READ_MEDIA_IMAGES)
                    == PackageManager.PERMISSION_GRANTED;
        }
        if (Build.VERSION.SDK_INT >= 23) {
            return checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
            String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Que la permission soit accordee ou non, lancer l'app
        launchMain();
    }

    private void launchMain() {
        startActivity(new Intent(this, QMain.class));
        finish();
    }
}
