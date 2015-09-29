package de.medieninf.mobcomp.scrapp.util;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

import java.io.IOException;

import de.medieninf.mobcomp.scrapp.database.DBHelper;
import de.medieninf.mobcomp.scrapp.scraping.Scraper;
import de.medieninf.mobcomp.scrapp.util.exceptions.ParseException;
import de.medieninf.mobcomp.scrapp.view.SubscriptionActivity;
import de.medieninf.mobcomp.scrapp.view.WebViewActivity;

/**
 * IntentService for network operations while starting full screen web view.
 */
public class WebService extends IntentService {
    private static final String TAG = WebService.class.getSimpleName();

    private ResultReceiver receiver;
    private DBHelper dbHelper;

    /**
     * Creates an IntentService. Invoked by your subclass's constructor.
     */
    public WebService() {
        super(TAG);
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);

        // init dbHelper and receiver
        dbHelper = new DBHelper(this);
        receiver = intent.getParcelableExtra(WebViewActivity.EXTRA_RECEIVER);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // get ruleId from extras
        int ruleId = intent.getIntExtra(SubscriptionActivity.EXTRA_RULE_ID, -1);
        if (ruleId != -1) {
            Scraper scraper = new Scraper(dbHelper.getActionsForParsing(ruleId));

            // scrape to get url
            String url;
            try {
                url = scraper.scrapeForBrowser();
            } catch (IOException | ParseException e) {
                Log.e(TAG, "", e);
                // send fail message to activity
                receiver.send(-1, null);
                return;
            } catch (Exception e) {
                // send fail message to activity
                receiver.send(-1, null);
                return;
            }

            // fill bundle with return data for activity
            Bundle data = new Bundle();
            data.putString(WebViewActivity.EXTRA_URL, url);
            data.putSerializable(WebViewActivity.EXTRA_COOKIES_KEYS, scraper.getCookies());

            // send data to activity
            receiver.send(1, data);
        } else {
            // send fail message to activity
            receiver.send(-1, null);
        }

    }
}