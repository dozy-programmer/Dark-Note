package com.akapps.dailynote.classes.other;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.akapps.dailynote.R;
import com.akapps.dailynote.classes.data.User;
import com.akapps.dailynote.classes.helpers.RealmSingleton;
import com.deishelon.roundedbottomsheet.RoundedBottomSheetDialogFragment;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import org.jetbrains.annotations.NotNull;

public class CreditsSheet extends RoundedBottomSheetDialogFragment {

    public CreditsSheet() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_credits, container, false);

        if (RealmSingleton.getUser().getScreenMode() == User.Mode.Dark)
            view.setBackgroundColor(getContext().getColor(R.color.darker_mode));
        else if (RealmSingleton.getUser().getScreenMode() == User.Mode.Gray)
            view.setBackgroundColor(getContext().getColor(R.color.gray));
        else if (RealmSingleton.getUser().getScreenMode() == User.Mode.Light) {

        }

        return view;
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