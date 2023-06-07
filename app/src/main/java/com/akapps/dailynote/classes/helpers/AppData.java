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
        return RealmSingleton.getInstance(context);
    }

    public static ArrayList getAllNotes(Context context){
        Realm realm = getRealm(context);
        RealmResults<Note> allNotes = getCurrentNoteSort(realm);
        ArrayList<Note> noteArrayList = new ArrayList<>();

        noteArrayList.addAll(realm.copyFromRealm(allNotes));

        for(int i= 0; i< noteArrayList.size(); i++) {
            Note currentNote = noteArrayList.get(i);
            if (currentNote.getTitle().isEmpty())
                noteArrayList.get(i).setTitle("- No Title -");
        }

        RealmSingleton.closeRealmInstance("AppData class, getAllNotes method");

        return noteArrayList;
    }

    public static ArrayList<String> getNoteChecklist(int noteId, Context context){
        Realm realm = getRealm(context);
        Note currentNote = realm.where(Note.class)
                .equalTo("noteId", noteId).findFirst();

        ArrayList<CheckListItem> noteArrayList = new ArrayList<>();
        ArrayList<String> allArraylistChecklist = new ArrayList<>();

        if(currentNote == null)
            allArraylistChecklist.add("* Note has been deleted, delete this widget * -Note-");
        else if(currentNote.getPinNumber() != 0)
            allArraylistChecklist.add("* Note is Locked * -Note-");
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
                    boolean containsAudio = current.getAudioPath() != null &&
                            !current.getAudioPath().isEmpty();
                    String text = current.getText() + (containsAudio ? "♬" : "");
                    allArraylistChecklist.add(current.isChecked() ?  text + "~~": text);
                    if(current.getSubChecklist().size() != 0){
                        for(SubCheckListItem subCheckListItem: current.getSubChecklist())
                            allArraylistChecklist.add(subCheckListItem.isChecked() ? "⤷️  " + subCheckListItem.getText() + "~~" :
                                    "⤷️  " + subCheckListItem.getText());
                    }
                }
                if(allArraylistChecklist.size() == 0)
                    allArraylistChecklist.add("Empty");
            }
        }

        RealmSingleton.closeRealmInstance("AppData class, getNoteChecklist method");

        return allArraylistChecklist;
    }

    public static void updateNoteWidget(Context context, int noteId, int widgetId){
        Realm realm = getRealm(context);
        Note currentNote = realm.where(Note.class)
                .equalTo("noteId", noteId).findFirst();

        assert currentNote != null;
        if(currentNote.getWidgetId() != widgetId){
            realm.beginTransaction();
            currentNote.setWidgetId(widgetId);
            realm.commitTransaction();
        }

        RealmSingleton.closeRealmInstance("AppData class, updateNoteWidget method");
    }


    public void setDarkerMode(boolean isDarkerMode){
        this.isDarkerMode = isDarkerMode;
    }

    public static RealmResults<Note> getCurrentNoteSort(Realm realm){
        RealmResults<Note> allNotes = realm.where(Note.class)
                .equalTo("archived", false)
                .equalTo("trash", false)
                .sort("dateEditedMilli", Sort.DESCENDING).findAll();

        return allNotes;
    }
}