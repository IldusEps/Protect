package org.kivy.android;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class MyReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.v("Hi!!","Hi");
            Intent serviceIntent = new Intent(context, MyService.class);
            //context.startForegroundService(serviceIntent);
            context.startService(serviceIntent);
    }
}
