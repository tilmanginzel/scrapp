package de.medieninf.mobcomp.scrapp.rest.service;

import android.content.Context;
import android.content.Intent;

/**
 * Helper class to make rest calls in a background thread.
 */
public class RestServiceHelper {

    // the context is used to start a service via an intent
    private Context context;

    public RestServiceHelper(Context context) {
        this.context = context.getApplicationContext();
    }

    /**
     * Starts an IntentService to get all rules.
     *
     * @return request id
     */
    public long getRules() {
        int methodKey = RestService.METHOD_GET_RULES;

        // set request id and add it to the list
        long requestId = concatenateLong(RestService.METHOD_GET_RULES, 0);

        // get intent
        Intent intent = getBasicRestIntent(RestService.RESOURCE_TYPE_RULE, methodKey, requestId);

        // start worker thread in background
        context.startService(intent);

        return requestId;
    }

    /**
     * Starts an IntentService to get a single rules with actions and action_params.
     * @param ruleId ruleId of the requested rule
     * @return request id
     */
    public long getRuleWithActions(int ruleId) {
        int methodKey = RestService.METHOD_GET_RULE_WITH_ACTIONS;

        // set request id and add it to the list
        long requestId = concatenateLong(methodKey, ruleId);

        // build intent
        Intent intent = getBasicRestIntent(RestService.RESOURCE_TYPE_RULE, methodKey, requestId);
        intent.putExtra(RestService.EXTRA_RULE_ID, ruleId);

        // start worker thread in background
        context.startService(intent);
        return requestId;
    }

    /**
     * Starts an IntentService to create a new app user.
     *
     * @return request id
     */
    public long createUser() {
        int methodKey = RestService.METHOD_CREATE_USER;

        // set request id and add it to the list
        long requestId = concatenateLong(methodKey, 0);

        // build intent
        Intent intent = getBasicRestIntent(RestService.RESOURCE_TYPE_USER, methodKey, requestId);

        // start worker thread in background
        context.startService(intent);
        return requestId;
    }

    /**
     * Starts an IntentService to create a subscription.
     *
     * @param ruleId The ruleId for the rule to be subscribed.
     * @return request id
     */
    public long createSubscription(int ruleId) {
        int methodKey = RestService.METHOD_CREATE_SUBSCRIPTION;

        // set request id and add it to the list
        long requestId = concatenateLong(methodKey, ruleId);

        // build intent
        Intent intent = getBasicRestIntent(RestService.RESOURCE_TYPE_SUBSCRIPTION, methodKey, requestId);
        intent.putExtra(RestService.EXTRA_RULE_ID, ruleId);

        // start worker thread in background
        context.startService(intent);
        return requestId;
    }

    /**
     * Starts an IntentService to update a subscription.
     *
     * @param ruleId The ruleId for the rule to be subscribed.
     * @return request id
     */
    public long updateSubscription(int ruleId) {
        int methodKey = RestService.METHOD_UPDATE_SUBSCRIPTION;

        // set request id and add it to the list
        long requestId = concatenateLong(methodKey, ruleId);

        // build intent
        Intent intent = getBasicRestIntent(RestService.RESOURCE_TYPE_SUBSCRIPTION, methodKey, requestId);
        intent.putExtra(RestService.EXTRA_RULE_ID, ruleId);

        // start worker thread in background
        context.startService(intent);
        return requestId;
    }

    /**
     * Starts an IntentService to delete a subscription.
     *
     * @param ruleId The ruleId for the rule to be unsubscribed.
     * @return request id
     */
    public long deleteSubscription(int ruleId) {
        int methodKey = RestService.METHOD_DELETE_SUBSCRIPTION;

        // set request id and add it to the list
        long requestId = concatenateLong(methodKey, ruleId);

        // build intent
        Intent intent = getBasicRestIntent(RestService.RESOURCE_TYPE_SUBSCRIPTION, methodKey, requestId);
        intent.putExtra(RestService.EXTRA_RULE_ID, ruleId);

        // start worker thread in background
        context.startService(intent);
        return requestId;
    }

    /**
     * Starts an IntentService to create a result for a given rule.
     *
     * @param ruleId - rule id
     * @return request id
     */
    public long createResult(int ruleId, boolean automaticScrape) {
        int methodKey = RestService.METHOD_CREATE_RESULT;

        // set request id and add it to the list
        long requestId = concatenateLong(methodKey, ruleId);

        // build intent
        Intent intent = getBasicRestIntent(RestService.RESOURCE_TYPE_RESULT, methodKey, requestId);
        intent.putExtra(RestService.EXTRA_RULE_ID, ruleId);
        intent.putExtra(RestService.EXTRA_SCRAPE_TYPE, automaticScrape);

        // start worker thread in background
        context.startService(intent);
        return requestId;
    }

    /**
     * Build a basic intent which is the same for each request.
     *
     * @param resource - resource type key
     * @param method - method key
     * @param requestId - request id
     * @return intent
     */
    private Intent getBasicRestIntent(int resource, int method, long requestId) {
        Intent intent = new Intent(context, RestService.class);
        intent.putExtra(RestService.RESOURCE_TYPE_KEY, resource);
        intent.putExtra(RestService.METHOD_KEY, method);
        intent.putExtra(RestService.REQUEST_ID, requestId);
        return intent;
    }

    /**
     * Concatenates two long values.
     *
     * @param a first long value
     * @param b second long value
     * @return ab as long
     */
    private long concatenateLong(long a, long b) {
        return Long.parseLong("" + a + b);
    }
}
