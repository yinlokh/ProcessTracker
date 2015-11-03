package com.example.eric.processtracker.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Sqlite db for storing processes polled by this app
 */
public class ProcessListOpenHelper extends SQLiteOpenHelper {

    public static final String TABLE_PROCESSES = "processes";
    public static final String KEY_PID = "pid";
    public static final String KEY_NAME = "name";
    public static final String KEY_VSIZES = "vsizes";
    public static final String KEY_TIME = "time";

    private static final int VERSION = 2;
    private static final String DATABASE_NAME = "ProcessListDatabase";

    public ProcessListOpenHelper(
            Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_PROCESSES +
            "(" +
                KEY_PID + " INTEGER ," +
                KEY_TIME + " INTEGER ," +
                KEY_NAME + " TEXT," +
                KEY_VSIZES + " INTEGER," +
                "PRIMARY KEY (" + KEY_PID + "," + KEY_TIME + ")" +
            ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // nothing
    }
}
