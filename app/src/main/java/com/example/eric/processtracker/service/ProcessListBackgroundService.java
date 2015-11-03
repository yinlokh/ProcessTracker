package com.example.eric.processtracker.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;

import com.example.eric.processtracker.Constants;
import com.example.eric.processtracker.MainActivity;
import com.example.eric.processtracker.model.ProcessEntry;
import com.example.eric.processtracker.database.ProcessListOpenHelper;

import java.util.ArrayList;

/**
 * Service for background fetching of process list
 */
public class ProcessListBackgroundService extends Service {

    private ProcessListOpenHelper mProcessListOpenHelper;
    private ProcessListFetcher mProcessListFetcher;
    private HandlerThread mHandlerThread;
    private Handler mHandler;
    private PollingRunnable mPollingRunnable;

    @Override
    public void onCreate() {
        mProcessListOpenHelper = new ProcessListOpenHelper(getApplicationContext());
        mProcessListFetcher = new ProcessListFetcher();
        mPollingRunnable = new PollingRunnable();
        mHandlerThread = new HandlerThread("PROCESS_LIST_HANDLER_THREAD");
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper());
        mHandler.post(mPollingRunnable);

        Intent activityIntent = new Intent(getApplicationContext(), MainActivity.class);
        activityIntent.setAction(Intent.ACTION_MAIN);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(
                        getApplicationContext(),
                        0,
                        activityIntent,
                        PendingIntent.FLAG_CANCEL_CURRENT);
        Notification notification = new NotificationCompat.Builder(getApplicationContext())
                .setSmallIcon(android.R.drawable.ic_menu_camera)
                .setContentText("Polling process info, press to open app")
                .setContentIntent(pendingIntent)
                .build();
        NotificationManager notificationManager =
                (NotificationManager) getApplicationContext().getSystemService(
                        Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, notification);
        startForeground(1, notification);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        mHandler.removeCallbacks(mPollingRunnable);
        mHandlerThread.quit();
    }

    private void fetchProcessList() {
        long timestampMs = System.currentTimeMillis();
        ArrayList<ProcessEntry> processEntries = mProcessListFetcher.fetchProcesses();

        // append values to db
        SQLiteDatabase db = mProcessListOpenHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            for (ProcessEntry processEntry : processEntries) {
                ContentValues contentValues = new ContentValues();
                contentValues.put(ProcessListOpenHelper.KEY_NAME, processEntry.name);
                contentValues.put(ProcessListOpenHelper.KEY_PID, processEntry.pid);
                contentValues.put(ProcessListOpenHelper.KEY_VSIZES, processEntry.vsize);
                contentValues.put(ProcessListOpenHelper.KEY_TIME, timestampMs);
                db.insert(ProcessListOpenHelper.TABLE_PROCESSES, null, contentValues);
            }

            // clean up old data
            db.delete(
                    ProcessListOpenHelper.TABLE_PROCESSES,
                    ProcessListOpenHelper.KEY_TIME + " <= ?",
                    new String[]{Long.toString(timestampMs - Constants.MAX_TIME_HISTORY_MS)});
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        db.close();
        broadcastChanges(processEntries, timestampMs);
    }

    private void broadcastChanges(ArrayList<ProcessEntry> processEntries, long timestampMs) {
        Intent resultIntent = new Intent();
        resultIntent.putParcelableArrayListExtra(Constants.EXTRA_PROCESS_LIST, processEntries);
        resultIntent.putExtra(Constants.EXTRA_TIMESTAMP, timestampMs);

        // Also put vsize values keyed using pid to allow process size fragment lookup
        for (ProcessEntry processEntry : processEntries) {
            resultIntent.putExtra(Integer.toString(processEntry.pid), processEntry.vsize);
        }
        resultIntent.setAction(Constants.ACTION_FETCH_PROCESS_STATS_RESPONSE);
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);
        localBroadcastManager.sendBroadcast(resultIntent);
    }

    /**
     * Runnable to be used with handler thread, will retrigger itself every second.
     */
    private class PollingRunnable implements Runnable {

        @Override
        public void run() {
            fetchProcessList();
            mHandler.postDelayed(this, Constants.POLLING_INTERVAL_MS);
        }
    }
}
