package com.rma.voicerecorder.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.rma.voicerecorder.R;

public class AppActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
    }
}
