package com.rma.voicerecorder.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.view.LayoutInflater;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.rma.voicerecorder.R;

public class LoadingDialog extends ConstraintLayout {

    Activity activity;
    AlertDialog dialog;

    public LoadingDialog(Activity myActivity){
        super(myActivity);
        activity = myActivity;
    }

    public void startLoadingDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        LayoutInflater inflater = activity.getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.custom_loading_dialog, null));
        builder.setCancelable(false);
        dialog = builder.create();
        dialog.show();
    }

    public void dismissDialog(){
        dialog.dismiss();
    }
}
