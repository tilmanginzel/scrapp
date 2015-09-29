package de.medieninf.mobcomp.scrapp.database;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

/**
 * ContentProvider to access the database with URIs and ContentResolver.
 */
public class ScrappProvider extends ContentProvider {
    private static final String TAG = ScrappProvider.class.getSimpleName();

    private static final String AUTHORITY = "de.medieninf.mobcomp.provider.scrapp";
    private static final String CONTENT_URI_STRING = "content://" + AUTHORITY;
    public static final Uri CONTENT_URI = Uri.parse(CONTENT_URI_STRING);

    // IDs are used by the uriMatcher to discriminate URIs.
    private static final int RULE = 1;
    private static final int RESULT = 2;
    private static final int ACTION = 3;
    private static final int ACTION_PARAM = 4;

    // UriMatcher
    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        uriMatcher.addURI(AUTHORITY, Database.Rule.TABLE, RULE);
        uriMatcher.addURI(AUTHORITY, Database.Result.TABLE, RESULT);
        uriMatcher.addURI(AUTHORITY, Database.Action.TABLE, ACTION);
        uriMatcher.addURI(AUTHORITY, Database.ActionParam.TABLE, ACTION_PARAM);
    }

    // Database
    private Database database;

    @Override
    public boolean onCreate() {
        database = new Database(getContext());
        database.open();
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        switch (uriMatcher.match(uri)) {
            case RULE:
                Cursor ruleCursor = database.db.query(Database.Rule.TABLE, projection, selection, selectionArgs, null, null, sortOrder);

                // register for changes
                ruleCursor.setNotificationUri(getContext().getContentResolver(), uri);
                return ruleCursor;
            case RESULT:
                // build a custom raw query
                String[] resultProjection = {
                        Database.Result.ID,
                        Database.Result.RULE_ID,
                        Database.Result.RESULT_ID,
                        Database.Result.CONTENT,
                        Database.Result.HASH,
                        Database.Result.REQUEST_STATE,
                        Database.Result.CURRENT_ACTION_NUMBER,
                        Database.Result.ACTION_COUNT,
                        Database.Result.AUTOMATIC_SCRAPE,
                        Database.Result.IS_NEW,
                        "datetime(" + Database.Result.UPDATED_AT + ", 'localtime') AS " + Database.Result.UPDATED_AT
                };

                String sql = "SELECT " + TextUtils.join(", ", resultProjection) +
                             " FROM " + Database.Result.TABLE;
                sql += selection == null ? " " : " WHERE " + selection;
                sql += sortOrder == null ? ";" : " ORDER BY " + sortOrder;

                Cursor resultCursor = database.db.rawQuery(sql, selectionArgs);

                // register for changes
                resultCursor.setNotificationUri(getContext().getContentResolver(), uri);
                return resultCursor;
            case ACTION:
                Cursor actionCursor = database.db.query(Database.Action.TABLE, projection, selection, selectionArgs, null, null, sortOrder);

                // register for changes
                actionCursor.setNotificationUri(getContext().getContentResolver(), uri);
                return actionCursor;
            case ACTION_PARAM:
                Cursor actionParamCursor = database.db.query(Database.ActionParam.TABLE, projection, selection, selectionArgs, null, null, sortOrder);

                // register for changes
                actionParamCursor.setNotificationUri(getContext().getContentResolver(), uri);
                return actionParamCursor;
            default:
                Log.e(TAG, "query: uri not supported " + uri);
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        switch (uriMatcher.match(uri)) {
            case RULE:
                database.db.insert(Database.Rule.TABLE, null, values);
                return null;
            case RESULT:
                long id = database.db.insert(Database.Result.TABLE, null, values);

                // notify loaders about change
                getContext().getContentResolver().notifyChange(uri, null);

                // return uri with id (this id must be used if the parsing fails)
                return uri.buildUpon().appendPath("" + id).build();
            case ACTION:
                database.db.insert(Database.Action.TABLE, null, values);
                return null;
            case ACTION_PARAM:
                database.db.insert(Database.ActionParam.TABLE, null, values);
                return null;
            default:
                Log.e(TAG, "insert: uri not supported " + uri);
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int count;
        switch (uriMatcher.match(uri)) {
            case RESULT:
                count = database.db.delete(Database.Result.TABLE, selection, selectionArgs);

                // notify loaders about change
                getContext().getContentResolver().notifyChange(uri, null);
                return count;
            default:
                Log.e(TAG, "getType: uri not supported " + uri);
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int count;

        switch (uriMatcher.match(uri)) {
            case RULE:
                count = database.db.update(Database.Rule.TABLE, values, selection, selectionArgs);

                // notify loaders about change
                getContext().getContentResolver().notifyChange(uri, null);
                return count;
            case RESULT:
                count = database.db.update(Database.Result.TABLE, values, selection, selectionArgs);

                // notify loaders about change
                getContext().getContentResolver().notifyChange(uri, null);
                return count;
            case ACTION_PARAM:
                return database.db.update(Database.ActionParam.TABLE, values, selection, selectionArgs);
            default:
                Log.e(TAG, "getType: uri not supported " + uri);
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }
}
