package com.akapps.dailynote.classes.data;

import io.realm.RealmObject;

public class Folder extends RealmObject {

    private int id;
    private String name;
    private int color;
    private int positionInList;

    public Folder(){}

    public Folder(String name, int positionInList) {
        this.name = name;
        id = (int)(Math.random() * 100000 + 1);
        this.positionInList = positionInList;
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
}
