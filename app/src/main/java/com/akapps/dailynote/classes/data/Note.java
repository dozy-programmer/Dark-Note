package com.akapps.dailynote.classes.data;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import io.realm.RealmList;
import io.realm.RealmObject;

public class Note extends RealmObject {

    // note information
    private int noteId;
    private String title;
    private String dateCreated;
    private String dateEdited;
    private String note;
    private int titleColor;
    private int textColor;
    private int backgroundColor;
    private boolean pin;
    private boolean archived;
    private boolean trash;
    private boolean isChecked;

    // multi-select
    private boolean isSelected;

    // note security and retrieval of password
    private String securityWord;
    private int pinNumber;
    private boolean fingerprint;

    // reminder time for note
    private String reminderDateTime;

    // note photos locations
    private RealmList<String> photos;

    // checklist
    private boolean isCheckList;
    private RealmList<CheckListItem> checklist;
    private String checklistConvertedToString;

    // category
    private String category;

    public Note(){}

    public Note(String title, String note){
        this.title = title;
        this.dateCreated = new SimpleDateFormat("E, MMM dd, yyyy\nhh:mm:ss aa").format(Calendar.getInstance().getTime());
        this.dateEdited = dateCreated;
        this.note = note;
        noteId = (int)(Math.random() * 10000000 + 1);
        reminderDateTime = securityWord = "";
        category = "none";
    }

    public int getNoteId(){
        return noteId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDateCreated() {
        return dateCreated;
    }

    public String getDateEdited() {
        return dateEdited;
    }

    public void setDateEdited(String dateEdited) {
        this.dateEdited = dateEdited;
    }

    public String getNote() {
        return note==null ? "" : note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public RealmList<String> getPhotos() {
        return photos;
    }

    public void setPhotos(RealmList<String> photos) {
        this.photos = photos;
    }

    public boolean isPin() {
        return pin;
    }

    public void setPin(boolean pin) {
        this.pin = pin;
    }

    public int getTitleColor() {
        return titleColor;
    }

    public void setTitleColor(int titleColor) {
        this.titleColor = titleColor;
    }

    public int getTextColor() {
        return textColor;
    }

    public void setTextColor(int textColor) {
        this.textColor = textColor;
    }

    public int getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public boolean isArchived() {
        return archived;
    }

    public void setArchived(boolean archived) {
        this.archived = archived;
    }

    public String getReminderDateTime() {
        return reminderDateTime;
    }

    public void setReminderDateTime(String reminderDateTime) {
        this.reminderDateTime = reminderDateTime;
    }

    public String getSecurityWord() {
        return securityWord;
    }

    public void setSecurityWord(String securityWord) {
        this.securityWord = securityWord;
    }

    public int getPinNumber() {
        return pinNumber;
    }

    public void setPinNumber(int pinNumber) {
        this.pinNumber = pinNumber;
    }

    public boolean isFingerprint() {
        return fingerprint;
    }

    public void setFingerprint(boolean fingerprint) {
        this.fingerprint = fingerprint;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public boolean isTrash() {
        return trash;
    }

    public void setTrash(boolean trash) {
        this.trash = trash;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }

    public RealmList<CheckListItem> getChecklist() {
        return checklist;
    }

    public void setChecklist(RealmList<CheckListItem> checklist) {
        this.checklist = checklist;
    }

    public boolean isCheckList() {
        return isCheckList;
    }

    public void setIsCheckList(boolean checkList) {
        this.isCheckList = checkList;
    }

    public String getChecklistConvertedToString() {
        return checklistConvertedToString;
    }

    public void setChecklistConvertedToString(String checklistConvertedToString) {
        this.checklistConvertedToString = checklistConvertedToString;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
