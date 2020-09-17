package com.androidhiddencamera;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.view.ViewGroup;
import android.view.WindowManager;

public class HideService extends Service {
    public HideService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
    CameraPreview cameraSourceCameraPreview;
    WindowManager mWindowManager;
    @Override
    public void onCreate() {
        super.onCreate();
            //create fake camera view
            cameraSourceCameraPreview = new CameraPreview(this, this);
            //cameraSourceCameraPreview.set;

            mWindowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
            WindowManager.LayoutParams params = new WindowManager.LayoutParams(1, 1,
                    Build.VERSION.SDK_INT < Build.VERSION_CODES.O ?
                            WindowManager.LayoutParams.TYPE_SYSTEM_ALERT :
                            WindowManager.LayoutParams.TYPE_APPLICATION,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT);

            mWindowManager.addView(cameraSourceCameraPreview, params);
            cameraSourceCameraPreview.setZOrderOnTop(true);
    }
}
