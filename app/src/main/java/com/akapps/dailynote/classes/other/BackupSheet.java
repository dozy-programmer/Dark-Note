package com.akapps.dailynote.classes.other;

import android.Manifest;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.biometric.BiometricManager;

import com.akapps.dailynote.R;
import com.akapps.dailynote.activity.NoteEdit;
import com.akapps.dailynote.activity.SettingsScreen;
import com.akapps.dailynote.classes.helpers.Helper;
import com.akapps.dailynote.classes.helpers.UiHelper;
import com.deishelon.roundedbottomsheet.RoundedBottomSheetDialogFragment;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.jetbrains.annotations.NotNull;

import www.sanju.motiontoast.MotionToast;

public class BackupSheet extends RoundedBottomSheetDialogFragment {

    public BackupSheet() { }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_backup, container, false);

        TextView info = view.findViewById(R.id.info);
        MaterialButton confirmButton = view.findViewById(R.id.confirm_button);
        SwitchCompat includeImageSwitch = view.findViewById(R.id.include_image_switch);
        SwitchCompat includeAudioSwitch = view.findViewById(R.id.include_audio_switch);

        StringBuilder backupMessage = new StringBuilder();
        backupMessage
                .append("Backup to Google Drive, OneDrive, and more.")
                .append("\n\n")
                .append("Google Drive or Files App are recommended.");

        info.setText(backupMessage);

        confirmButton.setOnClickListener(view1 -> {
            boolean includeImage = includeImageSwitch.isChecked();
            boolean includeAudio = includeAudioSwitch.isChecked();
            ((SettingsScreen) requireActivity()).backup(includeImage, includeAudio);
            dismiss();
        });

        return view;
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