package com.rma.voicerecorder.utils;

import android.media.MediaRecorder;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class VoiceRecorder {

    private MediaRecorder mediaRecorder;
    private SimpleDateFormat simpleDateFormat;
    private boolean isRecording = false;
    private String recordPath;
    private int maxDuration;

    public VoiceRecorder(String recordPath, int maxDuration){
        this.recordPath = recordPath;
        this.maxDuration = maxDuration;
        simpleDateFormat = new SimpleDateFormat("yyyyMMdd_hhmmss");
    }

    public void startRecording() {
        // file name
        Date dateNow = new Date();
        String recordFileName = "REC_" + simpleDateFormat.format(dateNow) + ".3gp";
        // media recorder start
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mediaRecorder.setOutputFile(recordPath + "/" + recordFileName);
        mediaRecorder.setMaxDuration(maxDuration);
        mediaRecorder.setOnInfoListener(new MediaRecorder.OnInfoListener() {
            @Override
            public void onInfo(MediaRecorder mr, int what, int extra) {
                // stop recording if it lasts longer than "MAX_AUDIO_RECORDING_TIME_MS" ms
                if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
                    stopRecording();
                }
            }
        });
        try {
            mediaRecorder.prepare();
            mediaRecorder.start();
            isRecording = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stopRecording() {
        if (mediaRecorder != null) {
            try {
                mediaRecorder.stop();
            } catch (RuntimeException ignored) {
            }
            mediaRecorder.reset();
            mediaRecorder.release();
            mediaRecorder = null;
            isRecording = false;
        }
    }

    public boolean isRecording(){
        return isRecording;
    }
}
