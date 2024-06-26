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

    public static String themePreferenceKey = "theme";

    public static int color(Context context, int colorName) {
        return context.getColor(colorName);
    }

    public static User.Mode getTheme(Context context) {
        return RealmHelper.getUser(context, "get theme style").getScreenMode();
    }

    public static boolean getLightThemePreference(Context context) {
        return Helper.getBooleanPreference(context, themePreferenceKey);
    }

    public static void saveLightThemePreference(Context context, User.Mode newTheme) {
        Helper.saveBooleanPreference(context, newTheme == User.Mode.Light, themePreferenceKey);
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

    public static int getColorFromTheme(Activity activity, int colorAttr) {
        TypedValue typedValue = new TypedValue();
        activity.getTheme().resolveAttribute(colorAttr, typedValue, true);
        return ContextCompat.getColor(activity, typedValue.resourceId);
    }

    public static String getColorFromThemeHex(Activity activity, int colorAttr) {
        return String.format("#%06X", (0xFFFFFF & getColorFromTheme(activity, colorAttr)));
    }

    public static int getColorFromTheme(Context context, int colorAttr) {
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(colorAttr, typedValue, true);
        return ContextCompat.getColor(context, typedValue.resourceId);
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

    public static int getBottomSheetThemeInt(Context context) {
        User.Mode themeMode = RealmHelper.getUser(context, "bottom sheet").getScreenMode();
        if (themeMode == User.Mode.Dark)
            return 2;
        else if (themeMode == User.Mode.Gray)
            return 1;
        else if (themeMode == User.Mode.Light) {
            return 0;
        }
        return 2;
    }

    public static void setStatusBarColor(Activity activity) {
        activity.getWindow().setStatusBarColor(getColorFromTheme(activity, R.attr.primaryBackgroundColor));
        if (getLightThemePreference(activity))
            activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
    }

    public static void setStatusBarColor(Activity activity, int color) {
        activity.getWindow().setStatusBarColor(color);
        if (getLightThemePreference(activity))
            activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
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
