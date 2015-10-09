package com.example.eric.processtracker.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.eric.processtracker.Constants;
import com.example.eric.processtracker.model.ProcessEntry;

import java.util.ArrayList;

/**
 * Broadcast receiver for polling intent from alarm manager
 */
public class ResponseBroadcastReceiver extends BroadcastReceiver {

    public static abstract class Listener {
        public void onProcessListUpdate(ArrayList<ProcessEntry> processList) {}

        public void onProcessListUpdate(Intent intent) {}
    }

    private Listener mListener;

    public void setListener(Listener listener) {
        mListener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals(Constants.ACTION_FETCH_PROCESS_STATS_RESPONSE)) {
            ArrayList<ProcessEntry> processEntries =
                    intent.getParcelableArrayListExtra(Constants.EXTRA_PROCESS_LIST);
            mListener.onProcessListUpdate(processEntries);
            mListener.onProcessListUpdate(intent);
        }
    }
}
