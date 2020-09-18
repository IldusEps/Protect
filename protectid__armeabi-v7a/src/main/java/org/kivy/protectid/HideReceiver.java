package org.kivy.protectid;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.kivy.android.HideService;
import org.kivy.android.MyService;

public class HideReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        Intent serviceIntent = new Intent(context, HideService.class);
        //context.startForegroundService(serviceIntent);
        context.startService(serviceIntent);
        throw new UnsupportedOperationException("Not yet implemented");

    }
}
