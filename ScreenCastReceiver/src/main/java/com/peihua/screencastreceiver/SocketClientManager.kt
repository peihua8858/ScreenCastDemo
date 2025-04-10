package com.peihua.screencastreceiver

import android.view.Surface
import com.peihua.logger.Logger
import java.net.URI

class SocketClientManager() {
    private var mServerPort: Int = 50000
    private var mScreenDecoder: ScreenDecoder? = null
    private var mScreenCastClient: ScreenCastClient? = null

    fun setSurface(surface: Surface) {
        mScreenDecoder = ScreenDecoder()
        mScreenDecoder?.start(surface)
    }

    fun setHost(host: String) {
        Logger.addLog("setHost:$host")
        mScreenCastClient = ScreenCastClient(::onReceiveData,::stop, URI("ws://$host:$mServerPort"))
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