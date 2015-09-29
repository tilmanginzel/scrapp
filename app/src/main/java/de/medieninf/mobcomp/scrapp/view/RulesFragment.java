package de.medieninf.mobcomp.scrapp.view;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import de.medieninf.mobcomp.scrapp.R;
import de.medieninf.mobcomp.scrapp.database.DBHelper;
import de.medieninf.mobcomp.scrapp.rest.service.RestServiceHelper;
import de.medieninf.mobcomp.scrapp.view.adapter.RuleListAdapter;

/**
 * Fragment to show a list of rules.
 */
public class RulesFragment extends Fragment {
    private static final String TAG = RulesFragment.class.getSimpleName();

    private SwipeRefreshLayout srlRules;
    private RuleListAdapter ruleListAdapter;
    private RuleLoader ruleLoader;
    private int RULE_LOADER_ID = 1;

    private RestServiceHelper restServiceHelper;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // set view
        View v = inflater.inflate(R.layout.fragment_rules, container, false);
        ListView lvRules = (ListView) v.findViewById(R.id.lv_rules);
        srlRules = (SwipeRefreshLayout) v.findViewById(R.id.srl_rules);

        // set rule adapter
        ruleListAdapter = new RuleListAdapter(getActivity(), null, false);
        lvRules.setAdapter(ruleListAdapter);

        // init rule loader
        ruleLoader = new RuleLoader(getActivity());
        getLoaderManager().initLoader(RULE_LOADER_ID, null, ruleLoader);

        // init RestServiceHelper
        restServiceHelper = new RestServiceHelper(getActivity());

        // init SwipeRefresh Listener
        srlRules.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // refresh List of Rules via Rest
                restServiceHelper.getRules();
             }
        });

        return v;
    }

    private class RuleLoader implements LoaderManager.LoaderCallbacks<Cursor> {
        private Context context;

        public RuleLoader(Context context) {
            this.context = context;
        }

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            DBHelper dbHelper = new DBHelper(context);
            return dbHelper.getRulesLoader();
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            ruleListAdapter.swapCursor(data);

            // disable animation of SwipeRefreshLayout
            srlRules.setRefreshing(false);
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            ruleListAdapter.swapCursor(null);
        }
    }
}
