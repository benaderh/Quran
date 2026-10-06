package quran.hb.com.quran;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import java.io.File;
import java.util.ArrayList;

public class Utils {

    private Context _context;

    public Utils(Context context) {
        this._context = context;
    }

    /**
     * Returns the app-private external files directory for Quran images/audio.
     * No permissions needed on Android 10+.
     * Path: /sdcard/Android/data/quran.hb.com.quran/files/QuranHW/
     */
    public File getQuranBaseDir() {
        // getExternalFilesDir(null) returns app-specific external dir - no permission needed
        File baseDir = _context.getExternalFilesDir(null);
        if (baseDir == null) {
            // Fallback to internal files dir if no external storage
            baseDir = _context.getFilesDir();
        }
        File quranDir = new File(baseDir, "QuranHW");
        quranDir.mkdirs();
        return quranDir;
    }

    public File getHafsImgDir() {
        File dir = new File(getQuranBaseDir(), "H/img");
        dir.mkdirs();
        return dir;
    }

    public File getHafsAudDir() {
        File dir = new File(getQuranBaseDir(), "H/aud");
        dir.mkdirs();
        return dir;
    }

    public File getWarshImgDir() {
        File dir = new File(getQuranBaseDir(), "W/img");
        dir.mkdirs();
        return dir;
    }

    public File getWarshAudDir() {
        File dir = new File(getQuranBaseDir(), "W/aud");
        dir.mkdirs();
        return dir;
    }

    public ArrayList<String> getFilePaths() {
        ArrayList<String> filePaths = new ArrayList<>();
        File directoryH = getHafsImgDir();
        File[] listFiles = directoryH.listFiles();
        if (listFiles != null && listFiles.length > 0) {
            for (int i = 0; i < listFiles.length; i++) {
                String filePath = listFiles[i].getAbsolutePath();
                if (IsSupportedFile(filePath)) {
                    filePaths.add(directoryH + "/p" + (608 - i) + ".jpg");
                }
            }
        }
        return filePaths;
    }

    public ArrayList<String> getFilePathsW() {
        ArrayList<String> filePathsW = new ArrayList<>();
        File directoryW = getWarshImgDir();
        File[] listFilesW = directoryW.listFiles();
        if (listFilesW != null && listFilesW.length > 0) {
            for (int i = 0; i < listFilesW.length; i++) {
                String filePathW = listFilesW[i].getAbsolutePath();
                if (IsSupportedFile(filePathW)) {
                    filePathsW.add(directoryW + "/p" + (608 - i) + ".png");
                }
            }
        }
        return filePathsW;
    }

    private boolean IsSupportedFile(String filePath) {
        return true;
    }

    public int getScreenWidth() {
        WindowManager wm = (WindowManager) _context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(metrics);
        return metrics.widthPixels;
    }
}
