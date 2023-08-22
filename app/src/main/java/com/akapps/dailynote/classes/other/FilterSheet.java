package com.akapps.dailynote.classes.other;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import com.akapps.dailynote.R;
import com.akapps.dailynote.classes.data.User;
import com.akapps.dailynote.classes.helpers.Helper;
import com.akapps.dailynote.classes.helpers.RealmSingleton;
import com.akapps.dailynote.fragments.notes;
import com.deishelon.roundedbottomsheet.RoundedBottomSheetDialogFragment;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import org.jetbrains.annotations.NotNull;

import www.sanju.motiontoast.MotionToast;

public class FilterSheet extends RoundedBottomSheetDialogFragment {

    private Fragment fragment;
    private int resetCounter;

    private boolean createdDate, editedDate;
    private boolean oldestToLatest, latestToOldest;
    private boolean isDateCorrectlySelected = false;
    private boolean aToZ, zToA;
    private boolean isAlphabeticalChosen;
    private String dateTypeSelected = "";

    public FilterSheet() {
    }

    public FilterSheet(Fragment fragment) {
        this.fragment = fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_filter, container, false);

        ImageView resetFilter = view.findViewById(R.id.reset_filter);
        MaterialButton confirmFilter = view.findViewById(R.id.confirm_filter);
        SwitchCompat saveSort = view.findViewById(R.id.save_sort);
        MaterialCardView sortByDate = view.findViewById(R.id.sort_by_date);
        MaterialCardView sortByAlphabetical = view.findViewById(R.id.alphabetical_sort);
        MaterialCardView createdDateButton = view.findViewById(R.id.created_date);
        MaterialCardView editedDateButton = view.findViewById(R.id.edited_date);
        MaterialCardView oldToNewButton = view.findViewById(R.id.old_new);
        MaterialCardView newToOldButton = view.findViewById(R.id.new_old);
        MaterialCardView aToZButton = view.findViewById(R.id.a_z);
        MaterialCardView zToAButton = view.findViewById(R.id.z_a);

        if (RealmSingleton.getUser().getScreenMode() == User.Mode.Dark) {
            view.setBackgroundColor(getContext().getColor(R.color.darker_mode));
            sortByDate.setCardBackgroundColor(getContext().getColor(R.color.darker_mode));
            sortByDate.setStrokeColor(getContext().getColor(R.color.light_gray_2));
            sortByDate.setStrokeWidth(5);
            sortByAlphabetical.setCardBackgroundColor(getContext().getColor(R.color.darker_mode));
            sortByAlphabetical.setStrokeColor(getContext().getColor(R.color.light_gray_2));
            sortByAlphabetical.setStrokeWidth(5);
        } else if (RealmSingleton.getUser().getScreenMode() == User.Mode.Gray)
            view.setBackgroundColor(getContext().getColor(R.color.gray));
        else if (RealmSingleton.getUser().getScreenMode() == User.Mode.Light) {

        }

        // set current filter
        String dateType = Helper.getPreference(getContext(), "_dateType");
        boolean oldestToNewest = Helper.getBooleanPreference(getContext(), "_oldestToNewest");
        boolean newestToOldest = Helper.getBooleanPreference(getContext(), "_newestToOldest");

        boolean aToZSaved = Helper.getBooleanPreference(getContext(), "_aToZ");
        boolean zToASaved = Helper.getBooleanPreference(getContext(), "_zToA");


        if (null != dateType) {
            if (dateType.equals("dateCreatedMilli"))
                createdDateButton.setCardBackgroundColor(getContext().getColor(R.color.darker_blue));
            else if (dateType.equals("dateEditedMilli"))
                editedDateButton.setCardBackgroundColor(getContext().getColor(R.color.darker_blue));


            if (oldestToNewest)
                oldToNewButton.setCardBackgroundColor(getContext().getColor(R.color.golden_rod));
            else if (newestToOldest)
                newToOldButton.setCardBackgroundColor(getContext().getColor(R.color.golden_rod));
        } else if (aToZSaved || zToASaved) {
            if (aToZSaved)
                aToZButton.setCardBackgroundColor(getContext().getColor(R.color.darker_blue));
            else if (zToASaved)
                zToAButton.setCardBackgroundColor(getContext().getColor(R.color.darker_blue));
        }

