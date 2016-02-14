package com.example.krist.geosnap.Models;

import java.util.Comparator;

/**
 * Created by krist on 14-Feb-16.
 */
public class ImgDataComparator implements Comparator<ImgData> {
    @Override
    public int compare(ImgData d1,ImgData d2){
        return d2.getmTimestamp().compareTo(d1.getmTimestamp());
    }
}

