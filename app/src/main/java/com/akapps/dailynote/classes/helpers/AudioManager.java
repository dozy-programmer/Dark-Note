package com.akapps.dailynote.classes.helpers;

import android.app.Activity;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Handler;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.TextView;
import com.akapps.dailynote.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.io.IOException;

public class AudioManager {

    private MediaPlayer player = null;
    private MediaRecorder recorder = null;

    // data
    private String fileName;
    private boolean isRecording, isPlaying, isPaused, isStopped;
    private int audioDuration;

    private Handler handlerTimer = new Handler();
    private Handler handlerTimerText = new Handler();

    private Activity activity;
    private SeekBar audioSeekbar;
    private TextView currentDuration;
    private FloatingActionButton pausePlayButton;

    public AudioManager(String fileName){
        this.fileName = fileName;
    }

    public AudioManager(String fileName, TextView currentDuration, SeekBar audioSeekbar,
                        FloatingActionButton pausePlayButton, Activity activity, int audioDuration){
        this.fileName = fileName;
        this.currentDuration = currentDuration;
        this.audioSeekbar = audioSeekbar;
        this.pausePlayButton = pausePlayButton;
        this.activity = activity;
        this.audioDuration = audioDuration;
        initSeekBar();
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
        isPaused = false;
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
            isPlaying = true;
            isStopped = isPaused = false;
        } catch (IOException e) {
            Log.e("Here", "prepare() failed");
        }
        Helper.startTimer(handlerTimer, 0);
        updateTextDuration(currentDuration, audioSeekbar);
        pausePlayButton.setImageDrawable(activity.getDrawable(R.drawable.pause_icon));
    }

    // pause playback
    public void pausePlaying() {
        player.pause();
        isStopped = isPlaying = false;
        isPaused = true;
        handlerTimer.removeCallbacksAndMessages(null);
        handlerTimerText.removeCallbacksAndMessages(null);
        pausePlayButton.setImageDrawable(activity.getDrawable(R.drawable.play_icon));
    }

    // pause playback
    public void resumePlaying() {
        if(AppData.timerDuration >= audioDuration) {
            AppData.timerDuration = 0;
            player.seekTo(AppData.timerDuration * 1000);
        }
        player.start();
        isPlaying = true;
        isStopped = isPaused = false;
        Helper.startTimer(handlerTimer, AppData.timerDuration);
        updateTextDuration(currentDuration, audioSeekbar);
        pausePlayButton.setImageDrawable(activity.getDrawable(R.drawable.pause_icon));
    }

    public void changePositionByIncrement(int increment){
        int newValue = AppData.timerDuration + increment;

        if(newValue > audioDuration)
            AppData.timerDuration = audioDuration;
        else if(AppData.timerDuration == audioDuration && increment < 0)
            AppData.timerDuration = audioDuration + increment;
        else if(newValue >= 0 && newValue <= audioDuration)
            AppData.timerDuration += increment;
        else
            AppData.timerDuration = 0;

        currentDuration.setText(Helper.secondsToDurationText(AppData.timerDuration));
        audioSeekbar.setProgress(AppData.timerDuration);
        if (player != null)
            player.seekTo(audioSeekbar.getProgress() * 1000);
    }

    private void updateTextDuration(TextView playingDuration, SeekBar audioSeekbar){
        handlerTimerText.postDelayed(new Runnable() {
            public void run() {
                int currentTime = AppData.timerDuration;
                if(currentTime <= audioDuration) {
                    playingDuration.setText(Helper.secondsToDurationText(currentTime));
                    if(audioSeekbar != null)
                        audioSeekbar.setProgress(AppData.timerDuration);
                    handlerTimerText.postDelayed(this, 1000);
                }
                else{
                    AppData.timerDuration--;
                    Log.d("Here", "~~~~~~~~~ should be stopped");
                    pausePlaying();
                    pausePlayButton.setImageDrawable(activity.getDrawable(R.drawable.play_icon));
                }
            }
        }, 0);
    }

    private void initSeekBar(){
        audioSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {}

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                AppData.timerDuration = seekBar.getProgress();
                currentDuration.setText(Helper.secondsToDurationText(AppData.timerDuration));
                if(player != null)
                    player.seekTo(seekBar.getProgress() * 1000);
            }
        });
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

        if(handlerTimerText != null)
            handlerTimerText.removeCallbacksAndMessages(null);

        if(handlerTimer != null)
            handlerTimer.removeCallbacksAndMessages(null);
    }
}
