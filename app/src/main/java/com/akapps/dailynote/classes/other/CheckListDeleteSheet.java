package com.akapps.dailynote.classes.other;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.akapps.dailynote.R;
import com.akapps.dailynote.activity.NoteEdit;
import com.akapps.dailynote.classes.helpers.UiHelper;
import com.deishelon.roundedbottomsheet.RoundedBottomSheetDialogFragment;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class CheckListDeleteSheet extends RoundedBottomSheetDialogFragment {

    public CheckListDeleteSheet() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_delete, container, false);

        TextView title = view.findViewById(R.id.title);
        MaterialButton confirmButton = view.findViewById(R.id.confirm_button);
        RadioGroup selectedOption = view.findViewById(R.id.delete_items_radio_layout);
        AtomicInteger counter = new AtomicInteger();
        AtomicInteger isSelected = new AtomicInteger();
        AtomicReference<RadioButton> selected = new AtomicReference<>();

        selectedOption.setOnCheckedChangeListener((radioGroup, radioId) -> {
            isSelected.set(1);
            selected.set(view.findViewById(radioId));
        });

        confirmButton.setOnClickListener(view1 -> {
            if (isSelected.get() == 0) {
                title.setText("Please Select One to Delete");
            } else if (counter.get() == 0) {
                confirmButton.setStrokeColor(ColorStateList.valueOf(
                        UiHelper.getColorFromTheme(getActivity(), R.attr.tertiaryButtonColor)
                ));
                title.setText("Are you sure you want to delete?");
                confirmButton.setText("YES");
                counter.getAndIncrement();
            } else {
                String selectedText = selected.get().getText().toString().replace("Items", "").trim().toLowerCase();
                ((NoteEdit) getActivity()).deleteChecklist(selectedText);
                dismiss();
            }
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