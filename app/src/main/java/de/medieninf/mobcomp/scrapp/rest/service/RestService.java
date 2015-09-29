package de.medieninf.mobcomp.scrapp.rest.service;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import java.util.HashSet;
import java.util.Set;

import de.medieninf.mobcomp.scrapp.rest.processor.ResultProcessor;
import de.medieninf.mobcomp.scrapp.rest.processor.RuleProcessor;
import de.medieninf.mobcomp.scrapp.rest.processor.SubscriptionProcessor;
import de.medieninf.mobcomp.scrapp.rest.processor.UserProcessor;

/**
 * Service, coordinating rest operations. Holds method keys for all possible methods and resources.
 * Depending on the codes, it is decided which processor is required.
 */
public class RestService extends IntentService {
    private static final String TAG = RestService.class.getSimpleName();

    // Bundle extra keys: resource types
    public static final String RESOURCE_TYPE_KEY = "resource_type";
    public static final int RESOURCE_TYPE_USER = 100;
    public static final int RESOURCE_TYPE_RULE = 200;
    public static final int RESOURCE_TYPE_SUBSCRIPTION = 300;
    public static final int RESOURCE_TYPE_RESULT = 400;

    // Bundle extra keys: methods
    public static final String METHOD_KEY = "method";
    public static final int METHOD_CREATE_USER = 101;
    public static final int METHOD_GET_RULES = 201;
    public static final int METHOD_GET_RULE_WITH_ACTIONS = 203;
    public static final int METHOD_CREATE_SUBSCRIPTION = 301;
    public static final int METHOD_DELETE_SUBSCRIPTION = 302;
    public static final int METHOD_UPDATE_SUBSCRIPTION = 303;
    public static final int METHOD_CREATE_RESULT = 401;

    // Bundle extra keys
    public static final String REQUEST_ID = "request_id";
    public static final String EXTRA_RULE_ID = "rule_id";
    public static final String EXTRA_SCRAPE_TYPE = "scrape_type";

    private Set<Long> pendingRequests;

    /**
     * Creates an IntentService. Invoked by your subclass's constructor.
     */
    public RestService() {
        super(TAG);
        pendingRequests = new HashSet<>();
    }

    @Override
    public void onStart(Intent intent, int startId) {
        long requestId = intent.getLongExtra(REQUEST_ID, -1);
        if (pendingRequests.contains(requestId)) {
            // ignore intent if it is already pending
            return;
        }

        // add request to pending requests
        pendingRequests.add(requestId);

        // add intent to message queue
        super.onStart(intent, startId);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // get desired resource and method from extras
        int resourceType = intent.getIntExtra(RESOURCE_TYPE_KEY, -1);
        int method = intent.getIntExtra(METHOD_KEY, -1);

        // get request id
        long requestId = intent.getLongExtra(REQUEST_ID, -1);

        switch(resourceType) {
            case RESOURCE_TYPE_USER:
                handleUserResource(method);
                break;
            case RESOURCE_TYPE_RULE:
                handleRuleResource(method, intent);
                break;
            case RESOURCE_TYPE_SUBSCRIPTION:
                handleSubscriptionResource(method, intent);
                break;
            case RESOURCE_TYPE_RESULT:
                handleResultResource(method, intent);
                break;
            default:
                Log.e(TAG, "Invalid resource call to RestService: "+ resourceType);
                break;
        }

        // remove request from pending requests
        pendingRequests.remove(requestId);
    }

    /**
     * Decides which method to call in the UserProcessor.
     *
     * @param method The method to call indicated with an integer.
     */
    private void handleUserResource(int method) {
        UserProcessor userProcessor = new UserProcessor(this);

        switch(method) {
            case METHOD_CREATE_USER:
                userProcessor.createUser();
                break;
            default:
                Log.e(TAG, "Invalid method call to RestService: "+ method);
                break;
        }
    }

    /**
     * Decides which method to call in the RuleProcessor.
     *
     * @param method The method to call indicated with an integer.
     * @param intent The intent with all bundle extras.
     */
    private void handleRuleResource(int method, Intent intent) {
        RuleProcessor ruleProcessor = new RuleProcessor(this);

        switch(method) {
            case METHOD_GET_RULES:
                ruleProcessor.getRules();
                break;
            case METHOD_GET_RULE_WITH_ACTIONS:
                ruleProcessor.getRuleWithActions(intent.getIntExtra(EXTRA_RULE_ID, -1));
                break;
            default:
                Log.e(TAG, "Invalid method call to RestService: "+ method);
                break;
        }
    }

    /**
     * Decides which method to call in the SubscriptionProcessor.
     *
     * @param method The method to call indicated with an integer.
     * @param intent The intent with all bundle extras.
     */
    private void handleSubscriptionResource(int method, Intent intent) {
        SubscriptionProcessor subscriptionProcessor = new SubscriptionProcessor(this);

        switch(method) {
            case METHOD_CREATE_SUBSCRIPTION:
                subscriptionProcessor.createSubscription(intent.getIntExtra(EXTRA_RULE_ID, -1));
                break;
            case METHOD_DELETE_SUBSCRIPTION:
                subscriptionProcessor.deleteSubscription(intent.getIntExtra(EXTRA_RULE_ID, -1));
                break;
            case METHOD_UPDATE_SUBSCRIPTION:
                subscriptionProcessor.updateSubscription(intent.getIntExtra(EXTRA_RULE_ID, -1));
                break;
            default:
                Log.e(TAG, "Invalid method call to RestService: " + method);
                break;
        }
    }

    /**
     * Decides which method to call in the ResultProcessor.
     *
     * @param method The method to call indicated with an integer.
     * @param intent The intent with all bundle extras.
     */
    private void handleResultResource(int method, Intent intent) {
        ResultProcessor resultProcessor = new ResultProcessor(this);

        switch(method) {
            case METHOD_CREATE_RESULT:
                int ruleId = intent.getIntExtra(EXTRA_RULE_ID, -1);
                boolean automaticScrape = intent.getBooleanExtra(EXTRA_SCRAPE_TYPE, false);
                resultProcessor.createResult(ruleId, automaticScrape);
                break;
            default:
                Log.e(TAG, "Invalid method call to RestService: " + method);
                break;
        }
    }
}
