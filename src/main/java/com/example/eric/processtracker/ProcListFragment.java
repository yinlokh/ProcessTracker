package com.example.eric.processtracker;

import android.content.Context;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.eric.processtracker.model.ProcessEntry;
import com.example.eric.processtracker.service.ResponseBroadcastReceiver;
import com.example.eric.processtracker.widget.InteractionFrameLayout;
import com.example.eric.processtracker.widget.ProcessListView;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Fragment to show a list of processes
 */
public class ProcListFragment extends Fragment {

    public static final String FRAGMENT_TAG = "ProcListFragment";

    private View mView;
    private InteractionFrameLayout mInteractionFrameLayout;
    private ProcessListView mProcessListView;
    private ResponseBroadcastReceiver mBroadcastReceiver;
    private Listener mListener;

    public interface Listener {
        void onProcessSelected(int pid, String name);

        void onResume();
    }

    public void setListener(Listener listener) {
        mListener = listener;
    }

    public void setProcessList(ArrayList<ProcessEntry> processEntries) {
        Collections.sort(processEntries);
        mProcessListView.setProcessList(processEntries);
    }

    @Override
    public void onCreate(Bundle savedInstancestate) {
        super.onCreate(savedInstancestate);
        mBroadcastReceiver = new ResponseBroadcastReceiver();
        mBroadcastReceiver.setListener(new ResponseBroadcastReceiver.Listener() {
            @Override
            public void onProcessListUpdate(ArrayList<ProcessEntry> processEntries) {
                if (mInteractionFrameLayout.isInteracting()) {
                    return;
                }
                setProcessList(processEntries);
            }
        });
    }

    @Nullable
    public View onCreateView(
            LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        if (mView == null) {
            mView = inflater.inflate(R.layout.process_list_fragment, container, false);
            mInteractionFrameLayout = (InteractionFrameLayout) mView.findViewById(R.id.frame);
            mProcessListView = (ProcessListView) mView.findViewById(R.id.process_list);
            mProcessListView.setListener(
                    new ProcessListView.Listener() {
                        @Override
                        public void onProcessSelected(ProcessEntry processEntry) {
                            if (mListener != null) {
                                mListener.onProcessSelected(processEntry.pid, processEntry.name);
                            }
                        }
                    });
        }
        return mView;
    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter psFilter = new IntentFilter(Constants.ACTION_FETCH_PROCESS_STATS_RESPONSE);
        getContext().registerReceiver(mBroadcastReceiver, psFilter);
        if (mListener != null) {
            mListener.onResume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        getContext().unregisterReceiver(mBroadcastReceiver);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }
}
