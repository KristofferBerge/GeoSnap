package com.example.krist.geosnap.Services;

import com.example.krist.geosnap.Models.ImgData;

import org.json.JSONArray;

import java.util.Date;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * Created by krist on 06-Feb-16.
 */
public class ImgProcessor {
    //Recieves stringified json array from api and int array containing id of images already collected
    public static ArrayList<ImgData> GetImgObjects(String s,Integer[] collectedImgs){
        ArrayList<ImgData> imgList = new ArrayList<ImgData>();
        try{
            //Parsing stringified json to object
            JSONArray arr = new JSONArray(s);
            //Creating ImgDataObject from json-objects IF the image has not been processed at an earlier point in time
            outer: for(int i=0; i<arr.length();i++){
                for(Integer j: collectedImgs){
                    if(Integer.parseInt(arr.getJSONObject(i).getString("ImgId")) == j){
                        //The user has already collected this picture. Continuing outer loop
                        continue outer;
                    }
                }
                //TODO Error handling for parsing timestamp
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                String timestampString = arr.getJSONObject(i).getString("Timestamp");
                //MYSQL-database adding T in timestamp. Java does not like...
                timestampString = timestampString.replace('T',' ');
                Date parsedDate = dateFormat.parse(timestampString);
                Timestamp timestamp = new java.sql.Timestamp(parsedDate.getTime());

                //Creating Imgdata objects and adding them to arraylist.
                ImgData d = new ImgData(
                        Integer.parseInt(arr.getJSONObject(i).getString("ImgId")),
                        arr.getJSONObject(i).getString("ImgUrl"),
                        timestamp,
                        Double.parseDouble(arr.getJSONObject(i).getString("Lat")),
                        Double.parseDouble(arr.getJSONObject(i).getString("Lng")),
                        arr.getJSONObject(i).getString("Usr")
                );
                imgList.add(d);
            }
        }
        catch(Exception e){
            e.printStackTrace();
            System.out.println("BAD JSON");
        }
        return imgList;
    }
}
