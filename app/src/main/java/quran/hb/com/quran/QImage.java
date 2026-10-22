package quran.hb.com.quran;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.SparseArray;
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

	/** Evenements envoyes a l'activite (mode horizontal : clic et passage de page par defilement) */
	public interface PageListener {
		void onPageTap(float fracX, float fracY);
		void onFlipNext(int position);
		void onFlipPrev(int position);
	}

	/** Vues d'une page : image, surlignage (tilawa) et defilement (mode horizontal seulement) */
	private static class Holder {
		View root;
		ImageView img;
		View hl;
		PageScrollView scroller;
		String sig = "";
	}

	private final SparseArray<Holder> holders = new SparseArray<Holder>();
	private PageListener pageListener;

	// surlignage de la partie en cours de tilawa
	private int hlPos = -1, hlLine = 0, hlPoint = 0;
	private boolean hlScrollPending = false;

	// defilement a appliquer a une page pas encore creee
	private int pendingScrollPos = -1;
	private boolean pendingScrollTop = true;

	public void setPageListener(PageListener l) {
		this.pageListener = l;
	}


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

		final int pagePosition = position;
		final Holder holder = new Holder();
		holder.root = viewLayout;
		holder.img = imgDisplay;
		holder.hl = viewLayout.findViewById(R.id.hlView);
		holder.scroller = (PageScrollView) viewLayout.findViewById(R.id.page_scroller);
		if (holder.scroller != null) {
			holder.scroller.setEdgeListener(new PageScrollView.EdgeListener() {
				@Override
				public void onReachEnd() {
					if (pageListener != null) pageListener.onFlipNext(pagePosition);
				}

				@Override
				public void onReachStart() {
					if (pageListener != null) pageListener.onFlipPrev(pagePosition);
				}

				@Override
				public void onTap(float fracX, float fracY) {
					if (pageListener != null) pageListener.onPageTap(fracX, fracY);
				}
			});
		}
		holders.put(position, holder);

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
		if (pendingScrollPos == position) {
			scheduleScroll(holder, pendingScrollTop);
			pendingScrollPos = -1;
		}
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
		holders.remove(position);
		//i2=position;

	}


	// ---------------------------------------------------------------
	// Passage a une autre page par defilement (mode horizontal)
	// ---------------------------------------------------------------

	/** Place le defilement de la page 'position' en haut (true) ou en bas (false) */
	public void prepareScroll(int position, boolean top) {
		Holder h = holders.get(position);
		if (h == null || h.scroller == null) {
			pendingScrollPos = position;
			pendingScrollTop = top;
		} else {
			scheduleScroll(h, top);
		}
	}

	private void scheduleScroll(final Holder h, final boolean top) {
		if (h.scroller == null) return;
		final int[] tries = {0};
		h.scroller.post(new Runnable() {
			@Override
			public void run() {
				View c = h.scroller.getChildAt(0);
				if (!top && (c == null || c.getHeight() <= 0 || h.scroller.getHeight() <= 0) && tries[0] < 20) {
					tries[0]++;
					h.scroller.postDelayed(this, 50);
					return;
				}
				if (top || c == null) {
					h.scroller.scrollTo(0, 0);
				} else {
					h.scroller.scrollTo(0, Math.max(0, c.getHeight() - h.scroller.getHeight()));
				}
			}
		});
	}

	// ---------------------------------------------------------------
	// Surlignage de la partie en cours de tilawa
	// ---------------------------------------------------------------

	/** Memorise la cellule (ligne 1..15, point 1..4) a surligner sur la page 'position' */
	public void setHighlight(int position, int line, int point) {
		if (position != hlPos || line != hlLine || point != hlPoint) {
			hlPos = position;
			hlLine = line;
			hlPoint = point;
			hlScrollPending = true;
		}
	}

	public void clearHighlight() {
		hlPos = -1;
		hlScrollPending = false;
		for (int i = 0; i < holders.size(); i++) {
			Holder h = holders.valueAt(i);
			if (h.hl != null) h.hl.setVisibility(View.GONE);
			h.sig = "";
		}
	}

	/** Affiche le surlignage sur la bonne page (a rappeler tant que la page n'est pas prete) */
	public void applyHighlight() {
		for (int i = 0; i < holders.size(); i++) {
			Holder h = holders.valueAt(i);
			if (h.hl == null) continue;
			if (holders.keyAt(i) != hlPos) {
				if (h.hl.getVisibility() != View.GONE) h.hl.setVisibility(View.GONE);
				h.sig = "";
				continue;
			}
			int w = h.img.getWidth();
			int ht = h.img.getHeight();
			if (w <= 0 || ht <= 0) continue;   // pas encore mesuree : on reessaie au prochain passage

			int line = Math.max(1, Math.min(15, hlLine));
			int col = Math.max(0, Math.min(3, 4 - hlPoint));   // point 4 = a gauche, point 1 = a droite
			int cw = w / 4;
			int ch = ht / 15;
			int left = h.img.getLeft() + col * cw;
			int top = h.img.getTop() + (line - 1) * ch;

			String sig = w + "x" + ht + ":" + line + ":" + col;
			if (!sig.equals(h.sig)) {
				RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(cw, ch);
				lp.leftMargin = left;
				lp.topMargin = top;
				h.hl.setLayoutParams(lp);
				h.sig = sig;
			}
			h.hl.setVisibility(View.VISIBLE);

			if (hlScrollPending) {
				if (h.scroller != null) {
					int sy = h.scroller.getScrollY();
					int vh = h.scroller.getHeight();
					if (top < sy || top + ch > sy + vh) {
						h.scroller.smoothScrollTo(0, Math.max(0, top - vh / 3));
					}
				}
				hlScrollPending = false;
			}
		}
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
