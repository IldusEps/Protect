package org.kivy.android;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import org.bytedeco.javacpp.DoublePointer;
import org.bytedeco.javacpp.IntPointer;
import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_face;
import org.bytedeco.javacpp.opencv_imgproc;
import org.bytedeco.javacpp.opencv_objdetect;
import org.kivy.android.launcher.Project;
import org.protect_id.R;
import org.libsdl.app.SDLActivity;
import org.myapp.Hardware;
import org.renpy.android.ResourceManager;

import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.security.Permission;
import java.security.Permissions;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import static android.app.PendingIntent.getActivity;
import static android.content.Context.DEVICE_POLICY_SERVICE;
import static android.support.v4.content.ContextCompat.startActivity;
import static org.bytedeco.javacpp.opencv_face.createFisherFaceRecognizer;
import static org.bytedeco.javacpp.opencv_face.createLBPHFaceRecognizer;
import static org.bytedeco.javacpp.opencv_imgcodecs.CV_LOAD_IMAGE_GRAYSCALE;
import static org.bytedeco.javacpp.opencv_imgcodecs.imread;
import static org.bytedeco.javacpp.opencv_imgcodecs.imwrite;

public class PythonActivity extends SDLActivity {
    private static final String TAG = "PythonActivity";

    public static PythonActivity mActivity = null;

    private ResourceManager resourceManager = null;
    private Bundle mMetaData = null;
    private PowerManager.WakeLock mWakeLock = null;
    public String getAppRoot() {
        String app_root =  getFilesDir().getAbsolutePath() + "/app";
        return app_root;
    }
    public opencv_face.FaceRecognizer faceRecognizer;
    public opencv_objdetect.CascadeClassifier face_cascade;
    AlarmManager alarmManager;
    PendingIntent pendingIntent;

    public String getLang(){
        Log.v("Hie",Locale.getDefault().getLanguage());
        return Locale.getDefault().getLanguage();
    }
    Notification notification;

