package com.akapps.dailynote.classes.helpers;

import android.content.Context;

import com.akapps.dailynote.classes.data.CheckListItem;
import com.akapps.dailynote.classes.data.Note;
import com.akapps.dailynote.classes.data.SubCheckListItem;

import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

public class AppData {
    private static AppData appData;
    public static boolean isAppFirstStarted;
    public static int timerDuration;
    public static boolean isKeyboardOpen;
    public static boolean isDisableAnimation;

    private AppData() {
    }

    public static AppData getAppData() {
        if (appData == null) {
            isAppFirstStarted = true;
            isKeyboardOpen = false;
            isDisableAnimation = false;
            appData = new AppData();
        }
        return appData;
    }

    private static Realm getRealm(Context context) {
        return RealmSingleton.getInstance(context);
    }

    public static ArrayList<Note> getAllNotes(Context context) {
        Realm realm = getRealm(context);
        RealmResults<Note> allNotes = getCurrentNoteSort(realm);
        ArrayList<Note> noteArrayList = new ArrayList<>();

        noteArrayList.addAll(realm.copyFromRealm(allNotes));

        for (int i = 0; i < noteArrayList.size(); i++) {
            Note currentNote = noteArrayList.get(i);
            if (currentNote.getTitle().isEmpty())
                noteArrayList.get(i).setTitle("- No Title -");
        }

        return noteArrayList;
    }

    public static ArrayList<String> getNoteChecklist(int noteId, Context context) {
        Realm realm = getRealm(context);
        Note currentNote = realm.where(Note.class)
                .equalTo("noteId", noteId).findFirst();

        ArrayList<CheckListItem> noteArrayList = new ArrayList<>();
        ArrayList<String> allArraylistChecklist = new ArrayList<>();

        if (currentNote == null)
            allArraylistChecklist.add("* Note has been deleted, delete this widget * -Note-");
        else if (currentNote.getPinNumber() != 0)
            allArraylistChecklist.add("* Note is Locked * -Note-");
        else {
            if (!currentNote.isCheckList()) {
                if (currentNote.getNote().isEmpty())
                    allArraylistChecklist.add("Empty");
                else
                    allArraylistChecklist.add(currentNote.getNote() + "-Note-");
            } else {
                noteArrayList.addAll(realm.copyFromRealm(currentNote.getChecklist()));
                for (CheckListItem current : noteArrayList) {
                    boolean containsAudio = current.getAudioPath() != null &&
                            !current.getAudioPath().isEmpty();
                    String text = current.getText() + (containsAudio ? "♬" : "");
                    allArraylistChecklist.add(current.isChecked() ? text + "~~" : text);
                    if (current.getSubChecklist().size() != 0) {
                        for (SubCheckListItem subCheckListItem : current.getSubChecklist())
                            allArraylistChecklist.add(subCheckListItem.isChecked() ? "⤷️  " + subCheckListItem.getText() + "~~" :
                                    "⤷️  " + subCheckListItem.getText());
                    }
                }
                if (allArraylistChecklist.size() == 0)
                    allArraylistChecklist.add("Empty");
            }
        }

        return allArraylistChecklist;
    }

    public static void updateNoteWidget(Context context, int noteId, int widgetId) {
        Realm realm = getRealm(context);
        Note currentNote = realm.where(Note.class).equalTo("noteId", noteId).findFirst();

        assert currentNote != null;
        if (currentNote.getWidgetId() != widgetId) {
            realm.beginTransaction();
            currentNote.setWidgetId(widgetId);
            realm.commitTransaction();
        }
    }

    public static RealmResults<Note> getCurrentNoteSort(Realm realm) {
        RealmResults<Note> allNotes = realm.where(Note.class)
                .equalTo("archived", false)
                .equalTo("trash", false)
                .sort("dateEditedMilli", Sort.DESCENDING).findAll();

        return allNotes;
    }
}