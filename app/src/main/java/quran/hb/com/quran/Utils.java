package quran.hb.com.quran;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Point;
import android.os.Build;
import android.view.Display;
import android.view.WindowManager;

import java.io.File;
import java.util.ArrayList;

public class Utils {

    private Context _context;

    public Utils(Context context) {
        this._context = context;
    }

    /**
     * Retourne le dossier de base des fichiers Quran.
     * Utilise le stockage interne de l'app (aucune permission requise, Android 4+).
     */
    private String getQuranBaseDir() {
        return _context.getFilesDir().getAbsolutePath() + "/QuranHW";
    }

    public ArrayList<String> getFilePaths() {
        ArrayList<String> filePaths = new ArrayList<String>();
        File directoryH = new File(getQuranBaseDir() + "/HI");
        if (directoryH.exists()) {
            File[] listFiles = directoryH.listFiles();
            if (listFiles != null && listFiles.length > 0) {
                for (int i = 0; i < listFiles.length; i++) {
                    filePaths.add(directoryH.getAbsolutePath() + "/p" + (608 - i) + ".jpg");
                }
            }
        }
        return filePaths;
    }

    public ArrayList<String> getFilePathsW() {
        ArrayList<String> filePathsW = new ArrayList<String>();
        File directoryW = new File(getQuranBaseDir() + "/WI");
        if (directoryW.exists()) {
            File[] listFilesW = directoryW.listFiles();
            if (listFilesW != null && listFilesW.length > 0) {
                for (int i = 0; i < listFilesW.length; i++) {
                    filePathsW.add(directoryW.getAbsolutePath() + "/p" + (608 - i) + ".png");
                }
            }
        }
        return filePathsW;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public int getScreenWidth() {
        int columnWidth;
        WindowManager wm = (WindowManager) _context
                .getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        final Point point = new Point();
        try {
            display.getSize(point);
        } catch (java.lang.NoSuchMethodError ignore) {
            point.x = display.getWidth();
            point.y = display.getHeight();
        }
        columnWidth = point.x;
        return columnWidth;
    }
}
