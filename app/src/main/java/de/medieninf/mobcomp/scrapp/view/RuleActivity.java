package de.medieninf.mobcomp.scrapp.view;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.medieninf.mobcomp.scrapp.R;
import de.medieninf.mobcomp.scrapp.database.DBHelper;
import de.medieninf.mobcomp.scrapp.database.Database;
import de.medieninf.mobcomp.scrapp.rest.model.Action;
import de.medieninf.mobcomp.scrapp.rest.model.ActionParam;
import de.medieninf.mobcomp.scrapp.rest.service.RestServiceHelper;
import de.medieninf.mobcomp.scrapp.util.RequestState;
import de.medieninf.mobcomp.scrapp.util.TimeUtil;
import de.medieninf.mobcomp.scrapp.util.Utils;
import de.medieninf.mobcomp.scrapp.util.TimeUtil.*;
import de.medieninf.mobcomp.scrapp.view.loader.ActionWithParamsLoader;
import de.medieninf.mobcomp.scrapp.view.widget.ExpandablePanel;
import se.simbio.encryption.Encryption;

import com.fourmob.datetimepicker.date.DatePickerDialog;
import com.fourmob.datetimepicker.date.DatePickerDialog.OnDateSetListener;
import com.sleepbot.datetimepicker.time.RadialPickerLayout;
import com.sleepbot.datetimepicker.time.TimePickerDialog;


/**
 * This activity shows all details for a given rule id.
 * Additionally, it is possible to configure all action params and to subscribe this rule.
 */
public class RuleActivity extends AppCompatActivity implements OnDateSetListener, TimePickerDialog.OnTimeSetListener  {
    private static final String TAG = RuleActivity.class.getSimpleName();

    private static final String DATE_TAG = "datepicker";
    private static final String TIME_TAG = "timepicker";

    public static final String EXTRA_RULE_ID = "rule_id";
    private RestServiceHelper restServiceHelper;

    private int ruleId;
    private boolean subscribed;
    private Date startTime;

    private List<ActionParamEditText> actionParamEditTexts;
    private LinearLayout llActionsWrapper, llTimeWrapper;
    private Switch wifiSwitch;
    private NumberPicker entityPicker, intervalPicker;
    private TextView tvDate, tvTime, tvEntity, tvInterval;

    private ProgressBar pbLoadActions;
    private Menu menu;

