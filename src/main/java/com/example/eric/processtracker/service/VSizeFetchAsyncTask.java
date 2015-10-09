package com.example.eric.processtracker.service;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;

import com.example.eric.processtracker.database.ProcessListDB;
import com.example.eric.processtracker.model.VSizeEntry;

import java.util.ArrayList;

/**
 * AsyncTask for fetching vsize history for given pid.
 */
public class VSizeFetchAsyncTask extends AsyncTask<Integer, Void , ArrayList<VSizeEntry>> {

    public interface Callback{
        void onResult(ArrayList<VSizeEntry> sizes);
    }

    private Context mContext;
    private Callback mCallback;

    public VSizeFetchAsyncTask(Context context, Callback callback) {
        mContext = context;
        mCallback = callback;
    }

    @Override
    protected ArrayList<VSizeEntry> doInBackground(Integer... pid) {
        // append values to db
        SQLiteDatabase db = new ProcessListDB(mContext).getReadableDatabase();
        ArrayList<VSizeEntry> vsizeHistory = new ArrayList<VSizeEntry>();
        db.beginTransaction();
        try {
            Cursor cursor = db.query(
                    true,
                    ProcessListDB.TABLE_PROCESSES,
                    null,
                    ProcessListDB.KEY_PID + " = ?",               // select
                    new String[]{Integer.toString(pid[0])}, // select args
                    null,
                    null,
                    ProcessListDB.KEY_TIME,              // orderby
                    null);
            try {
                int vsizeColIdx = cursor.getColumnIndex(ProcessListDB.KEY_VSIZES);
                int timeColIdx = cursor.getColumnIndex(ProcessListDB.KEY_TIME);
                while (cursor.moveToNext()) {
                    VSizeEntry vsize = new VSizeEntry(
                            cursor.getInt(vsizeColIdx),
                            cursor.getLong(timeColIdx));
                    vsizeHistory.add(vsize);
                }
            } finally {
                cursor.close();
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        db.close();
        return vsizeHistory;
    }

    @Override
    protected void onPostExecute(ArrayList<VSizeEntry> result) {
        if (mCallback == null || isCancelled()) {
            return;
        }
       mCallback.onResult(result);
    }
}