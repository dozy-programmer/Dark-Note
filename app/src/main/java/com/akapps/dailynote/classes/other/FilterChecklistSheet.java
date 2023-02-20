package com.akapps.dailynote.classes.other;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.akapps.dailynote.R;
import com.akapps.dailynote.activity.NoteEdit;
import com.akapps.dailynote.classes.data.Note;
import com.akapps.dailynote.classes.helpers.AppData;
import com.deishelon.roundedbottomsheet.RoundedBottomSheetDialogFragment;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import org.jetbrains.annotations.NotNull;
import io.realm.Realm;

public class FilterChecklistSheet extends RoundedBottomSheetDialogFragment{

    private Realm realm;
    private Note currentNote;

    public FilterChecklistSheet(){}

    public FilterChecklistSheet(Realm realm, Note currentNote){
        this.realm = realm;
        this.currentNote = currentNote;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_checklist_filter, container, false);

        MaterialCardView background = view.findViewById(R.id.background);
        MaterialCardView aZ = view.findViewById(R.id.a_z);
        MaterialCardView zA = view.findViewById(R.id.z_a);
        MaterialCardView checkedBottom = view.findViewById(R.id.checked_bottom);
        MaterialCardView checkedTop = view.findViewById(R.id.checked_top);
        MaterialCardView addedBottom = view.findViewById(R.id.add_bottom);
        MaterialCardView addedTop = view.findViewById(R.id.add_top);
        ImageView clearFilter = view.findViewById(R.id.clear_filter);
        CheckBox applyAll = view.findViewById(R.id.apply_all);

        if (AppData.getAppData().isDarkerMode) {
            view.setBackgroundColor(getContext().getColor(R.color.darker_mode));
            background.setCardBackgroundColor(getContext().getColor(R.color.darker_mode));
            background.setStrokeColor(getContext().getColor(R.color.gray));
            background.setStrokeWidth(8);

            darkModeItemLayout(aZ);
            darkModeItemLayout(zA);
            darkModeItemLayout(checkedBottom);
            darkModeItemLayout(checkedTop);
            darkModeItemLayout(addedBottom);
            darkModeItemLayout(addedTop);
        }
        else
            view.setBackgroundColor(getContext().getColor(R.color.gray));

        int sort = 0;
        try {
            sort = currentNote.getSort();
        }catch (Exception e) {
            this.dismiss();
        }

        if (sort == 1) {
            aZ.setCardBackgroundColor(getContext().getColor(R.color.havelock_blue));
            aZ.setStrokeColor(getContext().getColor(R.color.havelock_blue));
        }
        else if (sort == 2) {
            zA.setCardBackgroundColor(getContext().getColor(R.color.havelock_blue));
            zA.setStrokeColor(getContext().getColor(R.color.havelock_blue));
        }
        else if (sort == 4) {
            checkedBottom.setCardBackgroundColor(getContext().getColor(R.color.havelock_blue));
            checkedBottom.setStrokeColor(getContext().getColor(R.color.havelock_blue));
        }
        else if (sort == 3) {
            checkedTop.setCardBackgroundColor(getContext().getColor(R.color.havelock_blue));
            checkedTop.setStrokeColor(getContext().getColor(R.color.havelock_blue));
        }
        else if (sort == 5) {
            addedBottom.setCardBackgroundColor(getContext().getColor(R.color.havelock_blue));
            addedBottom.setStrokeColor(getContext().getColor(R.color.havelock_blue));
        }
        else if (sort == 6) {
            addedTop.setCardBackgroundColor(getContext().getColor(R.color.havelock_blue));
            addedTop.setStrokeColor(getContext().getColor(R.color.havelock_blue));
        }

        /** sorting
         if no sort or cleared, then it is -1
         if aToZ 1
         if zToA 2
         if checked top 3
         if checked bottom 4
         if added bottom 5
         if added top 6
         **/

        aZ.setOnClickListener(v -> {
            if(applyAll.isChecked()){
                realm.beginTransaction();
                realm.where(Note.class).findAll().setInt("sort", 1);
                realm.commitTransaction();
            }
            else{
                realm.beginTransaction();
                currentNote.setSort(1);
                realm.commitTransaction();
            }
            ((NoteEdit) getActivity()).sortChecklist();
            this.dismiss();
        });

        zA.setOnClickListener(v -> {
            if(applyAll.isChecked()){
                realm.beginTransaction();
                realm.where(Note.class).findAll().setInt("sort", 2);
                realm.commitTransaction();
            }
            else{
                realm.beginTransaction();
                currentNote.setSort(2);
                realm.commitTransaction();
            }
            ((NoteEdit) getActivity()).sortChecklist();
            this.dismiss();
        });

        checkedBottom.setOnClickListener(v -> {
            if(applyAll.isChecked()){
                realm.beginTransaction();
                realm.where(Note.class).findAll().setInt("sort", 4);
                realm.commitTransaction();
            }
            else{
                realm.beginTransaction();
                currentNote.setSort(4);
                realm.commitTransaction();
            }
            ((NoteEdit) getActivity()).sortChecklist();
            this.dismiss();
        });

        checkedTop.setOnClickListener(v -> {
            if(applyAll.isChecked()){
                realm.beginTransaction();
                realm.where(Note.class).findAll().setInt("sort", 3);
                realm.commitTransaction();
            }
            else{
                realm.beginTransaction();
                currentNote.setSort(3);
                realm.commitTransaction();
            }
            ((NoteEdit) getActivity()).sortChecklist();
            this.dismiss();
        });

        addedBottom.setOnClickListener(v -> {
            if(applyAll.isChecked()){
                realm.beginTransaction();
                realm.where(Note.class).findAll().setInt("sort", 5);
                realm.commitTransaction();
            }
            else{
                realm.beginTransaction();
                currentNote.setSort(5);
                realm.commitTransaction();
            }
            ((NoteEdit) getActivity()).sortChecklist();
            this.dismiss();
        });

        addedTop.setOnClickListener(v -> {
            if(applyAll.isChecked()){
                realm.beginTransaction();
                realm.where(Note.class).findAll().setInt("sort", 6);
                realm.commitTransaction();
            }
            else{
                realm.beginTransaction();
                currentNote.setSort(6);
                realm.commitTransaction();
            }
            ((NoteEdit) getActivity()).sortChecklist();
            this.dismiss();
        });

        clearFilter.setOnClickListener(v -> {
            if(applyAll.isChecked()){
                realm.beginTransaction();
                realm.where(Note.class).findAll().setInt("sort", 5);
                realm.commitTransaction();
            }
            else{
                realm.beginTransaction();
                currentNote.setSort(5);
                realm.commitTransaction();
            }
            ((NoteEdit) getActivity()).sortChecklist();
            this.dismiss();
        });

        return view;
    }

    private void darkModeItemLayout(MaterialCardView view){
        view.setCardBackgroundColor(getContext().getColor(R.color.darker_mode));
        view.setStrokeColor(getContext().getColor(R.color.gray));
        view.setStrokeWidth(8);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public int getTheme() {
        if(AppData.getAppData().isDarkerMode)
            return R.style.BaseBottomSheetDialogLight;
        else
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