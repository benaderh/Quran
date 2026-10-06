package quran.hb.com.quran;

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
    public static String DB_NAME = "db_quran.sqlite";

    private static String tf_TABLE  = "tb_tafcir";
    private static String tfw_TABLE = "tb_tafcirW";
    private static String sr_TABLE  = "tb_sora";
    private static String in_TABLE  = "tb_indice";
    private static String qr_TABLE  = "tb_q";
    private static String jz_TABLE  = "qr_joz";

    public static final String KEY_SoraI = "id_sora";
    public static final String KEY_SoraT = "t_sora";
    public static final String KEY_SoraP = "p_sora";
    public static final String KEY_AyaI  = "aya";
    public static final String KEY_AyaText = "aya_text";
    public static final String KEY_Line  = "line";
    public static final String KEY_Point = "point";
    public static final String KEY_Page  = "id_q";
    public static final String KEY_Sora  = "sora_p";
    public static final String KEY_Hizb  = "hizb";
    public static final String KEY_AyaT  = "aya_t";
    public static final String KEY_Detail = "detail";

    public SQLiteDatabase myDataBase;

    public DbHelper(Context context) throws IOException {
        super(context, DB_NAME, null, 1);
        this.mycontext = context;
    }

    /** Returns the path to the app-private databases directory */
    private String getDbPath() {
        // Use context.getDatabasePath() - works on all Android versions without permissions
        return mycontext.getDatabasePath(DB_NAME).getAbsolutePath();
    }

    public void createdatabase() throws IOException {
        boolean dbexist = checkdatabase();
        if (dbexist) {
            System.out.println("Database exists.");
        } else {
            this.getReadableDatabase();
            try {
                copydatabase();
            } catch (IOException e) {
                throw new Error("Error copying database");
            }
        }
    }

    private boolean checkdatabase() {
        boolean checkdb = false;
        try {
            File dbFile = mycontext.getDatabasePath(DB_NAME);
            checkdb = dbFile.exists();
        } catch (SQLiteException e) {
            System.out.println("Database doesn't exist");
        }
        return checkdb;
    }

    private void copydatabase() throws IOException {
        InputStream myinput = mycontext.getAssets().open(DB_NAME);
        String outfilename = getDbPath();
        // Ensure parent directory exists
        new File(outfilename).getParentFile().mkdirs();
        OutputStream myoutput = new FileOutputStream(outfilename);
        byte[] buffer = new byte[1024];
        int length;
        while ((length = myinput.read(buffer)) > 0) {
            myoutput.write(buffer, 0, length);
        }
        myoutput.flush();
        myoutput.close();
        myinput.close();
    }

    public void opendatabase() throws SQLException {
        String mypath = getDbPath();
        myDataBase = SQLiteDatabase.openDatabase(mypath, null, SQLiteDatabase.OPEN_READONLY);
    }

    /** Open database in read-write mode for UPDATE operations */
    public SQLiteDatabase openWritableDb() {
        return SQLiteDatabase.openDatabase(
                getDbPath(), null, SQLiteDatabase.OPEN_READWRITE);
    }

    public synchronized void close() {
        if (myDataBase != null) {
            myDataBase.close();
        }
        super.close();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) { }

    @Override
    public void onCreate(SQLiteDatabase arg0) { }

    public Cursor inrawQuery(String sql, String[] selectionArgs) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + in_TABLE, null);
    }

    public Cursor qrrawQuery(String sql, String[] selectionArgs) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + qr_TABLE, null);
    }

    public Cursor srrawQuery(String sql, String[] selectionArgs) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + sr_TABLE, null);
    }

    public Cursor jzrawQuery(String sql, String[] selectionArgs) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + jz_TABLE, null);
    }

    public Cursor tfrawQuery(String sql, String[] selectionArgs) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + tf_TABLE, null);
    }

    public Cursor tfwrawQuery(String sql, String[] selectionArgs) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + tfw_TABLE, null);
    }
}
