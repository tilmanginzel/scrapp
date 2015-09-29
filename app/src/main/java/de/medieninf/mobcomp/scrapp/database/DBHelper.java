package de.medieninf.mobcomp.scrapp.database;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.medieninf.mobcomp.scrapp.rest.model.Action;
import de.medieninf.mobcomp.scrapp.rest.model.ActionParam;
import de.medieninf.mobcomp.scrapp.rest.model.Result;
import de.medieninf.mobcomp.scrapp.rest.model.Rule;
import de.medieninf.mobcomp.scrapp.util.RequestState;
import de.medieninf.mobcomp.scrapp.util.Utils;
import se.simbio.encryption.Encryption;

/**
 * Helper class to access the database with convenience methods.
 */
public class DBHelper {
    private Context context;
    private ContentResolver contentResolver;

    private static final Uri RULE_URI;
    public static final Uri RESULT_URI;
    private static final Uri ACTION_URI;
    private static final Uri ACTION_PARAM_URI;

    static {
        Uri contentUri = ScrappProvider.CONTENT_URI;

        RULE_URI = contentUri.buildUpon().appendPath(Database.Rule.TABLE).build();
        RESULT_URI = contentUri.buildUpon().appendPath(Database.Result.TABLE).build();
        ACTION_URI = contentUri.buildUpon().appendPath(Database.Action.TABLE).build();
        ACTION_PARAM_URI = contentUri.buildUpon().appendPath(Database.ActionParam.TABLE).build();
    }

    public DBHelper(Context context) {
        this.context = context.getApplicationContext();
        this.contentResolver = this.context.getContentResolver();
    }

    /**
     * Get a CursorLoader with all rules.
     *
     * @return CursorLoader
     */
    public Loader<Cursor> getRulesLoader() {
        // select columns
        String[] projection = {
                Database.Rule.ID,
                Database.Rule.RULE_ID,
                Database.Rule.TITLE,
                Database.Rule.DESCRIPTION,
                Database.Rule.SUBSCRIBED,
                Database.Rule.INTERVAL,
                Database.Rule.START_TIME,
                Database.Rule.WIFI_ONLY,
                Database.Rule.REQUEST_STATE
        };

        return new CursorLoader(context, RULE_URI, projection, null, null, null);
    }

    /**
     * Get a CursorLoader for a given rule.
     *
     * @param ruleId - rule id
     * @return CursorLoader
     */
    public Loader<Cursor> getRuleLoader(int ruleId) {
        // select columns
        String[] projection = {
            Database.Rule.ID,
            Database.Rule.RULE_ID,
            Database.Rule.TITLE,
            Database.Rule.DESCRIPTION,
            Database.Rule.SUBSCRIBED,
            Database.Rule.WIFI_ONLY,
            Database.Rule.INTERVAL, Database.Rule.START_TIME,
            Database.Rule.REQUEST_STATE,
        };

        // set where clause
        String selection = Database.Rule.RULE_ID + " = ?";
        String[] selectionArgs = {"" + ruleId};

        return new CursorLoader(context, RULE_URI, projection, selection, selectionArgs, null);
    }

    /**
     * Get a rule title by its id.
     *
     * @param ruleId - rule id
     * @return title as string
     */
    public String getRuleTitle(int ruleId) {
        Cursor cursor = getSubscription(ruleId);

        if (cursor.moveToFirst()) {
            return cursor.getString(cursor.getColumnIndex(Database.Rule.TITLE));
        }
        return null;
    }

    /**
     * Get a CursorLoader with all subscriptions.
     *
     * @return CursorLoader
     */
    public Loader<Cursor> getSubscriptionsLoader() {
        // select columns
        String[] projection = {
                Database.Rule.ID,
                Database.Rule.RULE_ID,
                Database.Rule.TITLE,
                Database.Rule.DESCRIPTION,
                Database.Rule.WIFI_ONLY,
                Database.Rule.REQUEST_STATE,
                Database.Rule.UPDATED_AT
        };

        // select only subscribed rules
        String selection = Database.Rule.SUBSCRIBED + " = ?";
        String[] selectionArgs = {"" + 1};

        return new CursorLoader(context, RULE_URI, projection, selection, selectionArgs, null);
    }

