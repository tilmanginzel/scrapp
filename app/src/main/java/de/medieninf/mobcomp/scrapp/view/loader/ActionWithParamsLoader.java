package de.medieninf.mobcomp.scrapp.view.loader;

/**
 * Loader for actions with their params.
 */

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

import java.util.List;

import de.medieninf.mobcomp.scrapp.database.DBHelper;
import de.medieninf.mobcomp.scrapp.rest.model.Action;

/**
 * Custom loader to get all action with action params from the database.
 * The data source is _not_ observed and changes won't be recognized here.
 */
public class ActionWithParamsLoader extends AsyncTaskLoader<List<Action>> {
    private DBHelper dbHelper;
    private int ruleId;

    public ActionWithParamsLoader(Context context, int ruleId) {
        super(context);
        this.dbHelper = new DBHelper(context);
        this.ruleId = ruleId;
    }

    @Override
    public List<Action> loadInBackground() {
        return dbHelper.getActionsForParsing(ruleId);
    }
}
