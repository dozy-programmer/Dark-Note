package com.akapps.dailynote.classes.other;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import com.akapps.dailynote.R;
import com.akapps.dailynote.activity.SettingsScreen;
import com.akapps.dailynote.classes.data.Backup;
import com.akapps.dailynote.classes.data.User;
import com.akapps.dailynote.classes.helpers.AppData;
import com.akapps.dailynote.classes.helpers.Helper;
import com.deishelon.roundedbottomsheet.RoundedBottomSheetDialogFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import org.jetbrains.annotations.NotNull;
import io.realm.Realm;
import www.sanju.motiontoast.MotionToast;

public class AccountSheet extends RoundedBottomSheetDialogFragment{

    // account authentication
    private FirebaseAuth mAuth;

    // variables
    private int loginAttempts;
    private final int maxLoginAttempts = 3;
    private BottomSheetDialog dialog;

    // layout
    private TextView title;
    private TextView forgotPassword;
    private TextInputLayout emailLayout;
    private TextInputEditText emailInput;
    private TextInputLayout passwordLayout;
    private TextInputEditText passwordInput;
    private FloatingActionButton loginButton;

    private Realm realm;
    private User currentUser;
    private boolean signUp;

    private FragmentActivity activity;

    public AccountSheet(FirebaseAuth mAuth, User currentUser, Realm realm, boolean signUp){
        this.mAuth = mAuth;
        this.currentUser = currentUser;
        this.realm = realm;
        this.signUp = signUp;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_account_login, container, false);

        activity = getActivity();

        // layout
        title = view.findViewById(R.id.title);
        forgotPassword = view.findViewById(R.id.forgot_password);
        emailLayout = view.findViewById(R.id.insert_email_layout);
        emailInput = view.findViewById(R.id.insert_email);
        passwordLayout = view.findViewById(R.id.insert_password_layout);
        passwordInput = view.findViewById(R.id.insert_password);
        loginButton = view.findViewById(R.id.login);

        if (AppData.getAppData().isLightMode) {
            emailLayout.setBoxBackgroundColor(getContext().getColor(R.color.light_mode));
            emailLayout.setHintTextColor(ColorStateList.valueOf(getContext().getColor(R.color.gray)));
            emailLayout.setDefaultHintTextColor(ColorStateList.valueOf(getContext().getColor(R.color.gray)));
            emailInput.setTextColor(getContext().getColor(R.color.gray));
            passwordLayout.setBoxBackgroundColor(getContext().getColor(R.color.light_mode));
            passwordLayout.setHintTextColor(ColorStateList.valueOf(getContext().getColor(R.color.gray)));
            passwordLayout.setDefaultHintTextColor(ColorStateList.valueOf(getContext().getColor(R.color.gray)));
            passwordInput.setTextColor(getContext().getColor(R.color.gray));
            view.setBackgroundColor(getContext().getColor(R.color.light_mode));
        }
        else
            view.setBackgroundColor(getContext().getColor(R.color.gray));

        if(!signUp)
            title.setText("Log in");
        else
            forgotPassword.setVisibility(View.INVISIBLE);

        loginButton.setOnClickListener(view1 -> getInput());

