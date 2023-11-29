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
import com.akapps.dailynote.classes.data.User;
import com.akapps.dailynote.classes.helpers.RealmHelper;
import com.akapps.dailynote.classes.helpers.RealmSingleton;
import com.deishelon.roundedbottomsheet.RoundedBottomSheetDialogFragment;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import org.jetbrains.annotations.NotNull;
import io.realm.Realm;

public class FilterChecklistSheet extends RoundedBottomSheetDialogFragment {

    private Note currentNote;
    private User.Mode screenMode;

    // layout
    private MaterialCardView aZ;
    private MaterialCardView zA;
    private MaterialCardView checkedBottom;
    private MaterialCardView checkedTop;
    private MaterialCardView addedBottom;
    private MaterialCardView addedTop;

    public FilterChecklistSheet() {
    }

    public FilterChecklistSheet(Note currentNote) {
        this.currentNote = currentNote;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_checklist_filter, container, false);

        MaterialCardView background = view.findViewById(R.id.background);
        aZ = view.findViewById(R.id.a_z);
        zA = view.findViewById(R.id.z_a);
        checkedBottom = view.findViewById(R.id.checked_bottom);
        checkedTop = view.findViewById(R.id.checked_top);
        addedBottom = view.findViewById(R.id.add_bottom);
        addedTop = view.findViewById(R.id.add_top);
        ImageView clearFilter = view.findViewById(R.id.clear_filter);
        CheckBox applyAll = view.findViewById(R.id.apply_all);
        MaterialButton sortButton = view.findViewById(R.id.sort_button);

        screenMode = RealmHelper.getUser(getContext(), "bottom sheet").getScreenMode();

        if (screenMode == User.Mode.Dark) {
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
        } else if (screenMode == User.Mode.Gray)
            view.setBackgroundColor(getContext().getColor(R.color.gray));
        else if (RealmHelper.getUser(getContext(), "bottom sheet").getScreenMode() == User.Mode.Light) {

        }

        int sort = 0;
        try {
            sort = currentNote.getSort();
        } catch (Exception e) {
            this.dismiss();
        }

        if (sort == 1) {
            selectView(aZ, true);
        } else if (sort == 2) {
            selectView(zA, true);
        } else if (sort == 4) {
        selectView(checkedBottom, true);
        } else if (sort == 3) {
            selectView(checkedTop, true);
        } else if (sort == 5) {
            selectView(addedBottom, true);
        } else if (sort == 6) {
            selectView(addedTop, true);
        }
        else if (sort == 7) {
            selectView(aZ, true);
            selectView(checkedTop, true);
        }
        else if (sort == 8) {
            selectView(aZ, true);
            selectView(checkedBottom, true);
        }
        else if (sort == 9) {
            selectView(zA, true);
            selectView(checkedTop, true);
        }
        else if (sort == 10) {
            selectView(zA, true);
            selectView(checkedBottom, true);
        }

        /** sorting
         if no sort or cleared, then it is -1
         if aToZ 1
         if zToA 2
         if checked top 3
         if checked bottom 4
         if added bottom 5
         if added top 6
         if aToZ + checked top = 7
         if aToZ + checked bottom = 8
         if zToA + checked top = 9
         if zToA + checked bottom = 10
         **/

        aZ.setOnClickListener(v -> {
            unSelectAll(false);
            if(isViewSelected(zA)){
                selectView(zA, false);
            }

            selectView(aZ, !isViewSelected(aZ));
        });

        zA.setOnClickListener(v -> {
            unSelectAll(false);
            if(isViewSelected(aZ)){
                selectView(aZ, false);
            }

            selectView(zA, !isViewSelected(zA));
        });

        checkedBottom.setOnClickListener(v -> {
            unSelectAll(false);
            if(isViewSelected(checkedTop)){
                selectView(checkedTop, false);
            }

            selectView(checkedBottom, !isViewSelected(checkedBottom));
        });

        checkedTop.setOnClickListener(v -> {
            unSelectAll(false);
            if(isViewSelected(checkedBottom)){
                selectView(checkedBottom, false);
            }

            selectView(checkedTop, !isViewSelected(checkedTop));
        });

        addedBottom.setOnClickListener(v -> {
            boolean isCurrentlySelected = isViewSelected(addedBottom);
            unSelectAll(true);
            selectView(addedBottom, !isCurrentlySelected);
        });

        addedTop.setOnClickListener(v -> {
            boolean isCurrentlySelected= isViewSelected(addedTop);
            unSelectAll(true);
            selectView(addedTop, !isCurrentlySelected);
        });

