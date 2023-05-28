package com.akapps.dailynote.classes.data;

import io.realm.RealmObject;

public class User extends RealmObject {

    private int userId;
    private boolean proUser;
    private boolean ultimateUser;

    private String layoutSelected;

    // backup data
    private int textSize;

    private int titleLines;
    private int contentLines;
    private boolean openFoldersOnStart;
    private boolean showFolderNotes;

    private boolean showPreview;
    private boolean showPreviewNoteInfo;
    private boolean modeSettings;

    private String email;

    private String lastUpload;

    private int backupReminderOccurrence;
    private String backupReminderDate;

    // note settings
    private boolean enableSublists;
    private boolean increaseFabSize;
    private boolean enableEmptyNote;
    private boolean enableDeleteIcon;
    private String itemsSeparator;
    private String sublistSeparator;
    private String budgetCharacter;
    private String expenseCharacter;
    private boolean hideRichTextEditor;
    private boolean showAudioButton;
    private boolean hideBudget;
    private boolean twentyFourHourFormat;

    // note security and retrieval of password
    private String securityWord;
    private int pinNumber;
    private boolean fingerprint;

    public User() {}

    public User(int userId) {
        this.userId = userId;
        ultimateUser = proUser = openFoldersOnStart = showFolderNotes = enableEmptyNote =
                hideRichTextEditor = enableDeleteIcon = fingerprint = twentyFourHourFormat = false;
        textSize = backupReminderOccurrence = pinNumber = 0;
        layoutSelected = "stag";
        showPreview = showPreviewNoteInfo = increaseFabSize =
                modeSettings = enableSublists = showAudioButton = hideBudget = true;
        titleLines = contentLines = 3;
        lastUpload = backupReminderDate = email = securityWord = "";
        itemsSeparator = "newline";
        sublistSeparator = "space";
        budgetCharacter = "+$";
        expenseCharacter = "$";
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public boolean isProUser() {
        return proUser;
    }

    public void setProUser(boolean proUser) {
        this.proUser = proUser;
    }

    public int getTextSize() {
        return textSize;
    }

    public void setTextSize(int textSize) {
        this.textSize = textSize;
    }

    public String getLayoutSelected() {
        return layoutSelected;
    }

    public void setLayoutSelected(String layoutSelected) {
        this.layoutSelected = layoutSelected;
    }

    public boolean isShowPreview() {
        return showPreview;
    }

    public void setShowPreview(boolean showPreview) {
        this.showPreview = showPreview;
    }

    public int getTitleLines() {
        return titleLines;
    }

    public void setTitleLines(int titleLines) {
        this.titleLines = titleLines;
    }

    public int getContentLines() {
        return contentLines;
    }

    public void setContentLines(int contentLines) {
        this.contentLines = contentLines;
    }

    public boolean isOpenFoldersOnStart() {
        return openFoldersOnStart;
    }

    public void setOpenFoldersOnStart(boolean openFoldersOnStart) {
        this.openFoldersOnStart = openFoldersOnStart;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getLastUpload() {
        return lastUpload;
    }

    public void setLastUpload(String lastUpload) {
        this.lastUpload = lastUpload;
    }

    public boolean isShowFolderNotes() {
        return showFolderNotes;
    }

    public void setShowFolderNotes(boolean showFolderNotes) {
        this.showFolderNotes = showFolderNotes;
    }

    public boolean isShowPreviewNoteInfo() {
        return showPreviewNoteInfo;
    }

    public void setShowPreviewNoteInfo(boolean showPreviewNoteInfo) {
        this.showPreviewNoteInfo = showPreviewNoteInfo;
    }

    public boolean isModeSettings() {
        return modeSettings;
    }

    public void setModeSettings(boolean modeSettings) {
        this.modeSettings = modeSettings;
    }

    public int getBackupReminderOccurrence() {
        return backupReminderOccurrence;
    }

    public void setBackupReminderOccurrence(int backupReminderOccurrence) {
        this.backupReminderOccurrence = backupReminderOccurrence;
    }

    public boolean isEnableSublists() {
        return enableSublists;
    }

    public void setEnableSublists(boolean enableSublists) {
        this.enableSublists = enableSublists;
    }

    public String getBackupReminderDate() {
        return backupReminderDate;
    }

    public void setBackupReminderDate(String backupReminderDate) {
        this.backupReminderDate = backupReminderDate;
    }

    public boolean isUltimateUser() {
        return ultimateUser;
    }

    public void setUltimateUser(boolean ultimateUser) {
        this.ultimateUser = ultimateUser;
    }

    public boolean isIncreaseFabSize() {
        return increaseFabSize;
    }

    public void setIncreaseFabSize(boolean increaseFabSize) {
        this.increaseFabSize = increaseFabSize;
    }

    public boolean isEnableEmptyNote() {
        return enableEmptyNote;
    }

    public void setEnableEmptyNote(boolean enableEmptyNote) {
        this.enableEmptyNote = enableEmptyNote;
    }

    public String getItemsSeparator() {
        return itemsSeparator;
    }

    public void setItemsSeparator(String itemsSeparator) {
        this.itemsSeparator = itemsSeparator;
    }

    public String getSublistSeparator() {
        return sublistSeparator;
    }

    public void setSublistSeparator(String sublistSeparator) {
        this.sublistSeparator = sublistSeparator;
    }

    public String getBudgetCharacter() {
        return budgetCharacter;
    }

    public void setBudgetCharacter(String budgetCharacter) {
        this.budgetCharacter = budgetCharacter;
    }

    public String getExpenseCharacter() {
        return expenseCharacter;
    }

    public void setExpenseCharacter(String expenseCharacter) {
        this.expenseCharacter = expenseCharacter;
    }

    public boolean isEnableDeleteIcon() {
        return enableDeleteIcon;
    }

    public void setEnableDeleteIcon(boolean enableDeleteIcon) {
        this.enableDeleteIcon = enableDeleteIcon;
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

    public boolean isHideRichTextEditor() {
        return hideRichTextEditor;
    }

    public void setHideRichTextEditor(boolean hideRichTextEditor) {
        this.hideRichTextEditor = hideRichTextEditor;
    }

    public boolean isShowAudioButton() {
        return showAudioButton;
    }

    public void setShowAudioButton(boolean showAudioButton) {
        this.showAudioButton = showAudioButton;
    }

    public boolean isHideBudget() {
        return hideBudget;
    }

    public void setHideBudget(boolean hideBudget) {
        this.hideBudget = hideBudget;
    }

    public boolean isTwentyFourHourFormat() {
        return twentyFourHourFormat;
    }

    public void setTwentyFourHourFormat(boolean twentyFourHourFormat) {
        this.twentyFourHourFormat = twentyFourHourFormat;
    }
}
