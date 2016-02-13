package com.example.krist.geosnap;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.File;
import java.io.IOException;

public class ImgUploader extends AppCompatActivity {

    BroadcastReceiver uploadStatusReciever;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_img_uploader);
    }

    @Override
    protected void onStart(){
        super.onStart();
        ImageView imgView = (ImageView)findViewById(R.id.uploaderImageView);
        String dir = String.valueOf(this.getExternalFilesDir(Environment.DIRECTORY_PICTURES));
        String filepath = dir + "/cachedImage.jpg";
        File file = new File(filepath);
        Uri fileUri = Uri.fromFile(file);
        imgView.setImageURI(fileUri);

        Button b = (Button) findViewById(R.id.uploaderShareButton);
        b.setOnClickListener(new View.OnClickListener() {
                                 @Override
                                 public void onClick(View v) {
                                     requestUpload();
                                 }
                             }
        );

        uploadStatusReciever = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String status = intent.getStringExtra("STATUS");
                System.out.println(intent.getStringExtra("STATUS"));
                if(status == "200"){
                    System.out.println("STATUS 200 RECIEVED");
                    finish();
                }

            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(uploadStatusReciever, new IntentFilter("UploadStatus"));

    }

    private void requestUpload(){
        Intent i = new Intent("UploadImage");
        LocalBroadcastManager.getInstance(this).sendBroadcast(i);
    }



}
