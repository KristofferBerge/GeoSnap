package com.kristomb.geosnap.Controllers;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;

import com.kristomb.geosnap.Models.ImgData;
import com.kristomb.geosnap.Services.ApiCommunicator;
import com.kristomb.geosnap.Services.GeoService;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Timestamp;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by krist on 06-Feb-16.
 */
public class FileDataProvider {
    GeoService geoService;

    public FileDataProvider(GeoService c){
        //Storing context of service for use when reading files
        geoService = c;
    }

    //Overwrites file with and returns empty list
    private ArrayList<ImgData> resetImageDataFile(){
        System.out.println("RESETTING IMAGE DATA FILE");
        ArrayList<ImgData> emptyList = new ArrayList<ImgData>();
        overWriteImageDataFile(emptyList);
        return emptyList;
    }

    private void overWriteImageDataFile(ArrayList<ImgData> list){
        System.out.println("Overwriting image file with size: " + list.size());
        try {
            FileOutputStream fos = geoService.openFileOutput("ImgData.dat", geoService.MODE_PRIVATE);
            ObjectOutputStream os = new ObjectOutputStream(fos);
            os.writeObject(list);
            os.close();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private ArrayList<ImgData> removeOldFileData(ArrayList<ImgData> inputList){
        //New outputList to store return values while iterating input
        ArrayList<ImgData> outputList = new ArrayList<>();
        //timestamp for 24 hours ago
        Calendar c = Calendar.getInstance();
        Date date = new Date();
        c.setTime(date);
        c.add(Calendar.DATE, -1);
        Timestamp yesterday = new Timestamp(c.getTimeInMillis());
        //If image is taken in the last 24 hours, store in return list
        for(ImgData d: inputList){
            if(d.getmTimestamp().after(yesterday)){
                System.out.println("Image: " + d.getImgId() + " is not old");
                outputList.add(d);
            }
        }
        //Update file and return
        System.out.println("LIST OF NEW IMAGES SIZE: " + outputList.size());
        overWriteImageDataFile(outputList);
        return outputList;
    }


    private void initiateLoading(ArrayList<ImgData> inputList){
        Integer[] loadedImages = getLoadedImages();

        System.out.println("IMAGES LOADED:");
        for(int i = 0; i < loadedImages.length; i++){
            System.out.print(loadedImages[i] + ", ");
        }
        outer: for(ImgData d: inputList){
            //If image has not been displayed
            if(!d.getSeenStatus()){
                //Checking if image is loaded
                for(int i=0; i<loadedImages.length; i++){
                    if(loadedImages[i] == d.getImgId()){
                        //If loaded, go to next picture
                        d.setLoadedStatus(true);
                        continue outer;
                    }
                }
                //If not loaded, request load
                d.setLoadedStatus(false);
                geoService.loadImage(d.getImgId());
            }
        }
    }

    private Integer[] getLoadedImages(){
        //Getting directory where downloadManager is saving images
        File f = new File(String.valueOf(geoService.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)));
        //Getting contents of file
        File[] fileArr = f.listFiles();
        Integer[] imgIdArr = new Integer[fileArr.length];
        //Adding filename parsed to imgId to array
        for(int i = 0; i < fileArr.length; i++){
            try {
                imgIdArr[i] = ((Number) NumberFormat.getInstance().parse(fileArr[i].getName())).intValue();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return imgIdArr;
    }


    //Returns imgData objects stored in file
    public ArrayList<ImgData> getImageList(){
        try {
            FileInputStream fis = geoService.openFileInput("ImgData.dat");
            ObjectInputStream is = new ObjectInputStream(fis);
            ArrayList<ImgData> imgList = (ArrayList<ImgData>) is.readObject();
            System.out.println("IMGDATA LIST SIZE: " + imgList.size());
            is.close();
            fis.close();
            imgList = removeOwnImages(imgList);
            //Removing old files from list and updating file
            imgList = removeOldFileData(imgList);
            initiateLoading(imgList);
            return imgList;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        //Something went wrong reading the file
        //Restore file and return empty list
        System.out.println("RESETTING FILE");
        return resetImageDataFile();
    }


    //Filters out users own images
    private ArrayList<ImgData> removeOwnImages(ArrayList<ImgData> inputList){
        SharedPreferences settings = geoService.getSharedPreferences("UserSettings", 0);
        boolean getOwn = settings.getBoolean("getOwn", false);
        //If user wants to recieve own images, stop here
        if(getOwn){
            return inputList;
        }
        String username = settings.getString("Username", "");
        ArrayList<ImgData> outputList = new ArrayList<ImgData>();
        for(ImgData d: inputList){
            System.out.println("PICTURE FROM " + d.getUser() + " I AM " + username);
            if(!d.getUser().contentEquals(username)){
                outputList.add(d);
                System.out.println("ADDING");
            }
        }
        return outputList;
    }

    //Adding imgData objects to file
    public void addToImageList(ArrayList<ImgData> list){
        ArrayList<ImgData> oldList = getImageList();
        for(ImgData d: list){
            oldList.add(d);
        }
        System.out.println("FINISHED ADDING IMAGES. OVERVRITING WITH SIZE: " + oldList.size());
        overWriteImageDataFile(oldList);
    }

    //Marking images as viewed and overwriting data-file
    public void markImageViewed(int id){
        ArrayList<ImgData> imgList = getImageList();
        for(ImgData d:imgList){
            if(d.getImgId() == id){
                d.setSeen(true);
                System.out.println("Marking: " + d.getImgId() + " as seen");
            }
        }
        overWriteImageDataFile(imgList);
        removeDisplayedImageFiles();
    }

    public int getNumberOfUnviewedImages(){
        int i = 0;
        for(ImgData img: getImageList()){
            if(!img.getSeenStatus())
                i++;
        }
        return i;
    }

    //Returns string of imgIds
    public Integer[] getCollectedImgs(){
        ArrayList<ImgData> imgList = getImageList();
        Integer[] idArray = new Integer[imgList.size()];
        for(int i=0; i<imgList.size(); i++) {
            idArray[i] = imgList.get(i).getImgId();
        }
        return idArray;
    }



    //Marking images as loaded and overwriting data-file
    public void markLoadedImages(){
        ArrayList<ImgData> imgList = getImageList();
        Integer[] loadedList = getLoadedImages();
        for(ImgData imgData:imgList){
            for(int i=0; i<loadedList.length; i++){
                if(imgData.getImgId() == loadedList[i]){
                    imgData.setLoadedStatus(true);
                }
            }
        }
        overWriteImageDataFile(imgList);
        removeDisplayedImageFiles();
    }


    public Bitmap getCachedImage(){
        File f = new File(String.valueOf(geoService.getExternalFilesDir(Environment.DIRECTORY_PICTURES + "/cachedImage.jpg")));
        //Parsing image-file to bitmap
        return BitmapFactory.decodeFile(f.getPath());
    }

    //Removing image files already displayed or not matching any id in data-file
    public void removeDisplayedImageFiles(){
        File f = new File(String.valueOf(geoService.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)));
        //Getting contents of file
        File[] fileArr = f.listFiles();
        //Getting all images
        ArrayList<ImgData> imgList = getImageList();
        outer: for(int i = 0; i < fileArr.length; i++){
            int id = 0;
            try {
                //Setting id based on file name
                Number n = ((Number) NumberFormat.getInstance().parse(fileArr[i].getName())).intValue();
                id = Integer.parseInt(n.toString());
            } catch (ParseException e) {
                e.printStackTrace();
            }
            //Check for matching id in data-file
            for(ImgData d: imgList){
                //If same id
                if(d.getImgId() == id){
                    //If seen
                    if(d.getSeenStatus()){
                        //Remove file
                        fileArr[i].delete();
                    }
                    //Matching file was found
                    //break out of inner loop and continue outer.
                    continue outer;
                }
            }
            //If matching id was not found in data-file
            //Remove file
            fileArr[i].delete();
        }
    }
}
