package com.akapps.dailynote.classes.other;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import com.akapps.dailynote.R;
import com.akapps.dailynote.classes.helpers.Helper;
import com.akapps.dailynote.fragments.notes;
import com.deishelon.roundedbottomsheet.RoundedBottomSheetDialogFragment;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import org.jetbrains.annotations.NotNull;
import www.sanju.motiontoast.MotionToast;

public class FilterSheet extends RoundedBottomSheetDialogFragment{

    private Fragment fragment;
    private int resetCounter;

    private boolean note, checklist;
    private boolean isArchived, isPinned;
    private boolean createdDate, editedDate;
    private boolean oldestToLatest, latestToOldest;
    private boolean isDateCorrectlySelected = false;
    private boolean aToZ, zToA;
    private boolean isAlphabeticalChosen;
    private String kindSelected = "";
    private String dateTypeSelected = "";

    public FilterSheet(){ }

    public FilterSheet(Fragment fragment){
        this.fragment = fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_filter, container, false);

        view.setBackgroundColor(getContext().getColor(R.color.gray));

        ImageView closeFilter = view.findViewById(R.id.close_filter);
        TextView resetFilter = view.findViewById(R.id.reset_filter);
        MaterialButton confirmFilter = view.findViewById(R.id.confirm_filter);
        SwitchCompat saveSort = view.findViewById(R.id.save_sort);

        MaterialCardView noteButton = view.findViewById(R.id.note);
        MaterialCardView checklistButton = view.findViewById(R.id.checklist);
        ImageView noteIcon = view.findViewById(R.id.note_icon);
        ImageView checklistIcon = view.findViewById(R.id.checklist_icon);
        MaterialCardView archivedButton = view.findViewById(R.id.archived);
        MaterialCardView pinnedButton = view.findViewById(R.id.pinned);
        MaterialCardView noneButton = view.findViewById(R.id.none);
        MaterialCardView createdDateButton = view.findViewById(R.id.created_date);
        MaterialCardView editedDateButton = view.findViewById(R.id.edited_date);
        MaterialCardView oldToNewButton = view.findViewById(R.id.old_new);
        MaterialCardView newToOldButton = view.findViewById(R.id.new_old);
        MaterialCardView aToZButton = view.findViewById(R.id.a_z);
        MaterialCardView zToAButton = view.findViewById(R.id.z_a);
        MaterialCardView noSortButton = view.findViewById(R.id.no_sort);

        noteIcon.setColorFilter(getContext().getColor(R.color.darker_blue));
        checklistIcon.setColorFilter(getContext().getColor(R.color.darker_blue));

        noteButton.setOnClickListener(v -> {
            note = !note;
            if(note) {
                noteButton.setCardBackgroundColor(getContext().getColor(R.color.darker_blue));
                noteIcon.setColorFilter(getContext().getColor(R.color.black));
            }
            else {
                noteButton.setCardBackgroundColor(getContext().getColor(R.color.gray));
                noteIcon.setColorFilter(getContext().getColor(R.color.darker_blue));
            }
        });

        checklistButton.setOnClickListener(v -> {
            checklist = !checklist;
            if(checklist) {
                checklistButton.setCardBackgroundColor(getContext().getColor(R.color.darker_blue));
                checklistIcon.setColorFilter(getContext().getColor(R.color.black));
            }
            else {
                checklistButton.setCardBackgroundColor(getContext().getColor(R.color.gray));
                checklistIcon.setColorFilter(getContext().getColor(R.color.darker_blue));
            }
        });

        archivedButton.setOnClickListener(v -> {
            isArchived = !isArchived;
            noneButton.setCardBackgroundColor(getContext().getColor(R.color.gray));
            if(isArchived){
                archivedButton.setCardBackgroundColor(getContext().getColor(R.color.golden_rod));
                isPinned = false;
                pinnedButton.setCardBackgroundColor(getContext().getColor( R.color.gray));
            }
            else
                archivedButton.setCardBackgroundColor(getContext().getColor(R.color.gray));
        });

        pinnedButton.setOnClickListener(v -> {
            isPinned = !isPinned;
            noneButton.setCardBackgroundColor(getContext().getColor(R.color.gray));
            if(isPinned){
                pinnedButton.setCardBackgroundColor(getContext().getColor(R.color.golden_rod));
                isArchived = false;
                archivedButton.setCardBackgroundColor(getContext().getColor(R.color.gray));
            }
            else
                pinnedButton.setCardBackgroundColor(getContext().getColor(R.color.gray));
        });

        noneButton.setOnClickListener(v -> {
            isPinned = false;
            isArchived = false;
            noneButton.setCardBackgroundColor(getContext().getColor(R.color.golden_rod));
            archivedButton.setCardBackgroundColor(getContext().getColor(R.color.gray));
            pinnedButton.setCardBackgroundColor(getContext().getColor(R.color.gray));
        });

