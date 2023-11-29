package com.akapps.dailynote.activity;

import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.FragmentActivity;
import com.akapps.dailynote.R;
import com.akapps.dailynote.classes.data.Note;
import com.akapps.dailynote.classes.data.User;
import com.akapps.dailynote.classes.helpers.AppAnalytics;
import com.akapps.dailynote.classes.helpers.AppData;
import com.akapps.dailynote.classes.helpers.Helper;
import com.akapps.dailynote.classes.helpers.RealmHelper;
import com.akapps.dailynote.classes.helpers.RealmSingleton;
import com.akapps.dailynote.fragments.notes;
import io.realm.RealmResults;

public class Homepage extends FragmentActivity {

    private boolean isOpenApp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage_screen);

        isOpenApp = getIntent().getBooleanExtra("openApp", false);
        AppData.getAppData();
        AppAnalytics.get(this);

        if (AppData.isAppFirstStarted) {
            // initialize database and get data
            RealmSingleton.get(this).beginTransaction();
            RealmResults<Note> notesWithWidgets = RealmSingleton.get(this).where(Note.class)
                    .greaterThan("widgetId", 0).findAll();
            for (Note currentNote : notesWithWidgets)
                Helper.updateWidget(currentNote, Homepage.this, RealmSingleton.get(this));
            RealmSingleton.get(this).commitTransaction();

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
        getSupportFragmentManager().beginTransaction().add(android.R.id.content, new notes()).commit();
    }
}
