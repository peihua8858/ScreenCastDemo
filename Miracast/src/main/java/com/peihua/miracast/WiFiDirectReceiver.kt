package com.peihua.miracast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.wifi.p2p.WifiP2pDeviceList
import android.net.wifi.p2p.WifiP2pManager
import android.net.wifi.p2p.WifiP2pManager.Channel

class WiFiDirectReceiver(private val mManager: WifiP2pManager, private val mChannel: Channel) :
    BroadcastReceiver(), WifiP2pManager.PeerListListener {
    private val TAG = "WiFiDirectReceiver";
    override fun onReceive(context: Context?, intent: Intent?) {

    }

    override fun onPeersAvailable(peers: WifiP2pDeviceList?) {

    }
}