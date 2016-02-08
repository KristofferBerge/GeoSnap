package com.example.krist.geosnap.Services;

import android.content.Context;
import android.os.Environment;

import com.example.krist.geosnap.Models.ImgData;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;

/**
 * Created by krist on 06-Feb-16.
 */
public class FileDataProvider {
    Context C;

    public FileDataProvider(Context c){
        //Storing context of service for use when reading files
        C = c;
    }

    //TODO: Implement method to "Clean" image-file. Delete pictures that have expired

    //Returns imgData objects stored in file
    public ArrayList<ImgData> getImageList(){
        try {
            FileInputStream fis = C.openFileInput("ImgData.dat");
            ObjectInputStream is = new ObjectInputStream(fis);
            ArrayList<ImgData> imgList = (ArrayList<ImgData>) is.readObject();
            return imgList;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (StreamCorruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    //Adding imgData objects to file
    public void addToImageList(ArrayList<ImgData> list){

        ArrayList<ImgData> oldList = getImageList();
        for(ImgData d: list){
            oldList.add(d);
        }
        try {
            FileOutputStream fos = C.openFileOutput("ImgData.dat", C.MODE_PRIVATE);
            ObjectOutputStream os = new ObjectOutputStream(fos);
            os.writeObject(oldList);
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void overWriteImageList(ArrayList<ImgData> list){
        try {
            FileOutputStream fos = C.openFileOutput("ImgData.dat", C.MODE_PRIVATE);
            ObjectOutputStream os = new ObjectOutputStream(fos);
            os.writeObject(list);
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    public Integer[] getLoadedImages(){
        //Getting directory where downloadManager is saving images
        File f = new File(String.valueOf(C.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)));
        //Getting contents of file
        File[] fileArr = f.listFiles();
        Integer[] imgIdArr = new Integer[fileArr.length];
        //Adding filename parsed to imgId to array
        for(int i = 0; i < fileArr.length; i++){
            System.out.println(fileArr[i].getName());
            try {
                imgIdArr[i] = ((Number) NumberFormat.getInstance().parse(fileArr[i].getName())).intValue();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return imgIdArr;
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
        overWriteImageList(imgList);
    }

    //Marking images as viewed and overwriting data-file
    public void markImageViewed(int id){
        ArrayList<ImgData> imgList = getImageList();
        for(ImgData d:imgList){
            if(d.getImgId() == id){
                d.setSeen(true);
            }
        }
        overWriteImageList(imgList);
    }

    //Removing image files already displayed or not matching any id in data-file
    public void removeDisplayedImageFiles(){
        File f = new File(String.valueOf(C.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)));
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