    public Cursor getSubscriptions(){
        // select columns
        String[] projection = {
                Database.Rule.TITLE,
                Database.Rule.RULE_ID,
                Database.Rule.DESCRIPTION,
                Database.Rule.REQUEST_STATE,
                Database.Rule.START_TIME,
                Database.Rule.INTERVAL,
                Database.Rule.WIFI_ONLY,
                Database.Rule.UPDATED_AT,
                Database.Rule.CREATED_AT
        };

        // set where clause
        String selection = Database.Rule.SUBSCRIBED + " = ?";
        String[] selectionArgs = {"" + 1};

        return contentResolver.query(RULE_URI, projection, selection, selectionArgs, null);
    }

    /**
     * Get a CursorLoader with all results for a given subscription.
     *
     * @param ruleId - rule id
     * @return CursorLoader
     */
    public Loader<Cursor> getResultLoader(int ruleId) {
        // set where clause
        String selection = Database.Result.RULE_ID + " = ?";
        String[] selectionArgs = {"" + ruleId};

        // set order by
        String order = Database.Result.UPDATED_AT + " DESC";

        // info: projection will be neglected here, see ScrappProvider.query() instead.
        return new CursorLoader(context, RESULT_URI, null, selection, selectionArgs, order);
    }

    /**
     * Get a CursorLoader with all actions for a given rule id.
     * Only used to get notified about action changes, the actual content is irrelevant.
     *
     * @param ruleId rule id
     * @return CursorLoader.
     */
    public Loader<Cursor> getActionLoader(int ruleId) {
        String[] projection = { Database.Action.ID };

        // set where clause
        String selection = Database.Action.RULE_ID + " = ?";
        String[] selectionArgs = {"" + ruleId};

        return new CursorLoader(context, ACTION_URI, projection, selection, selectionArgs, null);
    }

    /**
     * Get a subscribed rule by given rule id.
     *
     * @param ruleId - rule id
     * @return Cursor with rule
     */
    public Cursor getSubscription(int ruleId) {
        return getSubscriptionOrRule(ruleId, 1);
    }

    /**
     * Get a not subscribed rule by given rule id.
     *
     * @param ruleId - rule id
     * @return Cursor with rule
     */
    public Cursor getRule(int ruleId) {
        return getSubscriptionOrRule(ruleId, 0);
    }

    /**
     * Informs wether a rule is or isn't subscribed by client.
     * @param ruleId related rule id
     * @return true if rule is subscribed else false
     */
    public boolean isRuleSubscribed(int ruleId){
        // select columns
        String[] projection = {
                Database.Rule.SUBSCRIBED
        };

        // set where clause
        String selection = Database.Rule.RULE_ID + " = ?";
        String[] selectionArgs = {"" + ruleId};

        Cursor cursor = contentResolver.query(RULE_URI, projection, selection, selectionArgs, null);
        return cursor.moveToFirst() && cursor.getInt(cursor.getColumnIndex(Database.Rule.SUBSCRIBED)) == 1;
    }

    /**
     * Get a subscription by given rule id.
     *
     * @param ruleId - rule id
     * @return Cursor
     */
    private Cursor getSubscriptionOrRule(int ruleId, int subscribed) {
        // select columns
        String[] projection = {
                Database.Rule.TITLE,
                Database.Rule.DESCRIPTION,
                Database.Rule.REQUEST_STATE,
                Database.Rule.START_TIME,
                Database.Rule.INTERVAL,
                Database.Rule.WIFI_ONLY,
                Database.Rule.UPDATED_AT,
                Database.Rule.CREATED_AT
        };

        // set where clause
        String selection = Database.Rule.RULE_ID + " = ? AND "+ Database.Rule.SUBSCRIBED + " = ?";
        String[] selectionArgs = {"" + ruleId, "" + subscribed};

        return contentResolver.query(RULE_URI, projection, selection, selectionArgs, null);
    }

