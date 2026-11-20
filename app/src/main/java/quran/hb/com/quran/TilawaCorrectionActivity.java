package quran.hb.com.quran;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StrikethroughSpan;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TilawaCorrectionActivity extends Activity {

    // ── constants ─────────────────────────────────────────────────────────────
    private static final int REQ_SPEECH       = 101;
    private static final int REQ_AUDIO_PERM   = 200;

    private static final String[] SORA_NAMES = {
        "الفاتحة","البقرة","آل عمران","النساء","المائدة","الأنعام","الأعراف",
        "الأنفال","التوبة","يونس","هود","يوسف","الرعد","إبراهيم","الحجر",
        "النحل","الإسراء","الكهف","مريم","طه","الأنبياء","الحج","المؤمنون",
        "النور","الفرقان","الشعراء","النمل","القصص","العنكبوت","الروم","لقمان",
        "السجدة","الأحزاب","سبأ","فاطر","يس","الصافات","ص","الزمر","غافر",
        "فصلت","الشورى","الزخرف","الدخان","الجاثية","الأحقاف","محمد","الفتح",
        "الحجرات","ق","الذاريات","الطور","النجم","القمر","الرحمن","الواقعة",
        "الحديد","المجادلة","الحشر","الممتحنة","الصف","الجمعة","المنافقون",
        "التغابن","الطلاق","التحريم","الملك","القلم","الحاقة","المعارج","نوح",
        "الجن","المزمل","المدثر","القيامة","الإنسان","المرسلات","النبأ",
        "النازعات","عبس","التكوير","الانفطار","المطففين","الانشقاق","البروج",
        "الطارق","الأعلى","الغاشية","الفجر","البلد","الشمس","الليل","الضحى",
        "الشرح","التين","العلق","القدر","البينة","الزلزلة","العاديات","القارعة",
        "التكاثر","العصر","الهمزة","الفيل","قريش","الماعون","الكوثر","الكافرون",
        "النصر","المسد","الإخلاص","الفلق","الناس"
    };

    private static final int[] SORA_AYA_COUNT = {
        7,286,200,176,120,165,206,75,129,109,123,111,43,52,99,
        128,111,110,98,135,112,78,118,64,77,227,93,88,69,60,34,
        30,73,54,45,83,182,88,75,85,54,53,89,59,37,35,38,29,18,
        45,60,52,62,55,78,96,29,22,24,13,14,11,11,18,12,12,30,52,
        52,44,28,28,20,56,40,31,50,45,33,27,44,26,24,47,33,30,24,
        23,22,17,11,22,22,33,36,23,13,13,11,10,10,8,6,6,7,19,18,
        12,11,10,10,8,6,5,6,5,8,10,11,12,10,7,5,4,3
    };

    // ── UI ────────────────────────────────────────────────────────────────────
    private RadioGroup  rgMode;
    private RadioButton rbImla, rbMuraja;

    // Mode Imlâ'
    private LinearLayout layoutImla;
    private Spinner      spinnerSora, spinnerAya;
    private TextView     tvSoraAya;
    private Button       btnRiwaya, btnRecord, btnAnalyze;
    private TextView     tvRecognized, tvScore;
    private LinearLayout llRules;
    private ScrollView   scrollImla;

    // Mode Murâja'a
    private LinearLayout layoutMuraja;
    private TextView     tvRefText, tvComparison;
    private Button       btnRecordMuraja;
    private LinearLayout llRulesMuraja;
    private ScrollView   scrollMuraja;

    // ── State ─────────────────────────────────────────────────────────────────
    private DbHelper   dbHelper;
    private SQLiteDatabase db;

    private int     currentMode  = 0;   // 0=Imlâ', 1=Murâja'a
    private boolean isHafs       = true;
    private int     selectedSora = 1;
    private int     selectedAya  = 1;

    // Murâja'a: from Intent
    private int     mSoraNum, mAyaNum, mPageNum;

    private String  recognizedText  = "";
    private String  referenceText   = "";

    // ── Lifecycle ─────────────────────────────────────────────────────────────
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Read Intent extras (Murâja'a mode)
        Intent intent = getIntent();
        mSoraNum  = intent.getIntExtra("soraNum",  1);
        mAyaNum   = intent.getIntExtra("ayaNum",   1);
        mPageNum  = intent.getIntExtra("pageNum",  1);
        isHafs    = intent.getBooleanExtra("isHafs", true);

        try {
            dbHelper = new DbHelper(this);
            dbHelper.createdatabase();
        } catch (IOException e) {
            e.printStackTrace();
        }
        db = SQLiteDatabase.openDatabase(
                "/data/data/quran.hb.com.quran/databases/db_quran.sqlite",
                null, SQLiteDatabase.OPEN_READONLY);

        buildUI();
        setupListeners();

        // Start in Murâja'a if data passed from QMain
        if (mSoraNum > 0 && mAyaNum > 0) {
            rbMuraja.setChecked(true);
            switchMode(1);
            loadRefText();
        } else {
            rbImla.setChecked(true);
            switchMode(0);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (db != null && db.isOpen()) db.close();
    }

    // ── UI construction ───────────────────────────────────────────────────────
    private void buildUI() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.parseColor("#1a0a0a"));
        root.setPadding(0, 0, 0, 0);
        setContentView(root);

        // ── Top bar ──────────────────────────────────────────────────────────
        LinearLayout topBar = new LinearLayout(this);
        topBar.setOrientation(LinearLayout.HORIZONTAL);
        topBar.setBackgroundColor(Color.parseColor("#1d0606"));
        topBar.setPadding(8, 8, 8, 8);

        Button btnBack = new Button(this);
        btnBack.setText("←");
        btnBack.setTextColor(Color.WHITE);
        btnBack.setBackgroundColor(Color.parseColor("#555555"));
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                finish();
                startActivity(new Intent(TilawaCorrectionActivity.this, QMain.class));
            }
        });
        topBar.addView(btnBack, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        TextView tvTitle = new TextView(this);
        tvTitle.setText("تقييم التلاوة");
        tvTitle.setTextColor(Color.parseColor("#FFF4CB"));
        tvTitle.setTextSize(18);
        tvTitle.setTypeface(null, Typeface.BOLD);
        tvTitle.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        topBar.addView(tvTitle, titleParams);

        root.addView(topBar, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        // ── Mode selector ────────────────────────────────────────────────────
        rgMode  = new RadioGroup(this);
        rgMode.setOrientation(RadioGroup.HORIZONTAL);
        rgMode.setBackgroundColor(Color.parseColor("#2a0f0f"));
        rgMode.setGravity(Gravity.CENTER);
        rgMode.setPadding(8, 4, 8, 4);

        rbImla   = new RadioButton(this);
        rbImla.setText("إملاء");
        rbImla.setTextColor(Color.WHITE);
        rbImla.setId(View.generateViewId());

        rbMuraja = new RadioButton(this);
        rbMuraja.setText("مراجعة");
        rbMuraja.setTextColor(Color.WHITE);
        rbMuraja.setId(View.generateViewId());

        rgMode.addView(rbMuraja);
        rgMode.addView(rbImla);
        root.addView(rgMode, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        // ── Mode Imlâ' panel ─────────────────────────────────────────────────
        scrollImla = new ScrollView(this);
        layoutImla = new LinearLayout(this);
        layoutImla.setOrientation(LinearLayout.VERTICAL);
        layoutImla.setPadding(16, 16, 16, 16);

        // Sora / Aya spinners row
        LinearLayout rowSpinners = new LinearLayout(this);
        rowSpinners.setOrientation(LinearLayout.HORIZONTAL);
        rowSpinners.setGravity(Gravity.CENTER_VERTICAL);

        TextView lblSora = new TextView(this);
        lblSora.setText("السورة: ");
        lblSora.setTextColor(Color.parseColor("#FFF4CB"));
        rowSpinners.addView(lblSora);

        spinnerSora = new Spinner(this);
        spinnerSora.setBackgroundColor(Color.parseColor("#2a0f0f"));
        List<String> soraList = new ArrayList<String>();
        for (int i = 0; i < SORA_NAMES.length; i++) {
            soraList.add((i + 1) + ". " + SORA_NAMES[i]);
        }
        ArrayAdapter<String> soraAdapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item, soraList);
        soraAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSora.setAdapter(soraAdapter);
        rowSpinners.addView(spinnerSora, new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        TextView lblAya = new TextView(this);
        lblAya.setText("  الآية: ");
        lblAya.setTextColor(Color.parseColor("#FFF4CB"));
        rowSpinners.addView(lblAya);

        spinnerAya = new Spinner(this);
        spinnerAya.setBackgroundColor(Color.parseColor("#2a0f0f"));
        rowSpinners.addView(spinnerAya, new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 0.5f));

        layoutImla.addView(rowSpinners, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        // Sora/Aya title display
        tvSoraAya = new TextView(this);
        tvSoraAya.setText("سورة الفاتحة - الآية 1");
        tvSoraAya.setTextColor(Color.WHITE);
        tvSoraAya.setTextSize(22);
        tvSoraAya.setTypeface(null, Typeface.BOLD);
        tvSoraAya.setGravity(Gravity.CENTER);
        tvSoraAya.setPadding(0, 12, 0, 12);
        layoutImla.addView(tvSoraAya, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        // Riwaya toggle
        btnRiwaya = new Button(this);
        btnRiwaya.setText("رواية: حفص");
        btnRiwaya.setBackgroundColor(Color.parseColor("#227e08"));
        btnRiwaya.setTextColor(Color.WHITE);
        layoutImla.addView(btnRiwaya, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        // Record button
        btnRecord = new Button(this);
        btnRecord.setText("🎤  تسجيل");
        btnRecord.setBackgroundColor(Color.parseColor("#c0392b"));
        btnRecord.setTextColor(Color.WHITE);
        btnRecord.setTextSize(16);
        layoutImla.addView(btnRecord, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        // Recognized text
        tvRecognized = new TextView(this);
        tvRecognized.setText("");
        tvRecognized.setHint("النص المُعَرَّف بالصوت سيظهر هنا...");
        tvRecognized.setTextColor(Color.WHITE);
        tvRecognized.setBackgroundColor(Color.parseColor("#0d0d0d"));
        tvRecognized.setPadding(8, 8, 8, 8);
        tvRecognized.setTextSize(18);
        tvRecognized.setGravity(Gravity.RIGHT);
        layoutImla.addView(tvRecognized, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        // Analyze button
        btnAnalyze = new Button(this);
        btnAnalyze.setText("تصحيح وتحليل التجويد");
        btnAnalyze.setBackgroundColor(Color.parseColor("#1a5276"));
        btnAnalyze.setTextColor(Color.WHITE);
        layoutImla.addView(btnAnalyze, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        // Score
        tvScore = new TextView(this);
        tvScore.setText("");
        tvScore.setTextColor(Color.parseColor("#f1c40f"));
        tvScore.setTextSize(20);
        tvScore.setTypeface(null, Typeface.BOLD);
        tvScore.setGravity(Gravity.CENTER);
        layoutImla.addView(tvScore, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        // Rules container
        llRules = new LinearLayout(this);
        llRules.setOrientation(LinearLayout.VERTICAL);
        layoutImla.addView(llRules, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        scrollImla.addView(layoutImla);
        root.addView(scrollImla, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        // ── Mode Murâja'a panel ───────────────────────────────────────────────
        scrollMuraja = new ScrollView(this);
        layoutMuraja = new LinearLayout(this);
        layoutMuraja.setOrientation(LinearLayout.VERTICAL);
        layoutMuraja.setPadding(16, 16, 16, 16);

        // Reference text display
        tvRefText = new TextView(this);
        tvRefText.setText("");
        tvRefText.setTextColor(Color.parseColor("#41e70e"));
        tvRefText.setBackgroundColor(Color.BLACK);
        tvRefText.setTextSize(22);
        tvRefText.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        tvRefText.setPadding(8, 12, 8, 12);
        layoutMuraja.addView(tvRefText, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        // Record button (Murâja'a)
        btnRecordMuraja = new Button(this);
        btnRecordMuraja.setText("🎤  تسجيل الترتيل");
        btnRecordMuraja.setBackgroundColor(Color.parseColor("#c0392b"));
        btnRecordMuraja.setTextColor(Color.WHITE);
        btnRecordMuraja.setTextSize(16);
        layoutMuraja.addView(btnRecordMuraja, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        // Word-level comparison
        tvComparison = new TextView(this);
        tvComparison.setText("");
        tvComparison.setBackgroundColor(Color.parseColor("#0d0d0d"));
        tvComparison.setTextSize(18);
        tvComparison.setGravity(Gravity.RIGHT);
        tvComparison.setPadding(8, 8, 8, 8);
        layoutMuraja.addView(tvComparison, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        // Tajwid rules (Murâja'a)
        llRulesMuraja = new LinearLayout(this);
        llRulesMuraja.setOrientation(LinearLayout.VERTICAL);
        layoutMuraja.addView(llRulesMuraja, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        scrollMuraja.addView(layoutMuraja);
        root.addView(scrollMuraja, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        // Initially hide both; switchMode() will show the right one
        layoutImla.setVisibility(View.GONE);
        layoutMuraja.setVisibility(View.GONE);
        scrollImla.setVisibility(View.GONE);
        scrollMuraja.setVisibility(View.GONE);
    }

    // ── Listeners ─────────────────────────────────────────────────────────────
    private void setupListeners() {
        // Mode switch
        rgMode.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == rbImla.getId())   switchMode(0);
                else if (checkedId == rbMuraja.getId()) switchMode(1);
            }
        });

        // Sora spinner
        spinnerSora.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                selectedSora = pos + 1;
                updateAyaSpinner();
                updateTvSoraAya();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Aya spinner
        spinnerAya.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                selectedAya = pos + 1;
                updateTvSoraAya();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Riwaya toggle
        btnRiwaya.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                isHafs = !isHafs;
                btnRiwaya.setText("رواية: " + (isHafs ? "حفص" : "ورش"));
            }
        });

        // Record (Imlâ')
        btnRecord.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                currentMode = 0;
                launchSpeechRecognizer();
            }
        });

        // Analyze
        btnAnalyze.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                if (recognizedText.isEmpty()) {
                    Toast.makeText(TilawaCorrectionActivity.this,
                            "سجّل نصاً أولاً", Toast.LENGTH_SHORT).show();
                    return;
                }
                referenceText = QuranTextMatcher.getAyaText(db, selectedSora, selectedAya, isHafs);
                runAnalysis(llRules, recognizedText, referenceText);
            }
        });

        // Record (Murâja'a)
        btnRecordMuraja.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                currentMode = 1;
                launchSpeechRecognizer();
            }
        });
    }

    // ── Mode switching ────────────────────────────────────────────────────────
    private void switchMode(int mode) {
        currentMode = mode;
        if (mode == 0) {
            scrollImla.setVisibility(View.VISIBLE);
            scrollMuraja.setVisibility(View.GONE);
            updateAyaSpinner();
        } else {
            scrollImla.setVisibility(View.GONE);
            scrollMuraja.setVisibility(View.VISIBLE);
            loadRefText();
        }
    }

    // ── Spinners ──────────────────────────────────────────────────────────────
    private void updateAyaSpinner() {
        int count = (selectedSora >= 1 && selectedSora <= 114)
                ? SORA_AYA_COUNT[selectedSora - 1] : 1;
        List<String> ayaList = new ArrayList<String>();
        for (int i = 1; i <= count; i++) ayaList.add(String.valueOf(i));
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item, ayaList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAya.setAdapter(adapter);
    }

    private void updateTvSoraAya() {
        String name = (selectedSora >= 1 && selectedSora <= 114)
                ? SORA_NAMES[selectedSora - 1] : "?";
        tvSoraAya.setText("سورة " + name + " - الآية " + selectedAya);
    }

    // ── Reference text from DB ────────────────────────────────────────────────
    private void loadRefText() {
        if (db == null || !db.isOpen()) return;
        referenceText = QuranTextMatcher.getAyaText(db, mSoraNum, mAyaNum, isHafs);
        if (referenceText == null || referenceText.isEmpty()) {
            referenceText = "";
            tvRefText.setText("لم يُعثر على النص");
        } else {
            tvRefText.setText(referenceText);
        }
    }

    // ── Speech recognizer ─────────────────────────────────────────────────────
    private void launchSpeechRecognizer() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.RECORD_AUDIO}, REQ_AUDIO_PERM);
                return;
            }
        }
        Intent speechIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ar-SA");
        speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
        speechIntent.putExtra(RecognizerIntent.EXTRA_PROMPT, "اتلُ الآية بصوتٍ واضح");
        try {
            startActivityForResult(speechIntent, REQ_SPEECH);
        } catch (Exception e) {
            Toast.makeText(this, "التعرف على الكلام غير متاح", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
            String[] permissions, int[] grantResults) {
        if (requestCode == REQ_AUDIO_PERM &&
                grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            launchSpeechRecognizer();
        } else {
            Toast.makeText(this, "يجب السماح بالوصول إلى الميكروفون", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_SPEECH && resultCode == RESULT_OK && data != null) {
            ArrayList<String> results =
                    data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (results != null && !results.isEmpty()) {
                recognizedText = results.get(0);
                if (currentMode == 0) {
                    // Imlâ': show recognized text
                    tvRecognized.setText(recognizedText);
                } else {
                    // Murâja'a: compare and analyze
                    compareMuraja(recognizedText, referenceText);
                    runAnalysis(llRulesMuraja, recognizedText, referenceText);
                }
            }
        }
    }

    // ── Word comparison (Murâja'a) ────────────────────────────────────────────
    /**
     * Compares recognized words against reference words.
     * Correct → green, Missing from reference → red strikethrough,
     * Extra (added) → orange.
     */
    private void compareMuraja(String recognized, String reference) {
        if (reference == null || reference.isEmpty()) {
            tvComparison.setText(recognized);
            return;
        }
        String[] refWords = normalizeArabic(reference).split("\\s+");
        String[] recWords = normalizeArabic(recognized).split("\\s+");

        // Raw (non-normalized) words for display
        String[] refDisplay = reference.split("\\s+");
        String[] recDisplay = recognized.split("\\s+");

        SpannableStringBuilder sb = new SpannableStringBuilder();

        int ri = 0, reci = 0;
        while (ri < refWords.length || reci < recWords.length) {
            if (ri < refWords.length && reci < recWords.length &&
                    refWords[ri].equals(recWords[reci])) {
                // Correct word
                appendColored(sb,
                        (ri < refDisplay.length ? refDisplay[ri] : refWords[ri]) + " ",
                        Color.GREEN);
                ri++;
                reci++;
            } else if (reci < recWords.length &&
                    (ri >= refWords.length || !refWords[ri].equals(recWords[reci]))) {
                // Extra (added) word
                appendColored(sb,
                        (reci < recDisplay.length ? recDisplay[reci] : recWords[reci]) + " ",
                        Color.parseColor("#e67e22"));
                reci++;
            } else {
                // Missing word (in reference but not in recognized)
                int start = sb.length();
                sb.append((ri < refDisplay.length ? refDisplay[ri] : refWords[ri]) + " ");
                sb.setSpan(new ForegroundColorSpan(Color.RED),
                        start, sb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                sb.setSpan(new StrikethroughSpan(),
                        start, sb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                ri++;
            }
        }
        tvComparison.setText(sb);
    }

    private void appendColored(SpannableStringBuilder sb, String text, int color) {
        int start = sb.length();
        sb.append(text);
        sb.setSpan(new ForegroundColorSpan(color),
                start, sb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    /** Strips Arabic diacritics for comparison purposes. */
    private String normalizeArabic(String text) {
        if (text == null) return "";
        // Remove harakat and tatweel
        return text.replaceAll("[\u064B-\u065F\u0670\u06D6-\u06DC\u06DF-\u06E4\u06E7\u06E8\u06EA-\u06ED\u0640]", "")
                   .trim();
    }

    // ── Tajwid analysis ───────────────────────────────────────────────────────
    private void runAnalysis(LinearLayout container, String recText, String refText) {
        TajwidAnalyzer analyzer = new TajwidAnalyzer();
        List<TajwidRule> rules = analyzer.analyze(recText, refText, isHafs);
        int score = analyzer.calculateScore(rules);

        if (container == llRules) {
            tvScore.setText("النتيجة: " + score + " / 10");
        }
        container.removeAllViews();

        for (TajwidRule rule : rules) {
            container.addView(buildRuleCard(rule));
        }

        if (rules.isEmpty()) {
            TextView tv = new TextView(this);
            tv.setText("✅ لم تُرصد أخطاء تجويدية");
            tv.setTextColor(Color.GREEN);
            tv.setPadding(8, 4, 8, 4);
            container.addView(tv);
        }
    }

    private View buildRuleCard(TajwidRule rule) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(12, 8, 12, 8);

        // Background per severity
        int bgColor;
        String icon;
        switch (rule.getSeverity()) {
            case TajwidRule.SEVERITY_ERROR:
                bgColor = Color.parseColor("#4d1a1a");
                icon = "❌";
                break;
            case TajwidRule.SEVERITY_WARNING:
                bgColor = Color.parseColor("#4d3a1a");
                icon = "⚠️";
                break;
            default:
                bgColor = Color.parseColor("#1a3a1a");
                icon = "✅";
                break;
        }
        card.setBackgroundColor(bgColor);

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        cardParams.setMargins(0, 4, 0, 4);
        card.setLayoutParams(cardParams);

        TextView tvRule = new TextView(this);
        tvRule.setText(icon + "  " + rule.getRuleNameAr() + "  (" + rule.getRuleNameFr() + ")");
        tvRule.setTextColor(Color.WHITE);
        tvRule.setTypeface(null, Typeface.BOLD);
        card.addView(tvRule);

        if (rule.getDescription() != null && !rule.getDescription().isEmpty()) {
            TextView tvDesc = new TextView(this);
            tvDesc.setText(rule.getDescription());
            tvDesc.setTextColor(Color.parseColor("#cccccc"));
            tvDesc.setTextSize(13);
            card.addView(tvDesc);
        }

        if (rule.getWrongText() != null && !rule.getWrongText().isEmpty()) {
            TextView tvWrong = new TextView(this);
            tvWrong.setText("❌ " + rule.getWrongText() + "  →  ✔ " + rule.getCorrectText());
            tvWrong.setTextColor(Color.parseColor("#e74c3c"));
            card.addView(tvWrong);
        }

        return card;
    }

    // ── Back key ──────────────────────────────────────────────────────────────
    @Override
    public void onBackPressed() {
        finish();
        startActivity(new Intent(this, QMain.class));
    }
}
