package com.example.finalproject;


import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
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

public class PhotoTagger extends AppCompatActivity {


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
        setContentView(R.layout.activity_photo_tagger);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = this.openOrCreateDatabase ("db1", Context.MODE_PRIVATE, null);
//        db.execSQL("DROP TABLE IF EXISTS Photos;");
        //        db.execSQL("DROP TABLE IF EXISTS Images;");


        db.execSQL("CREATE TABLE IF NOT EXISTS Photos (" +
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

        ImageView photoWoodenBoard = findViewById(R.id.photoBoardImage);
        int imageBitmapResource = getResources().getIdentifier("@drawable/wooden_board", null, getPackageName());
        photoWoodenBoard.setImageResource(imageBitmapResource);

        loadImages();
        updateScreen();

        ImageView imageView = findViewById(R.id.photo);
        imageView.setImageResource(R.drawable.blank);
    }

    private void updateScreen() {
        data.clear();
        for(int i = 0; i < imageBitmaps.size(); i++) {
            data.add(new PictureItem(imageBitmaps.get(i), tagStrings.get(i), dateStrings.get(i)));
        }

        adapter.notifyDataSetChanged();
        ListView listView = findViewById(R.id.photoList);
        listView.setAdapter(adapter);
    }

    public void save(View view) {
        ImageView mcas = findViewById(R.id.photo);
        EditText tags = findViewById(R.id.tagEntry);
        BitmapDrawable bitmapDrawable = (BitmapDrawable) mcas.getDrawable(); //we wrote this function inside custom view
        Bitmap b = bitmapDrawable.getBitmap();

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        b.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] ba = stream.toByteArray();

        try {
            File f = new File(getFilesDir().getAbsolutePath() + "/myphoto" + pictureNumber + ".png");

            SimpleDateFormat sdf = new SimpleDateFormat(pattern, Locale.getDefault());
            String time = sdf.format(new Date());
            time = formatDate(time);
            Log.v("Time", time);

            ContentValues cv = new ContentValues();
            cv.put("ID", pictureNumber);
            cv.put("TAGS", tags.getText().toString());
            cv.put("TIME", time);
            cv.put("IMAGE", ba);
            db.insert("PHOTOS", null, cv);

            cv = new ContentValues();
            cv.put("ID", pictureNumber);
            cv.put("TAGS", tags.getText().toString());
            cv.put("TIME", time);
            cv.put("IMAGE", ba);
            cv.put("ISPHOTO", 1);
            db.insert("IMAGES", null, cv);

            pictureNumber++;
            FileOutputStream fos = new FileOutputStream(f);
            b.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();
        }catch (Exception ex) {
            ex.printStackTrace();
        }

        loadImages();
        updateScreen();
        reset();
    }

    private void reset() {
        ImageView mcas = findViewById(R.id.photo);
        mcas.setImageResource(R.drawable.blank);
    }

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

        Cursor c = db.rawQuery("SELECT * FROM PHOTOS WHERE TAGS LIKE '%" + findText.getText() + "%' ORDER BY ID DESC;", null);

        boolean cMovable = c != null && c.moveToFirst();

        // Log.v("Load Images", c.getString(1));

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

    public void takePicture(View view) {
        TextView textView = findViewById(R.id.takePicture);
        textView.setText("");
        Intent x = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(x, 100);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("Permission", "Granted");
            } else {
                Log.d("Permission", "Denied");
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            Bitmap b = (Bitmap) data.getExtras().get("data");
            ImageView imageView = findViewById(R.id.photo);
            imageView.setImageBitmap(b);
        }
    }

    public void PlayBackgroundSound() {
        Intent intent = new Intent(PhotoTagger.this, BackgroundSoundService.class);
        startService(intent);
    }


    public void back(View view) {
        PlayBackgroundSound();
        finish();
    }

    private String formatDate(String date) {
        String formatted = date.replace("_", " ");
        return formatted;
    }

    public void getTags(View view) {
        ImageView mcas = findViewById(R.id.photo);
        BitmapDrawable bitmapDrawable = (BitmapDrawable) mcas.getDrawable(); //we wrote this function inside custom view
        Bitmap b = bitmapDrawable.getBitmap();

        String tags = TagGetter.getTopTags(b);
        Log.v("Tags during getTags", tags);
        EditText tagEntry = findViewById(R.id.tagEntry);
        tagEntry.setText(tags);
        Log.v("Tag Entry After getTags", tagEntry.getText().toString());

    }
}