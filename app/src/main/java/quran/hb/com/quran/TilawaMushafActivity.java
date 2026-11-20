package quran.hb.com.quran;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.text.SpannableString;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class TilawaMushafActivity extends Activity {

    public static final int MODE_TILAWA = 0;
    public static final int MODE_IMLA   = 1;

    private static final int COLOR_PENDING = 0xFF333333;
    private static final int COLOR_CORRECT = 0xFF2E7D32;
    private static final int COLOR_WRONG   = 0xFFB71C1C;
    private static final int COLOR_CURRENT = 0xFFF57F17;

    private int currentPage;
    private int soraNum;
    private int ayaNum;
    private boolean isHafs;
    private int currentMode;

    private ImageView ivPage;
    private Button btnModeTilawa;
    private Button btnModeImla;
    private Button btnRiwaya;
    private Button btnRecord;
    private Button btnPrevAya;
    private Button btnNextAya;
    private TextView tvAyaInfo;
    private TextView tvFeedback;
    private LinearLayout llWords;

    private DbHelper db;
    private QuranTextMatcher matcher;
    private SpeechRecognizer speechRecognizer;

    private String[] currentWords;
    private int recognizedWordIndex;
    private boolean isListening = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tilawa_mushaf);

        // Récupérer les extras
        soraNum = getIntent().getIntExtra("soraNum", 1);
        ayaNum  = getIntent().getIntExtra("ayaNum", 1);
        isHafs  = getIntent().getIntExtra("isHafs", 1) == 1;

        // Initialiser la base de donnees
        try {
            db = new DbHelper(this);
            db.createdatabase();
            db.opendatabase();
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }

        // Initialiser le matcher
        matcher = new QuranTextMatcher(this);

        // Trouver la page de l'aya
        currentPage = matcher.getAyaPage(soraNum, ayaNum);

        // Lier les vues
        ivPage        = (ImageView) findViewById(R.id.ivPage);
        tvAyaInfo     = (TextView)  findViewById(R.id.tvAyaInfo);
        tvFeedback    = (TextView)  findViewById(R.id.tvFeedback);
        llWords       = (LinearLayout) findViewById(R.id.llWords);
        btnModeTilawa = (Button) findViewById(R.id.btnModeTilawa);
        btnModeImla   = (Button) findViewById(R.id.btnModeImla);
        btnRiwaya     = (Button) findViewById(R.id.btnRiwaya);
        btnRecord     = (Button) findViewById(R.id.btnRecord);
        btnPrevAya    = (Button) findViewById(R.id.btnPrevAya);
        btnNextAya    = (Button) findViewById(R.id.btnNextAya);

        // Chargement initial
        currentMode = MODE_TILAWA;
        loadPage();
        loadAyaWords();
        updateAyaInfo();

        // --- Listeners ---
        btnModeTilawa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setMode(MODE_TILAWA);
            }
        });

        btnModeImla.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TilawaMushafActivity.this, TilawaImlaActivity.class);
                intent.putExtra("soraNum", soraNum);
                intent.putExtra("ayaNum",  ayaNum);
                intent.putExtra("isHafs",  isHafs ? 1 : 0);
                startActivity(intent);
            }
        });

        btnRiwaya.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isHafs = !isHafs;
                btnRiwaya.setText(isHafs ? "حفص" : "ورش");
                loadPage();
                loadAyaWords();
            }
        });

        btnRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isListening) {
                    stopListening();
                } else {
                    startListening();
                }
            }
        });

        btnPrevAya.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prevAya();
            }
        });

        btnNextAya.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nextAya();
            }
        });
    }

    // -------------------------------------------------------------------------
    // Mode
    // -------------------------------------------------------------------------

    private void setMode(int mode) {
        currentMode = mode;
        if (mode == MODE_TILAWA) {
            ivPage.setVisibility(View.VISIBLE);
            llWords.setVisibility(View.VISIBLE);
        }
    }

    // -------------------------------------------------------------------------
    // Chargement de la page Mushaf
    // -------------------------------------------------------------------------

    private void loadPage() {
        String dir  = isHafs ? "/QuranHW/HI/" : "/QuranHW/WI/";
        String ext  = isHafs ? ".jpg" : ".png";
        String path = Environment.getExternalStorageDirectory() + dir + "p" + currentPage + ext;
        Bitmap bmp  = BitmapFactory.decodeFile(path);
        if (bmp != null) {
            ivPage.setImageBitmap(bmp);
        }
    }

    // -------------------------------------------------------------------------
    // Chargement des mots de l'aya
    // -------------------------------------------------------------------------

    private void loadAyaWords() {
        String ayaText = matcher.getAyaText(soraNum, ayaNum, isHafs);
        if (ayaText == null || ayaText.trim().isEmpty()) {
            ayaText = "";
        }
        currentWords = ayaText.split("\\s+");
        recognizedWordIndex = -1;

        llWords.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);
        for (int i = 0; i < currentWords.length; i++) {
            if (currentWords[i].isEmpty()) continue;
            View wordView = inflater.inflate(R.layout.item_tilawa_word, llWords, false);
            TextView tv  = (TextView) wordView.findViewById(R.id.tvWord);
            tv.setText(currentWords[i]);
            tv.setBackgroundColor(COLOR_PENDING);
            tv.setTextColor(0xFFFFFFFF);
            llWords.addView(wordView);
        }
    }

    // -------------------------------------------------------------------------
    // Info aya
    // -------------------------------------------------------------------------

    private void updateAyaInfo() {
        String soraName = matcher.getSoraName(soraNum);
        tvAyaInfo.setText(soraName + " : " + ayaNum);
    }

    // -------------------------------------------------------------------------
    // Reconnaissance vocale
    // -------------------------------------------------------------------------

    private void startListening() {
        if (speechRecognizer == null) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        }
        Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ar-SA");
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        i.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
        speechRecognizer.setRecognitionListener(recognitionListener);
        speechRecognizer.startListening(i);
        isListening = true;
        btnRecord.setText("\u23F9\uFE0F");
        btnRecord.setBackgroundColor(0xFFB71C1C);
    }

    private void stopListening() {
        if (speechRecognizer != null) {
            speechRecognizer.stopListening();
        }
        isListening = false;
        resetRecordButton();
    }

    private void resetRecordButton() {
        btnRecord.setText("\uD83C\uDF99\uFE0F");
        btnRecord.setBackgroundColor(0xFF2E7D32);
        isListening = false;
    }

    private final RecognitionListener recognitionListener = new RecognitionListener() {

        @Override
        public void onReadyForSpeech(Bundle params) {}

        @Override
        public void onBeginningOfSpeech() {}

        @Override
        public void onRmsChanged(float rmsdB) {}

        @Override
        public void onBufferReceived(byte[] buffer) {}

        @Override
        public void onEndOfSpeech() {}

        @Override
        public void onError(int error) {
            resetRecordButton();
            String msg;
            switch (error) {
                case SpeechRecognizer.ERROR_AUDIO:            msg = "خطأ في الصوت";           break;
                case SpeechRecognizer.ERROR_CLIENT:           msg = "خطأ في التطبيق";         break;
                case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS: msg = "لا توجد صلاحية"; break;
                case SpeechRecognizer.ERROR_NETWORK:          msg = "خطأ في الشبكة";          break;
                case SpeechRecognizer.ERROR_NO_MATCH:         msg = "لم يُتعرف على الكلام";   break;
                case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:  msg = "المحرك مشغول";           break;
                case SpeechRecognizer.ERROR_SERVER:           msg = "خطأ في الخادم";          break;
                case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:   msg = "انتهت مهلة الكلام";      break;
                default:                                       msg = "خطأ غير معروف: " + error;
            }
            Toast.makeText(TilawaMushafActivity.this, msg, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onResults(Bundle results) {
            processFinalResults(results);
            resetRecordButton();
        }

        @Override
        public void onPartialResults(Bundle partialResults) {
            processPartialResults(partialResults);
        }

        @Override
        public void onEvent(int eventType, Bundle params) {}
    };

    // -------------------------------------------------------------------------
    // Traitement des résultats partiels
    // -------------------------------------------------------------------------

    private void processPartialResults(Bundle results) {
        if (results == null) return;
        ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        if (matches == null || matches.isEmpty()) return;

        String hypothesis = matches.get(0);
        String[] spokenWords = hypothesis.trim().split("\\s+");

        int limit = Math.min(spokenWords.length, currentWords.length);
        for (int i = 0; i < limit; i++) {
            String spoken    = QuranTextMatcher.normalize(spokenWords[i]);
            String reference = QuranTextMatcher.normalize(currentWords[i]);
            int dist = TajwidAnalyzer.levenshtein(spoken, reference);

            TextView tv = getWordTextView(i);
            if (tv != null) {
                if (dist <= 1) {
                    tv.setBackgroundColor(COLOR_CORRECT);
                } else {
                    tv.setBackgroundColor(COLOR_WRONG);
                }
            }
            recognizedWordIndex = i;
        }

        // Surligner le prochain mot en attente
        if (limit < currentWords.length) {
            TextView tv = getWordTextView(limit);
            if (tv != null) {
                tv.setBackgroundColor(COLOR_CURRENT);
            }
        }
    }

    // -------------------------------------------------------------------------
    // Traitement des résultats finaux
    // -------------------------------------------------------------------------

    private void processFinalResults(Bundle results) {
        if (results == null) return;
        ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        if (matches == null || matches.isEmpty()) return;

        String recognized = matches.get(0);
        String[] spokenWords = recognized.trim().split("\\s+");

        // Colorier tous les mots avec le résultat final
        int correct = 0;
        int total   = currentWords.length;
        int limit   = Math.min(spokenWords.length, total);

        for (int i = 0; i < limit; i++) {
            String spoken    = QuranTextMatcher.normalize(spokenWords[i]);
            String reference = QuranTextMatcher.normalize(currentWords[i]);
            int dist = TajwidAnalyzer.levenshtein(spoken, reference);
            TextView tv = getWordTextView(i);
            if (tv != null) {
                if (dist <= 1) {
                    tv.setBackgroundColor(COLOR_CORRECT);
                    correct++;
                } else {
                    tv.setBackgroundColor(COLOR_WRONG);
                }
            }
        }
        // Mots non prononcés → rouge
        for (int i = limit; i < total; i++) {
            TextView tv = getWordTextView(i);
            if (tv != null) tv.setBackgroundColor(COLOR_WRONG);
        }

        // Score
        int score = (total > 0) ? (correct * 100 / total) : 0;

        // Analyse Tajwid
        String ayaText = matcher.getAyaText(soraNum, ayaNum, isHafs);
        List<TajwidRule> rules = new TajwidAnalyzer().analyze(ayaText);

        // Construire le texte de feedback
        StringBuilder sb = new StringBuilder();
        sb.append("النتيجة : ").append(score).append("%\n");
        sb.append("الكلمات الصحيحة : ").append(correct).append(" / ").append(total).append("\n\n");

        if (rules != null && !rules.isEmpty()) {
            sb.append("ملاحظات التجويد :\n");
            for (TajwidRule rule : rules) {
                String icon = rule.getSeverity() == 0 ? "✅" : (rule.getSeverity() == 1 ? "⚠️" : "❌");
                sb.append(icon).append(" ").append(rule.getRuleNameAr());
                if (rule.getDescription() != null && !rule.getDescription().isEmpty()) {
                    sb.append(" — ").append(rule.getDescription());
                }
                sb.append("\n");
            }
        }

        tvFeedback.setText(sb.toString());
        tvFeedback.setVisibility(View.VISIBLE);

        // Si score >= 80, proposer de passer à l'aya suivante
        if (score >= 80) {
            new AlertDialog.Builder(this)
                    .setTitle("أحسنت!")
                    .setMessage("النتيجة ممتازة. الانتقال إلى الآية التالية؟")
                    .setPositiveButton("نعم", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            nextAya();
                        }
                    })
                    .setNegativeButton("لا", null)
                    .show();
        }
    }

    // -------------------------------------------------------------------------
    // Helper : obtenir le TextView d'un mot à l'index i
    // -------------------------------------------------------------------------

    private TextView getWordTextView(int index) {
        if (index < 0 || index >= llWords.getChildCount()) return null;
        View child = llWords.getChildAt(index);
        if (child == null) return null;
        return (TextView) child.findViewById(R.id.tvWord);
    }

    // -------------------------------------------------------------------------
    // Navigation entre ayas
    // -------------------------------------------------------------------------

    private void nextAya() {
        int max = matcher.getAyaCount(soraNum, isHafs);
        if (ayaNum < max) {
            ayaNum++;
        } else {
            soraNum = Math.min(114, soraNum + 1);
            ayaNum  = 1;
        }
        currentPage = matcher.getAyaPage(soraNum, ayaNum);
        loadPage();
        loadAyaWords();
        updateAyaInfo();
    }

    private void prevAya() {
        if (ayaNum > 1) {
            ayaNum--;
        } else {
            soraNum = Math.max(1, soraNum - 1);
            ayaNum  = matcher.getAyaCount(soraNum, isHafs);
        }
        currentPage = matcher.getAyaPage(soraNum, ayaNum);
        loadPage();
        loadAyaWords();
        updateAyaInfo();
    }

    // -------------------------------------------------------------------------
    // Cycle de vie
    // -------------------------------------------------------------------------

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
            speechRecognizer = null;
        }
        if (db != null) {
            db.close();
            db = null;
        }
    }
}