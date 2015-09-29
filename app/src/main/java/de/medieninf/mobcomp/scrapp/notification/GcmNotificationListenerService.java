package de.medieninf.mobcomp.scrapp.notification;

import android.os.Bundle;

import com.google.android.gms.gcm.GcmListenerService;

import de.medieninf.mobcomp.scrapp.database.DBHelper;
import de.medieninf.mobcomp.scrapp.view.SubscriptionActivity;

/**
 * Service to receive messages sent from a GCM-Server.
 */
public class GcmNotificationListenerService extends GcmListenerService {

    @Override
    public void onMessageReceived(String from, Bundle data) {
        int ruleId = -1;
        String title = null;

        // get rule id from gcm data and title from database
        try {
            ruleId = Integer.parseInt(data.getString(SubscriptionActivity.EXTRA_RULE_ID, "-1"));
            DBHelper dbHelper = new DBHelper(this);
            title = dbHelper.getRuleTitle(ruleId);
        } catch (NumberFormatException e) {
            // do nothing
        }

        // send notification
        if (ruleId > 0 && title != null) {
            NotificationHelper notificationHelper = new NotificationHelper(this);
            notificationHelper.showNotificationForNewResult(ruleId, title);
        }
    }
}
