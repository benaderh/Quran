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

    private String getQuranBaseDir() {
        return _context.getFilesDir().getAbsolutePath() + "/QuranHW";
    }

    public ArrayList<String> getFilePaths() {
        ArrayList<String> filePaths = new ArrayList<String>();
        File dir = new File(getQuranBaseDir() + "/HI");
        if (dir.exists()) {
            File[] files = dir.listFiles();
            if (files != null && files.length > 0) {
                for (int i = 0; i < files.length; i++) {
                    filePaths.add(dir.getAbsolutePath() + "/p" + (608 - i) + ".jpg");
                }
            }
        }
        return filePaths;
    }

    public ArrayList<String> getFilePathsW() {
        ArrayList<String> filePathsW = new ArrayList<String>();
        File dir = new File(getQuranBaseDir() + "/WI");
        if (dir.exists()) {
            File[] files = dir.listFiles();
            if (files != null && files.length > 0) {
                for (int i = 0; i < files.length; i++) {
                    filePathsW.add(dir.getAbsolutePath() + "/p" + (608 - i) + ".png");
                }
            }
        }
        return filePathsW;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public int getScreenWidth() {
        int columnWidth;
        WindowManager wm = (WindowManager) _context.getSystemService(Context.WINDOW_SERVICE);
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