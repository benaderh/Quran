package quran.hb.com.quran;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;

/**
 * activity to display all records from SQLite database
 * @author ketan(Visit my <a
 *         href="http://androidsolution4u.blogspot.in/">blog</a>)
 */
public class IndexF extends Activity {

	private DbHelper mHelper;
	private SQLiteDatabase dataBase;
	SQLiteDatabase SQLdb;
	//long dgL;

	ArrayList<IndexF_Variables> KranList = new ArrayList<IndexF_Variables>();


	//private ArrayList<String> user_SerchSora = new ArrayList<String>();
	//private ArrayList<String> user_SearchAyaI = new ArrayList<String>();
	//private ArrayList<String> user_SearchAyaT = new ArrayList<String>();
	//private ArrayList<String> user_SearchPage = new ArrayList<String>();
	IndexF_Connector listAdapter;
	int  i, pageJ;
	int j=0;
	//DbHelper db;
    int lastP, lastR, lineS, pointS;
	EditText inputSearch;
	TextView search_tv;

	private ListView userList;
	private AlertDialog.Builder build;
	//Button BtnHD;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.index_f);
		//userList.smoothScrollToPosition(40);
		userList = (ListView) findViewById(R.id.lv_search);
		inputSearch = (EditText) findViewById(R.id.inputSearch);
		search_tv = (TextView) findViewById(R.id.search_textView_resultsCount);
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
		lastR = curI.getInt(1);
		curI.close();

	/*	curI = mHelper.jzrawQuery("SELECT * FROM qr_joz", null);
		curI.moveToFirst();
		int i = 0;
		do {
			if (curI.getInt(0) > lastP) {
				curI.moveToPrevious();
				lastP=i;//curI.getInt(0);
				//Toast.makeText(IndexS.this, lastP, Toast.LENGTH_SHORT).show();
				//i = 115;
				break;
			}
			curI.moveToNext();
			i++;
		} while (i < 240);
		curI.close();*/



		/*userList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

			public boolean onItemLongClick(AdapterView<?> arg0, View arg1, final int arg2, long arg3) {
     			//lastP= Integer.parseInt(tempPage.get(arg2) );
				SQLdb = mHelper.openWritableDb();
				SQLdb.execSQL("UPDATE tb_indice SET last_page = " + lastP + "");
				finish();
				Intent intent = new Intent(getApplicationContext(), QMain.class);
                startActivity(intent);
				return true;

			}

		});*/

        userList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                // Getting Search ListView clicked item.
                IndexF_Variables ListViewClickData = (IndexF_Variables) parent.getItemAtPosition(position);

                // printing clicked item on screen using Toast message.
                //Toast.makeText(IndexF.this, ListViewClickData.getPage(), Toast.LENGTH_SHORT).show();

                lastP= Integer.parseInt(ListViewClickData.getPage());
				lineS= Integer.parseInt(ListViewClickData.getLine());
				pointS= Integer.parseInt(ListViewClickData.getPoint());

                SQLdb = mHelper.openWritableDb();
                SQLdb.execSQL("UPDATE tb_indice SET last_page = " + lastP + ", line = " + lineS + ", point = " + pointS + "");
                finish();
                Intent intent = new Intent(getApplicationContext(), QMain.class);
                startActivity(intent);



