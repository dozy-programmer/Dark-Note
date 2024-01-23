package com.akapps.dailynote.classes.data;

import io.realm.RealmObject;

public class Folder extends RealmObject {

    private int id;
    private String name;
    private int color;
    private int positionInList;

    private int pin;
    private String securityWord;
    private boolean isFingerprintAdded;

    public Folder() {
    }

    public Folder(String name, int positionInList) {
        this.name = name;
        id = (int) (Math.random() * 100000 + 1);
        this.positionInList = positionInList;
        pin = 0;
        securityWord = "";
        isFingerprintAdded = false;
    }

    public Folder(String name, int positionInList, int pin, String securityWord, boolean isFingerprintAdded) {
        this.name = name;
        id = (int) (Math.random() * 100000 + 1);
        this.positionInList = positionInList;
        this.pin = pin;
        this.securityWord = securityWord;
        this.isFingerprintAdded = isFingerprintAdded;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public int getPositionInList() {
        return positionInList;
    }

    public void setPositionInList(int positionInList) {
        this.positionInList = positionInList;
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
}
