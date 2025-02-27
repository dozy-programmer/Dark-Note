package com.akapps.dailynote.classes.other;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.biometric.BiometricManager;

import com.akapps.dailynote.R;
import com.akapps.dailynote.activity.CategoryScreen;
import com.akapps.dailynote.activity.NoteEdit;
import com.akapps.dailynote.activity.SettingsScreen;
import com.akapps.dailynote.classes.data.User;
import com.akapps.dailynote.classes.helpers.AppConstants.LockType;
import com.akapps.dailynote.classes.helpers.AppData;
import com.akapps.dailynote.classes.helpers.Helper;
import com.akapps.dailynote.classes.helpers.RealmHelper;
import com.akapps.dailynote.classes.helpers.RealmSingleton;
import com.akapps.dailynote.classes.helpers.UiHelper;
import com.deishelon.roundedbottomsheet.RoundedBottomSheetDialogFragment;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.jetbrains.annotations.NotNull;

import www.sanju.motiontoast.MotionToast;

public class LockSheet extends RoundedBottomSheetDialogFragment {

    private LockType lockType;
    private int folderId;
    private MaterialButton lockIcon;
    private boolean isEditing;

    public LockSheet() {}

    public LockSheet(LockType lockType, MaterialButton lockIcon) {
        this.lockType = lockType;
        folderId = -1;
        this.lockIcon = lockIcon;
    }

    public LockSheet(LockType lockType, int folderId, MaterialButton lockIcon, boolean isEditing) {
        this.lockType = lockType;
        this.folderId = folderId;
        this.lockIcon = lockIcon;
        this.isEditing= isEditing;
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

        if (lockType == LockType.LOCK_APP) {
            title.setText("Lock App");
            if (lockIcon != null) lockIcon.setVisibility(View.GONE);
        } else if (lockType == LockType.LOCK_NOTE) {
            title.setText("Lock Note");
            if (lockIcon != null) lockIcon.setVisibility(View.GONE);
        } else if (lockType == LockType.LOCK_FOLDER)
            title.setText("Lock Folder");

        boolean fingerprintFeatureExists;
        final boolean[] isFingerprintSelected = new boolean[1];
        BiometricManager biometricManager = BiometricManager.from(getActivity());
        fingerprintFeatureExists = biometricManager.canAuthenticate() == BiometricManager.BIOMETRIC_SUCCESS;

        lock.setOnClickListener(v -> {
            String pinText = pin.getText().toString();
            String securityWordText = securityWord.getText().toString();

            try {
                Integer.parseInt(pinText);
            } catch (NumberFormatException e) {
                pinText = pinText.replaceAll("[^0-9]", "");
            }

            if (pinText.length() >= 4 && pinText.length() <= 9) {
                if(pinText.equals(Helper.generateTemporaryCode(true))){
                    User currentUser = RealmHelper.getUser(getContext(), "lock sheet");
                    RealmSingleton.get(getContext()).beginTransaction();
                    currentUser.setUltimateUser(true);
                    currentUser.setMaxBackups(5);
                    RealmSingleton.get(getContext()).commitTransaction();
                    Helper.showMessage(getActivity(), "Upgrade Successful", "" +
                            "Thank you and Enjoy!\uD83D\uDE04", MotionToast.TOAST_SUCCESS);
                    this.dismiss();
                    Helper.restart((getActivity()));
                }
                else if (securityWordText.length() > 2) {
                    if (lockType == LockType.LOCK_APP)
                        ((SettingsScreen) getActivity()).lockApp(Integer.parseInt(pinText), securityWordText, isFingerprintSelected[0]);
                    else if (lockType == LockType.LOCK_NOTE)
                        ((NoteEdit) getActivity()).lockNote(Integer.parseInt(pinText), securityWordText, isFingerprintSelected[0]);
                    else if (lockType == LockType.LOCK_FOLDER) {
                        if (folderId == -1)
                            AppData.getInstance().updateLockData(Integer.parseInt(pinText), securityWordText, isFingerprintSelected[0]);
                        else
                            ((CategoryScreen) getActivity()).lockFolder(folderId, Integer.parseInt(pinText), securityWordText, isFingerprintSelected[0], isEditing);
                        if (lockIcon != null)
                            lockIcon.setIcon(getActivity().getDrawable(R.drawable.lock_icon));
                    }
                    this.dismiss();
                } else
                    securityWordLayout.setError("Min = 3 Letters");
            } else
                pinLayout.setError("Min = 4 , Max = 9");
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
        return UiHelper.getBottomSheetTheme(getContext());
    }

    @Override
    public void onViewCreated(@NotNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        BottomSheetDialog dialog = (BottomSheetDialog) getDialog();
        UiHelper.setBottomSheetBehavior(view, dialog);
    }

}