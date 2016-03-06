package com.kristomb.geosnap.Services;

import android.app.DownloadManager;
import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.Environment;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v4.content.LocalBroadcastManager;

import com.kristomb.geosnap.Controllers.FileDataProvider;
import com.kristomb.geosnap.Models.ImgData;
import com.kristomb.geosnap.Activities.UserSettings;

import java.util.ArrayList;

//TODO STILL NOT STARTING ON NEW TRHEAD!!!!!!!!!!
public class GeoService extends IntentService {

    LocationServiceCallback locationServiceCallback;
    ApiCommunicator apiCommunicator;
    FileDataProvider fileDataProvider;
    BroadcastReceiver imageDataRequestReciever;
    BroadcastReceiver forceUpdateRequestReciever;
    BroadcastReceiver downloadCompleteReciever;
    BroadcastReceiver imgDisplayedReciever;
    BroadcastReceiver uploadImageReciever;
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public GeoService(String name) {
        super(name);
    }
    public GeoService(){
        super("NewThread");
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

    }

    @Override
    public void onCreate(){

    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        locationServiceCallback = new LocationServiceCallback(this);
        apiCommunicator = new ApiCommunicator(this);
        fileDataProvider = new FileDataProvider(this);

        //Setting up broadcastreciever to handle request from activity
        imageDataRequestReciever = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                System.out.println("SERVICE REQUESTED IMAGE DATA");
                //Sending imageData from file and requesting loading of unloaded images
                sendImgDataToActivity();
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(imageDataRequestReciever,new IntentFilter("ImgDataRequest"));

        //Broadcastreciever for update request from activity
        forceUpdateRequestReciever = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                System.out.println("Recieved");
                Location lastknownLocation = locationServiceCallback.GetPosition();
                if(lastknownLocation != null){
                    System.out.println("Location found, updating");
                    RequestApiCall(lastknownLocation);
                }
                System.out.println("No location found");
                //TODO: Error if no position is found
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(forceUpdateRequestReciever, new IntentFilter("ForceUpdateImage"));

        //Broadcastreciever for completed download
        downloadCompleteReciever = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                System.out.println("DOWNLOAD COMPLETE");
                //Updating loaded status on images in file
                fileDataProvider.markLoadedImages();
                //Sending list of images with updated loaded status, without requesting loading
                sendImgDataToActivity();
            }
        };
        this.registerReceiver(downloadCompleteReciever, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

        //Broadcastreciever for image displayed
        imgDisplayedReciever = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                System.out.println("RECIEVED");
                String id = intent.getStringExtra("ID");
                fileDataProvider.markImageViewed(Integer.parseInt(id));
                //Sending list of images with updated loaded status, without requesting loading
                sendImgDataToActivity();
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(imgDisplayedReciever, new IntentFilter("ImgDisplayed"));
        //Broadcastreciever for image displayed
        uploadImageReciever = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                System.out.println("UPLOAD IMAGE REQUEST RECIEVED");
                uploadImage();
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(uploadImageReciever, new IntentFilter("UploadImage"));

        //Send image data back to activity when started
        sendImgDataToActivity();

        //TODO: WHY AM I DOING THIS?
        return START_STICKY;
    }

    //Sending imagedata without requesting loading
    private void sendImgDataToActivity(){
        ArrayList<ImgData> list = fileDataProvider.getImageList();
        Intent i = new Intent("ImgDataUpdate");
        i.putExtra("ArrayList<ImgData>",list);
        LocalBroadcastManager.getInstance(this).sendBroadcast(i);
    }


    public void loadImage(int id){
        //Check if image already is downloading
        DownloadManager downloader = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        DownloadManager.Query query = new DownloadManager.Query();
        query.setFilterByStatus(DownloadManager.STATUS_RUNNING | DownloadManager.STATUS_PENDING);
        Cursor c = downloader.query(query);
        while(c.moveToNext()){
            int title = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_TITLE));
            if(title == id){
                System.out.println("Image " + id + " is already downloading. Aborting...");
                return;
            }
        }
        try {
            String urlString = "https://kristofferberge.blob.core.windows.net/img/" + String.valueOf(id) + ".jpg";
            Uri url = Uri.parse(urlString);
            downloader.enqueue(new DownloadManager.Request(url)
                    .setTitle(String.valueOf(id))
                            //TODO: REMOVE. ONLY FOR DEBUGGING
                    .setVisibleInDownloadsUi(true)
                    .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE)
                    .setDestinationInExternalFilesDir(this, Environment.DIRECTORY_DOWNLOADS, id + ".jpg"));
            System.out.println("Downloading image: " + id);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    ////////////////////////////////////////////////////////////////////
    //OLD METHODS BELOW
    ////////////////////////////////////////////////////////////////////


    public void RequestApiCall(Location location){
        //TODO: replace test method with rest-call using location data
        String result = apiCommunicator.getAllImages();
        ArrayList<ImgData> imgList = ImgProcessor.GetImgObjects(result,fileDataProvider.getCollectedImgs());
        fileDataProvider.addToImageList(imgList);
        sendImgDataToActivity();
    }

    private void uploadImage(){
        //Get gps-position
        Location loc = locationServiceCallback.GetPosition();
        //Accessing shared preferences
        SharedPreferences settings = getSharedPreferences(UserSettings.USER_SETTINGS_RESOURCE, 0);
        String username = settings.getString(UserSettings.USERNAME_SETTING, "unknown");
        //TODO: USE CONF ID WHEN IMPLEMENTED BY API
        int confId = settings.getInt(UserSettings.CONFID_SETTING, 0);
        if(loc != null){
            apiCommunicator.UploadImage(fileDataProvider.getCachedImage(), loc.getLatitude(), loc.getLongitude());
        }
        else{
            //TODO: Add message to user to explain why settings opens
            Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
        }
    }
}