                //return true;
            }
        });



		inputSearch.addTextChangedListener(new TextWatcher() {

			public void afterTextChanged(Editable s) {
			}

			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			public void onTextChanged(CharSequence stringVar, int start, int before, int count) {

				listAdapter.getFilter().filter(stringVar.toString());
				//j=count;
				search_tv.setText(""+listAdapter.getCount());
			}
		});












	}

	@Override
	protected void onResume() {
		displayData();
		//userList.smoothScrollToPosition(40);
		userList.getLastVisiblePosition();
		//userList.smoothScrollToPosition(j -1);
		//userList.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
		//userList.setStackFromBottom(true);
		//userList.setSelection(lastP-1);
		//search_tv.setText(""+userList.getCount());
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
		String qry;
		if (lastR==1) {
		   qry ="SELECT * FROM tb_tafcir";
		}else{
			qry ="SELECT * FROM tb_tafcirW";
		}
		dataBase = mHelper.getWritableDatabase();
		//Cursor mCursor = dataBase.rawQuery("SELECT * FROM tb_tafcir", null);
		Cursor mCursor = dataBase.rawQuery(qry, null);
		//user_SerchSora.clear();
		//user_SearchAyaI.clear();
		//user_SearchAyaT.clear();
		//user_SearchPage.clear();

		IndexF_Variables kran;
		KranList = new ArrayList<IndexF_Variables>();




		if (mCursor.moveToFirst()) {
			do {
			  //vl=mCursor.getInt(mCursor.getColumnIndex(DbHelper.KEY_DG));

			  //userList.setBackgroundColor(Color.GRAY);userList.setBackgroundColor(Color.GRAY);
			  //user_DG0.add(mCursor.getString(mCursor.getColumnIndex(DbHelper.KEY_DG)));
			  //user_SerchSora.add(mCursor.getString(mCursor.getColumnIndex(DbHelper.KEY_SoraT)));
			  //user_SearchAyaI.add(mCursor.getString(mCursor.getColumnIndex(DbHelper.KEY_AyaI)));
			  //user_SearchAyaT.add(mCursor.getString(mCursor.getColumnIndex(DbHelper.KEY_AyaText)));
			  //user_SearchPage.add(mCursor.getString(mCursor.getColumnIndex(DbHelper.KEY_SoraP)));

			  String tempSora= mCursor.getString(mCursor.getColumnIndex(DbHelper.KEY_SoraT));
			  String tempAyaI= mCursor.getString(mCursor.getColumnIndex(DbHelper.KEY_AyaI));
			  String tempAyaT= mCursor.getString(mCursor.getColumnIndex(DbHelper.KEY_AyaText));
			  String tempPage= mCursor.getString(mCursor.getColumnIndex(DbHelper.KEY_SoraP));
			  String tempLine= mCursor.getString(mCursor.getColumnIndex(DbHelper.KEY_Line));
			  String tempPoint= mCursor.getString(mCursor.getColumnIndex(DbHelper.KEY_Point));

				kran = new IndexF_Variables(tempSora, tempAyaT, tempAyaI, tempPage, tempLine, tempPoint);

			  KranList.add(kran);
              j=j+1;
				//pageJ =  Integer.parseInt(user_DjPage.toString());
				//user_ID.get(arg2)
				//if(user_DjPage.equals(""+11)) {
				//	user_DjDetail.setTextColor(Color.parseColor("#04860b"));}
			} while (mCursor.moveToNext());
			//userList.setBackgroundColor(Color.GRAY);
			//userList.getChildAt(j-1).setBackgroundColor(Color.BLUE);
			//search_tv.setText(""+j);
		}
		//IndexF_Connector disadpt = new IndexF_Connector(IndexF.this,user_SerchSora, user_SearchAyaI, user_SearchAyaT, user_SearchPage);
		//userList.setAdapter(disadpt);
		//userList.deferNotifyDataSetChanged();

		listAdapter = new IndexF_Connector(IndexF.this, R.layout.index_f_item, KranList);

		userList.setAdapter(listAdapter);




		mCursor.close();
		//userList.setSelection(disadpt.getCount() - 1);  Fin
		//userList.setSelection(j);
		//userList.setBackgroundColor(Color.GRAY);
		//userList.getChildAt(4).setBackgroundColor(Color.BLUE);
		//userList.getItemAtPosition(2).setBackgroundColor(Color.BLUE);
		//userList.smoothScrollToPosition(40);
		//userList.setSelection(40);

		//startActivity(new Intent(this, ModifD.class));
	}

/*	inputSearch.addTextChangedListener(new TextWatcher() {

		@Override
		public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
			// When user changed the Text
			IndexF.this.user_SearchAyaT.getFilter().filter(cs);
		}

		@Override
		public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
		int arg3) {
			// TODO Auto-generated method stub

		}

		@Override
		public void afterTextChanged(Editable arg0) {
			// TODO Auto-generated method stub
		}
	});*/



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


