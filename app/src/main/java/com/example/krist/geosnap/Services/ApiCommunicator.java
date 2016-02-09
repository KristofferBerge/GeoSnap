package com.example.krist.geosnap.Services;

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutionException;

/**
 * Created by krist on 03-Feb-16.
 */
public class ApiCommunicator {

    public static String apiUrl = "http://geosnap.azurewebsites.net/api/Values";

    public String doWork(){
        try {
            return new AsyncApiCall().execute("").get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }


    public void test(){

    }


    private class AsyncApiCall extends AsyncTask<String, Void, String>
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
