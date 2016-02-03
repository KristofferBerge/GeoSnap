package com.example.krist.geosnap.Services;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.IBinder;

import com.example.krist.geosnap.Models.IEventCallback;
import com.google.android.gms.location.LocationCallback;

public class GeoService extends Service implements IEventCallback {

    LocationServiceCallback locationServiceCallback;
    ApiCommunicator apiCommunicator;

    public GeoService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
    @Override
    public void onCreate(){

    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        locationServiceCallback = new LocationServiceCallback(this);
        apiCommunicator = new ApiCommunicator();

        //TODO: WHY AM I DOING THIS?
        return START_STICKY;
    }

    public void RequestApiCall(Location location){
        //TODO: replace test method with rest-call using location data
        System.out.println("ASYNC TASK SOON START");
        apiCommunicator.RequestCallback(this);
        Thread t1 = new Thread(new Runnable(){
            public void run(){
                apiCommunicator.doWork();
            }
        });
        System.out.println("ASYNC TASK STARTED");
        //Should create new thread for api call. Current thread is main activity thread
        System.out.println("SERVICE REQUESTS API CALL");
        System.out.println(location);
    }

    public void ProcessApiResult(String s){

    }

    @Override
    public void EventTrigger(String s) {
        System.out.println(s);
    }
}
