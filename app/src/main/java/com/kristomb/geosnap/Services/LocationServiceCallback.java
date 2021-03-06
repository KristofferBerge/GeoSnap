package com.kristomb.geosnap.Services;

import android.Manifest;
import android.app.Application;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;

import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

/**
 * Created by krist on 03-Feb-16.
 */
public class LocationServiceCallback extends Application implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    private GoogleApiClient mGoogleApiClient;
    private GeoService ParentService;

    public LocationServiceCallback(GeoService parentService) {
        ParentService = parentService;
        mGoogleApiClient = new GoogleApiClient.Builder(parentService)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .addApi(AppIndex.API).build();
        mGoogleApiClient.connect();
    }

    public Location GetPosition() {
        if (ActivityCompat.checkSelfPermission(ParentService, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(ParentService, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return null;
        }
        if(mGoogleApiClient.isConnected()){
            return LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        }
        else{
            mGoogleApiClient.connect();
            return null;
        }
    }

    public void requestLocationUpdate() {
        LocationRequest mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(300000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        //Checking if the app has sufficient permissions to request location
        if (ActivityCompat.checkSelfPermission(ParentService, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(ParentService, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        //Requesting that play services trigger callback method "onLocationChanged" whenever location changes.
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    //Turning off discovery mode
    public void unRequestLocationUpdate(){
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    @Override
    public void onConnected(Bundle bundle) {
        SharedPreferences settings = ParentService.getSharedPreferences("UserSettings", 0);
        boolean discoveryMode = settings.getBoolean("DiscoveryMode", false);
        if(discoveryMode){
            requestLocationUpdate();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {
        ParentService.RequestApiCall(location);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }
}
