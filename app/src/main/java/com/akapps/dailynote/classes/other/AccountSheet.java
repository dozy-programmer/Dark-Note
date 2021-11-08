package com.akapps.dailynote.classes.other;

import android.net.Uri;
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
import com.akapps.dailynote.classes.data.User;
import com.akapps.dailynote.classes.helpers.Helper;
import com.deishelon.roundedbottomsheet.RoundedBottomSheetDialogFragment;
import com.google.android.gms.auth.api.signin.internal.Storage;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Random;
import java.util.concurrent.Executor;

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
    private TextInputLayout emailLayout;
    private TextInputEditText emailInput;
    private TextInputLayout passwordLayout;
    private TextInputEditText passwordInput;
    private FloatingActionButton loginButton;

    private Realm realm;
    private User currentUser;
    private boolean signUp;

    public AccountSheet(FirebaseAuth mAuth, User currentUser, Realm realm, boolean signUp){
        this.mAuth = mAuth;
        this.currentUser = currentUser;
        this.realm = realm;
        this.signUp = signUp;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_account_login, container, false);
        view.setBackgroundColor(requireContext().getColor(R.color.gray));

        // layout
        title = view.findViewById(R.id.title);
        emailLayout = view.findViewById(R.id.insert_email_layout);
        emailInput = view.findViewById(R.id.insert_email);
        passwordLayout = view.findViewById(R.id.insert_password_layout);
        passwordInput = view.findViewById(R.id.insert_password);
        loginButton = view.findViewById(R.id.login);

        if(!signUp){
            title.setText("Log in");
        }

        loginButton.setOnClickListener(view1 -> getInput());

        return view;
    }

    private void signUp(String email, String password){
        if (mAuth.getCurrentUser() == null) {
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(getActivity(), task -> {
                        if (task.isSuccessful()) {
                            realm.beginTransaction();
                            currentUser.setEmail(email);
                            currentUser.setPassword(password);
                            realm.commitTransaction();
                            Helper.showMessage(getActivity(), getContext().getString(R.string.signed_up_success_title),
                                    getContext().getString(R.string.signed_up_success_message),
                                    MotionToast.TOAST_SUCCESS);
                            dialog.dismiss();
                            ((SettingsScreen) getActivity()).restart();
                        } else {
                            Helper.showMessage(getActivity(), "Signing Up",
                                    "Issue signing up, try again",
                                    MotionToast.TOAST_ERROR);
                        }
                    });
        }
        else{
            Helper.showMessage(getActivity(), "Error",
                    "Currently logged in",
                    MotionToast.TOAST_ERROR);
        }
    }

    private void login(String email, String password){
        if(loginAttempts <= maxLoginAttempts) {
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(getActivity(), (OnCompleteListener<AuthResult>) task -> {
                        if (task.isSuccessful()) {
                            dialog.dismiss();
                            ((SettingsScreen) getActivity()).restart();
                        } else {
                            loginAttempts++;
                            // If sign in fails, display a message to the user.
                            Helper.showMessage(getActivity(), getContext().getString(R.string.login_error_title),
                                    getContext().getString(R.string.login_error_message),
                                    MotionToast.TOAST_ERROR);
                        }
                    });
        }
        else
            Helper.showMessage(getActivity(), getContext().getString(R.string.login_max_title),
                    getContext().getString(R.string.login_max_message),
                    MotionToast.TOAST_ERROR);
    }

    private void getInput(){
        String inputEmail = emailInput.getText().toString();
        String inputPassword = passwordInput.getText().toString();
        if(!inputEmail.isEmpty() && inputEmail.contains("@") &&
                inputEmail.contains(".com")){
            emailLayout.setErrorEnabled(false);
            if(!inputPassword.isEmpty()) {
                if(signUp)
                    signUp(inputEmail, inputPassword);
                else
                    login(inputEmail, inputPassword);
            }
            else
                passwordLayout.setError(getContext().getString(R.string.input_error));
        }
        else
            emailLayout.setError(getContext().getString(R.string.input_error));
    }

    @Override
    public int getTheme() {
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