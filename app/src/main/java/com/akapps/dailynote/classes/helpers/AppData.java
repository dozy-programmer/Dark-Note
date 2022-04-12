package com.akapps.dailynote.classes.helpers;

import android.content.Context;
import android.util.Log;

import com.akapps.dailynote.classes.data.CheckListItem;
import com.akapps.dailynote.classes.data.Note;
import com.akapps.dailynote.classes.data.User;

import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;

public class AppData{
    private static AppData appData;
    public boolean isLightMode;
    public static boolean isAppFirstStarted;

    private AppData() { }

    public static AppData getAppData() {
        //instantiate a new CustomerLab if we didn't instantiate one yet
        if (appData == null) {
            isAppFirstStarted = true;
            appData = new AppData();
        }
        return appData;
    }

    private static Realm getRealm(Context context){
        Realm realm;
        try {
            realm = Realm.getDefaultInstance();
        }
        catch (Exception e){
            realm = RealmDatabase.setUpDatabase(context);
        }
        return realm;
    }

    public static ArrayList getAllNotes(Context context){
        Realm realm = getRealm(context);
        RealmResults<Note> allNotes = realm.where(Note.class).findAll();
        ArrayList<Note> noteArrayList = new ArrayList<>();

        noteArrayList.addAll(realm.copyFromRealm(allNotes));

        if(realm != null)
            realm.close();

        return noteArrayList;
    }

    public static ArrayList getNoteChecklist(int noteId, Context context){
        Realm realm = getRealm(context);
        Note currentNote = realm.where(Note.class)
                .equalTo("noteId", noteId).findFirst();

        ArrayList<CheckListItem> noteArrayList = new ArrayList<>();
        ArrayList<String> allArraylistChecklist = new ArrayList<>();

        if(currentNote.getPinNumber() != 0)
            allArraylistChecklist.add("*Note is Locked*");
        else {
            if(!currentNote.isCheckList()){
                if(currentNote.getNote().isEmpty())
                    allArraylistChecklist.add("Empty");
                else
                    allArraylistChecklist.add(currentNote.getNote());
            }
            else {
                noteArrayList.addAll(realm.copyFromRealm(currentNote.getChecklist()));
                for (CheckListItem current : noteArrayList)
                    allArraylistChecklist.add(current.getText());
                if(allArraylistChecklist.size() == 0)
                    allArraylistChecklist.add("Empty");
            }
        }

        if(realm != null)
            realm.close();

        return allArraylistChecklist;
    }


    public void setLightMode(boolean isLightMode){
        this.isLightMode = isLightMode;
    }
}