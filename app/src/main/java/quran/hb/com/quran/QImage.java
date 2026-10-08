package quran.hb.com.quran;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.util.ArrayList;

public class QImage extends PagerAdapter {

	private Activity _activity;
	private ArrayList<String> _imagePaths;
	private LayoutInflater inflater;


	//private static final String POSITON = "position";
	//private static final String SCALE = "scale";
	//Integer x1, y1;
	//private int screenWidth;
	//private int screenHeight;
	//TextView imgSize;
	//private float[] lastTouchDownXY = new float[2];
    //int i1,i2;

	// constructor
	public QImage(Activity activity, ArrayList<String> imagePaths) {
		this._activity = activity;
		this._imagePaths = imagePaths;



	}


	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		//getWidthAndHeight();
		ImageView imgDisplay;


		inflater = (LayoutInflater) _activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View viewLayout = inflater.inflate(R.layout.q_image, container, false);

		imgDisplay = (ImageView) viewLayout.findViewById(R.id.imgDisplay);

		//imgDisplay.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
		//imgDisplay.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
		//imgDisplay.setAdjustViewBounds(false);
		//imgDisplay.setScaleType(ImageView.ScaleType.FIT_XY);


		//imgDisplay.getLayoutParams()..FLAG_FULLSCREEN;//,WindowManager.LayoutParams.FLAG_FULLSCREEN;

		//int orientation = this.getResources().getConfiguration().orientation;
		//if (orientation == Configuration.ORIENTATION_PORTRAIT) {
			//Toast.makeText(getApplicationContext(), "Portrait", Toast.LENGTH_SHORT).show();
			//imgDisplay.setAdjustViewBounds(false);
		//	imgDisplay.setScaleType(ImageView.ScaleType.FIT_XY);
		//} else {
			//Toast.makeText(getApplicationContext(), "Land", Toast.LENGTH_SHORT).show();
			//imgDisplay.setAdjustViewBounds(true);
		//	imgDisplay.setScaleType(ImageView.ScaleType.CENTER_CROP);

		//}


		BitmapFactory.Options options = new BitmapFactory.Options();
		//options.inPreferredConfig = Bitmap.Config.ARGB_8888;
		Bitmap bitmap = BitmapFactory.decodeFile(_imagePaths.get(position), options);
		imgDisplay.setImageBitmap(bitmap);


		 //imgDisplay.setOnTouchListener(touchListener);
		 //imgDisplay.setOnLongClickListener(longClickListener);
		//Toast.makeText(QImage.this, "Down", Toast.LENGTH_LONG).show();

	/*	imgDisplay.setOnLongClickListener(new View.OnLongClickListener() {

			@Override
			public boolean onLongClick(View v) {
				//Toast.makeText(QImage.this, "Down", Toast.LENGTH_LONG).show();
				Toast.makeText(_activity.getApplicationContext(), "Portrait", Toast.LENGTH_SHORT).show();
				return false;
			}
		});

    */


		((ViewPager) container).addView(viewLayout);
		//imgDisplay.setOnTouchListener(touchListener);
		//imgDisplay.setOnLongClickListener(longClickListener);
		//i1=position;
		return viewLayout;

	}


	@Override
	public int getCount() {
		return this._imagePaths.size();
	}

	@Override
	public boolean isViewFromObject(View view, Object object) {
		return view == ((RelativeLayout) object);
	}


	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		((ViewPager) container).removeView((RelativeLayout) object);
		//i2=position;

	}


	/**
	 * Get device screen width and height
	 */
/*	private void getWidthAndHeight() {
		//DisplayMetrics displaymetrics = new DisplayMetrics();
		//DisplayMetrics metrics = new DisplayMetrics();

		//_activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
		screenHeight = 1920;//displaymetrics.heightPixels;
		screenWidth = 1200;//displaymetrics.widthPixels;
		//Toast.makeText(QImage.this, "Down", Toast.LENGTH_LONG).show();
	}*/




}
