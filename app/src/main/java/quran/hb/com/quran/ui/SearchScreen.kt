package quran.hb.com.quran.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import quran.hb.com.quran.AppViewModel
import quran.hb.com.quran.data.Verse
import quran.hb.com.quran.data.normalizeAr

@Composable
fun SearchScreen(vm: AppViewModel, onBack: () -> Unit, openReader: () -> Unit) {
    var query by remember { mutableStateOf("") }
    val verses by produceState(emptyList<Verse>(), vm.riwaya) { value = vm.loadVerses() }
    val filtered = remember(query, verses) {
        val q = query.trim().normalizeAr()
        if (q.isEmpty()) verses else verses.filter { it.norm.contains(q) }
    }

    ScreenScaffold("البحث عن آية", onBack) {
        Row(Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Search, null, Modifier.width(48.dp))
            Text(
                "${filtered.size}", Modifier.width(80.dp), textAlign = TextAlign.Center,
                fontSize = 25.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD9270F)
            )
            OutlinedTextField(
                value = query, onValueChange = { query = it },
                modifier = Modifier.weight(1f),
                textStyle = TextStyle(fontSize = 25.sp),
                placeholder = { Text("بـحـث ...", fontSize = 25.sp) },
                singleLine = true
            )
        }
        LazyColumn(Modifier.fillMaxSize()) {
            items(filtered) { v ->
                Column(
                    Modifier.fillMaxWidth()
                        .clickable {
                            vm.goToPage(v.page.toIntOrNull() ?: 1, v.line.toIntOrNull() ?: 0, v.point.toIntOrNull() ?: 0)
                            openReader()
                        }
                        .padding(horizontal = 5.dp)
                ) {
                    Text(
                        v.sora, Modifier.fillMaxWidth().padding(top = 15.dp, end = 15.dp),
                        fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color(0xFF0B18D2)
                    )
                    Text(v.text, Modifier.fillMaxWidth(), fontSize = 25.sp)
                    Row(Modifier.fillMaxWidth().padding(horizontal = 5.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("الصفحة: ${v.page}", fontSize = 15.sp)
                        Text("الآية: ${v.ayaNo}", fontSize = 15.sp)
                    }
                    HorizontalDivider(Modifier.padding(top = 6.dp), color = Color(0xFFD0D0D0))
                }
            }
        }
    }
}
