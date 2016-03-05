package com.example.krist.geosnap.Activities;

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
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.krist.geosnap.R;

import org.w3c.dom.Text;

import java.io.File;
import java.io.IOException;

public class ImgUploader extends AppCompatActivity {

    BroadcastReceiver uploadStatusReciever;
    ProgressBar uploadProgressBar;
    Button uploadButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_img_uploader);
    }

    @Override
    protected void onStart(){
        super.onStart();
        uploadProgressBar = (ProgressBar)findViewById(R.id.uploadProgressSpinner);
        uploadProgressBar.setVisibility(View.GONE);
        ImageView imgView = (ImageView)findViewById(R.id.uploaderImageView);
        String dir = String.valueOf(this.getExternalFilesDir(Environment.DIRECTORY_PICTURES));
        String filepath = dir + "/cachedImage.jpg";
        File file = new File(filepath);
        Uri fileUri = Uri.fromFile(file);
        imgView.setImageURI(fileUri);

        uploadButton = (Button) findViewById(R.id.uploaderShareButton);
        uploadButton.setOnClickListener(new View.OnClickListener() {
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
                else{
                    uploadProgressBar.setVisibility(View.GONE);
                    uploadButton.setEnabled(true);
                    TextView t = (TextView)findViewById(R.id.uploadErrorText);
                    t.setText("Uploading failed...");
                    uploadButton.setText("Try again");
                }

            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(uploadStatusReciever, new IntentFilter("UploadStatus"));

    }

    private void requestUpload(){
        uploadProgressBar.setVisibility(View.VISIBLE);
        uploadButton.setEnabled(false);
        Intent i = new Intent("UploadImage");
        LocalBroadcastManager.getInstance(this).sendBroadcast(i);
    }



}