        createdDateButton.setOnClickListener(v -> {
            createdDate = !createdDate;
            unSelectAlphabetical(aToZButton, zToAButton);
            if (createdDate) {
                createdDateButton.setCardBackgroundColor(getContext().getColor(R.color.darker_blue));
                editedDate = false;
                editedDateButton.setCardBackgroundColor(getContext().getColor(R.color.gray));
            } else
                createdDateButton.setCardBackgroundColor(getContext().getColor(R.color.gray));
        });

        editedDateButton.setOnClickListener(v -> {
            editedDate = !editedDate;
            unSelectAlphabetical(aToZButton, zToAButton);
            if (editedDate) {
                editedDateButton.setCardBackgroundColor(getContext().getColor(R.color.darker_blue));
                createdDate = false;
                createdDateButton.setCardBackgroundColor(getContext().getColor(R.color.gray));
            } else
                editedDateButton.setCardBackgroundColor(getContext().getColor(R.color.gray));
        });

        oldToNewButton.setOnClickListener(v -> {
            oldestToLatest = !oldestToLatest;
            unSelectAlphabetical(aToZButton, zToAButton);
            if (oldestToLatest) {
                oldToNewButton.setCardBackgroundColor(getContext().getColor(R.color.golden_rod));
                latestToOldest = false;
                newToOldButton.setCardBackgroundColor(getContext().getColor(R.color.gray));
            } else
                oldToNewButton.setCardBackgroundColor(getContext().getColor(R.color.gray));
        });

        newToOldButton.setOnClickListener(v -> {
            latestToOldest = !latestToOldest;
            unSelectAlphabetical(aToZButton, zToAButton);
            if (latestToOldest) {
                newToOldButton.setCardBackgroundColor(getContext().getColor(R.color.golden_rod));
                oldestToLatest = false;
                oldToNewButton.setCardBackgroundColor(getContext().getColor(R.color.gray));
            } else
                newToOldButton.setCardBackgroundColor(getContext().getColor(R.color.gray));
        });

        aToZButton.setOnClickListener(v -> {
            aToZ = !aToZ;
            unSelectDate(oldToNewButton, newToOldButton, createdDateButton, editedDateButton);
            if (aToZ) {
                aToZButton.setCardBackgroundColor(getContext().getColor(R.color.darker_blue));
                zToA = false;
                zToAButton.setCardBackgroundColor(getContext().getColor(R.color.gray));
            } else
                aToZButton.setCardBackgroundColor(getContext().getColor(R.color.gray));
        });

        zToAButton.setOnClickListener(v -> {
            zToA = !zToA;
            unSelectDate(oldToNewButton, newToOldButton, createdDateButton, editedDateButton);
            if (zToA) {
                zToAButton.setCardBackgroundColor(getContext().getColor(R.color.darker_blue));
                aToZ = false;
                aToZButton.setCardBackgroundColor(getContext().getColor(R.color.gray));
            } else
                zToAButton.setCardBackgroundColor(getContext().getColor(R.color.gray));
        });

        resetFilter.setOnClickListener(v -> {
            resetCounter++;
            if (resetCounter == 2) {
                resetCounter = 0;
                saveSortData(null, false, false, false, false, true);
                ((notes) fragment).showDefaultSort();
                this.dismiss();
                Helper.showMessage(getActivity(), "Cleared", "Sorting has been reset " +
                        "to default", MotionToast.TOAST_SUCCESS);
            } else
                Helper.showMessage(getActivity(), "Resetting...", "Press reset again to clear " +
                        "all saved sorting", MotionToast.TOAST_WARNING);
        });

