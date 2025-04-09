package com.peihua.screencastreceiver

import android.os.Bundle
import android.view.SurfaceHolder
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.peihua.logger.LoggerList
import com.peihua.screencastreceiver.theme.ScreenCastReceiverTheme

class ScreenCastReceiverActivity : ComponentActivity() {
    private val mSocketClientManager = SocketClientManager()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //全屏设置
        window.setFlags(
            android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN,
            android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        setContent {
            ScreenCastReceiverTheme {
                val hostValue = remember { mutableStateOf("") }
                Box {
                    AndroidView(
                        factory = {
                            android.view.SurfaceView(it)
                        }, modifier = Modifier.fillMaxSize()
                    ) {
                        it.holder.addCallback(surfaceCallback())
                    }
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(alignment = Alignment.BottomCenter)
                            .background(Color(0xFFFFFFFF))
                    ) {
                        LoggerList(
                            modifier = Modifier
                                .height(300.dp)
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFFFFFFF))
                                .padding(8.dp)
                        ) {
                            OutlinedTextField(
                                modifier = Modifier.weight(1f),
                                placeholder = { Text("请输入ip") },
                                value = hostValue.value,
                                onValueChange = {
                                    hostValue.value = it
                                })
                            Button({
                                mSocketClientManager.setHost(hostValue.value)
                            }) { Text("连接") }
                        }
                    }
                }
            }
        }
    }

    private fun surfaceCallback(): SurfaceHolder.Callback {
        return object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                // 创建一个屏幕共享
                mSocketClientManager.setSurface(holder.surface)
            }

            override fun surfaceChanged(
                holder: SurfaceHolder,
                format: Int,
                width: Int,
                height: Int,
            ) {
            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
            }
        }
    }

    override fun onDestroy() {
        mSocketClientManager.stop()
        super.onDestroy()
    }
}