    /* Methods used in RuleProcessor */

    /**
     * Insert a list of rules into the database.
     *
     * @param rules - list of rules
     */
    public void createRules(List<Rule> rules) {
        for(Rule rule : rules) {
            ContentValues values = new ContentValues();
            values.put(Database.Rule.RULE_ID, rule.getRuleId());
            values.put(Database.Rule.TITLE, rule.getTitle());
            values.put(Database.Rule.START_TIME, new Date().getTime());
            values.put(Database.Rule.INTERVAL, 0);
            values.put(Database.Rule.DESCRIPTION, rule.getDescription());
            values.put(Database.Rule.REQUEST_STATE, RequestState.DONE.name());
            values.put(Database.Rule.UPDATED_AT_SERVER, rule.getUpdatedAtServer());

            // insert into database
            contentResolver.insert(RULE_URI, values);
        }

        // notify loaders about change even if no rules were added
        context.getContentResolver().notifyChange(RULE_URI, null);
    }

    /**
     * Insert a rule and Actions and if exist ActionParams.
     *
     * @param rule - Rule with Actions and if exist ActionParams
     */
    public void createRuleWithActions(Rule rule) {
        Encryption encryption = Utils.getEncryption();

        for (Action action : rule.getActions()) {
            ContentValues values = new ContentValues();
            values.put(Database.Action.ACTION_ID, action.getActionId());
            values.put(Database.Action.TITLE, action.getTitle());
            values.put(Database.Action.RULE_ID, rule.getRuleId());
            values.put(Database.Action.POSITION, action.getPosition());
            values.put(Database.Action.METHOD, action.getMethod());
            values.put(Database.Action.URL, action.getUrl());
            values.put(Database.Action.PARSE_EXPRESSION, action.getParseExpression());
            values.put(Database.Action.PARSE_TYPE, action.getParseType());
            values.put(Database.Action.PARSE_EXPRESSION_DISPLAY, action.getParseExpressionDisplay());
            values.put(Database.Action.PARSE_TYPE_DISPLAY, action.getParseTypeDisplay());
            values.put(Database.Action.UPDATED_AT_SERVER, action.getUpdatedAtServer());

            // insert action in database
            contentResolver.insert(ACTION_URI, values);

            List<ActionParam> actionParams = action.getActionParams();
            if (actionParams != null) {
                for (ActionParam actionParam : actionParams) {
                    ContentValues actionParamValues = new ContentValues();
                    actionParamValues.put(Database.ActionParam.ACTION_PARAM_ID, actionParam.getActionParamId());
                    actionParamValues.put(Database.ActionParam.ACTION_ID, action.getActionId());
                    actionParamValues.put(Database.ActionParam.TITLE, actionParam.getTitle());
                    actionParamValues.put(Database.ActionParam.KEY, actionParam.getKey());

                    String encrypted = null;
                    if (actionParam.getValue() != null && actionParam.getValue().length() > 0) {
                        encrypted = encryption.encryptOrNull(actionParam.getValue());
                    }
                    actionParamValues.put(Database.ActionParam.VALUE, encrypted);
                    actionParamValues.put(Database.ActionParam.TYPE, actionParam.getType());
                    actionParamValues.put(Database.ActionParam.REQUIRED, actionParam.getRequired() ? 1 : 0);
                    actionParamValues.put(Database.ActionParam.UPDATED_AT_SERVER, actionParam.getUpdatedAtServer());

                    // insert action_param in database
                    contentResolver.insert(ACTION_PARAM_URI, actionParamValues);
                }
            }
        }

        // notify loaders about change only if actions were inserted
        if (rule.getActions().size() > 0) {
            context.getContentResolver().notifyChange(ACTION_URI, null);
        }
    }

    /* Methods used in SubscriptionProcessor */

