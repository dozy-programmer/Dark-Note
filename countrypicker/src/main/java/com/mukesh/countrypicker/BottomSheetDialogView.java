package com.mukesh.countrypicker;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.deishelon.roundedbottomsheet.RoundedBottomSheetDialogFragment;
import com.github.mukeshsolanki.R;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.mukesh.countrypicker.listeners.BottomSheetInteractionListener;

import org.jetbrains.annotations.NotNull;

public class BottomSheetDialogView extends RoundedBottomSheetDialogFragment {

    private BottomSheetInteractionListener listener;
    private final int theme;

    BottomSheetDialogView(int theme){
        this.theme = theme;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.country_picker, container, false);
    }

    @Override
    public int getTheme() {
        return UiHelper.getBottomSheetTheme(theme);
    }

    @Override
    public void onViewCreated(@NotNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Dialog dialog = getDialog();
        UiHelper.setBottomSheetBehavior(view, dialog);
        listener.initiateUi(view);
        listener.setCustomStyle(view);
        listener.setSearchEditText();
        listener.setupRecyclerView(view);
    }

    @Override
    public void onDestroy() {
        listener = null;
        super.onDestroy();
    }

    public void setListener(BottomSheetInteractionListener listener) {
        this.listener = listener;
    }

}
