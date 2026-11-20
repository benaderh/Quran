package quran.hb.com.quran;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.util.Log;

import java.io.File;
import java.io.IOException;

public class AudioRecorderHelper {

    private static final String TAG       = "AudioRecorderHelper";
    private static final String DIR_NAME  = "QuranHW";
    private static final String FILE_NAME = "rec_tilawa.3gp";

    private MediaRecorder mediaRecorder;
    private MediaPlayer   mediaPlayer;

    private String  recordingPath;
    private boolean isRecording   = false;
    private boolean isPlayingBack = false;

    public AudioRecorderHelper(Context context) {
        File dir = new File(context.getFilesDir(), DIR_NAME);
        if (!dir.exists()) { dir.mkdirs(); }
        recordingPath = new File(dir, FILE_NAME).getAbsolutePath();
    }

    public void startRecording() {
        if (isRecording) return;
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
        } catch (IOException e) {
            Log.e(TAG, "prepare/start failed", e);
            releaseRecorder();
        }
    }

    public void stopRecording() {
        if (!isRecording || mediaRecorder == null) return;
        try { mediaRecorder.stop(); } catch (RuntimeException ignored) {}
        finally { releaseRecorder(); }
    }

    private void releaseRecorder() {
        if (mediaRecorder != null) { mediaRecorder.release(); mediaRecorder = null; }
        isRecording = false;
    }

    public void playback() {
        if (isRecording) stopRecording();
        File file = new File(recordingPath);
        if (!file.exists()) return;
        stopPlayback();
        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(recordingPath);
            mediaPlayer.prepare();
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override public void onCompletion(MediaPlayer mp) { isPlayingBack = false; }
            });
            mediaPlayer.start();
            isPlayingBack = true;
        } catch (IOException e) {
            Log.e(TAG, "playback failed", e);
            releasePlayer();
        }
    }

    public void stopPlayback() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) mediaPlayer.stop();
            releasePlayer();
        }
    }

    private void releasePlayer() {
        if (mediaPlayer != null) { mediaPlayer.release(); mediaPlayer = null; }
        isPlayingBack = false;
    }

    public void release() { stopRecording(); stopPlayback(); }

    public String  getRecordingPath() { return recordingPath; }
    public boolean isRecording()      { return isRecording; }
    public boolean isPlayingBack()    { return isPlayingBack; }
    public boolean hasRecording()     { return new File(recordingPath).exists(); }
}