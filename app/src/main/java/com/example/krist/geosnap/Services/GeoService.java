package com.example.krist.geosnap.Services;

import android.app.DownloadManager;
import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.Environment;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v4.content.LocalBroadcastManager;

import com.example.krist.geosnap.Models.ImgData;

import java.util.ArrayList;

//TODO STILL NOT STARTING ON NEW TRHEAD!!!!!!!!!!
public class GeoService extends IntentService {

    LocationServiceCallback locationServiceCallback;
    ApiCommunicator apiCommunicator;
    FileDataProvider fileDataProvider;
    BroadcastReceiver mReciever;
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
        mReciever = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                System.out.println("SERVICE RECIEVED BROADCAST");
                //Sending imageData from file and requesting loading of unloaded images
                sendImageData();
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(mReciever,new IntentFilter("ImgDataRequest"));

        //Broadcastreciever for completed download
        downloadCompleteReciever = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                System.out.println("DOWNLOAD COMPLETE");
                //Updating loaded status on images in file
                fileDataProvider.markLoadedImages();
                //Sending list of images with updated loaded status, without requesting loading
                sendUpdatedImgData();
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
                sendUpdatedImgData();
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

        //TODO: WHY AM I DOING THIS?
        return START_STICKY;
    }

    public void RequestApiCall(Location location){
        //TODO: replace test method with rest-call using location data
        String result = apiCommunicator.getAllImages();
        ArrayList<ImgData> imgList = ImgProcessor.GetImgObjects(result,fileDataProvider.getCollectedImgs());
        fileDataProvider.addToImageList(imgList);
        sendImageData();
    }

    //TODO: REMOVE TEST METHOD
    private void testWriteImage(){
        apiCommunicator.test(this);
    }

    private void uploadImage(){
        //Get gps-position
        Location loc = locationServiceCallback.GetPosition();
        if(loc != null){
            apiCommunicator.UploadImage(fileDataProvider.getCachedImage(), loc.getLatitude(), loc.getLongitude(), "Root");
        }
        else{
            //TODO: Add message to user to explain why settings opens
            Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
        }
    }

    //Sending imagedata and requesting loading of unloaded images
    private void sendImageData(){
        //Loading image data from file
        ArrayList<ImgData> imgDataList = fileDataProvider.getImageList();
        for(ImgData d: imgDataList){
            //if image is not loaded
            if(!d.getLoadedStatus()){
                loadImage(d.getImgId());
            }
        }
        Intent i = new Intent("ImgData");
        i.putExtra("ArrayList<ImgData>",imgDataList);
        LocalBroadcastManager.getInstance(this).sendBroadcast(i);
        //Clean up cached images
        fileDataProvider.removeDisplayedImageFiles();
    }

    //Sending imagedata without requesting loading
    private void sendUpdatedImgData(){
        Intent i = new Intent("ImgData");
        ArrayList<ImgData> imgDataList = fileDataProvider.getImageList();
        i.putExtra("ArrayList<ImgData>",imgDataList);
        LocalBroadcastManager.getInstance(this).sendBroadcast(i);
    }

    private void loadImage(int id){
        try {
            System.out.println("LOADING IMAGE" + id);
            String urlString = "https://kristofferberge.blob.core.windows.net/img/" + String.valueOf(id) + ".jpg";
            Uri url = Uri.parse(urlString);
            DownloadManager downloader = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
            downloader.enqueue(new DownloadManager.Request(url)
                    .setTitle(String.valueOf(id))
                            //TODO: REMOVE. ONLY FOR DEBUGGING
                    .setVisibleInDownloadsUi(true)
                    .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE)
                    .setDestinationInExternalFilesDir(this, Environment.DIRECTORY_DOWNLOADS, id + ".jpg"));
            System.out.println("DOWNLOAD STARTED");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
