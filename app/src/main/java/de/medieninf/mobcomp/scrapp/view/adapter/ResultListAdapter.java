package de.medieninf.mobcomp.scrapp.view.adapter;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.WebView;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import de.medieninf.mobcomp.scrapp.R;
import de.medieninf.mobcomp.scrapp.database.Database;
import de.medieninf.mobcomp.scrapp.util.RequestState;
import de.medieninf.mobcomp.scrapp.util.Utils;

/**
 * CursorAdapter for a list of results.
 */
public class ResultListAdapter extends CursorAdapter {
    private static final String TAG = ResultListAdapter.class.getSimpleName();

    private LayoutInflater layoutInflater;
    private Context context;

    private WebView resultView;

    public ResultListAdapter(Context context, Cursor c, boolean autoRequery, View resultView) {
        super(context, c, autoRequery);
        layoutInflater = LayoutInflater.from(context);
        this.context = context;

        this.resultView = (WebView) resultView;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return layoutInflater.inflate(R.layout.result_item, parent, false);
    }

    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {
        TextView tvTimeAgo = (TextView) view.findViewById(R.id.tv_time_ago);
        TextView tvAutomatic = (TextView) view.findViewById(R.id.tv_automatic_scrape);
        ProgressBar progressBar = (ProgressBar) view.findViewById(R.id.donut_progress);
        TextView tvProgress = (TextView) view.findViewById(R.id.tv_progress);
        ImageView ivState = (ImageView) view.findViewById(R.id.iv_state);
        TextView tvDate = (TextView) view.findViewById(R.id.tv_date);
        TextView tvTimeOfDay = (TextView) view.findViewById(R.id.tv_time_of_day);
        View timelineCircleIsNew = view.findViewById(R.id.v_timeline_circle_full);

        String status = cursor.getString(cursor.getColumnIndex(Database.Result.REQUEST_STATE));
        if (status.equals(RequestState.PENDING.name())) {
            tvTimeAgo.setText("");
            tvAutomatic.setText("");
            tvDate.setText("");
            tvTimeOfDay.setText("");

            // set progress text
            int currentAction = cursor.getInt(cursor.getColumnIndex(Database.Result.CURRENT_ACTION_NUMBER));
            int actionCount = cursor.getInt(cursor.getColumnIndex(Database.Result.ACTION_COUNT));
            tvProgress.setText(currentAction + "/" + actionCount);

            // set progress bar
            int progress = (int ) ((((float) currentAction) / actionCount) * 100);
            progress = progress == 0 ? 1 : progress;
            progressBar.setProgress(progress);

            // start animation if animation is null
            if (progressBar.getAnimation() == null) {
                Animation rotation = AnimationUtils.loadAnimation(context, R.anim.progress_rotation);
                rotation.setRepeatCount(Animation.INFINITE);
                progressBar.setAnimation(rotation);
            }

            // disable state icon visibility
            ivState.setVisibility(View.INVISIBLE);
        } else {
            // disable animation of progress bar
            if (progressBar.getAnimation() != null) {
                progressBar.getAnimation().cancel();
                progressBar.setAnimation(null);
            }

            // set progress bar invisible
            progressBar.setVisibility(View.INVISIBLE);
            tvProgress.setText("");

            // set pretty date string
            String prettyDate = Utils.getRelativeTimeSpanString(cursor.getString(cursor.getColumnIndex(Database.Result.UPDATED_AT)));
            tvTimeAgo.setText(prettyDate);

            // set date and time texts
            Date date = Utils.convertStringToDate(cursor.getString(cursor.getColumnIndex(Database.Result.UPDATED_AT)));
            SimpleDateFormat format = new SimpleDateFormat("dd/MM", Locale.GERMANY);
            String dayAndMonth = format.format(date);

            format = new SimpleDateFormat("HH:mm", Locale.GERMANY);
            String timeOfDay = format.format(date);

            tvDate.setText(dayAndMonth);
            tvTimeOfDay.setText(timeOfDay + " " + context.getResources().getString(R.string.clock));

            // set automatic scrape text
            boolean automaticScrape = cursor.getInt(cursor.getColumnIndex(Database.Result.AUTOMATIC_SCRAPE)) == 1;
            tvAutomatic.setText(automaticScrape ? R.string.automatic : R.string.manually);

            // set state icon
            ivState.setVisibility(View.VISIBLE);
            if (status.equals(RequestState.DONE.name())) {
                ivState.setImageResource(R.drawable.circle_check_48dp);
            } else { // RequestState == ERROR
                ivState.setImageResource(R.drawable.circle_error_48dp);
            }
        }

        // set timeline circle shape if result is new
        boolean isNew = cursor.getInt(cursor.getColumnIndex(Database.Result.IS_NEW)) == 1;
        if (isNew) {
            timelineCircleIsNew.setVisibility(View.VISIBLE);
        } else {
            timelineCircleIsNew.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * Loads result content into webview considering the count of results.
     * @param resultPosition content to display, if null first result is displayed
     */
    public void displayResult(int resultPosition){
        String resultContent, resultState;
        Cursor cursor = getCursor();

        // no result chlicked and no result yet
        if (resultPosition == -1 && cursor.getCount() == 0){
            resultContent = context.getString(R.string.no_results);
        } else {
            // no result clicked but already result/s existing
            if (resultPosition == -1){
                cursor.moveToFirst();
            } else {    // result clicked
                cursor.moveToPosition(resultPosition);
            }
            resultContent = cursor.getString(cursor.getColumnIndex(Database.Result.CONTENT));
            resultState = cursor.getString(cursor.getColumnIndex(Database.Result.REQUEST_STATE));
            if(resultState.equals(RequestState.PENDING.name())){
                resultContent = context.getString(R.string.scrapp_surfing);
            } else if(resultState.equals(RequestState.ERROR.name())){
                resultContent = context.getString(R.string.error_state);
            }
        }

        // get html template to be filled by result content
        String resultHtml = readFile(context.getString(R.string.html_template_path));
        resultHtml = String.format(resultHtml, resultContent);

        // load html to webview
        resultView.loadDataWithBaseURL(context.getString(R.string.base_url), resultHtml,
                context.getString(R.string.html_type), context.getString(R.string.encoding), null);
    }

    /**
     * Reads file content into string.
     * @param filePath path to file
     * @return string content
     */
    private String readFile(String filePath){
        InputStream input;
        String html = "";
        try {
            input = context.getAssets().open(filePath);
            BufferedReader bf = new BufferedReader(new InputStreamReader(input));
            String line;
            while ((line = bf.readLine()) != null) {
                html += line;
            }
        } catch(IOException i){
            Log.v(TAG, "", i);
        }
        return html;
    }
}