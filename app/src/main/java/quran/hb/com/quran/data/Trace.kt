package quran.hb.com.quran.data

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import java.io.File
import java.io.FileOutputStream

/** مراحل التنفيذ : تظهر كرسائل Toast و تُحفظ فورًا في ملف (تبقى حتى لو أُغلق التطبيق فجأة) */
object Trace {
    private var file: File? = null
    private var appContext: Context? = null
    private val handler = Handler(Looper.getMainLooper())

    fun init(ctx: Context) {
        appContext = ctx.applicationContext
        file = File(ctx.filesDir, "trace.txt")
    }

    fun reset() {
        runCatching { file?.writeText("") }
    }

    fun step(msg: String, toast: Boolean = true) {
        Log.d("QuranTrace", msg)
        runCatching {
            FileOutputStream(file, true).use {
                it.write((msg + "\n").toByteArray())
                it.fd.sync()
            }
        }
        if (toast) {
            val c = appContext ?: return
            handler.post { Toast.makeText(c, msg, Toast.LENGTH_SHORT).show() }
        }
    }

    fun read(): String = runCatching { file?.readText() ?: "" }.getOrDefault("")
    fun clear() = reset()
}
