package com.peihua.screencastreceiver

import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.lang.Exception
import java.net.URI
import java.nio.ByteBuffer

class ScreenCastClient(private val onReceiverData: (ByteArray) -> Unit, uri: URI) :
    WebSocketClient(uri) {

    override fun onOpen(handshakedata: ServerHandshake?) {

    }

    override fun onMessage(message: String?) {
    }

    override fun onMessage(bytes: ByteBuffer) {
        val byteArray = ByteArray(bytes.remaining())
        bytes.get(byteArray)
        onReceiverData(byteArray)
    }

    override fun onClose(code: Int, reason: String?, remote: Boolean) {

    }

    override fun onError(ex: Exception?) {

    }
}