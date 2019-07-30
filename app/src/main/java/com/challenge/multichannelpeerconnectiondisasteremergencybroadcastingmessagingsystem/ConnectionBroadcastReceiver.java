package com.challenge.multichannelpeerconnectiondisasteremergencybroadcastingmessagingsystem;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class ConnectionBroadcastReceiver extends BroadcastReceiver {

    private WifiP2pManager wifiP2pManager;

    private WifiP2pManager.Channel wifiP2pManagerChannel;

    private WiFiDirectListener wifiDirectListener;

    public ConnectionBroadcastReceiver(WifiP2pManager wifiP2pManager, WifiP2pManager.Channel wifiP2pManagerChannel, WiFiDirectListener wifiDirectListener) {
        this.wifiP2pManager = wifiP2pManager;
        this.wifiP2pManagerChannel = wifiP2pManagerChannel;
        this.wifiDirectListener = wifiDirectListener;
    }

    public static IntentFilter setAndGetIntentFilter() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        return intentFilter;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action != null) {
            switch (action) {
                case WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION: {
                    int wifiState = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
                    if (wifiState == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                        wifiDirectListener.enableWifiDirect(true);
                    } else {
                        wifiDirectListener.enableWifiDirect(false);
                        List<WifiP2pDevice> wifiP2pDeviceList = new ArrayList<>();
                        wifiDirectListener.whenPeersAreAvailable(wifiP2pDeviceList);
                    }
                    break;
                }

                case WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION: {
                    wifiDirectListener.setAvailableDevices(intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE));
                    break;
                }

                case WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION: {
                    NetworkInfo networkInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
                    if (networkInfo.isConnected()) {
                        wifiP2pManager.requestConnectionInfo(wifiP2pManagerChannel, new WifiP2pManager.ConnectionInfoListener() {
                            @Override
                            public void onConnectionInfoAvailable(WifiP2pInfo info) {
                                wifiDirectListener.setAvailableConnectionInformation(info);
                            }
                        });
                        Log.e("info", "connectedDevice");
                    } else {
                        wifiDirectListener.whenDisconnect();
                        Log.e("info", "disconnectedDevice");
                    }
                    break;
                }

                case WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION: {
                    wifiP2pManager.requestPeers(wifiP2pManagerChannel, new WifiP2pManager.PeerListListener() {
                        @Override
                        public void onPeersAvailable(WifiP2pDeviceList peers) {
                            wifiDirectListener.whenPeersAreAvailable(peers.getDeviceList());
                        }
                    });
                    break;
                }

            }
        }
    }

}
