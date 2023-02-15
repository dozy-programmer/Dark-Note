package com.akapps.dailynote.classes.helpers;

import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.util.Log;

import java.io.IOException;

public class RecordAudio {

    private MediaPlayer player = null;
    private MediaRecorder recorder = null;

    // data
    private String fileName;
    private boolean isRecording, isPlaying, isPaused, isStopped;

    public RecordAudio(String fileName){
        this.fileName = fileName;
    }

    // start recording audio
    public void startRecording() {
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        recorder.setOutputFile(fileName);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);

        try {
            recorder.prepare();
        } catch (IOException e) {
            Log.e("Here", "prepare() failed");
        }

        recorder.start();
        isRecording = true;
    }

    // pause audio recording
    public void pauseRecording(boolean pause) {
        if(pause) {
            recorder.pause();
            // update status
            isRecording = false;
            isPaused = true;
        }
        else {
            recorder.resume();
            // update status
            isRecording = true;
            isPaused = false;
        }
    }

    // stop recording
    public void stopRecording() {
        recorder.stop();
        recorder.release();
        recorder = null;
        // update status
        isRecording = isPaused = false;
        isStopped = true;
        onStop();
    }

    // playback
    public void startPlaying() {
        player = new MediaPlayer();
        try {
            player.setDataSource(fileName);
            player.prepare();
            player.start();
        } catch (IOException e) {
            Log.e("Here", "prepare() failed");
        }
    }

    // pause playback
    public void pausePlaying() {
        player.pause();
    }

    // pause playback
    public void resumePlaying() {
        player.start();
    }

    // stop playback
    public void stopPlaying() {
        player.release();
        player = null;
    }

    public boolean isRecording(){
        return isRecording;
    }

    public boolean isPlaying(){
        return isPlaying;
    }

    public boolean isPaused(){
        return isPaused;
    }

    public boolean isStopped(){
        return isStopped;
    }

    public void onStop(){
        if (recorder != null) {
            recorder.release();
            recorder = null;
        }

        if (player != null) {
            player.release();
            player = null;
        }
    }
}
