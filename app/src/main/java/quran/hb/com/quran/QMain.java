package quran.hb.com.quran;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;

public class QMain extends Activity {

	private Utils utils;
	private QImage adapter;
	private NoSwipeViewPager viewPager;
	private ContinuousPageView continuousView;
	private ArrayList<String> currentImagePaths;
	/** Taille de la liste passée à continuousView (listée inversée). */
	private int contListSize = 0;

	private MediaPlayer ringTone;

	SQLiteDatabase SQLdb;
	Button btnM, btnA, btnT, btnH, btnW, btnF, btnJ, btnP, btnS, btnAbout, btnAP, btnAN, btnAPb, btnANb, btnRetour;
	TextView ayaT, tefcirT, ayaB, tefcirB;
	TextView cadre;
	LinearLayout ll21, ll22;
	ScrollView scrollV, scrollVb;
	//Spinner btnM;
	//ImageView imgDisplayM;
	DbHelper db;
	int lastP, lastR, pos, ayaI, lineI, soraI, line, point;
	int bar = 0;
	int ctlAudio = 0;
	//byte[] image;
	String sora, Riwaya, RiwayaExel, RiwayaAudio;
	//public final static int LOOPS = 1000;
	//public QPagerAdapter adapter;
	//public ViewPager pager;
	//public static int count = 5; //ViewPager items size
	/**
	 * You shouldn't define first page = 0.
	 * Let define firstpage = 'number viewpager size' to make endless carousel
	 */
	//public static int FIRST_PAGE = 1;

	//private static final String POSITON = "position";
	//private static final String SCALE = "scale";
	int x1, y1;
	private int screenWidth;
	private int screenHeight;
	//TextView imgSizeM;
	//private float[] lastTouchDownXY = new float[2];
	//int [] imageArray;
	Cursor curQ, curT;
	//Bitmap bmp;
	//String tone_name;
	/**
	 * ATTENTION: This was auto-generated to implement the App Indexing API.
	 * See https://g.co/AppIndexing/AndroidStudio for more information.
	 */
	//private GoogleApiClient client;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.q_main);

		viewPager       = (NoSwipeViewPager)  findViewById(R.id.vPager);
		continuousView  = (ContinuousPageView) findViewById(R.id.continuousView);


		utils = new Utils(getApplicationContext());

		Intent i = getIntent();
		//int position = i.getIntExtra("position", 608);


		btnJ = (Button) findViewById(R.id.btnJ);
		btnP = (Button) findViewById(R.id.btnP);
		btnS = (Button) findViewById(R.id.btnS);
		btnM = (Button) findViewById(R.id.btnM);
		btnA = (Button) findViewById(R.id.btnA);
		btnT = (Button) findViewById(R.id.btnT);
		btnH = (Button) findViewById(R.id.btnH);
		btnW = (Button) findViewById(R.id.btnW);
		btnF = (Button) findViewById(R.id.btnF);
		btnAbout = (Button) findViewById(R.id.btnAbout);
		btnAP = (Button) findViewById(R.id.btnAP);
		btnAN = (Button) findViewById(R.id.btnAN);
		btnAPb = (Button) findViewById(R.id.btnAPb);
		btnANb = (Button) findViewById(R.id.btnANb);
		btnRetour = (Button) findViewById(R.id.btnRetour);
		//tv_img = (ImageView) findViewById(R.id.imgV);
		ayaT = (TextView) findViewById(R.id.tv_aya);
		tefcirT = (TextView) findViewById(R.id.tv_tefcir);
		ll21 = (LinearLayout) findViewById(R.id.ll21);
		ayaB = (TextView) findViewById(R.id.tv_ayab);
		tefcirB = (TextView) findViewById(R.id.tv_tefcirb);
		ll22 = (LinearLayout) findViewById(R.id.ll22);
		cadre = (TextView) findViewById(R.id.cadre);
		scrollV = (ScrollView) findViewById(R.id.scrollV);
		scrollVb = (ScrollView) findViewById(R.id.scrollVb);
		//scroll.setFocusableInTouchMode(true);
		//scroll.setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS);

		try {
			db = new DbHelper(this);
		} catch (IOException e2) {
			e2.printStackTrace();
		}

		try {
			db.createdatabase();
		} catch (IOException e) {
			e.printStackTrace();
		}


