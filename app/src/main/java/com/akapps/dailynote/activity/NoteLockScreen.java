package com.akapps.dailynote.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import android.content.Context;
import android.content.Intent;
import android.graphics.ColorFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieProperty;
import com.airbnb.lottie.SimpleColorFilter;
import com.airbnb.lottie.model.KeyPath;
import com.airbnb.lottie.value.LottieValueCallback;
import com.akapps.dailynote.R;
import com.akapps.dailynote.classes.helpers.AppData;
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
    private LottieAnimationView pinEmpty;
    private TextView forgotPassword;
    private ImageView fingerprintIcon;
    private TextView noteTitleText;

    // activity data
    private int noteId;
    private int notePinNumber;
    private String securityWord;
    private String noteTitle;
    private boolean fingerprint;
    private boolean isWidget;
    private Context context;

    // biometric data
    private Executor executor;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_lock_screen);

        overridePendingTransition(R.anim.left_in, R.anim.stay);

        context = this;

        initializeLayout();

        if(AppData.getAppData().isLightMode) {
            getWindow().setStatusBarColor(context.getColor(R.color.light_mode));
            findViewById(R.id.layout).setBackgroundColor(context.getColor(R.color.light_mode));
            lockView.setTextColor(context.getColor(R.color.gray));
            ((TextView) findViewById(R.id.instruction)).setTextColor(context.getColor(R.color.gray));
        }
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.stay, R.anim.right_out);
    }

    private void initializeLayout(){
        noteId = getIntent().getIntExtra("id", -1);
        notePinNumber = getIntent().getIntExtra("pin", -1);
        securityWord = getIntent().getStringExtra("securityWord");
        noteTitle = getIntent().getStringExtra("title");
        fingerprint = getIntent().getBooleanExtra("fingerprint", false);
        isWidget = getIntent().getBooleanExtra("isWidget", false);

        executor = ContextCompat.getMainExecutor(this);

        lockView = findViewById(R.id.pin_lock_view);
        indicatorDots = findViewById(R.id.indicator_dots);
        lockIcon = findViewById(R.id.lock_icon);
        pinEmpty = findViewById(R.id.pin_empty);
        forgotPassword = findViewById(R.id.forgot_password);
        fingerprintIcon = findViewById(R.id.fingerprint_icon);
        noteTitleText = findViewById(R.id.note_title);

        lockView.attachIndicatorDots(indicatorDots);
        lockView.setDeleteButtonSize(75);
        lockView.setPinLength(String.valueOf(notePinNumber).length());
        lockView.setDeleteButtonDrawable(getDrawable(R.drawable.icon_backspace));
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
                    changeLottieAnimationColor(lockIcon, R.color.red);
            }

            @Override
            public void onEmpty() {
                pinEmpty.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPinChange(int pinLength, String intermediatePin) {
                if(pinEmpty.getVisibility() == View.VISIBLE)
                    pinEmpty.setVisibility(View.INVISIBLE);

                changeLottieAnimationColor(lockIcon, R.color.cornflower_blue);
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

    private void changeLottieAnimationColor(ImageView icon, int newColor){
        icon.setColorFilter(context.getResources().getColor(newColor));
    }

    // if pin is correct, note is opened
    public void openNote(){
        Intent note;
        note = new Intent(this, NoteEdit.class);
        note.putExtra("id", noteId);
        note.putExtra("isWidget", isWidget);
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