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

	// constructor
	public Utils(Context context) {
		this._context = context;
	}

	/*
	 * Reading file paths from SDCard
	 */
	public ArrayList<String> getFilePaths() {
		ArrayList<String> filePaths = new ArrayList<String>();

		File DirectoryH = new File(android.os.Environment.getExternalStorageDirectory().toString() +"/QuranHW/H/img");

			File[] listFiles = DirectoryH.listFiles();
			// Check for count
			if (listFiles.length > 0) {

				for (int i = 0; i < listFiles.length; i++) {

					// get file path
					String filePath = listFiles[i].getAbsolutePath();

					// check for supported file extension
					if (IsSupportedFile(filePath)) {

						filePaths.add(DirectoryH+"/p"+(608-i)+".jpg");
					}

				}

			}


		return filePaths ;

	}

	public ArrayList<String> getFilePathsW() {
		ArrayList<String> filePathsW = new ArrayList<String>();

		File DirectoryW = new File(android.os.Environment.getExternalStorageDirectory().toString() +"/QuranHW/W/img");

		File[] listFilesW = DirectoryW.listFiles();
		// Check for count

		if (listFilesW.length > 0) {

			for (int i = 0; i < listFilesW.length; i++) {

				// get file path
				String filePathW = listFilesW[i].getAbsolutePath();

				// check for supported file extension
				if (IsSupportedFile(filePathW)) {

					filePathsW.add(DirectoryW+"/p"+(608-i)+".png");
				}

			}

		}

		return filePathsW ;
	}



















	/*
	 * Check supported file extensions
	 *
	 * @returns boolean
	 */
	private boolean IsSupportedFile(String filePath) {
		String ext = filePath.substring((filePath.lastIndexOf(".") + 1),
				filePath.length());

		//	if (AppConstant.FILE_EXTN
		//			.contains(ext.toLowerCase(Locale.getDefault())))
		return true;
		//	else
		//		return false;

	}

	/*
	 * getting screen width
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	public int getScreenWidth() {
		int columnWidth;
		WindowManager wm = (WindowManager) _context
				.getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();

		final Point point = new Point();
		try {
			display.getSize(point);
		} catch (java.lang.NoSuchMethodError ignore) { // Older device
			point.x = display.getWidth();
			point.y = display.getHeight();
		}
		columnWidth = point.x;
		return columnWidth;
	}


}
