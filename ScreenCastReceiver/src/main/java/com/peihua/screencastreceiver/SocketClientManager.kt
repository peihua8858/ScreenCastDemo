package com.peihua.screencastreceiver

import android.view.Surface
import java.net.URI

class SocketClientManager() {
    private var mServerPort: Int = 50000
    private var mScreenDecoder: ScreenDecoder? = null
    private var mScreenCastClient: ScreenCastClient? = null

    fun setSurface(surface: Surface) {
        mScreenDecoder = ScreenDecoder(surface)
        mScreenDecoder?.start()
    }

    fun setHost(host: String) {
        mScreenCastClient = ScreenCastClient({
            onReceiveData(it)
        }, URI("ws://$host:$mServerPort"))
        mScreenCastClient?.connect()
    }

    fun onReceiveData(data: ByteArray) {
        mScreenDecoder?.decodeData(data)
    }

    fun stop() {
        mScreenCastClient?.close()
        mScreenDecoder?.stopDecoder()
    }
}