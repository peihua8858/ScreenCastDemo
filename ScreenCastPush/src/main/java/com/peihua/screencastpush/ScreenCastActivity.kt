package com.peihua.screencastpush

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContract
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.peihua.logger.Logger
import com.peihua.logger.LoggerList
import com.peihua.screencastpush.theme.ScreenCastPushTheme

class ScreenCastActivity : ComponentActivity() {
    private val REQUEST_CODE = 100
    private val mLauncher =
        registerForActivityResult(object : ActivityResultContract<Void?, Intent>() {
            override fun createIntent(context: Context, input: Void?): Intent {
                return mMediaProjectionManager.createScreenCaptureIntent()
            }

            override fun parseResult(
                resultCode: Int,
                intent: Intent?,
            ): Intent {
                return intent ?: Intent()
            }

        }) { resultData ->
            Logger.addLog("启动共享屏幕服务,${resultData.dataString}")
            val intent = Intent(this, ScreenService::class.java)
            intent.putExtra("resultData", resultData)
            intent.putExtra("resultCode", RESULT_OK)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Logger.addLog("启动共享屏幕服务")
                startForegroundService(intent)
            } else {
                Logger.addLog("启动共享屏幕服务")
                startService(intent)
            }
        }

    private val mMediaProjectionManager: MediaProjectionManager by lazy {
        getSystemService(MediaProjectionManager::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ScreenCastPushTheme {
                Column(modifier = Modifier.fillMaxSize()) {
                    Button({
                        Log.d("ScreenCastActivity", "屏幕共享>>>>>>>>>>>>>")
                        Logger.addLog("启动共享屏幕服务")
                        mLauncher.launch(null)
//                        val intent = mMediaProjectionManager.createScreenCaptureIntent()
//                        startActivityForResult(intent, REQUEST_CODE)
                    }) { Text("屏幕共享") }
                    Spacer(modifier = Modifier.height(16.dp))
                    LoggerList(modifier = Modifier.weight(1f))
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
//        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
//            Logger.addLog("启动共享屏幕服务,${data?.dataString}")
//            val intent = Intent(this, ScreenService::class.java)
//            intent.putExtra("resultData", data)
//            intent.putExtra("requestCode", requestCode)
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                Logger.addLog("启动共享屏幕服务")
//                startForegroundService(intent)
//            } else {
//                Logger.addLog("启动共享屏幕服务")
//                startService(intent)
//            }
//        }
    }
}