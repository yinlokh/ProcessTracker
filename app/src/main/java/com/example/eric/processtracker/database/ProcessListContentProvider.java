package com.example.eric.processtracker.database;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

/**
 * ContentProvider for {@link ProcessListOpenHelper}
 */
public class ProcessListContentProvider extends ContentProvider {

    private static final String AUTHORITY_URI = "com.example.eric.processtracker";
    private static final String PATH_PROCESS_LIST = "process_list";
    private static final int CODE_PROCESS_LIST = 1;

    private static final UriMatcher sUriMatcher = new UriMatcher(0);
    static {
        sUriMatcher.addURI(AUTHORITY_URI, PATH_PROCESS_LIST, CODE_PROCESS_LIST);
    }

    private ProcessListOpenHelper mProcessListOpenHelper;

    @Override
    public boolean onCreate() {
        mProcessListOpenHelper = new ProcessListOpenHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Log.e("yinlokh", "received query");
        SQLiteDatabase db = mProcessListOpenHelper.getReadableDatabase();
        Cursor c = db.query(ProcessListOpenHelper.TABLE_PROCESSES, projection, selection, selectionArgs, null, null, sortOrder);
        return c;
    }

    @Override
    public String getType(Uri uri) {
        Log.e("yinlokh", "received gettype");
        if (sUriMatcher.match(uri) == CODE_PROCESS_LIST) {
            return "PROCESS_LIST";
        }
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }
}
