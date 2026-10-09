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

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;

public class QMain extends Activity {

	private Utils utils;
	private QImage adapter;
	private ViewPager viewPager;

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

		viewPager = (ViewPager) findViewById(R.id.vPager);


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
			//RiwayaExel = "/AudioQ/Hafs.xls";
			//RiwayaAudio = "/AudioQ/Hafs/";
			Riwaya = "/QuranHW/HA/";
			btnH.setTypeface(null, Typeface.BOLD);
			btnH.setTextColor(Color.parseColor("#ffffff"));
			btnW.setTypeface(null, Typeface.NORMAL);
			btnW.setTextColor(Color.parseColor("#aea7a7"));
			adapter = new QImage(QMain.this, utils.getFilePaths());
		} else {
			//RiwayaExel = "/AudioQ/Warsh.xls";
			//RiwayaAudio = "/AudioQ/Warsh/";
			Riwaya = "/QuranHW/WA/";
			btnH.setTypeface(null, Typeface.NORMAL);
			btnH.setTextColor(Color.parseColor("#aea7a7"));
			btnW.setTypeface(null, Typeface.BOLD);
			btnW.setTextColor(Color.parseColor("#ffffff"));
			adapter = new QImage(QMain.this, utils.getFilePathsW());
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
		viewPager.setAdapter(adapter);
		viewPager.setCurrentItem(608 - lastP);

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
						if (isAClick(startX, endX, startY, endY)) {
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
								soraI=j-1;
								sora="s"+soraI;
								curS.close();
								//btnS.setText(sora);

								try {
									//Workbook wb = Workbook.getWorkbook(new File(Environment.getExternalStorageDirectory().getAbsolutePath() + RiwayaExel));
									  Workbook wb = Workbook.getWorkbook(new File(Environment.getExternalStorageDirectory().getAbsolutePath() + Riwaya + sora + ".xls"));
									//Workbook wb = Workbook.getWorkbook(new File(Environment.getExternalStorageState() + RiwayaExel));
									Sheet s = wb.getSheet(0);

									//Cell z = s.getCell(3, x1);
									//sora = z.getContents();

									Cell z = s.getCell(4, x1);
									pos = new Integer(z.getContents().toString());

									z = s.getCell(5, x1);
									ayaI = new Integer(z.getContents().toString());

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

				adapter = new QImage(QMain.this, utils.getFilePathsW());
				viewPager.setAdapter(adapter);
				viewPager.setCurrentItem(608 - lastP);

				//Toast.makeText(getApplicationContext(),	"You clicked on OK", Toast.LENGTH_SHORT).show();
				SQLdb = openOrCreateDatabase("db_quran.sqlite", Context.MODE_PRIVATE, null);
				SQLdb.execSQL("UPDATE tb_indice SET last_riwaya = 2");
				btnW.setTypeface(null, Typeface.BOLD);
				btnW.setTextColor(Color.parseColor("#ffffff"));
				btnH.setTypeface(null, Typeface.NORMAL);
				btnH.setTextColor(Color.parseColor("#aea7a7"));
				//image = curQ.getBlob(2);
				//Bitmap bmp = BitmapFactory.decodeByteArray(image, 0, image.length);
				//tv_img.setImageBitmap(bmp);

				//RiwayaExel = "/AudioQ/Warsh.xls";
				//RiwayaAudio = "/AudioQ/Warsh/";
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
				//cadre.setVisibility(View.GONE);
				bar = 0;

			}
		});


		btnH.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {

				adapter = new QImage(QMain.this, utils.getFilePaths());
				viewPager.setAdapter(adapter);
				viewPager.setCurrentItem(608 - lastP);

				SQLdb = openOrCreateDatabase("db_quran.sqlite", Context.MODE_PRIVATE, null);
				SQLdb.execSQL("UPDATE tb_indice SET last_riwaya = 1");
				btnH.setTypeface(null, Typeface.BOLD);
				btnH.setTextColor(Color.parseColor("#ffffff"));
				btnW.setTypeface(null, Typeface.NORMAL);
				btnW.setTextColor(Color.parseColor("#aea7a7"));
				//image = curQ.getBlob(1);
				//Bitmap bmp = BitmapFactory.decodeByteArray(image, 0, image.length);
				//tv_img.setImageBitmap(bmp);
				//RiwayaExel = "/AudioQ/Hafs.xls";
				//RiwayaAudio = "/AudioQ/Hafs/";
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
				//cadre.setVisibility(View.GONE);
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
