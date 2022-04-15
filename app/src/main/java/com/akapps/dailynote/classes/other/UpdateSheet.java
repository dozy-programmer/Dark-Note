package com.akapps.dailynote.classes.other;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.akapps.dailynote.R;
import com.akapps.dailynote.classes.helpers.AppData;
import com.deishelon.roundedbottomsheet.RoundedBottomSheetDialogFragment;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import org.jetbrains.annotations.NotNull;

public class UpdateSheet extends RoundedBottomSheetDialogFragment{


    public UpdateSheet(){}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_new_update, container, false);

        if (AppData.getAppData().isLightMode) {
            view.setBackgroundColor(getContext().getColor(R.color.light_mode));
            TextView title = view.findViewById(R.id.title);
            TextView appVersion = view.findViewById(R.id.app_version);
            TextView what_is_new_title = view.findViewById(R.id.new_update_title);
            TextView message = view.findViewById(R.id.message);
            title.setTextColor(getContext().getColor(R.color.light_gray));
            appVersion.setTextColor(getContext().getColor(R.color.light_gray));
            what_is_new_title.setTextColor(getContext().getColor(R.color.light_gray));
            message.setTextColor(getContext().getColor(R.color.light_gray));
        }
        else 
            view.setBackgroundColor(getContext().getColor(R.color.gray));

        return view;
    }

    @Override
    public int getTheme() {
        if(AppData.getAppData().isLightMode)
            return R.style.BaseBottomSheetDialogLight;
        else
            return R.style.BaseBottomSheetDialog;
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        this.dismiss();
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