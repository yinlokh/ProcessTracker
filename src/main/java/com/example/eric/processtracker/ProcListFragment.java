package com.example.eric.processtracker;

import android.content.Context;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.eric.processtracker.model.ProcessEntry;
import com.example.eric.processtracker.service.ResponseBroadcastReceiver;
import com.example.eric.processtracker.widget.InteractionFrameLayout;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Fragment (red one)
 */
public class ProcListFragment extends Fragment {

    public static final String FRAGMENT_TAG = "ProcListFragment";

    private View mView;
    private InteractionFrameLayout mInteractionFrameLayout;
    private ResponseBroadcastReceiver mBroadcastReceiver;
    private RecyclerView mProcessListRecycler;
    private ProcessListAdapter mProcessListAdapter = new ProcessListAdapter();
    private ArrayList<ProcessEntry> mProcessList;
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
        mProcessList = processEntries;
        mProcessListAdapter.notifyDataSetChanged();
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
            mProcessListRecycler = (RecyclerView) mView.findViewById(R.id.recycler);
            mProcessListRecycler.setAdapter(mProcessListAdapter);
            mProcessListRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
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

    // Adapter for recycler we are showing
    private class ProcessListAdapter extends RecyclerView.Adapter<ProcessEntryVH> {

        @Override
        public ProcessEntryVH onCreateViewHolder(ViewGroup viewGroup, int i) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            View view = inflater.inflate(R.layout.process_list_item, viewGroup, false);
            return new ProcessEntryVH(view);
        }

        @Override
        public void onBindViewHolder(ProcessEntryVH holder, int i) {
            ProcessEntry processEntry = mProcessList.get(i);
            final int pid = processEntry.pid;
            final String name = processEntry.name;
            holder.name.setText(processEntry.name);
            holder.vsize.setText(Integer.toString(processEntry.vsize));
            holder.itemView.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (mListener == null) {
                                return ;
                            }
                            mListener.onProcessSelected(pid, name);
                        }
                    });
        }

        @Override
        public int getItemCount() {
            return mProcessList != null ? mProcessList.size() : 0;
        }
    }

    // ViewHolder for process entry item
    private static class ProcessEntryVH extends RecyclerView.ViewHolder {

        public final TextView name;
        public final TextView vsize;
        public ProcessEntryVH(View itemView) {
            super(itemView);
            name = (TextView)itemView.findViewById(R.id.process_name);
            vsize = (TextView)itemView.findViewById(R.id.vsize);
        }
    }
}
