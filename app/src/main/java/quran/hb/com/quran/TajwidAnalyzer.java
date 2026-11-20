package quran.hb.com.quran;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TajwidAnalyzer {

    // ── حروف مساعدة ──────────────────────────────────────────────────────────
    private static final String NOON       = "ن";
    private static final String MEEM       = "م";
    private static final String TANWIN     = "[\u064B\u064C\u064D]";   // تنوين
    private static final String SUKUN      = "\u0652";                 // سكون
    private static final String SHADDA     = "\u0651";                 // شدة
    private static final String MADD_ALEF  = "[\u0627\u0649]";        // ا ى
    private static final String MADD_WAW   = "\u0648";                 // و
    private static final String MADD_YAA   = "\u064A";                 // ي

    // حروف الإدغام بغنة
    private static final String IDGHAM_GH  = "[ينمو]";
    // حروف الإخفاء
    private static final String IKHFA_CHARS= "[تثجدذرزسشصضطظفقك]";
    // حروف القلقلة
    private static final String QALQALA    = "[قطبجد]";
    // حروف التفخيم
    private static final String TAFKHIM    = "[خصضغطقظ]";

    public List<TajwidRule> analyze(String text) {
        List<TajwidRule> rules = new ArrayList<>();
        if (text == null || text.isEmpty()) return rules;

        checkNoonSakinTanwin(text, rules);
        checkMimSakin(text, rules);
        checkMadd(text, rules);
        checkQalqala(text, rules);
        checkGhunna(text, rules);
        checkTafkhimTarqiq(text, rules);

        return rules;
    }

    // ── 1. نون ساكنة وتنوين ────────────────────────────────────────────────
    private void checkNoonSakinTanwin(String text, List<TajwidRule> rules) {
        // إدغام : نون ساكنة + حرف إدغام
        Pattern p = Pattern.compile("ن" + SUKUN + IDGHAM_GH);
        Matcher m = p.matcher(text);
        while (m.find()) {
            rules.add(new TajwidRule("إدغام", "Idgham",
                "النون الساكنة تُدغم في " + text.charAt(m.start() + 2),
                TajwidRule.SEVERITY_WARNING, m.group()));
        }
        // إقلاب : نون ساكنة + باء
        p = Pattern.compile("ن" + SUKUN + "ب");
        m = p.matcher(text);
        while (m.find()) {
            rules.add(new TajwidRule("إقلاب", "Iqlab",
                "النون الساكنة تُقلب ميماً قبل الباء",
                TajwidRule.SEVERITY_WARNING, m.group()));
        }
        // إخفاء : نون ساكنة + حرف إخفاء
        p = Pattern.compile("ن" + SUKUN + IKHFA_CHARS);
        m = p.matcher(text);
        while (m.find()) {
            rules.add(new TajwidRule("إخفاء", "Ikhfa",
                "النون الساكنة تُخفى قبل " + text.charAt(m.start() + 2),
                TajwidRule.SEVERITY_WARNING, m.group()));
        }
    }

    // ── 2. ميم ساكنة ───────────────────────────────────────────────────────
    private void checkMimSakin(String text, List<TajwidRule> rules) {
        Pattern p = Pattern.compile("م" + SUKUN + "ب");
        Matcher m = p.matcher(text);
        while (m.find()) {
            rules.add(new TajwidRule("إخفاء شفوي", "Ikhfa Shafawi",
                "الميم الساكنة تُخفى شفوياً قبل الباء",
                TajwidRule.SEVERITY_WARNING, m.group()));
        }
        p = Pattern.compile("م" + SUKUN + "م");
        m = p.matcher(text);
        while (m.find()) {
            rules.add(new TajwidRule("إدغام شفوي", "Idgham Shafawi",
                "الميم الساكنة تُدغم في الميم",
                TajwidRule.SEVERITY_WARNING, m.group()));
        }
    }

    // ── 3. مد ──────────────────────────────────────────────────────────────
    private void checkMadd(String text, List<TajwidRule> rules) {
        // مد طبيعي : حرف مد بعد فتحة/ضمة/كسرة
        Pattern p = Pattern.compile("[اوي]" + SUKUN);
        Matcher m = p.matcher(text);
        while (m.find()) {
            rules.add(new TajwidRule("مد طبيعي", "Madd Tabii",
                "مد طبيعي (2 حركات)",
                TajwidRule.SEVERITY_OK, m.group()));
        }
        // مد متصل
        p = Pattern.compile("[اوي]\u0621");   // حرف مد + همزة
        m = p.matcher(text);
        while (m.find()) {
            rules.add(new TajwidRule("مد متصل واجب", "Madd Muttasil",
                "مد متصل واجب (4-5 حركات)",
                TajwidRule.SEVERITY_WARNING, m.group()));
        }
    }

    // ── 4. قلقلة ───────────────────────────────────────────────────────────
    private void checkQalqala(String text, List<TajwidRule> rules) {
        Pattern p = Pattern.compile(QALQALA + SUKUN);
        Matcher m = p.matcher(text);
        while (m.find()) {
            rules.add(new TajwidRule("قلقلة", "Qalqala",
                "حرف القلقلة '" + m.group().charAt(0) + "' يحتاج قلقلة عند السكون",
                TajwidRule.SEVERITY_WARNING, m.group()));
        }
    }

    // ── 5. غنة ─────────────────────────────────────────────────────────────
    private void checkGhunna(String text, List<TajwidRule> rules) {
        Pattern p = Pattern.compile("[نم]" + SHADDA);
        Matcher m = p.matcher(text);
        while (m.find()) {
            rules.add(new TajwidRule("غنة", "Ghunna",
                "'" + m.group().charAt(0) + "' مشددة تستوجب غنة (2 حركات)",
                TajwidRule.SEVERITY_WARNING, m.group()));
        }
    }

    // ── 6. تفخيم وترقيق ────────────────────────────────────────────────────
    private void checkTafkhimTarqiq(String text, List<TajwidRule> rules) {
        Pattern p = Pattern.compile(TAFKHIM);
        Matcher m = p.matcher(text);
        while (m.find()) {
            rules.add(new TajwidRule("تفخيم", "Tafkhim",
                "الحرف '" + m.group() + "' من حروف التفخيم",
                TajwidRule.SEVERITY_OK, m.group()));
        }
    }

    // ── مقارنة كلمتين (Levenshtein) ────────────────────────────────────────
    public static int levenshtein(String a, String b) {
        int la = a.length(), lb = b.length();
        int[][] dp = new int[la + 1][lb + 1];
        for (int i = 0; i <= la; i++) dp[i][0] = i;
        for (int j = 0; j <= lb; j++) dp[0][j] = j;
        for (int i = 1; i <= la; i++)
            for (int j = 1; j <= lb; j++) {
                int cost = (a.charAt(i-1) == b.charAt(j-1)) ? 0 : 1;
                dp[i][j] = Math.min(Math.min(dp[i-1][j]+1, dp[i][j-1]+1), dp[i-1][j-1]+cost);
            }
        return dp[la][lb];
    }

    public static int calculateScore(String[] recognized, String[] reference) {
        if (reference == null || reference.length == 0) return 0;
        int correct = 0;
        int minLen  = Math.min(recognized.length, reference.length);
        for (int i = 0; i < minLen; i++) {
            if (levenshtein(normalize(recognized[i]), normalize(reference[i])) <= 1) correct++;
        }
        return (int) (100.0 * correct / reference.length);
    }

    public static String normalize(String s) {
        if (s == null) return "";
        return s.replaceAll("[\u064B-\u065F\u0670]", "").trim();
    }
}