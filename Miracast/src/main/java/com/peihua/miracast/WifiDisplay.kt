package com.peihua.miracast

data class WifiDisplay(val mDeviceName: String, val mDeviceAddress: String) {
    override fun toString(): String {
        return mDeviceName
    }
}