    /**
     * Insert a new subscription into the database.
     *
     * @param ruleId - rule id
     * @param state - current request state
     */
    public void createSubscription(int ruleId, RequestState state) {
        ContentValues values = new ContentValues();
        values.put(Database.Rule.RULE_ID, ruleId);
        values.put(Database.Rule.REQUEST_STATE, state.name());

        // set where clause
        String selection = Database.Rule.RULE_ID + " = ?";
        String[] selectionArgs = {"" + ruleId};

        // update subscription
        contentResolver.update(RULE_URI, values, selection, selectionArgs);
    }

    /**
     * Update a subscription.
     *  @param subscription - subscription
     * @param ruleId rule id
     * @param state - current request state
     */
    public void updateSubscription(Rule subscription, int ruleId, boolean subscribed, Date startTime, int interval, RequestState state) {
        String title = "";
        String description = "";
        if(subscription != null) {
            title = subscription.getTitle();
            description = subscription.getDescription();
        }

        ContentValues values = new ContentValues();
        values.put(Database.Rule.RULE_ID, ruleId);
        values.put(Database.Rule.SUBSCRIBED, subscribed ? 1 : 0);
        values.put(Database.Rule.START_TIME, startTime.getTime());
        values.put(Database.Rule.INTERVAL, interval);
        values.put(Database.Rule.REQUEST_STATE, state.name());
        values.put(Database.Rule.TITLE, title);
        values.put(Database.Rule.DESCRIPTION, description);

        // set where clause
        String selection = Database.Rule.RULE_ID + " = ?";
        String[] selectionArgs = {"" + ruleId};

        // update subscription
        contentResolver.update(RULE_URI, values, selection, selectionArgs);
    }

    /**
     * Mark subscription as deleted to hide it from the user.
     *
     * @param ruleId - rule id
     */
    public void markDeleteSubscription(int ruleId) {
        ContentValues values = new ContentValues();
        values.put(Database.Rule.REQUEST_STATE, RequestState.DELETING.name());

        // set where clause
        String selection = Database.Rule.RULE_ID + " = ? AND " + Database.Rule.SUBSCRIBED + " = ?";
        String[] selectionArgs = {"" + ruleId, "" + 1};

        // mark deleted
        contentResolver.update(RULE_URI, values, selection, selectionArgs);
    }

    /**
     * Delete a subscription and all its results.
     *
     * @param ruleId - rule id
     */
    public void deleteSubscription(int ruleId) {
        ContentValues values = new ContentValues();
        values.put(Database.Rule.SUBSCRIBED, 0); // set subscribed to false
        values.put(Database.Rule.INTERVAL, 0);
        values.put(Database.Rule.START_TIME, new Date().getTime());

        // set where clause for result deletion
        String selectionDelete = Database.Rule.RULE_ID + " = ?";
        String[] selectionArgsDelete = {"" + ruleId};

        // get action params and reset values
        Encryption encryption = Utils.getEncryption();
        Cursor cursor = getActions(ruleId);
        while(cursor.moveToNext()){
            int actionId = cursor.getInt(cursor.getColumnIndex(Database.Action.ACTION_ID));
            Cursor actionParams = getActionParams(actionId);
            while(actionParams.moveToNext()){
                int actionParamId = actionParams.getInt(actionParams.getColumnIndex
                        (Database.ActionParam.ACTION_PARAM_ID));
                updateActionParam(encryption, actionParamId, null);
            }
        }

        // delete results first
        contentResolver.delete(RESULT_URI, selectionDelete, selectionArgsDelete);

        // set where clause for subscription update
        String selectionUpdate = Database.Rule.RULE_ID + " = ? AND " + Database.Rule.SUBSCRIBED + " = ?";
        String[] selectionArgsUpdate = {"" + ruleId, "" + 1};

        // update subscription
        contentResolver.update(RULE_URI, values, selectionUpdate, selectionArgsUpdate);
    }

    /* Methods used in ResultProcessor */

