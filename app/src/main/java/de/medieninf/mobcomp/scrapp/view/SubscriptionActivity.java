package de.medieninf.mobcomp.scrapp.view;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import de.medieninf.mobcomp.scrapp.R;
import de.medieninf.mobcomp.scrapp.database.DBHelper;
import de.medieninf.mobcomp.scrapp.database.Database;
import de.medieninf.mobcomp.scrapp.rest.service.RestServiceHelper;
import de.medieninf.mobcomp.scrapp.view.adapter.ResultListAdapter;
import de.medieninf.mobcomp.scrapp.view.widget.ExpandablePanel;

/**
 * Activity to show a detailed subscription and its results.
 */
public class SubscriptionActivity extends AppCompatActivity {
    private static final String TAG = SubscriptionActivity.class.getSimpleName();

    public static final String EXTRA_RULE_ID = "rule_id";

    private RestServiceHelper restServiceHelper;
    private int ruleId;

    private ResultListAdapter resultListAdapter;
    private ResultLoader resultLoader;
    private int RESULT_LOADER_ID = 3;

    private WebView resultView;
    private SwipeRefreshLayout srlResults;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_subscription);

        restServiceHelper = new RestServiceHelper(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setTitle(getString(R.string.subscription_activity_name));
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // init SwipeRefresh with view and listener
        srlResults = (SwipeRefreshLayout) findViewById(R.id.srl_results);
        srlResults.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // refresh List of Results via Rest
                restServiceHelper.createResult(ruleId, false);
            }
        });

        Intent intent = getIntent();
        ruleId = intent.getIntExtra(EXTRA_RULE_ID, -1);

        init();
    }

    private void init() {
        DBHelper dbHelper = new DBHelper(this);
        // execute query
        Cursor cursor = dbHelper.getSubscription(ruleId);

        if (cursor.moveToFirst()) {
            // set title
            TextView tvTitle = (TextView) findViewById(R.id.tv_subscription_title);
            tvTitle.setText(cursor.getString(cursor.getColumnIndex(Database.Rule.TITLE)));

            resultView = (WebView) findViewById(R.id.wv_result);
        }
        cursor.close();

        // init result loader
        ListView lvResults = (ListView) findViewById(R.id.lv_results);

        // set result adapter
        resultListAdapter = new ResultListAdapter(this, null, false, resultView);
        lvResults.setAdapter(resultListAdapter);

        // init rule loader
        resultLoader = new ResultLoader(this);
        getSupportLoaderManager().initLoader(RESULT_LOADER_ID, null, resultLoader);

        // init ClickListener for results in listview
        AdapterView.OnItemClickListener resultClickedHandler = new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView parent, View v, int position, long id)
            {
                v.setActivated(true);
                resultListAdapter.displayResult(position);
            }
        };
        lvResults.setOnItemClickListener(resultClickedHandler);
    }

    private class ResultLoader implements LoaderManager.LoaderCallbacks<Cursor> {
        private Context context;

        public ResultLoader(Context context) {
            this.context = context;
        }

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            DBHelper dbHelper = new DBHelper(context);
            return dbHelper.getResultLoader(ruleId);
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            resultListAdapter.swapCursor(data);
            resultListAdapter.displayResult(-1);

            // disable animation of SwipeRefreshLayout
            srlResults.setRefreshing(false);
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            resultListAdapter.swapCursor(null);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.actions_subscription, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.menu_refresh:
                restServiceHelper.createResult(ruleId, false);
                srlResults.setRefreshing(true);
                return true;

            case R.id.menu_unsubscribe:
                restServiceHelper.deleteSubscription(ruleId);
                finish();
                return true;

            case R.id.menu_configurate:
                Intent configureIntent = new Intent(this, RuleActivity.class);
                configureIntent.putExtra(RuleActivity.EXTRA_RULE_ID, ruleId);
                startActivity(configureIntent);
                return true;

            case R.id.menu_go_to_page:
                Intent intent = new Intent(this, WebViewActivity.class);
                intent.putExtra(EXTRA_RULE_ID, ruleId);
                startActivity(intent);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
