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
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

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
    private TextView timerTV;
    private ImageButton recordButton;

    private int audioRecordingDelayMs;
    private int vibrationDuration;
    private static final int REQUEST_CODE = 100;
    private Toast toast;

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

        audioRecordingDelayMs = getResources().getInteger(R.integer.audio_recording_delay);
        vibrationDuration = getResources().getInteger(R.integer.vibration_duration);

        timerTV = view.findViewById(R.id.text_record_timer);
        int maxAudioRecordingTimeMs = getResources().getInteger(R.integer.max_audio_recording_time);
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
            if (voiceRecorder.startRecording()) {
                // timer start, button animation, update text
                timer.freshStart();
                Animation scaleAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.scale);
                scaleAnimation.reset();
                recordButton.startAnimation(scaleAnimation);
                // vibrate
                if (Build.VERSION.SDK_INT >= 26) {
                    vibrator.vibrate(VibrationEffect.createOneShot(vibrationDuration, VibrationEffect.DEFAULT_AMPLITUDE));
                } else {
                    vibrator.vibrate(vibrationDuration);
                }
            }
        }
    };

    private void recordButtonReleased() {
        recordButton.setImageDrawable(getResources().getDrawable(R.drawable.record_btn_stopped, null));
        handler.removeCallbacks(delayedStart);
        recordButton.clearAnimation();
        timer.stop();
        if (timer.getTime() < 200){
            voiceRecorder.stopAndDelete();
        } else if (voiceRecorder.stopRecording()) {
            showToast(getString(R.string.toast_saved_msg));
        }
    }

    protected void showToast(final String text) {
        if (toast == null) {
            toast = Toast.makeText(getActivity(), text, Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.BOTTOM, 0, timerTV.getHeight() + recordButton.getHeight() + 20);
        } else
            toast.setText(text);
        toast.show();
    }

    private boolean checkPermission() {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_CODE);
            return false;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if(voiceRecorder.isRecording()){
            recordButtonReleased();
        }
        if(toast != null)
            toast.cancel();
    }
}