package com.challenge.multichannelpeerconnectiondisasteremergencybroadcastingmessagingsystem;


import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.JsonObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;

public class SignalingActivity extends AppCompatActivity {
    private String uuid;

    private String deviceName;

    private LocationReporter locationReporter;

    private ArrayList<String> filesToBeSent;

    private HashSet<String> filesRecieved;

    private HashMap<String, String> devicesFound;

    private final String PublicKey = "AAAAB3NzaC1yc2EAAAADAQABAAAAgQCATPOsfbf9YjkxKtMm/GbqfLieHoAU+evVvqWs4ACJqRE0uCqfgh0oRGlHKIPt5XItG+zVqq8wX2bZg0wPc/bGoo/myvhSMY7VWUPt+OYgYkqZ8Y62HjlemX/V+R+ZBQhh0x/mXIpKmSs6WTXcQyfAu9v17sk7aR6b6Md0lhL27w== ";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        filesRecieved = new HashSet<>();
        devicesFound = new HashMap<>();
        filesToBeSent = new ArrayList<>();
        locationReporter = new LocationReporter();
    }



    public String getPublicKey() {
        return PublicKey;
    }


    protected String generateMessage(String usage){
        String messageName = uuid + " " + System.currentTimeMillis() + ".json";
        ContextWrapper contextWrapper = new ContextWrapper(getApplicationContext());
        File directory = contextWrapper.getDir(getFilesDir().getName(), Context.MODE_PRIVATE);
        File filePathAndName = new File(directory, messageName);
        while(!locationReporter.canGetCurrentLocation(this)){
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                Log.e("Exception", e.getMessage());
            }
        }

        Location currentLocation = locationReporter.getCurrentLocation();

        JsonObject messageBodyJson = new JsonObject();
        messageBodyJson.addProperty("location", currentLocation.toString());
        messageBodyJson.addProperty("usage", usage);
        messageBodyJson.addProperty("deviceName", getDeviceName());
        messageBodyJson.addProperty("uuid", getUuid());

        try(
                BufferedWriter output = new BufferedWriter(
                        new OutputStreamWriter(
                                new FileOutputStream(filePathAndName, false),
                                StandardCharsets.UTF_8
                        )
                )
        ){
            output.append(messageBodyJson.toString());
            output.flush();
        }catch(IOException ioe){
            Log.e("Exception", ioe.getMessage());
        }

        this.filesToBeSent.add(messageName);
        return messageName;
    }




    public String getDeviceName() {
        if(deviceName == null) {
            String manufacturer = Build.MANUFACTURER;
            String model = Build.MODEL;
            if (model.startsWith(manufacturer)) {
                String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
                deviceName = model + "_GeneratedWhen_" + timeStamp;
            } else {
                deviceName = model;
            }
        }

        return deviceName;
    }



    public String getUuid() {
        if (uuid == null) {
            deviceName = getDeviceName();
            uuid = java.util.UUID.randomUUID().toString();
        }
        return uuid;
    }

    public void tryToSendData(){

        //TODO: add try to send data to server
        Intent dataTransferViaDirectWiFiIntent = new Intent(getApplicationContext(), DataTransferViaWiFiDirectWiFi.class);
        dataTransferViaDirectWiFiIntent.putExtra("filesToBeSent", filesToBeSent);
        startActivity(dataTransferViaDirectWiFiIntent);
    }





}
