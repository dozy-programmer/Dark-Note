package com.akapps.dailynote.classes.other;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import com.akapps.dailynote.R;
import com.akapps.dailynote.classes.helpers.Helper;
import com.akapps.dailynote.classes.helpers.UiHelper;
import com.akapps.dailynote.fragments.notes;
import com.deishelon.roundedbottomsheet.RoundedBottomSheetDialogFragment;
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

    private int primaryButtonColor;
    private int secondaryButtonColor;
    private int backgroundColor;

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
        MaterialCardView createdDateButton = view.findViewById(R.id.created_date);
        MaterialCardView editedDateButton = view.findViewById(R.id.edited_date);
        MaterialCardView oldToNewButton = view.findViewById(R.id.old_new);
        MaterialCardView newToOldButton = view.findViewById(R.id.new_old);
        MaterialCardView aToZButton = view.findViewById(R.id.a_z);
        MaterialCardView zToAButton = view.findViewById(R.id.z_a);

        primaryButtonColor = UiHelper.getColorFromTheme(getActivity(), R.attr.primaryButtonColor);
        secondaryButtonColor = UiHelper.getColorFromTheme(getActivity(), R.attr.secondaryButtonColor);
        backgroundColor = UiHelper.getColorFromTheme(getActivity(), R.attr.primaryBackgroundColor);

        // set current filter
        String dateType = Helper.getPreference(getContext(), "_dateType");
        oldestToLatest = Helper.getBooleanPreference(getContext(), "_oldestToNewest");
        latestToOldest = Helper.getBooleanPreference(getContext(), "_newestToOldest");
        aToZ = Helper.getBooleanPreference(getContext(), "_aToZ");
        zToA = Helper.getBooleanPreference(getContext(), "_zToA");
        if (null != dateType) {
            if (dateType.equals("dateCreatedMilli")) {
                setColor(createdDateButton, primaryButtonColor);
                createdDate = true;
            }
            else if (dateType.equals("dateEditedMilli")) {
                setColor(editedDateButton, primaryButtonColor);
                editedDate = true;
            }

            if (oldestToLatest)
                setColor(oldToNewButton, secondaryButtonColor);
            else if (latestToOldest)
                setColor(newToOldButton, secondaryButtonColor);
        } else if (aToZ || zToA) {
            if (aToZ)
                setColor(aToZButton, primaryButtonColor);
            else
                setColor(zToAButton, primaryButtonColor);
        }

        createdDateButton.setOnClickListener(v -> {
            createdDate = !createdDate;
            unSelectAlphabetical(aToZButton, zToAButton);
            if (createdDate) {
                setColor(createdDateButton, primaryButtonColor);
                editedDate = false;
                setColor(editedDateButton, backgroundColor);
            } else
                setColor(createdDateButton, backgroundColor);
        });

        editedDateButton.setOnClickListener(v -> {
            editedDate = !editedDate;
            unSelectAlphabetical(aToZButton, zToAButton);
            if (editedDate) {
                setColor(editedDateButton, primaryButtonColor);
                createdDate = false;
                setColor(createdDateButton, backgroundColor);
            } else
                setColor(editedDateButton, backgroundColor);
        });

        oldToNewButton.setOnClickListener(v -> {
            oldestToLatest = !oldestToLatest;
            unSelectAlphabetical(aToZButton, zToAButton);
            if (oldestToLatest) {
                setColor(oldToNewButton, secondaryButtonColor);
                latestToOldest = false;
                setColor(newToOldButton, backgroundColor);
            } else
                setColor(oldToNewButton, backgroundColor);
        });

        newToOldButton.setOnClickListener(v -> {
            latestToOldest = !latestToOldest;
            unSelectAlphabetical(aToZButton, zToAButton);
            if (latestToOldest) {
                setColor(newToOldButton, secondaryButtonColor);
                oldestToLatest = false;
                setColor(oldToNewButton, backgroundColor);
            } else
                setColor(newToOldButton, backgroundColor);
        });

        aToZButton.setOnClickListener(v -> {
            aToZ = !aToZ;
            unSelectDate(oldToNewButton, newToOldButton, createdDateButton, editedDateButton);
            if (aToZ) {
                setColor(aToZButton, primaryButtonColor);
                zToA = false;
                setColor(zToAButton, backgroundColor);
            } else
                setColor(aToZButton, backgroundColor);
        });

        zToAButton.setOnClickListener(v -> {
            zToA = !zToA;
            unSelectDate(oldToNewButton, newToOldButton, createdDateButton, editedDateButton);
            if (zToA) {
                setColor(zToAButton, primaryButtonColor);
                aToZ = false;
                setColor(aToZButton, backgroundColor);
            } else
                setColor(zToAButton, backgroundColor);
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

    private void saveSortData(String dateType, boolean oldestToNewest, boolean newestToOldest,
                              boolean aToZ, boolean zToA, boolean clearSortData) {
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
        setColor(newToOldButton, backgroundColor);
        setColor(oldToNewButton, backgroundColor);
        setColor(createdDateButton, backgroundColor);
        setColor(editedDateButton, backgroundColor);
    }

    private void unSelectAlphabetical(MaterialCardView aZ, MaterialCardView zA) {
        zToA = aToZ = false;
        setColor(aZ, backgroundColor);
        setColor(zA, backgroundColor);
    }

    private void setColor(View view, int color) {
        ((MaterialCardView) view).setCardBackgroundColor(color);
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        this.dismiss();
        super.onConfigurationChanged(newConfig);
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