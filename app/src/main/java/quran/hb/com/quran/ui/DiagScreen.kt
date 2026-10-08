package quran.hb.com.quran.ui

import android.database.Cursor
import android.graphics.Typeface
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import quran.hb.com.quran.AppViewModel
import quran.hb.com.quran.data.Storage
import quran.hb.com.quran.data.Trace
import java.io.File

/** بيانات تنتقل من خطوة إلى التي بعدها */
private class DiagData {
    var cursor: Cursor? = null
    var aya = ""
    var tafsir = ""
    var ayaNo = ""
    var soraName = ""
    var page = 0
    var fontPath = ""
    var typeface: Typeface? = null
    var family: FontFamily? = null
}

private val STEP_NAMES = listOf(
    "تحضير الآية المختارة",                 // 0
    "قراءة tb_indice من قاعدة البيانات",     // 1
    "فتح جدول التفسير",                      // 2
    "البحث عن الآية و قراءة السطر",          // 3
    "فحص ملف الخط",                          // 4
    "تحميل الخط (Typeface)",                 // 5
    "إنشاء FontFamily",                      // 6
    "رسم نص قصير بالخط",                     // 7
    "رسم نص الآية كاملًا بالخط",             // 8
    "رسم نص التفسير",                        // 9
    "فتح شاشة التفسير الحقيقية"              // 10
)

