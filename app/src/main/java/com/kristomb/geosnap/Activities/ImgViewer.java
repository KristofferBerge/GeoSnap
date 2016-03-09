package com.kristomb.geosnap.Activities;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;

import com.kristomb.geosnap.R;

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
    private String imgId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        setContentView(R.layout.activity_img_viewer);

        mVisible = true;
        mControlsView = findViewById(R.id.fullscreen_content_controls);
        mContentView = findViewById(R.id.img_viewer_content);

        ImageView imgView = (ImageView) findViewById(R.id.img_viewer_content);
        Intent i = getIntent();
        imgId = i.getStringExtra("IMG-URI");
        File f = new File(String.valueOf(this.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)) +"/" + imgId + ".jpg");
        System.out.println(f.getAbsolutePath().toString());
        Bitmap bitmap = BitmapFactory.decodeFile(f.getAbsolutePath());
        bitmap = scaleBitmap(bitmap);
        imgView.setImageResource(R.color.colorAccent);
        imgView.setImageBitmap(bitmap);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        notifyImageDisplayed();
    }

    private void notifyImageDisplayed(){
        Intent i = new Intent("ImgDisplayed");
        i.putExtra("ID",imgId);
        LocalBroadcastManager.getInstance(this).sendBroadcast(i);
        System.out.println("IMGDISPLAYED BROADCAST SENT");
    }

    private Bitmap scaleBitmap(Bitmap unscaledBitmap){
        //Getting display properties
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int height = size.y;
        int width = size.x;
        //Returning proper sized bitmap
        return Bitmap.createScaledBitmap(unscaledBitmap, width, height, true);
    }
    private Bitmap rotateBitmap(Bitmap unrotatedBitmap){
        return null;
    }
}
