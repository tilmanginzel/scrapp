package de.medieninf.mobcomp.scrapp.rest.processor;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import java.util.Date;

import de.medieninf.mobcomp.scrapp.database.Database;
import de.medieninf.mobcomp.scrapp.rest.model.Rule;
import de.medieninf.mobcomp.scrapp.rest.model.Subscription;
import de.medieninf.mobcomp.scrapp.util.RequestState;
import de.medieninf.mobcomp.scrapp.util.ResultCode;
import retrofit.RetrofitError;

/**
 * This processor is responsible for subscription resources.
 * It makes rest calls and required inserts / updates to the local database.
 */
public class SubscriptionProcessor extends RestProcessor {
    private static final String TAG = SubscriptionProcessor.class.getSimpleName();

    public SubscriptionProcessor(Context context) {
        super(context);
    }

    /**
     * Makes a rest call to create a subscription and stores the rule in the client database.
     *
     * @param ruleId The ruleId to subscribe.
     */
    public void createSubscription(int ruleId) {
        if(isOnline()) {
            Log.e(TAG, "No internet connection available.");
        }

        // create subscription
        dbHelper.createSubscription(ruleId, RequestState.PENDING);
        Cursor cursor = dbHelper.getRule(ruleId);

        Subscription sb = new Subscription();
        if (cursor.moveToFirst()) {
            // subscription object for server
            sb.setInterval(cursor.getInt(cursor.getColumnIndex(Database.Rule.INTERVAL)));

            Date stTime = new Date();
            stTime.setTime(cursor.getLong(cursor.getColumnIndex(Database.Rule.START_TIME)));
            sb.setStartTime(stTime);
        }
        Log.v(TAG, sb.toString());

        Rule subscription = null;
        // make synchronous call and get rule
        try {
            subscription = restClient.getApiService().createSubscription(ruleId, sb);
            // update subscription
            Log.v(TAG, sb.getStartTime() + "");
            dbHelper.updateSubscription(subscription, ruleId, true, sb.getStartTime(), sb.getInterval(), RequestState.DONE);

            // create the alarm to scrape
            if(sb.getInterval() == 0){
                scrapeAlarmMgr.cancelAlarm(ruleId);
            } else if(sb.getInterval() > 0) {
                scrapeAlarmMgr.setAlarm(sb.getInterval(), sb.getStartTime(), ruleId);
            }
        } catch (RetrofitError e) {
            Log.e(TAG, "RetrofitError: "+ e.getMessage());

            // update subscription
            dbHelper.updateSubscription(subscription, ruleId, false, new Date(), -1, RequestState.ERROR);
        }
    }

    /**
     * Makes a rest call to update a subscription on server.
     *
     * @param ruleId The related ruleId of subscription to update.
     */
    public void updateSubscription(int ruleId) {
        if(isOnline()) {
            Log.e(TAG, "No internet connection available.");
        }

        // getRule geht immer davon aus, dass noch nicht subscribed wurde
        Cursor cursor = dbHelper.getSubscription(ruleId);

        Subscription sb = new Subscription();
        if (cursor.moveToFirst()) {
            // subscription object for server
            sb.setInterval(cursor.getInt(cursor.getColumnIndex(Database.Rule.INTERVAL)));

            Date stTime = new Date();
            stTime.setTime(cursor.getLong(cursor.getColumnIndex(Database.Rule.START_TIME)));
            sb.setStartTime(stTime);
        }
        Log.v(TAG, sb.toString() + " " + sb.getStartTime() + " " + sb.getInterval());

        // make synchronous call and get rule
        try {
            restClient.getApiService().updateSubscription(ruleId, sb);
            // TODO: auf Fehler reagieren - was passiert dann?

            if (sb.getInterval() == 0){
                scrapeAlarmMgr.cancelAlarm(ruleId);
            } else if(sb.getInterval() > 0) {
                scrapeAlarmMgr.updateAlarm(sb.getInterval(), sb.getStartTime(), ruleId);
            }
        } catch (RetrofitError e) {
            Log.e(TAG, "RetrofitError: "+ e.getMessage());
        }
    }

    /**
     * Deletes a subscription on the server and client.
     *
     * @param ruleId The ruleId to unsubscribe.
     */
    public int deleteSubscription(int ruleId) {
        if(isOnline()) {
            Log.e(TAG, "No internet connection available.");
            return ResultCode.ERROR;
        }

        // mark as deleted
        dbHelper.markDeleteSubscription(ruleId);

        // make synchronous rest call
        restClient.getApiService().deleteSubscription(ruleId);

        // delete subscription and results
        dbHelper.deleteSubscription(ruleId);

        // delete alarm
        scrapeAlarmMgr.cancelAlarm(ruleId);

        return ResultCode.OK;
    }
}