    public void restartNotify(String line, String line1) {
        Log.v("Helllo","I an");
        SharedPreferences prefs = getContext().getSharedPreferences("setting",Context.MODE_PRIVATE);
        SharedPreferences.Editor editor=prefs.edit();

        if (prefs.contains("state"))
            if (prefs.getString("state","on")=="on"){
                editor.putString("state","off").apply();
            }
        int clicks = 0;
        if (prefs.contains("clicks"))
            clicks = prefs.getInt("clicks", 0);

        Log.v("hi",line);
        int gg=2;
        if (line != "") {
            line = line.replace("сек", "");
            Log.v("Hi", Integer.toString(line.indexOf("мин")));
            if (line.indexOf("мин") > -1) line = line.replace("мин", "");
            line = line.replace("sec", "");
            if (line.indexOf("min") > -1) line = line.replace("min", "");
            line = line.replace(".", "");
            Log.v("hi", line);
             gg= Integer.parseInt(line);
        }
        if (line1 != "") Log.v("Hi",line1);
        editor.putInt("wait",gg);
        editor.putInt("clicks",clicks+1);
        editor.putString("state","on").apply();

        //requestPermissions(new String[] {Manifest.permission.SYSTEM_ALERT_WINDOW});
        //checkSelfPermission(Manifest.permission.SYSTEM_ALERT_WINDOW);
        Intent notificationIntent = new Intent(PythonActivity.this, PythonActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(PythonActivity.this,
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
                        .setPriority(NotificationManager.IMPORTANCE_LOW)
                ;

        notification = builder.build();
        notification.flags=notification.flags|Notification.FLAG_NO_CLEAR;

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        NotificationChannel notfChannel=new NotificationChannel("ProtectID","ProtectID",notificationManager.IMPORTANCE_DEFAULT);
        notfChannel.enableVibration(false);
        notfChannel.enableLights(false);
        notfChannel.setImportance(NotificationManager.IMPORTANCE_LOW);
        notificationManager.createNotificationChannel(notfChannel);

        notificationManager.notify(1, notification);

        if (!(Settings.canDrawOverlays(this))){
            Intent permIntent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
            startActivityForResult(permIntent, 0);
        }

        Log.v("Hi!","Hi");
        //alarmManager.set(AlarmManager.RTC_WAKEUP, 0, pendingIntent);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, 0, pendingIntent);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, 0, pendingIntent);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, 0, pendingIntent);
        }


    }
    public void stopNotify() {
        alarmManager.cancel(pendingIntent);
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancel(1);

        SharedPreferences prefs = getSharedPreferences("setting",Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        int stop = 0;
        if (prefs.contains("stop"))
            stop = prefs.getInt("stop", 0);
        editor.putInt("stop", stop + 1);
        editor.putString("state","off").apply();
    }

public void delete(int i){
    SharedPreferences prefs=getSharedPreferences("setting",Context.MODE_PRIVATE);

    if (prefs.contains("state")){
        if (prefs.getString("state","off")=="on") stopNotify();
    }
    FilenameFilter imgFilter = new FilenameFilter() {
        public boolean accept(File dir, String name) {
            name = name.toLowerCase();
            boolean bool_=false;
            if ( name.endsWith(".png")){
            bool_=true;
            }
            if (!name.startsWith("predict")){
                bool_=true;
            }
            return bool_;

        }
    };
    File root = new File(Environment.getDataDirectory().getAbsolutePath()+"/data/org.kivy.protectid/files");
    File[] imageFiles = root.listFiles(imgFilter);
    for (File image : imageFiles) {
        if (image.getName().startsWith(Integer.toString(i))) {
            image.delete();
            Log.v("Hie", image.getAbsolutePath());

        }
    }
   new Hardware().train();
}

public boolean Predict (String line) {
    SharedPreferences prefs=getSharedPreferences("setting",Context.MODE_PRIVATE);
    if (prefs.contains("state")){
        if (prefs.getString("state","off")=="on") stopNotify();
    }
    /*int sizeImg1 = 0;
    int sizeImg2 = 0;
    if (prefs.contains("size1")) {
        sizeImg1 = prefs.getInt("size1", 800);
        Log.v("Size", Integer.toString(sizeImg1));
    }
    if (prefs.contains("size2")) {
        sizeImg2 = prefs.getInt("size2", 800);
        Log.v("Size", Integer.toString(sizeImg2));
    }
    opencv_core.Size sizeImg = new opencv_core.Size(sizeImg1, sizeImg2);*/
    faceRecognizer = createLBPHFaceRecognizer(2,10,10,10,20.0);
    faceRecognizer.load(getFilesDir().getAbsolutePath()+"/mymodel.xml");
    opencv_core.RectVector faces;
    IntPointer label;
    DoublePointer confidence;
    int i;

    label = new IntPointer(1);
    confidence = new DoublePointer(1);

    Log.v("Hie","My");
    faces = new opencv_core.RectVector();
    opencv_core.Mat image = imread(getFilesDir().getAbsolutePath()+"/"+line, CV_LOAD_IMAGE_GRAYSCALE);
    Log.v("Hie","My");
    int predicted_label = -1;
    double predicted_confidence = 0.0;
    // Get the prediction and associated confidence from the model
    face_cascade.detectMultiScale(image,faces);
    Boolean bool=false;
    for (i = 0; i < faces.size(); i++) {
        opencv_core.Rect face_i = faces.get(i);
        opencv_core.Mat mat = new opencv_core.Mat(image, face_i);
       // opencv_imgproc.resize(mat, mat, sizeImg);
        imwrite(getFilesDir()+"/app/predict.png", mat);
        faceRecognizer.predict(mat, label, confidence);
        Log.v("My","1");
        //for (int j = 0; j < label.sizeof(); j++) {
        Log.v("Hie", Integer.toString(label.get(0)));
        Log.v("Hie", Double.toString(confidence.get(0)));
        if (confidence.get(0) > 10) bool = true;
        //}
    }
    Log.v("Hie", Integer.toString(label.get(0)));
    Log.v("Hie", Double.toString(confidence.get(0)));
    Log.v("Hie",Boolean.toString(bool));
   // new File(getFilesDir()+"/"+line).delete();
    return bool;

}
    public String[] Images(){
        FilenameFilter imgFilter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                name = name.toLowerCase();
                boolean bool_=false;
                if ( name.endsWith(".png")){
                    bool_=true;
                }
                if (name.startsWith("predict")){
                    bool_=false;
                }
                return bool_;
            }
        };
        File root = new File(Environment.getDataDirectory().getAbsolutePath()+"/data/org.kivy.protectid/files");
        File[] imageFiles = root.listFiles(imgFilter);
        String[] images=new String[imageFiles.length/5];
        Log.v("Hie", Integer.toString(imageFiles.length/5));
        int i=0;
        int j=1;
        for (File image : imageFiles) {

            if (image.getName().startsWith(Integer.toString(j))) {
                Log.v("Hie", image.getAbsolutePath());
                images[j-1] = image.getAbsolutePath();

                j=j+1;
            }
            Log.v("Hie1", Integer.toString(i));
            i = i + 1;
        }
        Log.v("Hie", Integer.toString(imageFiles.length/5));
        return images;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Log.v(TAG, "PythonActivity onCreate running");
        resourceManager = new ResourceManager(this);

        Log.v(TAG, "About to do super onCreate");
        super.onCreate(savedInstanceState);
        Log.v(TAG, "Did super onCreate");

        this.mActivity = this;
        this.showLoadingScreen();

        new UnpackFilesTask().execute(getAppRoot());

        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        Intent alarmIntent = new Intent(this, MyReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(this, 0, alarmIntent, PendingIntent.FLAG_CANCEL_CURRENT);



        faceRecognizer = createFisherFaceRecognizer();
        Log.v("Hie","My");

        face_cascade = new opencv_objdetect.CascadeClassifier(
                getFilesDir().getAbsolutePath()+"/app/lbpcascade_frontalface_improved.xml");
    }

    public void loadLibraries() {
        String app_root = new String(getAppRoot());
        File app_root_file = new File(app_root);
        PythonUtil.loadLibraries(app_root_file,
            new File(getApplicationInfo().nativeLibraryDir));
    }

    /**
     * Show an error using a toast. (Only makes sense from non-UI
     * threads.)
     */
    public void toastError(final String msg) {

        final Activity thisActivity = this;

        runOnUiThread(new Runnable () {
            public void run() {
                Toast.makeText(thisActivity, msg, Toast.LENGTH_LONG).show();
            }
        });

        // Wait to show the error.
        synchronized (this) {
            try {
                this.wait(1000);
            } catch (InterruptedException e) {
            }
        }
    }

    private class UnpackFilesTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            File app_root_file = new File(params[0]);
            Log.v(TAG, "Ready to unpack");
            PythonActivityUtil pythonActivityUtil = new PythonActivityUtil(mActivity, resourceManager);
            pythonActivityUtil.unpackData("private", app_root_file);
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            // Figure out the directory where the game is. If the game was
            // given to us via an intent, then we use the scheme-specific
            // part of that intent to determine the file to launch. We
            // also use the android.txt file to determine the orientation.
            //
            // Otherwise, we use the public data, if we have it, or the
            // private data if we do not.
            mActivity.finishLoad();

            // finishLoad called setContentView with the SDL view, which
            // removed the loading screen. However, we still need it to
            // show until the app is ready to render, so pop it back up
            // on top of the SDL view.
            mActivity.showLoadingScreen();

            String app_root_dir = getAppRoot();
            if (getIntent() != null && getIntent().getAction() != null &&
                    getIntent().getAction().equals("org.kivy.LAUNCH")) {
                File path = new File(getIntent().getData().getSchemeSpecificPart());

                Project p = Project.scanDirectory(path);
                String entry_point = getEntryPoint(p.dir);
                SDLActivity.nativeSetenv("ANDROID_ENTRYPOINT", p.dir + "/" + entry_point);
                SDLActivity.nativeSetenv("ANDROID_ARGUMENT", p.dir);
                SDLActivity.nativeSetenv("ANDROID_APP_PATH", p.dir);

                if (p != null) {
                    if (p.landscape) {
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                    } else {
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                    }
                }

                // Let old apps know they started.
                try {
                    FileWriter f = new FileWriter(new File(path, ".launch"));
                    f.write("started");
                    f.close();
                } catch (IOException e) {
                    // pass
                }
            } else {
                String entry_point = getEntryPoint(app_root_dir);
                SDLActivity.nativeSetenv("ANDROID_ENTRYPOINT", entry_point);
                SDLActivity.nativeSetenv("ANDROID_ARGUMENT", app_root_dir);
                SDLActivity.nativeSetenv("ANDROID_APP_PATH", app_root_dir);
            }

            String mFilesDirectory = mActivity.getFilesDir().getAbsolutePath();
            Log.v(TAG, "Setting env vars for start.c and Python to use");
            SDLActivity.nativeSetenv("ANDROID_PRIVATE", mFilesDirectory);
            SDLActivity.nativeSetenv("ANDROID_UNPACK", app_root_dir);
            SDLActivity.nativeSetenv("PYTHONHOME", app_root_dir);
            SDLActivity.nativeSetenv("PYTHONPATH", app_root_dir + ":" + app_root_dir + "/lib");
            SDLActivity.nativeSetenv("PYTHONOPTIMIZE", "2");

            try {
                Log.v(TAG, "Access to our meta-data...");
                mActivity.mMetaData = mActivity.getPackageManager().getApplicationInfo(
                        mActivity.getPackageName(), PackageManager.GET_META_DATA).metaData;

                PowerManager pm = (PowerManager) mActivity.getSystemService(Context.POWER_SERVICE);
                if ( mActivity.mMetaData.getInt("wakelock") == 1 ) {
                    mActivity.mWakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "Screen On");
                    mActivity.mWakeLock.acquire();
                }
                if ( mActivity.mMetaData.getInt("surface.transparent") != 0 ) {
                    Log.v(TAG, "Surface will be transparent.");
                    getSurface().setZOrderOnTop(true);
                    getSurface().getHolder().setFormat(PixelFormat.TRANSPARENT);
                } else {
                    Log.i(TAG, "Surface will NOT be transparent");
                }
            } catch (PackageManager.NameNotFoundException e) {
            }

