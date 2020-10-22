package org.kivy.android;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.KeyguardManager;
import android.app.PendingIntent;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.nfc.Tag;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Toast;

import com.androidhiddencamera.CameraConfig;
import com.androidhiddencamera.CameraError;
import com.androidhiddencamera.CameraPreview;
import com.androidhiddencamera.HiddenCameraService;
import com.androidhiddencamera.HiddenCameraUtils;
import com.androidhiddencamera.config.CameraFacing;
import com.androidhiddencamera.config.CameraFocus;
import com.androidhiddencamera.config.CameraImageFormat;
import com.androidhiddencamera.config.CameraResolution;
import com.androidhiddencamera.config.CameraRotation;

import org.bytedeco.javacpp.DoublePointer;
import org.bytedeco.javacpp.IntPointer;
import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_face;
import org.bytedeco.javacpp.opencv_objdetect;
import org.kivy.protectid.R;
import org.renpy.android.ResourceManager;

import java.io.File;

import static android.app.admin.DevicePolicyManager.FLAG_EVICT_CREDENTIAL_ENCRYPTION_KEY;
import static android.content.Context.DEVICE_POLICY_SERVICE;
import static android.support.v4.content.ContextCompat.getSystemService;
import static android.support.v4.content.ContextCompat.startActivity;
import static org.bytedeco.javacpp.opencv_face.createLBPHFaceRecognizer;
import static org.bytedeco.javacpp.opencv_imgcodecs.CV_LOAD_IMAGE_GRAYSCALE;
import static org.bytedeco.javacpp.opencv_imgcodecs.imread;
import static org.bytedeco.javacpp.opencv_imgcodecs.imwrite;

