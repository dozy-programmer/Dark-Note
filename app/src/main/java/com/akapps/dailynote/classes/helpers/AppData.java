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

    private boolean isAppFirstStarted;
    private boolean isKeyboardOpen;
    private boolean isDisableAnimation;
    private ArrayList<Integer> wordFoundPositions;
    private int wordIndex;
    private int pin;
    private String securityWord;
    private boolean isFingerprintAdded;
    private int timerDuration;
    private String noteSearch;

    // Private constructor prevents instantiation
    private AppData() {
        this.isAppFirstStarted = true;
        this.isKeyboardOpen = false;
        this.isDisableAnimation = false;
        this.wordFoundPositions = new ArrayList<>();
        this.wordIndex = -1;
        this.pin = 0;
        this.securityWord = "";
        this.noteSearch = "";
        this.isFingerprintAdded = false;
        this.timerDuration = 0;
    }

    private static final class AppDataHolder {
        static final AppData appData = new AppData();
    }

    // Public method to provide access to the singleton instance
    public static AppData getInstance() {
        return AppDataHolder.appData;
    }

    // Getter and Setter methods
    public boolean isAppFirstStarted() {
        return isAppFirstStarted;
    }

    public void setAppFirstStarted(boolean isAppFirstStarted) {
        this.isAppFirstStarted = isAppFirstStarted;
    }

    public boolean isKeyboardOpen() {
        return isKeyboardOpen;
    }

    public void setKeyboardOpen(boolean isKeyboardOpen) {
        this.isKeyboardOpen = isKeyboardOpen;
    }

    public boolean isDisableAnimation() {
        return isDisableAnimation;
    }

    public void setDisableAnimation(boolean isDisableAnimation) {
        this.isDisableAnimation = isDisableAnimation;
    }

    public int getPin() {
        return pin;
    }

    public void setPin(int pin) {
        this.pin = pin;
    }

    public String getSecurityWord() {
        return securityWord;
    }

    public void setSecurityWord(String securityWord) {
        this.securityWord = securityWord;
    }

    public boolean isFingerprintAdded() {
        return isFingerprintAdded;
    }

    public void setFingerprintAdded(boolean fingerprintAdded) {
        isFingerprintAdded = fingerprintAdded;
    }

    public int getTimerDuration() {
        return timerDuration;
    }

    public void setTimerDuration(int timerDuration) {
        this.timerDuration = timerDuration;
    }

    public ArrayList<Integer> getWordFoundPositions() {
        return wordFoundPositions;
    }

    public String getNoteSearch() {
        return noteSearch;
    }

    public void setNoteSearch(String noteSearch) {
        this.noteSearch = noteSearch;
    }

    public void resetWordFoundPositions() {
        wordFoundPositions.clear();
        wordIndex = -1;
    }

    public int getIndexPosition(boolean isGoingUp) {
        if (isGoingUp) {
            wordIndex = (wordIndex == 0 || wordIndex == -1) ? wordFoundPositions.size() - 1 : wordIndex - 1;
        } else {
            wordIndex = (wordIndex == wordFoundPositions.size() - 1 || wordIndex == -1) ? 0 : wordIndex + 1;
        }
        return wordFoundPositions.get(wordIndex);
    }

    private Realm getRealm(Context context) {
        return RealmSingleton.getInstance(context);
    }

    public void updateLockData(int pin, String securityWord, boolean isFingerprintAdded) {
        this.pin = pin;
        this.securityWord = securityWord;
        this.isFingerprintAdded = isFingerprintAdded;
    }

    public ArrayList<Note> getAllNotes(Context context, boolean includeArchive) {
        Realm realm = getRealm(context);
        RealmResults<Note> allNotes = getCurrentNoteSort(realm, includeArchive);
        ArrayList<Note> noteArrayList = new ArrayList<>(realm.copyFromRealm(allNotes));

        for (int i = 0; i < noteArrayList.size(); i++) {
            Note currentNote = noteArrayList.get(i);
            if (currentNote.getTitle().isEmpty())
                noteArrayList.get(i).setTitle("- No Title -");
        }

        return noteArrayList;
    }

    public ArrayList<String> getNoteChecklist(int noteId, Context context) {
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
                    boolean containsAudio = current.getAudioPath() != null && !current.getAudioPath().isEmpty();
                    String text = current.getText() + (containsAudio ? "♬" : "");
                    allArraylistChecklist.add(current.isChecked() ? text + "~~" : text);
                    if (!current.getSubChecklist().isEmpty()) {
                        for (SubCheckListItem subCheckListItem : current.getSubChecklist())
                            allArraylistChecklist.add(subCheckListItem.isChecked() ? "⤷️  " + subCheckListItem.getText() + "~~" : "⤷️  " + subCheckListItem.getText());
                    }
                }
                if (allArraylistChecklist.isEmpty())
                    allArraylistChecklist.add("Empty");
            }
        }

        return allArraylistChecklist;
    }

    public void updateNoteWidget(Context context, int noteId, int widgetId) {
        Note currentNote = getRealm(context).where(Note.class).equalTo("noteId", noteId).findFirst();
        if (currentNote == null) return;

        getRealm(context).beginTransaction();
        currentNote.setWidgetId(widgetId);
        getRealm(context).commitTransaction();
    }

    public RealmResults<Note> getCurrentNoteSort(Realm realm, boolean includeArchive) {
        RealmResults<Note> notes = realm.where(Note.class)
                .equalTo("trash", false)
                .sort("dateEditedMilli", Sort.DESCENDING).findAll();

        if (!includeArchive) {
            notes = notes.where()
                    .equalTo("archived", false)
                    .findAll();
        }
        return notes;
    }
    public void addWordFoundPositions(int position) {
        if (wordFoundPositions != null && !wordFoundPositions.contains(position)) {
            wordFoundPositions.add(position);
            Log.d("Here", "positions -> " + wordFoundPositions);
        }
    }

}