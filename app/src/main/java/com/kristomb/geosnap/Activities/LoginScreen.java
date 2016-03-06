package com.kristomb.geosnap.Activities;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.kristomb.geosnap.R;

public class LoginScreen extends AppCompatActivity {

    CallbackManager callbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();

        //TODO: error if faceboook is not installed
        try{
            ApplicationInfo info = getPackageManager().getApplicationInfo("com.facebook.katana", 0);
            System.out.println("FACEBOOK IS INSTALLED");
        }
        catch( PackageManager.NameNotFoundException e ){
            System.out.println("NO FACEBOOK FOUND");
        }


        setContentView(R.layout.activity_login_screen);
        LoginButton loginButton = (LoginButton) findViewById(R.id.login_button);
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                System.out.println("LOGGED IN");
                System.out.println(AccessToken.getCurrentAccessToken().getToken());
            }

            @Override
            public void onCancel() {
                System.out.println("CANCELLED LOGIN");
            }

            @Override
            public void onError(FacebookException error) {
                System.out.println("LOGIN ERROR");
            }
        });
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        callbackManager.onActivityResult(requestCode,resultCode,data);
    }
}
