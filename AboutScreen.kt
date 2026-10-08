package quran.hb.com.quran.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import quran.hb.com.quran.R

@Composable
fun AboutScreen(onBack: () -> Unit) {
    val context = LocalContext.current

    @Composable
    fun Line(text: String, color: Color = Color.Unspecified, top: Int = 5, onClick: (() -> Unit)? = null) {
        Text(
            text,
            Modifier.fillMaxWidth().padding(top = top.dp, start = 10.dp, end = 10.dp)
                .let { if (onClick != null) it.clickable(onClick = onClick) else it },
            fontSize = 18.sp, color = color, textAlign = TextAlign.Start
        )
    }

    ScreenScaffold("حول", onBack) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            Line(stringResource(R.string.txt01), top = 28)
            Line("هشام  بن نادر - عين البيضاء - الجزائر")
            Line(stringResource(R.string.txt031), Color.Blue) {
                context.startActivity(
                    Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:benaderh@gmail.com"))
                )
            }
            Line(stringResource(R.string.txt041), Color.Blue) {
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("http://facebook.com/benaderh.hb.1")))
            }
            Box(Modifier.fillMaxWidth().padding(top = 10.dp).height(1.dp).background(Color(0xFF736F6F)))
            Line(stringResource(R.string.txt06), top = 10)
            Line(stringResource(R.string.txt07))
            Line(stringResource(R.string.txt08))
            Line(stringResource(R.string.txt09))
            Line(stringResource(R.string.txt10))
            Line(stringResource(R.string.txt11))
            Line(stringResource(R.string.txt12))
            Line(stringResource(R.string.txt13))
            Line(stringResource(R.string.txt14))
        }
    }
}
