package de.medieninf.mobcomp.scrapp.util.alarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.Calendar;
import java.util.Date;

import de.medieninf.mobcomp.scrapp.rest.service.RestService;

/**
 * Manages all alarm operations to repeat scraping automatically.
 */
public class ScrapeAlarmManager {
    private static final String TAG = ScrapeAlarmManager.class.getSimpleName();

    private AlarmManager alarmMgr;
    private Context context;

    /**
     * Constructor, set the context and get the system alarm manager.
     * @param context context in which it is instantiated
     */
    public ScrapeAlarmManager(Context context){
        this.context = context;
        alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    }

    /**
     * Set a alarm for related rule id.
     * @param interval repeat interval
     * @param time start timestamp
     * @param ruleId related rule
     */
    public void setAlarm(int interval, Date time, int ruleId) {
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra(RestService.EXTRA_RULE_ID, ruleId);

        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, ruleId, intent,
                PendingIntent.FLAG_CANCEL_CURRENT);

        // set the alarm to start at start time
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.setTime(time);

        // set the special interval
        alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                1000 * 60 * interval, alarmIntent);
        Log.v(TAG, "Alarm set for ruleId " + ruleId + " with interval " + interval + " starting on " + time);
    }

    /**
     * Update a alarm by setting it with the same parameters as the created alarm before.
     * Original alarm will be overwritten.
     * @param interval repeat interval
     * @param time start timestamp
     * @param ruleId related rule
     */
    public void updateAlarm(int interval, Date time, int ruleId){
        Log.v(TAG, "Alarm update for ruleId " + ruleId + " with interval " + interval + " starting on " + time);
        setAlarm(interval, time, ruleId);
    }

    public void cancelAlarm(int ruleId) {
        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, ruleId, intent,
                PendingIntent.FLAG_CANCEL_CURRENT);
        alarmMgr.cancel(sender);
    }
}