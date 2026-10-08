package quran.hb.com.quran.data

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import jxl.Workbook

/**
 * الوصول إلى db_quran.sqlite (نفس القاعدة القديمة بنفس الجداول).
 * تُنسخ من assets عند أول تشغيل إلى مجلد databases ، و إن كانت موجودة من النسخة القديمة تبقى كما هي.
 */
class QuranDb(private val context: Context) {

    private val dbName = "db_quran.sqlite"
    private val db: SQLiteDatabase = openDb()

    private fun openDb(): SQLiteDatabase {
        val file = context.getDatabasePath(dbName)
        if (!file.exists()) {
            file.parentFile?.mkdirs()
            val source = Storage.openDbSource(context)
                ?: throw IllegalStateException("db_quran.sqlite introuvable")
            source.use { input -> file.outputStream().use { out -> input.copyTo(out) } }
        }
        return SQLiteDatabase.openDatabase(file.path, null, SQLiteDatabase.OPEN_READWRITE)
    }

    private fun Cursor.str(i: Int): String = getString(i) ?: ""

    // ---------- tb_indice ----------
    fun readIndice(): Indice =
        db.rawQuery("SELECT * FROM tb_indice", null).use { c ->
            c.moveToFirst()
            val size = c.getInt(12)
            Indice(
                lastPage = c.getInt(0),
                riwaya = if (c.getInt(1) == 2) 2 else 1,
                bookmarkPages = listOf(c.getInt(2), c.getInt(4), c.getInt(6)),
                bookmarkNames = listOf(c.str(3), c.str(5), c.str(7)),
                soraIndex = c.getInt(10),
                ayaIndex = c.getInt(11),
                fontSize = if (size in 20..70) size else 30
            )
        }

    fun setLastPage(p: Int) = db.execSQL("UPDATE tb_indice SET last_page = ?", arrayOf<Any>(p))
    fun setRiwaya(r: Int) = db.execSQL("UPDATE tb_indice SET last_riwaya = ?", arrayOf<Any>(r))
    fun setFontSize(s: Int) = db.execSQL("UPDATE tb_indice SET sizei = ?", arrayOf<Any>(s))
    fun setTafsirIndex(sora: Int, aya: Int) =
        db.execSQL("UPDATE tb_indice SET soraIndex = ?, ayaIndex = ?", arrayOf<Any>(sora, aya))

    /** n = 1..3 */
    fun setBookmark(n: Int, page: Int, soraName: String) {
        require(n in 1..3)
        db.execSQL("UPDATE tb_indice SET ri$n = ?, rt$n = ?", arrayOf<Any>(page, soraName))
    }

    // ---------- الجداول المرجعية ----------
    fun loadPages(): List<PageRow> =
        db.rawQuery("SELECT * FROM tb_q", null).use { c ->
            val list = ArrayList<PageRow>(610)
            while (c.moveToNext()) list.add(PageRow(c.getInt(0), c.str(1), c.str(2), c.str(3)))
            list
        }

    fun loadSoras(): List<SoraRow> =
        db.rawQuery("SELECT * FROM tb_sora", null).use { c ->
            val list = ArrayList<SoraRow>(114)
            while (c.moveToNext()) list.add(SoraRow(c.getInt(0), c.str(1), c.getInt(2), c.getInt(3)))
            list
        }

    fun loadJoz(): List<JozRow> =
        db.rawQuery("SELECT * FROM qr_joz", null).use { c ->
            val iPage = c.getColumnIndexOrThrow("id_q")
            val iHizb = c.getColumnIndexOrThrow("hizb")
            val iAya = c.getColumnIndexOrThrow("aya_t")
            val iDet = c.getColumnIndexOrThrow("detail")
            val list = ArrayList<JozRow>(240)
            while (c.moveToNext()) list.add(JozRow(c.getInt(iPage), c.str(iHizb), c.str(iAya), c.str(iDet)))
            list
        }

    // ---------- البحث / التفسير ----------
    private fun tafsirTable(riwaya: Int) = if (riwaya == 1) "tb_tafcir" else "tb_tafcirW"

    fun loadVerses(riwaya: Int): List<Verse> =
        db.rawQuery("SELECT * FROM ${tafsirTable(riwaya)}", null).use { c ->
            val iSora = c.getColumnIndexOrThrow("t_sora")
            val iAyaI = c.getColumnIndexOrThrow("aya")
            val iText = c.getColumnIndexOrThrow("aya_text")
            val iPage = c.getColumnIndexOrThrow("p_sora")
            val iLine = c.getColumnIndexOrThrow("line")
            val iPoint = c.getColumnIndexOrThrow("point")
            val list = ArrayList<Verse>(6300)
            while (c.moveToNext()) {
                val t = c.str(iText)
                list.add(Verse(c.str(iSora), c.str(iAyaI), t, c.str(iPage), c.str(iLine), c.str(iPoint), t.normalizeAr()))
            }
            list
        }

    /** المؤشر مفتوح على كامل الجدول؛ على المستدعي إغلاقه */
    fun openTafsir(riwaya: Int): Cursor =
        db.rawQuery("SELECT * FROM ${tafsirTable(riwaya)}", null)

    // ---------- توقيت التلاوة (ملفات xls في مجلد QuranHW) ----------
    /** dir = "HA" أو "WA" ، row = رقم الخانة داخل السورة (الملف خارج الـ APK) */
    fun readTiming(dir: String, sora: Int, row: Int): Timing? = try {
        java.io.FileInputStream(Storage.file(dir, "s$sora.xls")).use { stream ->
            val wb = Workbook.getWorkbook(stream)
            try {
                val sheet = wb.getSheet(0)
                fun cell(col: Int) = sheet.getCell(col, row).contents.trim().toDouble().toInt()
                Timing(pos = cell(4), aya = cell(5), line = cell(1))
            } finally {
                wb.close()
            }
        }
    } catch (e: Exception) {
        null
    }
}
