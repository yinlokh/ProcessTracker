package com.example.eric.processtracker;

import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.eric.processtracker.database.ProcessListOpenHelper;
import com.example.eric.processtracker.model.VSizeEntry;
import com.example.eric.processtracker.service.ResponseBroadcastReceiver;
import com.example.eric.processtracker.service.VSizeFetchAsyncTask;
import com.example.eric.processtracker.widget.VSizeGraphView;

import java.util.ArrayList;

/**
 * main fragment
 */
public class ProcessSizeFragment extends Fragment {

    public static final String FRAGMENT_TAG = "ProcessSizeFragment";

    public interface Listener {
        void onSizeUpdate(int size);
    }

    private static final String EXTRA_PID = "EXTRA_PID";
    private static final String EXTRA_NAME = "EXTRA_NAME";
    private static final int LOADER_ID = 0;

    private int mPid;
    private LocalBroadcastManager mLocalBroadcastManager;
    private ResponseBroadcastReceiver mResponseBroadcastReceiver;
    private IntentFilter mIntentFilter;
    private View mView;
    private VSizeGraphView mVSizeGraphView;
    private boolean mHasInitialData;
    private Listener mListener;

    public static ProcessSizeFragment newInstance(int pid, String name) {
        Bundle args = new Bundle();
        args.putInt(EXTRA_PID, pid);
        args.putString(EXTRA_NAME, name);

        ProcessSizeFragment fragment = new ProcessSizeFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    public View onCreateView(
            LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        if (mView != null) {
            return mView;
        }

        Bundle args = getArguments();
        mPid = args.getInt(EXTRA_PID);

        mView = inflater.inflate(R.layout.process_size_fragment, container, false);
        mVSizeGraphView = (VSizeGraphView) mView.findViewById(R.id.vsize_graph);
        mVSizeGraphView.setListener(
                new VSizeGraphView.Listener() {
                    @Override
                    public void onSizeUpdate(int size) {
                        if (mListener != null) {
                            mListener.onSizeUpdate(size);
                        }
                    }
                });

        mLocalBroadcastManager = LocalBroadcastManager.getInstance(getContext());
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(Constants.ACTION_FETCH_PROCESS_STATS_RESPONSE);
        mResponseBroadcastReceiver = new ResponseBroadcastReceiver();
        mResponseBroadcastReceiver.setListener(new ResponseBroadcastReceiver.Listener() {
            @Override
            public void onProcessListUpdate(Intent intent) {
                ProcessSizeFragment.this.onProcessListUpdate(intent);
            }
        });

        initializeWithCursorLoader();
//        initializeWIthAsyncTask();
        return mView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }


    @Override
    public void onResume() {
        super.onResume();
        mLocalBroadcastManager.registerReceiver(mResponseBroadcastReceiver, mIntentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        mLocalBroadcastManager.unregisterReceiver(mResponseBroadcastReceiver);
    }

    public void setListener(Listener listener) {
        mListener = listener;
    }

    private void initializeWIthAsyncTask() {
        VSizeFetchAsyncTask asyncTask = new VSizeFetchAsyncTask(
                getContext().getApplicationContext(),
                new VSizeFetchAsyncTask.Callback() {
                    @Override
                    public void onResult(ArrayList<VSizeEntry> sizes) {
                        mVSizeGraphView.setVSizes(sizes);
                        mHasInitialData = true;
                    }
                });
        asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mPid);
    }

    private void initializeWithCursorLoader() {
        getLoaderManager().initLoader(LOADER_ID, null, new CursorLoaderCallback());
    }

    private void onProcessListUpdate(Intent intent) {
        if (mHasInitialData) {
            int vsize = intent.getIntExtra(Integer.toString(mPid), -1);
            long timestamp = intent.getLongExtra(Constants.EXTRA_TIMESTAMP, 0);
            mVSizeGraphView.appendVSize(vsize, timestamp);
        }
    }

    private class CursorLoaderCallback implements LoaderManager.LoaderCallbacks<Cursor> {

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            return new CursorLoader(
                    getActivity(),
                    Uri.parse("content://com.example.eric.processtracker/process_list"),
                    new String[]{ // projection
                            ProcessListOpenHelper.KEY_PID,
                            ProcessListOpenHelper.KEY_TIME,
                            ProcessListOpenHelper.KEY_VSIZES},
                    ProcessListOpenHelper.KEY_PID + "= ?", // selection
                    new String[]{Integer.toString(mPid)}, // selection args
                    ProcessListOpenHelper.KEY_TIME + " ASC");
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
            int vsizeColIdx = cursor.getColumnIndex(ProcessListOpenHelper.KEY_VSIZES);
            int timeColIdx = cursor.getColumnIndex(ProcessListOpenHelper.KEY_TIME);
            ArrayList<VSizeEntry> vsizeHistory = new ArrayList<>();
            while (cursor.moveToNext()) {
                VSizeEntry vsize = new VSizeEntry(
                        cursor.getInt(vsizeColIdx),
                        cursor.getLong(timeColIdx));
                vsizeHistory.add(vsize);
            }
            mVSizeGraphView.setVSizes(vsizeHistory);
            mHasInitialData = true;
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {

        }
    }
}