/** شاشة تشخيص مؤقتة : تنفذ كل مرحلة بزر ، و تعرض نجحت أو فشلت دون أن ينهار التطبيق */
@Composable
fun DiagScreen(vm: AppViewModel, onBack: () -> Unit, openReal: () -> Unit) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val scope = rememberCoroutineScope()
    val log = remember { mutableStateListOf<String>() }
    var step by remember { mutableIntStateOf(0) }
    var failed by remember { mutableStateOf(false) }
    var render by remember { mutableIntStateOf(0) }
    val d = remember { DiagData() }

    DisposableEffect(Unit) { onDispose { d.cursor?.close() } }

    // نجاح الرسم : لا يُسجَّل إلا إذا بقي التطبيق حيًا بعد الرسم
    LaunchedEffect(render) {
        if (render > 0) {
            delay(1500)
            Trace.step("✔ رسم $render نجح", toast = false)
            log.add("✔ الرسم $render ظهر و بقي التطبيق حيًا")
        }
    }

    fun exec(i: Int): String {
        when (i) {
            0 -> {
                vm.prepareTafsir()
                return "رواية=${vm.riwaya} ، سورة=${vm.tafsirSora} ، آية=${vm.tafsirAya}"
            }
            1 -> {
                val ind = vm.repo.readIndice()
                return "last_page=${ind.lastPage} ، riwaya=${ind.riwaya} ، soraIndex=${ind.soraIndex} ، ayaIndex=${ind.ayaIndex} ، sizei=${ind.fontSize}"
            }
            2 -> {
                val c = vm.repo.openTafsir(vm.riwaya)
                d.cursor = c
                return "${c.count} سطر ، ${c.columnCount} عمودًا : ${c.columnNames.joinToString(",")}"
            }
            3 -> {
                val c = d.cursor ?: error("المؤشر غير مفتوح")
                var found = false
                if (c.moveToFirst()) {
                    do {
                        if (c.getInt(1) == vm.tafsirSora && c.getInt(2) == vm.tafsirAya) { found = true; break }
                    } while (c.moveToNext())
                }
                if (!found) c.moveToFirst()
                d.aya = c.getString(3) ?: ""
                d.ayaNo = c.getString(2) ?: ""
                d.tafsir = c.getString(4) ?: ""
                d.soraName = c.getString(7) ?: ""
                d.page = c.getString(6)?.trim()?.toIntOrNull() ?: 1
                return (if (found) "وُجدت" else "لم توجد (أول سطر)") +
                    " : سورة ${d.soraName} آية ${d.ayaNo} صفحة ${d.page} ، نص الآية ${d.aya.length} حرف ، التفسير ${d.tafsir.length} حرف"
            }
            4 -> {
                d.fontPath = if (vm.riwaya == 1) "font/UthmanicHafs1 Ver09.otf"
                else "font/P" + d.page.toString().padStart(3, '0') + ".otf"
                val inAssets = runCatching { context.assets.open(d.fontPath).close() }.isSuccess
                val f = File(Storage.root, d.fontPath)
                return "المسار ${d.fontPath}\nداخل assets: ${if (inAssets) "نعم" else "لا"}\n" +
                    "في QuranHW: ${f.path} موجود=${f.exists()} قراءة=${f.canRead()} حجم=${f.length()}"
            }
            5 -> {
                try {
                    d.typeface = Typeface.createFromAsset(context.assets, d.fontPath)
                    return "تم من assets"
                } catch (e: Throwable) {
                    val f = File(Storage.root, d.fontPath)
                    d.typeface = Typeface.createFromFile(f)
                    return "تم من الملف ${f.path}"
                }
            }
            6 -> {
                d.family = FontFamily(d.typeface ?: error("لا يوجد Typeface"))
                return "تم"
            }
            7 -> { render = 1; return "جارٍ الرسم ... (انتظر ثانيتين)" }
            8 -> { render = 2; return "جارٍ الرسم ... (انتظر ثانيتين)" }
            9 -> { render = 3; return "جارٍ الرسم ... (انتظر ثانيتين)" }
        }
        return ""
    }

    fun runStep() {
        val i = step
        scope.launch {
            Trace.step("▶ ${i + 1}: ${STEP_NAMES[i]}", toast = false)
            try {
                if (i == STEP_NAMES.size - 1) {
                    openReal()
                    return@launch
                }
                val renderStep = i in 7..9
                val msg = if (renderStep) exec(i) else withContext(Dispatchers.IO) { exec(i) }
                log.add("✔ ${i + 1}. ${STEP_NAMES[i]}\n    $msg")
                if (!renderStep) Trace.step("✔ ${i + 1}", toast = false)
                step = i + 1
            } catch (e: Throwable) {
                failed = true
                log.add("✘ ${i + 1}. ${STEP_NAMES[i]}\n    ${e.javaClass.simpleName}: ${e.message}")
                Trace.step("✘ ${i + 1}: ${Log.getStackTraceString(e)}", toast = false)
            }
        }
    }

    ScreenScaffold("تشخيص التفسير - نسخة 3", onBack) {
        Column(Modifier.fillMaxSize().padding(8.dp)) {
            Column(Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState())) {
                SelectionContainer {
                    Text(
                        if (log.isEmpty()) "اضغط «تنفيذ الخطوة» للبدء" else log.joinToString("\n\n"),
                        fontSize = 13.sp
                    )
                }
                val fam = d.family
                if (fam != null) {
                    if (render >= 1) Text("بِسْمِ ٱللَّهِ", fontFamily = fam, fontSize = 30.sp, modifier = Modifier.background(Color.Black), color = Color.Green)
                    if (render >= 2) Text(d.aya, fontFamily = fam, fontSize = vm.fontSize.sp, modifier = Modifier.background(Color.Black), color = Color.Green)
                    if (render >= 3) Text(d.tafsir, fontSize = 20.sp)
                }
            }
            if (step < STEP_NAMES.size && !failed) {
                Button(onClick = { runStep() }, modifier = Modifier.fillMaxWidth()) {
                    Text("تنفيذ الخطوة ${step + 1}/${STEP_NAMES.size} : ${STEP_NAMES[step]}")
                }
            }
            OutlinedButton(
                onClick = { clipboard.setText(AnnotatedString(log.joinToString("\n\n") + "\n\n" + Trace.read())) },
                modifier = Modifier.fillMaxWidth()
            ) { Text("نسخ التقرير") }
        }
    }
}
