package quran.hb.com.quran;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class TilawaCorrectionActivity extends Activity {

    // ── Constantes ────────────────────────────────────────────────────────
    private static final int REQ_AUDIO_PERM = 200;
    private static final int MODE_IMLA      = 0;   // إملاء
    private static final int MODE_MURAJA    = 1;   // مراجعة

    // ── Mode / riwaya courants ─────────────────────────────────────────────
    private int     currentMode  = MODE_IMLA;
    private boolean isHafs       = true;

    // ── Sora / aya ────────────────────────────────────────────────────────
    private int soraNum = 1;
    private int ayaNum  = 1;

    // ── Vues partagées ────────────────────────────────────────────────────
    private RadioGroup  rgMode;
    private RadioButton rbImla, rbMuraja;
    private Button      btnRiwaya;
    private TextView    tvSoraAya, tvTitle;
    private LinearLayout llImla, llMuraja, llResult;
    private ScrollView  svResult;

    // ── Vues mode إملاء ──────────────────────────────────────────────────
    private Button   btnRecord, btnAnalyze;
    private TextView tvRecognized, tvScore;
    private LinearLayout llRules;

    // ── Vues mode مراجعة ─────────────────────────────────────────────────
    private Button   btnRecordMuraja;
    private TextView tvAyaRef, tvComparison, tvScoreMuraja;
    private LinearLayout llRulesMuraja;

    // ── Helpers ───────────────────────────────────────────────────────────
    private QuranTextMatcher matcher;
    private TajwidAnalyzer   analyzer;
    private SpeechRecognizer speechRecognizer;

    // ── Noms des 114 sourates ─────────────────────────────────────────────
    private static final String[] SORA_NAMES = {
        "الفاتحة","البقرة","آل عمران","النساء","المائدة","الأنعام","الأعراف","الأنفال",
        "التوبة","يونس","هود","يوسف","الرعد","إبراهيم","الحجر","النحل","الإسراء",
        "الكهف","مريم","طه","الأنبياء","الحج","المؤمنون","النور","الفرقان","الشعراء",
        "النمل","القصص","العنكبوت","الروم","لقمان","السجدة","الأحزاب","سبأ","فاطر",
        "يس","الصافات","ص","الزمر","غافر","فصلت","الشورى","الزخرف","الدخان","الجاثية",
        "الأحقاف","محمد","الفتح","الحجرات","ق","الذاريات","الطور","النجم","القمر",
        "الرحمن","الواقعة","الحديد","المجادلة","الحشر","الممتحنة","الصف","الجمعة",
        "المنافقون","التغابن","الطلاق","التحريم","الملك","القلم","الحاقة","المعارج",
        "نوح","الجن","المزمل","المدثر","القيامة","الإنسان","المرسلات","النبأ",
        "النازعات","عبس","التكوير","الانفطار","المطففين","الانشقاق","البروج","الطارق",
        "الأعلى","الغاشية","الفجر","البلد","الشمس","الليل","الضحى","الشرح","التين",
        "العلق","القدر","البينة","الزلزلة","العاديات","القارعة","التكاثر","العصر",
        "الهمزة","الفيل","قريش","الماعون","الكوثر","الكافرون","النصر","المسد","الإخلاص",
        "الفلق","الناس"
    };

    private static final int[] AYA_COUNTS = {
        7,286,200,176,120,165,206,75,129,109,123,111,43,52,99,128,111,110,98,135,
        112,78,118,64,77,227,93,88,69,60,34,30,73,54,45,83,182,88,75,85,54,53,89,
        59,37,35,38,29,18,45,60,25,78,65,30,54,71,40,52,45,35,8,24,33,30,22,13,
        43,32,24,57,19,96,15,13,25,22,11,8,78,28,92,55,45,36,25,35,38,29,106,21,
        49,33,30,22,14,13,11,4,8,65,174,13,4,5,6,19,2,12,3,5,2,4,27,1,7,2,19,5,8,2,20,109,112
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        matcher  = new QuranTextMatcher(this);
        analyzer = new TajwidAnalyzer();

        // Récupérer extras depuis QMain
        Intent intent = getIntent();
        if (intent != null) {
            soraNum = intent.getIntExtra("soraNum", 1);
            ayaNum  = intent.getIntExtra("ayaNum", 1);
            isHafs  = (intent.getIntExtra("isHafs", 1) == 1);
        }

        buildUI();
    }

    // ══════════════════════════════════════════════════════════════════════
    //  Construction de l'UI par code
    // ══════════════════════════════════════════════════════════════════════
    private void buildUI() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.parseColor("#F5F5F5"));
        root.setPadding(16, 16, 16, 16);

        // ── Titre ────────────────────────────────────────────────────────
        tvTitle = new TextView(this);
        tvTitle.setText("تقييم التلاوة");
        tvTitle.setTextSize(20);
        tvTitle.setGravity(Gravity.CENTER);
        tvTitle.setTextColor(Color.parseColor("#1B5E20"));
        root.addView(tvTitle, lp(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        // ── Sora / Aya ───────────────────────────────────────────────────
        tvSoraAya = new TextView(this);
        updateSoraAyaLabel();
        tvSoraAya.setTextSize(16);
        tvSoraAya.setGravity(Gravity.CENTER);
        tvSoraAya.setPadding(0, 8, 0, 8);
        root.addView(tvSoraAya, lp(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        // ── Boutons navigation ───────────────────────────────────────────
        LinearLayout llNav = new LinearLayout(this);
        llNav.setOrientation(LinearLayout.HORIZONTAL);
        llNav.setGravity(Gravity.CENTER);

        Button btnPrevAya = makeBtn("◀ آية", "#455A64");
        Button btnNextAya = makeBtn("آية ▶", "#455A64");
        Button btnPrevSora = makeBtn("◀ سورة", "#37474F");
        Button btnNextSora = makeBtn("سورة ▶", "#37474F");

        llNav.addView(btnPrevSora, lpW(0, 1f));
        llNav.addView(btnPrevAya,  lpW(0, 1f));
        llNav.addView(btnNextAya,  lpW(0, 1f));
        llNav.addView(btnNextSora, lpW(0, 1f));
        root.addView(llNav, lp(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        // ── Riwaya ───────────────────────────────────────────────────────
        btnRiwaya = makeBtn(isHafs ? "رواية: حفص" : "رواية: ورش", "#0277BD");
        root.addView(btnRiwaya, lp(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        // ── Mode (RadioGroup) ────────────────────────────────────────────
        rgMode  = new RadioGroup(this);
        rgMode.setOrientation(RadioGroup.HORIZONTAL);
        rgMode.setGravity(Gravity.CENTER);
        rbImla   = new RadioButton(this); rbImla.setText("إملاء (ترتيل بدون نص)");
        rbMuraja = new RadioButton(this); rbMuraja.setText("مراجعة (مع النص)");
        rbImla.setChecked(true);
        rgMode.addView(rbImla);
        rgMode.addView(rbMuraja);
        root.addView(rgMode, lp(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        // ══ Panel إملاء ══════════════════════════════════════════════════
        llImla = new LinearLayout(this);
        llImla.setOrientation(LinearLayout.VERTICAL);
        llImla.setPadding(0, 12, 0, 0);

        btnRecord = makeBtn("🎤  بدء التسجيل", "#C62828");
        llImla.addView(btnRecord, lp(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        tvRecognized = new TextView(this);
        tvRecognized.setText("النص المُعرَّف سيظهر هنا...");
        tvRecognized.setTextSize(16);
        tvRecognized.setTextColor(Color.parseColor("#1A237E"));
        tvRecognized.setGravity(Gravity.RIGHT);
        tvRecognized.setPadding(8, 12, 8, 12);
        tvRecognized.setBackgroundColor(Color.WHITE);
        llImla.addView(tvRecognized, lp(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        btnAnalyze = makeBtn("🔍  تحليل التجويد", "#2E7D32");
        btnAnalyze.setVisibility(View.GONE);
        llImla.addView(btnAnalyze, lp(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        tvScore = new TextView(this);
        tvScore.setTextSize(18);
        tvScore.setGravity(Gravity.CENTER);
        tvScore.setPadding(0, 8, 0, 8);
        llImla.addView(tvScore, lp(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        llRules = new LinearLayout(this);
        llRules.setOrientation(LinearLayout.VERTICAL);
        llImla.addView(llRules, lp(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        root.addView(llImla, lp(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        // ══ Panel مراجعة ════════════════════════════════════════════════
        llMuraja = new LinearLayout(this);
        llMuraja.setOrientation(LinearLayout.VERTICAL);
        llMuraja.setVisibility(View.GONE);
        llMuraja.setPadding(0, 12, 0, 0);

        tvAyaRef = new TextView(this);
        tvAyaRef.setTextSize(18);
        tvAyaRef.setGravity(Gravity.RIGHT);
        tvAyaRef.setPadding(8, 12, 8, 12);
        tvAyaRef.setBackgroundColor(Color.WHITE);
        tvAyaRef.setTextColor(Color.parseColor("#1A237E"));
        llMuraja.addView(tvAyaRef, lp(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        btnRecordMuraja = makeBtn("🎤  بدء الترتيل", "#C62828");
        llMuraja.addView(btnRecordMuraja, lp(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        tvComparison = new TextView(this);
        tvComparison.setTextSize(16);
        tvComparison.setGravity(Gravity.RIGHT);
        tvComparison.setPadding(8, 12, 8, 12);
        tvComparison.setBackgroundColor(Color.WHITE);
        llMuraja.addView(tvComparison, lp(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        tvScoreMuraja = new TextView(this);
        tvScoreMuraja.setTextSize(18);
        tvScoreMuraja.setGravity(Gravity.CENTER);
        tvScoreMuraja.setPadding(0, 8, 0, 8);
        llMuraja.addView(tvScoreMuraja, lp(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        llRulesMuraja = new LinearLayout(this);
        llRulesMuraja.setOrientation(LinearLayout.VERTICAL);
        llMuraja.addView(llRulesMuraja, lp(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        root.addView(llMuraja, lp(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        // ── ScrollView ───────────────────────────────────────────────────
        svResult = new ScrollView(this);
        svResult.addView(root);
        setContentView(svResult);

        // ══ Listeners ═════════════════════════════════════════════════════
        btnPrevAya.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                if (ayaNum > 1) { ayaNum--; updateSoraAyaLabel(); refreshMurajaText(); }
            }
        });
        btnNextAya.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                if (ayaNum < getAyaCount()) { ayaNum++; updateSoraAyaLabel(); refreshMurajaText(); }
            }
        });
        btnPrevSora.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                if (soraNum > 1) { soraNum--; ayaNum = 1; updateSoraAyaLabel(); refreshMurajaText(); }
            }
        });
        btnNextSora.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                if (soraNum < 114) { soraNum++; ayaNum = 1; updateSoraAyaLabel(); refreshMurajaText(); }
            }
        });

        btnRiwaya.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                isHafs = !isHafs;
                btnRiwaya.setText(isHafs ? "رواية: حفص" : "رواية: ورش");
                refreshMurajaText();
            }
        });

        rgMode.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == rbImla.getId()) {
                    currentMode = MODE_IMLA;
                    llImla.setVisibility(View.VISIBLE);
                    llMuraja.setVisibility(View.GONE);
                } else {
                    currentMode = MODE_MURAJA;
                    llImla.setVisibility(View.GONE);
                    llMuraja.setVisibility(View.VISIBLE);
                    refreshMurajaText();
                }
            }
        });

        // Bouton record (Imlâ')
        btnRecord.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { startListening(MODE_IMLA); }
        });

        // Bouton record (Murâja'a)
        btnRecordMuraja.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { startListening(MODE_MURAJA); }
        });

        // Bouton analyser
        btnAnalyze.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                String text = tvRecognized.getText().toString();
                showTajwidRules(text, llRules, tvScore, null);
            }
        });
    }

    // ══════════════════════════════════════════════════════════════════════
    //  Helpers UI
    // ══════════════════════════════════════════════════════════════════════
    private Button makeBtn(String text, String color) {
        Button b = new Button(this);
        b.setText(text);
        b.setBackgroundColor(Color.parseColor(color));
        b.setTextColor(Color.WHITE);
        b.setTextSize(14);
        b.setPadding(8, 8, 8, 8);
        return b;
    }

    private LinearLayout.LayoutParams lp(int w, int h) {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(w, h);
        p.setMargins(0, 4, 0, 4);
        return p;
    }

    private LinearLayout.LayoutParams lpW(int h, float weight) {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, h == 0 ? LinearLayout.LayoutParams.WRAP_CONTENT : h, weight);
        p.setMargins(2, 4, 2, 4);
        return p;
    }

    private void updateSoraAyaLabel() {
        String name = (soraNum >= 1 && soraNum <= 114) ? SORA_NAMES[soraNum - 1] : "سورة " + soraNum;
        tvSoraAya.setText("سورة " + name + " — الآية " + ayaNum);
    }

    private int getAyaCount() {
        if (soraNum >= 1 && soraNum <= AYA_COUNTS.length) return AYA_COUNTS[soraNum - 1];
        return 7;
    }

    private void refreshMurajaText() {
        if (currentMode == MODE_MURAJA && tvAyaRef != null) {
            String ref = matcher.getAyaText(soraNum, ayaNum, isHafs);
            tvAyaRef.setText(ref.isEmpty() ? "( نص الآية غير متوفر )" : ref);
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    //  Reconnaissance vocale
    // ══════════════════════════════════════════════════════════════════════
    private void startListening(final int mode) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.RECORD_AUDIO}, REQ_AUDIO_PERM);
            return;
        }
        if (speechRecognizer != null) { speechRecognizer.destroy(); }
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ar-SA");
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "ar-SA");
        intent.putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, true);
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);

        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override public void onReadyForSpeech(Bundle params) {
                Toast.makeText(TilawaCorrectionActivity.this, "🎤 اقرأ الآن...", Toast.LENGTH_SHORT).show();
            }
            @Override public void onResults(Bundle results) {
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) {
                    onRecognitionResult(matches.get(0), mode);
                }
            }
            @Override public void onError(int error) {
                Toast.makeText(TilawaCorrectionActivity.this,
                    "خطأ في التعرف على الصوت: " + error, Toast.LENGTH_SHORT).show();
            }
            @Override public void onBeginningOfSpeech() {}
            @Override public void onRmsChanged(float rmsdB) {}
            @Override public void onBufferReceived(byte[] buffer) {}
            @Override public void onEndOfSpeech() {}
            @Override public void onPartialResults(Bundle partialResults) {}
            @Override public void onEvent(int eventType, Bundle params) {}
        });

        speechRecognizer.startListening(intent);
    }

    private void onRecognitionResult(String recognized, int mode) {
        if (mode == MODE_IMLA) {
            tvRecognized.setText(recognized);
            btnAnalyze.setVisibility(View.VISIBLE);
            // Auto-analyse
            showTajwidRules(recognized, llRules, tvScore, null);
        } else {
            // Murâja'a : comparer avec référence
            String refText = matcher.getAyaText(soraNum, ayaNum, isHafs);
            showComparison(recognized, refText);
            showTajwidRules(recognized, llRulesMuraja, tvScoreMuraja, refText);
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    //  Comparaison mot par mot (Murâja'a)
    // ══════════════════════════════════════════════════════════════════════
    private void showComparison(String recognized, String reference) {
        if (reference == null || reference.isEmpty()) {
            tvComparison.setText("( نص الآية غير متوفر )");
            return;
        }
        String[] refWords = reference.trim().split("\\s+");
        String[] recWords = recognized.trim().split("\\s+");

        SpannableString span = new SpannableString(reference);
        int pos = 0;
        for (int i = 0; i < refWords.length; i++) {
            String rw = refWords[i];
            int start = reference.indexOf(rw, pos);
            if (start < 0) { pos += rw.length(); continue; }
            int end = start + rw.length();

            int color;
            if (i < recWords.length) {
                int dist = TajwidAnalyzer.levenshtein(
                    TajwidAnalyzer.normalize(recWords[i]),
                    TajwidAnalyzer.normalize(rw));
                color = (dist == 0) ? Color.parseColor("#2E7D32")   // vert = correct
                      : (dist <= 2) ? Color.parseColor("#E65100")   // orange = proche
                                    : Color.parseColor("#B71C1C");   // rouge = faux
            } else {
                color = Color.parseColor("#B71C1C"); // rouge = manquant
            }
            span.setSpan(new ForegroundColorSpan(color), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            pos = end;
        }
        tvComparison.setText(span);
    }

    // ══════════════════════════════════════════════════════════════════════
    //  Affichage des règles Tajwid
    // ══════════════════════════════════════════════════════════════════════
    private void showTajwidRules(String text, LinearLayout container,
                                  TextView tvScoreView, String reference) {
        container.removeAllViews();

        // Score
        if (reference != null && !reference.isEmpty()) {
            String[] rw = reference.trim().split("\\s+");
            String[] tw = text.trim().split("\\s+");
            int score = TajwidAnalyzer.calculateScore(tw, rw);
            tvScoreView.setText("النتيجة: " + score + " / 100");
            tvScoreView.setTextColor(score >= 80 ? Color.parseColor("#2E7D32")
                                   : score >= 50 ? Color.parseColor("#E65100")
                                                 : Color.parseColor("#B71C1C"));
        } else {
            // Imlâ' : pas de référence, on montre juste les règles
            tvScoreView.setText("");
        }

        // Règles Tajwid
        List<TajwidRule> rules = analyzer.analyze(text);
        if (rules.isEmpty()) {
            addRuleCard(container, "✅", "لم يُكتشف أي خطأ تجويدي", "", TajwidRule.SEVERITY_OK);
            return;
        }
        for (TajwidRule rule : rules) {
            addRuleCard(container,
                rule.getSeverityIcon(),
                rule.getRuleNameAr() + " — " + rule.getRuleNameFr(),
                rule.getDescription(),
                rule.getSeverity());
        }
    }

    private void addRuleCard(LinearLayout container, String icon, String title,
                              String desc, int severity) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(12, 8, 12, 8);
        int bg = (severity == TajwidRule.SEVERITY_ERROR)   ? Color.parseColor("#FFEBEE")
               : (severity == TajwidRule.SEVERITY_WARNING) ? Color.parseColor("#FFF8E1")
                                                           : Color.parseColor("#E8F5E9");
        card.setBackgroundColor(bg);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 4, 0, 4);
        card.setLayoutParams(lp);

        TextView tvTitle = new TextView(this);
        tvTitle.setText(icon + "  " + title);
        tvTitle.setTextSize(14);
        tvTitle.setTextColor(Color.parseColor("#212121"));
        card.addView(tvTitle);

        if (desc != null && !desc.isEmpty()) {
            TextView tvDesc = new TextView(this);
            tvDesc.setText(desc);
            tvDesc.setTextSize(12);
            tvDesc.setTextColor(Color.parseColor("#555555"));
            card.addView(tvDesc);
        }
        container.addView(card);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
            String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_AUDIO_PERM
                && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "إذن المايكروفون ممنوح. اضغط مجدداً.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (speechRecognizer != null) { speechRecognizer.destroy(); }
    }
}