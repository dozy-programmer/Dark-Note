package com.akapps.dailynote.classes.helpers;

import android.content.ContentResolver;
import android.content.Context;

import com.akapps.dailynote.classes.data.Note;
import com.akapps.dailynote.classes.data.User;
import io.realm.Realm;
import io.realm.RealmResults;

public class AppData{
    private static AppData appData;

    // app variables
    public static boolean isAppFirstStarted;
    public boolean isLightTheme;
    public User user;

    private AppData() {}

    public static AppData getAppData() {
        // instantiate a new AppData if we didn't instantiate one yet
        if (appData == null) {
            isAppFirstStarted = true;
            appData = new AppData();
        }
        return appData;
    }

    public boolean isLightTheme(Realm realm){
        return getUser(realm).isModeSettings();
    }

    public User getUser(Realm realm){
        if(user == null || user.getRealm().isClosed()){
            if (realm.where(User.class).findAll().size() == 0)
                user = addUser(realm);
            else {
                user = realm.where(User.class).findFirst();
                if (user.getTitleLines() == 0) {
                    realm.beginTransaction();
                    user.setTitleLines(3);
                    user.setContentLines(3);
                    realm.commitTransaction();
                }
            }
        }
        return user;
    }

    public RealmResults<Note> getNotes(Context context){
        return RealmDatabase.getRealm(context).where(Note.class).findAll();
    }

    private User addUser(Realm realm){
        int generateId = (int)(Math.random() * 10000000 + 1);
        User user = new User(generateId);
        realm.beginTransaction();
        realm.insert(user);
        realm.commitTransaction();
        return user;
    }
}