package org.kivy.android;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
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
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.media.Image;
import android.media.ImageReader;
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
import android.view.Surface;
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
import org.kivy.protectid.R;
import org.renpy.android.ResourceManager;

import java.io.File;
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

public class MyService extends Service {
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
    CameraManager cameraManager;

    SurfaceView cameraPreview;
    public void onCreate() {

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
        SharedPreferences prefs = getSharedPreferences("setting", Context.MODE_PRIVATE);
        if (prefs.contains("wait")) {
            wait_int = prefs.getInt("wait", 2) * 1000;
        } else wait_int = 2000;

        cameraManager = getSystemService(Context.CAMERA_SERVICE);
        String camId="";
        for (String id : cameraManager.getCameraIdList()){
            CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(Id);
            if (characteristics.get(CameraCharacteristics.LENS_FACING)==characteristics.LENS_FACING_FRONT){
                camId = id;
                break;
            }
        }


    }
    protected void onHandleIntent(@Nullable Intent intent) {


    }

    @Override
    public void onDestroy() {
        run.stop();
        super.onDestroy();
    }



    public class TimerTask_ extends TimerTask {
        @Override
        public void run() {
                    SharedPreferences prefs=getSharedPreferences("setting",Context.MODE_PRIVATE);
                    if (prefs.contains("state")){
                        if (prefs.getString("state","off")=="off"){
                            stopSelf();
                        }
                    }
                    takePicture();
        }
    }

    public class TimerTask1_ extends TimerTask {
        @Override
        public void run() {
            SharedPreferences prefs=getSharedPreferences("setting",Context.MODE_PRIVATE);
            if (prefs.contains("state")){
                if (prefs.getString("state","off")=="off"){
                    stopSelf();
                }
            }
        }
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
                        .setCameraResolution(CameraResolution.MEDIUM_RESOLUTION)
                        .setImageFormat(CameraImageFormat.FORMAT_PNG)
                        .setCameraFocus(CameraFocus.AUTO)
                        .build();

                startCamera(cameraConfig);

                run = new Thread(new Runnable() {
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
                });
                TimerTask_ timerTask = new TimerTask_();
                Timer timer = new Timer(true);
                timer.scheduleAtFixedRate(timerTask, 0, wait_int);

                TimerTask1_ timerTask1 = new TimerTask1_();
                Timer timer1 = new Timer(true);
                timer1.scheduleAtFixedRate(timerTask1, 0, 500);
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

    public class myImageReader extends ImageReader {

        public void onImageAvailable(ImageReader reader){
            Image image = reader.acquireLatestImage();
            Mat(image.getPlanes()[0].getBuffer().

            Log.v("Hi", imageFile.getAbsolutePath());
            opencv_core.Mat image = imread(imageFile.getAbsolutePath(), CV_LOAD_IMAGE_GRAYSCALE);
            //imwrite(Environment.getDataDirectory().getAbsolutePath()+"/data/org.kivy.protectid/files/app/fff.png",image);
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
                    //imwrite(Environment.getDataDirectory().getAbsolutePath()+"/data/org.kivy.protectid/files/app/fff"+Integer.toString(i)+".png",mat);
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
            Toast.makeText(MyService.this, "Capturing image." + Double.toString(confidence.get(0)), Toast.LENGTH_SHORT).show();
            i = i + 1;
            Toast.makeText(MyService.this,
                    "Time-end", Toast.LENGTH_SHORT).show();
            if ((bool == false) | (confidence.get(0) < 1)) {
                if (boolHideServ == false) {
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
                    Canvas canvas = new Canvas();
                    Paint mPaint = new Paint();
                    mPaint.setColor(Color.rgb(61, 183, 1));
                    mPaint.setStyle(Paint.Style.FILL);
                    Paint tPaint = new Paint();
                    tPaint.setColor(Color.RED);
                    tPaint.setStyle(Paint.Style.STROKE);
                    tPaint.setTextAlign(Paint.Align.CENTER);
                    tPaint.setTextSize(35f);
                    canvas.drawPaint(mPaint);
                    canvas.drawText("Device locked", 0, 0, tPaint);
                    cameraSourceCameraPreview.draw(canvas);
                    cameraSourceCameraPreview.onDrawForeground(canvas);
                    cameraSourceCameraPreview.setZOrderOnTop(true);
                    boolHideServ = true;
                    wait_int = 100;

                    Log.v("Hi", "Lock");
                }
            } else {
                if (boolHideServ == true) {
                    mWindowManager.removeView(cameraSourceCameraPreview);
                    SharedPreferences prefs = getSharedPreferences("setting", Context.MODE_PRIVATE);
                    if (prefs.contains("wait")) {
                        wait_int = prefs.getInt("wait", 2) * 1000;
                    } else wait_int = 2000;
                }
                //Set next stap Service
            /*   AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
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
                stopSelf();*/
                boolHideServ = false;
            }
        }
    }

    public void onCameraError(int errorCode) {
        if (errorCode== CameraError.ERROR_CAMERA_OPEN_FAILED){
            Log.v("Hi","ERROR_CAMERA_OPEN_FAILED");
        }
        if (errorCode==CameraError.ERROR_IMAGE_WRITE_FAILED) Log.v("Hi","ERROR_IMAGE_WRITE_FAILED");
        if (errorCode==CameraError.ERROR_CAMERA_PERMISSION_NOT_AVAILABLE) Log.v("Hi","ERROR_CAMERA_PERMISSION_NOT_AVAILABLE");
        if (errorCode==CameraError.ERROR_DOES_NOT_HAVE_FRONT_CAMERA) Log.v("Hi","ERROR_DOES_NOT_HAVE_FRONT_CAMERA");
        if (errorCode==CameraError.ERROR_DOES_NOT_HAVE_FRONT_CAMERA) Log.v("Hi","ERROR_DOES_NOT_HAVE_FRONT_CAMERA");
    }

public void StartPreview(){
    cameraPreview = new SurfaceView(MyService.this);
    mWindowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
    WindowManager.LayoutParams params = new WindowManager.LayoutParams(1,
            1,
            Build.VERSION.SDK_INT < Build.VERSION_CODES.O ?
                    WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY :
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
            PixelFormat.TRANSLUCENT);
    mWindowManager.addView(cameraPreview, params);
    cameraPreview.setZOrderOnTop(true);
}

}
