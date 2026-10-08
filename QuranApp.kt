package quran.hb.com.quran

import android.app.Application
import android.util.Log
import java.io.File

/** يحفظ سبب أي انهيار في crash.txt ليُعرض عند التشغيل التالي */
class QuranApp : Application() {
    override fun onCreate() {
        super.onCreate()
        val previous = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, error ->
            try {
                File(filesDir, "crash.txt").writeText(Log.getStackTraceString(error))
            } catch (_: Throwable) {
            }
            previous?.uncaughtException(thread, error)
        }
    }
}
