package com.akapps.dailynote.classes.other.insertsheet;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.akapps.dailynote.R;
import com.akapps.dailynote.activity.NoteEdit;
import com.akapps.dailynote.classes.helpers.UiHelper;
import com.deishelon.roundedbottomsheet.RoundedBottomSheetDialogFragment;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;

import org.jetbrains.annotations.NotNull;

public class MediaTypeSheet extends RoundedBottomSheetDialogFragment {

    public MediaTypeSheet() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_select_media_type, container, false);

        MaterialButton linkImage = view.findViewById(R.id.image_media);
        MaterialButton linkMedia = view.findViewById(R.id.link_media);

        linkImage.setOnClickListener(view1 -> openSelectedBottomSheet(0));
        linkMedia.setOnClickListener(view1 -> openSelectedBottomSheet(1));

        return view;
    }

    private void openSelectedBottomSheet(int selection) {
        switch (selection) {
            case 0:
                ((NoteEdit) getActivity()).showLockScreen = false;
                InsertImageSheet insertImageSheet = new InsertImageSheet();
                insertImageSheet.show(getActivity().getSupportFragmentManager(), insertImageSheet.getTag());
                break;
            case 1:
                InsertLinkSheet insertLinkSheet = new InsertLinkSheet();
                insertLinkSheet.show(getActivity().getSupportFragmentManager(), insertLinkSheet.getTag());
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