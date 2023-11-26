package com.akapps.dailynote.classes.other;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.akapps.dailynote.R;
import com.akapps.dailynote.activity.NoteEdit;
import com.akapps.dailynote.adapter.IconMenuAdapter;
import com.akapps.dailynote.classes.data.CheckListItem;
import com.akapps.dailynote.classes.data.User;
import com.akapps.dailynote.classes.helpers.AppData;
import com.akapps.dailynote.classes.helpers.AudioManager;
import com.akapps.dailynote.classes.helpers.Helper;
import com.akapps.dailynote.classes.helpers.RealmHelper;
import com.akapps.dailynote.classes.helpers.RealmSingleton;
import com.deishelon.roundedbottomsheet.RoundedBottomSheetDialogFragment;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
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

        if (RealmSingleton.getUser(getContext()).getScreenMode() == User.Mode.Dark) {
            view.setBackgroundColor(getContext().getColor(R.color.darker_mode));
            infoRecording.setBackgroundColor(getContext().getColor(R.color.not_too_dark_gray));
        } else if (RealmSingleton.getUser(getContext()).getScreenMode() == User.Mode.Gray)
            view.setBackgroundColor(getContext().getColor(R.color.gray));
        else if (RealmSingleton.getUser(getContext()).getScreenMode() == User.Mode.Light) {

        }

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
                .setBackgroundColor(getContext().getColor(R.color.light_gray))
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
                Helper.shareFile(getContext(), "audio", item.getAudioPath(), item.getText());
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
        if (RealmSingleton.getUser(getContext()).getScreenMode() == User.Mode.Dark)
            return R.style.BaseBottomSheetDialogLight;
        else if (RealmSingleton.getUser(getContext()).getScreenMode() == User.Mode.Gray)
            return R.style.BaseBottomSheetDialog;
        else if (RealmSingleton.getUser(getContext()).getScreenMode() == User.Mode.Light) {
        }
        return 0;
    }

    @Override
    public void onViewCreated(@NotNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.getViewTreeObserver()
                .addOnGlobalLayoutListener(() -> {
                    BottomSheetDialog dialog = (BottomSheetDialog) getDialog();
                    if (dialog != null) {
                        FrameLayout bottomSheet = dialog.findViewById(R.id.design_bottom_sheet);
                        if (bottomSheet != null) {
                            BottomSheetBehavior behavior = BottomSheetBehavior.from(bottomSheet);
                            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                        }
                    }
                });
    }

}