        createdDateButton.setOnClickListener(v -> {
            createdDate = !createdDate;
            unSelectAlphabetical(aToZButton, zToAButton, noSortButton);
            if(createdDate){
                createdDateButton.setCardBackgroundColor(getContext().getColor(R.color.darker_blue));
                editedDate = false;
                editedDateButton.setCardBackgroundColor(getContext().getColor(R.color.gray));
            }
            else
                createdDateButton.setCardBackgroundColor(getContext().getColor(R.color.gray));
        });

        editedDateButton.setOnClickListener(v -> {
            editedDate = !editedDate;
            unSelectAlphabetical(aToZButton, zToAButton, noSortButton);
            if(editedDate){
                editedDateButton.setCardBackgroundColor(getContext().getColor(R.color.darker_blue));
                createdDate = false;
                createdDateButton.setCardBackgroundColor(getContext().getColor(R.color.gray));
            }
            else
                editedDateButton.setCardBackgroundColor(getContext().getColor(R.color.gray));
        });

        oldToNewButton.setOnClickListener(v -> {
            oldestToLatest = !oldestToLatest;
            unSelectAlphabetical(aToZButton, zToAButton, noSortButton);
            if(oldestToLatest){
                oldToNewButton.setCardBackgroundColor(getContext().getColor(R.color.golden_rod));
                latestToOldest = false;
                newToOldButton.setCardBackgroundColor(getContext().getColor(R.color.gray));
            }
            else
                oldToNewButton.setCardBackgroundColor(getContext().getColor(R.color.gray));
        });

        newToOldButton.setOnClickListener(v -> {
            latestToOldest = !latestToOldest;
            unSelectAlphabetical(aToZButton, zToAButton, noSortButton);
            if(latestToOldest){
                newToOldButton.setCardBackgroundColor(getContext().getColor(R.color.golden_rod));
                oldestToLatest = false;
                oldToNewButton.setCardBackgroundColor(getContext().getColor(R.color.gray));
            }
            else
                newToOldButton.setCardBackgroundColor(getContext().getColor(R.color.gray));
        });

        aToZButton.setOnClickListener(v -> {
            aToZ = !aToZ;
            unSelectDate(oldToNewButton, newToOldButton, createdDateButton, editedDateButton, noSortButton);
            if(aToZ){
                aToZButton.setCardBackgroundColor(getContext().getColor(R.color.darker_blue));
                zToA = false;
                zToAButton.setCardBackgroundColor(getContext().getColor(R.color.gray));
            }
            else
                aToZButton.setCardBackgroundColor(getContext().getColor(R.color.gray));
        });

        zToAButton.setOnClickListener(v -> {
            zToA = !zToA;
            unSelectDate(oldToNewButton, newToOldButton, createdDateButton, editedDateButton, noSortButton);
            if(zToA){
                zToAButton.setCardBackgroundColor(getContext().getColor(R.color.darker_blue));
                aToZ = false;
                aToZButton.setCardBackgroundColor(getContext().getColor(R.color.gray));
            }
            else
                zToAButton.setCardBackgroundColor(getContext().getColor(R.color.gray));
        });

        noSortButton.setOnClickListener(v -> {
            unSelectDate(oldToNewButton, newToOldButton, createdDateButton, editedDateButton, noSortButton);
            unSelectAlphabetical(aToZButton, zToAButton, noSortButton);
            noSortButton.setCardBackgroundColor(getContext().getColor(R.color.darker_blue));
        });

        closeFilter.setOnClickListener(v -> {
            this.dismiss();
        });

        resetFilter.setOnClickListener(v -> {
            resetCounter++;
            if(resetCounter==2) {
                resetCounter = 0;
                saveSortData(false, false, null,
                        false, false, false, false, true);
                ((notes) fragment).getSortDataAndSort();
                this.dismiss();
                Helper.showMessage(getActivity(), "Cleared", "Sorting has been reset " +
                        "to default", MotionToast.TOAST_SUCCESS);
            }
            else
                Helper.showMessage(getActivity(), "Resetting...", "Press reset again to clear " +
                        "all saved sorting", MotionToast.TOAST_WARNING);
        });

