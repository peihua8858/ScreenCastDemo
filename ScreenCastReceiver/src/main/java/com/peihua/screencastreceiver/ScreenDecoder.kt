package com.peihua.screencastreceiver

import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.view.Surface
import com.peihua.logger.Logger

class ScreenDecoder(private val surface: Surface) : Thread() {
    private val VEDIO_WIDTH = 1280
    private val VEDIO_HEIGHT = 720
    private val VEDIO_BIT_RATE = 10000000
    private val VEDIO_FRAME_RATE = 20
    private val VEDIO_I_FRAME_INTERVAL = 1
    private val TYPE_FRAME_INTERVAL = 19
    private val DECODE_TIME_OUT = 10_000L

    // vps
    private val TYPE_FRAME_VPS = 32
    private val mMediaCodec: MediaCodec by lazy {
        try {
            MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_HEVC)
        } catch (e: Exception) {
            throw e
        }
    }

    init {
        Logger.addLog("ScreenDecoder init")
        val mediaFormat = MediaFormat.createVideoFormat(
            MediaFormat.MIMETYPE_VIDEO_HEVC,
            VEDIO_WIDTH,
            VEDIO_HEIGHT
        )
        mediaFormat.setInteger(
            MediaFormat.KEY_COLOR_FORMAT,
            MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface
        )
        // 设置比特率
        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, VEDIO_BIT_RATE)
        // 设置帧率
        mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, VEDIO_FRAME_RATE)
        mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, VEDIO_I_FRAME_INTERVAL)
        try {
            mMediaCodec.configure(mediaFormat, surface, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
            mMediaCodec.start()
            Logger.addLog("ScreenDecoder init success")
        } catch (e: Exception) {
            e.printStackTrace()
            Logger.addELog("ScreenDecoder init fail. createEncoderByType error:${e.message}")
        }
    }

    override fun run() {

    }

    fun decodeData(bytes: ByteArray) {
        Logger.addLog("decodeData:${bytes.size}")
        val index = mMediaCodec.dequeueInputBuffer(DECODE_TIME_OUT)
        if (index >= 0) {
            val inputBuffer = mMediaCodec.getInputBuffer(index)
            Logger.addLog("decodeData inputBuffer:${inputBuffer?.remaining()}")
            inputBuffer?.apply {
                clear()
                put(bytes, 0, bytes.size)
            }
            mMediaCodec.queueInputBuffer(index, 0, bytes.size, System.currentTimeMillis(), 0)
        }
        val bufferInfo = MediaCodec.BufferInfo()
        var outputBufferIndex = mMediaCodec.dequeueOutputBuffer(bufferInfo, DECODE_TIME_OUT)
        Logger.addLog("decodeData outputBufferIndex:$outputBufferIndex")
        while (outputBufferIndex >= 0) {
            mMediaCodec.releaseOutputBuffer(outputBufferIndex, true)
            outputBufferIndex = mMediaCodec.dequeueOutputBuffer(bufferInfo, 0)
            Logger.addLog("decodeData outputBufferIndex:$outputBufferIndex")
        }
    }

    fun stopDecoder() {
        mMediaCodec.stop()
        mMediaCodec.release()
        Logger.addLog("ScreenDecoder stop")
    }
}