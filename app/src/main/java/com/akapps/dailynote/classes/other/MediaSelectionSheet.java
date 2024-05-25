package com.akapps.dailynote.classes.other;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.akapps.dailynote.R;
import com.akapps.dailynote.activity.NoteEdit;
import com.akapps.dailynote.classes.helpers.UiHelper;
import com.akapps.dailynote.classes.other.insertsheet.InsertImageSheet;
import com.deishelon.roundedbottomsheet.RoundedBottomSheetDialogFragment;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;

import org.jetbrains.annotations.NotNull;

public class MediaSelectionSheet extends RoundedBottomSheetDialogFragment {

    private int action;

    private InsertImageSheet imageSheet;
    private ChecklistItemSheet checklistItemSheet;

    public MediaSelectionSheet() { }

    public MediaSelectionSheet(int action) {
        this.action = action;
    }

    public MediaSelectionSheet(InsertImageSheet imageSheet) {
        this.imageSheet = imageSheet;
        this.action = 1;
    }

    public MediaSelectionSheet(ChecklistItemSheet checklistItemSheet) {
        this.checklistItemSheet = checklistItemSheet;
        this.action = 2;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_select_image_type, container, false);

        MaterialButton galleryButton = view.findViewById(R.id.gallery_media);
        MaterialButton cameraButton = view.findViewById(R.id.camera_media);

        galleryButton.setOnClickListener(view1 -> openSelectedBottomSheet(0));
        cameraButton.setOnClickListener(view1 -> openSelectedBottomSheet(1));

        return view;
    }

    private void openSelectedBottomSheet(int selection) {
        switch (action) {
            case 0:
                // NoteEdit
                ((NoteEdit) getActivity()).openMediaSelected(selection);
                break;
            case 1:
                imageSheet.openMediaSelect(selection);
                break;
            case 2:
                checklistItemSheet.openMediaSelect(selection);
                break;
        }
        dismiss();
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