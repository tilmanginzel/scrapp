package de.medieninf.mobcomp.scrapp.rest.processor;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import de.medieninf.mobcomp.scrapp.rest.model.Rule;
import retrofit.RetrofitError;

/**
 * This processor is responsible for rule resources.
 * It makes rest calls and required inserts / updates to the local database.
 */
public class RuleProcessor extends RestProcessor {
    private static final String TAG = RuleProcessor.class.getSimpleName();

    public RuleProcessor(Context context) {
        super(context);
    }

    /**
     * Gets all rules via synchronous rest call and inserts them into the database.
     */
    public void getRules() {
        List<Rule> rules = new ArrayList<>();
        String updatedAtServer = dbHelper.getUpdatedAtServerFromRule();

        // make synchronous rest call
        try {
            rules = restClient.getApiService().getRules(updatedAtServer);
        } catch (RetrofitError e) {
            Log.e(TAG, "RetrofitError: "+ e.getMessage());
        }

        // insert into database
        dbHelper.createRules(rules);
    }

    /**
     * Gets requested rule with actions (and action_params) via synchronous rest call and
     * inserts them into the database.
     *
     * @param ruleId Id of the requested rule
     */
    public void getRuleWithActions(int ruleId) {
        Rule rule = new Rule();
        String updatedAtServer = dbHelper.getUpdatedAtServerFromAction(ruleId);

        // make synchronous rest call
        try {
            rule = restClient.getApiService().getRuleWithActions(ruleId, updatedAtServer);
        } catch (RetrofitError e) {
            Log.e(TAG, "RetrofitError: "+ e.getMessage());
        }

        // insert into database
        dbHelper.createRuleWithActions(rule);
    }
}
