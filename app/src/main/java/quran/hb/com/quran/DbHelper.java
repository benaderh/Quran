package quran.hb.com.quran; /**
 * Created by B on 14/11/2016.
 */
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class DbHelper extends SQLiteOpenHelper {
	private Context mycontext;
	//public String DB_PATH = Environment.getExternalStorageDirectory() + "/QuranHB/";
	public String DB_PATH = "/data/data/quran.hb.com.quran/databases/";
	public static String DB_NAME = "db_quran.sqlite";
	//private static String ho_TABLE = "qr_horaires";
	//private static String al_TABLE = "tb_alarms";
	//private static String qral_TABLE = "qr_alarms";
	//private static String hi_TABLE = "tb_hijc";
	private static String tf_TABLE = "tb_tafcir";
	private static String tfw_TABLE = "tb_tafcirW";
	private static String sr_TABLE = "tb_sora";
	private static String in_TABLE = "tb_indice";
	private static String qr_TABLE = "tb_q";
	private static String jz_TABLE = "qr_joz";

	public static final String KEY_SoraI = "id_sora";
	public static final String KEY_SoraT = "t_sora";
	public static final String KEY_SoraP = "p_sora";

	public static final String KEY_AyaI = "aya";
	public static final String KEY_AyaText = "aya_text";
	public static final String KEY_Line = "line";
	public static final String KEY_Point = "point";

	//public static final String KEY_ID = "id_q";
	//public static final String KEY_SP = "sora_p";
	public static final String KEY_Page = "id_q";
	public static final String KEY_Sora = "sora_p";

	//public static final String KEY_Page = "id_q";
	public static final String KEY_Hizb = "hizb";
	public static final String KEY_AyaT = "aya_t";
	public static final String KEY_Detail = "detail";




	//public static final String EMPLOYEE_TABLE = "employee";

	public SQLiteDatabase myDataBase;

    /*private String DB_PATH = "/data/data/"
                                + mycontext.getApplicationContext().getPackageName()
                                + "/databases/";
*/


/*	public DatabaseHelper(Context context) {
		super(context, DB_NAME, null, 1);

	}*/

/*	@Override
	public void onCreate(SQLiteDatabase db) {
		String CREATE_TABLE="CREATE TABLE "+hi_TABLE+" ("+KEY_DG+" INTEGER PRIMARY KEY, "+KEY_DH+" TEXT, "+KEY_NJ+" INTEGR)";
		db.execSQL(CREATE_TABLE);

	}*/

/*	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS "+hi_TABLE);
		onCreate(db);

	}*/


	public DbHelper(Context context) throws IOException {
		super(context,DB_NAME,null,1);
		this.mycontext=context;
	}



	public void createdatabase() throws IOException {
		boolean dbexist = checkdatabase();
		if(dbexist)
		{
			System.out.println(" Database exists.");
		}
		else{
			this.getReadableDatabase();
			try{
				copydatabase();
			}
			catch(IOException e){
				throw new Error("Error copying database");
			}
		}
	}
	private boolean checkdatabase() {
		//SQLiteDatabase checkdb = null;
		boolean checkdb = false;
		try{
			String myPath = DB_PATH + DB_NAME;
			File dbfile = new File(myPath);
			checkdb = SQLiteDatabase.openDatabase(myPath,null, SQLiteDatabase.OPEN_READWRITE) != null;
			checkdb = dbfile.exists();
		}
		catch(SQLiteException e){
			System.out.println("Database doesn't exist");
		}

		return checkdb;
	}
	private void copydatabase() throws IOException {

		//Open your local db as the input stream
		InputStream myinput = mycontext.getAssets().open(DB_NAME);

		// Path to the just created empty db
		String outfilename = DB_PATH + DB_NAME;

		//Open the empty db as the output stream
		OutputStream myoutput = new FileOutputStream(outfilename);

		// transfer byte to inputfile to outputfile
		byte[] buffer = new byte[1024];
		int length;
		while ((length = myinput.read(buffer))>0)
		{
			myoutput.write(buffer,0,length);
		}

		//Close the streams
		myoutput.flush();
		myoutput.close();
		myinput.close();

	}

	public void opendatabase() throws SQLException
	{
		//Open the database
		String mypath = DB_PATH + DB_NAME;
		myDataBase = SQLiteDatabase.openDatabase(mypath, null, SQLiteDatabase.OPEN_READONLY);

	}

	public synchronized void close(){
		if(myDataBase != null){
			myDataBase.close();
		}
		super.close();
	}
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	}


	/*	public Cursor horawQuery (String sql, String[] selectionArgs) {
            String hoQuery = "SELECT * FROM " + ho_TABLE;
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor hocursor = db.rawQuery(hoQuery, null);
            return hocursor;
        }

        public Cursor alrawQuery (String sql, String[] selectionArgs) {
            String alQuery = "SELECT * FROM " + al_TABLE;
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor alcursor = db.rawQuery(alQuery, null);
            return alcursor;
        }

        public Cursor qralrawQuery (String sql, String[] selectionArgs) {
            String qralQuery = "SELECT * FROM " + qral_TABLE;
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor qralcursor = db.rawQuery(qralQuery, null);
            return qralcursor;
        }

        public Cursor hirawQuery (String sql, String[] selectionArgs) {
            String hiQuery = "SELECT * FROM " + hi_TABLE;
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor hicursor = db.rawQuery(hiQuery, null);
            return hicursor;
        }

        public Cursor virawQuery (String sql, String[] selectionArgs) {
            String viQuery = "SELECT * FROM " + vi_TABLE;
            //String countQuery = "SELECT * FROM tb_horaires where d_gr=?";
            ////Cursor c=db.rawQuery("SELECT "+colDeptID+" as _id FROM "+deptTable+" WHERE "+colDeptName+"=?", new String []{Dept});
            SQLiteDatabase db = this.getReadableDatabase();
            //Cursor cursor = db.rawQuery(countQuery, new String []{"2016-11-15"});
            Cursor vicursor = db.rawQuery(viQuery, null);
            return vicursor;
        }

        public Cursor fvrawQuery (String sql, String[] selectionArgs) {
            String fvQuery = "SELECT tx_v FROM " + fv_TABLE;
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor fvcursor = db.rawQuery(fvQuery, null);
            return fvcursor;
        }*/

        public Cursor inrawQuery (String sql, String[] selectionArgs) {
            String inQuery = "SELECT * FROM " + in_TABLE;
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor prcursor = db.rawQuery(inQuery, null);
            return prcursor;
        }

	public Cursor qrrawQuery (String sql, String[] selectionArgs) {
		String qrQuery = "SELECT * FROM " + qr_TABLE;
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor prcursor = db.rawQuery(qrQuery, null);
		return prcursor;
	}

	public Cursor srrawQuery (String sql, String[] selectionArgs) {
		String srQuery = "SELECT * FROM " + sr_TABLE;
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor prcursor = db.rawQuery(srQuery, null);
		return prcursor;
	}

	public Cursor jzrawQuery (String sql, String[] selectionArgs) {
		String jzQuery = "SELECT * FROM " + jz_TABLE;
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor prcursor = db.rawQuery(jzQuery, null);
		return prcursor;
	}

	public Cursor tfrawQuery (String sql, String[] selectionArgs) {
		String tfQuery = "SELECT * FROM " + tf_TABLE;
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor prcursor = db.rawQuery(tfQuery, null);
		return prcursor;
	}

	public Cursor tfwrawQuery (String sql, String[] selectionArgs) {
		String tfwQuery = "SELECT * FROM " + tfw_TABLE;
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor prcursor = db.rawQuery(tfwQuery, null);
		return prcursor;
	}


	@Override
	public void onCreate(SQLiteDatabase arg0) {
		// TODO Auto-generated method stub

	}

}