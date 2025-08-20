package com.example.finalproject;

import android.graphics.Bitmap;

public class PictureItem {
    private final Bitmap imageResource;
    private final String tags;
    private final String date;

    PictureItem(Bitmap imageResource, String tags, String date) {
        this.imageResource = imageResource;
        this.tags = tags;
        this.date = date;
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
}
