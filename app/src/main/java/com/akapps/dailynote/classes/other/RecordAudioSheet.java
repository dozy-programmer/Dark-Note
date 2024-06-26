package com.akapps.dailynote.classes.other;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.airbnb.lottie.LottieAnimationView;
import com.akapps.dailynote.R;
import com.akapps.dailynote.activity.NoteEdit;
import com.akapps.dailynote.classes.data.CheckListItem;
import com.akapps.dailynote.classes.data.Place;
import com.akapps.dailynote.classes.helpers.AppData;
import com.akapps.dailynote.classes.helpers.AudioManager;
import com.akapps.dailynote.classes.helpers.Helper;
import com.akapps.dailynote.classes.helpers.UiHelper;
import com.deishelon.roundedbottomsheet.RoundedBottomSheetDialogFragment;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class RecordAudioSheet extends RoundedBottomSheetDialogFragment {

    private AudioManager audioManager;
    private String recordToFilePath;
    private final Handler handlerTimer = new Handler();
    private final Handler handlerTimerText = new Handler();

    private BottomSheetDialog dialog;

    public RecordAudioSheet() {
        AppData.timerDuration = 0;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_record_audio, container, false);

        // generate random file name
        String randomString = UUID.randomUUID().toString();
        recordToFilePath = getActivity().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) +
                "/recording_" + randomString.substring(0, randomString.length() / 3)
                .replaceAll("-", "_") + ".mp3";

        audioManager = new AudioManager(recordToFilePath);

        MaterialButton close = view.findViewById(R.id.cancel_recording);
        MaterialButton done = view.findViewById(R.id.done_recording);
        FloatingActionButton pauseOrPlayButton = view.findViewById(R.id.pause_play_button);
        TextView recordingDuration = view.findViewById(R.id.timer);
        LottieAnimationView recordingAnimation = view.findViewById(R.id.recording_animation);

        recordingAnimation.pauseAnimation();

        pauseOrPlayButton.setOnClickListener(view12 -> {
            if (audioManager.isRecording()) {
                handlerTimer.removeCallbacksAndMessages(null);
                audioManager.pauseRecording(true);
                recordingAnimation.pauseAnimation();
                pauseOrPlayButton.setImageDrawable(getActivity().getDrawable(R.drawable.mic_icon));
                pauseOrPlayButton.setBackgroundTintList(ColorStateList.valueOf(getActivity()
                        .getResources().getColor(R.color.red)));
            } else if (audioManager.isPaused()) {
                Helper.startTimer(handlerTimer, AppData.timerDuration);
                audioManager.pauseRecording(false);
                recordingAnimation.playAnimation();
                pauseOrPlayButton.setImageDrawable(getActivity().getDrawable(R.drawable.pause_icon));
                pauseOrPlayButton.setBackgroundTintList(ColorStateList.valueOf(getActivity()
                        .getResources().getColor(R.color.ocean_green)));
            } else {
                dialog.setCanceledOnTouchOutside(false);
                dialog.setCancelable(false);
                audioManager.startRecording();
                Helper.startTimer(handlerTimer, 0);
                updateRecordingDuration(recordingDuration);
                recordingAnimation.playAnimation();
                pauseOrPlayButton.setImageDrawable(getActivity().getDrawable(R.drawable.pause_icon));
                pauseOrPlayButton.setBackgroundTintList(ColorStateList.valueOf(getActivity()
                        .getResources().getColor(R.color.ocean_green)));
            }
        });

        close.setOnClickListener(view13 -> {
            if (audioManager != null && audioManager.isRecording())
                audioManager.stopRecording();
            Helper.deleteFile(recordToFilePath);
            dismiss();
        });

        done.setOnClickListener(view1 -> {
            if (audioManager != null && audioManager.isRecording())
                audioManager.stopRecording();
            if (!Helper.isFileEmpty(recordToFilePath)) {
                CheckListItem item = ((NoteEdit) getActivity()).addCheckList("", new Place(), "");
                ((NoteEdit) getActivity()).addCheckList(item.getSubListId(), recordToFilePath, AppData.timerDuration);
            }
            dismiss();
        });

        return view;
    }

    @Override
    public void onStop() {
        if (handlerTimerText != null)
            handlerTimerText.removeCallbacksAndMessages(null);
        if (handlerTimer != null)
            handlerTimer.removeCallbacksAndMessages(null);
        super.onStop();
    }

    private void updateRecordingDuration(TextView recordingDuration) {
        handlerTimerText.postDelayed(new Runnable() {
            public void run() {
                int currentTime = AppData.timerDuration;
                recordingDuration.setText(Helper.secondsToDurationText(currentTime));
                handlerTimerText.postDelayed(this, 1000);
            }
        }, 0);
    }

    @Override
    public int getTheme() {
        return UiHelper.getBottomSheetTheme(getContext());
    }

    @Override
    public void onViewCreated(@NotNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        dialog = (BottomSheetDialog) getDialog();
        UiHelper.setBottomSheetBehavior(view, dialog);
    }

}