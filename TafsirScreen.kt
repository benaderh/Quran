package quran.hb.com.quran.ui

import android.database.Cursor
import android.graphics.Typeface
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import quran.hb.com.quran.AppViewModel
import quran.hb.com.quran.data.Storage
import quran.hb.com.quran.data.Tafsir

/** مؤشر على كامل جدول التفسير ، التنقل بالموضع مع الدوران من النهاية إلى البداية */
private class TafsirHolder(private val cursor: Cursor, private val riwaya: Int) {
    val count = cursor.count

    fun find(sora: Int, aya: Int): Int {
        if (!cursor.moveToFirst()) return 0
        var i = 0
        do {
            if (cursor.getInt(1) == sora && cursor.getInt(2) == aya) return i
            i++
        } while (cursor.moveToNext())
        return 0
    }

    fun get(pos: Int): Tafsir {
        cursor.moveToPosition(pos)
        return Tafsir(
            aya = cursor.getString(3) ?: "",
            ayaNo = cursor.getString(2) ?: "",
            soraName = cursor.getString(7) ?: "",
            tafsir = cursor.getString(4) ?: "",
            page = if (riwaya == 2) (cursor.getString(6)?.trim()?.toIntOrNull() ?: 1) else 0
        )
    }

    fun close() = cursor.close()
}

@Composable
fun TafsirScreen(vm: AppViewModel, onBack: () -> Unit) {
    val context = LocalContext.current
    val riwaya = vm.riwaya
    val holder = remember { TafsirHolder(vm.repo.openTafsir(riwaya), riwaya) }
    DisposableEffect(Unit) { onDispose { holder.close() } }

    var pos by remember { mutableIntStateOf(0) }
    var loaded by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        pos = withContext(Dispatchers.IO) { holder.find(vm.tafsirSora, vm.tafsirAya) }
        loaded = true
    }

    val fonts = remember { HashMap<String, FontFamily>() }
    fun fontFor(item: Tafsir): FontFamily {
        val path = if (riwaya == 1) "font/UthmanicHafs1 Ver09.otf"
        else "font/P" + item.page.toString().padStart(3, '0') + ".otf"
        return fonts.getOrPut(path) {
            try {
                FontFamily(Typeface.createFromAsset(context.assets, path))      // داخل الـ APK
            } catch (e: Exception) {
                try {
                    val f = java.io.File(Storage.root, path)                      // أو في QuranHW/font
                    FontFamily(Typeface.createFromFile(f))
                } catch (e2: Exception) {
                    FontFamily.Default
                }
            }
        }
    }

    val item = remember(pos, loaded) { if (loaded && holder.count > 0) holder.get(pos) else null }
    val scroll = rememberScrollState()
    val scope = rememberCoroutineScope()
    var size by remember { mutableIntStateOf(vm.fontSize) }

    fun move(delta: Int) {
        if (holder.count == 0) return
        pos = (pos + delta + holder.count) % holder.count
        scope.launch { scroll.animateScrollTo(0) }
    }

    ScreenScaffold("تفسير ميسر", onBack) {
        Column(Modifier.fillMaxSize().background(Cream)) {
            Column(Modifier.weight(1f).fillMaxWidth().verticalScroll(scroll)) {
                if (item != null) {
                    Text(
                        item.aya, Modifier.fillMaxWidth().background(Color.Black).padding(end = 5.dp),
                        color = Color(0xFF41E70E), fontSize = size.sp, fontFamily = fontFor(item)
                    )
                    Text(
                        "الآية ${item.ayaNo}   سورة ${item.soraName}",
                        Modifier.fillMaxWidth().background(Color.Black).padding(end = 25.dp),
                        color = Color(0xFF94EF78), fontSize = 15.sp
                    )
                    Text(
                        item.tafsir, Modifier.fillMaxWidth().padding(end = 5.dp),
                        fontSize = 30.sp, fontWeight = FontWeight.Bold, color = Color.Black
                    )
                }
            }
            Row(Modifier.fillMaxWidth().padding(4.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                Button(onClick = { move(+1) }, contentPadding = PaddingValues(4.dp), modifier = Modifier.width(110.dp).height(50.dp)) { Text("الآية التالية") }
                Spacer(Modifier.width(4.dp))
                Button(onClick = {
                    size += 5; if (size == 75) size = 70
                    vm.saveFontSize(size)
                }, contentPadding = PaddingValues(0.dp), modifier = Modifier.width(50.dp).height(50.dp)) { Text("+") }
                Spacer(Modifier.width(4.dp))
                Button(onClick = {
                    size -= 5; if (size == 15) size = 20
                    vm.saveFontSize(size)
                }, contentPadding = PaddingValues(0.dp), modifier = Modifier.width(50.dp).height(50.dp)) { Text("-") }
                Spacer(Modifier.width(4.dp))
                Button(onClick = { move(-1) }, contentPadding = PaddingValues(4.dp), modifier = Modifier.width(110.dp).height(50.dp)) { Text("الآية السابقة") }
            }
        }
    }
}
