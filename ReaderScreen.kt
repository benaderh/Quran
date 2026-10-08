package quran.hb.com.quran.ui

import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import kotlinx.coroutines.flow.drop
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import coil.compose.AsyncImage
import quran.hb.com.quran.AppViewModel
import quran.hb.com.quran.data.Storage
import quran.hb.com.quran.data.Trace
import kotlin.math.roundToInt

private val BarHeight = 36.dp

@Composable
fun ReaderScreen(vm: AppViewModel, onNavigate: (String) -> Unit) {
    val context = LocalContext.current
    val view = LocalView.current

    // وضع ملء الشاشة كما في التطبيق القديم
    DisposableEffect(Unit) {
        val window = (view.context as Activity).window
        val controller = WindowCompat.getInsetsController(window, view)
        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        onDispose {
            controller.show(WindowInsetsCompat.Type.systemBars())
            vm.stopAudio()
        }
    }

    val pagerState = rememberPagerState(initialPage = vm.page.coerceIn(0, 608)) { 609 }
    var bar by remember { mutableStateOf(false) }
    var menu by remember { mutableStateOf(false) }
    var jumpedTo by remember { mutableIntStateOf(-1) }

    // انتقال خارجي (من الفهارس / العلامات / البحث)
    LaunchedEffect(vm.page) {
        if (pagerState.currentPage != vm.page) pagerState.scrollToPage(vm.page)
    }

    // تغيّر الصفحة بالتمرير
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect { p ->
            if (p != vm.page) {
                vm.onPageChanged(p)
                val joz = vm.pageRow(p).joz
                if (joz.length > 8) Toast.makeText(context, joz, Toast.LENGTH_SHORT).show()
            }
            if (p == 0 || p > 604) {
                bar = false
                menu = false
            }
        }
    }

    // تصفح دائري : الصفحة 0 و 608 طرفان يقفز كل منهما إلى الآخر (كما في النسخة القديمة)
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage }.drop(1).collect { s ->
            if (s == jumpedTo) {
                jumpedTo = -1
                return@collect
            }
            if (s == 0) {
                jumpedTo = 608
                pagerState.scrollToPage(608)
            } else if (s == 608) {
                jumpedTo = 0
                pagerState.scrollToPage(0)
            }
        }
    }

    val onTap: (Int, Offset, IntSize) -> Unit = { p, off, size ->
        vm.stopAudio()
        vm.highlight = null
        if (!bar && p in 1..604) {
            bar = true
            // الشبكة : 4 أعمدة × 15 سطرًا ، العمود 1 = أقصى اليمين
            val row = (off.y / (size.height / 15f)).toInt().coerceIn(0, 14) + 1
            val colLeft = (off.x / (size.width / 4f)).toInt().coerceIn(0, 3)
            vm.selectCell(p, row, 4 - colLeft)
        } else {
            bar = false
            menu = false
        }
    }

    val onLong: (Int) -> Unit = { p ->
        if (p in 1..604) onNavigate("landmark")
    }

    Box(Modifier.fillMaxSize().background(Cream)) {
        // الصفحات من اليمين إلى اليسار
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { p ->
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    PageView(vm, p, onTap, onLong)
                }
            }
        }

        if (bar) {
            val current = vm.pageRow(pagerState.currentPage)

            Column(Modifier.align(Alignment.TopStart).fillMaxWidth()) {
                Row(Modifier.fillMaxWidth()) {
                    // بحث
                    Box(
                        Modifier.weight(0.5f).height(BarHeight).background(DarkBar)
                            .clickable { onNavigate("search") },
                        contentAlignment = Alignment.Center
                    ) { Icon(Icons.Default.Search, null, tint = Color.White) }

                    BarButton("ورش", 1f, DarkBar, if (vm.riwaya == 2) Color.White else Color(0xFFAEA7A7), vm.riwaya == 2) {
                        vm.setRiwaya(2); bar = false; menu = false
                    }
                    BarButton("حفص", 1f, DarkBar, if (vm.riwaya == 1) Color.White else Color(0xFFAEA7A7), vm.riwaya == 1) {
                        vm.setRiwaya(1); bar = false; menu = false
                    }
                    BarButton("تفسير", 1f, DarkBar) {
                        Trace.reset()
                        Trace.step("1 - ضغط زر تفسير")
                        vm.prepareTafsir()
                        Trace.step("2 - تحضير الآية تم")
                        onNavigate("tafsir")
                        Trace.step("3 - الانتقال إلى الشاشة")
                    }
                    BarButton("تلاوة", 1f, Color(0xFF227E08), Color.White, true) {
                        vm.play(); bar = false; menu = false
                    }
                    BarButton("القائمة", 1f, DarkBar) { menu = !menu }
                }
                if (menu) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        Box(
                            Modifier.height(BarHeight).background(DarkBar).clickable { onNavigate("about") }
                                .padding(horizontal = 16.dp),
                            contentAlignment = Alignment.Center
                        ) { Text("حول", color = Color.White, fontSize = 14.sp) }
                    }
                }
            }

            Row(Modifier.align(Alignment.BottomStart).fillMaxWidth()) {
                BarButton(current.sora, 2f, DarkBar) { onNavigate("sora") }
                BarButton("${current.page}", 1f, DarkBar) { onNavigate("page") }
                BarButton(current.joz, 3f, DarkBar) { onNavigate("joz") }
            }
        }
    }
}

@Composable
private fun PageView(
    vm: AppViewModel,
    p: Int,
    onTap: (Int, Offset, IntSize) -> Unit,
    onLong: (Int) -> Unit
) {
    var size by remember { mutableStateOf(IntSize.Zero) }
    val density = LocalDensity.current
    val imageFile = if (vm.riwaya == 1) Storage.file("HI", "p$p.jpg") else Storage.file("WI", "p$p.png")

    Box(
        Modifier.fillMaxSize()
            .background(Cream)
            .onSizeChanged { size = it }
            .pointerInput(p, vm.riwaya) {
                detectTapGestures(
                    onLongPress = { onLong(p) },
                    onTap = { off -> onTap(p, off, size) }
                )
            }
    ) {
        AsyncImage(
            model = imageFile,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // إطار تمييز موضع الآية بعد الاختيار من نتائج البحث
        val hl = vm.highlight
        if (hl != null && p == vm.page && size.width > 0) {
            val (line, point) = hl
            val cellW = size.width / 4f
            val cellH = size.height / 15f
            val left = (4 - point).coerceIn(0, 3) * cellW
            val top = (line - 1).coerceAtLeast(0) * cellH
            Box(
                Modifier
                    .offset { IntOffset(left.roundToInt(), top.roundToInt()) }
                    .size(with(density) { cellW.toDp() }, with(density) { cellH.toDp() })
                    .background(Color(0x44521400))
            )
        }
    }
}

@Composable
private fun RowScope.BarButton(
    text: String,
    weight: Float,
    bg: Color,
    fg: Color = Color.White,
    bold: Boolean = false,
    onClick: () -> Unit
) {
    Box(
        Modifier.weight(weight).height(BarHeight).background(bg).clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text, color = fg, fontSize = 14.sp, maxLines = 1,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal
        )
    }
}
