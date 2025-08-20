package com.example.finalproject;

import android.graphics.Bitmap;

public class CheckedPictureItem {
    private boolean isChecked;
    private final Bitmap imageResource;
    private final String tags;
    private final String date;

    CheckedPictureItem(Bitmap imageResource, String tags, String date) {
        this.isChecked = false;
        this.imageResource = imageResource;
        this.tags = tags;
        this.date = date;
    }

    public boolean getIsChecked() {
        return isChecked;
    }

    public Bitmap getImageResource() {
        return imageResource;
    }

    public String getTags() {
        return tags;
    }

    public String getDate() {
        return date;
    }

    public void checkItem() {
        isChecked = !isChecked;
    }
}