            // Launch app if that hasn't been done yet:
            if (mActivity.mHasFocus && (
                    // never went into proper resume state:
                    mActivity.mCurrentNativeState == NativeState.INIT ||
                    (
                    // resumed earlier but wasn't ready yet
                    mActivity.mCurrentNativeState == NativeState.RESUMED &&
                    mActivity.mSDLThread == null
                    ))) {
                // Because sometimes the app will get stuck here and never
                // actually run, ensure that it gets launched if we're active:
                mActivity.onResume();
            }
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }
    }

    public static SurfaceView getSurface() {
        return   mSurface;
    }

    public static ViewGroup getLayout() {
        return   mLayout;
    }

    //----------------------------------------------------------------------------
    // Listener interface for onNewIntent
    //

    public interface NewIntentListener {
        void onNewIntent(Intent intent);
    }

    private List<NewIntentListener> newIntentListeners = null;

    public void registerNewIntentListener(NewIntentListener listener) {
        if ( this.newIntentListeners == null )
            this.newIntentListeners = Collections.synchronizedList(new ArrayList<NewIntentListener>());
        this.newIntentListeners.add(listener);
    }

    public void unregisterNewIntentListener(NewIntentListener listener) {
        if ( this.newIntentListeners == null )
            return;
        this.newIntentListeners.remove(listener);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if ( this.newIntentListeners == null )
            return;
        this.onResume();
        synchronized ( this.newIntentListeners ) {
            Iterator<NewIntentListener> iterator = this.newIntentListeners.iterator();
            while ( iterator.hasNext() ) {
                (iterator.next()).onNewIntent(intent);
            }
        }
    }

    //----------------------------------------------------------------------------
    // Listener interface for onActivityResult
    //

    public interface ActivityResultListener {
        void onActivityResult(int requestCode, int resultCode, Intent data);
    }

    private List<ActivityResultListener> activityResultListeners = null;

    public void registerActivityResultListener(ActivityResultListener listener) {
        if ( this.activityResultListeners == null )
            this.activityResultListeners = Collections.synchronizedList(new ArrayList<ActivityResultListener>());
        this.activityResultListeners.add(listener);
    }

    public void unregisterActivityResultListener(ActivityResultListener listener) {
        if ( this.activityResultListeners == null )
            return;
        this.activityResultListeners.remove(listener);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if ( this.activityResultListeners == null )
            return;
        this.onResume();
        synchronized ( this.activityResultListeners ) {
            Iterator<ActivityResultListener> iterator = this.activityResultListeners.iterator();
            while ( iterator.hasNext() )
                (iterator.next()).onActivityResult(requestCode, resultCode, intent);
        }
    }

    public static void start_service(
            String serviceTitle,
            String serviceDescription,
            String pythonServiceArgument
            ) {
        _do_start_service(
            serviceTitle, serviceDescription, pythonServiceArgument, true
        );
    }

    public static void start_service_not_as_foreground(
            String serviceTitle,
            String serviceDescription,
            String pythonServiceArgument
            ) {
        _do_start_service(
            serviceTitle, serviceDescription, pythonServiceArgument, false
        );
    }

    public static void _do_start_service(
            String serviceTitle,
            String serviceDescription,
            String pythonServiceArgument,
            boolean showForegroundNotification
            ) {
        Intent serviceIntent = new Intent(PythonActivity.mActivity, PythonService.class);
        String argument = PythonActivity.mActivity.getFilesDir().getAbsolutePath();
        String app_root_dir = PythonActivity.mActivity.getAppRoot();
        String entry_point = PythonActivity.mActivity.getEntryPoint(app_root_dir + "/service");
        serviceIntent.putExtra("androidPrivate", argument);
        serviceIntent.putExtra("androidArgument", app_root_dir);
        serviceIntent.putExtra("serviceEntrypoint", "service/" + entry_point);
        serviceIntent.putExtra("pythonName", "python");
        serviceIntent.putExtra("pythonHome", app_root_dir);
        serviceIntent.putExtra("pythonPath", app_root_dir + ":" + app_root_dir + "/lib");
        serviceIntent.putExtra("serviceStartAsForeground",
            (showForegroundNotification ? "true" : "false")
        );
        serviceIntent.putExtra("serviceTitle", serviceTitle);
        serviceIntent.putExtra("serviceDescription", serviceDescription);
        serviceIntent.putExtra("pythonServiceArgument", pythonServiceArgument);
        PythonActivity.mActivity.startService(serviceIntent);
    }

    public static void stop_service() {
        Intent serviceIntent = new Intent(PythonActivity.mActivity, PythonService.class);
        PythonActivity.mActivity.stopService(serviceIntent);
    }

    /** Loading screen view **/
    public static ImageView mImageView = null;
    /** Whether main routine/actual app has started yet **/
    protected boolean mAppConfirmedActive = false;
    /** Timer for delayed loading screen removal. **/
    protected Timer loadingScreenRemovalTimer = null; 

    // Overridden since it's called often, to check whether to remove the
    // loading screen:
    @Override
    protected boolean sendCommand(int command, Object data) {
        boolean result = super.sendCommand(command, data);
        considerLoadingScreenRemoval();
        return result;
    }
   
    /** Confirm that the app's main routine has been launched.
     **/
    @Override
    public void appConfirmedActive() {
        if (!mAppConfirmedActive) {
            Log.v(TAG, "appConfirmedActive() -> preparing loading screen removal");
            mAppConfirmedActive = true;
            considerLoadingScreenRemoval();
        }
    }

    /** This is called from various places to check whether the app's main
     *  routine has been launched already, and if it has, then the loading
     *  screen will be removed.
     **/
    public void considerLoadingScreenRemoval() {
        if (loadingScreenRemovalTimer != null)
            return;
        runOnUiThread(new Runnable() {
            public void run() {
                if (((PythonActivity)PythonActivity.mSingleton).mAppConfirmedActive &&
                        loadingScreenRemovalTimer == null) {
                    // Remove loading screen but with a delay.
                    // (app can use p4a's android.loadingscreen module to
                    // do it quicker if it wants to)
                    // get a handler (call from main thread)
                    // this will run when timer elapses
                    TimerTask removalTask = new TimerTask() {
                        @Override
                        public void run() {
                            // post a runnable to the handler
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    PythonActivity activity =
                                        ((PythonActivity)PythonActivity.mSingleton);
                                    if (activity != null)
                                        activity.removeLoadingScreen();
                                }
                            });
                        }
                    };
                    loadingScreenRemovalTimer = new Timer();
                    loadingScreenRemovalTimer.schedule(removalTask, 5000);
                }
            }
        });
    }

    public void removeLoadingScreen() {
        runOnUiThread(new Runnable() {
            public void run() {
                if (PythonActivity.mImageView != null && 
                        PythonActivity.mImageView.getParent() != null) {
                    ((ViewGroup)PythonActivity.mImageView.getParent()).removeView(
                        PythonActivity.mImageView);
                    PythonActivity.mImageView = null;
                }
            }
        });
    }

    public String getEntryPoint(String search_dir) {
        /* Get the main file (.pyc|.pyo|.py) depending on if we
         * have a compiled version or not.
        */
        List<String> entryPoints = new ArrayList<String>();
        entryPoints.add("main.pyo");  // python 2 compiled files
        entryPoints.add("main.pyc");  // python 3 compiled files
		for (String value : entryPoints) {
            File mainFile = new File(search_dir + "/" + value);
            if (mainFile.exists()) {
                return value;
            }
        }
        return "main.py";
    }

    protected void showLoadingScreen() {
        // load the bitmap
        // 1. if the image is valid and we don't have layout yet, assign this bitmap
        // as main view.
        // 2. if we have a layout, just set it in the layout.
        // 3. If we have an mImageView already, then do nothing because it will have
        // already been made the content view or added to the layout.

        if (mImageView == null) {
            int presplashId = this.resourceManager.getIdentifier("presplash", "drawable");
            InputStream is = this.getResources().openRawResource(presplashId);
            Bitmap bitmap = null;
            try {
                bitmap = BitmapFactory.decodeStream(is);
            } finally {
                try {
                    is.close();
                } catch (IOException e) {};
            }

            mImageView = new ImageView(this);
            mImageView.setImageBitmap(bitmap);

            /*
             * Set the presplash loading screen background color
             * https://developer.android.com/reference/android/graphics/Color.html
             * Parse the color string, and return the corresponding color-int.
             * If the string cannot be parsed, throws an IllegalArgumentException exception.
             * Supported formats are: #RRGGBB #AARRGGBB or one of the following names:
             * 'red', 'blue', 'green', 'black', 'white', 'gray', 'cyan', 'magenta', 'yellow',
             * 'lightgray', 'darkgray', 'grey', 'lightgrey', 'darkgrey', 'aqua', 'fuchsia',
             * 'lime', 'maroon', 'navy', 'olive', 'purple', 'silver', 'teal'.
             */
            String backgroundColor = resourceManager.getString("presplash_color");
            if (backgroundColor != null) {
                try {
                    mImageView.setBackgroundColor(Color.parseColor(backgroundColor));
                } catch (IllegalArgumentException e) {}
            }   
            mImageView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.FILL_PARENT,
                ViewGroup.LayoutParams.FILL_PARENT));
            mImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        }

        try {
            if (mLayout == null) {
                setContentView(mImageView);
            } else if (PythonActivity.mImageView.getParent() == null) {
                mLayout.addView(mImageView);
            }
        } catch (IllegalStateException e) {
            // The loading screen can be attempted to be applied twice if app
            // is tabbed in/out, quickly.
            // (Gives error "The specified child already has a parent.
            // You must call removeView() on the child's parent first.")
        }
    }
    
    @Override
    protected void onPause() {
        if (this.mWakeLock != null && mWakeLock.isHeld()) {
            this.mWakeLock.release();
        }

        //NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        //notificationManager.cancel(1);
        Log.v(TAG, "onPause()");
        try {
            super.onPause();
        } catch (UnsatisfiedLinkError e) {
            // Catch pause while still in loading screen failing to
            // call native function (since it's not yet loaded)
        }
    }

    @Override
    protected void onResume() {
        if (this.mWakeLock != null) {
            this.mWakeLock.acquire();
        }
        Log.v(TAG, "onResume()");
        try {
            super.onResume();
        } catch (UnsatisfiedLinkError e) {
            // Catch resume while still in loading screen failing to
            // call native function (since it's not yet loaded)
        }
        considerLoadingScreenRemoval();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        try {
            super.onWindowFocusChanged(hasFocus);
        } catch (UnsatisfiedLinkError e) {
            // Catch window focus while still in loading screen failing to
            // call native function (since it's not yet loaded)
        }
        considerLoadingScreenRemoval();
    }

    /**
     * Used by android.permissions p4a module to register a call back after
     * requesting runtime permissions
     **/
    public interface PermissionsCallback {
        void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults);
    }

    private PermissionsCallback permissionCallback;
    private boolean havePermissionsCallback = false;

    public void addPermissionsCallback(PermissionsCallback callback) {
        permissionCallback = callback;
        havePermissionsCallback = true;
        Log.v(TAG, "addPermissionsCallback(): Added callback for onRequestPermissionsResult");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        Log.v(TAG, "onRequestPermissionsResult()");
        if (havePermissionsCallback) {
            Log.v(TAG, "onRequestPermissionsResult passed to callback");
            permissionCallback.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    /**
     * Used by android.permissions p4a module to check a permission
     **/
    public boolean checkCurrentPermission(String permission) {
        if (android.os.Build.VERSION.SDK_INT < 23)
            return true;

        try {
            java.lang.reflect.Method methodCheckPermission =
                Activity.class.getMethod("checkSelfPermission", String.class);
            Object resultObj = methodCheckPermission.invoke(this, permission);
            int result = Integer.parseInt(resultObj.toString());
            if (result == PackageManager.PERMISSION_GRANTED) 
                return true;
        } catch (IllegalAccessException | NoSuchMethodException |
                 InvocationTargetException e) {
        }
        return false;
    }

    /**
     * Used by android.permissions p4a module to request runtime permissions
     **/
    public void requestPermissionsWithRequestCode(String[] permissions, int requestCode) {
        if (android.os.Build.VERSION.SDK_INT < 23)
            return;
        try {
            java.lang.reflect.Method methodRequestPermission =
                Activity.class.getMethod("requestPermissions",
                String[].class, int.class);
            methodRequestPermission.invoke(this, permissions, requestCode);
        } catch (IllegalAccessException | NoSuchMethodException |
                 InvocationTargetException e) {
        }
    }

    public void requestPermissions(String[] permissions) {
        requestPermissionsWithRequestCode(permissions, 1);
    }
}
