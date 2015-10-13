package com.example.eric.processtracker;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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

    private int mPid;
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

        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(Constants.ACTION_FETCH_PROCESS_STATS_RESPONSE);
        mResponseBroadcastReceiver = new ResponseBroadcastReceiver();
        mResponseBroadcastReceiver.setListener(new ResponseBroadcastReceiver.Listener() {
            @Override
            public void onProcessListUpdate(Intent intent) {
                ProcessSizeFragment.this.onProcessListUpdate(intent);
            }
        });

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
        return mView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }


    @Override
    public void onResume() {
        super.onResume();
        getContext().registerReceiver(mResponseBroadcastReceiver, mIntentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        getContext().unregisterReceiver(mResponseBroadcastReceiver);
    }

    public void setListener(Listener listener) {
        mListener = listener;
    }

    private void onProcessListUpdate(Intent intent) {
        if (mHasInitialData) {
            int vsize = intent.getIntExtra(Integer.toString(mPid), -1);
            long timestamp = intent.getLongExtra(Constants.EXTRA_TIMESTAMP, 0);
            mVSizeGraphView.appendVSize(vsize, timestamp);
        }
    }
}
