package de.medieninf.mobcomp.scrapp.view.adapter;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import de.medieninf.mobcomp.scrapp.R;
import de.medieninf.mobcomp.scrapp.database.Database;
import de.medieninf.mobcomp.scrapp.util.Utils;
import de.medieninf.mobcomp.scrapp.view.SubscriptionActivity;

/**
 * CursorAdapter for a list of subscriptions.
 */
public class SubscriptionListAdapter extends CursorAdapter {
    private static final String TAG = RuleListAdapter.class.getSimpleName();

    private LayoutInflater layoutInflater;

    public SubscriptionListAdapter(Context context, Cursor c, boolean autoRequery) {
        super(context, c, autoRequery);
        layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return layoutInflater.inflate(R.layout.subscription_item, parent, false);
    }

    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {
        // set title
        TextView tvTitle = (TextView) view.findViewById(R.id.tv_subscription_title);
        tvTitle.setText(cursor.getString(cursor.getColumnIndex(Database.Rule.TITLE)));

        // set description
        TextView tvDescription = (TextView) view.findViewById(R.id.tv_subscription_description);
        tvDescription.setText(cursor.getString(cursor.getColumnIndex(Database.Rule.DESCRIPTION)));

        // set updated at // TODO: remove TextView
        TextView tvUpdatedAt = (TextView) view.findViewById(R.id.tv_subscription_updated_at);
        // String prettyDate = Utils.getRelativeTimeSpanString(cursor.getString(cursor.getColumnIndex(Database.Rule.UPDATED_AT)));
        tvUpdatedAt.setText("");

        // create intent
        int ruleId = cursor.getInt(cursor.getColumnIndex(Database.Rule.RULE_ID));
        final Intent intent = new Intent(context, SubscriptionActivity.class);
        intent.putExtra(SubscriptionActivity.EXTRA_RULE_ID, ruleId);

        // set click listener
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                context.startActivity(intent);
            }
        });
    }
}
