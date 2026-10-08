package quran.hb.com.quran.data

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import androidx.core.content.ContextCompat
import java.io.File
import java.io.InputStream

/**
 * مكان ملفات المصحف خارج الـ APK.
 * الترتيب : /storage/emulated/0/QuranHW  (يحتاج إذن الوصول إلى الملفات)  ثم  Android/data/<package>/files/QuranHW
 * الأسماء المقبولة : HI, HA, WI, WA, db_quran  (أو الهيكل القديم H/img, H/aud, W/img, W/aud)
 */
object Storage {

    @Volatile
    var root: File? = null
        private set

    private val legacy = mapOf("HI" to "H/img", "HA" to "H/aud", "WI" to "W/img", "WA" to "W/aud")

    fun hasSharedAccess(ctx: Context): Boolean =
        if (Build.VERSION.SDK_INT >= 30) Environment.isExternalStorageManager()
        else ContextCompat.checkSelfPermission(ctx, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED

    private fun candidates(ctx: Context): List<File> {
        val list = mutableListOf<File>()
        if (hasSharedAccess(ctx)) list += File(Environment.getExternalStorageDirectory(), "QuranHW")
        ctx.getExternalFilesDir(null)?.let { list += File(it, "QuranHW") }
        return list
    }

    private fun sub(base: File, name: String): File? =
        listOfNotNull(File(base, name), legacy[name]?.let { File(base, it) }).firstOrNull { it.isDirectory }

    /** يبحث عن مجلد الملفات؛ يعيد true إذا وُجدت الصور */
    fun resolve(ctx: Context): Boolean {
        root = candidates(ctx).firstOrNull { sub(it, "HI") != null || sub(it, "WI") != null }
        return root != null
    }

    /** مسار ملف داخل أحد المجلدات (HI/HA/WI/WA) */
    fun file(name: String, child: String): File {
        val base = root ?: return File(child)
        return File(sub(base, name) ?: File(base, name), child)
    }

    fun missingFolders(): List<String> {
        val base = root ?: return listOf("HI", "HA", "WI", "WA")
        return listOf("HI", "HA", "WI", "WA").filter { sub(base, it) == null }
    }

    private fun dbSources(ctx: Context): List<File> {
        val bases = candidates(ctx)
        return bases.flatMap {
            listOf(File(it, "db_quran/db_quran.sqlite"), File(it, "db_quran.sqlite"), File(it, "db_quran"))
        }
    }

    /** مصدر القاعدة عند أول تشغيل : المجلد الخارجي أولًا ثم assets */
    fun openDbSource(ctx: Context): InputStream? {
        dbSources(ctx).firstOrNull { it.isFile }?.let { return it.inputStream() }
        return try { ctx.assets.open("db_quran.sqlite") } catch (e: Exception) { null }
    }

    fun dbAvailable(ctx: Context): Boolean {
        if (ctx.getDatabasePath("db_quran.sqlite").exists()) return true
        if (dbSources(ctx).any { it.isFile }) return true
        return try { ctx.assets.open("db_quran.sqlite").close(); true } catch (e: Exception) { false }
    }
}
