package com.mukesh.countrypicker;

import android.app.Dialog;
import android.view.View;
import android.widget.FrameLayout;
import com.github.mukeshsolanki.R;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

public class UiHelper {

    public static int getBottomSheetTheme(int theme) {
        if (theme == 1)
            return R.style.BaseBottomSheetDialogDark;
        else if (theme == 2)
            return R.style.BaseBottomSheetDialogGray;
        else
            return R.style.BaseBottomSheetDialogLight;
    }


    public static void setBottomSheetBehavior(View view, final Dialog dialog) {
        view.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            if (dialog != null) {
                FrameLayout bottomSheet = dialog.findViewById(R.id.design_bottom_sheet);
                if (bottomSheet != null) {
                    BottomSheetBehavior<FrameLayout> behavior = BottomSheetBehavior.from(bottomSheet);
                    behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                }
            }
        });
    }

}
