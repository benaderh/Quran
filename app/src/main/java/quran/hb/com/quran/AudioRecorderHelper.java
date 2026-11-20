package quran.hb.com.quran;

import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.IOException;

/**
 * Helper pour l'enregistrement audio et la lecture de la récitation (tilawa).
 *
 * Chemin de sortie : /sdcard/QuranHW/rec_tilawa.3gp
 *
 * Usage :
 *   AudioRecorderHelper recorder = new AudioRecorderHelper();
 *   recorder.startRecording();
 *   // ... l'utilisateur récite ...
 *   recorder.stopRecording();
 *   recorder.playback();
 *   // ...
 *   recorder.stopPlayback();
 *   recorder.release();
 */
public class AudioRecorderHelper {

    private static final String TAG        = "AudioRecorderHelper";
    private static final String DIR_NAME   = "QuranHW";
    private static final String FILE_NAME  = "rec_tilawa.3gp";

    private MediaRecorder mediaRecorder;
    private MediaPlayer   mediaPlayer;

    private String recordingPath;
    private boolean isRecording  = false;
    private boolean isPlayingBack = false;

    // ------------------------------------------------------------------ //
    //  Constructeur
    // ------------------------------------------------------------------ //

    public AudioRecorderHelper() {
        // Construire le chemin d'enregistrement
        File dir = new File(Environment.getExternalStorageDirectory(), DIR_NAME);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        recordingPath = new File(dir, FILE_NAME).getAbsolutePath();
    }

    // ------------------------------------------------------------------ //
    //  Enregistrement
    // ------------------------------------------------------------------ //

    /**
     * Démarre l'enregistrement microphone vers /sdcard/QuranHW/rec_tilawa.3gp.
     * Nécessite les permissions RECORD_AUDIO et WRITE_EXTERNAL_STORAGE.
     */
    public void startRecording() {
        if (isRecording) {
            Log.w(TAG, "startRecording() appelé alors qu'un enregistrement est en cours.");
            return;
        }
        // Arrêter une éventuelle lecture en cours
        stopPlayback();

        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mediaRecorder.setOutputFile(recordingPath);

        try {
            mediaRecorder.prepare();
            mediaRecorder.start();
            isRecording = true;
            Log.d(TAG, "Enregistrement démarré : " + recordingPath);
        } catch (IOException e) {
            Log.e(TAG, "Erreur lors de la préparation de MediaRecorder", e);
            releaseRecorder();
        }
    }

    /**
     * Arrête l'enregistrement en cours.
     */
    public void stopRecording() {
        if (!isRecording || mediaRecorder == null) {
            Log.w(TAG, "stopRecording() appelé alors qu'aucun enregistrement n'est en cours.");
            return;
        }
        try {
            mediaRecorder.stop();
            Log.d(TAG, "Enregistrement arrêté.");
        } catch (RuntimeException e) {
            Log.e(TAG, "Erreur lors de l'arrêt de MediaRecorder", e);
        } finally {
            releaseRecorder();
        }
    }

    private void releaseRecorder() {
        if (mediaRecorder != null) {
            mediaRecorder.release();
            mediaRecorder = null;
        }
        isRecording = false;
    }

    // ------------------------------------------------------------------ //
    //  Lecture (playback)
    // ------------------------------------------------------------------ //

    /**
     * Lit le fichier enregistré.
     */
    public void playback() {
        if (isRecording) {
            Log.w(TAG, "playback() : enregistrement en cours, arrêt préalable.");
            stopRecording();
        }

        File file = new File(recordingPath);
        if (!file.exists()) {
            Log.e(TAG, "Fichier audio introuvable : " + recordingPath);
            return;
        }

        stopPlayback(); // Libérer un éventuel player précédent

        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(recordingPath);
            mediaPlayer.prepare();
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    isPlayingBack = false;
                }
            });
            mediaPlayer.start();
            isPlayingBack = true;
            Log.d(TAG, "Lecture démarrée : " + recordingPath);
        } catch (IOException e) {
            Log.e(TAG, "Erreur lors de la lecture audio", e);
            releasePlayer();
        }
    }

    /**
     * Arrête la lecture en cours.
     */
    public void stopPlayback() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            releasePlayer();
        }
    }

    private void releasePlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        isPlayingBack = false;
    }

    // ------------------------------------------------------------------ //
    //  Libération globale (appeler dans onDestroy de l'Activity)
    // ------------------------------------------------------------------ //

    /**
     * Libère toutes les ressources. À appeler dans onDestroy() de l'Activity.
     */
    public void release() {
        stopRecording();
        stopPlayback();
    }

    // ------------------------------------------------------------------ //
    //  Accesseurs
    // ------------------------------------------------------------------ //

    /** Retourne le chemin absolu du fichier d'enregistrement. */
    public String getRecordingPath() {
        return recordingPath;
    }

    /** Indique si un enregistrement est actuellement en cours. */
    public boolean isRecording() {
        return isRecording;
    }

    /** Indique si une lecture est actuellement en cours. */
    public boolean isPlayingBack() {
        return isPlayingBack;
    }

    /** Indique si le fichier enregistré existe. */
    public boolean hasRecording() {
        return new File(recordingPath).exists();
    }
}
