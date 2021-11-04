package com.akapps.dailynote.classes.other;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.akapps.dailynote.R;
import com.akapps.dailynote.activity.NoteEdit;
import com.akapps.dailynote.classes.helpers.Helper;
import com.akapps.dailynote.fragments.notes;
import com.deishelon.roundedbottomsheet.RoundedBottomSheetDialogFragment;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import org.jetbrains.annotations.NotNull;

public class FilterChecklistSheet extends RoundedBottomSheetDialogFragment{
    public FilterChecklistSheet(){
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_checklist_filter, container, false);

        view.setBackgroundColor(getContext().getColor(R.color.gray));

        MaterialCardView aZ = view.findViewById(R.id.a_z);
        MaterialCardView zA = view.findViewById(R.id.z_a);
        MaterialCardView checkedBottom = view.findViewById(R.id.checked_bottom);
        MaterialCardView checkedTop = view.findViewById(R.id.checked_top);
        MaterialButton clearFilter = view.findViewById(R.id.clear_filter);

        String currentSelection = Helper.getPreference(getContext(), "order");

        if(currentSelection != null) {
            if (currentSelection.equals("1"))
                aZ.setCardBackgroundColor(getContext().getColor(R.color.havelock_blue));
            else if (currentSelection.equals("2"))
                zA.setCardBackgroundColor(getContext().getColor(R.color.havelock_blue));
            else if (currentSelection.equals("3"))
                checkedBottom.setCardBackgroundColor(getContext().getColor(R.color.havelock_blue));
            else if (currentSelection.equals("4"))
                checkedTop.setCardBackgroundColor(getContext().getColor(R.color.havelock_blue));
        }

        aZ.setOnClickListener(v -> {
            Helper.savePreference(getContext(), "1", "order");
            ((NoteEdit) getActivity()).sortEnable = false;
            ((NoteEdit) getActivity()).sortChecklist();
            this.dismiss();
        });

        zA.setOnClickListener(v -> {
            Helper.savePreference(getContext(), "2", "order");
            ((NoteEdit) getActivity()).sortEnable = false;
            ((NoteEdit) getActivity()).sortChecklist();
            this.dismiss();
        });

        checkedBottom.setOnClickListener(v -> {
            Helper.savePreference(getContext(), "3", "order");
            ((NoteEdit) getActivity()).sortEnable = false;
            ((NoteEdit) getActivity()).sortChecklist();
            this.dismiss();
        });

        checkedTop.setOnClickListener(v -> {
            Helper.savePreference(getContext(), "4", "order");
            ((NoteEdit) getActivity()).sortEnable = false;
            ((NoteEdit) getActivity()).sortChecklist();
            this.dismiss();
        });

        clearFilter.setOnClickListener(v -> {
            Helper.savePreference(getContext(), "", "order");
            ((NoteEdit) getActivity()).sortEnable = true;
            ((NoteEdit) getActivity()).sortChecklist();
            this.dismiss();
        });

        return view;
    }

    @Override
    public int getTheme() {
        return R.style.BaseBottomSheetDialog;
    }

    @Override
    public void onViewCreated(@NotNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.getViewTreeObserver()
                .addOnGlobalLayoutListener(() -> {
                    BottomSheetDialog dialog =(BottomSheetDialog) getDialog ();
                    if (dialog != null) {
                        FrameLayout bottomSheet = dialog.findViewById (R.id.design_bottom_sheet);
                        if (bottomSheet != null) {
                            BottomSheetBehavior behavior = BottomSheetBehavior.from (bottomSheet);
                            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                        }
                    }
                });
    }

}