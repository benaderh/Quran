package quran.hb.com.quran;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.io.IOException;
import java.util.ArrayList;

/**
 * activity to display all records from SQLite database
 * @author ketan(Visit my <a
 *         href="http://androidsolution4u.blogspot.in/">blog</a>)
 */
public class IndexP extends Activity {

	private DbHelper mHelper;
	private SQLiteDatabase dataBase;
	SQLiteDatabase SQLdb;
	//long dgL;
	//private ArrayList<String> user_Page0 = new ArrayList<String>();
	private ArrayList<String> user_Page = new ArrayList<String>();
	private ArrayList<String> user_Sora = new ArrayList<String>();
	//private ArrayList<String> user_NJ = new ArrayList<String>();
	int  i;
	int j=0;
	//DbHelper db;
    int lastP;

	private ListView userList;
	private AlertDialog.Builder build;
	//Button BtnHD;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.index_p);
		//userList.smoothScrollToPosition(40);
		userList = (ListView) findViewById(R.id.lvPageI);
		//userList.smoothScrollToPosition(40);
		//TextView textView = new TextView(context);
		//textView.setText("Hello. I'm a header view");


		//userList.addHeaderView(textView);
		try {
			mHelper = new DbHelper(this);
		} catch (IOException e) {
			e.printStackTrace();
		}
		//userList.smoothScrollToPosition(40);

		Cursor curI;
		mHelper.opendatabase();
		curI = mHelper.inrawQuery("SELECT * FROM tb_indice", null);
		curI.moveToFirst();
		lastP = curI.getInt(0);
		curI.close();

	/*	//add new record
		findViewById(R.id.btnAdd).setOnClickListener(new OnClickListener() {

			public void onClick(View v) {

				Intent i = new Intent(getApplicationContext(),
						AddActivity.class);
				i.putExtra("update", false);
				startActivity(i);

			}
		});*/
		
