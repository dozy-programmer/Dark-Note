package com.akapps.dailynote.classes.other;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.akapps.dailynote.R;
import com.akapps.dailynote.activity.NoteEdit;
import com.akapps.dailynote.classes.helpers.RealmHelper;
import com.akapps.dailynote.classes.helpers.UiHelper;
import com.deishelon.roundedbottomsheet.RoundedBottomSheetDialogFragment;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class CheckListHideSheet extends RoundedBottomSheetDialogFragment {

    private int currentSelected;
    private int noteId;

    public CheckListHideSheet() { }

    public CheckListHideSheet(int currentSelected, int noteId) {
        this.currentSelected = currentSelected;
        this.noteId = noteId;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_hide, container, false);

        MaterialButton confirmButton = view.findViewById(R.id.confirm_button);

        CheckBox unchecked = view.findViewById(R.id.unchecked_item);
        CheckBox checked = view.findViewById(R.id.checked_item);

        unchecked.setChecked(currentSelected <= 1);
        checked.setChecked(currentSelected % 2 == 0);

        confirmButton.setOnClickListener(view1 -> {
            Log.d("Here", "Selected -> " + getCurrentSelected(unchecked.isChecked(), checked.isChecked()));
            RealmHelper.getRealm(getContext()).beginTransaction();
            RealmHelper.getCurrentNote(getContext(), noteId).setVisibilityStatus(getCurrentSelected(unchecked.isChecked(), checked.isChecked()));
            RealmHelper.getRealm(getContext()).commitTransaction();
            dismiss();
        });

        return view;
    }

    /**
     * unchecked [ ] and checked [ ] 3
     * unchecked [ ] and checked [x] 2
     * unchecked [x] and checked [ ] 1
     * unchecked [x] and checked [x] 0
     */
    private int getCurrentSelected(boolean unChecked, boolean checked){
        if(!unChecked && !checked){
            return 3;
        }
        else if(!unChecked && checked){
            return 2;
        }
        else if(unChecked && !checked){
            return 1;
        }
        else {
            return 0;
        }
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