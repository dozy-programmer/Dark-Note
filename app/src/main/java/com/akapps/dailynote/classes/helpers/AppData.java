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

public class AppData {
    private static AppData appData;
    public static boolean isAppFirstStarted;
    public static int timerDuration;
    public static boolean isKeyboardOpen;
    public static boolean isDisableAnimation;
    public static ArrayList<Integer> wordFoundPositions = new ArrayList<>();
    public static int wordIndex;

    public static int pin;
    public static String securityWord;
    public static boolean isFingerprintAdded;

    private AppData() {
    }

    public static AppData getAppData() {
        if (appData == null) {
            isAppFirstStarted = true;
            isKeyboardOpen = false;
            isDisableAnimation = false;
            wordFoundPositions = new ArrayList<>();
            wordIndex = -1;
            pin = 0;
            securityWord = "";
            isFingerprintAdded = false;
            appData = new AppData();
        }
        return appData;
    }

    private static Realm getRealm(Context context) {
        return RealmSingleton.getInstance(context);
    }

    public static void updateLockData(int pin, String securityWord, boolean isFingerprintAdded) {
        AppData.pin = pin;
        AppData.securityWord = securityWord;
        AppData.isFingerprintAdded = isFingerprintAdded;
    }

    public static ArrayList<Note> getAllNotes(Context context, Boolean includeArchive) {
        Realm realm = getRealm(context);
        RealmResults<Note> allNotes = getCurrentNoteSort(realm, includeArchive);
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
        Note currentNote = realm.where(Note.class).equalTo("noteId", noteId).findFirst();

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
                else {
                    allArraylistChecklist.add(currentNote.getNote() + "-Note-");
                }
            } else {
                noteArrayList.addAll(realm.copyFromRealm(Helper.sortChecklist(context, noteId, realm)));
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

    public static ArrayList<String> ensureNoteIsNotTooLarge(Context context, int noteId){
        Realm realm = getRealm(context);
        Note currentNote = realm.where(Note.class)
                .equalTo("noteId", noteId).findFirst();
        String note = currentNote.getNote();
        ArrayList<String> allArraylistChecklist = new ArrayList<>();

        int maxLength = 1500;
        String truncatedNote = note.substring(0, Math.min(note.length(), maxLength));
        if (truncatedNote.length() < note.length()) {
            truncatedNote += "... note too large, open note to see more";
        }
        if(note.isEmpty()){
            allArraylistChecklist.add("Empty");
        }
        else{
            allArraylistChecklist.add(truncatedNote + "-Note-");
        }

        return allArraylistChecklist;
    }

    public static void updateNoteWidget(Context context, int noteId, int widgetId) {
        Note currentNote = getRealm(context).where(Note.class).equalTo("noteId", noteId).findFirst();
        if (currentNote == null) return;

        getRealm(context).beginTransaction();
        currentNote.setWidgetId(widgetId);
        getRealm(context).commitTransaction();
    }

    public static RealmResults<Note> getCurrentNoteSort(Realm realm, Boolean includeArchive) {
        RealmResults<Note> notes = realm.where(Note.class)
                .equalTo("trash", false)
                .sort("dateEditedMilli", Sort.DESCENDING).findAll();

        if(!includeArchive){
            notes = notes.where()
                    .equalTo("archived", false)
                    .findAll();
        }
        return notes;
    }

    public static void resetWordFoundPositions() {
        if (wordFoundPositions != null) {
            wordFoundPositions.clear();
            wordIndex = -1;
        }
    }

    public static void addWordFoundPositions(int position) {
        if (wordFoundPositions != null && !wordFoundPositions.contains(position)) {
            wordFoundPositions.add(position);
            Log.d("Here", "positions -> " + AppData.getWordFoundPositions());
        }
    }

    public static ArrayList<Integer> getWordFoundPositions() {
        return wordFoundPositions;
    }

    public static int getIndexPosition(boolean isGoingUp) {
        if (isGoingUp) {
            if (wordIndex == 0 || wordIndex == -1)
                wordIndex = getWordFoundPositions().size() - 1;
            else
                wordIndex--;
        } else {
            if (wordIndex == getWordFoundPositions().size() - 1 || wordIndex == -1)
                wordIndex = 0;
            else
                wordIndex++;
        }
        return getWordFoundPositions().get(wordIndex);
    }
}