package com.example.krist.geosnap.Services;

import android.os.AsyncTask;
import android.telecom.Call;

import com.example.krist.geosnap.Models.IEventCallback;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutionException;

import javax.security.auth.callback.Callback;

/**
 * Created by krist on 03-Feb-16.
 */
public class ApiCommunicator {

    public static String apiUrl = "http://geosnap.azurewebsites.net/api/Values";
    private IEventCallback ServiceCallback;

    public void RequestCallback(IEventCallback callback){
        ServiceCallback = callback;
    }

    public void doWork(){
        try {
            new AsyncApiCall().execute("/1").get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

    }

    //TODO: No need to return string... Probably
    private class AsyncApiCall extends AsyncTask<String, Void, String>
    {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
        @Override
        protected String doInBackground(String... params) {
            try{
                String paramString = "";
                for(String s: params){
                    paramString += s;
                }
                System.out.println(paramString);
                URL url = new URL(apiUrl + paramString);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                try{
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(con.getInputStream()));
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;
                    while((line = bufferedReader.readLine()) != null){
                        stringBuilder.append(line).append("\n");
                    }
                    ServiceCallback.EventTrigger(stringBuilder.toString());
                    return stringBuilder.toString();
                }
                catch(Exception e){
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