        confirmFilter.setOnClickListener(v -> {
            if(isArchived)
                kindSelected = "archived";
            else if(isPinned)
                kindSelected = "pin";
            else
                kindSelected = "null";

            if(createdDate)
                dateTypeSelected = "dateCreated";
            else if(editedDate)
                dateTypeSelected = "dateEdited";
            else
                dateTypeSelected = "null";

            if(!dateTypeSelected.equals("null")){
                if(!oldestToLatest && !latestToOldest)
                    isDateCorrectlySelected = false;
                else
                    isDateCorrectlySelected = true;
            }
            else{
                if(oldestToLatest || latestToOldest)
                    isDateCorrectlySelected = false;
                else
                    isDateCorrectlySelected = true;
            }

            isAlphabeticalChosen = aToZ || zToA;

            if(isAlphabeticalChosen && (createdDate || editedDate || oldestToLatest || latestToOldest)){
                Helper.showMessage(getActivity(), "Error", "Choose to sort either by " +
                        "date or by alphabetical", MotionToast.TOAST_ERROR);
            }
            else if((note || checklist) && isDateCorrectlySelected) {

                if(saveSort.isChecked()){
                    if(isSelectedNotesCorrect() && (isAlphabeticalChosen ||
                            (!dateTypeSelected.equals("null") && isDateCorrectlySelected))) {
                        saveSortData(note, checklist, dateTypeSelected,
                                oldestToLatest, latestToOldest, aToZ, zToA, false);
                        this.dismiss();
                        ((notes) fragment).filterAndSortNotes(note, checklist, kindSelected,
                                dateTypeSelected, oldestToLatest, latestToOldest, aToZ, zToA);
                    }
                    else {
                        Helper.showMessage(getActivity(), "Save Sort Requirement", "Select Note & " +
                                "Checklist & sorting method", MotionToast.TOAST_ERROR);
                    }
                }
                else{
                    this.dismiss();
                    ((notes) fragment).filterAndSortNotes(note, checklist, kindSelected,
                            dateTypeSelected, oldestToLatest, latestToOldest, aToZ, zToA);
                }
            }
            else {
                if(!isDateCorrectlySelected)
                    Helper.showMessage(getActivity(), "Date Type", "Select the order of " +
                            "date type", MotionToast.TOAST_ERROR);
                else
                    Helper.showMessage(getActivity(), "**Required**", "Please select all " +
                        "note type(s) desired", MotionToast.TOAST_ERROR);
            }
        });

        return view;
    }

    private void saveSortData(boolean note, boolean checklist, String dateType, boolean oldestToNewest,
                              boolean newestToOldest, boolean aToZ, boolean zToA, boolean clearSortData){

        if(!clearSortData) {
            String noteSaving = "";
            if (note && checklist) {
                noteSaving = "notes";
                Helper.savePreference(getContext(), noteSaving, "notes_sort");
            }

            if (!dateType.equals("null")) {
                Helper.savePreference(getContext(), dateType, noteSaving + "_dateType");
                Helper.saveBooleanPreference(getContext(), oldestToNewest, noteSaving + "_oldestToNewest");
                Helper.saveBooleanPreference(getContext(), newestToOldest, noteSaving + "_newestToOldest");
                Helper.saveBooleanPreference(getContext(), false, noteSaving + "_aToZ");
                Helper.saveBooleanPreference(getContext(), false, noteSaving + "_zToA");
            } else {
                Helper.savePreference(getContext(), null, noteSaving + "_dateType");
                Helper.saveBooleanPreference(getContext(), false, noteSaving + "_oldestToNewest");
                Helper.saveBooleanPreference(getContext(), false, noteSaving + "_newestToOldest");
                Helper.saveBooleanPreference(getContext(), aToZ, noteSaving + "_aToZ");
                Helper.saveBooleanPreference(getContext(), zToA, noteSaving + "_zToA");
            }
        }
        else{
            // clear notes
            Helper.savePreference(getContext(), null, "notes_sort");
            Helper.savePreference(getContext(), null, "notes" + "_dateType");
            Helper.saveBooleanPreference(getContext(), false, "notes" + "_oldestToNewest");
            Helper.saveBooleanPreference(getContext(), false, "notes" + "_newestToOldest");
            Helper.saveBooleanPreference(getContext(), false, "notes" + "_aToZ");
            Helper.saveBooleanPreference(getContext(), false, "notes" + "_zToA");
        }
    }

    private void unSelectDate(MaterialCardView newToOldButton, MaterialCardView oldToNewButton,
                              MaterialCardView createdDateButton, MaterialCardView editedDateButton,
                              MaterialCardView noSortButton){
        oldestToLatest =  latestToOldest  = false;
        createdDate = editedDate = false;
        newToOldButton.setCardBackgroundColor(getContext().getColor(R.color.gray));
        oldToNewButton.setCardBackgroundColor(getContext().getColor(R.color.gray));
        createdDateButton.setCardBackgroundColor(getContext().getColor(R.color.gray));
        editedDateButton.setCardBackgroundColor(getContext().getColor(R.color.gray));
        noSortButton.setCardBackgroundColor(getContext().getColor(R.color.gray));
    }

    private void unSelectAlphabetical(MaterialCardView aZ, MaterialCardView zA, MaterialCardView noSortButton){
        zToA =  aToZ  = false;
        aZ.setCardBackgroundColor(getContext().getColor(R.color.gray));
        zA.setCardBackgroundColor(getContext().getColor(R.color.gray));
        noSortButton.setCardBackgroundColor(getContext().getColor(R.color.gray));
    }

    private boolean isSelectedNotesCorrect(){
        if(note && checklist)
            return true;
        else
            return false;
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        this.dismiss();
        super.onConfigurationChanged(newConfig);
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