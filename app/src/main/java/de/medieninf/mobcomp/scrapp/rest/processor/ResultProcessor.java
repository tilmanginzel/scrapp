package de.medieninf.mobcomp.scrapp.rest.processor;

import android.content.Context;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.net.NetworkInterface;
import java.util.List;

import de.medieninf.mobcomp.scrapp.R;
import de.medieninf.mobcomp.scrapp.database.DBHelper;
import de.medieninf.mobcomp.scrapp.database.Database;
import de.medieninf.mobcomp.scrapp.notification.NotificationHelper;
import de.medieninf.mobcomp.scrapp.rest.model.ActionParam;
import de.medieninf.mobcomp.scrapp.scraping.Scraper;
import de.medieninf.mobcomp.scrapp.rest.model.Action;
import de.medieninf.mobcomp.scrapp.rest.model.Result;
import de.medieninf.mobcomp.scrapp.util.RequestState;
import de.medieninf.mobcomp.scrapp.util.exceptions.ParseException;
import retrofit.RetrofitError;

/**
 * This processor is responsible for result resources.
 * It makes rest calls and required inserts / updates to the local database.
 *
 * Additionally it processes all parsing actions_subscription for a given rule.
 */
public class ResultProcessor extends RestProcessor {
    private static final String TAG = ResultProcessor.class.getSimpleName();

    public ResultProcessor(Context context) {
        super(context);
    }

    public void createResult(int ruleId, boolean automaticScrape) {
        // test wifi required and wifi connection if automaticScrape
        if(automaticScrape && !checkWifi(ruleId)){
            return;
        }

        List<Action> actions = dbHelper.getActionsForParsing(ruleId);

        // check if all required action params are filled
        if (!checkRequiredActionParams(actions)) {
            if (!automaticScrape) {
                final String text = context.getResources().getString(R.string.required_param_missing);
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
                    }
                });

                // notify rule loader
                context.getContentResolver().notifyChange(DBHelper.RESULT_URI, null);
            }
            return;
        }

        // insert (empty) result into database
        long internalResultId = dbHelper.createResult(ruleId, actions.size(), RequestState.PENDING, automaticScrape);

        // 1. Parse Website Data
        Scraper scraper = new Scraper(dbHelper, actions);
        Result result;
        try {
            result = scraper.scrape(internalResultId);
        } catch (ParseException | IOException e1) {
            Log.e(TAG, e1.getMessage(), e1);
            // if there is an error during parsing
            dbHelper.updateResult(new Result(), internalResultId, RequestState.ERROR);
            return;
        } catch (Exception e2) {
            Log.v(TAG, "catched Exception!! " + e2.getMessage(), e2);
            dbHelper.updateResult(new Result(), internalResultId, RequestState.ERROR);
            return;
        }

        // Copy result and set Content to null -> Content isn't sent to server!
        Result restResult = new Result(result);
        restResult.setContent(null);

        // 2. make rest call
        try {
            restResult = restClient.getApiService().createResult(ruleId, restResult);
        } catch(RetrofitError e) {
            Log.e(TAG, "RetrofitError: "+ e.getMessage());
        }

        // 3. update content provider
        if(restResult != null && restResult.getResultId() != 0) {
            dbHelper.updateResult(result, internalResultId, RequestState.DONE);
        } else {
            dbHelper.updateResult(new Result(), internalResultId, RequestState.ERROR);
        }

        // check if result is new and set flag
        boolean isNew = dbHelper.isNewResult(ruleId);
        if (isNew) {
            dbHelper.setResultAsNew(internalResultId);
        }

        // display notification for myself if scraping triggered by alarm and result is new
        if(automaticScrape && isNew) {
            NotificationHelper notificationHelper = new NotificationHelper(context);
            notificationHelper.showNotificationForNewResult(ruleId, dbHelper.getRuleTitle(ruleId));
        }
    }

    private boolean checkWifi(int ruleId){
        ConnectivityManager connMgr  = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        Cursor cursor = dbHelper.getSubscription(ruleId);
        if(cursor.moveToFirst()){
            boolean wifiOnly = cursor.getInt(cursor.getColumnIndex(Database.Rule.WIFI_ONLY)) == 1;

            if(wifiOnly && !wifiInfo.isConnected()){
                return false;
            }
        }
        return true;
    }

    /**
     * Check if all requird action params are filled.
     *
     * @param actions - list of actions
     * @return true if all required fields are filled
     */
    private boolean checkRequiredActionParams(List<Action> actions) {
        for (Action action : actions) {
            if (action.getActionParams() != null) {
                for (ActionParam param : action.getActionParams()) {
                    if (param.getRequired() && (param.getValue() == null || param.getValue().length() == 0)) {
                        return false;
                    }
                }
            }
        }

        return true;
    }
}
