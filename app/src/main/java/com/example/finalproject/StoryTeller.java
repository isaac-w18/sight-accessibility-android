package com.example.finalproject;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.Locale;

public class StoryTeller extends AppCompatActivity {
    TextToSpeech tts = null;
    boolean includeSketches = true;

    SQLiteDatabase db;

    ArrayList<Bitmap> imageBitmaps = new ArrayList<>();
    ArrayList<String> tagStrings = new ArrayList<>();
    ArrayList<String> dateStrings = new ArrayList<>();
    ArrayList<CheckedPictureItem> data = new ArrayList<>();
    CheckedPictureListAdapter adapter;
    private final String pattern = "MM/dd_HH:mm:ss";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_story_teller);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = this.openOrCreateDatabase("db1",  Context.MODE_PRIVATE, null);

//                db.execSQL("DROP TABLE IF EXISTS Photos;");
//                db.execSQL("DROP TABLE IF EXISTS Drawings;");
//                db.execSQL("DROP TABLE IF EXISTS Images;");


        db.execSQL("CREATE TABLE IF NOT EXISTS Images (" +
                "ID INT PRIMARY KEY, TAGS TEXT, TIME TEXT, IMAGE BLOB, ISPHOTO INTEGER);");

        adapter = new CheckedPictureListAdapter(this, R.layout.story_list_item, data);

        ImageView parchment = findViewById(R.id.parchment);
        int imageBitmapResource = getResources().getIdentifier("@drawable/parchment", null, getPackageName());
        parchment.setImageResource(imageBitmapResource);

        ImageView woodenBoard = findViewById(R.id.wooden_board);
        imageBitmapResource = getResources().getIdentifier("@drawable/wooden_board", null, getPackageName());
        woodenBoard.setImageResource(imageBitmapResource);


        loadImages();
        updateScreen();

        tts = new TextToSpeech(this, status -> {
            if(status != TextToSpeech.ERROR) {
                tts.setLanguage(Locale.US);
            }
        });
    }

    private void updateScreen() {
        data.clear();
        for(int i = 0; i < imageBitmaps.size(); i++) {
            CheckedPictureItem cpi = new CheckedPictureItem(imageBitmaps.get(i), tagStrings.get(i), dateStrings.get(i));
            data.add(cpi);
        }

        adapter.notifyDataSetChanged();
        ListView listView = findViewById(R.id.storyList);
        listView.setAdapter(adapter);
    }

    private void loadImages() {
        EditText findText = findViewById(R.id.tagEntry);
        tagStrings.clear();
        dateStrings.clear();
        imageBitmaps.clear();

        Cursor c = null;
        if(includeSketches) {
            c = db.rawQuery("SELECT * FROM Images WHERE TAGS LIKE '%" + findText.getText() + "%' ORDER BY ID DESC;", null);
        } else {
            c = db.rawQuery("SELECT * FROM Images WHERE (TAGS LIKE '%" + findText.getText() + "%') AND (ISPHOTO = 1) ORDER BY ID DESC;", null);
        }
        boolean cMovable = c != null && c.moveToFirst();
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

    public void includeSketches(View view) {
        includeSketches = !includeSketches;
        loadImages();
        updateScreen();
    }



    public void createStory(View view) {
        EditText storyBox = findViewById(R.id.editTextTextMultiLine);
        String keywords = "";
        for(CheckedPictureItem cpi : data) {
            if(cpi.getIsChecked()) {
                keywords += cpi.getTags() + ", ";
            }
        }
        String context = "Outside";

        if(!keywords.isEmpty()) {
            StoryCreator.getStory(context, keywords, this, storyBox);
            storyBox.invalidate();
//            String[] sentences = storyBox.getText().toString().split("\\.\\s+");
//            tts.speak(storyBox.getText(), TextToSpeech.QUEUE_FLUSH, null, null);
        } else {
            storyBox.setText("No Tags Selected.");
        }
//        tts.speak("This is a test.", TextToSpeech.QUEUE_FLUSH, null, null);

    }
    public void PlayBackgroundSound() {
        Intent intent = new Intent(StoryTeller.this, BackgroundSoundService.class);
        startService(intent);
    }

    public void back(View view) {
        tts.speak(" ", TextToSpeech.QUEUE_FLUSH, null, null);
        PlayBackgroundSound();
        finish();
    }

    public void find(View view) {
        loadImages();
        updateScreen();
    }

    public void checkItem(View view) {
        CheckBox checkBox = (CheckBox) view;
        data.get((int)checkBox.getTag()).checkItem();
        checkBox.invalidate();

        TextView textView = findViewById(R.id.selections);
        String selections = "you selected: ";
        for(CheckedPictureItem cpi : data) {
            if(cpi.getIsChecked()) {
                selections += cpi.getTags() + ", ";
            }
        }
//        if(selections.length() > 28) {
//            selections = selections.substring(0, 48) + "...";
//        } else
        if (selections.indexOf(',') > -1) {
            selections = selections.substring(0, selections.length()-2);
        }
        textView.setText(selections);
    }

    public void speak(View view) {
        EditText storyBox = findViewById(R.id.editTextTextMultiLine);

//        tts.speak("This is a test.", TextToSpeech.QUEUE_FLUSH, null, null);
        String[] sentences = storyBox.getText().toString().split(" ");
        if(sentences.length > 0) {
            tts.speak(sentences[0], TextToSpeech.QUEUE_FLUSH, null, null);
        }
        for (int i = 1; i < sentences.length; i++) {
//            Log.v("TTS Sentence", sentences[i]);
            tts.speak(sentences[i], TextToSpeech.QUEUE_ADD, null, null);
        }
    }
}