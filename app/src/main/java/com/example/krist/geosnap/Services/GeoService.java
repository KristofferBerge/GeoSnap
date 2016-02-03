package com.example.krist.geosnap.Services;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.IBinder;

import com.google.android.gms.location.LocationCallback;

public class GeoService extends Service {

    LocationServiceCallback locationServiceCallback;

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

        //TODO: WHY AM I DOING THIS?
        return START_STICKY;
    }

    public void RequestApiCall(Location location){
        //TODO: do stuff with location
        //Should create new thread for api call. Current thread is main activity thread
        System.out.println("SERVICE REQUESTS API CALL");
        System.out.println(location);
    }
}
