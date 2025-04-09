package com.peihua.screencastpush

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.IBinder
import com.peihua.logger.Logger

class ScreenService : Service() {
    private val mMediaProjectionManager: MediaProjectionManager by lazy {
        getSystemService(MediaProjectionManager::class.java)
    }
    private var resultData: Intent? = null
    private var requestCode = 0
    private val mSocketManager: SocketManager by lazy {
        val mediaProjection = mMediaProjectionManager.getMediaProjection(
            requestCode,
            resultData!!
        )
        SocketManager(mediaProjection)
    }

    fun Intent?.getParcelableExtraCompat(name: String, clazz: Class<Intent>): Intent? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            this?.getParcelableExtra(name, clazz)
        } else {
            return this?.getParcelableExtra(name)
        }
    }

    override fun onCreate() {
        super.onCreate()
        Logger.addLog("start service")
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        Logger.addLog("create notification channel")
        val builder = Notification.Builder(this.applicationContext)
        val intent = Intent(this, ScreenCastActivity::class.java)
        builder.setContentIntent(
            PendingIntent.getActivity(
                this, 0, intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
        )
        builder.setSmallIcon(R.drawable.ic_screen_cast)
        builder.setContentTitle("屏幕共享")
        builder.setContentText("屏幕共享中")
        builder.setWhen(System.currentTimeMillis())
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId("screen_cast")
            getSystemService(NotificationManager::class.java).createNotificationChannel(
                NotificationChannel(
                    "screen_cast",
                    "屏幕共享",
                    NotificationManager.IMPORTANCE_HIGH
                )
            )
        }
        builder.setDefaults(Notification.DEFAULT_SOUND)
        startForeground(1112, builder.build())
        Logger.addLog("create notification channel end")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        requestCode = intent?.getIntExtra("requestCode", 0) ?: 0
        resultData = intent?.getParcelableExtraCompat("resultData", Intent::class.java)
        Logger.addLog("onStartCommand>>>$requestCode,$resultData")
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        mSocketManager.close()
        super.onDestroy()
        Logger.addLog("stop service")
    }
}