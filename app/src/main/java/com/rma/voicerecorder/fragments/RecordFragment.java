package com.rma.voicerecorder.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
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
import com.rma.voicerecorder.utils.VoiceRecorder;

import static android.content.Context.VIBRATOR_SERVICE;

/**
 * A simple {@link Fragment} subclass.
 */

public class RecordFragment extends Fragment{
    private NavController navController;
    private Handler handler = new Handler();
    Vibrator vibrator;

    private VoiceRecorder voiceRecorder;
    private Timer timer;
    private TextView recordStatus;
    private ImageButton recordButton;

    private int audioRecordingDelayMs;
    private int onscreenTextTime;
    private static final int REQUEST_CODE = 100;

    public RecordFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_record, container, false);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);

        ImageButton listButton = view.findViewById(R.id.btn_record_list);
        recordButton = view.findViewById(R.id.btn_record);
        recordStatus = view.findViewById(R.id.text_record_status);

        int maxAudioRecordingTimeMs = getResources().getInteger(R.integer.max_audio_recording_time);
        audioRecordingDelayMs = getResources().getInteger(R.integer.audio_recording_delay);
        onscreenTextTime = getResources().getInteger(R.integer.onscreen_text_time);

        TextView timerTV = view.findViewById(R.id.text_record_timer);
        timer = new Timer(timerTV, maxAudioRecordingTimeMs);
        voiceRecorder = new VoiceRecorder(getActivity().getExternalFilesDir("/").getAbsolutePath(), maxAudioRecordingTimeMs);

        recordButton.setOnTouchListener(new View.OnTouchListener() {
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
        });
        listButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                voiceRecorder.stopRecording();
                navController.navigate(R.id.action_recordFragment_to_audioListFragment);
            }
        });

        vibrator = (Vibrator) getActivity().getSystemService(VIBRATOR_SERVICE);
    }

    private void recordButtonPressed() {
        recordButton.setImageDrawable(getResources().getDrawable(R.drawable.record_btn_recording, null));
        timer.reset();
        if (checkPermission() && !voiceRecorder.isRecording()) {
            // start recording if the button is pressed for more than "AUDIO_RECORDING_DELAY_MS" ms
            handler.postDelayed(delayedStart, audioRecordingDelayMs);
        }
    }

    private Runnable delayedStart = new Runnable() {
        @Override
        public void run() {
            if (!voiceRecorder.isRecording()) {
                voiceRecorder.startRecording();
                // timer start, button animation, update text
                timer.freshStart();
                recordStatus.setText(R.string.record_status_recording);
                recordStatus.clearAnimation();
                handler.removeCallbacks(recordStatusDisappear);
                Animation scaleAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.scale);
                scaleAnimation.reset();
                recordButton.startAnimation(scaleAnimation);
                // vibrate
                if (Build.VERSION.SDK_INT >= 26) {
                    vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE));
                } else {
                    vibrator.vibrate(100);
                }
            }
        }
    };

    private void recordButtonReleased() {
        recordButton.setImageDrawable(getResources().getDrawable(R.drawable.record_btn_stopped, null));
        handler.removeCallbacks(delayedStart);
        if (voiceRecorder.isRecording()) {
            voiceRecorder.stopRecording();
            timer.stop();
            recordStatus.setText(R.string.record_status_file_saved);
            handler.removeCallbacks(recordStatusDisappear);
            handler.postDelayed(recordStatusDisappear, onscreenTextTime);
            recordButton.clearAnimation();
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

    private Runnable recordStatusDisappear = new Runnable() {
        @Override
        public void run() {
            Animation disappearAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.disappear);
            disappearAnimation.reset();
            recordStatus.clearAnimation();
            recordStatus.startAnimation(disappearAnimation);
        }
    };
}