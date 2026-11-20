package quran.hb.com.quran;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Point;
import android.os.Build;
import android.os.Environment;
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
     * Retourne les chemins des 608 pages Hafs (HI).
     * Chemin construit directement p1.jpg..p608.jpg (sans listFiles).
     * Index 0 = p608 (ViewPager affiche de la fin vers le debut : swipe droite = page precedente).
     */
    public ArrayList<String> getFilePaths() {
        ArrayList<String> filePaths = new ArrayList<String>();
        String base = Environment.getExternalStorageDirectory().getAbsolutePath() + "/QuranHW/HI/";
        // Index 0 = page 608 (premiere vue = derniere page Coran)
        for (int p = 608; p >= 1; p--) {
            filePaths.add(base + "p" + p + ".jpg");
        }
        return filePaths;
    }

    /**
     * Retourne les chemins des 608 pages Warsh (WI).
     * Chemin construit directement p1.png..p608.png (sans listFiles).
     * Index 0 = p608.
     */
    public ArrayList<String> getFilePathsW() {
        ArrayList<String> filePathsW = new ArrayList<String>();
        String base = Environment.getExternalStorageDirectory().getAbsolutePath() + "/QuranHW/WI/";
        for (int p = 608; p >= 1; p--) {
            filePathsW.add(base + "p" + p + ".png");
        }
        return filePathsW;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public int getScreenWidth() {
        int columnWidth;
        WindowManager wm = (WindowManager) _context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        final Point point = new Point();
        try { display.getSize(point); }
        catch (java.lang.NoSuchMethodError ignore) {
            point.x = display.getWidth();
            point.y = display.getHeight();
        }
        columnWidth = point.x;
        return columnWidth;
    }
}
