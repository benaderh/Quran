package quran.hb.com.quran;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TilawaImlaActivity extends Activity {

    // ── Nombre de sourates ────────────────────────────────────────────────
    private static final int SORA_COUNT = 114;

    private static final int[] AYA_COUNTS = {
        7,286,200,176,120,165,206,75,129,109,123,111,43,52,99,128,111,110,98,135,
        112,78,118,64,77,227,93,88,69,60,34,30,73,54,45,83,182,88,75,85,54,53,89,
        59,37,35,38,29,18,45,60,25,78,65,30,54,71,40,52,45,35,8,24,33,30,22,13,
        43,32,24,57,19,96,15,13,25,22,11,8,78,28,92,55,45,36,25,35,38,29,106,21,
        49,33,30,22,14,13,11,4,8,65,174,13,4,5,6,19,2,12,3,5,2,4,27,1,7,2,19,5,8,2,20,109,112
    };

    // ── Champs membres ────────────────────────────────────────────────────
    private int     soraNum  = 1;
    private int     ayaNum   = 1;
    private boolean isHafs   = true;

    private String   ayaText;
    private String[] ayaWords;

    private TextView tvAyaNumber;
    private TextView tvSoraName;
    private TextView tvAyaRevealed;
    private TextView tvListening;
    private TextView tvResultImla;

    private Button btnRecordImla;
    private Button btnPrevImla;
    private Button btnNextImla;
    private Button btnBack;
    private Button btnRiwayaImla;

    private DbHelper         db;
    private QuranTextMatcher matcher;
    private SpeechRecognizer speechRecognizer;

    private boolean isRecording = false;
    private Handler handler     = new Handler();

    // ── RecognitionListener ───────────────────────────────────────────────
    private final RecognitionListener listener = new RecognitionListener() {

        @Override
        public void onResults(Bundle results) {
            ArrayList<String> matches =
                results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            if (matches == null || matches.isEmpty()) {
                resetRecordButton();
                isRecording = false;
                return;
            }

            // Prendre la première hypothèse
            String firstHypothesis = matches.get(0);

            // Normaliser
            String normalizedResult = QuranTextMatcher.normalize(firstHypothesis);
            String[] recognized = normalizedResult.split("\\s+");

            // Calculer score
            int score = TajwidAnalyzer.calculateScore(recognized, ayaWords);

            if (score >= 70) {
                // ✅ Correct : révéler l'aya en vert
                tvAyaRevealed.setText(ayaText);
                tvAyaRevealed.setVisibility(View.VISIBLE);
                tvAyaRevealed.setTextColor(0xFF2E7D32);
                tvResultImla.setText("✅ ممتاز! " + score + "/100");
                tvResultImla.setVisibility(View.VISIBLE);
                tvResultImla.setTextColor(0xFFFFD700);
                tvListening.setText("صحيح! انتقل للآية التالية");
            } else {
                // ❌ Incorrect : ne pas montrer l'aya
                tvAyaRevealed.setVisibility(View.GONE);

                // Calculer mots incorrects
                int incorrectCount = 0;
                int minLen = Math.min(recognized.length, ayaWords.length);
                for (int i = 0; i < minLen; i++) {
                    if (TajwidAnalyzer.levenshtein(
                            TajwidAnalyzer.normalize(recognized[i]),
                            TajwidAnalyzer.normalize(ayaWords[i])) > 1) {
                        incorrectCount++;
                    }
                }
                // Mots manquants comptent aussi
                incorrectCount += Math.abs(ayaWords.length - recognized.length);

                tvResultImla.setText("❌ " + score + "/100 — حاول مرة أخرى");
                tvResultImla.setVisibility(View.VISIBLE);
                tvResultImla.setTextColor(0xFFB71C1C);
                tvListening.setText("لم تُتلَ الآية بشكل صحيح");
            }

            // Reset bouton
            btnRecordImla.setText("🎙️ أعد");
            btnRecordImla.setBackgroundColor(0xFF227e08);
            isRecording = false;
        }

        @Override
        public void onError(int error) {
            resetRecordButton();
            tvListening.setText("خطأ في التعرف. حاول مجدداً");
            isRecording = false;
        }

        @Override public void onReadyForSpeech(Bundle params) {}
        @Override public void onBeginningOfSpeech() {}
        @Override public void onRmsChanged(float rmsdB) {}
        @Override public void onBufferReceived(byte[] buffer) {}
        @Override public void onEndOfSpeech() {}
        @Override public void onPartialResults(Bundle partialResults) {}
        @Override public void onEvent(int eventType, Bundle params) {}
    };

    // ══════════════════════════════════════════════════════════════════════
    //  Lifecycle
    // ══════════════════════════════════════════════════════════════════════

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tilawa_imla);

        // Lire extras
        Intent intent = getIntent();
        if (intent != null) {
            soraNum = intent.getIntExtra("soraNum", 1);
            ayaNum  = intent.getIntExtra("ayaNum",  1);
            isHafs  = (intent.getIntExtra("isHafs", 1) == 1);
        }

        // Init DB et matcher
        try {
            db = new DbHelper(this);
            db.createdatabase();
            db.opendatabase();
        } catch (IOException e) {
            Toast.makeText(this, "Erreur DB: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        matcher = new QuranTextMatcher(this);

        // Lier les vues
        tvAyaNumber   = (TextView) findViewById(R.id.tvAyaNumber);
        tvSoraName    = (TextView) findViewById(R.id.tvSoraName);
        tvAyaRevealed = (TextView) findViewById(R.id.tvAyaRevealed);
        tvListening   = (TextView) findViewById(R.id.tvListening);
        tvResultImla  = (TextView) findViewById(R.id.tvResultImla);

        btnRecordImla  = (Button) findViewById(R.id.btnRecordImla);
        btnPrevImla    = (Button) findViewById(R.id.btnPrevImla);
        btnNextImla    = (Button) findViewById(R.id.btnNextImla);
        btnBack        = (Button) findViewById(R.id.btnBack);
        btnRiwayaImla  = (Button) findViewById(R.id.btnRiwayaImla);

        // Charger l'aya (texte caché)
        loadAya();

        // Listeners
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { finish(); }
        });

        btnRiwayaImla.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                isHafs = !isHafs;
                btnRiwayaImla.setText(isHafs ? "حفص" : "ورش");
                loadAya();
            }
        });

        btnRecordImla.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                if (!isRecording) {
                    startListening();
                } else {
                    stopListening();
                }
            }
        });

        btnPrevImla.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { prevAya(); }
        });

        btnNextImla.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { nextAya(); }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
            speechRecognizer = null;
        }
        if (db != null) {
            db.close();
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    //  Chargement de l'aya
    // ══════════════════════════════════════════════════════════════════════

    private void loadAya() {
        ayaText  = matcher.getAyaText(soraNum, ayaNum, isHafs);
        ayaWords = QuranTextMatcher.normalize(ayaText).split("\\s+");
        tvAyaNumber.setText("الآية " + convertToArabicNumerals(ayaNum));
        tvSoraName.setText("سورة " + matcher.getSoraName(soraNum));
        tvAyaRevealed.setVisibility(View.GONE);
        tvResultImla.setVisibility(View.GONE);
        tvListening.setText("اضغط على الزر وابدأ التلاوة");
        btnRecordImla.setText("🎙️ ابدأ");
        btnRecordImla.setBackgroundColor(0xFF227e08);
        isRecording = false;
    }

    // ══════════════════════════════════════════════════════════════════════
    //  Reconnaissance vocale
    // ══════════════════════════════════════════════════════════════════════

    private void startListening() {
        if (speechRecognizer == null)
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ar-SA");
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);

        speechRecognizer.setRecognitionListener(listener);
        speechRecognizer.startListening(intent);

        isRecording = true;
        btnRecordImla.setText("⏹️ إيقاف");
        btnRecordImla.setBackgroundColor(0xFFB71C1C);
        tvListening.setText("جارٍ الاستماع...");
        tvAyaRevealed.setVisibility(View.GONE);
    }

    private void stopListening() {
        if (speechRecognizer != null) {
            speechRecognizer.stopListening();
        }
        resetRecordButton();
        isRecording = false;
        tvListening.setText("تم الإيقاف. اضغط مجدداً للمحاولة.");
    }

    private void resetRecordButton() {
        btnRecordImla.setText("🎙️ أعد");
        btnRecordImla.setBackgroundColor(0xFF227e08);
    }

    // ══════════════════════════════════════════════════════════════════════
    //  Navigation
    // ══════════════════════════════════════════════════════════════════════

    private void nextAya() {
        int maxAya = getAyaCount(soraNum);
        if (ayaNum < maxAya) {
            ayaNum++;
        } else if (soraNum < SORA_COUNT) {
            soraNum++;
            ayaNum = 1;
        } else {
            Toast.makeText(this, "نهاية المصحف", Toast.LENGTH_SHORT).show();
            return;
        }
        loadAya();
    }

    private void prevAya() {
        if (ayaNum > 1) {
            ayaNum--;
        } else if (soraNum > 1) {
            soraNum--;
            ayaNum = getAyaCount(soraNum);
        } else {
            Toast.makeText(this, "بداية المصحف", Toast.LENGTH_SHORT).show();
            return;
        }
        loadAya();
    }

    private int getAyaCount(int sora) {
        if (sora >= 1 && sora <= AYA_COUNTS.length) return AYA_COUNTS[sora - 1];
        return matcher.getAyaCount(sora, isHafs);
    }

    // ══════════════════════════════════════════════════════════════════════
    //  Utilitaires
    // ══════════════════════════════════════════════════════════════════════

    /**
     * Convertit un entier (1-604) en chiffres arabes (١-٦٠٤).
     */
    private String convertToArabicNumerals(int n) {
        // Chiffres arabes : ٠١٢٣٤٥٦٧٨٩ (U+0660–U+0669)
        String s = String.valueOf(n);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c >= '0' && c <= '9') {
                sb.append((char) ('\u0660' + (c - '0')));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}
