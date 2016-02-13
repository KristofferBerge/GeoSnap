package com.example.krist.geosnap.Models;


import java.io.Serializable;
import java.sql.Timestamp;

/**
 * Created by krist on 03-Feb-16.
 */
public class ImgData implements Serializable{
    private int ImgId;
    private Timestamp mTimestamp;
    private double Lat;
    private double Lng;
    private String User;
    private boolean Seen;
    private boolean Loaded;

    public ImgData(int imgId, Timestamp timestamp, double lat, double lng, String user){
        ImgId = imgId;
        mTimestamp = timestamp;
        Lat = lat;
        Lng = lng;
        User = user;
    }
    
    public int getImgId() {
        return ImgId;
    }

    public Timestamp getmTimestamp() {
        return mTimestamp;
    }

    public double getLat() {
        return Lat;
    }

    public double getLng() {
        return Lng;
    }

    public String getUser() {
        return User;
    }

    public Boolean getSeenStatus(){return Seen;}
    public void setSeen(Boolean b){Seen = b;}
    public Boolean getLoadedStatus(){return Loaded;}
    public void setLoadedStatus(Boolean b){Loaded = b;}
}
