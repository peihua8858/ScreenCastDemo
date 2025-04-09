package com.peihua.screencastreceiver;


import android.app.Activity;
import android.os.Bundle;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;

public class MainActivity extends Activity {

    private SocketClientManager mSocketClientManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SurfaceView surfaceView = findViewById(R.id.sv_screen);
        surfaceView
                .getHolder()
                .addCallback(
                        new SurfaceHolder.Callback() {
                            @Override
                            public void surfaceCreated(@NonNull SurfaceHolder holder) {
                                // 连接到服务端
                                initSocketManager(holder.getSurface());
                            }

                            @Override
                            public void surfaceChanged(
                                    @NonNull SurfaceHolder holder, int format, int width, int height) {
                            }

                            @Override
                            public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
                            }
                        });
    }

    private void initSocketManager(Surface surface) {
        mSocketClientManager = new SocketClientManager();
        mSocketClientManager.setSurface(surface);
        mSocketClientManager.setHost("172.16.0.81");
    }

    @Override
    protected void onDestroy() {
        if (mSocketClientManager != null) {
            mSocketClientManager.stop();
        }
        super.onDestroy();
    }
}