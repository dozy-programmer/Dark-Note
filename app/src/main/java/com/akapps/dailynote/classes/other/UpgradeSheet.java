package com.akapps.dailynote.classes.other;

import android.content.res.ColorStateList;
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
import com.akapps.dailynote.activity.SettingsScreen;
import com.akapps.dailynote.classes.helpers.AppData;
import com.deishelon.roundedbottomsheet.RoundedBottomSheetDialogFragment;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import org.jetbrains.annotations.NotNull;

public class UpgradeSheet extends RoundedBottomSheetDialogFragment{

    // layout
    private TextView one;
    private TextView one_sub;
    private TextView two;
    private TextView two_sub;
    private TextView three;
    private TextView three_sub;
    private TextView four;
    private TextView four_sub;
    private TextView five;
    private TextView five_sub;
    private TextView six;
    private TextView seven;

    public UpgradeSheet(){}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_upgrade, container, false);

        one = view.findViewById(R.id.title);
        two = view.findViewById(R.id.one);
        three = view.findViewById(R.id.two);
        four = view.findViewById(R.id.three);
        five = view.findViewById(R.id.four);
        six = view.findViewById(R.id.five);
        one_sub = view.findViewById(R.id.one_sub);
        two_sub = view.findViewById(R.id.two_sub);
        three_sub = view.findViewById(R.id.three_sub);
        four_sub = view.findViewById(R.id.four_sub);
        five_sub = view.findViewById(R.id.five_sub);
        seven = view.findViewById(R.id.bottom_message);


        if (AppData.getAppData().isLightTheme)
            view.setBackgroundColor(getContext().getColor(R.color.light_mode));
        else {
            view.setBackgroundColor(getContext().getColor(R.color.gray));
            updateLayoutColors();
        }

        MaterialButton upgradeToPro = view.findViewById(R.id.upgrade_to_pro);

        upgradeToPro.setOnClickListener(view1 -> ((SettingsScreen) getActivity()).buyApp());

        return view;
    }

    private void updateLayoutColors(){
        // main-text
        one.setTextColor(getContext().getColor(R.color.ultra_white));
        two.setTextColor(getContext().getColor(R.color.ultra_white));
        three.setTextColor(getContext().getColor(R.color.ultra_white));
        four.setTextColor(getContext().getColor(R.color.ultra_white));
        five.setTextColor(getContext().getColor(R.color.ultra_white));
        six.setTextColor(getContext().getColor(R.color.ultra_white));
        seven.setTextColor(getContext().getColor(R.color.ultra_white));
        // sub-text
        one_sub.setTextColor(getContext().getColor(R.color.greyish));
        two_sub.setTextColor(getContext().getColor(R.color.greyish));
        three_sub.setTextColor(getContext().getColor(R.color.greyish));
        four_sub.setTextColor(getContext().getColor(R.color.greyish));
        five_sub.setTextColor(getContext().getColor(R.color.greyish));
    }

    @Override
    public int getTheme() {
        if(AppData.getAppData().isLightTheme)
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