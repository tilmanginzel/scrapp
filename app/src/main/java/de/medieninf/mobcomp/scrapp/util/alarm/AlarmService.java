package de.medieninf.mobcomp.scrapp.util.alarm;

import android.app.IntentService;
import android.content.Intent;
import android.database.Cursor;
import android.support.v4.content.Loader;

import java.util.Date;

import de.medieninf.mobcomp.scrapp.database.DBHelper;
import de.medieninf.mobcomp.scrapp.database.Database;

/**
 * A intent service to recreate alarms
 */
public class AlarmService extends IntentService {

    private static final String TAG = AlarmService.class.getSimpleName();

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     */
    public AlarmService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        ScrapeAlarmManager scrappAlarmMgr = new ScrapeAlarmManager(this);
        DBHelper dbHelper = new DBHelper(this);
        Cursor cursor = dbHelper.getSubscriptions();
        while(cursor.moveToNext()){
            long startTime = cursor.getLong(cursor.getColumnIndex(Database.Rule.START_TIME));
            Date timeDate = new Date();
            timeDate.setTime(startTime);

            int interval = cursor.getInt(cursor.getColumnIndex(Database.Rule.INTERVAL));
            int ruleId = cursor.getInt(cursor.getColumnIndex(Database.Rule.RULE_ID));

            scrappAlarmMgr.setAlarm(interval, timeDate, ruleId);
        }
    }
}
