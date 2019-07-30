package com.challenge.multichannelpeerconnectiondisasteremergencybroadcastingmessagingsystem;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;

public class DataTransferViaWiFiDirectWiFi extends AppCompatActivity implements WiFiDirectListener {

    private LinkedList<WifiP2pDevice> deviceList;

    private WifiP2pManager wifiP2pManager;

    private WifiP2pManager.Channel wifiP2pManagerChannel;

    private ArrayList<String> filesToBeSent;

    private boolean isWifiDirectEnabled;

    private BroadcastReceiver broadcastReceiver;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        moveTaskToBack(true);
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        if(intent != null)
        {
            filesToBeSent = (ArrayList<String>) intent.getExtras().getStringArrayList("filesToBeSent");
        }
        isWifiDirectEnabled = false;
        deviceList = new LinkedList<>();
        wifiP2pManager = (WifiP2pManager) getSystemService(WIFI_P2P_SERVICE);
        wifiP2pManagerChannel = wifiP2pManager.initialize(this, getMainLooper(), this);
        broadcastReceiver = new ConnectionBroadcastReceiver(wifiP2pManager, wifiP2pManagerChannel, this);
        registerReceiver(broadcastReceiver, ConnectionBroadcastReceiver.setAndGetIntentFilter());

        Thread sendDataToPeersThread = new Thread(
                new Runnable(){
                    public void run(){
                        while(true) {
                            broadcastDataToAll();
                            try {
                                Thread.sleep(10000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
        );
        sendDataToPeersThread.start();

    }

    public void broadcastDataToAll(){
        if (!isWifiDirectEnabled) {
            return;
        }
        Thread sendDataThread = new Thread(
                new Runnable(){
                    public void run(){
                        searchDevicesNearby();
                        if(deviceList.size() != 0){
                            for(WifiP2pDevice device: deviceList){
                                tryToConnect(device);
                                disconnectAll();
                            }
                        }
                    }
                }
        );
        sendDataThread.start();
    }

    public void searchDevicesNearby(){
        wifiP2pManager.discoverPeers(wifiP2pManagerChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {Log.e("info", "discoverPeersSucceeded");}

            @Override
            public void onFailure(int reasonCode) {Log.e("warn", "discoverPeersFailed");}
        });
    }

    private WifiP2pConfig tryToConnect(WifiP2pDevice device) {
        if (device.status != WifiP2pDevice.AVAILABLE){
            return null;
        }
        WifiP2pConfig wifiP2pConfig = new WifiP2pConfig();
        if (wifiP2pConfig.deviceAddress != null && device != null) {
            wifiP2pConfig.deviceAddress = device.deviceAddress;
            wifiP2pConfig.wps.setup = WpsInfo.PBC;
            wifiP2pManager.connect(wifiP2pManagerChannel, wifiP2pConfig, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    Log.e("info", "connectSucceeded");
                }

                @Override
                public void onFailure(int reason) {
                    Log.e("warn", "connectFailed");
                }
            });
        }
        return wifiP2pConfig;
    }

    private void sendDataToGroupOwner(WifiP2pInfo wifiP2pInfo) {
        ContextWrapper contextWrapper = new ContextWrapper(getApplicationContext());
        File directory = contextWrapper.getDir(getFilesDir().getName(), Context.MODE_PRIVATE);
        for(String fileName: filesToBeSent){
            File fileToBeSent = new File(directory, fileName);
            if (fileToBeSent.exists() && wifiP2pInfo != null) {
                FileInformation fileInformation = new FileInformation(fileToBeSent.getName(), fileToBeSent.length());
                Log.e("info", "sending " + fileInformation);
                new SendingService(this, fileInformation).execute(wifiP2pInfo.groupOwnerAddress.getHostAddress());
            }
        }
    }

    private void disconnectAll() {
        wifiP2pManager.removeGroup(wifiP2pManagerChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onFailure(int reasonCode) {
                Log.e("warn", "disconnectFailed " + reasonCode);
            }

            @Override
            public void onSuccess() {
                Log.e("info", "disconnectSucceeded");
            }
        });
    }

    @Override
    public void enableWifiDirect(boolean enabled) {
        isWifiDirectEnabled = enabled;
    }

    @Override
    public void setAvailableConnectionInformation(WifiP2pInfo wifiP2pInfo) {
        deviceList.clear();
        if (wifiP2pInfo.groupFormed && wifiP2pInfo.isGroupOwner) {
            ReceivingService wifiServerService = new ReceivingService();
            if (wifiServerService != null) {
                startService(new Intent(this, ReceivingService.class));
            }
        }else if (wifiP2pInfo.groupFormed) {
            sendDataToGroupOwner(wifiP2pInfo);
        }
    }

    @Override
    public void whenDisconnect() {
        Log.e("info", "Disconnection");
        deviceList.clear();
    }

    @Override
    public void setAvailableDevices(WifiP2pDevice wifiP2pDevice) {
        Log.e("info", "DeviceAvailable");
    }

    @Override
    public void whenPeersAreAvailable(Collection<WifiP2pDevice> wifiP2pDeviceList) {
        this.deviceList.clear();
        this.deviceList.addAll(wifiP2pDeviceList);
    }

    @Override
    public void onChannelDisconnected() {
        Log.e("info", "ChannelDisconnected");
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
    }




}