    // ids for loader callbacks, should be unique
    private int RULE_LOADER_ID = 1;
    private int ACTION_LOADER_ID = 2;
    private int ACTION_WITH_PARAMS_LOADER_ID = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rule);

        llActionsWrapper = (LinearLayout) findViewById(R.id.ll_actions);
        wifiSwitch = (Switch) findViewById(R.id.wifi_switch);
        llTimeWrapper = (LinearLayout) findViewById(R.id.ll_timeslot);

        pbLoadActions = (ProgressBar) findViewById(R.id.pb_load_actions);
        pbLoadActions.setVisibility(View.VISIBLE);

        actionParamEditTexts = new ArrayList<>();
        restServiceHelper = new RestServiceHelper(this);
        ruleId = getIntent().getIntExtra(EXTRA_RULE_ID, -1);
        subscribed = false;

        // init action loader
        ActionLoaderCallbacks actionLoaderCallbacks = new ActionLoaderCallbacks();
        getSupportLoaderManager().initLoader(ACTION_LOADER_ID, null, actionLoaderCallbacks);

        // init the toolbar
        initToolbar();

        // init pickers (time config)
        this.startTime = new Date();
        initFrequencyPickers();

        // request actions and action params
        restServiceHelper.getRuleWithActions(ruleId);
    }

    /**
     * Set the title and description.
     */
    private void initTitleAndActivity(Cursor cursor) {
        // set title
        TextView tvTitle = (TextView) findViewById(R.id.tv_rule_title);
        tvTitle.setText(cursor.getString(cursor.getColumnIndex(Database.Rule.TITLE)));

        // Set expandable panel listener
        final ExpandablePanel panel = (ExpandablePanel) findViewById(R.id.ep_rule_description);
        TextView tvDescription = (TextView) findViewById(R.id.value);
        tvDescription.setText(cursor.getString(cursor.getColumnIndex(Database.Rule.DESCRIPTION)));
        panel.setOnExpandListener(new ExpandablePanel.OnExpandListener() {
            public void onCollapse(View handle, View content) {
                ImageView expand = (ImageView) handle;
                expand.setImageResource(R.drawable.ic_keyboard_arrow_down_black_24dp);
            }

            public void onExpand(View handle, View content) {
                ImageView expand = (ImageView) handle;
                expand.setImageResource(R.drawable.ic_keyboard_arrow_up_black_24dp);
            }
        });

        boolean wifiOnly = cursor.getInt(cursor.getColumnIndex(Database.Rule.WIFI_ONLY)) == 1;
        wifiSwitch.setChecked(wifiOnly);

        // time config can firstly be instantiated here; values from database needed
        setIntervalView(cursor);
        setStartTimeView();
    }

    /**
     * Creates a linear layout for an action and adds it to the activity.
     * @param action - action
     */
    private void addViewForAction(Action action) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        LinearLayout actionWrapper = (LinearLayout) inflater.inflate(R.layout.list_view_action_item, llActionsWrapper, false);

        int padding = getResources().getDimensionPixelOffset(R.dimen.action_padding);
        actionWrapper.setPadding(padding, padding, padding, padding);

        // set title
        TextView tvTitle = (TextView) actionWrapper.findViewById(R.id.tv_action_title);
        tvTitle.setText((action.getPosition() + 1) + ". " + action.getTitle());

        // set method
        TextView tvMethod = (TextView) actionWrapper.findViewById(R.id.tv_action_method);
        tvMethod.setText(action.getMethod().toUpperCase());

        // set parse type
        TextView tvParseType = (TextView) actionWrapper.findViewById(R.id.tv_action_parse_type);
        tvParseType.setText(action.getParseType() == null ? "" : action.getParseType().toUpperCase());

        // set url
        TextView tvUrl = (TextView) actionWrapper.findViewById(R.id.tv_action_url);
        if (action.getUrl() == null || action.getUrl().length() == 0) {
            tvUrl.setText("wird in Aktion "+ action.getPosition() + " ermittelt"); // TODO: remove hardcoded string
        } else {
            tvUrl.setText(action.getUrl());
        }

        // set parse expression
        TextView tvParseExpression = (TextView) actionWrapper.findViewById(R.id.tv_action_expression);
        tvParseExpression.setText(action.getParseExpression() == null ? "-" : action.getParseExpression());

        // set display expression
        LinearLayout llDisplayExpression = (LinearLayout) actionWrapper.findViewById(R.id.ll_display_expression);
        if (action.getParseExpressionDisplay() == null || action.getParseExpressionDisplay().length() == 0) {
            llDisplayExpression.setVisibility(View.GONE);
        } else {
            TextView tvDisplayExpression = (TextView) actionWrapper.findViewById(R.id.tv_display_expression);
            tvDisplayExpression.setText(action.getParseExpressionDisplay());
        }

        // add input for action param
        if (action.getActionParams() != null) {
            LinearLayout llActionParams = (LinearLayout) actionWrapper.findViewById(R.id.ll_action_params);
            for (ActionParam param : action.getActionParams()) {
                if (!param.getType().equals("invisible")) {
                    llActionParams.addView(getActionParamView(param, inflater, llActionParams));
                }
            }
        }



        // add divider on top
        View divider = inflater.inflate(R.layout.action_divider, llActionsWrapper, false);
        llActionsWrapper.addView(divider);

        llActionsWrapper.addView(actionWrapper);
    }

    /**
     * Creates a linear layout for a specific action param.
     *
     * @param param - action param
     * @param inflater - layout inflater
     * @param parent - parent view
     * @return linear layout
     */
    private View getActionParamView(ActionParam param, LayoutInflater inflater, ViewGroup parent) {
        LinearLayout llActionParam = (LinearLayout) inflater.inflate(R.layout.action_param_item, parent, false);

        // set title
        TextView tvParamTitle = (TextView) llActionParam.findViewById(R.id.tv_action_param_title);
        tvParamTitle.setText(param.getTitle());

        // set type
        TextView tvParamType = (TextView) llActionParam.findViewById(R.id.tv_action_param_type);
        tvParamType.setText(param.getType().toUpperCase());

        // disable required *
        if (!param.getRequired()) {
            TextView tvRequired = (TextView) llActionParam.findViewById(R.id.tv_required);
            tvRequired.setVisibility(View.INVISIBLE);
        }

        // set the proper edit text
        LinearLayout inputWrapper = (LinearLayout) llActionParam.findViewById(R.id.ll_action_param_input);
        ActionParamEditText editText = null;
        if (param.getType().equals("string")) {
            editText = getStringInput(inputWrapper, param.getActionParamId());
        } else if (param.getType().equals("password")) {
            editText = getPasswordInput(inputWrapper, param.getActionParamId());
        }

        // add edit text to list and view
        if (editText != null) {
            editText.setText(param.getValue());
            // set line color
            editText.getBackground().setColorFilter(getResources().getColor(R.color.secondary), PorterDuff.Mode.SRC_ATOP);
            actionParamEditTexts.add(editText);
            inputWrapper.addView(editText);
        }

        return llActionParam;
    }

    /**
     * Creates an edit text with standard text input.
     *
     * @param parent - parent view
     * @param actionParamId - action param id
     * @return edit text
     */
    private ActionParamEditText getStringInput(LinearLayout parent, int actionParamId) {
        ActionParamEditText editText = new ActionParamEditText(this, actionParamId);
        editText.setLayoutParams(parent.getLayoutParams());
        return editText;
    }

    /**
     * Creates an edit text with input type set to password.
     *
     * @param parent - parent view
     * @param actionParamId - action param id
     * @return edit text
     */
    private ActionParamEditText getPasswordInput(LinearLayout parent, int actionParamId) {
        ActionParamEditText et = getStringInput(parent, actionParamId);
        et.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        return et;
    }

    /**
     * Saves all configuration components in the database in another Thread.
     * These are all action params, wifi only flag and time params.
     */
    private void saveConfigurationParams() {
        new Thread() {
            @Override
            public void run() {
                DBHelper dbHelper = new DBHelper(RuleActivity.this);

                // update time slot and start time and proof time config change
                int interval = TimeUtil.calculateInterval(entityPicker.getValue(), intervalPicker.getValue());
                boolean timeConfigChanged = dbHelper.updateTimeConfiguration(ruleId, interval, startTime);

                // test if time config has changed and rule is already subscribed
                if(timeConfigChanged && dbHelper.isRuleSubscribed(ruleId)){
                    // update time config on server
                    restServiceHelper.updateSubscription(ruleId);
                }

                // update wifi only
                dbHelper.updateWifiOnly(ruleId, wifiSwitch.isChecked());

                // get encryption instance only once, not for each action param
                Encryption encryption = Utils.getEncryption();
                for(ActionParamEditText et : actionParamEditTexts) {
                    dbHelper.updateActionParam(encryption, et.getActionParamId(), et.getText().toString());
                }
            }
        }.start();
    }

    /**
     * Init toolbar.
     */
    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.rule_activity_title);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    /**
     * Toggle the toolbars icon.
     *
     * @param data - cursor data
     */
    private void toggleToolbarIcon(Cursor data) {
        String requestState = data.getString(data.getColumnIndex(Database.Rule.REQUEST_STATE));
        if(requestState.equals(RequestState.PENDING.name())) {
            // show indeterminate loading icon
            menu.getItem(0).setActionView(R.layout.toolbar_indeterminate_progress);
        } else {
            menu.getItem(0).setActionView(null);

            boolean subscribed = data.getInt(data.getColumnIndex(Database.Rule.SUBSCRIBED)) == 1;
            RuleActivity.this.subscribed = subscribed;
            if (!subscribed) {
                // show subscribe button
                menu.getItem(0).setIcon(R.drawable.ic_add_white_24dp);
            } else {
                // show save button
                menu.getItem(0).setIcon(R.drawable.ic_save_white_24dp);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.rule_actions, menu);

        this.menu = menu;
        menu.getItem(0).setActionView(R.layout.toolbar_indeterminate_progress);

        // init rule loader
        RuleLoaderCallbacks ruleLoaderCallbacks = new RuleLoaderCallbacks();
        getSupportLoaderManager().initLoader(RULE_LOADER_ID, null, ruleLoaderCallbacks);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_subscribe_or_save:
                saveConfigurationParams();

                if (!subscribed) {
                    restServiceHelper.createSubscription(ruleId);
                }

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Loader callbacks for a rule.
     *
     * It is used to show different icons for different states (e.g. subscribed),
     * and to show the title and description of the rule.
     */
    private class RuleLoaderCallbacks implements LoaderManager.LoaderCallbacks<Cursor> {

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            DBHelper dbHelper = new DBHelper(RuleActivity.this);
            return dbHelper.getRuleLoader(ruleId);
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            if(!data.moveToFirst()){
                return;
            }

            // set title and description
            initTitleAndActivity(data);

            // toggle toolbar icon
            toggleToolbarIcon(data);
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) { }
    }

    /**
     * Loader callbacks for a CursorLoader with all actions for the a rule id.
     * This is only used to observe the actions in the database.
     *
     * On successful onLoadFinished(), another loader is forced to reload all actions
     * with action params.
     */
    private class ActionLoaderCallbacks implements LoaderManager.LoaderCallbacks<Cursor> {

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            DBHelper dbHelper = new DBHelper(RuleActivity.this);
            return dbHelper.getActionLoader(ruleId);
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            ActionWithParamsLoaderCallbacks actionWithParamsLoaderCallbacks = new ActionWithParamsLoaderCallbacks();

            // force load to get all action with action params
            getSupportLoaderManager().restartLoader(ACTION_WITH_PARAMS_LOADER_ID, null,
                    actionWithParamsLoaderCallbacks).forceLoad();
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) { }
    }

    /**
     * Loader callbacks to get all actions with action params.
     * The view gets inflated in onLoadFinished()
     */
    private class ActionWithParamsLoaderCallbacks implements LoaderManager.LoaderCallbacks<List<Action>> {

        @Override
        public Loader<List<Action>> onCreateLoader(int id, Bundle args) {
            return new ActionWithParamsLoader(RuleActivity.this, ruleId);
        }

        @Override
        public void onLoadFinished(Loader<List<Action>> loader, List<Action> data) {
            pbLoadActions.setVisibility(View.GONE);

            // clear all action views (should be empty in almost all cases)
            llActionsWrapper.removeAllViews();

            // add views for all actions
            for(Action a : data) {
                addViewForAction(a);
            }
        }

        @Override
        public void onLoaderReset(Loader<List<Action>> loader) { }
    }

    /*
     * Picker Stuff
     */

    /**
     * Initialize pickers for entity and interval.
     */
    private void initFrequencyPickers(){
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.time_config, llTimeWrapper, true);

        tvEntity = (TextView) findViewById(R.id.tv_entity);
        tvInterval = (TextView) findViewById(R.id.tv_interval);

        // create custom dialog
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.frequency));
        View layout = inflater.inflate(R.layout.interval_dialog, null);
        builder.setView(layout);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked OK button
                dialog.dismiss();

                // set text to text views
                tvEntity.setText(String.valueOf(ENTITY.values()[entityPicker.getValue()].toString()));
                tvInterval.setText(String.valueOf(intervalPicker.getValue()));
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
                dialog.cancel();
            }
        });

        final AlertDialog dialog = builder.create();

        LinearLayout llInterval = (LinearLayout) findViewById(R.id.ll_interval);
        llInterval.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.show();
            }
        });

        // TODO: vorgeschlagener Interval vom Server holen und setzen
        // Frequency pickers
        final ENTITY [] entities = ENTITY.values();
        entityPicker = (NumberPicker) layout.findViewById(R.id.entity_picker);
        entityPicker.setMinValue(0);
        entityPicker.setMaxValue(entities.length - 1);
        entityPicker.setDisplayedValues(ENTITY.getStringValues());
        // forbid editing the text
        entityPicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        entityPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                intervalPicker.setMinValue(TimeUtil.MIN);
                intervalPicker.setMaxValue(entities[newVal].getMaxValue());
            }
        });

        intervalPicker = (NumberPicker) layout.findViewById(R.id.interval_picker);
        intervalPicker.setMinValue(TimeUtil.MIN);
        intervalPicker.setMaxValue(TimeUtil.MIN);
        intervalPicker.setWrapSelectorWheel(false);
        intervalPicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
    }

    private void setIntervalView(Cursor cursor){
        // retrieve interval and start time from DB
        int interval = cursor.getInt(cursor.getColumnIndex(Database.Rule.INTERVAL));
        startTime.setTime(cursor.getLong(cursor.getColumnIndex(Database.Rule.START_TIME)));

        // init time config pickers with values
        ENTITY entity = TimeUtil.getEntity(interval);
        tvEntity.setText(entity.toString());
        try {
            entityPicker.setValue(entity.ordinal());

            int value = TimeUtil.reCalculateInterval(interval, entity);
            tvInterval.setText(String.valueOf(value));
            intervalPicker.setMinValue(TimeUtil.MIN);
            intervalPicker.setMaxValue(entity.getMaxValue());
            intervalPicker.setValue(value);
            intervalPicker.setWrapSelectorWheel(true);
        } catch(NullPointerException e){
            Log.e(TAG, e.getMessage(), e);
        }
    }

    /**
     * Initialize pickers for date and time.
     */
    private void setStartTimeView(){
        // date and time pickers
        final Calendar cal = Calendar.getInstance();

        final DatePickerDialog datePickerDialog = DatePickerDialog.newInstance(this,
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH), true);
        final TimePickerDialog timePickerDialog = TimePickerDialog.newInstance(this,
                cal.get(Calendar.HOUR_OF_DAY) , cal.get(Calendar.MINUTE), false, false);

        tvDate = (TextView) findViewById(R.id.tv_date);
        setDateText();
        tvDate.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                datePickerDialog.setVibrate(true);
                datePickerDialog.setYearRange(1985, 2028);
                datePickerDialog.setCloseOnSingleTapDay(false);
                datePickerDialog.show(getSupportFragmentManager(), DATE_TAG);
            }
        });

        tvTime = (TextView) findViewById(R.id.tv_time);
        setTimeText();
        tvTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timePickerDialog.setVibrate(true);
                timePickerDialog.setCloseOnSingleTapMinute(false);
                timePickerDialog.show(getSupportFragmentManager(), TIME_TAG);
            }
        });

        DatePickerDialog dpd = (DatePickerDialog) getSupportFragmentManager().findFragmentByTag(DATE_TAG);
        if (dpd != null) {
            dpd.setOnDateSetListener(this);
        }

        TimePickerDialog tpd = (TimePickerDialog) getSupportFragmentManager().findFragmentByTag(TIME_TAG);
        if (tpd != null) {
            tpd.setOnTimeSetListener(this);
        }
    }

    /**
     * Sets actual time to time text view.
     */
    private void setTimeText(){
        SimpleDateFormat format = new SimpleDateFormat(TimeUtil.TIME_PATTERN, Locale.GERMANY);
        tvTime.setText(format.format(startTime.getTime()));
    }

    /**
     * Sets actual date to date text view.
     */
    private void setDateText(){
        SimpleDateFormat format = new SimpleDateFormat(TimeUtil.DATE_PATTERN, Locale.GERMANY);
        tvDate.setText(format.format(startTime.getTime()));
    }

    @Override
    public void onDateSet(DatePickerDialog datePickerDialog, int year, int monthOfY, int dayOfM) {
        // pre save date
        preStoreTimeConfig(year, monthOfY, dayOfM, null, null);
        setDateText();
    }

    @Override
    public void onTimeSet(RadialPickerLayout radialPickerLayout, int hourOfD, int minute) {
        // pre save time
        preStoreTimeConfig(null, null, null, hourOfD, minute);
        setTimeText();
    }

    /**
     * Method to save changed time or date values locally until they are finally
     * stored into the database.
     * @param year year
     * @param month month
     * @param day day
     * @param hour hour
     * @param minute minute
     */
    private void preStoreTimeConfig(Integer year, Integer month, Integer day, Integer hour, Integer minute){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startTime);

        int y = year == null? calendar.get(Calendar.YEAR) : year;
        int mon = month == null?calendar.get(Calendar.MONTH) : month;
        int d = day == null? calendar.get(Calendar.DAY_OF_MONTH) : day;
        int h = hour == null? calendar.get(Calendar.HOUR) : hour;
        int min = minute == null? calendar.get(Calendar.MINUTE) : minute;

        calendar.set(y, mon, d, h, min);
        this.startTime = calendar.getTime();
    }
}