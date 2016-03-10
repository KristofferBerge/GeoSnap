package com.kristomb.geosnap.Activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
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
    CallbackManager callbackManager;
    Switch discoveryModeSwitch;
    SeekBar rangeSeekBar;
    CheckBox debugDiscoveryCheckbox;
    Button saveSettingsButton;
    AccessTokenTracker accessTokenTracker;
    TextView reputationTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();
        setContentView(R.layout.activity_settings);

        discoveryModeSwitch = (Switch)findViewById(R.id.discoveryModeSwitch);
        rangeSeekBar = (SeekBar)findViewById(R.id.rangeSeekBar);
        debugDiscoveryCheckbox = (CheckBox)findViewById(R.id.debugDiscoveryCheckbox);
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

        loadSettings();
        setVisibility();

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
            //Let's hope login screen was called from inbox activity
            this.finish();
        }
    }

    private void setVisibility(){

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
            ApiCommunicator communicator = new ApiCommunicator(this);
            String username = communicator.getUsername();
            //If user is not registered in database
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
                setNonLoginSettingsEnabled(false);
                return;
            }
            String rating = communicator.getRating();
            reputationTextView.setText(rating);
            //if user is registered
            Button SaveButton = (Button)findViewById(R.id.LoginPageSaveUsernameButton);
            SaveButton.setVisibility(View.INVISIBLE);
            EditText UsernameEditText = (EditText)findViewById(R.id.LoginPageUsernameEditText);
            UsernameEditText.setText(username.toString());
            UsernameEditText.setEnabled(false);
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
        System.out.println("ADDED USERNAME TO DATABASE");
        EditText UsernameEditText = (EditText)findViewById(R.id.LoginPageUsernameEditText);
        String username = UsernameEditText.getText().toString();
        ApiCommunicator communicator = new ApiCommunicator(this);
        String result = communicator.setUsername(username);
        System.out.println(result);
        this.finish();
    }

    private void saveSettings(){
        SharedPreferences settings = getSharedPreferences("UserSettings", 0);
        SharedPreferences.Editor settingsEditor = settings.edit();
        settingsEditor.putBoolean("DiscoveryMode",discoveryModeSwitch.isChecked());

        //Send broadcast to let service know that discovery mode may have changed
        Intent i = new Intent("SetDiscoveryMode");
        i.putExtra("setting",discoveryModeSwitch.isChecked());
        LocalBroadcastManager.getInstance(this).sendBroadcast(i);

        settingsEditor.putInt("Range", rangeSeekBar.getProgress());
        settingsEditor.putBoolean("debugMode", debugDiscoveryCheckbox.isChecked());
        settingsEditor.commit();
    }

    private void loadSettings(){
        SharedPreferences settings = getSharedPreferences("UserSettings", 0);
        boolean discoveryMode = settings.getBoolean("DiscoveryMode", true);
        discoveryModeSwitch.setChecked(discoveryMode);
        int range = settings.getInt("Range", 50);
        rangeSeekBar.setProgress(range);
        boolean debugMode = settings.getBoolean("debugMode",false);
        debugDiscoveryCheckbox.setChecked(debugMode);
    }
}