        forgotPassword.setOnClickListener(view12 -> {
            String inputEmail = emailInput.getText().toString();
            if(inputEmail.contains("@") && inputEmail.contains(".com")) {
                mAuth.sendPasswordResetEmail(inputEmail)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Helper.showMessage(activity, getContext().getString(R.string.password_reset_title),
                                        getContext().getString(R.string.password_reset_message),
                                        MotionToast.TOAST_SUCCESS);
                                dismiss();
                            } else {
                                Helper.showMessage(activity, "Reset Password",
                                        "Account does not exists or no internet connection",
                                        MotionToast.TOAST_ERROR);
                            }
                        });
            }
            else
                emailLayout.setError("Enter Email to Reset");
        });

        return view;
    }

    private void signUp(String email, String password){
        if(mAuth.getCurrentUser() == null){
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(activity, task2 -> {
                        if (task2.isSuccessful()) {
                            if (mAuth.getCurrentUser() != null) {
                                mAuth.getCurrentUser().sendEmailVerification()
                                        .addOnCompleteListener(task -> {
                                            FirebaseAuth.getInstance().signOut();
                                            if (task.isSuccessful()) {
                                                Helper.showMessage(activity, "Verify Email",
                                                        "Check your Inbox/Spam for email",
                                                        MotionToast.TOAST_SUCCESS);
                                            }
                                            else
                                                Helper.showMessage(activity, "Signing Up",
                                                        "Cannot Send Email, check internet connection",
                                                        MotionToast.TOAST_ERROR);
                                        });
                            }
                            dialog.dismiss();
                        } else {
                            Helper.showMessage(activity, "Signing Up",
                                    "Account exists or no internet connection",
                                    MotionToast.TOAST_ERROR);
                        }
                    });
        }
        else{
            Helper.showMessage(activity, "Error",
                    "Currently logged in",
                    MotionToast.TOAST_ERROR);
        }
    }

    private void login(String email, String password){
        if(loginAttempts <= maxLoginAttempts) {
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(activity, task -> {
                        if (task.isSuccessful()) {
                            if(mAuth.getCurrentUser().isEmailVerified()){
                                realm.beginTransaction();
                                currentUser.setEmail(email);
                                currentUser.setProUser(true);
                                realm.commitTransaction();
                                dialog.dismiss();
                                ((SettingsScreen) activity).restart();
                            }
                            else {
                                mAuth.getCurrentUser().sendEmailVerification()
                                        .addOnCompleteListener(task3 -> {
                                            if (task.isSuccessful())
                                                Helper.showMessage(activity, "Email Verification Sent",
                                                        "Check your Inbox/Spam and try again",
                                                        MotionToast.TOAST_ERROR);
                                            else
                                                Helper.showMessage(activity, "Signing Up",
                                                        "Cannot Send Email, check internet connection",
                                                        MotionToast.TOAST_ERROR);
                                            FirebaseAuth.getInstance().signOut();
                                        });
                            }
                        } else {
                            loginAttempts++;
                            // If sign in fails, display a message to the user.
                            Helper.showMessage(activity, getContext().getString(R.string.login_error_title),
                                    getContext().getString(R.string.login_error_message),
                                    MotionToast.TOAST_ERROR);
                        }
                    });
        }
        else
            Helper.showMessage(activity, getContext().getString(R.string.login_max_title),
                    getContext().getString(R.string.login_max_message),
                    MotionToast.TOAST_ERROR);
    }

    private void getInput(){
        String inputEmail = emailInput.getText().toString();
        String inputPassword = passwordInput.getText().toString();

        String freeUpgradeEmail = getString(R.string.upgrade_free_email);
        String passwordUpgradePassword = getString(R.string.upgrade_free_pass);

        if(inputEmail.toLowerCase().equals(freeUpgradeEmail) &&
                inputPassword.toLowerCase().equals(passwordUpgradePassword)){
            if(currentUser.isProUser()){
                Helper.showMessage(activity, "Whatcha Doing?",
                        "Buddy, you are already a pro user, thanks!",
                        MotionToast.TOAST_WARNING);
            }
            else {
                realm.beginTransaction();
                currentUser.setProUser(true);
                realm.commitTransaction();
                dialog.dismiss();
                ((SettingsScreen) activity).restart();
            }
        }
        else {
            if (!inputEmail.isEmpty() && inputEmail.contains("@") && inputEmail.contains(".com")) {
                emailLayout.setErrorEnabled(false);
                if (!inputPassword.isEmpty()) {
                    if (signUp)
                        signUp(inputEmail, inputPassword);
                    else
                        login(inputEmail, inputPassword);
                } else
                    passwordLayout.setError(getContext().getString(R.string.input_error));
            } else
                emailLayout.setError(getContext().getString(R.string.input_error));
        }
    }

    @Override
    public int getTheme() {
        if(AppData.getAppData().isLightMode)
            return R.style.BaseBottomSheetDialogLight;
        else
            return R.style.BaseBottomSheetDialog;
    }

    @Override
    public void onViewCreated(@NotNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.getViewTreeObserver()
                .addOnGlobalLayoutListener(() -> {
                    dialog =(BottomSheetDialog) getDialog ();
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