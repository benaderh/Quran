package quran.hb.com.quran

import android.app.Application
import android.media.MediaPlayer
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import quran.hb.com.quran.data.*

class AppViewModel(app: Application) : AndroidViewModel(app) {

    lateinit var repo: QuranDb
        private set

    var ready by mutableStateOf(false); private set

    var pages: Map<Int, PageRow> = emptyMap(); private set
    var soras: List<SoraRow> = emptyList(); private set

    /** الصفحة الحالية (0..608) */
    var page by mutableIntStateOf(1); private set
    /** 1 = حفص ، 2 = ورش */
    var riwaya by mutableIntStateOf(1); private set
    var fontSize by mutableIntStateOf(30); private set
    var bookmarks by mutableStateOf(listOf(Bookmark(0, ""), Bookmark(0, ""), Bookmark(0, "")))
        private set

    /** (سطر، عمود) لإطار التمييز بعد الضغط على نتيجة بحث */
    var highlight by mutableStateOf<Pair<Int, Int>?>(null)

    // الآية المختارة بالضغط على الصفحة
    private var selSora = 0
    private var selAya = 0
    private var selPos = 0

    var tafsirSora = 1; private set
    var tafsirAya = 1; private set

    @Volatile private var player: MediaPlayer? = null

    private var started = false

    /** يُستدعى بعد التأكد من وجود الملفات و الإذن */
    fun start() {
        if (started) return
        started = true
        viewModelScope.launch(Dispatchers.IO) {
            val r = QuranDb(getApplication<Application>())
            repo = r
            val ind = r.readIndice()
            pages = r.loadPages().associateBy { it.page }
            soras = r.loadSoras()
            page = ind.lastPage.coerceIn(0, 608)
            riwaya = ind.riwaya
            fontSize = ind.fontSize
            tafsirSora = ind.soraIndex.coerceAtLeast(1)
            tafsirAya = ind.ayaIndex.coerceAtLeast(1)
            bookmarks = List(3) { Bookmark(ind.bookmarkPages[it], ind.bookmarkNames[it]) }
            ready = true
        }
    }

    private fun io(block: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) { block() }
    }

    fun pageRow(p: Int): PageRow = pages[p] ?: PageRow(p, "", "", "")

    // ---------- التنقل بين الصفحات ----------
    fun goToPage(p: Int, line: Int = 0, point: Int = 0) {
        page = p
        highlight = if (line > 0) line to point else null
        io { repo.setLastPage(p) }
    }

    /** يُستدعى عند تمرير المستخدم بين الصفحات */
    fun onPageChanged(p: Int) {
        if (p == page) return
        page = p
        highlight = null
        io { repo.setLastPage(p) }
    }

    fun setRiwaya(r: Int) {
        riwaya = r
        io { repo.setRiwaya(r) }
    }

    // ---------- العلامات ----------
    /** i = 0..2 : حفظ الصفحة الحالية في العلامة */
    fun saveBookmark(i: Int) {
        val b = Bookmark(page, pageRow(page).soraName)
        bookmarks = bookmarks.toMutableList().also { it[i] = b }
        io { repo.setBookmark(i + 1, b.page, b.sora) }
    }

    // ---------- اختيار آية بالضغط ----------
    /** colFromRight : 1 = أقصى اليمين ... 4 = أقصى اليسار (كما في التطبيق القديم) */
    fun selectCell(p: Int, row: Int, colFromRight: Int) {
        val idx = (p - 1) * 60 + (row - 1) * 4 + colFromRight + 1
        val s = soras.lastOrNull { it.start <= idx } ?: return
        selSora = s.id
        val rel = idx - s.start
        val dir = audioDir()
        viewModelScope.launch(Dispatchers.IO) {
            val t = repo.readTiming(dir, s.id, rel)
            selPos = t?.pos ?: 0
            selAya = t?.aya ?: 0
        }
    }

    private fun audioDir() = if (riwaya == 1) "HA" else "WA"

    // ---------- التلاوة ----------
    fun play() {
        stopAudio()
        val s = selSora
        if (s <= 0) return
        val pos = selPos
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val mp = MediaPlayer()
                mp.setDataSource(Storage.file(audioDir(), "s$s.wav").path)
                mp.prepare()
                mp.seekTo(pos)
                mp.setOnCompletionListener {
                    it.release()
                    if (player === it) player = null
                }
                mp.start()
                player = mp
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun stopAudio() {
        player?.let {
            try { it.stop() } catch (_: Exception) {}
            it.release()
        }
        player = null
    }

    // ---------- التفسير ----------
    fun prepareTafsir() {
        if (selSora > 0) {
            tafsirSora = selSora
            tafsirAya = selAya.coerceAtLeast(1)
            io { repo.setTafsirIndex(tafsirSora, tafsirAya) }
        }
    }

    fun saveFontSize(s: Int) {
        fontSize = s
        io { repo.setFontSize(s) }
    }

    suspend fun loadVerses(): List<Verse> = withContext(Dispatchers.IO) { repo.loadVerses(riwaya) }

    override fun onCleared() {
        stopAudio()
        super.onCleared()
    }
}
