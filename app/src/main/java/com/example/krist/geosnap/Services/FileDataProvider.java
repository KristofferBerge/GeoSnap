package com.example.krist.geosnap.Services;

import android.content.Context;

import com.example.krist.geosnap.Models.ImgData;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
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
    public void updateImageList(ArrayList<ImgData> list){

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

    //Returns string of imgIds
    public Integer[] getCollectedImgs(){
        ArrayList<ImgData> imgList = getImageList();
        Integer[] idArray = new Integer[imgList.size()];
        for(int i=0; i<imgList.size(); i++) {
            idArray[i] = imgList.get(i).getImgId();
        }
        return idArray;
    }
}
