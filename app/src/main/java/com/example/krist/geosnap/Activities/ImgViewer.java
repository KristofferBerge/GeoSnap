package com.example.krist.geosnap.Activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import com.example.krist.geosnap.R;

import java.io.File;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class ImgViewer extends AppCompatActivity {

    private final Handler mHideHandler = new Handler();
    private View mContentView;
    private View mControlsView;
    private boolean mVisible;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_img_viewer);

        mVisible = true;
        mControlsView = findViewById(R.id.fullscreen_content_controls);
        mContentView = findViewById(R.id.img_viewer_content);

        ImageView imgView = (ImageView) findViewById(R.id.img_viewer_content);
        Intent i = getIntent();
        String id = i.getStringExtra("IMG-URI");
        File f = new File(String.valueOf(this.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)) +"/" + id + ".jpg");
        System.out.println(f.getAbsolutePath().toString());
        Bitmap bitmap = BitmapFactory.decodeFile(f.getAbsolutePath());
        imgView.setImageResource(R.color.colorAccent);
        imgView.setImageBitmap(bitmap);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }
}
