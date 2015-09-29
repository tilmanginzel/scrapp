package de.medieninf.mobcomp.scrapp.view.adapter;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import de.medieninf.mobcomp.scrapp.R;
import de.medieninf.mobcomp.scrapp.database.Database;
import de.medieninf.mobcomp.scrapp.rest.service.RestServiceHelper;
import de.medieninf.mobcomp.scrapp.view.RuleActivity;

/**
 * CursorAdapter for a list of rules.
 */
public class RuleListAdapter extends CursorAdapter {
    private static final String TAG = RuleListAdapter.class.getSimpleName();

    private LayoutInflater layoutInflater;
    private RestServiceHelper restServiceHelper;

    public RuleListAdapter(Context context, Cursor c, boolean autoRequery) {
        super(context, c, autoRequery);
        layoutInflater = LayoutInflater.from(context);

        restServiceHelper = new RestServiceHelper(context);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return layoutInflater.inflate(R.layout.rule_item, parent, false);
    }

    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {
        // set title
        TextView tvTitle = (TextView) view.findViewById(R.id.tv_rule_title);
        tvTitle.setText(cursor.getString(cursor.getColumnIndex(Database.Rule.TITLE)));

        // set description
        TextView tvDescription = (TextView) view.findViewById(R.id.tv_rule_description);
        tvDescription.setText(cursor.getString(cursor.getColumnIndex(Database.Rule.DESCRIPTION)));

        // show icon if rule is subscribed
        ImageView ivSubscribedToRule = (ImageView) view.findViewById(R.id.iv_subscribed_to_rule);
        boolean subscribed = cursor.getInt(cursor.getColumnIndex(Database.Rule.SUBSCRIBED)) == 1;
        ivSubscribedToRule.setVisibility(subscribed ? View.VISIBLE : View.INVISIBLE);

        final int ruleId = cursor.getInt(cursor.getColumnIndex(Database.Rule.RULE_ID));
        final Intent intent = new Intent(context, RuleActivity.class);
        intent.putExtra(RuleActivity.EXTRA_RULE_ID, ruleId);

        // set click listener
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                context.startActivity(intent);
            }
        });
    }
}