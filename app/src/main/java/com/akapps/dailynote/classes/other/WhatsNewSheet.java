package com.akapps.dailynote.classes.other;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import com.akapps.dailynote.R;
import com.akapps.dailynote.classes.helpers.RealmHelper;
import com.akapps.dailynote.classes.helpers.UiHelper;
import com.akapps.dailynote.fragments.notes;
import com.deishelon.roundedbottomsheet.RoundedBottomSheetDialogFragment;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import org.jetbrains.annotations.NotNull;

public class WhatsNewSheet extends RoundedBottomSheetDialogFragment {

    private Fragment fragmentActivity;

    public WhatsNewSheet() {
    }

    public WhatsNewSheet(Fragment fragmentActivity) {
        this.fragmentActivity = fragmentActivity;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_whats_new, container, false);

        SwitchCompat moveInfoToBottom = view.findViewById(R.id.move_info_icon_switch);
        moveInfoToBottom.setOnCheckedChangeListener((compoundButton, checked) -> {
            RealmHelper.getRealm(getContext()).beginTransaction();
            RealmHelper.getUser(getContext(), "Whats New").setShowPreviewNoteInfoAtBottom(checked);
            RealmHelper.getRealm(getContext()).commitTransaction();
            if (fragmentActivity != null)
                ((notes) fragmentActivity).showDefaultSort();
        });
        moveInfoToBottom.performClick();

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