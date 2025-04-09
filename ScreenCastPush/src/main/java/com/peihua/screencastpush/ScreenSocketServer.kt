package com.peihua.screencastpush

import com.peihua.logger.Logger
import org.java_websocket.WebSocket
import org.java_websocket.handshake.ClientHandshake
import org.java_websocket.server.WebSocketServer
import java.lang.Exception
import java.net.InetSocketAddress

class ScreenSocketServer(address: InetSocketAddress) : WebSocketServer(address) {
    private var mWebSocket: WebSocket? = null
    override fun onOpen(
        conn: WebSocket?,
        handshake: ClientHandshake?,
    ) {
        this.mWebSocket = conn
    }

    override fun onClose(
        conn: WebSocket?,
        code: Int,
        reason: String?,
        remote: Boolean,
    ) {
        Logger.addLog("onClose:$reason")
    }

    override fun onMessage(conn: WebSocket?, message: String?) {
        Logger.addLog("onMessage:$message")
    }

    override fun onError(conn: WebSocket?, ex: Exception?) {
        Logger.addELog("onError:${ex?.message}")
    }

    override fun onStart() {
        Logger.addLog("socket onStart")
    }

    fun send(message: String) {
        val webSocket = mWebSocket
        if (webSocket == null || webSocket.isOpen) {
            return
        }
        webSocket.send(message)
    }

    fun sendData(data: ByteArray) {
        val webSocket = mWebSocket
        if (webSocket == null || webSocket.isOpen) {
            return
        }
        webSocket.send(data)
    }

    fun close() {
        mWebSocket?.close()
    }
}