		File myFolder = new File(Environment.getExternalStorageDirectory(), "QuranHW");
		if (!myFolder.exists()) {
			myFolder.mkdir();
			try {
				(new File(myFolder + "/.nomedia")).createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
	    }


		myFolder = new File(Environment.getExternalStorageDirectory()+"/QuranHW/", "HI");
		if (!myFolder.exists()) {
			myFolder.mkdir();
			CopyAssetsHI();
		}

		myFolder = new File(Environment.getExternalStorageDirectory()+"/QuranHW/", "HA");
		if (!myFolder.exists()) {
			myFolder.mkdir();
			CopyAssetsHA();
		}

		myFolder = new File(Environment.getExternalStorageDirectory()+"/QuranHW/", "WI");
		if (!myFolder.exists()) {
			myFolder.mkdir();
			CopyAssetsWI();
		}

		myFolder = new File(Environment.getExternalStorageDirectory()+"/QuranHW/", "WA");
		if (!myFolder.exists()) {
			myFolder.mkdir();
			CopyAssetsWA();
		}
		Cursor curI;
		db.opendatabase();
		curI = db.inrawQuery("SELECT * FROM tb_indice", null);
		curI.moveToFirst();
		lastP = curI.getInt(0);
		lastR = curI.getInt(1);
		line= curI.getInt(8);
		point= curI.getInt(9);
		curI.close();
        if (point==4) point=0;
		else if (point==3) point=1;
		else if (point==2) point=2;
		else point=3;
		//Toast.makeText(getApplicationContext(), "line: " + line + "   point: " + point, Toast.LENGTH_SHORT).show();

	/*	if (line>0) {
			cadre.setVisibility(View.VISIBLE);

			//TextView textV = new TextView(this);
			cadre.setGravity(Gravity.LEFT);
			RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
					RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);

			layoutParams.setMargins(121, 140, 0, 0);
			cadre.setLayoutParams(layoutParams);
			//text.setText("Result ");




			//cadre.setPadding(point * 10, line * 10, 0, 0);  // left, top, right, bottom
			//cadre.setPadding(25, 25, 25, 25);  // left, top, right, bottom
			//TextView textView = (TextView) findViewById(R.id.text_view);
			//cadre.setGravity(Gravity.CENTER);
			//cadre.setGravity(0);
			//cadre.setPaddingLeft(cadre, 10);
			SQLdb = openOrCreateDatabase("db_quran.sqlite", Context.MODE_PRIVATE, null);
			SQLdb.execSQL("UPDATE tb_indice SET line = " + 0 + "");

		}*/



		if (lastR == 1) {
			Riwaya = "/QuranHW/HA/";
			btnH.setTypeface(null, Typeface.BOLD);
			btnH.setTextColor(Color.parseColor("#ffffff"));
			btnW.setTypeface(null, Typeface.NORMAL);
			btnW.setTextColor(Color.parseColor("#aea7a7"));
			currentImagePaths = utils.getFilePaths();
			adapter = new QImage(QMain.this, currentImagePaths);
			adapter.setPageListener(pageListener);
		} else {
			Riwaya = "/QuranHW/WA/";
			btnH.setTypeface(null, Typeface.NORMAL);
			btnH.setTextColor(Color.parseColor("#aea7a7"));
			btnW.setTypeface(null, Typeface.BOLD);
			btnW.setTextColor(Color.parseColor("#ffffff"));
			currentImagePaths = utils.getFilePathsW();
			adapter = new QImage(QMain.this, currentImagePaths);
			adapter.setPageListener(pageListener);
		}


		db.opendatabase();
		curQ = db.qrrawQuery("SELECT * FROM tb_q", null);
		curQ.moveToFirst();

		int j = 0;
		do {
			if (curQ.getInt(0) == lastP) {
				//i = 366;
				break;
			}
			curQ.moveToNext();
			j++;
		} while (j < 609);

		//adapter = new QImage(QMain.this, utils.getFilePaths());
		if (isLandscape()) {
			// Mode paysage : défilement continu PDF
			// Liste inversée : index 0 = p1 (1ère page Coran, en haut)
			//                  index N-1 = p608 (dernière, en bas)
			// Scroll BAS = page suivante
			ArrayList<String> reversedPaths = reversePaths(currentImagePaths);
			contListSize = reversedPaths.size();
			int continuousStart = lastP; // reversed[lastP] = p{lastP}.jpg
			viewPager.setVisibility(View.GONE);
			continuousView.setVisibility(View.VISIBLE);
			continuousView.setPageChangeListener(continuousPageListener);
			continuousView.setImagePaths(reversedPaths, continuousStart);
		} else {
			// Mode portrait : ViewPager normal
			viewPager.setVisibility(View.VISIBLE);
			continuousView.setVisibility(View.GONE);
			viewPager.setAdapter(adapter);
			viewPager.setCurrentItem(608 - lastP);
		}

		//viewPager.setCurrentItem(0);

//		//Cursor curQ;
//		db.opendatabase();
//		curQ = db.qrrawQuery("SELECT * FROM tb_q", null);
//		curQ.moveToFirst();
		//int j=curQ.getInt(0);
		//tvS.setText(j);
		btnP.setText("" + curQ.getInt(0));
		btnS.setText(curQ.getString(1));
		btnJ.setText(curQ.getString(2));
		//image = curQ.getBlob(lastR);
		//Bitmap bmp = BitmapFactory.decodeByteArray(image, 0, image.length);
		//tv_img.setImageBitmap(bmp);


		//curQ.close();

		//Display display = getWindowManager().getDefaultDisplay();
		//Point size = new Point();
		//display.getSize(size);
		//int screenWidth = size.x;
		//int screenHeight = size.y;

		Display display = getWindowManager().getDefaultDisplay();
		screenWidth = display.getWidth();
		screenHeight = display.getHeight();

		if (line>0) {
			cadre.setVisibility(View.VISIBLE);

			//TextView textV = new TextView(this);
			cadre.setGravity(Gravity.LEFT);
			RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
					RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);

			//layoutParams.setMargins(121, 140, 0, 0);
			layoutParams.setMargins(point * (screenWidth/4), (line-1) * (screenHeight/15), 0, 0);
			//cadre.setLayoutParams(layoutParams);
			layoutParams.width =screenWidth/4;
			layoutParams.height =screenHeight/15;
			cadre.setLayoutParams(layoutParams);
			//text.setText("Result ");




			//cadre.setPadding(point * 10, line * 10, 0, 0);  // left, top, right, bottom
			//cadre.setPadding(25, 25, 25, 25);  // left, top, right, bottom
			//TextView textView = (TextView) findViewById(R.id.text_view);
			//cadre.setGravity(Gravity.CENTER);
			//cadre.setGravity(0);
			//cadre.setPaddingLeft(cadre, 10);
			SQLdb = openOrCreateDatabase("db_quran.sqlite", Context.MODE_PRIVATE, null);
			SQLdb.execSQL("UPDATE tb_indice SET line = " + 0 + "");

		}


		//int orientation = this.getResources().getConfiguration().orientation;
		//if (orientation == Configuration.ORIENTATION_PORTRAIT) {
			//Toast.makeText(getApplicationContext(), "Portrait", Toast.LENGTH_SHORT).show();
			//tv_img.setScaleType(ImageView.ScaleType.FIT_XY);
		//} else {
			//Toast.makeText(getApplicationContext(), "Land", Toast.LENGTH_SHORT).show();
		//tv_img.setScaleType(ImageView.ScaleType.CENTER_CROP);
		//}

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		final GestureDetector gestureDetector = new GestureDetector(new GestureDetector.SimpleOnGestureListener() {
			public void onLongPress(MotionEvent e) {
				//Toast.makeText(getApplicationContext(), "Land", Toast.LENGTH_SHORT).show();
			if (lastP > 0 && lastP < 605) {
				finish();
				Intent intent = new
						Intent(QMain.this, LandMark.class);
				startActivity(intent);
			}
				//Log.e("", "Longpress detected");
			}
		});

		viewPager.setOnTouchListener(new View.OnTouchListener() {
			private int CLICK_ACTION_THRESHOLD = 200;
			private float startX;
			private float startY;

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
					case MotionEvent.ACTION_DOWN:
						//Toast.makeText(getApplicationContext(), "Land DOWN", Toast.LENGTH_SHORT).show();
						startX = event.getX();
						startY = event.getY();
						break;


					case MotionEvent.ACTION_UP:
						//Toast.makeText(getApplicationContext(), "Land UP", Toast.LENGTH_SHORT).show();
						float endX = event.getX();
						float endY = event.getY();
						if (isAClick(startX, endX, startY, endY) && !isLandscape()) {
							handleTap(startX, startY);


						}
						break;
				}
				v.getParent().requestDisallowInterceptTouchEvent(true); //specific to my project
				//return false; //specific to my project
				return gestureDetector.onTouchEvent(event);
			}

