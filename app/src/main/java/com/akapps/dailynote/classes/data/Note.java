package com.akapps.dailynote.classes.data;

import com.akapps.dailynote.R;
import com.akapps.dailynote.classes.helpers.Helper;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import io.realm.RealmList;
import io.realm.RealmObject;

public class Note extends RealmObject {

    // note information
    private int noteId;
    private String title;
    private String note;
    private int titleColor;
    private int textColor;
    private int lightTextColor;
    private int backgroundColor;
    private boolean pin;
    private boolean archived;
    private boolean trash;
    private boolean isChecked;

    // date
    private String dateCreated;
    private String dateEdited;
    private long dateCreatedMilli;
    private long dateEditedMilli;

    // multi-select
    private boolean isSelected;

    // note security and retrieval of password
    private String securityWord;
    private int pinNumber;
    private boolean fingerprint;

    // reminder time for note
    private String reminderDateTime;

    // note photos locations
    private RealmList<String> photos = new RealmList<>();

    // checklist
    private boolean isCheckList;
    private RealmList<CheckListItem> checklist = new RealmList<>();
    private String checklistConvertedToString;

    // category
    private String category;

    // sorting
    private int sort;

    // show sublist
    private boolean enableSublist;

    // note widget
    private int widgetId;

    public Note(){}

    public Note(String title, String note, boolean enableSublist){
        this.title = title;
        dateCreated = new SimpleDateFormat("E, MMM dd, yyyy\nhh:mm:ss aa").format(Calendar.getInstance().getTime());
        dateEdited = dateCreated;
        this.note = note;
        noteId = (int)(Math.random() * 10000000 + 1);
        reminderDateTime = securityWord = "";
        dateCreatedMilli = dateEditedMilli = Helper.dateToCalender(dateCreated).getTimeInMillis();
        category = "none";
        sort = 5;
        lightTextColor = R.color.black;
        textColor = R.color.white;
        this.enableSublist = enableSublist;
        widgetId = -1;
        pinNumber = 0;
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
        return note == null ? "" : note;
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

    public long getDateCreatedMilli() {
        return dateCreatedMilli;
    }

    public void setDateCreatedMilli(long dateCreatedMilli) {
        this.dateCreatedMilli = dateCreatedMilli;
    }

    public long getDateEditedMilli() {
        return dateEditedMilli;
    }

    public void setDateEditedMilli(long dateEditedMilli) {
        this.dateEditedMilli = dateEditedMilli;
    }

    public int getSort() {
        return sort;
    }

    public void setSort(int sort) {
        this.sort = sort;
    }

    public boolean isEnableSublist() {
        return enableSublist;
    }

    public void setEnableSublist(boolean enableSublist) {
        this.enableSublist = enableSublist;
    }

    public int getWidgetId() {
        return widgetId;
    }

    public void setWidgetId(int widgetId) {
        this.widgetId = widgetId;
    }

    public int getLightTextColor() {
        return lightTextColor;
    }

    public void setLightTextColor(int lightTextColor) {
        this.lightTextColor = lightTextColor;
    }
}
