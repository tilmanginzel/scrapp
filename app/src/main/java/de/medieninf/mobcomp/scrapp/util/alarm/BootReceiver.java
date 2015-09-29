package de.medieninf.mobcomp.scrapp.util.alarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Receiver for broadcast after rebooting system.
 */
public class BootReceiver extends BroadcastReceiver{

    private static final String TAG = BootReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            // start intent for AlarmService to recreate all alarms
            Log.v(TAG, "Received BOOT_COMPLETED intent");
            Intent alarmIntent = new Intent(context, AlarmService.class);
            context.startService(alarmIntent);
        }
    }
}
