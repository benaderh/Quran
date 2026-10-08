package quran.hb.com.quran.data

/** tb_indice : أعمدة بالترتيب كما في التطبيق القديم */
data class Indice(
    val lastPage: Int,        // 0  last_page
    val riwaya: Int,          // 1  last_riwaya (1 = حفص ، 2 = ورش)
    val bookmarkPages: List<Int>,    // 2,4,6  ri1..ri3
    val bookmarkNames: List<String>, // 3,5,7  rt1..rt3
    val soraIndex: Int,       // 10 soraIndex
    val ayaIndex: Int,        // 11 ayaIndex
    val fontSize: Int         // 12 sizei
)

/** tb_q : صفحة المصحف */
data class PageRow(val page: Int, val sora: String, val joz: String, val soraName: String)

/** tb_sora : id ، الاسم ، صفحة البداية ، رقم خانة البداية في الشبكة */
data class SoraRow(val id: Int, val name: String, val page: Int, val start: Int)

/** qr_joz : ثمن حزب */
data class JozRow(val page: Int, val hizb: String, val ayaT: String, val detail: String)

/** سطر من tb_tafcir / tb_tafcirW للبحث */
data class Verse(
    val sora: String, val ayaNo: String, val text: String,
    val page: String, val line: String, val point: String,
    val norm: String
)

data class Tafsir(val aya: String, val ayaNo: String, val soraName: String, val tafsir: String, val page: Int)

/** توقيت الآية في ملف Excel الخاص بالسورة */
data class Timing(val pos: Int, val aya: Int, val line: Int)

data class Bookmark(val page: Int, val sora: String)

/** إزالة التشكيل وتوحيد الألف و الياء ليصبح البحث أسهل */
fun String.normalizeAr(): String {
    val sb = StringBuilder(length)
    for (ch in this) {
        val code = ch.code
        val skip = code in 0x064B..0x065F || code == 0x0670 || code in 0x06D6..0x06ED || code == 0x0640
        if (skip) continue
        sb.append(
            when (ch) {
                'أ', 'إ', 'آ', 'ٱ' -> 'ا'
                'ى' -> 'ي'
                else -> ch.lowercaseChar()
            }
        )
    }
    return sb.toString()
}
