package com.example.krist.geosnap.Services;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;

import com.example.krist.geosnap.Models.ImgData;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.util.ArrayList;

//TODO STILL NOT STARTING ON NEW TRHEAD!!!!!!!!!!
public class GeoService extends IntentService {

    LocationServiceCallback locationServiceCallback;
    ApiCommunicator apiCommunicator;
    FileDataProvider fileDataProvider;
    BroadcastReceiver mReciever;

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

}