			private boolean isAClick(float startX, float endX, float startY, float endY) {
				float differenceX = Math.abs(startX - endX);
				float differenceY = Math.abs(startY - endY);
				//Toast.makeText(QMain.this, startX+" "+endX+"  "+startY+" "+endY, Toast.LENGTH_LONG).show();
				return !(differenceX > CLICK_ACTION_THRESHOLD/* =5 */ || differenceY > CLICK_ACTION_THRESHOLD);
			}
		});


		viewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {

			// optional
			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

				//Toast.makeText(QMain.this, "position: "+position, Toast.LENGTH_SHORT).show();


			}

			// optional
			@Override
			public void onPageSelected(int position) {

				//lastP=608-position;

          /*      if (lastP>=10-position){
				   if (lastP == 10)
					  curQ.moveToFirst();
				   else
					   curQ.moveToPrevious();
				} else {
				   if (lastP == 0)
					  curQ.moveToFirst();
			    	else
					  curQ.moveToNext();
				}*/
				lastP = 608 - position;

				int j = 0;
				curQ.moveToFirst();
				do {
					if (curQ.getInt(0) == lastP) {
						//i = 366;
						break;
					}
					curQ.moveToNext();
					j++;
				} while (j < 609);
                if (curQ.getString(2).length()>8) Toast.makeText(QMain.this, curQ.getString(2) , Toast.LENGTH_SHORT).show();
				SQLdb = openOrCreateDatabase("db_quran.sqlite", Context.MODE_PRIVATE, null);
				SQLdb.execSQL("UPDATE tb_indice SET last_page = " + curQ.getInt(0) + "");
				btnP.setText("" + curQ.getInt(0));
				btnS.setText(curQ.getString(1));
				btnJ.setText(curQ.getString(2));
				cadre.setVisibility(View.GONE);

				//Toast.makeText(QMain.this, "lastP: " + lastP + "   pos: " + position, Toast.LENGTH_SHORT).show();
				if (curQ.getInt(0)== 0 || curQ.getInt(0)>604) {
					btnJ.setVisibility(View.GONE);
					btnP.setVisibility(View.GONE);
					btnS.setVisibility(View.GONE);
					btnM.setVisibility(View.GONE);
					btnAbout.setVisibility(View.GONE);
					btnA.setVisibility(View.GONE);
					btnT.setVisibility(View.GONE);
					btnH.setVisibility(View.GONE);
					btnW.setVisibility(View.GONE);
					btnF.setVisibility(View.GONE);
					//cadre.setVisibility(View.GONE);
					bar=0;
				}

			}

			// optional
			@Override
			public void onPageScrollStateChanged(int state) {
				if (state == ViewPager.SCROLL_STATE_IDLE) {
					int index = viewPager.getCurrentItem();
					if (index == 0)
						viewPager.setCurrentItem(adapter.getCount() - 1, false);
					else if (index == adapter.getCount() - 1)
						viewPager.setCurrentItem(0, false);
				}
				//Toast.makeText(QMain.this, " "+adapter.getCount() , Toast.LENGTH_SHORT).show();
			}
		});

		// ATTENTION: This was auto-generated to implement the App Indexing API.
		// See https://g.co/AppIndexing/AndroidStudio for more information.
		//	client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();









		btnW.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				currentImagePaths = utils.getFilePathsW();
				adapter = new QImage(QMain.this, currentImagePaths);
				adapter.setPageListener(pageListener);
				if (isLandscape()) {
					ArrayList<String> rev = reversePaths(currentImagePaths);
					contListSize = rev.size();
					continuousView.setImagePaths(rev, lastP);
				} else {
					viewPager.setAdapter(adapter);
					viewPager.setCurrentItem(608 - lastP);
				}
				SQLdb = openOrCreateDatabase("db_quran.sqlite", Context.MODE_PRIVATE, null);
				SQLdb.execSQL("UPDATE tb_indice SET last_riwaya = 2");
				btnW.setTypeface(null, Typeface.BOLD);
				btnW.setTextColor(Color.parseColor("#ffffff"));
				btnH.setTypeface(null, Typeface.NORMAL);
				btnH.setTextColor(Color.parseColor("#aea7a7"));
				Riwaya = "/QuranHW/WA/";
				btnJ.setVisibility(View.GONE);
				btnP.setVisibility(View.GONE);
				btnS.setVisibility(View.GONE);
				btnM.setVisibility(View.GONE);
				btnAbout.setVisibility(View.GONE);
				btnA.setVisibility(View.GONE);
				btnT.setVisibility(View.GONE);
				btnH.setVisibility(View.GONE);
				btnW.setVisibility(View.GONE);
				btnF.setVisibility(View.GONE);
				bar = 0;
			}
		});


		btnH.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				currentImagePaths = utils.getFilePaths();
				adapter = new QImage(QMain.this, currentImagePaths);
				adapter.setPageListener(pageListener);
				if (isLandscape()) {
					ArrayList<String> rev = reversePaths(currentImagePaths);
					contListSize = rev.size();
					continuousView.setImagePaths(rev, lastP);
				} else {
					viewPager.setAdapter(adapter);
					viewPager.setCurrentItem(608 - lastP);
				}
				SQLdb = openOrCreateDatabase("db_quran.sqlite", Context.MODE_PRIVATE, null);
				SQLdb.execSQL("UPDATE tb_indice SET last_riwaya = 1");
				btnH.setTypeface(null, Typeface.BOLD);
				btnH.setTextColor(Color.parseColor("#ffffff"));
				btnW.setTypeface(null, Typeface.NORMAL);
				btnW.setTextColor(Color.parseColor("#aea7a7"));
				Riwaya = "/QuranHW/HA/";
				btnJ.setVisibility(View.GONE);
				btnP.setVisibility(View.GONE);
				btnS.setVisibility(View.GONE);
				btnM.setVisibility(View.GONE);
				btnAbout.setVisibility(View.GONE);
				btnA.setVisibility(View.GONE);
				btnT.setVisibility(View.GONE);
				btnH.setVisibility(View.GONE);
				btnW.setVisibility(View.GONE);
				btnF.setVisibility(View.GONE);
				bar = 0;
			}
		});


		btnT.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				SQLdb = openOrCreateDatabase("db_quran.sqlite", Context.MODE_PRIVATE, null);
		//		Toast.makeText(getApplicationContext(), "s: " + curT.getInt(10) + "   a: " +  curT.getInt(11) , Toast.LENGTH_SHORT).show();
				SQLdb.execSQL("UPDATE tb_indice SET soraIndex = " + soraI + ", ayaIndex = " + ayaI + "" );
				//SQLdb.execSQL("UPDATE tb_indice SET soraIndex = " + soraI + "" );
				SQLdb.execSQL("UPDATE tb_indice SET ayaIndex = " + ayaI + "" );
				finish();
				Intent intent = new
						Intent(QMain.this, Tefcir.class);
				startActivity(intent);
		//		Toast.makeText(getApplicationContext(), "s: " + soraI + "   a: " + ayaI , Toast.LENGTH_SHORT).show();





		/*		btnJ.setVisibility(View.GONE);
				btnP.setVisibility(View.GONE);
				btnS.setVisibility(View.GONE);
				btnM.setVisibility(View.GONE);
				btnAbout.setVisibility(View.GONE);
				btnA.setVisibility(View.GONE);
				btnT.setVisibility(View.GONE);
				btnH.setVisibility(View.GONE);
				btnW.setVisibility(View.GONE);
				btnF.setVisibility(View.GONE);
				bar = 0;

				//ayaT.setVisibility(View.VISIBLE);
				//tefcir.setVisibility(View.VISIBLE);
				//if (lineI<8) {
					//ll21.setVisibility(View.GONE);
					//ll22.setVisibility(View.VISIBLE);
					//ViewGroup.LayoutParams params = ll22.getLayoutParams();
					//params.height = screenHeight * 45 / 100;
				//}else{
					ll21.setVisibility(View.VISIBLE);
				//	ll22.setVisibility(View.GONE);
					//ViewGroup.LayoutParams params = ll21.getLayoutParams();
					//params.height = screenHeight * 45 / 100;
				//}

				db.opendatabase();
				//Cursor curT;
				curT = db.tfrawQuery("SELECT * FROM tb_tafcir", null);
				curT.moveToFirst();

				int j = 1;
				do {
					if (curT.getInt(1) == soraI && curT.getInt(2) == ayaI) {
						//i = 366;
						break;
					}
					curT.moveToNext();
					j++;
				} while (j < 6237);
				//Toast.makeText(getApplicationContext(), "s: "+soraI+"   a: "+ayaI+"   t: "+curT.getString(3) , Toast.LENGTH_SHORT).show();

				ayaT.setText(curT.getString(3));
				tefcirT.setText(curT.getString(4));
				ayaB.setText(curT.getString(3));
				tefcirB.setText(curT.getString(4));
        */

			}
		});


		btnA.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (ctlAudio == 1) {
					ringTone.stop();
					ctlAudio = 0;
				}
				//String filePath = Environment.getExternalStorageDirectory()+ "/AudioQ/" + tone_name + ".mp3";
				//String filePath = Environment.getExternalStorageDirectory()+ "/AudioQ/عصفور.mp3";

				//String filePath = Environment.getExternalStorageDirectory() + RiwayaAudio + sora + ".wav";
				String filePath = Environment.getExternalStorageDirectory() + Riwaya + sora + ".wav";
				//String filePath = Environment.getExternalStorageState() + RiwayaAudio+sora+".wav";
				ringTone = new MediaPlayer();
				try {
					ringTone.setDataSource(filePath);
				} catch (IOException e) {
					e.printStackTrace();
				}
				try {
					ringTone.prepare();
				} catch (IOException e) {
					e.printStackTrace();
				}
				//if (mCurrentPosition > 0) {
				//	mVideoView.seekTo(mCurrentPosition);
				ringTone.seekTo(pos);
				//}

				//mVideoView.start();
				ringTone.start();
				ctlAudio = 1;
				ringTone.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
					@Override
					public void onCompletion(MediaPlayer mp) {
						ctlAudio = 0;
						stopAudioTracking();
						// Passer automatiquement à la sourate suivante
						playNextSora();
					}
				});
				startAudioTracking();
				//ringTone.callPlayerSeeked(startPos, endPos, false);

				btnJ.setVisibility(View.GONE);
				btnP.setVisibility(View.GONE);
				btnS.setVisibility(View.GONE);
				btnM.setVisibility(View.GONE);
				btnAbout.setVisibility(View.GONE);
				btnA.setVisibility(View.GONE);
				btnT.setVisibility(View.GONE);
				btnH.setVisibility(View.GONE);
				btnW.setVisibility(View.GONE);
				btnF.setVisibility(View.GONE);
				//cadre.setVisibility(View.GONE);
				bar = 0;


			}
		});


		btnM.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				//Toast.makeText(getApplicationContext(), "القائمة", Toast.LENGTH_SHORT).show();
				btnAbout.setVisibility(View.VISIBLE);

			}
		});


		btnAbout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				//Toast.makeText(getApplicationContext(), "حول", Toast.LENGTH_SHORT).show();
				finish();
				Intent intent = new
						Intent(QMain.this, About.class);
				startActivity(intent);
				//startActivity(new Intent(QMain.this, About.class));
				//break;

				//btnAbout.setVisibility(View.VISIBLE);
			/*	File sd = Environment.getExternalStorageDirectory();
				File data = Environment.getDataDirectory();
				FileChannel source=null;
				FileChannel destination=null;
				//in = assetManager.open("AudioQ/"+filename);   // if files resides inside the "Files" directory itself
				//out = new FileOutputStream(Environment.getExternalStorageDirectory().toString() +"/AudioQ/" + filename);
				//String currentDBPath = "<your sdcard path>";
				String currentDBPath = "/Eff/";
				//String backupDBPath = "data/data/"+ "<yourpackagename>" +"/databases/"+SAMPLE_DB_NAME;
				String backupDBPath = "/data/data/quran.hb.com.quran/databases/db_quran.sqlite";
				File currentDB = new File(sd, currentDBPath);
				File backupDB = new File(data, backupDBPath);
				try {
					source = new FileInputStream(currentDB).getChannel();
					destination = new FileOutputStream(backupDB).getChannel();
					destination.transferFrom(source, 0, source.size());
					source.close();
					destination.close();
					//Toast.makeText(this, "DB Exported!", Toast.LENGTH_LONG).show();
					Toast.makeText(getApplicationContext(), "DB Exported!", Toast.LENGTH_SHORT).show();
				} catch(IOException e) {
					e.printStackTrace();
				}*/


				//	Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.image4);
				//	ByteArrayOutputStream bos = new ByteArrayOutputStream();
				//	bitmap.compress(Bitmap.CompressFormat.PNG, 0 /*ignored for PNG*/, bos);
				//	byte[] bitmapdata = bos.toByteArray();

				//QLiteDatabase db=this.openOrCreateDatabase("imagedatabase", this.MODE_PRIVATE, null);
				//db.execSQL("CREATE TABLE IF NOT EXISTS imagetable (" + "_id INTEGER PRIMARY KEY AUTOINCREMENT," + "image BLOB" + ");");

				//ContentValues values = new ContentValues();
				//values.put("haf", bitmapdata);
				//long row_id=db.insert("imagetable", null, values);

				//	SQLdb = openOrCreateDatabase("db_quran.sqlite", Context.MODE_PRIVATE, null);
				//SQLdb.execSQL("UPDATE tb_q SET haf = " + curQ.getInt(0) + "");
				//	ContentValues values = new ContentValues();
				//	values.put("haf", bitmapdata);
				//long row_id=db.insert("imagetable", null, values);

				//	SQLdb.execSQL("UPDATE tb_q set haf=" + values + " WHERE id_q=4");


			}
		});


		btnS.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				//Toast.makeText(getApplicationContext(), "You clicked on OK", Toast.LENGTH_SHORT).show();
				finish();
				Intent intent = new
						Intent(QMain.this, IndexS.class);
				startActivity(intent);

			}
		});


		btnP.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				//Toast.makeText(getApplicationContext(), "You clicked on OK", Toast.LENGTH_SHORT).show();
				//case R.id.menu_modif_d:
				finish();
				Intent intent = new
						Intent(QMain.this, IndexP.class);
				startActivity(intent);


			}
		});


		btnJ.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				//Toast.makeText(getApplicationContext(), "You clicked on OK", Toast.LENGTH_SHORT).show();
				finish();
				Intent intent = new
						Intent(QMain.this, IndexJ.class);
				startActivity(intent);


			}
		});


		btnF.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				//Toast.makeText(getApplicationContext(), "You clicked on OK", Toast.LENGTH_SHORT).show();
				finish();
				Intent intent = new
						Intent(QMain.this, IndexF.class);
				        //Intent(QMain.this, SearchSQLiteActivity.class);
				startActivity(intent);


			}
		});




		btnAP.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (curT.getInt(0)==1) curT.moveToLast(); else curT.moveToPrevious();
				ayaT.setText(curT.getString(3));
				tefcirT.setText(curT.getString(4));
				//ayaB.setText(curT.getString(3));
				//tefcirB.setText(curT.getString(4));
				scrollV.smoothScrollTo(0,0);
				//scrollVb.smoothScrollTo(0,0);


			}
		});

		btnAN.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (curT.getInt(0)==6236) curT.moveToFirst(); else curT.moveToNext();
				ayaT.setText(curT.getString(3));
				tefcirT.setText(curT.getString(4));
				//ayaB.setText(curT.getString(3));
				//tefcirB.setText(curT.getString(4));
				scrollV.smoothScrollTo(0,0);
				//scrollVb.smoothScrollTo(0,0);

			}
		});


		btnAPb.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (curT.getInt(0)==1) curT.moveToLast(); else curT.moveToPrevious();
				//ayaT.setText(curT.getString(3));
				//tefcirT.setText(curT.getString(4));
				ayaB.setText(curT.getString(3));
				tefcirB.setText(curT.getString(4));
				//scrollV.smoothScrollTo(0,0);
				scrollVb.smoothScrollTo(0,0);

			}
		});

		btnANb.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (curT.getInt(0)==6236) curT.moveToFirst(); else curT.moveToNext();
				//ayaT.setText(curT.getString(3));
				//tefcirT.setText(curT.getString(4));
				ayaB.setText(curT.getString(3));
				tefcirB.setText(curT.getString(4));
				//scrollV.smoothScrollTo(0,0);
				scrollVb.smoothScrollTo(0,0);

			}
		});

		btnRetour.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				ll21.setVisibility(View.GONE);

			}
		});



	}
















	private void CopyAssetsHI() {
		AssetManager assetManager = getAssets();
		String[] files = null;
		try {
			files = assetManager.list("HI");
		} catch (IOException e) {
			Log.e("tag", e.getMessage());
		}

		for (String filename : files) {
			System.out.println("File name => " + filename);
			InputStream in = null;
			OutputStream out = null;
			try {
				in = assetManager.open("HI/" + filename);   // if files resides inside the "Files" directory itself
				out = new FileOutputStream(Environment.getExternalStorageDirectory().toString() + "/QuranHW/HI/" + filename);
				//out = new FileOutputStream(Environment.getExternalStorageState().toString() +"/AudioQ/" + filename);
				copyFile(in, out);
				in.close();
				in = null;
				out.flush();
				out.close();
				out = null;
			} catch (Exception e) {
				Log.e("tag", e.getMessage());
			}
		}
	}

	private void CopyAssetsHA() {
		AssetManager assetManager = getAssets();
		String[] files = null;
		try {
			files = assetManager.list("HA");
		} catch (IOException e) {
			Log.e("tag", e.getMessage());
		}

		for (String filename : files) {
			System.out.println("File name => " + filename);
			InputStream in = null;
			OutputStream out = null;
			try {
				in = assetManager.open("HA/" + filename);   // if files resides inside the "Files" directory itself
				out = new FileOutputStream(Environment.getExternalStorageDirectory().toString() + "/QuranHW/HA/" + filename);
				//out = new FileOutputStream(Environment.getExternalStorageState().toString() +"/AudioQ/" + filename);
				copyFile(in, out);
				in.close();
				in = null;
				out.flush();
				out.close();
				out = null;
			} catch (Exception e) {
				Log.e("tag", e.getMessage());
			}
		}
	}


	private void CopyAssetsWI() {
		AssetManager assetManager = getAssets();
		String[] files = null;
		try {
			files = assetManager.list("WI");
		} catch (IOException e) {
			Log.e("tag", e.getMessage());
		}

		for (String filename : files) {
			System.out.println("File name => " + filename);
			InputStream in = null;
			OutputStream out = null;
			try {
				in = assetManager.open("WI/" + filename);   // if files resides inside the "Files" directory itself
				out = new FileOutputStream(Environment.getExternalStorageDirectory().toString() + "/QuranHW/WI/" + filename);
				//out = new FileOutputStream(Environment.getExternalStorageState().toString() +"/AudioQ/" + filename);
				copyFile(in, out);
				in.close();
				in = null;
				out.flush();
				out.close();
				out = null;
			} catch (Exception e) {
				Log.e("tag", e.getMessage());
			}
		}
	}


	private void CopyAssetsWA() {
		AssetManager assetManager = getAssets();
		String[] files = null;
		try {
			files = assetManager.list("WA");
		} catch (IOException e) {
			Log.e("tag", e.getMessage());
		}

		for (String filename : files) {
			System.out.println("File name => " + filename);
			InputStream in = null;
			OutputStream out = null;
			try {
				in = assetManager.open("WA/" + filename);   // if files resides inside the "Files" directory itself
				out = new FileOutputStream(Environment.getExternalStorageDirectory().toString() + "/QuranHW/WA/" + filename);
				//out = new FileOutputStream(Environment.getExternalStorageState().toString() +"/AudioQ/" + filename);
				copyFile(in, out);
				in.close();
				in = null;
				out.flush();
				out.close();
				out = null;
			} catch (Exception e) {
				Log.e("tag", e.getMessage());
			}
		}
	}


	/** Orientation horizontale : le defilement et le clic sont geres par PageScrollView */
	private boolean isLandscape() {
		return getResources().getConfiguration().orientation
				== android.content.res.Configuration.ORIENTATION_LANDSCAPE;
	}

	/**
	 * Clic sur une page : startX / startY sont des coordonnees "ecran" (0..screenWidth, 0..screenHeight)
	 * qui servent a retrouver la ligne, le point, la sourate et l'aya.
	 */
	private void handleTap(float startX, float startY) {
							cadre.setVisibility(View.GONE);
							if (ctlAudio==1){
								ringTone.stop();
								ctlAudio=0;
							}

							// launchFullPhotoActivity(imageUrls);// WE HAVE A CLICK!!
							//Toast.makeText(QMain.this, "screenHeight: " + screenHeight + "     screenWidth: " + screenWidth, Toast.LENGTH_LONG).show();

							//if (bar == 0 && curQ.getInt(0)>0 && curQ.getInt(0)<605) {
							if (bar == 0 && curQ.getInt(0) > 0 && curQ.getInt(0) < 605) {
								//if (bar == 0) {
								//tvB.setVisibility(View.VISIBLE);
								btnJ.setVisibility(View.VISIBLE);
								btnP.setVisibility(View.VISIBLE);
								btnS.setVisibility(View.VISIBLE);
								btnM.setVisibility(View.VISIBLE);
								btnA.setVisibility(View.VISIBLE);
								btnT.setVisibility(View.VISIBLE);
								btnH.setVisibility(View.VISIBLE);
								btnW.setVisibility(View.VISIBLE);
								btnF.setVisibility(View.VISIBLE);
								bar = 1;

								// retrieve the stored coordinates
								float x = startX;//lastTouchDownXY[0];
								float y = startY;//lastTouchDownXY[1];
								if (0 < y && y < ((screenHeight / 15) * 1) + 1) {
									y1 = 1;
								} else if ((screenHeight / 15) * 1 < y && y < ((screenHeight / 15) * 2) + 1) {
									y1 = 2;
								} else if ((screenHeight / 15) * 2 < y && y < ((screenHeight / 15) * 3) + 1) {
									y1 = 3;
								} else if ((screenHeight / 15) * 3 < y && y < ((screenHeight / 15) * 4) + 1) {
									y1 = 4;
								} else if ((screenHeight / 15) * 4 < y && y < ((screenHeight / 15) * 5) + 1) {
									y1 = 5;
								} else if ((screenHeight / 15) * 5 < y && y < ((screenHeight / 15) * 6) + 1) {
									y1 = 6;
								} else if ((screenHeight / 15) * 6 < y && y < ((screenHeight / 15) * 7) + 1) {
									y1 = 7;
								} else if ((screenHeight / 15) * 7 < y && y < ((screenHeight / 15) * 8) + 1) {
									y1 = 8;
								} else if ((screenHeight / 15) * 8 < y && y < ((screenHeight / 15) * 9) + 1) {
									y1 = 9;
								} else if ((screenHeight / 15) * 9 < y && y < ((screenHeight / 15) * 10) + 1) {
									y1 = 10;
								} else if ((screenHeight / 15) * 10 < y && y < ((screenHeight / 15) * 11) + 1) {
									y1 = 11;
								} else if ((screenHeight / 15) * 11 < y && y < ((screenHeight / 15) * 12) + 1) {
									y1 = 12;
								} else if ((screenHeight / 15) * 12 < y && y < ((screenHeight / 15) * 13) + 1) {
									y1 = 13;
								} else if ((screenHeight / 15) * 13 < y && y < ((screenHeight / 15) * 14) + 1) {
									y1 = 14;
								} else if ((screenHeight / 15) * 14 < y && y < ((screenHeight / 15) * 15) + 1) {
									y1 = 15;
								}

								if (0 < x && x < ((screenWidth / 4) * 1) + 1) {
									x1 = 4;
								} else if ((screenWidth / 4) * 1 < x && x < ((screenWidth / 4) * 2) + 1) {
									x1 = 3;
								} else if ((screenWidth / 4) * 2 < x && x < ((screenWidth / 4) * 3) + 1) {
									x1 = 2;
								} else if ((screenWidth / 4) * 3 < x && x < ((screenWidth / 4) * 4) + 1) {
									x1 = 1;
								}

								//if (0 < x && x < ((screenWidth / 8) * 1) + 1) {x1 = 8;
								//} else if ((screenWidth / 8) * 1 < x && x < ((screenWidth / 8) * 2) + 1) {x1 = 7;
								//} else if ((screenWidth / 8) * 2 < x && x < ((screenWidth / 8) * 3) + 1) {x1 = 6;
								//} else if ((screenWidth / 8) * 3 < x && x < ((screenWidth / 8) * 4) + 1) {x1 = 5;
								//} else if ((screenWidth / 8) * 4 < x && x < ((screenWidth / 8) * 5) + 1) {x1 = 4;
								//} else if ((screenWidth / 8) * 5 < x && x < ((screenWidth / 8) * 6) + 1) {x1 = 3;
								//} else if ((screenWidth / 8) * 6 < x && x < ((screenWidth / 8) * 7) + 1) {x1 = 2;
								//} else if ((screenWidth / 8) * 7 < x && x < ((screenWidth / 8) * 8) + 1) {x1 = 1;
								//}
								//btnS.setText("x: " + x1 + "    y: " + y1);
								//x1=((Page -1) x 120) + ((Ligne - 1) x 8) + (PointClick)        (Row0: Titre)

								int click_page = curQ.getInt(0);
								int click_line = y1;
								int click_point = x1;

								//x1=(curQ.getInt(0)-1)*120+(y1-1)*8+x1;
								x1 = (curQ.getInt(0) - 1) * 60 + (y1 - 1) * 4 + x1+1;
								//btnS.setText("" + x1);

                                Cursor curS;
								db.opendatabase();
								curS = db.srrawQuery("SELECT * FROM tb_sora" , null);
								curS.moveToFirst();
								int j = 1;
								do {
									if (curS.getInt(3) > x1) {
										//i = 366;
										break;
									}
									curS.moveToNext();
									j++;
								} while (j < 115);
								curS.moveToPrevious();
								x1=x1-curS.getInt(3);
								sora="s"+(j - 1);
								curS.close();
								//btnS.setText(sora);

								// --- Find sora/aya from click position using tb_tafcir/tb_tafcirW ---
								// Logic: find the LAST aya whose start position (p_sora,line,point) is
								// <= clicked position. Since (p_sora,line,point) has a few duplicates,
								// we take the one with the highest tefcir_id (= last inserted = last aya).
								// Query 1: on the clicked page, at or before the clicked line/point.
								// Query 2: fallback to last aya of any previous page.
								int bestSora = 1;
								int bestAya  = 1;

								String tbl = (lastR == 1) ? "tb_tafcir" : "tb_tafcirW";

								// Build a raw DB query directly (not through DbHelper cursor helpers
								// so we can pass a real WHERE clause)
								try {
									SQLiteDatabase rawDb = android.database.sqlite.SQLiteDatabase.openDatabase(
										"/data/data/quran.hb.com.quran/databases/db_quran.sqlite",
										null, android.database.sqlite.SQLiteDatabase.OPEN_READONLY);

									// Rows on the clicked page with position <= click position
									String q1 = "SELECT sora, aya FROM " + tbl
										+ " WHERE p_sora = " + click_page
										+ " AND (line * 4 + point) <= " + (click_line * 4 + click_point)
										+ " ORDER BY line DESC, point DESC, tefcir_id DESC LIMIT 1";
									Cursor c1 = rawDb.rawQuery(q1, null);
									if (c1 != null && c1.moveToFirst()) {
										bestSora = c1.getInt(0);
										bestAya  = c1.getInt(1);
										c1.close();
									} else {
										// Fallback: last aya of any page before the clicked page
										if (c1 != null) c1.close();
										String q2 = "SELECT sora, aya FROM " + tbl
											+ " WHERE p_sora < " + click_page
											+ " ORDER BY p_sora DESC, line DESC, point DESC, tefcir_id DESC LIMIT 1";
										Cursor c2 = rawDb.rawQuery(q2, null);
										if (c2 != null && c2.moveToFirst()) {
											bestSora = c2.getInt(0);
											bestAya  = c2.getInt(1);
										}
										if (c2 != null) c2.close();
									}
									rawDb.close();
								} catch (Exception eDb) {
									eDb.printStackTrace();
								}

								soraI = bestSora;
								ayaI  = bestAya;


								try {
									//Workbook wb = Workbook.getWorkbook(new File(Environment.getExternalStorageDirectory().getAbsolutePath() + RiwayaExel));
									  Workbook wb = Workbook.getWorkbook(new File(Environment.getExternalStorageDirectory().getAbsolutePath() + Riwaya + sora + ".xls"));
									//Workbook wb = Workbook.getWorkbook(new File(Environment.getExternalStorageState() + RiwayaExel));
									Sheet s = wb.getSheet(0);

									//Cell z = s.getCell(3, x1);
									//sora = z.getContents();

									Cell z = s.getCell(4, x1);
									pos = new Integer(z.getContents().toString());

									// We don't need ayaI from xls anymore because it's retrieved from SQLite above!
									//z = s.getCell(5, x1);
									//ayaI = new Integer(z.getContents().toString());

									z = s.getCell(1, x1);
									lineI = new Integer(z.getContents().toString());

									//btnS.setText(sora + "  " + pos + "  " + ayaI + "  " + lineI);
								} catch (Exception e) {
								}

							} else {
								//tvB.setVisibility(View.GONE);
								btnJ.setVisibility(View.GONE);
								btnP.setVisibility(View.GONE);
								btnS.setVisibility(View.GONE);
								btnM.setVisibility(View.GONE);
								btnAbout.setVisibility(View.GONE);
								btnA.setVisibility(View.GONE);
								btnT.setVisibility(View.GONE);
								btnH.setVisibility(View.GONE);
								btnW.setVisibility(View.GONE);
								btnF.setVisibility(View.GONE);
								//cadre.setVisibility(View.GONE);
								bar = 0;


							}
							ll21.setVisibility(View.GONE);
							ll22.setVisibility(View.GONE);
							//Toast.makeText(QMain.this, "x: " + x1 + "    y: " + y1, Toast.LENGTH_LONG).show();
			}
		}
	}

	/**
	 * Retourne une copie inversée de la liste de chemins.
	 * En mode paysage PDF : index 0 = 1ère page Coran (p1), scroll BAS = page suivante.
	 */
	private ArrayList<String> reversePaths(ArrayList<String> paths) {
		ArrayList<String> reversed = new ArrayList<String>(paths);
		Collections.reverse(reversed);
		return reversed;
	}

	/** Pageur : clic et passage de page par defilement en mode horizontal */
	private final QImage.PageListener pageListener = new QImage.PageListener() {
		@Override
		public void onPageTap(float fracX, float fracY) {
			int wc = (screenWidth / 4) * 4;
			int hc = (screenHeight / 15) * 15;
			float x = Math.max(1f, Math.min((float) wc, fracX * wc));
			float y = Math.max(1f, Math.min((float) hc, fracY * hc));
			handleTap(x, y);
		}

		@Override
		public void onFlipNext(int position) {
			if (viewPager.getCurrentItem() != position || position - 1 < 0) return;
			adapter.prepareScroll(position - 1, true);
			viewPager.setCurrentItem(position - 1, true);
		}

		@Override
		public void onFlipPrev(int position) {
			if (viewPager.getCurrentItem() != position || position + 1 > adapter.getCount() - 1) return;
			adapter.prepareScroll(position + 1, false);
			viewPager.setCurrentItem(position + 1, true);
		}
	};

	/**
	 * Listener pour ContinuousPageView en mode paysage :
	 * met à jour la barre (sora/page/juz) quand l'utilisateur fait défiler.
	 */
	private final ContinuousPageView.PageChangeListener continuousPageListener =
			new ContinuousPageView.PageChangeListener() {
		@Override
		public void onPageChanged(int pagerIndex) {
			// Liste inversée : index 0 = p1, donc lastP = pagerIndex + 1
			int newLastP = pagerIndex + 1;
			if (contListSize > 0) {
				// Sécurité : recalcul générique si la taille diffère de 608
				newLastP = 608 - (contListSize - 1 - pagerIndex);
			}
			if (newLastP == lastP) return;
			lastP = newLastP;

			// Mettre à jour curQ pour afficher sora/page/juz
			int j = 0;
			curQ.moveToFirst();
			do {
				if (curQ.getInt(0) == lastP) break;
				curQ.moveToNext();
				j++;
			} while (j < 609);

			if (curQ.getString(2).length() > 8)
				Toast.makeText(QMain.this, curQ.getString(2), Toast.LENGTH_SHORT).show();

			SQLdb = openOrCreateDatabase("db_quran.sqlite", Context.MODE_PRIVATE, null);
			SQLdb.execSQL("UPDATE tb_indice SET last_page = " + curQ.getInt(0));
			btnP.setText("" + curQ.getInt(0));
			btnS.setText(curQ.getString(1));
			btnJ.setText(curQ.getString(2));
			cadre.setVisibility(View.GONE);

			if (curQ.getInt(0) == 0 || curQ.getInt(0) > 604) {
				btnJ.setVisibility(View.GONE);
				btnP.setVisibility(View.GONE);
				btnS.setVisibility(View.GONE);
				btnM.setVisibility(View.GONE);
				btnAbout.setVisibility(View.GONE);
				btnA.setVisibility(View.GONE);
				btnT.setVisibility(View.GONE);
				btnH.setVisibility(View.GONE);
				btnW.setVisibility(View.GONE);
				btnF.setVisibility(View.GONE);
				bar = 0;
			}
		}

		@Override
		public void onTap(float fracX, float fracY) {
			int wc = (screenWidth / 4) * 4;
			int hc = (screenHeight / 15) * 15;
			float x = Math.max(1f, Math.min((float) wc, fracX * wc));
			float y = Math.max(1f, Math.min((float) hc, fracY * hc));
			handleTap(x, y);
		}
	};

	// ---------------------------------------------------------------
	// Tilawa : surlignage dynamique de la partie en cours + changement de page
	// ---------------------------------------------------------------
	private final Handler audioHandler = new Handler();
	private final Runnable audioTicker = new Runnable() {
		@Override
		public void run() {
			audioTick();
		}
	};
	private int[] tPage = new int[0], tLine = new int[0], tPoint = new int[0], tStart = new int[0];
	private int tCount = 0;
	private int audioIdx = -1;

	private static int toInt(String t) {
		try {
			return (int) Double.parseDouble(t.trim().replace(',', '.'));
		} catch (Exception e) {
			return 0;
		}
	}

	/** Lit le fichier .xls de la sourate : page, ligne, point, debut (ms) de chaque cellule */
	private boolean loadTimings() {
		tCount = 0;
		try {
			Workbook wb = Workbook.getWorkbook(new File(Environment.getExternalStorageDirectory().getAbsolutePath() + Riwaya + sora + ".xls"));
			Sheet sh = wb.getSheet(0);
			int n = sh.getRows() - 1;      // la ligne 0 est l'en-tete
			if (n <= 0) {
				wb.close();
				return false;
			}
			tPage = new int[n];
			tLine = new int[n];
			tPoint = new int[n];
			tStart = new int[n];
			for (int r = 1; r <= n; r++) {
				tPage[r - 1] = toInt(sh.getCell(0, r).getContents());
				tLine[r - 1] = toInt(sh.getCell(1, r).getContents());
				tPoint[r - 1] = toInt(sh.getCell(2, r).getContents());
				tStart[r - 1] = toInt(sh.getCell(4, r).getContents());
			}
			tCount = n;
			wb.close();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	private void startAudioTracking() {
		audioHandler.removeCallbacks(audioTicker);
		audioIdx = -1;
		if (loadTimings()) {
			audioHandler.post(audioTicker);
		}
	}

	private void stopAudioTracking() {
		audioHandler.removeCallbacks(audioTicker);
		audioIdx = -1;
		if (adapter != null) adapter.clearHighlight();
		if (continuousView != null) continuousView.clearHighlight();
	}

	/**
	 * Passe à la sourate suivante (s1..s114 cycliquement) et lance son audio automatiquement.
	 * Cherche la première page de la sourate dans tb_tafcir (colonne p_sora).
	 */
	private void playNextSora() {
		if (sora == null || sora.isEmpty()) return;
		try {
			int currentNum = Integer.parseInt(sora.substring(1)); // "s5" -> 5
			final int nextNum = (currentNum >= 114) ? 1 : currentNum + 1;
			final String nextSoraName = "s" + nextNum;

			// Chercher la première page de la sourate dans tb_tafcir (p_sora = numéro de page)
			// ou dans tb_tafcirW selon la riwaya courante
			final int[] targetPage = {-1};
			try {
				android.database.sqlite.SQLiteDatabase rawDb =
						android.database.sqlite.SQLiteDatabase.openDatabase(
						"/data/data/quran.hb.com.quran/databases/db_quran.sqlite",
						null, android.database.sqlite.SQLiteDatabase.OPEN_READONLY);
				String tbl = (lastR == 1) ? "tb_tafcir" : "tb_tafcirW";
				// p_sora = numéro de page Coran pour cet ayat
				Cursor cPage = rawDb.rawQuery(
					"SELECT p_sora FROM " + tbl + " WHERE sora = " + nextNum
					+ " ORDER BY aya ASC LIMIT 1", null);
				if (cPage != null && cPage.moveToFirst()) {
					targetPage[0] = cPage.getInt(0);
					cPage.close();
				} else {
					if (cPage != null) cPage.close();
				}
				rawDb.close();
			} catch (Exception ignored) {}

			// Poster sur le thread UI
			audioHandler.post(new Runnable() {
				@Override
				public void run() {
					sora = nextSoraName;
					pos = 0;
					// Naviguer vers la première page si trouvée
					if (targetPage[0] > 0 && targetPage[0] <= 604) {
						if (isLandscape()) {
							// Liste inversée : reversed[targetPage[0]] = p{targetPage[0]}
							continuousView.scrollToIndex(targetPage[0], true);
						} else {
							viewPager.setCurrentItem(608 - targetPage[0], true);
						}
					}
					// Lancer l'audio de la sourate suivante
					String filePath = android.os.Environment.getExternalStorageDirectory()
							+ Riwaya + sora + ".wav";
					if (ringTone != null) {
						try { ringTone.release(); } catch (Exception ignored) {}
					}
					ringTone = new MediaPlayer();
					try {
						ringTone.setDataSource(filePath);
						ringTone.prepare();
						ringTone.start();
						ctlAudio = 1;
						ringTone.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
							@Override
							public void onCompletion(MediaPlayer mp) {
								ctlAudio = 0;
								stopAudioTracking();
								// Chaîne récursive : passer à la suivante
								playNextSora();
							}
						});
						startAudioTracking();
					} catch (Exception e) {
						ctlAudio = 0;
					}
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void audioTick() {
		if (ctlAudio != 1 || ringTone == null || tCount == 0) {
			stopAudioTracking();
			return;
		}
		int t;
		try {
			t = ringTone.getCurrentPosition();
		} catch (Exception e) {
			stopAudioTracking();
			return;
		}
		// cellule en cours = celle dont le debut (non nul) est le plus grand sans depasser la position
		int best = -1, bestStart = -1;
		for (int i = 0; i < tCount; i++) {
			if (tStart[i] > 0 && tStart[i] <= t && tStart[i] > bestStart) {
				best = i;
				bestStart = tStart[i];
			}
		}
		if (best < 0) best = 0;

		int item = 608 - tPage[best];
		if (isLandscape()) {
			// Mode paysage : liste inversée, index dans ContinuousPageView = tPage[best]-1
			int continuousIdx = tPage[best]; // reversed[tPage[best]] = p{tPage[best]}.jpg
			if (best != audioIdx) {
				audioIdx = best;
				// Auto-scroll géré par applyHighlight
			}
			continuousView.setHighlight(continuousIdx, tLine[best], tPoint[best]);
			continuousView.applyHighlight();
		} else {
			// Mode portrait : surlignage sur QImage (ViewPager)
			if (best != audioIdx) {
				audioIdx = best;
				if (item >= 0 && item < adapter.getCount() && viewPager.getCurrentItem() != item) {
					viewPager.setCurrentItem(item, true);
				}
			}
			adapter.setHighlight(item, tLine[best], tPoint[best]);
			adapter.applyHighlight();
		}
		audioHandler.postDelayed(audioTicker, 150);
	}

	@Override
	protected void onDestroy() {
		audioHandler.removeCallbacks(audioTicker);
		super.onDestroy();
	}


	private void copyFile(InputStream in, OutputStream out) throws IOException {
		byte[] buffer = new byte[1024];
		int read;
		while ((read = in.read(buffer)) != -1) {
			out.write(buffer, 0, read);
		}
	}



	@Override
	public boolean onKeyUp( int keyCode, KeyEvent event )
	{


		if(keyCode == KeyEvent.KEYCODE_BACK)
		{
			if (ctlAudio==1){
				ringTone.stop();
				ctlAudio=0;
			}
		}
		else if(keyCode == KeyEvent.KEYCODE_HOME)
		{
			if (ctlAudio==1){
				ringTone.stop();
				ctlAudio=0;
			}
		}

	/*	if( keyCode == KeyEvent.KEYCODE_BACK )
		{
			//SQLdb = openOrCreateDatabase("db_quran.sqlite", Context.MODE_PRIVATE, null);
			//SQLdb.execSQL("UPDATE tb_indice SET last_page = " + lastP + "");
			this.finish();
			//startActivity(new Intent(this, QMain.class));
			if (ctlAudio==1){
				ringTone.stop();
				ctlAudio=0;
			}
			return true;
		}*/
		//this.finish();
		return super.onKeyUp( keyCode, event );
	}




	/**
	 * ATTENTION: This was auto-generated to implement the App Indexing API.
	 * See https://g.co/AppIndexing/AndroidStudio for more information.
	 */
/*	public Action getIndexApiAction() {
		Thing object = new Thing.Builder()
				.setName("FullScreenView Page") // TODO: Define a title for the content shown.
				// TODO: Make sure this auto-generated URL is correct.
				.setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
				.build();
		return new Action.Builder(Action.TYPE_VIEW)
				.setObject(object)
				.setActionStatus(Action.STATUS_TYPE_COMPLETED)
				.build();
	}

	@Override
	public void onStart() {
		super.onStart();

		// ATTENTION: This was auto-generated to implement the App Indexing API.
		// See https://g.co/AppIndexing/AndroidStudio for more information.
		client.connect();
		AppIndex.AppIndexApi.start(client, getIndexApiAction());
	}

	@Override
	public void onStop() {
		super.onStop();

		// ATTENTION: This was auto-generated to implement the App Indexing API.
		// See https://g.co/AppIndexing/AndroidStudio for more information.
		AppIndex.AppIndexApi.end(client, getIndexApiAction());
		client.disconnect();
	}*/
}
