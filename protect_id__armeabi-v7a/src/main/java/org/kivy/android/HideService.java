package org.kivy.android;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.view.WindowManager;

import org.protect_id.R;

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
        Intent notificationIntent = new Intent(HideService.this, PythonActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(HideService.this,
                0, notificationIntent,
                PendingIntent.FLAG_CANCEL_CURRENT);
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(getApplicationContext(),"ProtectID")
                        .setSmallIcon(R.drawable.icon)
                        .setContentTitle("ProtectID")
                        .setContentText("Locked device")
                        .setContentIntent(contentIntent)
                        .setAutoCancel(true)
                        .setDefaults(Notification.COLOR_DEFAULT)
                        .setPriority(NotificationManager.IMPORTANCE_LOW)
                ;

        Notification notification = builder.build();
        notification.flags = notification.flags|Notification.FLAG_NO_CLEAR;
        startForeground(2,notification);
        //create fake camera view
        Log.v("Hi","SHOW");

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mWindowManager.removeView(cameraSourceCameraPreview);
    }
}
