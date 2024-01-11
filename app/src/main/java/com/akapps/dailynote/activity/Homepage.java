package com.akapps.dailynote.activity;

import static com.akapps.dailynote.classes.helpers.UiHelper.getThemeStyle;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.akapps.dailynote.R;
import com.akapps.dailynote.classes.helpers.AppData;
import com.akapps.dailynote.classes.helpers.Helper;
import com.akapps.dailynote.classes.helpers.RealmHelper;
import com.akapps.dailynote.classes.helpers.RealmSingleton;
import com.akapps.dailynote.fragments.notes;

public class Homepage extends FragmentActivity {

    private boolean isOpenApp;
    private Fragment notes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(getThemeStyle(this));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage_screen);
        Log.d("Here", "in home screen");

        isOpenApp = getIntent().getBooleanExtra("openApp", false);
        AppData.getAppData();

        if (AppData.isAppFirstStarted) {
            // initialize database and get data
            Helper.updateAllWidgetTypes(this);

            if (RealmHelper.getUser(this, "home") != null) {
                if (RealmHelper.getUser(this, "home").getScreenMode().getValue() == 0) {
                    RealmSingleton.get(this).beginTransaction();
                    RealmHelper.getUser(this, "home").setScreenMode(RealmHelper.getUser(this, "home").isModeSettings() ? 1 : 2);
                    RealmSingleton.get(this).commitTransaction();
                }

                if (RealmHelper.getUser(this, "home").isDisableAnimation())
                    AppData.isDisableAnimation = true;

                if (RealmHelper.getUser(this, "home").getPinNumber() > 0 && !isOpenApp && AppData.isAppFirstStarted) {
                    Intent lockScreen = new Intent(this, NoteLockScreen.class);
                    lockScreen.putExtra("id", -11);
                    lockScreen.putExtra("title", "Unlock App");
                    lockScreen.putExtra("pin", RealmHelper.getUser(this, "home").getPinNumber());
                    lockScreen.putExtra("securityWord", RealmHelper.getUser(this, "home").getSecurityWord());
                    lockScreen.putExtra("fingerprint", RealmHelper.getUser(this, "home").isFingerprint());
                    lockScreen.putExtra("isAppLocked", true);
                    startActivity(lockScreen);
                } else {
                    if (savedInstanceState == null)
                        openApp();
                }
            } else
                openApp();
        } else if (savedInstanceState == null)
            openApp();
    }

    private void openApp() {
        notes = new notes();
        getSupportFragmentManager().beginTransaction().add(android.R.id.content, notes).commit();
    }

    @Override
    protected void onDestroy() {
        if(notes != null && notes.isVisible()) getSupportFragmentManager().beginTransaction().detach(notes).commit();
        super.onDestroy();
    }
}
