package org.kivy.android;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.hardware.Sensor;
import android.hardware.SensorManager;
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
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
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
import org.bytedeco.javacpp.opencv_imgproc;
import org.bytedeco.javacpp.opencv_objdetect;
import org.IldusEps.protect_id.R;
import org.renpy.android.ResourceManager;

import java.io.File;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static android.app.admin.DevicePolicyManager.FLAG_EVICT_CREDENTIAL_ENCRYPTION_KEY;
import static android.content.Context.DEVICE_POLICY_SERVICE;
import static android.support.v4.content.ContextCompat.getSystemService;
import static android.support.v4.content.ContextCompat.startActivity;
import static org.bytedeco.javacpp.opencv_face.createEigenFaceRecognizer;
import static org.bytedeco.javacpp.opencv_face.createFisherFaceRecognizer;
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
    int intShoting;
    opencv_core.Size sizeImg;
    Notification notification;
    SurfaceView cameraSourceCameraPreview;
    WindowManager mWindowManager;
    View view;
    KeyguardManager myKM;
    SharedPreferences.Editor editor;
    SharedPreferences prefs;
    int count;

    public void onCreate() {
        super.onCreate();
        mWindowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
         myKM = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        Intent notificationIntent = new Intent(MyService.this, PythonActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(MyService.this,
                0, notificationIntent,
                PendingIntent.FLAG_CANCEL_CURRENT);
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(getApplicationContext(),"ProtectID")
                        .setSmallIcon(R.drawable.icon)
                        .setContentTitle("ProtectID")
                        .setContentText("Нажмите, что бы остановить приложение")
                        .setContentIntent(contentIntent)
                        .setAutoCancel(true)
                        .setDefaults(Notification.COLOR_DEFAULT)
                        .setPriority(NotificationManager.IMPORTANCE_LOW);

        prefs = getApplicationContext().getSharedPreferences("setting", Context.MODE_PRIVATE);
        editor = prefs.edit();

        notification = builder.build();
        notification.flags = notification.flags|Notification.FLAG_NO_CLEAR;
        startForeground(1,notification);
        intShoting = 0;
        i = 0;
        faceRecognizer = createLBPHFaceRecognizer(2,10,10,10,20.0);
        // faceRecognizer = createFisherFaceRecognizer();
        faceRecognizer.load(getFilesDir().getAbsolutePath() + "/mymodel.xml");
        label = new IntPointer(1);
        confidence = new DoublePointer(1);
        face_cascade = new opencv_objdetect.CascadeClassifier(
                getFilesDir().getAbsolutePath() + "/app/lbpcascade_frontalface_improved.xml");
        faces = new opencv_core.RectVector();
        boolHideServ = false;
        if (prefs.contains("wait")) {
            wait_int = prefs.getInt("wait", 2) * 1000;
        } else wait_int = 2000;
        SharedPreferences.Editor editor=prefs.edit();
        count = 0;
        if (prefs.contains("count"))
            count = prefs.getInt("count", 0);
        //read size FisherImage
        /*int sizeImg1 = 0;
        int sizeImg2 = 0;
        if (prefs.contains("size1")) {
            sizeImg1 = prefs.getInt("size1", 800);
        }
        if (prefs.contains("size2")) {
            sizeImg2 = prefs.getInt("size2", 800);
        }
        sizeImg = new opencv_core.Size(sizeImg1,sizeImg2);*/
    }
    protected void onHandleIntent(@Nullable Intent intent) {


    }

    @Override
    public void onDestroy() {
        if (boolHideServ == true) {
            mWindowManager.removeView(view);
        }
        super.onDestroy();

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public class TimerTask_ extends TimerTask {
        @Override
        public void run() {
                    if (prefs.contains("state")){
                        if (prefs.getString("state","off")=="off"){
                            stopSelf();
                        }
                    }
                    Log.v("HiRR",Integer.toString(mWindowManager.getDefaultDisplay().getRotation()));
                    int rot=270;
                    if (mWindowManager.getDefaultDisplay().getRotation() == 1) {
                        rot=0;
                    }
                    else if (mWindowManager.getDefaultDisplay().getRotation() == 2) {
                        rot=90;
                    }
                    else if (mWindowManager.getDefaultDisplay().getRotation() == 3) {
                            rot=180;
                    }
                    takePicture(rot);

                    count = count + 1;
                    if (count % 5 == 0) editor.putInt("count", count).apply();

                    timer.cancel();
                    timer = new Timer();
                    timer.schedule(new TimerTask_(), wait_int);
        }
    }
    Timer timer;
    Timer timer1;

    public class TimerTask1_ extends TimerTask {
        @Override
        public void run() {
            SharedPreferences prefs = getSharedPreferences("setting", Context.MODE_PRIVATE);
            if (prefs.contains("state")) {
                if (prefs.getString("state", "off") == "off") {
                    stopSelf();
                }
            }
            if (myKM.isDeviceLocked()) {
                if (boolHideServ == true) {
                    Log.v("Hi", "Locked");
                    mWindowManager.removeView(view);
                    boolHideServ = false;
                    if (prefs.contains("wait")) {
                        wait_int = prefs.getInt("wait", 5) * 1000;
                    } else wait_int = 5000;
                }
            }
        }
    }



        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            Log.v("Hi!!", "Hi");
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED) {

                if (HiddenCameraUtils.canOverDrawOtherApps(this)) {
                    CameraConfig cameraConfig = new CameraConfig()
                            .getBuilder(this)
                            .setImageRotation(CameraRotation.ROTATION_270)
                            .setCameraFacing(CameraFacing.FRONT_FACING_CAMERA)
                            .setCameraResolution(CameraResolution.MEDIUM_RESOLUTION)
                            .setImageFormat(CameraImageFormat.FORMAT_PNG)
                            .setCameraFocus(CameraFocus.AUTO)
                            .build();

                    startCamera(cameraConfig);
                   /* run = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            while(true) {
                                //  try {
                                //Thread.sleep(wait_int);
                                SharedPreferences prefs=getSharedPreferences("setting",Context.MODE_PRIVATE);
                                if (prefs.contains("state")){
                                    if (prefs.getString("state","off")=="off"){
                                        stopSelf();
                                    }
                                }
                                takePicture();

                                //} catch(InterruptedException ex){ }
                            }
                        }
                    });*/

                    boolHideServ = false;
                    LayoutInflater layoutManager = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    view = layoutManager.inflate(R.layout.locklayout, null);


                    timer = new Timer(true);
                    timer.scheduleAtFixedRate( new TimerTask_(), 0, wait_int);

                    Log.v("Me", "This is me!");
                    timer1 = new Timer(true);
                    timer1.scheduleAtFixedRate(new TimerTask1_(), 0, 500);
                    //run.start();
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

            Log.v("Hi", imageFile.getAbsolutePath());
            opencv_core.Mat image = imread(imageFile.getAbsolutePath(), CV_LOAD_IMAGE_GRAYSCALE);
            //imwrite(Environment.getDataDirectory().getAbsolutePath()+"/data/org.IldusEps.protect_id/files/app/fff.png",image);
            int predicted_label = -1;
            double predicted_confidence = 0.0;
            // Get the prediction and associated confidence from the model
            face_cascade.detectMultiScale(image, faces);
            Boolean bool = false;
            if (faces.size() > 0) {
                for (int i = 0; i < faces.size(); i++) {
                    opencv_core.Rect face_i = faces.get(i);
                    //resize Image
                    opencv_core.Mat mat = new opencv_core.Mat(image, face_i);
                    //opencv_imgproc.resize(mat, mat, sizeImg);
                    //predict Image
                    faceRecognizer.predict(mat, label, confidence);
                    Log.v("My", "1");
                    //for (int j = 0; j < label.sizeof(); j++) {
                    Log.v("Hie", Integer.toString(label.get(0)));
                    Log.v("Hie", Double.toString(confidence.get(0)));
                    //imwrite(Environment.getDataDirectory().getAbsolutePath()+"/data/org.IldusEps.protect_id/files/app/fff"+Integer.toString(i)+".png",mat);
                    if (confidence.get(0) > 1) bool = true;
                    //}
                }
            } else {
                bool = false;
                label.put(0);
                confidence.put(0.0);
                Log.v("Hie", "Null");
            }
            Log.v("Hie", Integer.toString(label.get(0)));
            Log.v("Hie", Double.toString(confidence.get(0)));
            i = i + 1;


            if (myKM.isDeviceLocked()) {
                if (boolHideServ == true) {
                    Log.v("Hi", "Locked");
                    mWindowManager.removeView(view);
                    boolHideServ = false;
                    SharedPreferences prefs = getSharedPreferences("setting", Context.MODE_PRIVATE);
                    if (prefs.contains("wait")) {
                        wait_int = prefs.getInt("wait", 5) * 1000;
                    } else wait_int = 5000;
                }
            } else if ((bool == false) | (confidence.get(0) < 1)) {
                if (boolHideServ == false) {


                    cameraSourceCameraPreview = new SurfaceView(this);
                    //cameraSourceCameraPreview.set;
                    WindowManager.LayoutParams params = new WindowManager.LayoutParams(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                            WindowManager.LayoutParams.FLAG_FULLSCREEN,
                            Build.VERSION.SDK_INT < Build.VERSION_CODES.O ?
                                    WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY :
                                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                            WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                            PixelFormat.TRANSLUCENT);
                    mWindowManager.addView(view, params);
                    view.setZ(99);
                    boolHideServ = true;
                    wait_int = 1000;

                    Log.v("Hi", "Lock");
                }
            } else {
                if (boolHideServ == true) {
                    mWindowManager.removeView(view);
                    SharedPreferences prefs = getSharedPreferences("setting", Context.MODE_PRIVATE);
                    if (prefs.contains("wait")) {
                        wait_int = prefs.getInt("wait", 2) * 1000;
                    } else wait_int = 2000;
                }
                boolHideServ = false;
            }

            image.close();
        }

        @Override
        public void onCameraError(int errorCode) {
            if (errorCode == CameraError.ERROR_CAMERA_OPEN_FAILED) {
                Log.v("Hi", "ERROR_CAMERA_OPEN_FAILED");
            }
            if (errorCode == CameraError.ERROR_IMAGE_WRITE_FAILED)
                Log.v("Hi", "ERROR_IMAGE_WRITE_FAILED");
            if (errorCode == CameraError.ERROR_CAMERA_PERMISSION_NOT_AVAILABLE)
                Log.v("Hi", "ERROR_CAMERA_PERMISSION_NOT_AVAILABLE");
            if (errorCode == CameraError.ERROR_DOES_NOT_HAVE_FRONT_CAMERA)
                Log.v("Hi", "ERROR_DOES_NOT_HAVE_FRONT_CAMERA");
            if (errorCode == CameraError.ERROR_DOES_NOT_HAVE_FRONT_CAMERA)
                Log.v("Hi", "ERROR_DOES_NOT_HAVE_FRONT_CAMERA");
        }
    }