        confirmFilter.setOnClickListener(v -> {
            if (createdDate)
                dateTypeSelected = "dateCreatedMilli";
            else if (editedDate)
                dateTypeSelected = "dateEditedMilli";
            else
                dateTypeSelected = "null";

            if (!dateTypeSelected.equals("null")) {
                isDateCorrectlySelected = oldestToLatest || latestToOldest;
            } else {
                isDateCorrectlySelected = !oldestToLatest && !latestToOldest;
            }

            isAlphabeticalChosen = aToZ || zToA;

            if (isAlphabeticalChosen && (createdDate || editedDate || oldestToLatest || latestToOldest)) {
                Helper.showMessage(getActivity(), "Error", "Choose to sort either by " +
                        "date or by alphabetical", MotionToast.TOAST_ERROR);
            } else if (isDateCorrectlySelected) {

                if (saveSort.isChecked()) {
                    if (isAlphabeticalChosen || (!dateTypeSelected.equals("null") && isDateCorrectlySelected)) {
                        saveSortData(dateTypeSelected,
                                oldestToLatest, latestToOldest, aToZ, zToA, false);
                        this.dismiss();
                        ((notes) fragment).filterAndSortNotes(dateTypeSelected, oldestToLatest,
                                latestToOldest, aToZ, zToA);
                    } else {
                        Helper.showMessage(getActivity(), "Save Sort Requirement", "Select Note & " +
                                "Checklist & sorting method", MotionToast.TOAST_ERROR);
                    }
                } else {
                    this.dismiss();
                    ((notes) fragment).filterAndSortNotes(dateTypeSelected, oldestToLatest,
                            latestToOldest, aToZ, zToA);
                }
            } else {
                if (!isDateCorrectlySelected)
                    Helper.showMessage(getActivity(), "Date Type", "Select the order of " +
                            "date type", MotionToast.TOAST_ERROR);
                else
                    Helper.showMessage(getActivity(), "**Required**", "Please select all " +
                            "note type(s) desired", MotionToast.TOAST_ERROR);
            }
        });

        return view;
    }

    private void saveSortData(String dateType, boolean oldestToNewest,
                              boolean newestToOldest, boolean aToZ, boolean zToA, boolean clearSortData) {

        if (!clearSortData) {
            if (!dateType.equals("null")) {
                Helper.savePreference(getContext(), dateType, "_dateType");
                Helper.saveBooleanPreference(getContext(), oldestToNewest, "_oldestToNewest");
                Helper.saveBooleanPreference(getContext(), newestToOldest, "_newestToOldest");
                Helper.saveBooleanPreference(getContext(), false, "_aToZ");
                Helper.saveBooleanPreference(getContext(), false, "_zToA");
            } else {
                Helper.savePreference(getContext(), null, "_dateType");
                Helper.saveBooleanPreference(getContext(), false, "_oldestToNewest");
                Helper.saveBooleanPreference(getContext(), false, "_newestToOldest");
                Helper.saveBooleanPreference(getContext(), aToZ, "_aToZ");
                Helper.saveBooleanPreference(getContext(), zToA, "_zToA");
            }
        } else {
            // clear notes
            Helper.savePreference(getContext(), null, "_dateType");
            Helper.saveBooleanPreference(getContext(), false, "_oldestToNewest");
            Helper.saveBooleanPreference(getContext(), false, "_newestToOldest");
            Helper.saveBooleanPreference(getContext(), false, "_aToZ");
            Helper.saveBooleanPreference(getContext(), false, "_zToA");
        }
    }

    private void unSelectDate(MaterialCardView newToOldButton, MaterialCardView oldToNewButton,
                              MaterialCardView createdDateButton, MaterialCardView editedDateButton) {
        oldestToLatest = latestToOldest = false;
        createdDate = editedDate = false;
        newToOldButton.setCardBackgroundColor(getContext().getColor(R.color.gray));
        oldToNewButton.setCardBackgroundColor(getContext().getColor(R.color.gray));
        createdDateButton.setCardBackgroundColor(getContext().getColor(R.color.gray));
        editedDateButton.setCardBackgroundColor(getContext().getColor(R.color.gray));
    }

    private void unSelectAlphabetical(MaterialCardView aZ, MaterialCardView zA) {
        zToA = aToZ = false;
        aZ.setCardBackgroundColor(getContext().getColor(R.color.gray));
        zA.setCardBackgroundColor(getContext().getColor(R.color.gray));
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        this.dismiss();
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public int getTheme() {
        if (RealmSingleton.getUser().getScreenMode() == User.Mode.Dark)
            return R.style.BaseBottomSheetDialogLight;
        else if (RealmSingleton.getUser().getScreenMode() == User.Mode.Gray)
            return R.style.BaseBottomSheetDialog;
        else if (RealmSingleton.getUser().getScreenMode() == User.Mode.Light) {
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