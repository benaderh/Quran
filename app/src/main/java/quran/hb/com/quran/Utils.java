package quran.hb.com.quran;

import android.content.Context;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import java.io.File;
import java.util.ArrayList;

public class Utils {

    private Context _context;

    // Répertoire racine : /sdcard/QuranHW/
    // L'utilisateur dépose manuellement ses fichiers dans les 4 sous-dossiers :
    //   HI/ -> images Hafs (.jpg)
    //   HA/ -> audio  Hafs (.wav, .csv)
    //   WI/ -> images Warsh (.png)
    //   WA/ -> audio  Warsh (.wav, .csv)
    private static final String BASE_DIR_NAME = "QuranHW";

    public Utils(Context context) {
        this._context = context;
    }

    /** Racine : /sdcard/QuranHW/ */
    public File getQuranBaseDir() {
        File sdcard = Environment.getExternalStorageDirectory(); // /storage/emulated/0
        File dir = new File(sdcard, BASE_DIR_NAME);
        dir.mkdirs();
        return dir;
    }

    /** /sdcard/QuranHW/HI/ — images Hafs */
    public File getHafsImgDir() {
        return new File(getQuranBaseDir(), "HI");
    }

    /** /sdcard/QuranHW/HA/ — audio Hafs */
    public File getHafsAudDir() {
        return new File(getQuranBaseDir(), "HA");
    }

    /** /sdcard/QuranHW/WI/ — images Warsh */
    public File getWarshImgDir() {
        return new File(getQuranBaseDir(), "WI");
    }

    /** /sdcard/QuranHW/WA/ — audio Warsh */
    public File getWarshAudDir() {
        return new File(getQuranBaseDir(), "WA");
    }

    /** Liste triée des chemins images Hafs p0.jpg … p608.jpg */
    public ArrayList<String> getFilePaths() {
        ArrayList<String> filePaths = new ArrayList<>();
        File dir = getHafsImgDir();
        for (int i = 0; i <= 608; i++) {
            filePaths.add(new File(dir, "p" + i + ".jpg").getAbsolutePath());
        }
        return filePaths;
    }

    /** Liste triée des chemins images Warsh p0.png … p608.png */
    public ArrayList<String> getFilePathsW() {
        ArrayList<String> filePathsW = new ArrayList<>();
        File dir = getWarshImgDir();
        for (int i = 0; i <= 608; i++) {
            filePathsW.add(new File(dir, "p" + i + ".png").getAbsolutePath());
        }
        return filePathsW;
    }

    public int getScreenWidth() {
        WindowManager wm = (WindowManager) _context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(metrics);
        return metrics.widthPixels;
    }
}