    /**
     * Insert a new result for a given rule.
     *
     * @param ruleId - rule id
     * @param state - current request state
     * @return internal row id
     */
    public long createResult(int ruleId, int actionCount, RequestState state, boolean automaticScrape) {
        ContentValues values = new ContentValues();
        values.put(Database.Result.RULE_ID, ruleId);
        values.put(Database.Result.ACTION_COUNT, actionCount);
        values.put(Database.Result.REQUEST_STATE, state.name());
        values.put(Database.Result.AUTOMATIC_SCRAPE, automaticScrape ? 1 : 0);

        Uri uri = contentResolver.insert(RESULT_URI, values);

        // return internal _id
        return ContentUris.parseId(uri);
    }

    /**
     * Updates a result during the crawl process and sets the current action number.
     *
     * @param internalId - internal result id
     * @param currentActionNumber - current action number
     */
    public void updateResultAtParsing(long internalId, int currentActionNumber) {
        ContentValues values = new ContentValues();
        values.put(Database.Result.CURRENT_ACTION_NUMBER, currentActionNumber);

        // set where clause
        String selection = Database.Result.ID + " = ?";
        String[] selectionArgs = {"" + internalId};

        contentResolver.update(RESULT_URI, values, selection, selectionArgs);
    }

    /**
     * Update a given result in the database.
     *
     * @param result - result
     * @param internalId - internal result id
     * @param state - current request state
     */
    public void updateResult(Result result, long internalId, RequestState state) {
        ContentValues values = new ContentValues();
        values.put(Database.Result.RESULT_ID, result.getResultId());
        values.put(Database.Result.HASH, result.getHash());
        values.put(Database.Result.CONTENT, result.getContent());
        values.put(Database.Result.REQUEST_STATE, state.name());

        // set where clause
        String selection = Database.Result.ID + " = ?";
        String[] selectionArgs = {"" + internalId};

        contentResolver.update(RESULT_URI, values, selection, selectionArgs);
    }

    /**
     * Get all Actions by given rule id ordered by position.
     *
     * @param ruleId - rule id
     * @return Cursor
     */
    private Cursor getActions(int ruleId) {
        // select columns
        String[] projection = {
                Database.Action.ACTION_ID,
                Database.Action.TITLE,
                Database.Action.POSITION,
                Database.Action.METHOD,
                Database.Action.URL,
                Database.Action.PARSE_EXPRESSION,
                Database.Action.PARSE_TYPE,
                Database.Action.PARSE_EXPRESSION_DISPLAY,
                Database.Action.PARSE_TYPE_DISPLAY
        };

        // set where clause
        String selection = Database.Action.RULE_ID + " = ?";
        String[] selectionArgs = {"" + ruleId};
        String sortOrder = Database.Action.POSITION + " ASC";

        return contentResolver.query(ACTION_URI, projection, selection, selectionArgs, sortOrder);
    }

    /**
     * Get all ActionParams by given action id.
     *
     * @param actionId - action id
     * @return Cursor
     */
    private Cursor getActionParams(int actionId) {
        // select columns
        String[] projection = {
                Database.ActionParam.ACTION_PARAM_ID,
                Database.ActionParam.TITLE,
                Database.ActionParam.KEY,
                Database.ActionParam.VALUE,
                Database.ActionParam.TYPE,
                Database.ActionParam.REQUIRED
        };

        // set where clause
        String selection = Database.ActionParam.ACTION_ID + " = ?";
        String[] selectionArgs = {"" + actionId};

        return contentResolver.query(ACTION_PARAM_URI, projection, selection, selectionArgs, null);
    }

