package de.medieninf.mobcomp.scrapp.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Create all database tables.
 */
public class Database {

    private static final String DATABASE_NAME = "scrapp.db";
    private static final int DATABASE_VERSION = 1;

    public SQLiteDatabase db;
    private DBHelper dbHelper;

    public Database(Context context) {
        this.dbHelper = new DBHelper(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Helper class to
     * - create tables
     * - open and close the database connection
     * - perform database version upgrades
     */
    private static class DBHelper extends SQLiteOpenHelper {
        public DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            // create tables
            db.execSQL(Rule.CREATE_TABLE);
            db.execSQL(Result.CREATE_TABLE);
            db.execSQL(Action.CREATE_TABLE);
            db.execSQL(ActionParam.CREATE_TABLE);

            // create update timestamp trigger
            db.execSQL(getUpdateTimestampTrigger(Rule.TABLE, Rule.UPDATED_AT));
            db.execSQL(getUpdateTimestampTrigger(Result.TABLE, Result.UPDATED_AT));
            db.execSQL(getUpdateTimestampTrigger(Action.TABLE, Action.UPDATED_AT));
            db.execSQL(getUpdateTimestampTrigger(ActionParam.TABLE, ActionParam.UPDATED_AT));
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            throw new UnsupportedOperationException("Not implemented.");
        }
    }

    /**
     * Open database connection.
     * @throws SQLiteException
     */
    public void open() throws SQLiteException {
        try {
            db = dbHelper.getWritableDatabase();
        } catch (SQLiteException e) {
            db = dbHelper.getReadableDatabase();
        }
    }

    /**
     * Table Rule data.
     */
    public class Rule {
        public static final String TABLE = "rule";

        // columns
        public static final String ID = "_id";
        public static final String RULE_ID = "rule_id";
        public static final String TITLE = "title";
        public static final String DESCRIPTION = "description";
        public static final String SUBSCRIBED = "subscribed";
        public static final String START_TIME = "start_time";
        public static final String INTERVAL = "interval";
        public static final String WIFI_ONLY = "wifi_only";
        public static final String REQUEST_STATE = "request_state";
        public static final String CREATED_AT = "created_at";
        public static final String UPDATED_AT = "updated_at";
        public static final String UPDATED_AT_SERVER = "updated_at_server";

        // create table
        private static final String CREATE_TABLE =
                "CREATE TABLE " + TABLE + "("
                + ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + RULE_ID + " INTEGER NOT NULL UNIQUE, "
                + TITLE + " TEXT NOT NULL, "
                + DESCRIPTION + " TEXT NOT NULL, "
                + SUBSCRIBED + " INTEGER NOT NULL DEFAULT 0, "
                + START_TIME + " TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, "
                + INTERVAL + " INTEGER NOT NULL DEFAULT 0, "
                + WIFI_ONLY + " INTEGER NOT NULL DEFAULT 0, "
                + REQUEST_STATE + " TEXT, "
                + CREATED_AT + " TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, "
                + UPDATED_AT + " TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, "
                + UPDATED_AT_SERVER + " TEXT "
                + ");";
    }

    /**
     * Table for Action data.
     */
    public class Action {
        public static final String TABLE = "action";

        // columns
        public static final String ID = "_id";
        public static final String ACTION_ID = "action_id";
        public static final String TITLE = "title";
        public static final String RULE_ID = "rule_id";
        public static final String POSITION = "position";
        public static final String METHOD = "method";
        public static final String URL = "url";
        public static final String PARSE_EXPRESSION = "parse_expression";
        public static final String PARSE_TYPE = "parse_type";
        public static final String PARSE_EXPRESSION_DISPLAY = "parse_expression_display";
        public static final String PARSE_TYPE_DISPLAY = "parse_type_display";
        public static final String CREATED_AT = "created_at";
        public static final String UPDATED_AT = "updated_at";
        public static final String UPDATED_AT_SERVER = "updated_at_server";

        // create table
        private static final String CREATE_TABLE =
                "CREATE TABLE " + TABLE + "("
                        + ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                        + ACTION_ID + " INTEGER NOT NULL UNIQUE, "
                        + TITLE + " TEXT, "
                        + RULE_ID + " INTEGER NOT NULL, "
                        + POSITION + " INTEGER NOT NULL, "
                        + METHOD + " TEXT NOT NULL, "
                        + URL + " TEXT, "
                        + PARSE_EXPRESSION + " TEXT, "
                        + PARSE_TYPE + " TEXT, "
                        + PARSE_EXPRESSION_DISPLAY + " TEXT, "
                        + PARSE_TYPE_DISPLAY + " TEXT, "
                        + CREATED_AT + " TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, "
                        + UPDATED_AT + " TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, "
                        + UPDATED_AT_SERVER + " TEXT, "
                        + "FOREIGN KEY(" + RULE_ID + ") REFERENCES " + Rule.TABLE + "(" + Rule.RULE_ID + ")"
                        + ");";
    }

    /**
     * Table for Action_Param data.
     */
    public class ActionParam {
        public static final String TABLE = "action_param";

        // columns
        public static final String ID = "_id";
        public static final String ACTION_PARAM_ID = "action_param_id";
        public static final String ACTION_ID = "action_id";
        public static final String TITLE = "title";
        public static final String KEY = "key";
        public static final String VALUE = "value";
        public static final String TYPE = "type";
        public static final String REQUIRED = "required";
        public static final String CREATED_AT = "created_at";
        public static final String UPDATED_AT = "updated_at";
        public static final String UPDATED_AT_SERVER = "updated_at_server";

        // create table
        private static final String CREATE_TABLE =
                "CREATE TABLE " + TABLE + "("
                        + ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                        + ACTION_PARAM_ID + " INTEGER NOT NULL UNIQUE, "
                        + ACTION_ID + " INTEGER NOT NULL, "
                        + TITLE + " TEXT, "
                        + KEY + " TEXT NOT NULL, "
                        + VALUE + " TEXT, "
                        + TYPE + " TEXT NOT NULL, "
                        + REQUIRED + " INTEGER, "
                        + CREATED_AT + " TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, "
                        + UPDATED_AT + " TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, "
                        + UPDATED_AT_SERVER + " TEXT, "
                        + "FOREIGN KEY(" + ACTION_ID + ") REFERENCES " + Action.TABLE + "(" + Action.ACTION_ID + ")"
                        + ");";
    }

    /**
     * Table for result data.
     */
    public class Result {
        public static final String TABLE = "result";

        // columns
        public static final String ID = "_id";
        public static final String RESULT_ID = "result_id";
        public static final String RULE_ID = "rule_id";
        public static final String CONTENT = "content";
        public static final String HASH = "hash";
        public static final String REQUEST_STATE = "request_state";
        public static final String CURRENT_ACTION_NUMBER = "current_action_number";
        public static final String ACTION_COUNT = "action_count";
        public static final String AUTOMATIC_SCRAPE = "automatic_scrape";
        public static final String IS_NEW = "is_new";
        public static final String CREATED_AT = "created_at";
        public static final String UPDATED_AT = "updated_at";

        // create table
        private static final String CREATE_TABLE =
                "CREATE TABLE " + TABLE + "("
                + ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + RESULT_ID + " INTEGER, "
                + RULE_ID + " INTEGER NOT NULL, "
                + CONTENT + " TEXT, "
                + HASH + " TEXT, "
                + REQUEST_STATE + " TEXT, "
                + CURRENT_ACTION_NUMBER + " INTEGER DEFAULT 0, "
                + ACTION_COUNT + " INTEGER DEFAULT 0, "
                + AUTOMATIC_SCRAPE + " INTEGER DEFAULT 0, "
                + IS_NEW + " INTEGER DEFAULT 0, "
                + CREATED_AT + " TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, "
                + UPDATED_AT + " TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, "
                + "FOREIGN KEY(" + RULE_ID + ") REFERENCES " + Rule.TABLE + "(" + Rule.RULE_ID + ")"
                + ");";
    }

    /**
     * Creates a sql statement which creates a trigger to update the updated_at column.
     *
     * @param table table
     * @param column column
     * @return trigger string
     */
    private static String getUpdateTimestampTrigger(String table, String column) {
        return  "CREATE TRIGGER update_" + table + "_timestamp_trigger "
                + "AFTER UPDATE ON " + table + " FOR EACH ROW "
                + "BEGIN "
                + "UPDATE " + table
                + " SET " + column + " = CURRENT_TIMESTAMP"
                + " WHERE _id = old._id;"
                + "END;";
    }
}
