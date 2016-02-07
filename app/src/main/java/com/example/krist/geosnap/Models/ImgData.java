package com.example.krist.geosnap.Models;


import java.io.Serializable;
import java.sql.Timestamp;

/**
 * Created by krist on 03-Feb-16.
 */
public class ImgData implements Serializable{
    private int ImgId;
    private String ImgUrl;
    private Timestamp mTimestamp;
    private double Lat;
    private double Lng;
    private String User;
    private boolean Seen;

    public ImgData(int imgId, String imgUrl, Timestamp timestamp, double lat, double lng, String user){
        ImgId = imgId;
        ImgUrl = imgUrl;
        mTimestamp = timestamp;
        Lat = lat;
        Lng = lng;
        User = user;
    }
    
    public int getImgId() {
        return ImgId;
    }

    public String getImgUrl() {
        return ImgUrl;
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
    public void setSeen(Boolean b){Seen = b;};
}
