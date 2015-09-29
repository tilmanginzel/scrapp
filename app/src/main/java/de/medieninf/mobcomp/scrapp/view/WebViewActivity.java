package de.medieninf.mobcomp.scrapp.view;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

import de.medieninf.mobcomp.scrapp.R;
import de.medieninf.mobcomp.scrapp.util.Config;
import de.medieninf.mobcomp.scrapp.util.WebService;
import de.medieninf.mobcomp.scrapp.util.WebResultReceiver;

/**
 * Activity to show the website of newest result in full screen mode.
 */
public class WebViewActivity extends AppCompatActivity implements WebResultReceiver.Receiver {

    private static final String TAG = WebViewActivity.class.getSimpleName();

    public static final String EXTRA_RECEIVER = "receiver";
    public static final String EXTRA_URL = "url";
    public static final String EXTRA_COOKIES_KEYS = "cookieKeys";

    private WebView fullView;
    private CookieManager cookieManager;
    private WebResultReceiver webResultReceiver;

    private ActionBar supportedBar;

    private ProgressDialog progressDialog;

    @Override
    @SuppressWarnings("deprecation")
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.webview_fullscreen);

        // init toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            supportedBar = getSupportActionBar();
            if(supportedBar != null) {supportedBar.setDisplayHomeAsUpEnabled(true); }
        }

        // init web view
        fullView = (WebView) findViewById(R.id.webview_full);
        fullView.setWebViewClient(new MyCustomWebViewClient());
        fullView.getSettings().setJavaScriptEnabled(true);
        fullView.getSettings().setUserAgentString(Config.BROWSER_USER_AGENT);
        fullView.getSettings().setBuiltInZoomControls(true);
        fullView.getSettings().setDisplayZoomControls(false);

        progressDialog = ProgressDialog.show(this, getString(R.string.just_moment_please), getString(R.string.scrapp_surfing), true, true);


        // init cookie manager
        CookieSyncManager.createInstance(this);
        cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        //CookieHandler.setDefault(new java.net.CookieManager());

        // get rule id to display
        int ruleId = getIntent().getIntExtra(SubscriptionActivity.EXTRA_RULE_ID, -1);

        // start service to scrape for web view
        if(ruleId != -1) {
            Intent intent = new Intent(this, WebService.class);
            intent.putExtra(SubscriptionActivity.EXTRA_RULE_ID, ruleId);
            webResultReceiver = new WebResultReceiver(new Handler());
            webResultReceiver.setReceiver(this);
            intent.putExtra(EXTRA_RECEIVER, webResultReceiver);
            startService(intent);
        }
    }

    @Override
    @SuppressWarnings({"deprecation", "unchecked"})
    public void onReceiveResult(int resultCode, Bundle resultData) {
        if (resultCode == 1) {
            cookieManager.removeAllCookie();

            String url = resultData.getString(EXTRA_URL);

            /* get cookies from result bundle and set to web view */
            HashMap<String, String> cookies = (HashMap<String, String>) resultData.getSerializable(EXTRA_COOKIES_KEYS);
            if(url != null && cookies != null){
                for (Map.Entry<String, String> entry : cookies.entrySet()){
                    Log.v(TAG, entry.getKey() + ": " + entry.getValue());
                    cookieManager.setCookie(url, entry.getKey() + "=" + entry.getValue());
                }
                CookieSyncManager.getInstance().sync();
                Log.v(TAG, "URL:" + url);

                // display url in web view
                fullView.loadUrl(url);
                setViewTitle(url);

                if(progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
            } else {
                Toast.makeText(this, R.string.parsing_error, Toast.LENGTH_SHORT).show();
                finish();
            }
        } else {
            Toast.makeText(this, R.string.parsing_error, Toast.LENGTH_SHORT).show();
            finish();
        }
    }


    /**
     * Sets the displayed title to web view.
     * @param url displayed url
     */
    private void setViewTitle(String url){
        url = url.substring(url.indexOf(".")+1, url.length());
        if(url.contains("/")) {
            url = url.substring(0, url.indexOf("/"));
        }
        if (supportedBar != null) { supportedBar.setTitle(url); }
    }

    /**
     * Custom wev view client to override url loading. All urls klicked in the
     * web view are loaded in the webview.
     */
    private class MyCustomWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            fullView.loadUrl(url);
            return true;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        // MenuInflater inflater = getMenuInflater();
        // inflater.inflate(R.menu.actions_subscription, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume(){
        super.onResume();
        webResultReceiver.setReceiver(this);
    }

    @Override
    public void onPause(){
        super.onPause();
        webResultReceiver.setReceiver(null);
    }
}