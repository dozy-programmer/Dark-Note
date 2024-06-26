package com.akapps.dailynote.classes.other;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.akapps.dailynote.R;
import com.akapps.dailynote.activity.NoteEdit;
import com.akapps.dailynote.adapter.IconMenuAdapter;
import com.akapps.dailynote.classes.data.CheckListItem;
import com.akapps.dailynote.classes.helpers.AppData;
import com.akapps.dailynote.classes.helpers.AudioManager;
import com.akapps.dailynote.classes.helpers.Helper;
import com.akapps.dailynote.classes.helpers.RealmHelper;
import com.akapps.dailynote.classes.helpers.UiHelper;
import com.deishelon.roundedbottomsheet.RoundedBottomSheetDialogFragment;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.skydoves.powermenu.CustomPowerMenu;
import com.skydoves.powermenu.MenuAnimation;
import com.skydoves.powermenu.OnMenuItemClickListener;

import org.jetbrains.annotations.NotNull;

import java.io.File;

import www.sanju.motiontoast.MotionToast;

public class PlayAudioSheet extends RoundedBottomSheetDialogFragment {

    // data
    private final CheckListItem item;

    // timer
    private AudioManager audioManager;

    // UI
    private FloatingActionButton pausePlayButton;
    private CustomPowerMenu audioMenu;
    private ImageView dropDownMenu;

    public PlayAudioSheet(CheckListItem item) {
        this.item = item;
        AppData.timerDuration = 0;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_play_audio, container, false);

        FloatingActionButton rewind = view.findViewById(R.id.rewind_audio);
        pausePlayButton = view.findViewById(R.id.pause_play_button);
        dropDownMenu = view.findViewById(R.id.dropdown_menu);
        FloatingActionButton forward = view.findViewById(R.id.forward_audio);
        SeekBar audioSeekbar = view.findViewById(R.id.audio_seekbar);
        TextView currentDuration = view.findViewById(R.id.current_duration);
        TextView totalAudioTime = view.findViewById(R.id.total_audio_time);
        MaterialButton infoRecording = view.findViewById(R.id.info_recording);

        String audioPath = item.getAudioPath();
        int audioDuration = item.getAudioDuration();
        totalAudioTime.setText(Helper.secondsToDurationText(audioDuration));
        audioSeekbar.setMax(audioDuration);
        audioManager = new AudioManager(audioPath, currentDuration, audioSeekbar,
                pausePlayButton, getActivity(), item.getAudioDuration());

        dropDownMenu.setOnClickListener(view1 -> openMenuDialog());

        infoRecording.setOnClickListener(view15 -> {
            InfoSheet info = new InfoSheet(getRecordingInfo(), 11);
            info.show(getActivity().getSupportFragmentManager(), info.getTag());
        });

        rewind.setOnClickListener(view12 -> {
            if (audioManager.isPlaying() || audioManager.isPaused())
                audioManager.changePositionByIncrement(-5);
        });

        pausePlayButton.setOnClickListener(view1 -> {
            if (audioManager.isPlaying())
                audioManager.pausePlaying();
            else if (audioManager.isPaused())
                audioManager.resumePlaying();
            else
                audioManager.startPlaying();
        });

        forward.setOnClickListener(view13 -> {
            if (audioManager.isPlaying() || audioManager.isPaused())
                audioManager.changePositionByIncrement(5);
        });

        return view;
    }

    private String getRecordingInfo() {
        String recordingLocation = "Audio Location:\n" + item.getAudioPath() + "\n\n";
        String recordingData = "Date Recorded: " + item.getDateCreated() + "\n\n";
        String recordingDuration = "Recording Duration: " +
                Helper.secondsToDurationText(item.getAudioDuration()) + "\n\n";
        String recordingSize = "Audio Size: " +
                Helper.getFormattedFileSize(getContext(), new File(item.getAudioPath()).length());

        return recordingLocation + recordingData + recordingDuration + recordingSize;
    }

    private void openMenuDialog() {
        audioMenu = new CustomPowerMenu.Builder<>(getContext(), new IconMenuAdapter(false))
                .addItem(new IconPowerMenuItem(getContext().getDrawable(R.drawable.delete_icon), "Delete"))
                .addItem(new IconPowerMenuItem(getContext().getDrawable(R.drawable.send_icon), "Send"))
                .setBackgroundColor(getContext().getResources().getColor(R.color.gray_100))
                .setOnMenuItemClickListener(onIconMenuItemClickListener)
                .setAnimation(MenuAnimation.SHOW_UP_CENTER)
                .setMenuRadius(15f)
                .setMenuShadow(10f)
                .build();
        audioMenu.showAsDropDown(dropDownMenu);
    }

    private final OnMenuItemClickListener<IconPowerMenuItem> onIconMenuItemClickListener = new OnMenuItemClickListener<IconPowerMenuItem>() {
        @Override
        public void onItemClick(int position, IconPowerMenuItem menuItem) {
            if (position == 0) {
                // delete audio
                RealmHelper.deleteRecording(item, getContext());
                Helper.showMessage(getActivity(), "Recording", "Deleted Recording", MotionToast.TOAST_SUCCESS);
                ((NoteEdit) getActivity()).checklistAdapter.notifyDataSetChanged();
                ((NoteEdit) getActivity()).updateDateEdited();
                dismiss();
            } else if (position == 1) {
                // send audio
                Helper.shareFile(getActivity(), "audio", "mp3", item.getAudioPath(), item.getText());
            }
            audioMenu.dismiss();
        }
    };

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        if (audioManager != null)
            audioManager.onStop();
        super.onDismiss(dialog);
    }

    @Override
    public int getTheme() {
        return UiHelper.getBottomSheetTheme(getContext());
    }

    @Override
    public void onViewCreated(@NotNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        BottomSheetDialog dialog = (BottomSheetDialog) getDialog();
        UiHelper.setBottomSheetBehavior(view, dialog);
    }

}