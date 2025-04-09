package com.peihua.screencastreceiver

import com.peihua.logger.Logger
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.lang.Exception
import java.net.URI
import java.nio.ByteBuffer

class ScreenCastClient(private val onReceiverData: (ByteArray) -> Unit, uri: URI) :
    WebSocketClient(uri) {
    init {
        Logger.addLog("ScreenCastClient init, uri:$uri")
    }

    override fun onOpen(handshakedata: ServerHandshake?) {
        Logger.addLog("onOpen, httpStatus:${handshakedata?.httpStatus}, ${handshakedata?.httpStatusMessage}")
    }

    override fun onMessage(message: String?) {
        Logger.addLog("onMessage:$message")
    }

    override fun onMessage(bytes: ByteBuffer) {
        Logger.addLog("onMessage:${bytes.remaining()}")
        val byteArray = ByteArray(bytes.remaining())
        bytes.get(byteArray)
        onReceiverData(byteArray)
    }

    override fun onClose(code: Int, reason: String?, remote: Boolean) {
        Logger.addLog("onClose,code:$code,reason:$reason")
    }

    override fun onError(ex: Exception?) {
        Logger.addELog("onError:${ex?.message}")
    }
}