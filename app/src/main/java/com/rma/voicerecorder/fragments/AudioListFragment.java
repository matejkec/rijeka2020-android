package com.rma.voicerecorder.fragments;

import android.Manifest;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.rma.voicerecorder.adapters.AudioListAdapter;
import com.rma.voicerecorder.R;
import com.rma.voicerecorder.models.VoiceRecord;
import com.rma.voicerecorder.network.UploadToServer;
import com.rma.voicerecorder.utils.LoadingDialog;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class AudioListFragment extends Fragment implements AudioListAdapter.ItemClickListener, View.OnClickListener {

    private static final int NO_POSITION = -1;
    private static final int REQUEST_CODE = 1000;
    private final static String UPLOAD_SERVER_URI = "http://192.168.1.30:80/upload/upload.php";

    private BottomSheetBehavior bottomSheetBehavior;
    private RecyclerView audioListRecyclerView;
    private SharedPreferences sharedPreferences;

    private Drawable drawablePlay, drawablePause;
    private ImageButton btnPlay, btnNext, btnPrevious;
    private TextView textPlayerStatus, textPlayerFileName, textNoContent;
    private SeekBar playerSeekBar;
    private Handler seekBarHandler = new Handler();

    private int lastPosition = NO_POSITION;
    private File fileToPlay;
    private ArrayList<VoiceRecord> voiceRecords;
    private AudioListAdapter audioListAdapter;
    private ActionMode actionMode;
    private ActionModeCallback actionModeCallback = new ActionModeCallback();
    private MediaPlayer mediaPlayer;

    private LoadingDialog loadingDialog;

    public AudioListFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_audio_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        ConstraintLayout player = view.findViewById(R.id.player);
        bottomSheetBehavior = BottomSheetBehavior.from(player);

        textPlayerStatus = view.findViewById(R.id.text_player_header_title);
        textPlayerFileName = view.findViewById(R.id.text_player_filename);
        btnPrevious = view.findViewById(R.id.btn_previous);
        btnPrevious.setOnClickListener(this);
        btnNext = view.findViewById(R.id.btn_next);
        btnNext.setOnClickListener(this);
        btnPlay = view.findViewById(R.id.btn_play);
        btnPlay.setOnClickListener(this);
        playerSeekBar = view.findViewById(R.id.player_seekbar);
        playerSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (mediaPlayer != null) {
                    int progress = seekBar.getProgress();
                    mediaPlayer.seekTo(progress);
                }
            }
        });

        voiceRecords = getVoiceRecords();
        audioListAdapter = new AudioListAdapter(this.getContext(), voiceRecords, this);
        audioListRecyclerView = view.findViewById(R.id.view_audio_list);
        audioListRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        audioListRecyclerView.setAdapter(audioListAdapter);
        textNoContent = view.findViewById(R.id.text_no_content);
        textNoContent.setVisibility((audioListAdapter.getItemCount() != 0) ? View.INVISIBLE : View.VISIBLE);
        loadingDialog = new LoadingDialog(getActivity());

        drawablePause = getActivity().getResources().getDrawable(R.drawable.player_pause_btn, null);
        drawablePlay = getActivity().getResources().getDrawable(R.drawable.player_play_btn, null);


        // Handling back button
        OnBackPressedCallback onBackPressedCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (bottomSheetBehavior != null) {
                    if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                        return;
                    }
                }
                Navigation.findNavController(view).navigateUp();
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(this, onBackPressedCallback);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_previous:
                if (mediaPlayer != null) {
                    stopAudio();
                    lastPosition = (lastPosition == 0) ? audioListAdapter.getItemCount() - 1 : lastPosition - 1;
                    playAudio(voiceRecords.get(lastPosition));
                }
                break;
            case R.id.btn_play:
                if (mediaPlayer != null) {
                    if (mediaPlayer.isPlaying()) {
                        pauseAudio();
                    } else {
                        resumeAudio();
                    }
                }
                break;
            case R.id.btn_next:
                if (mediaPlayer != null) {
                    stopAudio();
                    lastPosition = (lastPosition + 1) % audioListAdapter.getItemCount();
                    playAudio(voiceRecords.get(lastPosition));
                }
                break;
        }
    }

    @Override
    public void onItemClicked(VoiceRecord voiceRecord, int position) {
        if (actionMode != null) {
            // Selection
            toggleSelection(position);
        } else {
            // Play audio
            if (mediaPlayer != null) {
                stopAudio();
            }
            lastPosition = position;
            playAudio(voiceRecord);
        }
    }

    @Override
    public boolean onItemLongClicked(int position) {
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        if (actionMode == null) {
            final AppCompatActivity appCompatActivity = (AppCompatActivity) this.getActivity();
            actionMode = appCompatActivity.startSupportActionMode(actionModeCallback);
        }
        toggleSelection(position);
        return true;
    }

    private void toggleSelection(int position) {
        audioListAdapter.toggleSelection(position);
        int count = audioListAdapter.getSelectedItemCount();
        if (count == 0) {
            actionMode.finish();
        } else {
            actionMode.setTitle(String.valueOf(count));
            actionMode.invalidate();
        }
    }

    private void playAudio(VoiceRecord voiceRecord) {
        fileToPlay = voiceRecord.getFile();
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
                pauseAudio();
                mediaPlayer.seekTo(0);
                playerSeekBar.setProgress(0);
                textPlayerStatus.setText(R.string.mp_finished);

            }
        });

        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        audioListRecyclerView.setNestedScrollingEnabled(false);
        btnPlay.setImageDrawable(drawablePause);
        textPlayerStatus.setText(R.string.mp_playing);
        textPlayerFileName.setText(fileToPlay.getName());
        playerSeekBar.setMax(mediaPlayer.getDuration());
        seekBarUpdate();

        String status = sharedPreferences.getString(fileToPlay.getName(), null);
        if (status == null) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(fileToPlay.getName(), "seen");
            editor.apply();
            voiceRecord.setStatus("seen");
        }
        voiceRecord.setPlaying(true);
        audioListAdapter.notifyItemChanged(lastPosition, true);
    }

    private void stopAudio() {
        mediaPlayer.stop();
        mediaPlayer.release();
        mediaPlayer = null;

        btnPlay.setImageDrawable(drawablePlay);
        textPlayerStatus.setText(R.string.mp_not_playing);
        textPlayerFileName.setText(R.string.mp_filename);
        playerSeekBar.setProgress(0);

        voiceRecords.get(lastPosition).setPlaying(false);
        audioListAdapter.notifyItemChanged(lastPosition, false);

        fileToPlay = null;
    }

    private void pauseAudio() {
        mediaPlayer.pause();

        btnPlay.setImageDrawable(drawablePlay);
        textPlayerStatus.setText(R.string.mp_paused);
        seekBarHandler.removeCallbacksAndMessages(null);
    }

    private void resumeAudio() {
        mediaPlayer.start();

        btnPlay.setImageDrawable(drawablePause);
        textPlayerStatus.setText(R.string.mp_playing);
        seekBarUpdate();
    }

    private void seekBarUpdate() {
        if (mediaPlayer != null) {
            playerSeekBar.setProgress(mediaPlayer.getCurrentPosition());
            seekBarHandler.postDelayed(getUpdateSeekBar, 100);
        } else {
            playerSeekBar.setProgress(0);
        }
    }

    private Runnable getUpdateSeekBar = new Runnable() {
        @Override
        public void run() {
            seekBarUpdate();
        }
    };

    private ArrayList<VoiceRecord> getVoiceRecords() {
        ArrayList<VoiceRecord> voiceRecords = new ArrayList<>();
        String path = getActivity().getExternalFilesDir("/").getAbsolutePath();
        File dir = new File(path);
        for (File file : dir.listFiles()) {
            voiceRecords.add(new VoiceRecord(file, sharedPreferences.getString(file.getName(), "")));
        }
        Collections.sort(voiceRecords, new Comparator<VoiceRecord>() {
            @Override
            public int compare(VoiceRecord f1, VoiceRecord f2) {
                return -Long.compare(f1.getFile().lastModified(), f2.getFile().lastModified());
            }
        });
        return voiceRecords;
    }

    private void deleteAudioFiles(List<Integer> positions) {
        List<Integer> positionsCopy = new ArrayList<>(positions);
        // Delete from device and SharedPreference
        SharedPreferences.Editor editor = sharedPreferences.edit();
        while (!positions.isEmpty()) {
            String fileName = voiceRecords.get(positions.get(0)).getFileName();
            editor.remove(fileName);
            voiceRecords.get(positions.get(0)).deleteFile();
            positions.remove(0);
        }
        editor.apply();
        // Delete items from RecyclerView
        audioListAdapter.removeItems(positionsCopy);
        // If RecyclerView is empty show "No content"
        if (audioListAdapter.getItemCount() == 0)
            textNoContent.setVisibility(View.VISIBLE);

        lastPosition = NO_POSITION;
    }

    private void uploadAudioFiles(List<Integer> positions) {
        if (!positions.isEmpty()) {
            VoiceRecord voiceRecord = voiceRecords.get(positions.get(0));
            File file = voiceRecord.getFile();
            UploadToServer uploadToServer = new UploadToServer(UPLOAD_SERVER_URI, file);
            uploadToServer.setNetworkOperationFinished(new UploadToServer.NetworkOperationFinished() {
                @Override
                public void onNetworkOperationFinished(String response) {
                    if(response.equals(getString(R.string.html_successful))){
                        Toast.makeText(getContext(), getString(R.string.msg_uploaded) + ": " + file.getName(), Toast.LENGTH_SHORT).show();
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString(file.getName(), "uploaded");
                        editor.apply();
                        voiceRecord.setStatus("uploaded");
                        audioListAdapter.notifyItemChanged(positions.get(0), true);
                        positions.remove(0);
                        uploadAudioFiles(positions);
                    } else {
                        Toast.makeText(getContext(), getString(R.string.html_error) + " " + response, Toast.LENGTH_SHORT).show();
                        loadingDialog.dismissDialog();
                        return;
                    }
                }
            });
            uploadToServer.execute();
        } else {
            loadingDialog.dismissDialog();
        }
    }

    public List<Integer> filteredUploads(List<Integer> positions){
        List<Integer> filtered = new ArrayList<>();
        for (int i : positions){
            if (!voiceRecords.get(i).getStatus().equals("uploaded")){
                filtered.add(i);
            }
        }
        return filtered;
    }


    private boolean checkPermission() {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE);
            return false;
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mediaPlayer != null) {
            stopAudio();
        }
    }

    private class ActionModeCallback implements ActionMode.Callback {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.selected_menu, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(final ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.menu_delete:
                    new MaterialAlertDialogBuilder(getContext())
                            .setMessage(getResources().getString(R.string.dialog_delete_msg))
                            .setNegativeButton(getResources().getString(R.string.dialog_no), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                }
                            })
                            .setPositiveButton(getResources().getString(R.string.dialog_yes), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    if (mediaPlayer != null) {
                                        stopAudio();
                                    }
                                    deleteAudioFiles(audioListAdapter.getSelectedItems());
                                    Toast.makeText(getContext(), getResources().getString(R.string.toast_deleted_msg), Toast.LENGTH_SHORT).show();
                                    mode.finish();
                                }
                            })
                            .show();
                    return true;
                case R.id.menu_upload:
                    if (checkPermission()) {
                        loadingDialog.startLoadingDialog();
                        uploadAudioFiles(filteredUploads(audioListAdapter.getSelectedItems()));
                    }
                    mode.finish();
                    return true;
                case R.id.menu_select_all:
                    audioListAdapter.selectAll();
                    actionMode.setTitle(String.valueOf(audioListAdapter.getSelectedItemCount()));
                    actionMode.invalidate();
                    return true;
                default:
                    return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            audioListAdapter.clearSelection();
            actionMode = null;
        }
    }
}