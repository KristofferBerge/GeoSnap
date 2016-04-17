package com.kristomb.geosnap.Activities;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.kristomb.geosnap.R;
import com.kristomb.geosnap.Services.ApiCommunicator;

import org.w3c.dom.Text;

public class UserSettings extends AppCompatActivity {

    String username;
    String reputation;

    CallbackManager callbackManager;
    Switch discoveryModeSwitch;
    SeekBar rangeSeekBar;
    CheckBox debugDiscoveryCheckbox;
    CheckBox getOwnImagesCheckbox;
    Button saveSettingsButton;
    AccessTokenTracker accessTokenTracker;
    TextView reputationTextView;


    @Override
    protected void onStart(){
        super.onStart();
        loadSettings();
        setVisibility();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();
        setContentView(R.layout.activity_settings);

        discoveryModeSwitch = (Switch)findViewById(R.id.discoveryModeSwitch);
        rangeSeekBar = (SeekBar)findViewById(R.id.rangeSeekBar);
        debugDiscoveryCheckbox = (CheckBox)findViewById(R.id.debugDiscoveryCheckbox);
        getOwnImagesCheckbox = (CheckBox)findViewById(R.id.getOwnImagesCheck);
        saveSettingsButton = (Button)findViewById(R.id.saveSettingsButton);
        reputationTextView = (TextView)findViewById(R.id.reputationTextView);




        SeekBar seekBar = (SeekBar) findViewById(R.id.rangeSeekBar);
        final TextView seekBarLabel = (TextView) findViewById(R.id.rangeSeekBarLabel);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                seekBarLabel.setText(Integer.toString(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

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

        //Tracking changes on accesstoken to know when user has logged out
        accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken) {
                if(currentAccessToken == null){
                    setVisibility();
                }
            }
        };


        discoveryModeSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setVisibility();
            }
        });

        Button saveChangesButton = (Button) findViewById(R.id.saveSettingsButton);
        saveChangesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveSettings();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        setVisibility();
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void closeApplication(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            //Exits all activities
            this.finishAffinity();
        }
        else{
            this.finish();
        }
    }

    private void refreshUserInfo(){
        ApiCommunicator communicator = new ApiCommunicator(this);
        username = communicator.getUsername();
        if(username != null){
            //Cleaning response from api
            username = username.replace("\"", "");
            username = username.replace("\n", "");
            //Saving username to device to avoid api calls.
            SharedPreferences settings = getSharedPreferences("UserSettings", 0);
            SharedPreferences.Editor settingsEditor = settings.edit();
            settingsEditor.putString("Username", username);
            settingsEditor.commit();
        }
        reputation = communicator.getRating();
        if(reputation != null){
            reputation = reputation.replace("\"", "");
        }

        //Networks that require password login in browser can respond to our get-request with that page.
        //Checking length and displaying error message if the username is too long.
        if(username == null || username.length() > 20){
            showSnackbar(snackbarMessage.ERROR_LOADING_SETTINGS);
            username = "";
            reputation = "";
            return;
        }

        if(username.length() > 20){
            showSnackbar(snackbarMessage.ERROR_LOADING_SETTINGS);
            username = "";
            reputation = "";
        }
    }

    private void setVisibility(){

        System.out.println("USERNAME IS:" + username);

        //If not logged in
        if(AccessToken.getCurrentAccessToken() == null ||AccessToken.getCurrentAccessToken().isExpired()){
            Button SaveButton = (Button)findViewById(R.id.LoginPageSaveUsernameButton);
            SaveButton.setVisibility(View.INVISIBLE);
            EditText UsernameEditText = (EditText)findViewById(R.id.LoginPageUsernameEditText);
            UsernameEditText.setEnabled(false);
            setNonLoginSettingsEnabled(false);
        }
        //if logged in
        else{
            //If user is not registered in database
            if(username == "Enter username" || username == ""){
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
                setNonLoginSettingsEnabled(false);
                return;
            }
            reputationTextView.setText(reputation);
            //if user is registered
            Button SaveButton = (Button)findViewById(R.id.LoginPageSaveUsernameButton);
            SaveButton.setVisibility(View.INVISIBLE);
            EditText UsernameEditText = (EditText)findViewById(R.id.LoginPageUsernameEditText);
            UsernameEditText.setText(username.toString());
            UsernameEditText.setEnabled(false);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                UsernameEditText.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            }
            UsernameEditText.setTextSize(25);
            setNonLoginSettingsEnabled(true);
        }
    }

    private void setNonLoginSettingsEnabled(boolean setting){
        discoveryModeSwitch.setEnabled(setting);
        saveSettingsButton.setEnabled(setting);
        if(setting){
            //Discovery mode is off
            if(!discoveryModeSwitch.isChecked()) {
                rangeSeekBar.setEnabled(false);
                debugDiscoveryCheckbox.setEnabled(false);
                return;
            }
        }
        rangeSeekBar.setEnabled(setting);
        debugDiscoveryCheckbox.setEnabled(setting);
    }

    private void registerUsername(){
        EditText UsernameEditText = (EditText)findViewById(R.id.LoginPageUsernameEditText);
        String username = UsernameEditText.getText().toString();
        //checking username length
        if(username.length() > 20){
            showSnackbar(snackbarMessage.USERNAME_TOO_LONG);
            return;
        }
        if(username.length() < 3){
            showSnackbar(snackbarMessage.USERNAME_TOO_SHORT);
            return;
        }
        System.out.println("ADDED USERNAME TO DATABASE");
        ApiCommunicator communicator = new ApiCommunicator(this);
        String result = communicator.setUsername(username);
        System.out.println(result);
        this.finish();
    }

    private void saveSettings(){
        SharedPreferences settings = getSharedPreferences("UserSettings", 0);
        SharedPreferences.Editor settingsEditor = settings.edit();
        settingsEditor.putBoolean("DiscoveryMode", discoveryModeSwitch.isChecked());

        //Send broadcast to let service know that discovery mode may have changed
        Intent i = new Intent("SetDiscoveryMode");
        i.putExtra("setting", discoveryModeSwitch.isChecked());
        LocalBroadcastManager.getInstance(this).sendBroadcast(i);

        settingsEditor.putInt("Range", rangeSeekBar.getProgress());
        settingsEditor.putBoolean("debugMode", debugDiscoveryCheckbox.isChecked());
        settingsEditor.putBoolean("getOwn", getOwnImagesCheckbox.isChecked());
        settingsEditor.commit();

    }

    private void loadSettings(){
        SharedPreferences settings = getSharedPreferences("UserSettings", 0);
        boolean discoveryMode = settings.getBoolean("DiscoveryMode", true);
        discoveryModeSwitch.setChecked(discoveryMode);
        int range = settings.getInt("Range", 50);
        rangeSeekBar.setProgress(range);
        boolean debugMode = settings.getBoolean("debugMode", false);
        debugDiscoveryCheckbox.setChecked(debugMode);
        boolean getOwn = settings.getBoolean("getOwn", false);
        getOwnImagesCheckbox.setChecked(getOwn);
        username = settings.getString("Username","");
        if(username == ""){
            refreshUserInfo();
        }
    }

    private enum snackbarMessage{
        USERNAME_TOO_LONG,
        USERNAME_TOO_SHORT,
        ERROR_LOADING_SETTINGS
    };

    private void showSnackbar(snackbarMessage message){
        //Creating snackbar and getting properties
        RelativeLayout layout = (RelativeLayout) findViewById(R.id.settings_layout);
        Snackbar sb = Snackbar.make(layout, "", Snackbar.LENGTH_LONG);
        View sbView = sb.getView();
        TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);

        if(message == snackbarMessage.ERROR_LOADING_SETTINGS){
            sb.setText("Error loading settings, please check your connection...");
            textView.setTextColor(Color.RED);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            }
        }
        else if(message == snackbarMessage.USERNAME_TOO_LONG){
            sb.setText("Username too long");
            textView.setTextColor(Color.RED);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            }
        }
        else if(message == snackbarMessage.USERNAME_TOO_SHORT){
            sb.setText("Username too short");
            textView.setTextColor(Color.RED);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            }
        }
        sb.show();
    }
}
