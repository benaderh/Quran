package quran.hb.com.quran.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import quran.hb.com.quran.AppViewModel
import quran.hb.com.quran.data.JozRow

// ---------------------------------------------------------------- فهرس السور
@Composable
fun SoraScreen(vm: AppViewModel, onBack: () -> Unit, openReader: () -> Unit) {
    val soras = vm.soras
    // السورة الحالية = آخر سورة تبدأ في صفحة <= الصفحة الحالية
    val current = remember { soras.indexOfLast { it.page <= vm.page }.coerceAtLeast(0) }
    val state = rememberLazyListState(initialFirstVisibleItemIndex = current)

    ScreenScaffold("البحث عن سورة", onBack) {
        LazyColumn(state = state, modifier = Modifier.fillMaxSize()) {
            itemsIndexed(soras) { _, s ->
                Row(
                    Modifier.fillMaxWidth().background(ListGray)
                        .clickable { vm.goToPage(s.page); openReader() }
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                        .height(35.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("${s.page}", Modifier.weight(3f), textAlign = TextAlign.Center, fontSize = 25.sp, color = Color.Black)
                    Text(
                        s.name,
                        Modifier.weight(7f).fillMaxHeight().background(Color.Black).padding(end = 10.dp),
                        fontSize = 25.sp, color = GreenLive, maxLines = 1
                    )
                    Text("${s.id}", Modifier.weight(1f), textAlign = TextAlign.Center, fontSize = 15.sp, color = Color.Black)
                }
                HorizontalDivider(color = Color(0xFFB0B0B0))
            }
        }
    }
}

// ---------------------------------------------------------------- فهرس الصفحات
@Composable
fun PageScreen(vm: AppViewModel, onBack: () -> Unit, openReader: () -> Unit) {
    val rows = remember { (1..604).map { vm.pageRow(it) } }
    val state = rememberLazyListState(initialFirstVisibleItemIndex = (vm.page - 1).coerceIn(0, 603))

    ScreenScaffold("البحث عن صفحة", onBack) {
        LazyColumn(state = state, modifier = Modifier.fillMaxSize()) {
            itemsIndexed(rows) { _, r ->
                Row(
                    Modifier.fillMaxWidth().background(ListGray)
                        .clickable { vm.goToPage(r.page); openReader() }
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                        .height(35.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        r.sora,
                        Modifier.weight(2f).fillMaxHeight().background(Color.Black).padding(end = 10.dp),
                        fontSize = 25.sp, color = GreenLive, maxLines = 1
                    )
                    Text("${r.page}", Modifier.weight(1f), textAlign = TextAlign.Center, fontSize = 25.sp, color = Color.Black)
                }
                HorizontalDivider(color = Color(0xFFB0B0B0))
            }
        }
    }
}

// ---------------------------------------------------------------- فهرس الأجزاء و الأحزاب
private data class JozStyle(val rowBg: Color, val hizbBg: Color, val hizbFg: Color, val textFg: Color, val label: String)

@Composable
fun JozScreen(vm: AppViewModel, onBack: () -> Unit, openReader: () -> Unit) {
    val rows by produceState(emptyList<JozRow>()) {
        value = withContext(Dispatchers.IO) { vm.repo.loadJoz() }
    }
    val state = rememberLazyListState()
    LaunchedEffect(rows) {
        if (rows.isNotEmpty()) state.scrollToItem(rows.indexOfLast { it.page <= vm.page }.coerceAtLeast(0))
    }

    ScreenScaffold("البحث عن جزء", onBack) {
        LazyColumn(state = state, modifier = Modifier.fillMaxSize().background(Color.Black)) {
            itemsIndexed(rows) { pos, row ->
                val st = when {
                    pos % 8 == 0 -> JozStyle(Color.Black, Color.Black, Color.White, Color.White,
                        "جزء ${1 + pos / 8}    حزب ${1 + pos / 4}")
                    pos % 4 == 0 -> JozStyle(ListGray, Color.Black, Color.White, Color.Black, "حزب ${1 + pos / 4}")
                    else -> JozStyle(ListGray, ListGray, Color.Black, Color.Black, row.hizb)
                }
                Column(
                    Modifier.fillMaxWidth().background(st.rowBg)
                        .clickable { vm.goToPage(row.page); openReader() }
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        st.label,
                        Modifier.fillMaxWidth().background(st.hizbBg),
                        textAlign = TextAlign.Center, fontSize = 20.sp, fontWeight = FontWeight.Bold,
                        color = st.hizbFg, textDecoration = TextDecoration.Underline
                    )
                    Text(row.ayaT, Modifier.fillMaxWidth().heightIn(min = 50.dp), fontSize = 20.sp, color = st.textFg)
                    Text(row.detail, Modifier.fillMaxWidth(), textAlign = TextAlign.Center, fontSize = 15.sp, color = st.textFg)
                }
                HorizontalDivider(color = Color(0xFF04860B), thickness = 2.dp)
            }
        }
    }
}
