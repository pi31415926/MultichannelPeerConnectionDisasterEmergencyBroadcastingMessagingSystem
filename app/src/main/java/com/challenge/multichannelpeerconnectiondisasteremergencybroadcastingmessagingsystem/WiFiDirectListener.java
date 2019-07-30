package com.challenge.multichannelpeerconnectiondisasteremergencybroadcastingmessagingsystem;

import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;

import java.util.Collection;

public interface WiFiDirectListener extends WifiP2pManager.ChannelListener {

    void enableWifiDirect(boolean enabled);

    void setAvailableConnectionInformation(WifiP2pInfo wifiP2pInfo);

    void whenDisconnect();

    void setAvailableDevices(WifiP2pDevice wifiP2pDevice);

    void whenPeersAreAvailable(Collection<WifiP2pDevice> wifiP2pDeviceList);

}