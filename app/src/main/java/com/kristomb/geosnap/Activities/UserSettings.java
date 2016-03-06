package com.kristomb.geosnap.Activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.kristomb.geosnap.R;

public class UserSettings extends AppCompatActivity {

    public static final String USER_SETTINGS_RESOURCE = "userSettings";
    public static final String USERNAME_SETTING = "username";
    public static final String CONFID_SETTING = "confId";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Button saveChangesButton = (Button) findViewById(R.id.confirmUserSettingUpdate);
        saveChangesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Accessing shared preferences
                SharedPreferences settings = getSharedPreferences(USER_SETTINGS_RESOURCE,0);
                SharedPreferences.Editor settingsEditor = settings.edit();

                //Saving username
                EditText usernameField= (EditText) findViewById(R.id.usernameText);
                String usernameText = usernameField.getText().toString();
                settingsEditor.putString(USERNAME_SETTING,usernameText);
                settingsEditor.commit();
                //Saving conf id
                EditText confIdField = (EditText) findViewById(R.id.confirmationIdText);
                try{
                    int confIdText = Integer.parseInt(confIdField.getText().toString());
                    settingsEditor.putInt(CONFID_SETTING,confIdText);
                    settingsEditor.commit();
                }
                catch(Exception e){
                    e.printStackTrace();
                }
            }
        });

        Button LoginPageButton = (Button) findViewById(R.id.openloginButton);
        LoginPageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openLoginScreen();
            }
        });

        //Accessing shared preferences
        SharedPreferences settings = getSharedPreferences(USER_SETTINGS_RESOURCE, 0);
        String username = settings.getString(USERNAME_SETTING, "");
        System.out.println(username + " HAS BEEN LOADED");
        int confId = settings.getInt(CONFID_SETTING,0);

        //Populating text fields if saved value is found
        if(username != ""){
            EditText usernameTextField = (EditText) findViewById(R.id.usernameText);
            usernameTextField.setText(username);
        }
        if(confId != 0){
            EditText confIdTextField = (EditText) findViewById(R.id.confirmationIdText);
            confIdTextField.setText(Integer.toString(confId));
        }

    }

    private void openLoginScreen(){
        Intent i = new Intent(this,LoginScreen.class);
        startActivity(i);
    }
}
