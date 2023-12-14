package com.akapps.dailynote.classes.helpers;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.util.TypedValue;
import android.view.View;
import android.widget.FrameLayout;

import androidx.core.content.ContextCompat;

import com.akapps.dailynote.R;
import com.akapps.dailynote.classes.data.User;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

public class UiHelper {

    public static User.Mode getThemeMode(Context context) {
        return RealmHelper.getUser(context, "getting theme").getScreenMode();
    }

    public static int getThemeStyle(Context context) {
        User.Mode themeMode = RealmHelper.getUser(context, "get theme style").getScreenMode();
        if (themeMode == User.Mode.Dark)
            return R.style.Theme_DailyDay_Dark;
        else if (themeMode == User.Mode.Gray)
            return R.style.Theme_DailyDay_Gray;
        else if (themeMode == User.Mode.Light) {
            return R.style.Theme_DailyDay_Light;
        }
        return 0;
    }

    public static int getColorFromTheme(Activity activity, int colorAttr){
        TypedValue typedValue = new TypedValue();
        activity.getTheme().resolveAttribute(colorAttr, typedValue, true);
        return ContextCompat.getColor(activity, typedValue.resourceId);
    }

    public static void setStatusBarColor(Activity activity){
        activity.getWindow().setStatusBarColor(getColorFromTheme(activity, R.attr.secondaryBackgroundColor));
    }

    public static int getBottomSheetTheme(Context context) {
        User.Mode themeMode = RealmHelper.getUser(context, "bottom sheet").getScreenMode();
        if (themeMode == User.Mode.Dark)
            return R.style.BaseBottomSheetDialogDark;
        else if (themeMode == User.Mode.Gray)
            return R.style.BaseBottomSheetDialogGray;
        else if (themeMode == User.Mode.Light) {
            return R.style.BaseBottomSheetDialogLight;
        }
        return 0;
    }

    public static void setBottomSheetBehavior(View view, final Dialog dialog) {
        view.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
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