    /**
     * Get a list of actions with action params for a given rule id.
     *
     * @param ruleId - rule id
     * @return list of actions
     */
    public List<Action> getActionsForParsing(int ruleId) {
        List<Action> actions = new ArrayList<>();

        Encryption encryption = Utils.getEncryption();

        // get all actions
        Cursor c = getActions(ruleId);
        if (c.moveToFirst()) {
            do {
                int actionId = c.getInt(c.getColumnIndex(Database.Action.ACTION_ID));

                // get action params
                Cursor cActionParams = getActionParams(actionId);
                List<ActionParam> actionParams = null;
                if (cActionParams.moveToFirst()) {
                    actionParams = new ArrayList<>();
                    do {
                        String encryptedValue = cActionParams.getString(cActionParams.getColumnIndex(Database.ActionParam.VALUE));
                        String decryptedValue = encryption.decryptOrNull(encryptedValue);

                        ActionParam actionParam = new ActionParam(
                                cActionParams.getInt(cActionParams.getColumnIndex(Database.ActionParam.ACTION_PARAM_ID)),
                                cActionParams.getString(cActionParams.getColumnIndex(Database.ActionParam.TITLE)),
                                cActionParams.getString(cActionParams.getColumnIndex(Database.ActionParam.KEY)),
                                decryptedValue,
                                cActionParams.getString(cActionParams.getColumnIndex(Database.ActionParam.TYPE)),
                                cActionParams.getInt(cActionParams.getColumnIndex(Database.ActionParam.REQUIRED)) == 1,
                                null,
                                null
                        );
                        actionParams.add(actionParam);
                    } while (cActionParams.moveToNext());
                }
                cActionParams.close();

                Action action = new Action(
                        actionId,
                        c.getString(c.getColumnIndex(Database.Action.TITLE)),
                        c.getInt(c.getColumnIndex(Database.Action.POSITION)),
                        c.getString(c.getColumnIndex(Database.Action.METHOD)),
                        c.getString(c.getColumnIndex(Database.Action.URL)),
                        c.getString(c.getColumnIndex(Database.Action.PARSE_EXPRESSION)),
                        c.getString(c.getColumnIndex(Database.Action.PARSE_TYPE)),
                        c.getString(c.getColumnIndex(Database.Action.PARSE_EXPRESSION_DISPLAY)),
                        c.getString(c.getColumnIndex(Database.Action.PARSE_TYPE_DISPLAY)),
                        null, // createdAt
                        null, // updatedAt
                        actionParams
                );

                actions.add(action);
            } while (c.moveToNext());
        }
        c.close();

        return actions;
    }

    /**
     * Updates a value of a given action param.
     *
     * @param actionParamId - param id
     * @param value - value
     */
    public void updateActionParam(Encryption encryption, int actionParamId, String value) {
        ContentValues values = new ContentValues();

        String encryptedValue = encryption.encryptOrNull(value);
        values.put(Database.ActionParam.VALUE, encryptedValue);

        // set where clause
        String selection = Database.ActionParam.ACTION_PARAM_ID + " = ?";
        String[] selectionArgs = {"" + actionParamId};

        contentResolver.update(ACTION_PARAM_URI, values, selection, selectionArgs);
    }

    /**
     * Updates the time konfiguration a rule.
     *
     * @param ruleId - rule id
     * @param interval - scrape interval
     */
    public boolean updateTimeConfiguration(int ruleId, int interval, Date startTime) {
        boolean timeChanged = timeConfigChanged(ruleId, interval, startTime);

        ContentValues values = new ContentValues();
        values.put(Database.Rule.INTERVAL, interval);
        values.put(Database.Rule.START_TIME, startTime.getTime());

        // set where clause
        String selection = Database.Rule.RULE_ID + " = ?";
        String[] selectionArgs = {"" + ruleId};

        // update rule
        contentResolver.update(RULE_URI, values, selection, selectionArgs);

        return timeChanged;
    }


    /**
     * Proofs if time config has changed. Updates time config on server if it has changed.
     * @param ruleId rule id
     * @param interval probably new interval
     * @param startTime probably new start time
     */
    private boolean timeConfigChanged(int ruleId, int interval, Date startTime){
        Cursor cursor = getSubscription(ruleId);
        if(cursor.moveToFirst()){
            int oldInterval = cursor.getInt(cursor.getColumnIndex(Database.Rule.INTERVAL));
            Date oldStartTime = new Date();
            oldStartTime.setTime(cursor.getLong(cursor.getColumnIndex(Database.Rule.START_TIME)));

            if (oldInterval != interval || !oldStartTime.equals(startTime)){
                return true;
            }
        }
        return false;
    }

