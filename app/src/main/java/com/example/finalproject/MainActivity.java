package com.example.finalproject;

import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

//        SQLiteDatabase db = this.openOrCreateDatabase ("db1", Context.MODE_PRIVATE, null);
//        db.execSQL("DROP TABLE IF EXISTS Photos;");
//        db.execSQL("DROP TABLE IF EXISTS Drawings;");
//        db.execSQL("DROP TABLE IF EXISTS Images;");

        PlayBackgroundSound();

    }

    public void PlayBackgroundSound() {
        Intent intent = new Intent(MainActivity.this, BackgroundSoundService.class);
        startService(intent);
    }

    public void toPhotoTagger(View view) {
        Intent intent = new Intent(MainActivity.this, BackgroundSoundService.class);
        stopService(intent);

        Intent i = new Intent(getApplicationContext(), PhotoTagger.class);
        startActivity(i,
                ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
    }

    public void toSketchTagger(View view) {
        Intent intent = new Intent(MainActivity.this, BackgroundSoundService.class);
        stopService(intent);

        Intent i = new Intent(getApplicationContext(), SketchTagger.class);
        startActivity(i,
                ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
    }

    public void toStoryTeller(View view) {
        Intent intent = new Intent(MainActivity.this, BackgroundSoundService.class);
        stopService(intent);

        Intent i = new Intent(getApplicationContext(), StoryTeller.class);
        startActivity(i,
                ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
    }



}