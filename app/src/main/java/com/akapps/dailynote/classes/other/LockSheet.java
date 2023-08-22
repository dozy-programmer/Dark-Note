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
import com.akapps.dailynote.activity.SettingsScreen;
import com.akapps.dailynote.classes.data.User;
import com.akapps.dailynote.classes.helpers.Helper;
import com.akapps.dailynote.classes.helpers.RealmSingleton;
import com.deishelon.roundedbottomsheet.RoundedBottomSheetDialogFragment;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.jetbrains.annotations.NotNull;

import www.sanju.motiontoast.MotionToast;

public class LockSheet extends RoundedBottomSheetDialogFragment {

    private boolean isAppLock;

    public LockSheet() {
    }

    public LockSheet(boolean isAppLock) {
        this.isAppLock = isAppLock;
    }

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

        if (isAppLock)
            title.setText("Lock App");
        else
            title.setText("Lock Note");

        if (RealmSingleton.getUser().getScreenMode() == User.Mode.Dark) {
            pinLayout.setBoxBackgroundColor(getContext().getColor(R.color.darker_mode));
            pinLayout.setHintTextColor(ColorStateList.valueOf(getContext().getColor(R.color.light_light_gray)));
            pinLayout.setDefaultHintTextColor(ColorStateList.valueOf(getContext().getColor(R.color.ultra_white)));
            pin.setTextColor(getContext().getColor(R.color.ultra_white));
            securityWordLayout.setBoxBackgroundColor(getContext().getColor(R.color.darker_mode));
            securityWordLayout.setHintTextColor(ColorStateList.valueOf(getContext().getColor(R.color.ultra_white)));
            securityWordLayout.setDefaultHintTextColor(ColorStateList.valueOf(getContext().getColor(R.color.ultra_white)));
            securityWord.setTextColor(getContext().getColor(R.color.ultra_white));
            view.setBackgroundColor(getContext().getColor(R.color.darker_mode));
        } else if (RealmSingleton.getUser().getScreenMode() == User.Mode.Gray)
            view.setBackgroundColor(getContext().getColor(R.color.gray));
        else if (RealmSingleton.getUser().getScreenMode() == User.Mode.Light) {

        }

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

            if (pinText.length() >= 4 && pinText.length() <= 10) {
                if (securityWordText.length() > 0) {
                    if (isAppLock)
                        ((SettingsScreen) getActivity()).lockNote(Integer.parseInt(pinText), securityWordText, isFingerprintSelected[0]);
                    else
                        ((NoteEdit) getActivity()).lockNote(Integer.parseInt(pinText), securityWordText, isFingerprintSelected[0]);
                    this.dismiss();
                } else
                    securityWordLayout.setError("Required");
            } else
                pinLayout.setError("Min = 4 , Max = 10");
        });

        fingerprint.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                if (!fingerprintFeatureExists) {
                    buttonView.setChecked(false);
                    Helper.showMessage(getActivity(), "Error", "Not Supported by this device",
                            MotionToast.TOAST_ERROR);
                } else
                    isFingerprintSelected[0] = true;
            } else {
                isFingerprintSelected[0] = false;
            }
        });

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