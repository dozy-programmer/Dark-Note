package com.akapps.dailynote.classes.other;

import android.content.res.ColorStateList;
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
import androidx.biometric.BiometricManager;
import com.akapps.dailynote.R;
import com.akapps.dailynote.activity.NoteEdit;
import com.akapps.dailynote.classes.helpers.AppData;
import com.akapps.dailynote.classes.helpers.Helper;
import com.deishelon.roundedbottomsheet.RoundedBottomSheetDialogFragment;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import org.jetbrains.annotations.NotNull;
import www.sanju.motiontoast.MotionToast;

public class LockSheet extends RoundedBottomSheetDialogFragment{

    public LockSheet(){}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_lock_unlock, container, false);

        ImageView lock = view.findViewById(R.id.lock);
        TextInputLayout pinLayout = view.findViewById(R.id.pin_layout);
        TextInputEditText pin = view.findViewById(R.id.pin);
        TextInputLayout securityWordLayout = view.findViewById(R.id.security_word_layout);
        TextInputEditText securityWord = view.findViewById(R.id.security_word);
        SwitchCompat fingerprint = view.findViewById(R.id.fingerprint);
        TextView title = view.findViewById(R.id.title);

        if (AppData.getAppData().isLightTheme) {
            pinLayout.setBoxBackgroundColor(getContext().getColor(R.color.light_mode));
            pinLayout.setHintTextColor(ColorStateList.valueOf(getContext().getColor(R.color.light_gray)));
            pinLayout.setDefaultHintTextColor(ColorStateList.valueOf(getContext().getColor(R.color.light_gray)));
            pin.setTextColor(getContext().getColor(R.color.gray));
            securityWordLayout.setBoxBackgroundColor(getContext().getColor(R.color.light_mode));
            securityWordLayout.setHintTextColor(ColorStateList.valueOf(getContext().getColor(R.color.light_gray)));
            securityWordLayout.setDefaultHintTextColor(ColorStateList.valueOf(getContext().getColor(R.color.light_gray)));
            securityWord.setTextColor(getContext().getColor(R.color.gray));
            view.setBackgroundColor(getContext().getColor(R.color.light_mode));
        }
        else
            view.setBackgroundColor(getContext().getColor(R.color.gray));

        boolean fingerprintFeatureExists;
        final boolean[] isFingerprintSelected = new boolean[1];
        BiometricManager biometricManager = BiometricManager.from(getActivity());
        switch (biometricManager.canAuthenticate()) {
            case BiometricManager.BIOMETRIC_SUCCESS:
                    fingerprintFeatureExists = true;
                break;
            default:
                fingerprintFeatureExists = false;
        }

        lock.setOnClickListener(v -> {
            String pinText = pin.getText().toString();
            String securityWordText = securityWord.getText().toString();

            if(pinText.length()>=4 && pinText.length()<=10){
                if(securityWordText.length()>0){
                   ((NoteEdit) getActivity()).lockNote(Integer.parseInt(pinText), securityWordText, isFingerprintSelected[0]);
                    this.dismiss();
                }
                else
                    securityWordLayout.setError("Required");
            }
            else
                pinLayout.setError("Min = 4 , Max = 10");
        });

        fingerprint.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(isChecked){
                if(!fingerprintFeatureExists) {
                    buttonView.setChecked(false);
                    Helper.showMessage(getActivity(), "Error", "Not Supported by this device",
                            MotionToast.TOAST_ERROR);
                }
                else
                    isFingerprintSelected[0] = true;
            }
            else{
                isFingerprintSelected[0] = false;
            }
        });

        return view;
    }

    @Override
    public int getTheme() {
        if(AppData.getAppData().isLightTheme)
            return R.style.BaseBottomSheetDialogLight;
        else
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