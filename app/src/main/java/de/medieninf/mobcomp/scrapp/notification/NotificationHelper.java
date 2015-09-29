package de.medieninf.mobcomp.scrapp.notification;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

import de.medieninf.mobcomp.scrapp.R;
import de.medieninf.mobcomp.scrapp.view.SubscriptionActivity;

/**
 * Helper class to create notifications.
 */
public class NotificationHelper {
    private Context context;

    public NotificationHelper(Context context) {
        this.context = context.getApplicationContext();
    }

    /**
     * Create and show a simple notification containing the received GCM message.
     *
     * @param ruleId - rule id
     * @param ruleTitle - rule title
     */
    public void showNotificationForNewResult(int ruleId, String ruleTitle) {
        Intent intent = new Intent(context, SubscriptionActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(SubscriptionActivity.EXTRA_RULE_ID, ruleId);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.scrapp_s_white_32dp)
                .setContentTitle(context.getResources().getString(R.string.result_notification_title))
                .setContentText(ruleTitle)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(ruleId, notificationBuilder.build());
    }
}
