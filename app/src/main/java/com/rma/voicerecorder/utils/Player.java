package com.rma.voicerecorder.utils;

import android.media.MediaPlayer;
import android.util.Log;

import java.io.File;
import java.io.IOException;

public class Player {

    private MediaPlayer mediaPlayer;

    private void playAudio(File fileToPlay) {
        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(fileToPlay.getAbsolutePath());
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                stopAudio();
                Log.d("asdd", "Finished");
            }
        });
        Log.d("asdd", "Play");
    }

    private void stopAudio() {
        mediaPlayer.stop();
        mediaPlayer.release();
        mediaPlayer = null;
        Log.d("asdd", "Stopped");
    }

    private void pauseAudio() {
        Log.d("asdd", "Pause");
        mediaPlayer.pause();
    }

    private void resumeAudio() {
        Log.d("asdd", "Resume");
        mediaPlayer.start();
    }

    private int getAudioDuration() {
        return mediaPlayer.getDuration();
    }

    private boolean isPlaying(){
        return mediaPlayer != null;
    }
}
