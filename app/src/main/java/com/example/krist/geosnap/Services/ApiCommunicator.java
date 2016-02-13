package com.example.krist.geosnap.Services;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Base64;

import com.example.krist.geosnap.Models.ImgData;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

/**
 * Created by krist on 03-Feb-16.
 */
public class ApiCommunicator {

    public static String apiUrl = "http://geosnap.azurewebsites.net/api/Values";
    public Context C;

    public ApiCommunicator(Context c){
        C = c;
    }

    //TODO: Replace with method using gps-position
    public String doWork(){
        try {
            return new GetTask().execute("").get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getAllImages(){
        try {
            return new GetTask().execute("").get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }


    public void test(GeoService context){
        //TODO: REMOVE TEST IMAGE
        File f = new File(String.valueOf(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS + "/31.jpg")));

        //Parsing image-file to bitmap
        Bitmap bm = BitmapFactory.decodeFile(f.getPath());
        //Bitmap to bytearray
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG,100,baos);
        byte[] b = baos.toByteArray();
        //Bytearray to Base64 string
        String encodedImage = Base64.encodeToString(b,Base64.URL_SAFE);
        new PostTask().execute(encodedImage,"10");

    }

    public void UploadImage(Bitmap bm, double lat, double lng, String user) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG,100,baos);
        byte[] b = baos.toByteArray();
        //Bytearray to Base64 string
        String encodedImage = Base64.encodeToString(b,Base64.URL_SAFE);
        //StringParameters: 0:Base64Image 1:Latitude 2:Longitude 3:Username
        new PostTask().execute(encodedImage,Double.toString(lat),Double.toString(lng),user);
    }

    private class PostTask extends AsyncTask<String,String, String>{
        @Override
        protected String doInBackground(String... params) {
            try{
                String imgString = params[0];
                double lat = Double.parseDouble(params[1]);
                double lng = Double.parseDouble(params[2]);
                String username = params[3];

                URL url = new URL("http://geosnap.azurewebsites.net/api/Values?lat="+ lat + "&lng=" + lng + "&usr=" + username);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                System.out.println("CONNECTING...");
                con.setRequestMethod("POST");
                try{
                    System.out.println("Opening stream");
                    OutputStreamWriter writer = new OutputStreamWriter(con.getOutputStream());
                    writer.write(imgString);
                    writer.close();
                    System.out.println("WRITING...");
                }
                catch (Exception e){
                    e.printStackTrace();
                    System.out.print("ERROR!");
                }
                finally {
                    System.out.println("RESPONSE: " + con.getResponseCode());
                    if(con.getResponseCode() == 200){
                        Intent i = new Intent("UploadStatus");
                        i.putExtra("STATUS","200");
                        LocalBroadcastManager.getInstance(C).sendBroadcast(i);
                    }
                    else{
                        Intent i = new Intent("UploadStatus");
                        i.putExtra("STATUS","500");
                        LocalBroadcastManager.getInstance(C).sendBroadcast(i);
                    }
                    con.disconnect();
                    System.out.print("DISCONNECTING...");
                }
            }
            catch (Exception e){
                e.printStackTrace();
                System.out.print("ERROR!");
            }
            return null;
        }
    }




    private class GetTask extends AsyncTask<String, Void, String>
    {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
        @Override
        protected String doInBackground(String... params) {
            try{
                //Constructing the url
                String paramString = "";
                for(String s: params){
                    paramString += s;
                }
                System.out.println(paramString);
                URL url = new URL(apiUrl + paramString);
                //Connecting to api
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                try{
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(con.getInputStream()));
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;
                    //Constructing string of result
                    while((line = bufferedReader.readLine()) != null){
                        stringBuilder.append(line).append("\n");
                    }
                    //Returning result as string
                    return stringBuilder.toString();
                }
                catch(Exception e){
                    e.printStackTrace();
                    System.out.println("FAILED TO PROCESS DATA");
                }
                finally {
                    con.disconnect();
                }
            }
            catch(Exception e){
                System.out.println("FAILED TO CONNECT TO API");
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            //this method will be running on UI thread
        }
    }
}
