package com.akapps.dailynote.classes.helpers;

import android.content.Context;
import android.util.Log;

import com.akapps.dailynote.classes.data.CheckListItem;
import com.akapps.dailynote.classes.data.Note;
import com.akapps.dailynote.classes.data.SubCheckListItem;
import java.util.ArrayList;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

public class AppData{
    private static AppData appData;
    public boolean isDarkerMode;
    public static boolean isAppFirstStarted;
    public static int timerDuration;

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
        RealmResults<Note> allNotes = realm.where(Note.class)
                .equalTo("archived", false)
                .equalTo("trash", false)
                .sort("dateEdited", Sort.DESCENDING).findAll();
        ArrayList<Note> noteArrayList = new ArrayList<>();

        noteArrayList.addAll(realm.copyFromRealm(allNotes));

        for(int i= 0; i< noteArrayList.size(); i++) {
            Note currentNote = noteArrayList.get(i);
            if (currentNote.getTitle().isEmpty())
                noteArrayList.get(i).setTitle("- No Title -");
        }

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
                    allArraylistChecklist.add(currentNote.getNote() + "-Note-");
            }
            else {
                noteArrayList.addAll(realm.copyFromRealm(currentNote.getChecklist()));
                for (CheckListItem current : noteArrayList) {
                    if(current.getSubChecklist().size() == 0)
                        allArraylistChecklist.add(current.isChecked() ? current.getText() + "~~" : current.getText());
                    else{
                        allArraylistChecklist.add(current.isChecked() ? current.getText() + "~~" : current.getText());
                        for(SubCheckListItem subCheckListItem: current.getSubChecklist())
                            allArraylistChecklist.add(subCheckListItem.isChecked() ? "⤷️  " + subCheckListItem.getText() + "~~" :
                                    "⤷️  " + subCheckListItem.getText());
                    }
                }
                if(allArraylistChecklist.size() == 0)
                    allArraylistChecklist.add("Empty");
            }
        }

        if(realm != null)
            realm.close();

        return allArraylistChecklist;
    }

    public static void updateNoteWidget(Context context, int noteId, int widgetId){
        Realm realm = getRealm(context);
        Note currentNote = realm.where(Note.class)
                .equalTo("noteId", noteId).findFirst();

        if(currentNote.getWidgetId() != widgetId){
            realm.beginTransaction();
            currentNote.setWidgetId(widgetId);
            realm.commitTransaction();
        }

        if(realm != null)
            realm.close();
    }


    public void setDarkerMode(boolean isDarkerMode){
        this.isDarkerMode = isDarkerMode;
    }
}