	/*	//click to update data
		userList.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {

				Intent i = new Intent(getApplicationContext(),
						AddActivity.class);
				i.putExtra("Fname", user_fName.get(arg2));
				i.putExtra("Lname", user_lName.get(arg2));
				i.putExtra("ID", userId.get(arg2));
				i.putExtra("update", true);
				startActivity(i);

			}
		});*/
		//dateg=dateg.substring( dateg.length()-6, dateg.length());
		//long click to delete data
		userList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			public void onItemClick(AdapterView<?> arg0, View arg1, final int arg2, long arg3) {
				//Toast.makeText( "Update "+mCursor.getString(mCursor.getColumnIndex(DbHelper.KEY_Page)), Toast.LENGTH_LONG).show();
				//Toast.makeText(ModifD.this, j, Toast.LENGTH_SHORT).show();
				//Toast.makeText(ModifD.this, userList.get(position)+"", Toast.LENGTH_SHORT).show();
				//Toast.makeText(getApplicationContext(),	"Page: "  + user_Page.get(arg2) + " J: " + j, Toast.LENGTH_LONG).show();
				//lastP=user_Page.get(arg2);//Toast.makeText(mContext, "Delete "+position, Toast.LENGTH_LONG).show();
				lastP= Integer.parseInt(user_Page.get(arg2) );
				SQLdb = mHelper.openWritableDb();
				SQLdb.execSQL("UPDATE tb_indice SET last_page = " + lastP + "");
				finish();
				//startActivity(new Intent(this, QMain.class));
				//Intent intent = new Intent(this, QMain.class);
                Intent intent = new Intent(getApplicationContext(), QMain.class);
                startActivity(intent);
				//startActivity(intent);
				//String value = userList.getAdapter().getItem(position).toString();
				//userList.smoothScrollToPosition(40);
	/*			build = new AlertDialog.Builder(ModifD.this);
				build.setTitle("طھط¨ط¯ظٹظ„");
				if (user_NJ.get(arg2).substring(0,1).equals("2")) {
					build.setMessage("ظ‡ظ„ طھط±ظٹط¯ طھط؛ظٹظٹط± ط´ظ‡ط±" + "\n" + user_Sora.get(arg2).substring(2, user_Sora.get(arg2).length())  + "\n" + "ظ…ظ† 29 ظٹظˆظ…  ط¥ظ„ظ‰ 30 ظٹظˆظ… طں"+"\n"+"ظپظٹ ط­ط§ظ„ط© ط§ظ„ظ…ظˆط§ظپظ‚ط© ظپط¥ظ† ط¨ط¯ط§ظٹط§طھ ط§ظ„ط´ظ‡ظˆط± ط§ظ„ظ„ط§ط­ظ‚ط© ط³طھطھط؛ظٹط±.");


					i=1;
					//i=i+1;
					//build.setMessage(""+i);
					//int  i;
					//i=parseInt(user_Page0.get(arg2));
					//annDateI = Integer.parseInt(annDate);
				}
                else {
					build.setMessage("ظ‡ظ„ طھط±ظٹط¯ طھط؛ظٹظٹط± ط´ظ‡ط±" + "\n" + user_Sora.get(arg2).substring(2, user_Sora.get(arg2).length())  + "\n" + "ظ…ظ† 30 ظٹظˆظ…  ط¥ظ„ظ‰ 29 ظٹظˆظ… طں"+"\n"+"ظپظٹ ط­ط§ظ„ط© ط§ظ„ظ…ظˆط§ظپظ‚ط© ظپط¥ظ† ط¨ط¯ط§ظٹط§طھ ط§ظ„ط´ظ‡ظˆط± ط§ظ„ظ„ط§ط­ظ‚ط© ط³طھطھط؛ظٹط±.");
					i=-1;
				}
				//userList.getChildAt(j).setBackgroundColor(Color.GRAY);
				j=parseInt(user_Page0.get(arg2));
				//j=1+(j-42292)/30;
				//userList.getChildAt(j-1).setBackgroundColor(Color.BLUE);
				build.setPositiveButton("ظ†ط¹ظ…",
						new DialogInterface.OnClickListener() {

							public void onClick(DialogInterface dialog,
												int which) {

                                //if (i==1) {
								//	SQLdb=openOrCreateDatabase("db_horaires.sqlite", Context.MODE_PRIVATE, null);
								 //   //SQLdb= SQLiteDatabase.openDatabase("/data/data/athansalat.hb.com.athansalat/databases/db_horaires.sqlite", null, SQLiteDatabase.OPEN_READWRITE);
							    //	SQLdb.execSQL("UPDATE tb_hijc SET n_jr = '30' " + "' WHERE d_gr1 = '" + j + "'");	}
								//else {
								//    SQLdb=openOrCreateDatabase("db_horaires.sqlite", Context.MODE_PRIVATE, null);
								//    //SQLdb= SQLiteDatabase.openDatabase("/data/data/athansalat.hb.com.athansalat/databases/db_horaires.sqlite", null, SQLiteDatabase.OPEN_READWRITE);
								 //   SQLdb.execSQL("UPDATE tb_hijc SET n_jr = '29' " + "' WHERE d_gr1 = '" + j + "'");}



								//Toast.makeText(getApplicationContext(),	"Page0: "  + user_Page0.get(arg2) + " J: " + j, Toast.LENGTH_LONG).show();

								//dataBase.delete(DbHelper.hi_TABLE, DbHelper.KEY_Page + "=" + user_Page.get(arg2), null);

								SQLdb=openOrCreateDatabase("db_horaires.sqlite", Context.MODE_PRIVATE, null);
								//SQLdb= SQLiteDatabase.openDatabase("/data/data/athansalat.hb.com.athansalat/databases/db_horaires.sqlite", null, SQLiteDatabase.OPEN_READWRITE);
								//SQLdb.execSQL("UPDATE tb_hijc SET n_jr = n_jr + '" + i + "' WHERE d_gr1 = '" + j  + "'");
								SQLdb.execSQL("UPDATE tb_hijc SET n_jr = n_jr + '" + i + "' WHERE d_gr1 > '" + (j - 2) +  "' And d_gr1 < '" + (j + 2) + "'");
							    SQLdb.execSQL("UPDATE tb_hijc SET d_gr1 = d_gr1 + '" + i + "' WHERE d_gr1 > '" + (j + 2) + "'");
								//SQLdb.execSQL("UPDATE tb_hijc SET n_jr = n_jr + '" + i + "' WHERE d_gr1 = '" + (j - i) + "'");

								//userList.deferNotifyDataSetChanged();
								//SQLdb.execSQL("UPDATE tb_hijc SET n_jr = 29 where d_gr1='" + 42321 + "'");
								//Toast.makeText(mContext, "Delete "+position, Toast.LENGTH_LONG).show();


								j=1+(j-42292)/30;
								displayData();
								//dialog.cancel();

								//dialog.cancel();
								//userList.smoothScrollToPosition(40);
							}

						});

				build.setNegativeButton("ظ„ط§",
						new DialogInterface.OnClickListener() {

							public void onClick(DialogInterface dialog,
									int which) {
								//userList.smoothScrollToPosition(40);
								dialog.cancel();
							}
						});
				AlertDialog alert = build.create();
				alert.show();
*/
				//return true;
				//userList.smoothScrollToPosition(5);
			}
			//setContentView(R.layout.index_p);
			//startActivity(new Intent(this, ModifD.class));

		});
		//startActivity(new Intent(this, ModifD.class));
		//userList.deferNotifyDataSetChanged();
	/*	BtnHD.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View userList) {
				displayData();
				//SQLdb=openOrCreateDatabase("db_horaires.sqlite", Context.MODE_PRIVATE, null);
	//			SQLdb=SQLiteDatabase.openDatabase("/data/data/com.gnirt69.sqlitefromassetexample/databases/db_horaires.sqlite", null, SQLiteDatabase.OPEN_READWRITE);
				//SQLdb.execSQL("UPDATE tb_villes SET fv_v = fv_v + 1 WHERE fv_v < '" + i + "'");
	//			SQLdb.execSQL("UPDATE tb_hijc SET n_jr = 30 where d_gr1='" + 42321 + "'");
			//	Toast.makeText( "Update "+mCursor.getString(mCursor.getColumnIndex(DbHelper.KEY_Page)), Toast.LENGTH_LONG).show();
			}


			//}
		});*/

		//userList.smoothScrollToPosition(40);
	}

	@Override
	protected void onResume() {
		displayData();
		//userList.smoothScrollToPosition(40);
		//userList.getLastVisiblePosition();
		//userList.smoothScrollToPosition(j -1);
		//userList.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
		//userList.setStackFromBottom(true);
		userList.setSelection(lastP-1);
		super.onResume();
	}

	/**
	 * displays data from SQLite
	 */



