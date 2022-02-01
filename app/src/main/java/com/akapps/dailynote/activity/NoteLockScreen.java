package com.akapps.dailynote.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.akapps.dailynote.R;
import com.akapps.dailynote.classes.helpers.Helper;
import com.akapps.dailynote.classes.other.InfoSheet;
import com.andrognito.pinlockview.IndicatorDots;
import com.andrognito.pinlockview.PinLockListener;
import com.andrognito.pinlockview.PinLockView;
import java.util.concurrent.Executor;
import www.sanju.motiontoast.MotionToast;

public class NoteLockScreen extends AppCompatActivity {

    // layout
    private PinLockView lockView;
    private IndicatorDots indicatorDots;
    private ImageView lockIcon;
    private TextView forgotPassword;
    private ImageView fingerprintIcon;
    private TextView noteTitleText;

    // activity data
    private int noteId;
    private String fullId;
    private int notePinNumber;
    private String securityWord;
    private String noteTitle;
    private boolean fingerprint;
    private String user;

    // biometric data
    private Executor executor;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_lock_screen);

        overridePendingTransition(R.anim.left_in, R.anim.stay);

        initializeLayout();
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.stay, R.anim.right_out);
    }

    private void initializeLayout(){
        noteId = getIntent().getIntExtra("id", -1);
        fullId = getIntent().getStringExtra("fullId");
        notePinNumber = getIntent().getIntExtra("pin", -1);
        securityWord = getIntent().getStringExtra("securityWord");
        noteTitle = getIntent().getStringExtra("title");
        fingerprint = getIntent().getBooleanExtra("fingerprint", false);
        user = getIntent().getStringExtra("user");

        executor = ContextCompat.getMainExecutor(this);

        lockView = findViewById(R.id.pin_lock_view);
        indicatorDots = findViewById(R.id.indicator_dots);
        lockIcon = findViewById(R.id.lock_icon);
        forgotPassword = findViewById(R.id.forgot_password);
        fingerprintIcon = findViewById(R.id.fingerprint_icon);
        noteTitleText = findViewById(R.id.note_title);
        lockView.attachIndicatorDots(indicatorDots);

        lockView.setDeleteButtonSize(75);
        lockView.setPinLength(String.valueOf(notePinNumber).length());
        lockView.setDeleteButtonDrawable(getDrawable(R.drawable.backspace_icon));
        lockView.setShowDeleteButton(true);
        lockView.setDeleteButtonPressedColor(getColor(R.color.red));

        // if there is no pin number/it is 0, then just open note
        if(notePinNumber==0)
            openNote();

        // shows the note title so that user knows which note they are trying to open
        noteTitleText.setText(noteTitle);
        // if fingerprint is not enabled for note, don't show icon button
        if(!fingerprint)
            fingerprintIcon.setVisibility(View.GONE);

        lockView.setPinLockListener(new PinLockListener() {
            @Override
            public void onComplete(String pin) {
                if(Integer.parseInt(pin) == notePinNumber)
                    openNote();
                else
                    lockIcon.setColorFilter(getColor(R.color.red));
            }

            @Override
            public void onEmpty() { }

            @Override
            public void onPinChange(int pinLength, String intermediatePin) {
                lockIcon.setColorFilter(getColor(R.color.darker_blue));
            }
        });

        forgotPassword.setOnClickListener(v -> forgotPasswordDialog());

        fingerprintIcon.setOnClickListener(v -> fingerprintDialog());

        biometricPrompt = new BiometricPrompt(NoteLockScreen.this,
                executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                openNote();
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Helper.showMessage(NoteLockScreen.this, "Fingerprint Error",
                        "Fingerprint not read, try again!",
                        MotionToast.TOAST_ERROR);
            }
        });
    }

    // if pin is correct, note is opened
    public void openNote(){
        Intent note;
        note = new Intent(this, NoteEdit.class);
        note.putExtra("id", noteId);
        startActivity(note);
        finish();
    }

    // shows a dialog for user to get their note pin
    private void forgotPasswordDialog(){
        InfoSheet info = new InfoSheet(5, securityWord);
        info.show(getSupportFragmentManager(), info.getTag());
    }

    // shows fingerprint dialog for user to use fingerprint
    private void fingerprintDialog(){
        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Biometric login for note")
                .setNegativeButtonText("Use pin instead")
                .build();
        biometricPrompt.authenticate(promptInfo);
    }
}