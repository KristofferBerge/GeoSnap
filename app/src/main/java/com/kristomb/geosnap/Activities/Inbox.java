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
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

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

import org.w3c.dom.Text;

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
    private ArrayAdapter emptyAdapter;
    SwipeRefreshLayout swipeLayout;

    private void setInboxSource(ArrayList<ImgData> data){
        inboxSource.clear();
        for(ImgData d: data){
            inboxSource.add(d);
        }
        //If no images, show empty text
        if(data.size() == 0){
            lv.setAdapter(emptyAdapter);
            lv.setEnabled(false);
            return;
        }
        //If empty text is shown, switch back to imgDataAdapter
        if(lv.getAdapter() != imgDataAdapter){
            lv.setAdapter(imgDataAdapter);
            lv.setEnabled(true);
        }
        imgDataAdapter.notifyDataSetChanged();
    }

    //Possible error-messages that can be shown in snackbar
    private enum snackbarMessage{
        NO_LOCATION_AVAILABLE,
        GENERAL_ERROR_LOADING_IMAGEDATA
    };

    //Adding buttons to actionbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.inbox_menu,menu);
        return true;
    }

    //Event handler for buttons on actionbar
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.settings_menu_item:
                displaySettings();
                return true;
            case R.id.capture_menu_item:
                startCamera();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());

        setContentView(R.layout.activity_inbox);
        lv = (ListView) findViewById(R.id.listView);

        //Creating custom adapter to display images
        imgDataAdapter = new ImgDataAdapter(this,inboxSource);

        //Creating empty adapter to display if inbox is empty
        emptyAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1);
        emptyAdapter.add("You have no images. Go out and find some!");


        lv.setAdapter(imgDataAdapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(parent.getAdapter() == emptyAdapter){
                    return;
                }
                //Only allow click event if image is loaded and not viewed.
                ImgData data = (ImgData) parent.getAdapter().getItem(position);
                if (!data.getSeenStatus() && data.getLoadedStatus()) {
                    //Opening new fullscreen activity to display image
                    displayImgFullscreen(data.getImgId(), data.getUser());


                    //TODO: Remove this. Method moved to imgViewer activity
                    //Notify service that image is displayed
                    //notifyImageDisplayed(data.getImgId());
                }
            }
        });

        //Event handler on pull to refresh inbox
        swipeLayout = (SwipeRefreshLayout) findViewById(R.id.pulltorefresh_container);
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeLayout.setRefreshing(true);
                requestImgDataUpdate();
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
        i.putExtra("username", username);
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

                int statusCode = intent.getIntExtra("status",500);
                if(statusCode == 200){
                    ArrayList<ImgData> imgList = (ArrayList<ImgData>) intent.getSerializableExtra("ArrayList<ImgData>");
                    Collections.sort(imgList, new ImgDataComparator());
                    setInboxSource(imgList);
                    swipeLayout.setRefreshing(false);
                    System.out.println("UPDATED IMAGE LIST IS SIZE: " + imgList.size());
                }
                else if(statusCode == 400){
                    showSnackbar(snackbarMessage.NO_LOCATION_AVAILABLE);
                    swipeLayout.setRefreshing(false);
                }
                else if(statusCode == 500){
                    showSnackbar(snackbarMessage.GENERAL_ERROR_LOADING_IMAGEDATA);
                    swipeLayout.setRefreshing(false);
                }

            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(imgDataUpdateReciever, new IntentFilter("ImgDataUpdate"));

        //TODO: only for debugging
        printKeyHash(this);
    }

    private void showSnackbar(snackbarMessage message){
        //Creating snackbar and getting properties
        Snackbar sb = Snackbar.make(swipeLayout, "", Snackbar.LENGTH_LONG);
        View sbView = sb.getView();
        TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);

        if(message == snackbarMessage.NO_LOCATION_AVAILABLE){
            sb.setText("No gps-data available, please check your settings...");
            textView.setTextColor(Color.RED);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            }
        }
        else if(message == snackbarMessage.GENERAL_ERROR_LOADING_IMAGEDATA){
            sb.setText("Updating failed, please try again...");
            textView.setTextColor(Color.RED);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            }
        }
        sb.show();
    }


    //TODO: only for debugging
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