        clearFilter.setOnClickListener(v -> {
            if (applyAll.isChecked()) {
                getRealm().beginTransaction();
                getRealm().where(Note.class).findAll().setInt("sort", 5);
                getRealm().commitTransaction();
            } else {
                getRealm().beginTransaction();
                currentNote.setSort(5);
                getRealm().commitTransaction();
            }
            ((NoteEdit) getActivity()).sortChecklist();
            this.dismiss();
        });

        sortButton.setOnClickListener(view1 -> {
            int selectedSort = getSelectedSort();
            if (applyAll.isChecked()) {
                getRealm().beginTransaction();
                getRealm().where(Note.class).findAll().setInt("sort", selectedSort);
                getRealm().commitTransaction();
            } else {
                getRealm().beginTransaction();
                currentNote.setSort(selectedSort);
                getRealm().commitTransaction();
            }
            ((NoteEdit) getActivity()).sortChecklist();
            dismiss();
        });

        return view;
    }

    private boolean isViewSelected(MaterialCardView view){
        int selectionColor = getContext().getColor(R.color.havelock_blue);
        return view.getCardBackgroundColor().getDefaultColor() == selectionColor ||
                view.getStrokeColorStateList().getDefaultColor() == selectionColor;
    }

    private void selectView(MaterialCardView view, boolean select){
        int selectionColor = getContext().getColor(R.color.havelock_blue);
        if(screenMode == User.Mode.Dark){
            if(select)
                view.setStrokeColor(selectionColor);
            else
                darkModeItemLayout(view);
        }
        else if(screenMode == User.Mode.Gray){
            if(select) {
                view.setCardBackgroundColor(selectionColor);
                view.setStrokeColor(selectionColor);
            }
            else
                grayModeItemLayout(view);
        }
        else {

        }
    }

    private void grayModeItemLayout(MaterialCardView view) {
        view.setCardBackgroundColor(getContext().getColor(R.color.gray));
        view.setStrokeColor(getContext().getColor(R.color.gray));
        view.setStrokeWidth(8);
    }

    private void darkModeItemLayout(MaterialCardView view) {
        view.setCardBackgroundColor(getContext().getColor(R.color.darker_mode));
        view.setStrokeColor(getContext().getColor(R.color.gray));
        view.setStrokeWidth(8);
    }

    private void unSelectAll(boolean deselectAll){
        if(screenMode == User.Mode.Dark){
            if(deselectAll) {
                darkModeItemLayout(aZ);
                darkModeItemLayout(zA);
                darkModeItemLayout(checkedTop);
                darkModeItemLayout(checkedBottom);
            }
            darkModeItemLayout(addedTop);
            darkModeItemLayout(addedBottom);
        }
        else if(screenMode == User.Mode.Gray){
            if(deselectAll) {
                grayModeItemLayout(aZ);
                grayModeItemLayout(zA);
                grayModeItemLayout(checkedTop);
                grayModeItemLayout(checkedBottom);
            }
            grayModeItemLayout(addedTop);
            grayModeItemLayout(addedBottom);
        }
        else{

        }
    }

    private int getSelectedSort(){
        if(isViewSelected(aZ) && isViewSelected(checkedTop)){
            return 7;
        }
        else if(isViewSelected(aZ) && isViewSelected(checkedBottom)){
            return 8;
        }
        else if(isViewSelected(aZ)){
            return 1;
        }
        else if(isViewSelected(zA) && isViewSelected(checkedTop)){
            return 9;
        }
        else if(isViewSelected(zA) && isViewSelected(checkedBottom)){
            return 10;
        }
        else if(isViewSelected(zA)){
            return 2;
        }
        else if(isViewSelected(checkedTop)){
            return 3;
        }
        else if(isViewSelected(checkedBottom)){
            return 4;
        }
        else if(isViewSelected(addedTop)){
            return 6;
        }
        else if(isViewSelected(addedBottom)){
            return 5;
        }

        return -1;
    }

    private Realm getRealm(){
        return RealmSingleton.getInstance(getContext());
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public int getTheme() {
        if (RealmHelper.getUser(getContext(), "bottom sheet").getScreenMode() == User.Mode.Dark)
            return R.style.BaseBottomSheetDialogLight;
        else if (RealmHelper.getUser(getContext(), "bottom sheet").getScreenMode() == User.Mode.Gray)
            return R.style.BaseBottomSheetDialog;
        else if (RealmHelper.getUser(getContext(), "bottom sheet").getScreenMode() == User.Mode.Light) {
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