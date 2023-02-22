package com.akapps.dailynote.activity;

import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.FragmentActivity;
import com.akapps.dailynote.R;
import com.akapps.dailynote.classes.data.Note;
import com.akapps.dailynote.classes.data.User;
import com.akapps.dailynote.classes.helpers.AppData;
import com.akapps.dailynote.classes.helpers.Helper;
import com.akapps.dailynote.classes.helpers.RealmDatabase;
import com.akapps.dailynote.fragments.notes;
import io.realm.Realm;
import io.realm.RealmResults;

public class Homepage extends FragmentActivity{

    private Realm realm;
    private User user;
    private boolean isOpenApp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage_screen);

        isOpenApp = getIntent().getBooleanExtra("openApp", false);
        AppData.getAppData();

        if(AppData.isAppFirstStarted) {
            // initialize database and get data
            try {
                realm = Realm.getDefaultInstance();
            } catch (Exception e) {
                realm = RealmDatabase.setUpDatabase(this);
            }

            user = realm.where(User.class).findFirst();

            RealmResults<Note> notesWithWidgets = realm.where(Note.class)
                    .greaterThan("widgetId", 0).findAll();
            for(Note currentNote: notesWithWidgets)
                Helper.updateWidget(currentNote, this, realm);

            if(user != null) {
                if (user.isModeSettings())
                    AppData.getAppData().setDarkerMode(true);

                if (user.getPinNumber() > 0 && !isOpenApp && AppData.isAppFirstStarted) {
                    Intent lockScreen = new Intent(this, NoteLockScreen.class);
                    lockScreen.putExtra("id", -11);
                    lockScreen.putExtra("title", "Unlock App");
                    lockScreen.putExtra("pin", user.getPinNumber());
                    lockScreen.putExtra("securityWord", user.getSecurityWord());
                    lockScreen.putExtra("fingerprint", user.isFingerprint());
                    lockScreen.putExtra("isAppLocked", true);
                    startActivity(lockScreen);
                } else {
                    if (savedInstanceState == null)
                        openApp();
                }
            }
            else
                openApp();

            if(realm != null)
                realm.close();
        }
        else if (savedInstanceState == null)
           openApp();
    }

    private void openApp(){
        getSupportFragmentManager().beginTransaction().add(android.R.id.content, new notes()).commit();
    }

}
