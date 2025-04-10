package com.peihua.miracast

import android.app.TvManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.display.DisplayManager
import android.net.Uri
import android.net.wifi.WifiManager
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pManager
import android.net.wifi.p2p.WifiP2pManager.Channel
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.widget.ArrayAdapter
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContract
import androidx.lifecycle.Observer
import androidx.mediarouter.media.MediaControlIntent
import androidx.mediarouter.media.MediaRouteSelector
import androidx.mediarouter.media.MediaRouter
import com.peihua.logger.Logger
import com.peihua.miracast.theme.MiracastTheme

class MiracastActivity : ComponentActivity(), WifiP2pManager.ChannelListener {
    companion object {
        const val TAG = "MiracastActivity"
        const val WFD_URL = 0
        const val WFD_CREATEGROUP = 1
        const val WFD_PINANY = 2
        const val WFD_RESET = 3
        const val WFD_STATE_INIT = 0
        const val WFD_STATE_READY = 1
        const val WFD_STATE_CONNECTING = 2
        const val WFD_STATE_CONNECTED = 3
        const val WFD_STATE_DISCONNECTED = 4
        private const val DEFAULT_CONTROL_PORT = 8554
        private const val MAX_THROUGHPUT = 50

        @JvmField
        public var p2pscan_flag = -1

        @JvmField
        public var disableWfd = 1
    }

    val curState = WFD_STATE_INIT

    private var mTV: TvManager? = null
    private val mWifiP2pManager: WifiP2pManager by lazy { getSystemService(WifiP2pManager::class.java) }
    private val mWifiManager: WifiManager by lazy { getSystemService(WifiManager::class.java) }
    private var isWifiP2pEnabled = false
    private var retryChannel = false
    private val intentFilter = IntentFilter()
    private val intentFilterPlayershutdown = IntentFilter()
    private var mChannel: Channel? = null
    private var receiver: BroadcastReceiver? = null
    private var PlayershutdownReceiver: BroadcastReceiver? = null
    var mCurConnectedDevice: WifiP2pDevice? = null
    private var ipAddr: String? = null
    public var mWfdEnabled = false
    public var mWfdEnabling = false
    public var devname: String? = null
    private var ImgProgressCount = 0;
    public var bWait = true;
    private val mHandler = Handler(Looper.getMainLooper());
    public var timerUI_flag: CountDownTimer? = null;
    public var timer_flag: CountDownTimer? = null;


    private var mediaRouter: MediaRouter? = null
    private var mSelector: MediaRouteSelector? = null
    private var mSelectedRoute: MediaRouter.RouteInfo? = null
    private var mPlayer: Player? = null
    private var uri: Uri? = null
    private val mLauncher =
        registerForActivityResult(object : ActivityResultContract<Intent, Intent>() {
            override fun createIntent(context: Context, input: Intent): Intent {
                return input
            }

            override fun parseResult(resultCode: Int, intent: Intent?): Intent {
                return intent ?: Intent()
            }
        }) {

        }

    private lateinit var adapter: ArrayAdapter<WifiDisplay>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkWifiP2pState()
        enableEdgeToEdge()
        setContent {
            MiracastTheme {

            }
        }
    }

    private fun checkWifiP2pState() {
        if (!mWifiManager.isWifiEnabled) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q)
                mWifiManager.isWifiEnabled = true
            else {
                val panelIntent = Intent(Settings.Panel.ACTION_WIFI);
                startActivityForResult(panelIntent, 1);
            }
        }
        mChannel = mWifiP2pManager.initialize(this, mainLooper, this)

        WifiDisplayManager.attach(applicationContext)
        // Get the media router service.
        mediaRouter = MediaRouter.getInstance(this)
        // Create a route selector for the type of routes your app supports.
        mSelector = MediaRouteSelector.Builder()
            // These are the framework-supported intents
            .addControlCategory(MediaControlIntent.CATEGORY_REMOTE_PLAYBACK)
            .addControlCategory(MediaControlIntent.CATEGORY_LIVE_VIDEO)
            .build()
        WifiDisplayManager.displays.observe(this, Observer {
            adapter.clear()
            adapter.addAll(it)
        })

    }

    private val mediaRouterCallback = object : MediaRouter.Callback() {
        override fun onRouteSelected(
            router: MediaRouter,
            route: MediaRouter.RouteInfo,
            reason: Int,
        ) {
            if (route.supportsControlCategory(MediaControlIntent.CATEGORY_REMOTE_PLAYBACK)) {
                Logger.addLog("onRouteSelected>>>remote playback device ${route.name}")
            } else {
                Logger.addLog("onRouteSelected>>>secondary output device ${route.name}")
                mSelectedRoute = route
                mPlayer = Player.create(applicationContext, route)
                mPlayer?.updatePresentation()
            }
        }

        override fun onRouteAdded(router: MediaRouter, route: MediaRouter.RouteInfo) {
            Logger.addLog("onRouteAdded>>>router: ${route.name}")
        }

        override fun onRouteChanged(router: MediaRouter, route: MediaRouter.RouteInfo) {
            Logger.addLog("onRouteChanged>>>router: $route")
            if (route == mSelectedRoute) {
                WifiDisplayManager.displayManager.getDisplays(DisplayManager.DISPLAY_CATEGORY_PRESENTATION)
                    .forEach {
                        if (it.name == route.name) {
                            mPlayer?.updatePresentation(it)
                            mPlayer?.play(PlaylistItem(null, null, uri, null, null))
                        }
                    }
            }
        }
    }

    private fun fileSearch() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "*/*"
        startActivityForResult(intent, 101)
    }

    override fun onStart() {
        WifiDisplayManager.startScan()
        mediaRouter?.addCallback(
            mSelector!!,
            mediaRouterCallback,
            MediaRouter.CALLBACK_FLAG_REQUEST_DISCOVERY
        )
        super.onStart()
    }

    override fun onStop() {
        WifiDisplayManager.stopScan()
        mediaRouter?.removeCallback(mediaRouterCallback)
        super.onStop()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data == null || resultCode != RESULT_OK) return
        if (requestCode == 101) {
            uri = data.data
        }
    }

    override fun onChannelDisconnected() {
        TODO("Not yet implemented")
    }
}