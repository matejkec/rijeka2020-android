package com.rma.voicerecorder.models;

import com.rma.voicerecorder.utils.TimeAgo;
import java.io.File;

public class VoiceRecord {
    private File file;
    private String status;
    private boolean playing;

    public VoiceRecord(File file){
        this(file, "", false);
    }

    public VoiceRecord(File file, String status){
        this(file, status, false);
    }

    public VoiceRecord(File file, String status, boolean playing) {
        this.file = file;
        this.status = status;
        this.playing = playing;
    }

    public File getFile() {
        return file;
    }

    public String getFileName() {
        return file.getName();
    }

    public String getTimeAgo() {
        return new TimeAgo().getTimeAgo(file.lastModified());
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isPlaying() {
        return playing;
    }

    public void setPlaying(boolean playing) {
        this.playing = playing;
    }

    public boolean deleteFile(){
        return file.delete();
    }
}
