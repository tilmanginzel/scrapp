package de.medieninf.mobcomp.scrapp.util.alarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;

import de.medieninf.mobcomp.scrapp.rest.service.RestService;
import de.medieninf.mobcomp.scrapp.rest.service.RestServiceHelper;

/**
 * Receiver for alarms from context AlarmManager. CAUTION: Operates on main thread!
 */
public class AlarmReceiver extends BroadcastReceiver {
    private static final String TAG = AlarmReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "YOUR TAG");

        // acquire the lock
        wl.acquire();

        // get rule data from intent
        Bundle data = intent.getExtras();
        int ruleId = data.getInt(RestService.EXTRA_RULE_ID, -1);

        Log.v(TAG, "Alarm received with ruleId: " + ruleId);

        if(ruleId != -1) {
            // scrape (not on main thread!)
            RestServiceHelper restServiceHelper = new RestServiceHelper(context);
            restServiceHelper.createResult(ruleId, true);
        }

        //Release the lock
        wl.release();
    }
}
