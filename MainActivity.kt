package quran.hb.com.quran

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import quran.hb.com.quran.data.Storage
import quran.hb.com.quran.ui.*

class MainActivity : ComponentActivity() {

    private val vm: AppViewModel by viewModels()

    /** يتغير عند العودة من شاشة الإذن لإعادة الفحص */
    private var tick by mutableIntStateOf(0)

    private val readPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { tick++ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContent {
            MaterialTheme {
                val ok = remember(tick) { Storage.resolve(this) && Storage.dbAvailable(this) }
                LaunchedEffect(ok) { if (ok) vm.start() }

                when {
                    !ok -> StorageGate(
                        hasAccess = remember(tick) { Storage.hasSharedAccess(this) },
                        missing = remember(tick) { Storage.missingFolders() },
                        dbOk = remember(tick) { Storage.dbAvailable(this) },
                        onGrant = ::requestAccess,
                        onRetry = { tick++ }
                    )
                    !vm.ready -> Box(Modifier.fillMaxSize().background(Color(0xFFFFF4CB)), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                    else -> AppNav(vm)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        tick++
    }

    override fun onStop() {
        vm.stopAudio()
        super.onStop()
    }

    private fun requestAccess() {
        if (Build.VERSION.SDK_INT >= 30) {
            try {
                startActivity(
                    Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, Uri.parse("package:$packageName"))
                )
            } catch (e: Exception) {
                startActivity(Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION))
            }
        } else {
            readPermission.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }
}

@Composable
private fun StorageGate(
    hasAccess: Boolean,
    missing: List<String>,
    dbOk: Boolean,
    onGrant: () -> Unit,
    onRetry: () -> Unit
) {
    Column(
        Modifier.fillMaxSize().background(Color(0xFFFFF4CB)).systemBarsPadding().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("ملفات المصحف غير موجودة", fontSize = 22.sp, textAlign = TextAlign.Center)
        Spacer(Modifier.height(16.dp))
        Text(
            "ضع المجلدات HI و HA و WI و WA و db_quran داخل:\n/storage/emulated/0/QuranHW/",
            fontSize = 17.sp, textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(12.dp))
        if (!hasAccess) {
            Text("ثم امنح التطبيق إذن الوصول إلى الملفات.", fontSize = 17.sp, textAlign = TextAlign.Center)
            Spacer(Modifier.height(16.dp))
            Button(onClick = onGrant) { Text("منح إذن الوصول إلى الملفات") }
        } else {
            val parts = buildList {
                if (missing.isNotEmpty()) add("مجلدات ناقصة: " + missing.joinToString(" , "))
                if (!dbOk) add("قاعدة البيانات db_quran.sqlite غير موجودة")
            }
            if (parts.isNotEmpty()) Text(parts.joinToString("\n"), fontSize = 16.sp, textAlign = TextAlign.Center, color = Color(0xFFB00020))
        }
        Spacer(Modifier.height(12.dp))
        OutlinedButton(onClick = onRetry) { Text("إعادة المحاولة") }
    }
}

@Composable
private fun AppNav(vm: AppViewModel) {
    val nav = rememberNavController()
    val back: () -> Unit = { nav.popBackStack() }
    val openReader: () -> Unit = { nav.popBackStack("reader", false) }

    NavHost(nav, startDestination = "reader") {
        composable("reader") { ReaderScreen(vm) { route -> nav.navigate(route) } }
        composable("sora") { SoraScreen(vm, back, openReader) }
        composable("page") { PageScreen(vm, back, openReader) }
        composable("joz") { JozScreen(vm, back, openReader) }
        composable("search") { SearchScreen(vm, back, openReader) }
        composable("landmark") { LandmarkScreen(vm, back, openReader) }
        composable("tafsir") { TafsirScreen(vm, back) }
        composable("about") { AboutScreen(back) }
    }
}