public class MyService extends HiddenCameraService {
    opencv_face.FaceRecognizer faceRecognizer;
    opencv_objdetect.CascadeClassifier face_cascade;
    opencv_core.RectVector faces;
    IntPointer label;
    DoublePointer confidence;
    Integer i;
    Thread run;
    int wait_int;
    boolean boolHideServ;
    public void onCreate() {
        i = 0;
        faceRecognizer = createLBPHFaceRecognizer();
        faceRecognizer.load(getFilesDir().getAbsolutePath() + "/mymodel.xml");
        label = new IntPointer(1);
        confidence = new DoublePointer(1);
        face_cascade = new opencv_objdetect.CascadeClassifier(
                getFilesDir().getAbsolutePath() + "/app/lbpcascade_frontalface.xml");
        faces = new opencv_core.RectVector();
        boolHideServ = false;
        SharedPreferences prefs=getSharedPreferences("setting",Context.MODE_PRIVATE);
        if (prefs.contains("wait")){
            wait_int=prefs.getInt("wait",2)*1000;
        } else wait_int=2000;

    }
    protected void onHandleIntent(@Nullable Intent intent) {


    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        run.stop();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.v("Hi!!","Hi");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {

            if (HiddenCameraUtils.canOverDrawOtherApps(this)) {
                CameraConfig cameraConfig = new CameraConfig()
                        .getBuilder(this)
                        .setImageRotation(CameraRotation.ROTATION_270)
                        .setCameraFacing(CameraFacing.FRONT_FACING_CAMERA)
                        .setCameraResolution(CameraResolution.HIGH_RESOLUTION)
                        .setImageFormat(CameraImageFormat.FORMAT_PNG)
                        .setCameraFocus(CameraFocus.AUTO)
                        .build();

                startCamera(cameraConfig);

                run = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        while(true) {
                            try {
                                Thread.sleep(wait_int);
                                SharedPreferences prefs=getSharedPreferences("setting",Context.MODE_PRIVATE);
                                if (prefs.contains("state")){
                                    if (prefs.getString("state","off")=="off"){
                                        stopSelf();
                                    }
                                }
                                takePicture();

                            } catch(InterruptedException ex){

                            }
                        }
                    }
                });
                run.start();
            } else {

                //Open settings to grant permission for "Draw other apps".
                HiddenCameraUtils.openDrawOverPermissionSetting(this);
            }
        } else {

            //TODO Ask your parent activity for providing runtime permission
            Toast.makeText(this, "Camera permission not available", Toast.LENGTH_SHORT).show();
        }
        return START_NOT_STICKY;
    }
    public static final int RESULT_ENABLE = 11;
    @Override
    public void onImageCapture(@NonNull File imageFile) {

        Log.v("Hi",imageFile.getAbsolutePath());
        opencv_core.Mat image = imread(imageFile.getAbsolutePath(), CV_LOAD_IMAGE_GRAYSCALE);
        int predicted_label = -1;
        double predicted_confidence = 0.0;
        // Get the prediction and associated confidence from the model
        face_cascade.detectMultiScale(image,faces);
        Boolean bool=false;
        for (int i = 0; i < faces.size(); i++) {
            opencv_core.Rect face_i = faces.get(i);
            faceRecognizer.predict(new opencv_core.Mat(image, face_i), label, confidence);
            Log.v("My","1");
            //for (int j = 0; j < label.sizeof(); j++) {
            Log.v("Hie", Integer.toString(label.get(0)));
            Log.v("Hie", Double.toString(confidence.get(0)));
            if (confidence.get(0) > 1) bool = true;
            //}
        }
        Log.v("Hie", Integer.toString(label.get(0)));
        Log.v("Hie", Double.toString(confidence.get(0)));
        //Toast.makeText(MyService.this,"Capturing image."+Double.toString(confidence.get(0)), Toast.LENGTH_SHORT).show();
        i=i+1;
        Toast.makeText(MyService.this,
                "Time-end", Toast.LENGTH_SHORT).show();
        if (bool==false){
            Log.v("Hi", "Lock");
            if (boolHideServ==false) {
                Intent intent = new Intent(getApplicationContext(), HideService.class);
                getApplicationContext().startService(intent);
                boolHideServ=true;
                wait_int=500;
            }
        } else{
            Intent intent = new Intent(getApplicationContext(), HideService.class);
            getApplicationContext().stopService(intent);
            SharedPreferences prefs=getSharedPreferences("setting",Context.MODE_PRIVATE);
            if (prefs.contains("wait")){
                wait_int=prefs.getInt("wait",2)*1000;
            } else wait_int=2000;
            if (boolHideServ==false) {
                AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                Log.v("Hi!!", "Hi");
                Intent alarmIntent = new Intent(this, MyReceiver.class);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, alarmIntent, 0);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, wait_int - 1000, pendingIntent);
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, wait_int - 1000, pendingIntent);
                } else {
                    alarmManager.set(AlarmManager.RTC_WAKEUP, wait_int - 1000, pendingIntent);
                }
                stopSelf();
            }
        }
    }

    @Override
    public void onCameraError(int errorCode) {
        if (errorCode== CameraError.ERROR_CAMERA_OPEN_FAILED){
            Log.v("Hi","ERROR_CAMERA_OPEN_FAILED");
        }
        if (errorCode==CameraError.ERROR_IMAGE_WRITE_FAILED) Log.v("Hi","ERROR_IMAGE_WRITE_FAILED");
        if (errorCode==CameraError.ERROR_CAMERA_PERMISSION_NOT_AVAILABLE) Log.v("Hi","ERROR_CAMERA_PERMISSION_NOT_AVAILABLE");
        if (errorCode==CameraError.ERROR_DOES_NOT_HAVE_FRONT_CAMERA) Log.v("Hi","ERROR_DOES_NOT_HAVE_FRONT_CAMERA");
        if (errorCode==CameraError.ERROR_DOES_NOT_HAVE_FRONT_CAMERA) Log.v("Hi","ERROR_DOES_NOT_HAVE_FRONT_CAMERA");
    }



}
