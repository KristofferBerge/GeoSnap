package com.kristomb.geosnap.Activities;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import com.facebook.AccessToken;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.kristomb.geosnap.Adapters.ImgDataAdapter;
import com.kristomb.geosnap.Models.ImgData;
import com.kristomb.geosnap.Models.ImgDataComparator;
import com.kristomb.geosnap.R;
import com.kristomb.geosnap.Services.GeoService;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.common.GoogleApiAvailability;

import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;


public class Inbox extends AppCompatActivity {

    private BroadcastReceiver imgDataUpdateReciever;
    private ListView lv;
    private ArrayList<ImgData> inboxSource = new ArrayList<ImgData>();
    private ImgDataAdapter imgDataAdapter;

    private void setInboxSource(ArrayList<ImgData> data){
        inboxSource.clear();
        for(ImgData d: data){
            inboxSource.add(d);
        }
        imgDataAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());

        setContentView(R.layout.activity_inbox);
        lv = (ListView) findViewById(R.id.listView);
        imgDataAdapter = new ImgDataAdapter(this,inboxSource);
        //ImgDataAdapter arrayAdapter = new ImgDataAdapter(this, inboxSource);
        lv.setAdapter(imgDataAdapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Only allow click event if image is loaded and not viewed.
                ImgData data = (ImgData) parent.getAdapter().getItem(position);
                if (!data.getSeenStatus() && data.getLoadedStatus()) {
                    //Opening new fullscreen activity to display image
                    displayImgFullscreen(data.getImgId(),data.getUser());


                    //TODO: Remove this. Method moved to imgViewer activity
                    //Notify service that image is displayed
                    //notifyImageDisplayed(data.getImgId());
                }
            }
        });
        Button b = (Button) findViewById(R.id.testKnapp);
        b.setOnClickListener(new View.OnClickListener() {
                                 @Override
                                 public void onClick(View v) {
                                     requestImgDataUpdate();
                                 }
                             }
        );
        Button b2 = (Button) findViewById(R.id.uploadTest);
        b2.setOnClickListener(new View.OnClickListener() {
                                 @Override
                                 public void onClick(View v) {
                                    startCamera();
                                 }
                             }
        );
        Button settingsButton = (Button) findViewById(R.id.openSettingsButton);
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displaySettings();
            }
        });


        //Checking if google play services is available
        int r = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);
        if(r != 0){
            //If not available, user is prompted to install or update play services
            Dialog d = GoogleApiAvailability.getInstance().getErrorDialog(this,r,1);
            d.show();
        }
        //TODO: Do complete check if everything is good to go and start service'

        startService(new Intent(this,GeoService.class));
    }

    private void displayImgFullscreen(int id,String username){
        ImgViewer viewer = new ImgViewer();
        Intent i = new Intent(this,ImgViewer.class);
        i.putExtra("IMG-URI", Integer.toString(id));
        i.putExtra("username",username);
        startActivity(i);
    }


    //Moved to imgViewer activity
    private void notifyImageDisplayed(int id){
        Intent i = new Intent("ImgDisplayed");
        i.putExtra("ID",Integer.toString(id));
        LocalBroadcastManager.getInstance(this).sendBroadcast(i);
        System.out.println("IMGDISPLAYED BROADCAST SENT");
    }

    private void displayImgUploader(){
        ImgUploader uploader = new ImgUploader();
        Intent i = new Intent(this,ImgUploader.class);
        startActivity(i);
    }
    private void displaySettings(){
        Intent i = new Intent(this,UserSettings.class);
        startActivity(i);
    }

    private void displayLoginPage(){
        Intent i = new Intent(this,UserSettings.class);
        startActivity(i);
    }

    @Override
    public void onStart() {
        super.onStart();

        //TODO: Make generic static method to call in top of all activities
        if(AccessToken.getCurrentAccessToken() == null ||AccessToken.getCurrentAccessToken().isExpired()){
            displayLoginPage();
            this.finish();
        }

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Inbox Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://com.krist.geosnap/http/host/path")
        );

        imgDataUpdateReciever = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                System.out.println("ACTIVITY RECIEVED UPDATED IMAGES");
                ArrayList<ImgData> imgList = (ArrayList<ImgData>) intent.getSerializableExtra("ArrayList<ImgData>");
                Collections.sort(imgList, new ImgDataComparator());
                setInboxSource(imgList);
                System.out.println("UPDATED IMAGE LIST IS SIZE: " + imgList.size());
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(imgDataUpdateReciever, new IntentFilter("ImgDataUpdate"));

        printKeyHash(this);
    }


    //For debugging
    public static String printKeyHash(Activity context) {
        PackageInfo packageInfo;
        String key = null;
        try {
            //getting application package name, as defined in manifest
            String packageName = context.getApplicationContext().getPackageName();

            //Retriving package info
            packageInfo = context.getPackageManager().getPackageInfo(packageName,
                    PackageManager.GET_SIGNATURES);

            Log.e("Package Name=", context.getApplicationContext().getPackageName());

            for (Signature signature : packageInfo.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                key = new String(Base64.encode(md.digest(), 0));

                // String key = new String(Base64.encodeBytes(md.digest()));
                Log.e("Key Hash=", key);
            }
        } catch (PackageManager.NameNotFoundException e1) {
            Log.e("Name not found", e1.toString());
        }
        catch (NoSuchAlgorithmException e) {
            Log.e("No such an algorithm", e.toString());
        } catch (Exception e) {
            Log.e("Exception", e.toString());
        }

        return key;
    }


    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Inbox Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://com.krist.geosnap/http/host/path")
        );
    }

    private void requestImgDataFromService(){
        Intent i = new Intent("ImgDataRequest");
        LocalBroadcastManager.getInstance(this).sendBroadcast(i);
    }
    private void requestImgDataUpdate(){
        System.out.println("Button clicked");
        Intent i = new Intent("ForceUpdateImage");
        LocalBroadcastManager.getInstance(this).sendBroadcast(i);
    }


    private void startCamera(){
        //PREPARE OUTPUT FILE
        String dir = String.valueOf(this.getExternalFilesDir(Environment.DIRECTORY_PICTURES));
        String filepath = dir + "/cachedImage.jpg";
        File outputFile = new File(filepath);
        try {
            outputFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Uri outputFileUri = Uri.fromFile(outputFile);
        Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        i.putExtra(MediaStore.EXTRA_OUTPUT,outputFileUri);
        startActivityForResult(i, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 0 && resultCode == RESULT_OK){
            displayImgUploader();
        }
    }

}
