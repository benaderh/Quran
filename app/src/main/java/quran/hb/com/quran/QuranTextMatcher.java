package quran.hb.com.quran;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.io.IOException;

public class QuranTextMatcher {

    private static final String TAG = "QuranTextMatcher";
    private DbHelper dbHelper;

    public QuranTextMatcher(Context context) {
        try {
            dbHelper = new DbHelper(context);
            dbHelper.createdatabase();
            dbHelper.opendatabase();
        } catch (IOException e) {
            Log.e(TAG, "DB init error", e);
        }
    }

    public static String normalize(String text) {
        if (text == null) return "";
        return text
            .replaceAll("[\u064B-\u065F\u0670]", "")
            .replaceAll("[\u0622\u0623\u0625]", "\u0627")
            .replaceAll("\u0629", "\u0647")
            .replaceAll("\u0649", "\u064A")
            .trim();
    }

    public String getAyaText(int soraNum, int ayaNum, boolean isHafs) {
        String table = isHafs ? "tb_tafcir" : "tb_tafcirW";
        try {
            Cursor c = dbHelper.inrawQuery(
                "SELECT aya_text FROM " + table + " WHERE sora=" + soraNum + " AND aya=" + ayaNum,
                null);
            if (c != null && c.moveToFirst()) {
                String txt = c.getString(0);
                c.close();
                return txt;
            }
            if (c != null) c.close();
        } catch (Exception e) {
            Log.e(TAG, "getAyaText error", e);
        }
        return "";
    }

    public int getAyaCount(int soraNum, boolean isHafs) {
        String table = isHafs ? "tb_tafcir" : "tb_tafcirW";
        try {
            Cursor c = dbHelper.inrawQuery(
                "SELECT COUNT(*) FROM " + table + " WHERE sora=" + soraNum, null);
            if (c != null && c.moveToFirst()) {
                int count = c.getInt(0);
                c.close();
                return count;
            }
            if (c != null) c.close();
        } catch (Exception e) {
            Log.e(TAG, "getAyaCount error", e);
        }
        return 7;
    }

    public int getAyaPage(int soraNum, int ayaNum) {
        try {
            Cursor c = dbHelper.inrawQuery(
                "SELECT p_sora FROM tb_tafcir WHERE sora=" + soraNum + " AND aya=" + ayaNum, null);
            if (c != null && c.moveToFirst()) {
                int page = c.getInt(0);
                c.close();
                return page;
            }
            if (c != null) c.close();
        } catch (Exception e) {
            Log.e(TAG, "getAyaPage error", e);
        }
        return 1;
    }

    public String getSoraName(int soraNum) {
        try {
            Cursor c = dbHelper.inrawQuery(
                "SELECT t_sora FROM tb_sora WHERE id_sora=" + soraNum, null);
            if (c != null && c.moveToFirst()) {
                String name = c.getString(0);
                c.close();
                return name;
            }
            if (c != null) c.close();
        } catch (Exception e) {
            Log.e(TAG, "getSoraName error", e);
        }
        return "سورة " + soraNum;
    }
}