package quran.hb.com.quran.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import quran.hb.com.quran.AppViewModel

private val Green = Color(0xFF368F0A)

@Composable
fun LandmarkScreen(vm: AppViewModel, onBack: () -> Unit, openReader: () -> Unit) {
    val current = vm.pageRow(vm.page)

    ScreenScaffold("حفظ و تعديل العلامة", onBack) {
        Column(Modifier.fillMaxSize().background(Color.Black).padding(horizontal = 10.dp)) {
            Spacer(Modifier.height(40.dp))
            Box(Modifier.fillMaxWidth().height(80.dp).background(Green), contentAlignment = Alignment.Center) {
                Text(
                    "الصفحة الحالية\nسورة ${current.soraName}\nالصفحة ${vm.page}",
                    color = Color.White, fontSize = 20.sp, textAlign = TextAlign.Center
                )
            }
            Spacer(Modifier.height(40.dp))
            HorizontalDivider(color = Green)

            vm.bookmarks.forEachIndexed { i, b ->
                Row(
                    Modifier.fillMaxWidth().height(70.dp).padding(top = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BookmarkButton("تغيير", Modifier.weight(1f)) {
                        vm.saveBookmark(i); openReader()
                    }
                    Text(
                        "سورة ${b.sora}\nالصفحة ${b.page}",
                        Modifier.weight(2f), textAlign = TextAlign.Center,
                        fontSize = 20.sp, color = Color(0xF977E42E)
                    )
                    BookmarkButton("فتح", Modifier.weight(1f)) {
                        vm.goToPage(b.page); openReader()
                    }
                }
                HorizontalDivider(Modifier.padding(top = 5.dp), color = Green)
            }
        }
    }
}

@Composable
private fun BookmarkButton(text: String, modifier: Modifier, onClick: () -> Unit) {
    Button(
        onClick = onClick, modifier = modifier.height(50.dp),
        shape = RoundedCornerShape(6.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF333333), contentColor = Color.White)
    ) { Text(text) }
}
