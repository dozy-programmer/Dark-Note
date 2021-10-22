package com.akapps.dailynote.classes.other;

import android.graphics.drawable.Drawable;

public class IconPowerMenuItem {
    private final Drawable icon;
    private String title;

    public IconPowerMenuItem(String title) {
        this.title = title;
        this.icon = null;
    }

    public IconPowerMenuItem(Drawable icon, String title) {
        this.icon = icon;
        this.title = title;
    }

    public Drawable getIcon() {
        return icon;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}