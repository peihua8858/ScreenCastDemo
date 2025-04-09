package com.peihua.screencastreceiver

import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.view.Surface
import com.peihua.logger.Logger

class ScreenDecoder() {
    private val VIDEO_WIDTH = 1280
    private val VIDEO_HEIGHT = 720
    private val SCREEN_FRAME_RATE = 20
    private val SCREEN_FRAME_INTERVAL = 1
    private val DECODE_TIME_OUT = 10000L
    private val mMediaCodec: MediaCodec by lazy {
        try {
            MediaCodec.createDecoderByType(MediaFormat.MIMETYPE_VIDEO_HEVC)
        } catch (e: Exception) {
            throw e
        }
    }

    fun start(surface: Surface) {
        Logger.addLog("ScreenDecoder init")
        // 配置MediaCodec
        val mediaFormat =
            MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_HEVC, VIDEO_WIDTH, VIDEO_HEIGHT);
        // 设置比特率
        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, VIDEO_WIDTH * VIDEO_HEIGHT);
        // 设置帧率
        mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, SCREEN_FRAME_RATE);
        mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, SCREEN_FRAME_INTERVAL);
        try {
            mMediaCodec.configure(mediaFormat, surface, null, 0)
            mMediaCodec.start()
            Logger.addLog("ScreenDecoder init success")
        } catch (e: Exception) {
            e.printStackTrace()
            Logger.addELog("ScreenDecoder init fail. createEncoderByType error:${e.message}")
        }
    }


    fun decodeData(data: ByteArray) {
        Logger.addLog("decodeData:${data.size}")
        try {
            val index = mMediaCodec.dequeueInputBuffer(DECODE_TIME_OUT);
            Logger.addLog("decodeData index:$index")
            if (index >= 0) {
                val inputBuffer = mMediaCodec.getInputBuffer(index);
                Logger.addLog("decodeData inputBuffer:${inputBuffer?.remaining()}")
                inputBuffer?.clear();
                inputBuffer?.put(data, 0, data.size);
                mMediaCodec.queueInputBuffer(index, 0, data.size, System.currentTimeMillis(), 0);
            }
            val bufferInfo =  MediaCodec.BufferInfo();
            var outputBufferIndex = mMediaCodec.dequeueOutputBuffer(bufferInfo, DECODE_TIME_OUT);
            Logger.addLog("decodeData outputBufferIndex:$outputBufferIndex")
            while (outputBufferIndex > 0) {
                mMediaCodec.releaseOutputBuffer(outputBufferIndex, true);
                outputBufferIndex = mMediaCodec.dequeueOutputBuffer(bufferInfo, 0);
                Logger.addLog("decodeData outputBufferIndex:$outputBufferIndex")
            }
        } catch (e: Exception) {
           e.printStackTrace()
            Logger.addELog("ScreenDecoder decodeData fail. dequeueInputBuffer error:${e.message}")
        }

//
//        val index = mMediaCodec.dequeueInputBuffer(DECODE_TIME_OUT)
//        if (index >= 0) {
//            val inputBuffer = mMediaCodec.getInputBuffer(index)
//            Logger.addLog("decodeData inputBuffer:${inputBuffer?.remaining()}")
//            inputBuffer?.apply {
//                clear()
//                put(bytes, 0, bytes.size)
//            }
//            mMediaCodec.queueInputBuffer(index, 0, bytes.size, System.currentTimeMillis(), 0)
//        }
//        val bufferInfo = MediaCodec.BufferInfo()
//        var outputBufferIndex = mMediaCodec.dequeueOutputBuffer(bufferInfo, DECODE_TIME_OUT)
//        Logger.addLog("decodeData outputBufferIndex:$outputBufferIndex")
//        while (outputBufferIndex >= 0) {
//            mMediaCodec.releaseOutputBuffer(outputBufferIndex, true)
//            outputBufferIndex = mMediaCodec.dequeueOutputBuffer(bufferInfo, 0)
//            Logger.addLog("decodeData outputBufferIndex:$outputBufferIndex")
//        }
    }

    fun stopDecoder() {
        mMediaCodec.stop()
        mMediaCodec.release()
        Logger.addLog("ScreenDecoder stop")
    }
}