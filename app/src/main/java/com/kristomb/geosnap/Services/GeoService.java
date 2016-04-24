package com.kristomb.geosnap.Services;

import android.app.DownloadManager;
import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.Environment;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.NotificationCompat;

import com.kristomb.geosnap.Activities.Inbox;
import com.kristomb.geosnap.Controllers.FileDataProvider;
import com.kristomb.geosnap.Models.ImgData;
import com.kristomb.geosnap.R;

import java.util.ArrayList;

public class GeoService extends IntentService {

    LocationServiceCallback locationServiceCallback;
    ApiCommunicator apiCommunicator;
    FileDataProvider fileDataProvider;
    BroadcastReceiver imageDataRequestReciever;
    BroadcastReceiver forceUpdateRequestReciever;
    BroadcastReceiver downloadCompleteReciever;
    BroadcastReceiver imgDisplayedReciever;
    BroadcastReceiver uploadImageReciever;
    BroadcastReceiver setDiscoveryModeReciever;
    BroadcastReceiver voteReceiver;
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
                    //RequestApiCall(lastknownLocation);
                    Runnable r = new RequestApiThread(lastknownLocation);
                    new Thread(r).start();
                }
                else{
                    sendErrorToInbox(400);
                    System.out.println("No location found");
                }
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

        //Broadcastreciever for uploading image
        uploadImageReciever = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                System.out.println("UPLOAD IMAGE REQUEST RECIEVED");
                uploadImage();
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(uploadImageReciever, new IntentFilter("UploadImage"));

        //Broadcastreciever to toggle discovery mode
        setDiscoveryModeReciever = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Boolean setting = intent.getBooleanExtra("setting",true);
                if(setting){
                    locationServiceCallback.requestLocationUpdate();
                }
                else{
                    locationServiceCallback.unRequestLocationUpdate();
                }
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(setDiscoveryModeReciever,new IntentFilter("SetDiscoveryMode"));

        //Broadcastreciever for sending vote to api
        voteReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String username = intent.getStringExtra("username");
                int vote = intent.getIntExtra("vote",0);
                apiCommunicator.vote(username,vote);
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(voteReceiver,new IntentFilter("Vote"));


    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        //Send image data back to activity when started
        sendImgDataToActivity();
        return START_STICKY;
    }

    //Sending imagedata without requesting loading
    private void sendImgDataToActivity(){
        ArrayList<ImgData> list = fileDataProvider.getImageList();
        Intent i = new Intent("ImgDataUpdate");
        i.putExtra("ArrayList<ImgData>", list);
        i.putExtra("status",200);
        LocalBroadcastManager.getInstance(this).sendBroadcast(i);
    }
    private void sendErrorToInbox(int errorCode){
        ArrayList<ImgData> list = fileDataProvider.getImageList();
        Intent i = new Intent("ImgDataUpdate");
        i.putExtra("ArrayList<ImgData>", list);
        i.putExtra("status",errorCode);
        LocalBroadcastManager.getInstance(this).sendBroadcast(i);
    }

    //Using the download manager to download images in background
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
                    .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE)
                    .setDestinationInExternalFilesDir(this, Environment.DIRECTORY_DOWNLOADS, id + ".jpg"));
            System.out.println("Downloading image: " + id);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void RequestApiCall(Location location){
        //Reading current settings
        SharedPreferences settings =getSharedPreferences("UserSettings", 0);
        boolean debugMode = settings.getBoolean("debugMode", false);
        int range = settings.getInt("Range",50);
        String result = null;
        if(debugMode){
            result = apiCommunicator.getAllImages();
        }
        else{
            result = apiCommunicator.getImagesInRange(location, range);
        }

        ArrayList<ImgData> imgList = ImgProcessor.GetImgObjects(result,fileDataProvider.getCollectedImgs());
        //Creating notification with number of new images
        fileDataProvider.addToImageList(imgList);
        if(imgList.size()>0)
        Notify();
        sendImgDataToActivity();
    }

    //Does a api call on a new thread to avoid freezing UI
    public class RequestApiThread implements Runnable{
        Location location;
        public RequestApiThread(Location location){
            this.location = location;
        }
        public void run(){
            //Reading current settings
            SharedPreferences settings = getSharedPreferences("UserSettings", 0);
            boolean debugMode = settings.getBoolean("debugMode", false);
            int range = settings.getInt("Range",50);
            String result = null;
            if(debugMode){
                result = apiCommunicator.getAllImages();
            }
            else{
                result = apiCommunicator.getImagesInRange(location, range);
            }

            ArrayList<ImgData> imgList = ImgProcessor.GetImgObjects(result,fileDataProvider.getCollectedImgs());
            fileDataProvider.addToImageList(imgList);
            sendImgDataToActivity();
        }
    }

    private void uploadImage(){
        //Get gps-position
        Location loc = locationServiceCallback.GetPosition();
        if(loc != null){
            apiCommunicator.UploadImage(fileDataProvider.getCachedImage(), loc.getLatitude(), loc.getLongitude());
        }
        else{
            Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
        }
    }

    //Creates notification
    private void Notify(){
        Resources res = getResources();
        NotificationCompat.Builder notificationBuilder = (NotificationCompat.Builder) new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_camera_alt_24dp)
                .setContentTitle(res.getQuantityString(R.plurals.numberOfNewImagesFound, fileDataProvider.getNumberOfUnviewedImages()))
                .setContentText(getString(R.string.clickToOpenInGeoSnap));

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, Inbox.class), PendingIntent.FLAG_UPDATE_CURRENT);

        notificationBuilder.setContentIntent(contentIntent);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(1772,notificationBuilder.build());
    }
}
