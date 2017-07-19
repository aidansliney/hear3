package com.hear2.aidansliney.hear2;

import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.util.Log;

import java.util.concurrent.atomic.AtomicBoolean;

public class Record extends Thread {

    private static final int bufferSize = 200000;
    private final short[] buffer = new short[bufferSize];
    private final int audioSessionId;
    private short[] readBuffer = new short[bufferSize];
    private AtomicBoolean recording = new AtomicBoolean();

    public Record(int audioSessionId) {
        this.audioSessionId = audioSessionId;
    }

    public void stopRecording() {
        recording.set(false);
    }

    @Override
    public void run() {
        recording.set(true);
        android.os.Process.setThreadPriority
                (android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);

        int buffersize = AudioRecord.getMinBufferSize(
                11025, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);


        AudioRecord audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
                11025,
                AudioFormat.CHANNEL_IN_MONO,
                MediaRecorder.AudioEncoder.AMR_NB,
                buffersize);
        AudioTrack audioTrack = new AudioTrack(
                new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ASSISTANCE_ACCESSIBILITY)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build(),
                new AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_DEFAULT)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build(),
                buffersize,
                AudioTrack.MODE_STREAM,
                audioSessionId);

        Log.d("AUDIO", "sample rate = : " + audioRecord.getSampleRate());

        audioTrack.setPlaybackRate(11025);

        byte[] buffer = new byte[buffersize];
        audioRecord.startRecording();
        audioTrack.play();

        while(recording.get()) {
            audioRecord.read(buffer, 0, buffersize);
            audioTrack.write(buffer, 0, buffer.length);
        }
    }
}
