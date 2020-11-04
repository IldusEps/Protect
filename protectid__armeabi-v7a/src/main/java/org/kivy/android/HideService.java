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

import org.kivy.protectid.R;

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
        Intent notificationIntent = new Intent(HideService.this, HideService.class);
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
        Canvas canvas=new Canvas();
        Paint mPaint=new Paint();
        mPaint.setColor(Color.rgb(61,183,1));
        mPaint.setStyle(Paint.Style.FILL);
        Paint tPaint=new Paint();
        tPaint.setColor(Color.RED);
        tPaint.setStyle(Paint.Style.STROKE);
        tPaint.setTextAlign(Paint.Align.CENTER);
        tPaint.setTextSize(35f);
        canvas.drawPaint(mPaint);
        canvas.drawText("Device locked",0,0,tPaint);
        cameraSourceCameraPreview.draw(canvas);
        cameraSourceCameraPreview.onDrawForeground(canvas);
        cameraSourceCameraPreview.setZOrderOnTop(true);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mWindowManager.removeView(cameraSourceCameraPreview);
    }
}
