package com.example.finalproject;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class SketchTagger extends AppCompatActivity {

    int pictureNumber = -1;
    SQLiteDatabase db;

    ArrayList<Bitmap> imageBitmaps = new ArrayList<>();
    ArrayList<String> tagStrings = new ArrayList<>();
    ArrayList<String> dateStrings = new ArrayList<>();

    ArrayList<PictureItem> data = new ArrayList<>();
    PictureListAdapter adapter;

    private final String pattern = "MM/dd_HH:mm:ss";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sketch_tagger);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = this.openOrCreateDatabase ("db1",Context.MODE_PRIVATE, null);

//        db.execSQL("DROP TABLE IF EXISTS Drawings;");

        db.execSQL("CREATE TABLE IF NOT EXISTS Drawings (" +
                "ID INT PRIMARY KEY, TAGS TEXT, TIME TEXT, IMAGE BLOB);");
        db.execSQL("CREATE TABLE IF NOT EXISTS Images (" +
                "ID INT PRIMARY KEY, TAGS TEXT, TIME TEXT, IMAGE BLOB, ISPHOTO INTEGER);");

        // Find current picture index
        File[] files = getFilesDir().listFiles();
        if(files != null) {
            pictureNumber = files.length;
        }
        pictureNumber--;

        adapter = new PictureListAdapter(this, R.layout.picture_list_item, data);

        ImageView sketchWoodenBoard = findViewById(R.id.sketchBoardImage);
        int imageBitmapResource = getResources().getIdentifier("@drawable/wooden_board", null, getPackageName());
        sketchWoodenBoard.setImageResource(imageBitmapResource);

        sketchWoodenBoard = findViewById(R.id.sketchingBoard);
        imageBitmapResource = getResources().getIdentifier("@drawable/sketching", null, getPackageName());
        sketchWoodenBoard.setImageResource(imageBitmapResource);

        loadImages();
        updateScreen();

    }

    private void updateScreen() {
        data.clear();
        for(int i = 0; i < imageBitmaps.size(); i++) {
            data.add(new PictureItem(imageBitmaps.get(i), tagStrings.get(i), dateStrings.get(i)));
        }

        adapter.notifyDataSetChanged();
        ListView listView = findViewById(R.id.sketchList);
        listView.setAdapter(adapter);
    }


    public void save(View view) {
        MyDrawingArea mcas = findViewById(R.id.cusview);
        EditText tags = findViewById(R.id.tagEntry);
        Bitmap b = mcas.getBitmap(); //we wrote this function inside custom view

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        b.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] ba = stream.toByteArray();

        try {
            File f = new File(getFilesDir().getAbsolutePath() + "/mysketch" + pictureNumber + ".png");

            SimpleDateFormat sdf = new SimpleDateFormat(pattern, Locale.getDefault());
            String time = sdf.format(new Date());
            time = formatDate(time);
            Log.v("Time", time);

            ContentValues cv = new ContentValues();
            cv.put("ID", pictureNumber);
            cv.put("TAGS", tags.getText().toString());
            cv.put("TIME", time);
            cv.put("IMAGE", ba);
            db.insert("DRAWINGS", null, cv);
//            db.execSQL("INSERT INTO DRAWINGS " +
//                    "VALUES ("+ pictureNumber + ", " + tags.getText() + ", ");

            cv = new ContentValues();
            cv.put("ID", pictureNumber);
            cv.put("TAGS", tags.getText().toString());
            cv.put("TIME", time);
            cv.put("IMAGE", ba);
            cv.put("ISPHOTO", 0);
            db.insert("IMAGES", null, cv);

            pictureNumber++;
            FileOutputStream fos = new FileOutputStream(f);
            b.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();
        }catch (Exception ex) {
            ex.printStackTrace();
        }

        mcas.reset();
        loadImages();
        updateScreen();
    }

    public void reset(View view) {
        MyDrawingArea mcas = findViewById(R.id.cusview);
        mcas.reset();
    }

//    public void loadImages(View view) {
//        Log.v("Load Images", "Began");
//
//        EditText findText = findViewById(R.id.textView2);
//
//        Log.v("Load Images", "Initialized");
//
//        Cursor c = db.rawQuery("SELECT * FROM DRAWINGS WHERE TAGS LIKE '%" + findText.getText() + "%' OR TAGS LIKE '" + findText.getText() + "' ORDER BY ID DESC;", null);
//        // change this to check if the sql query that c is pointing to is null
//        if(c != null && c.getCount() > 0) {
//            c.moveToFirst();
//        }
//        Log.v("Load Images", "Moved to first");
//
//        while(c != null && c.getCount() > 0) {
//            String tags = c.getString(1);
//            tagStrings.add(tags);
//            String time = c.getString(2);
//            dateStrings.add(time);
//            byte[] ba = c.getBlob(3);
//            Bitmap b = BitmapFactory.decodeByteArray(ba, 0, ba.length);
//            imageBitmaps.add(b);
//            if(!c.moveToNext()) {
//                break;
//            }
//        }
//        Log.v("Load Images", "Moved to last");
//
//
//        updateScreen();
//    }

    public void loadImages(View view) {
        loadImages();
        updateScreen();
    }

    public void loadImages() {
        Log.v("Load Images", "Began");

        EditText findText = findViewById(R.id.textView2);

        tagStrings.clear();
        dateStrings.clear();
        imageBitmaps.clear();

        Log.v("Load Images", "Initialized");

        Cursor c = db.rawQuery("SELECT * FROM DRAWINGS WHERE TAGS LIKE '%" + findText.getText() + "%' ORDER BY ID DESC;", null);

        boolean cMovable = c != null && c.moveToFirst();

        //            Log.v("Load Images", c.getString(1));

        while(cMovable) {
            String tags = c.getString(1);
            tagStrings.add(tags);
            String time = c.getString(2);
            dateStrings.add(time);
            byte[] ba = c.getBlob(3);
            Bitmap b = BitmapFactory.decodeByteArray(ba, 0, ba.length);
            imageBitmaps.add(b);
            Log.v("Load Images", time);
            cMovable = c.moveToNext();
        }
        Log.v("Load Images", "Moved to last");

    }

    public void PlayBackgroundSound() {
        Intent intent = new Intent(SketchTagger.this, BackgroundSoundService.class);
        startService(intent);
    }

    public void goBack(View view) {
        PlayBackgroundSound();
        finish();
    }

    private String formatDate(String date) {
        String formatted = date.replace("_", " ");
        return formatted;
    }


    public void getTags(View view) {
        MyDrawingArea mcas = findViewById(R.id.cusview);
        Bitmap b = mcas.getBitmap(); //we wrote this function inside custom view

        String tags = TagGetter.getTopTags(b);
        EditText tagEntry = findViewById(R.id.tagEntry);

        tagEntry.setText(tags);
    }
}