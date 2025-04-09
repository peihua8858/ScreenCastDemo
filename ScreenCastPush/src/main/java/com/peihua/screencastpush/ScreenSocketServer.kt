package com.peihua.screencastpush

import com.peihua.logger.Logger
import org.java_websocket.WebSocket
import org.java_websocket.handshake.ClientHandshake
import org.java_websocket.server.WebSocketServer
import java.lang.Exception
import java.net.InetSocketAddress

class ScreenSocketServer(address: InetSocketAddress) : WebSocketServer(address) {
    private val mWebSockets = mutableListOf<WebSocket>()
    override fun onOpen(
        conn: WebSocket?,
        handshake: ClientHandshake?,
    ) {
        Logger.addLog("onOpen:${conn?.remoteSocketAddress}，status:${handshake?.content.toString()}")
        if (conn != null) {
            mWebSockets.add(conn)
            Logger.addLog("onOpen:${conn.remoteSocketAddress}，status:${handshake?.content.toString()}")
        }
    }

    override fun onClose(
        conn: WebSocket?,
        code: Int,
        reason: String?,
        remote: Boolean,
    ) {
        mWebSockets.remove(conn)
        Logger.addLog("onClose>>>code:$code,reason:$reason,remote:$remote")

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
        if (mWebSockets.isEmpty()) {
            return
        }
        mWebSockets.forEach {
            try {
                it.send(message)
            } catch (e: Exception) {
                Logger.addELog("sendData>>>socket:$it,error:${e.message}")
            }
        }
    }

    fun sendData(data: ByteArray) {
        if (mWebSockets.isEmpty()) {
            return
        }
        mWebSockets.forEach {
            try {
                it.send(data)
            } catch (e: Exception) {
                Logger.addELog("sendData>>>socket:$it,error:${e.message}")
            }
        }
    }

    fun close() {
        mWebSockets.forEach {
            try {
                it.close()
            } catch (e: Exception) {
                Logger.addELog("close>>>socket:$it,error:${e.message}")
            }
        }
    }
}