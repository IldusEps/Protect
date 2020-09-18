package org.kivy.android;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.androidhiddencamera.CameraCallbacks;
import com.androidhiddencamera.CameraPreview;

public class HideService extends Service  {
    public HideService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
    SurfaceView cameraSourceCameraPreview;
    WindowManager mWindowManager;
    @Override
    public void onCreate() {
        super.onCreate();
            //create fake camera view
        Log.v("Hi","SHOW");
            cameraSourceCameraPreview = new SurfaceView(this);
            //cameraSourceCameraPreview.set;

            mWindowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
            WindowManager.LayoutParams params = new WindowManager.LayoutParams(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    Build.VERSION.SDK_INT < Build.VERSION_CODES.O ?
                            WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY :
                            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                    PixelFormat.TRANSLUCENT);
            mWindowManager.addView(cameraSourceCameraPreview, params);
            cameraSourceCameraPreview.setZOrderOnTop(true);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mWindowManager.removeView(cameraSourceCameraPreview);
    }
}
