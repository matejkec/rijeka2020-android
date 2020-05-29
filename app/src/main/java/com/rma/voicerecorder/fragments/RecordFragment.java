package com.rma.voicerecorder.fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.TextView;

import com.rma.voicerecorder.R;
import com.rma.voicerecorder.utils.Timer;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * A simple {@link Fragment} subclass.
 */
public class RecordFragment extends Fragment implements View.OnTouchListener{
    private NavController navController;
    private Handler handler = new Handler();

    private MediaRecorder mediaRecorder;
    private TextView recordStatus;
    private Timer timer;
    private ImageButton recordButton;

    private String recordFileName, recordPath;
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd_hhmmss");

    private int maxAudioRecordingTimeMs;
    private int minAudioRecordingTimeMs;
    private int audioRecordingDelayMs;
    private int onscreenTextTime;
    private static final int REQUEST_CODE = 100;

    private boolean isRecording = false;

    public RecordFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_record, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);
        ImageButton listButton = view.findViewById(R.id.btn_record_list);
        recordButton = view.findViewById(R.id.btn_record);
        recordStatus = view.findViewById(R.id.text_record_status);

        maxAudioRecordingTimeMs = getResources().getInteger(R.integer.max_audio_recording_time);
        minAudioRecordingTimeMs = getResources().getInteger(R.integer.min_audio_recording_time);
        audioRecordingDelayMs = getResources().getInteger(R.integer.audio_recording_delay);
        onscreenTextTime = getResources().getInteger(R.integer.onscreen_text_time);

        TextView timerTV = view.findViewById(R.id.text_record_timer);
        timer = new Timer(timerTV, maxAudioRecordingTimeMs);

        recordButton.setOnTouchListener(this);
        listButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopRecording();
                navController.navigate(R.id.action_recordFragment_to_audioListFragment);
            }
        });
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                recordButtonPressed();
                break;
            case MotionEvent.ACTION_UP:
                recordButtonReleased();
                break;
        }
        return false;
    }

    private void recordButtonPressed() {
        recordButton.setImageDrawable(getResources().getDrawable(R.drawable.record_btn_recording, null));
        timer.reset();
        if (checkPermission() && !isRecording) {
            // start recording if the button is pressed for more than "AUDIO_RECORDING_DELAY_MS" ms
            handler.postDelayed(delayedStart, audioRecordingDelayMs);
        }
    }

    private void recordButtonReleased() {
        recordButton.setImageDrawable(getResources().getDrawable(R.drawable.record_btn_stopped, null));
        handler.removeCallbacks(delayedStart);
        if (isRecording) {
            stopRecording();
            // delete file if record lasts less than "MIN_AUDIO_RECORDING_TIME_MS" ms
            if (timer.getTime() < minAudioRecordingTimeMs) {
                if (new File(recordPath + "/" + recordFileName).delete())
                    recordStatus.setText(R.string.record_status_short_voice);
            } else {
                recordStatus.setText(R.string.record_status_file_saved);
            }
            timer.stop();
            updateRecordStatusAnimation();
            recordButton.clearAnimation();
        }
    }

    private void startRecording() {
        // file name
        Date dateNow = new Date();
        recordPath = getActivity().getExternalFilesDir("/").getAbsolutePath();
        recordFileName = "REC_" + simpleDateFormat.format(dateNow) + ".3gp";
        // media recorder start
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mediaRecorder.setOutputFile(recordPath + "/" + recordFileName);
        mediaRecorder.setMaxDuration(maxAudioRecordingTimeMs);
        mediaRecorder.setOnInfoListener(new MediaRecorder.OnInfoListener() {
            @Override
            public void onInfo(MediaRecorder mr, int what, int extra) {
                // stop recording if it lasts longer than "MAX_AUDIO_RECORDING_TIME_MS" ms
                if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
                    recordButtonReleased();
                }
            }
        });
        try {
            mediaRecorder.prepare();
            mediaRecorder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        isRecording = true;
    }

    private void stopRecording() {
        if (mediaRecorder != null) {
            mediaRecorder.stop();
            mediaRecorder.reset();
            mediaRecorder.release();
            mediaRecorder = null;
            isRecording = false;
        }
    }

    private boolean checkPermission() {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_CODE);
            return false;
        }
    }

    private Runnable delayedStart = new Runnable() {
        @Override
        public void run() {
            if (!isRecording) {
                startRecording();
                // timer start, button animation, update text
                timer.freshStart();
                recordStatus.setText(R.string.record_status_recording);
                updateRecordStatusAnimation();
                Animation scaleAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.scale);
                scaleAnimation.reset();
                recordButton.startAnimation(scaleAnimation);
            }
        }
    };

    private void updateRecordStatusAnimation() {
        handler.removeCallbacks(startRecordStatusAnimation);
        if (recordStatus.getText() != getResources().getString(R.string.record_status_recording)) {
            handler.postDelayed(startRecordStatusAnimation, onscreenTextTime);
        } else {
            recordStatus.clearAnimation();
        }
    }

    private Runnable startRecordStatusAnimation = new Runnable() {
        @Override
        public void run() {
            Animation disappearAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.disappear);
            disappearAnimation.reset();
            recordStatus.clearAnimation();
            recordStatus.startAnimation(disappearAnimation);
        }
    };
}