    /**
     * Updates the wifi_only flag for a rule.
     *
     * @param ruleId - rule id
     * @param wifiOnly - true or false
     */
    public void updateWifiOnly(int ruleId, boolean wifiOnly) {
        ContentValues values = new ContentValues();
        values.put(Database.Rule.WIFI_ONLY, wifiOnly ? 1 : 0);

        // set where clause
        String selection = Database.Rule.RULE_ID + " = ?";
        String[] selectionArgs = {"" + ruleId};

        // update rule
        contentResolver.update(RULE_URI, values, selection, selectionArgs);
    }

    /**
     * Retrieve the latest updated_at_server column from rule table.
     *
     * @return latest updated_at_server
     */
    public String getUpdatedAtServerFromRule() {
        String[] projection = {"MAX(" + Database.Rule.UPDATED_AT_SERVER + ") AS " + Database.Rule.UPDATED_AT_SERVER};

        Cursor cursor = contentResolver.query(RULE_URI, projection, null, null, null);
        if (cursor.moveToFirst()) {
            return cursor.getString(cursor.getColumnIndex(Database.Rule.UPDATED_AT_SERVER));
        }
        cursor.close();
        return null;
    }

    /**
     * Retrieve the latest updated_at_server column from action table by a given rule id.
     *
     * @param ruleId rule id
     * @return latest updated_at_server
     */
    public String getUpdatedAtServerFromAction(int ruleId) {
        String[] projection = {"MAX(" + Database.Action.UPDATED_AT_SERVER + ") AS " + Database.Action.UPDATED_AT_SERVER};

        String selection = Database.Action.RULE_ID + " = ?";
        String[] selectionArgs = {"" + ruleId};

        Cursor cursor = contentResolver.query(ACTION_URI, projection, selection, selectionArgs, null);
        if (cursor.moveToFirst()) {
            return cursor.getString(cursor.getColumnIndex(Database.Action.UPDATED_AT_SERVER));
        }
        cursor.close();
        return null;
    }

    /**
     * Checks if the latest result for a given rule is a new one (new content)
     *
     * @param ruleId - rule id
     * @return true if latest result is new
     */
    public boolean isNewResult(int ruleId){
        // set where clause
        String selection = Database.Result.RULE_ID + " = ? AND " + Database.Result.REQUEST_STATE + " = ?";
        String[] selectionArgs = {"" + ruleId, RequestState.DONE.name()};
        String sortOrder = Database.Result.UPDATED_AT + " DESC";

        // projection is build by scrapp provider

        Cursor cursor = contentResolver.query(RESULT_URI, null, selection, selectionArgs, sortOrder);

        // first result for rule
        if(cursor.getCount() == 1){
            return true;
        } else if(cursor.getCount() > 1) {
            cursor.moveToFirst();
            String newHash = cursor.getString(cursor.getColumnIndex(Database.Result.HASH));
            Log.v("DBHelper", cursor.getString(cursor.getColumnIndex(Database.Result.UPDATED_AT)));

            cursor.moveToNext();
            String oldHash = cursor.getString(cursor.getColumnIndex(Database.Result.HASH));
            Log.v("DBHelper", cursor.getString(cursor.getColumnIndex(Database.Result.UPDATED_AT)));

            if ((oldHash == null && newHash!= null) || (newHash != null && oldHash != null && !newHash.equals(oldHash))) {
                return true;
            }
        }
        cursor.close();
        return false;
    }

    /**
     * Sets a result as new.
     *
     * @param internalResultId - internal _id
     */
    public void setResultAsNew(long internalResultId) {
        ContentValues values = new ContentValues();
        values.put(Database.Result.IS_NEW, 1);

        // set where clause
        String selection = Database.Result.ID + " = ?";
        String[] selectionArgs = {"" + internalResultId};

        contentResolver.update(RESULT_URI, values, selection, selectionArgs);
    }
}
