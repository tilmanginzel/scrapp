package de.medieninf.mobcomp.scrapp.rest.processor;

import android.content.ContentResolver;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import de.medieninf.mobcomp.scrapp.database.DBHelper;
import de.medieninf.mobcomp.scrapp.rest.RestClient;
import de.medieninf.mobcomp.scrapp.util.alarm.ScrapeAlarmManager;

/**
 * Base class for all processors to hold relevant objects.
 */
class RestProcessor {
    Context context;
    RestClient restClient;
    private ContentResolver contentResolver;
    DBHelper dbHelper;
    ScrapeAlarmManager scrapeAlarmMgr;

    RestProcessor(Context context) {
        this.context = context;
        this.restClient = new RestClient(context);
        this.contentResolver = context.getContentResolver();
        this.dbHelper = new DBHelper(context);
        this.scrapeAlarmMgr = new ScrapeAlarmManager(context);
    }

    /**
     * Checks whether a Network interface is available and a connection is possible.
     * @return boolean
     */
    boolean isOnline() {
        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo == null || !networkInfo.isConnected());
    }
}
