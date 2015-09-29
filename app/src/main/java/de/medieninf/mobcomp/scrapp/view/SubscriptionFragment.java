package de.medieninf.mobcomp.scrapp.view;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import de.medieninf.mobcomp.scrapp.R;
import de.medieninf.mobcomp.scrapp.database.DBHelper;
import de.medieninf.mobcomp.scrapp.view.adapter.SubscriptionListAdapter;

/**
 * Fragment to show a list of subscriptions.
 */
public class SubscriptionFragment extends Fragment {
    private SubscriptionListAdapter subscriptionListAdapter;
    private SubscriptionLoader subscriptionLoader;
    private int SUBSCRIPTION_LOADER_ID = 2;

    private TextView tvNoSubscriptions;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // set view
        View v = inflater.inflate(R.layout.fragment_subscription, container, false);
        ListView lvSubscriptions = (ListView) v.findViewById(R.id.lv_subscriptions);
        tvNoSubscriptions = (TextView) v.findViewById(R.id.tv_no_subscriptions);

        // set subscription adapter
        subscriptionListAdapter = new SubscriptionListAdapter(getActivity(), null, false);
        lvSubscriptions.setAdapter(subscriptionListAdapter);

        // init rule loader
        subscriptionLoader = new SubscriptionLoader(getActivity());
        getLoaderManager().initLoader(SUBSCRIPTION_LOADER_ID, null, subscriptionLoader);

        return v;
    }

    private class SubscriptionLoader implements LoaderManager.LoaderCallbacks<Cursor> {
        private Context context;

        public SubscriptionLoader(Context context) {
            this.context = context;
        }

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            DBHelper dbHelper = new DBHelper(context);
            return dbHelper.getSubscriptionsLoader();
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            subscriptionListAdapter.swapCursor(data);

            // set text view visibility
            tvNoSubscriptions.setVisibility(data.getCount() == 0 ? View.VISIBLE : View.GONE);
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            subscriptionListAdapter.swapCursor(null);
        }
    }
}
