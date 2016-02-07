package com.example.krist.geosnap.Services;

import android.app.DownloadManager;
import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.Environment;
import android.os.IBinder;
import android.service.wallpaper.WallpaperService;
import android.support.v4.content.LocalBroadcastManager;

import com.example.krist.geosnap.Models.ImgData;

import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;

//TODO STILL NOT STARTING ON NEW TRHEAD!!!!!!!!!!
public class GeoService extends IntentService {

    LocationServiceCallback locationServiceCallback;
    ApiCommunicator apiCommunicator;
    FileDataProvider fileDataProvider;
    BroadcastReceiver mReciever;
    BroadcastReceiver downloadCompleteReciever;

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
        apiCommunicator = new ApiCommunicator();
        fileDataProvider = new FileDataProvider(this);

        //Setting up broadcastreciever to handle request from activity
        mReciever = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                System.out.println("SERVICE RECIEVED BROADCAST");
                returnImageData();
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(mReciever,new IntentFilter("ImgDataRequest"));
        downloadCompleteReciever = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                System.out.println("DOWNLOAD COMPLETE");
            }
        };
        this.registerReceiver(downloadCompleteReciever, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        loadImage(10);
        fileDataProvider.getLoadedImages();

        //TODO: WHY AM I DOING THIS?
        return START_STICKY;
    }


    public void RequestApiCall(Location location){
        //TODO: replace test method with rest-call using location data
        String result = apiCommunicator.doWork();
        ArrayList<ImgData> imgList = ImgProcessor.GetImgObjects(result,fileDataProvider.getCollectedImgs());
        System.out.println(imgList.size());
        fileDataProvider.updateImageList(imgList);
        System.out.println(fileDataProvider.getImageList().size());
        System.out.println(location);
    }

    public ArrayList<ImgData> GetImageList(){
        return null;
    }

    private void returnImageData(){
        Intent i = new Intent("ImgData");
        i.putExtra("ArrayList<ImgData>",fileDataProvider.getImageList());
        LocalBroadcastManager.getInstance(this).sendBroadcast(i);
    }

    private void loadImage(int id){
        try {
            String urlString = "http://www.kristofferberge.no/img/" + String.valueOf(id) + ".jpg";
            Uri url = Uri.parse(urlString);
            System.out.println(urlString);
            DownloadManager downloader = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
            downloader.enqueue(new DownloadManager.Request(url)
                    .setTitle(String.valueOf(id))
                    .setVisibleInDownloadsUi(true)
                    .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE)
                    .setDestinationInExternalFilesDir(this, Environment.DIRECTORY_DOWNLOADS, id + ".jpg"));
            System.out.println("DOWNLOAD STARTED");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
