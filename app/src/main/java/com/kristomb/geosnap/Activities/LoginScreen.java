package com.kristomb.geosnap.Activities;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.kristomb.geosnap.R;
import com.kristomb.geosnap.Services.ApiCommunicator;

import org.w3c.dom.Text;

public class LoginScreen extends AppCompatActivity {

    CallbackManager callbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();





        setContentView(R.layout.activity_login_screen);
        LoginButton loginButton = (LoginButton) findViewById(R.id.login_button);
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                System.out.println("LOGGED IN");
                //System.out.println(AccessToken.getCurrentAccessToken().getToken());
                setVisibility();
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

        Button closeApplicationButton = (Button) findViewById(R.id.closeApplicationButton);
        closeApplicationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeApplication();
            }
        });

        setVisibility();
    }

    private void closeApplication(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            //Exits all activities
            this.finishAffinity();
        }
        else{
            //Let's hope login screen was called from inbox activity
            this.finish();
        }
    }
    private void openInbox(){
        Intent i = new Intent(this,Inbox.class);
        startActivity(i);
        this.finish();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        setVisibility();
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }


    private void setVisibility(){
        //If not logged in
        if(AccessToken.getCurrentAccessToken() == null ||AccessToken.getCurrentAccessToken().isExpired()){
            Button SaveButton = (Button)findViewById(R.id.LoginPageSaveUsernameButton);
            SaveButton.setVisibility(View.INVISIBLE);
            EditText UsernameEditText = (EditText)findViewById(R.id.LoginPageUsernameEditText);
            UsernameEditText.setEnabled(false);
        }
        //if logged in
        else{
            final ApiCommunicator communicator = new ApiCommunicator(this);
            String username = communicator.getUsername();
            if(username == "Enter username"){
                Button SaveButton = (Button)findViewById(R.id.LoginPageSaveUsernameButton);
                SaveButton.setVisibility(View.VISIBLE);
                EditText UsernameEditText = (EditText)findViewById(R.id.LoginPageUsernameEditText);
                UsernameEditText.setEnabled(true);
                UsernameEditText.setHint("Enter username");
                SaveButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        registerUsername();
                    }
                });
                return;
            }

            Button SaveButton = (Button)findViewById(R.id.LoginPageSaveUsernameButton);
            SaveButton.setVisibility(View.INVISIBLE);
            EditText UsernameEditText = (EditText)findViewById(R.id.LoginPageUsernameEditText);
            UsernameEditText.setText(username);
            UsernameEditText.setEnabled(false);
        }
    }

    private void registerUsername(){
        System.out.println("ADDED USERNAME TO DATABASE");
        EditText UsernameEditText = (EditText)findViewById(R.id.LoginPageUsernameEditText);
        String username = UsernameEditText.getText().toString();
        ApiCommunicator communicator = new ApiCommunicator(this);
        String result = communicator.setUsername(username);
        System.out.println(result);
        openInbox();
    }
}
