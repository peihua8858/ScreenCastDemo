package com.peihua.screencastpush

import android.media.projection.MediaProjection
import java.net.InetSocketAddress

class SocketManager(private val mediaProjection: MediaProjection) {
    private var mServerPort: Int = 50000
    private val mServer: ScreenSocketServer by lazy {
        ScreenSocketServer(InetSocketAddress(mServerPort))
    }
    private val mScreenEncoder: ScreenEncoder by lazy {
        ScreenEncoder(mediaProjection, this)
    }

    fun start(mediaProjection: MediaProjection) {
        mServer.start()
        mScreenEncoder.start()
    }

    fun close() {
        try {
            mServer.stop()
            mServer.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        mScreenEncoder.stopEncoder()
    }

    fun sendData(data: ByteArray) {
        mServer.sendData(data)
    }
}