//	long vl; //val
//	Date dd;  // date
//	String ds, jSemaineS;  // dateText
//	int jSemaineI;
//	SimpleDateFormat df2 = new SimpleDateFormat("yyyy/MM/dd");
//	SimpleDateFormat df3 = new SimpleDateFormat("E");
	private void displayData() {
		dataBase = mHelper.getWritableDatabase();
		Cursor mCursor = dataBase.rawQuery("SELECT * FROM tb_q where id_q>0 and id_q<605", null);
		user_Page.clear();
		user_Sora.clear();
		//user_NJ.clear();
		if (mCursor.moveToFirst()) {
			do {
			  //vl=mCursor.getInt(mCursor.getColumnIndex(DbHelper.KEY_Page));

			  //userList.setBackgroundColor(Color.GRAY);userList.setBackgroundColor(Color.GRAY);
			  //user_Page0.add(mCursor.getString(mCursor.getColumnIndex(DbHelper.KEY_Page)));
			  user_Page.add(mCursor.getString(mCursor.getColumnIndex(DbHelper.KEY_Page)));
			  user_Sora.add(mCursor.getString(mCursor.getColumnIndex(DbHelper.KEY_Sora)));
			  //user_NJ.add(mCursor.getString(mCursor.getColumnIndex(DbHelper.KEY_NJ))+" ظٹظˆظ…");

			} while (mCursor.moveToNext());
			//userList.setBackgroundColor(Color.GRAY);
			//userList.getChildAt(j-1).setBackgroundColor(Color.BLUE);
		}
		IndexP_Connector disadpt = new IndexP_Connector(IndexP.this,user_Page, user_Sora);
		userList.setAdapter(disadpt);
		//userList.deferNotifyDataSetChanged();
		mCursor.close();
		//userList.setSelection(disadpt.getCount() - 1);  Fin
		userList.setSelection(j);
		//userList.setBackgroundColor(Color.GRAY);
		//userList.getChildAt(j).setBackgroundColor(Color.BLUE);
		//userList.smoothScrollToPosition(40);
		//userList.setSelection(40);

		//startActivity(new Intent(this, ModifD.class));

	}

	@Override
	public boolean onKeyUp( int keyCode, KeyEvent event )
	{
		if( keyCode == KeyEvent.KEYCODE_BACK )
		{
			//SQLdb = mHelper.openWritableDb();
			//SQLdb.execSQL("UPDATE tb_indice SET last_page = " + lastP + "");
			this.finish();
			startActivity(new Intent(this, QMain.class));
			return true;
		}
		//this.finish();
		return super.onKeyUp( keyCode, event );
	}


}


