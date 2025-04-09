package com.peihua.screencastpush

import android.hardware.display.DisplayManager
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.projection.MediaProjection
import com.peihua.logger.Logger
import java.nio.ByteBuffer
import kotlin.experimental.and

class ScreenEncoder(
    private val mMediaProjection: MediaProjection,
    private val mSocketManager: SocketManager,
) : Thread() {
    private val VIDEO_WIDTH = 1280
    private val VIDEO_HEIGHT = 720
    private val SOCKET_TIME_OUT = 10000L
    private val SCREEN_FRAME_RATE = 20
    private val SCREEN_FRAME_INTERVAL = 1
    private val TYPE_FRAME_INTERVAL = 19

    // vps
    private val TYPE_FRAME_VPS = 32

    //记录vps pps sps
    private var vpsByteArray: ByteArray? = null
    private var isProcessing = true
    private val mMediaCodec: MediaCodec by lazy {
        try {
            MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_HEVC)
        } catch (e: Exception) {
            throw e
        }
    }

    init {
        Logger.addLog("ScreenEncoder init")
        val mediaFormat =
            MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_HEVC, VIDEO_WIDTH, VIDEO_HEIGHT)
        Logger.addLog("ScreenEncoder init mediaFormat:$mediaFormat")
        mediaFormat.setInteger(
            MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        Logger.addLog("ScreenEncoder color COLOR_FormatSurface")
        // 设置比特率
        // 比特率&#xff08;比特/秒&#xff09;
        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, VIDEO_WIDTH * VIDEO_HEIGHT);
        Logger.addLog("ScreenEncoder bitrate:${VIDEO_WIDTH * VIDEO_HEIGHT}")
        // 帧率
        mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, SCREEN_FRAME_RATE);
        Logger.addLog("ScreenEncoder frameRate:$SCREEN_FRAME_RATE")
        // I帧的频率
        mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, SCREEN_FRAME_INTERVAL);
        Logger.addLog("ScreenEncoder iFrameInterval:$SCREEN_FRAME_INTERVAL")
        try {
            Logger.addLog("mMediaCodec configure")
            mMediaCodec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            Logger.addLog("mMediaCodec createInputSurface")
            val surface = mMediaCodec.createInputSurface();
            Logger.addLog("mMediaCodec createVirtualDisplay")
            mMediaProjection.createVirtualDisplay(
                "screen",
                VIDEO_WIDTH,
                VIDEO_HEIGHT,
                1,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC,
                surface,
                null,
                null);
            Logger.addLog("ScreenEncoder init success")
        } catch (e: Exception) {
            e.printStackTrace()
            Logger.addELog("ScreenEncoder init fail. createEncoderByType error:${e.message}")
        }
    }

    override fun run() {
        mMediaCodec.start()
        val bufferInfo = MediaCodec.BufferInfo()
        Logger.addLog("ScreenEncoder running.......")
        while (isProcessing) {
            Logger.addLog("ScreenEncoder dequeueOutputBuffer")
            val outputBufferIndex = mMediaCodec.dequeueOutputBuffer(bufferInfo, SOCKET_TIME_OUT)
            Logger.addLog("ScreenEncoder dequeueOutputBuffer outputBufferIndex:$outputBufferIndex")
            if (outputBufferIndex >= 0) {
                Logger.addLog("ScreenEncoder getOutputBuffer....")
                val outputBuffer = mMediaCodec.getOutputBuffer(outputBufferIndex)
                Logger.addLog("ScreenEncoder getOutputBuffer outputBuffer.size:${outputBuffer?.remaining()}")
                if (outputBuffer == null) {
                    continue
                }
                Logger.addLog("ScreenEncoder encode start .....")
                encodeData(outputBuffer, bufferInfo)
                Logger.addLog("ScreenEncoder encode end .....")
                mMediaCodec.releaseOutputBuffer(outputBufferIndex, false)
                Logger.addLog("ScreenEncoder releaseOutputBuffer")
            }
        }
    }

    private fun encodeData(byteBuffer: ByteBuffer, bufferInfo: MediaCodec.BufferInfo) {
        Logger.addLog("ScreenEncoder encode")
        var offset = 4
        Logger.addLog("ScreenEncoder encode offset:$offset")
        if (byteBuffer.get(2) == 0x01.toByte()) {
            offset = 3
        }
        Logger.addLog("ScreenEncoder encode offset:$offset")
        val type = (byteBuffer.get(offset) and 0x7E).toInt() shr 1
        Logger.addLog("ScreenEncoder encode type:$type")
        if (type == TYPE_FRAME_VPS) {
            vpsByteArray = ByteArray(bufferInfo.size)
            byteBuffer.get(vpsByteArray!!)
        } else if (type == TYPE_FRAME_INTERVAL) {
            val vpsBytes = vpsByteArray
            val byteArray = ByteArray(bufferInfo.size)
            Logger.addLog("ScreenEncoder encode byteArray.size:${byteArray.size}")
            byteBuffer.get(byteArray)
            val vpsLength = vpsBytes?.size ?: 0
            Logger.addLog("ScreenEncoder encode vpsLength:$vpsLength")
            val newBytes = ByteArray(bufferInfo.size + vpsLength)
            Logger.addLog("ScreenEncoder encode newBytes.size:${newBytes.size}")
            if (vpsBytes != null) {
                System.arraycopy(vpsBytes, 0, newBytes, 0, vpsLength)
            }
            System.arraycopy(byteArray, 0, newBytes, vpsLength, byteArray.size)
            Logger.addLog("ScreenEncoder encode sendData size:${newBytes.size}")
            mSocketManager.sendData(newBytes)
        } else {
            Logger.addLog("ScreenEncoder encode sendData size:${bufferInfo.size}")
            val bytes = ByteArray(bufferInfo.size)
            byteBuffer.get(bytes)
            Logger.addLog("ScreenEncoder encode sendData bytes:${bytes.size}")
            mSocketManager.sendData(bytes)
        }
    }

    fun stopEncoder() {
        Logger.addLog("ScreenEncoder stop")
        isProcessing = false
        mMediaCodec.stop()
        mMediaCodec.release()
        mMediaProjection.stop()
    }

    override fun start() {
        isProcessing = true
        Logger.addLog("ScreenEncoder start")
        super.start()
    }
}