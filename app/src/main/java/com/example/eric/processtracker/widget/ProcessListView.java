package com.example.eric.processtracker.widget;

import android.animation.ValueAnimator;
import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.OnScrollListener;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.example.eric.processtracker.R;
import com.example.eric.processtracker.model.ProcessEntry;

import java.util.ArrayList;

/**
 * Custom view to show a list of processes
 */
public class ProcessListView extends FrameLayout {

    public interface Listener {

        void onProcessSelected(ProcessEntry processEntry);
    }

    private static final int SEARCH_BOX_ANIMATOR_DELAY_MS = 100;

    private RecyclerView mProcessListRecycler;
    private ProcessListAdapter mProcessListAdapter = new ProcessListAdapter();
    private ArrayList<ProcessEntry> mProcessList;
    private ArrayList<ProcessEntry> mFilteredProcessList;
    private View mSearchFrame;
    private EditText mSearchBox;
    private LinearLayoutManager mLinearLayoutManager;
    private int mSearchFrameHeight;
    private ValueAnimator mSearchBoxAnimator;
    private Button mClearButton;
    private Listener mListener;

    public ProcessListView(Context context) {
        super(context);
        init();
    }

    public ProcessListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ProcessListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View view = inflater.inflate(R.layout.process_list_view, this);
        mSearchFrameHeight =
                getResources().getDimensionPixelSize(R.dimen.search_bar_height)
                        + getResources().getDimensionPixelSize(R.dimen.search_bar_margin);
        mSearchFrame = view.findViewById(R.id.search_frame);
        mSearchBox = (EditText) view.findViewById(R.id.search_box);
        mClearButton = (Button) view.findViewById(R.id.clear_button);
        mLinearLayoutManager = new LinearLayoutManager(getContext());
        mProcessListRecycler = (RecyclerView) view.findViewById(R.id.recycler);
        mProcessListRecycler.setAdapter(mProcessListAdapter);
        mProcessListRecycler.setLayoutManager(mLinearLayoutManager);
        mProcessListRecycler.setOnScrollListener(
                new OnScrollListener() {

                    @Override
                    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                        super.onScrolled(recyclerView, dx, dy);
                        setSearchBoxTranslation(dy);
                    }

                    @Override
                    public void onScrollStateChanged(RecyclerView recyclerView, int newState){
                        if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                            return;
                        }
                        boolean pastHalfway =
                                mSearchFrame.getTranslationY() > -mSearchFrameHeight / 2;
                        boolean isAtTop =
                                mLinearLayoutManager.findFirstVisibleItemPosition() == 0;
                        startSearchBoxTranslationAnimation(pastHalfway || isAtTop);
                    }
                });
        mProcessListRecycler.setOnTouchListener(
                new OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        if (mSearchBox.hasFocus()) {
                            stopSearch();
                            return true;
                        }
                        return false;
                    }
                });

        mSearchBox.addTextChangedListener(
                new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        mFilteredProcessList = filterResults();
                        mProcessListAdapter.notifyDataSetChanged();

                        if (s.length() == 0) {
                            mClearButton.setVisibility(GONE);
                        } else {
                            mClearButton.setVisibility(VISIBLE);
                        }
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                    }
                });
        mSearchBoxAnimator = new ValueAnimator();
        mSearchBoxAnimator.setDuration(SEARCH_BOX_ANIMATOR_DELAY_MS);
        mSearchBoxAnimator.addUpdateListener(
                new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        Float value = (Float) animation.getAnimatedValue();
                        updateSearchBoxTranslation(value);
                    }
                });

        mClearButton.setOnClickListener(
                new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mSearchBox.setText("");
                    }
                });
    }

    public void setListener(Listener listener) {
        mListener = listener;
    }

    public void setProcessList(ArrayList<ProcessEntry> processList) {
        mProcessList = processList;
        mFilteredProcessList = filterResults();
        mProcessListAdapter.notifyDataSetChanged();
    }

    private void stopSearch() {
        mSearchBox.clearFocus();
        InputMethodManager inputMethodManager =
                (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(ProcessListView.this.getWindowToken(), 0);
    }

    private ArrayList<ProcessEntry> filterResults() {
        ArrayList<ProcessEntry> filtered = new ArrayList<ProcessEntry>();
        if (mProcessList == null) {
            return filtered;
        }

        for (ProcessEntry processEntry : mProcessList) {
            if (processEntry.name.contains(mSearchBox.getText())) {
                filtered.add(processEntry);
            }
        }

        return filtered;
    }

    private void setSearchBoxTranslation(int dy) {
        float minTranslationY = -1 * getSearchBoxHeight();
        float currentTranslationY = mSearchFrame.getTranslationY() - dy;
        float newTranslationY = Math.max(minTranslationY, currentTranslationY);
        newTranslationY = Math.min(0, newTranslationY);
        mSearchFrame.setTranslationY(newTranslationY);
    }

    private float getSearchBoxHeight() {
        LayoutParams lp = (LayoutParams) mSearchFrame.getLayoutParams();
        return mSearchFrame.getHeight() + lp.topMargin + lp.bottomMargin;
    }

    private void startSearchBoxTranslationAnimation(boolean isExpand) {
        float start = mSearchFrame.getTranslationY() / mSearchFrameHeight;
        float end = isExpand ? 0.f : -1.f;
        mSearchBoxAnimator.cancel();
        mSearchBoxAnimator.setFloatValues(start, end);
        mSearchBoxAnimator.start();
    }

    private void updateSearchBoxTranslation(float animatorValue) {
        mSearchFrame.setTranslationY(animatorValue * mSearchFrameHeight);
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
            final ProcessEntry processEntry = mFilteredProcessList.get(i);
            holder.name.setText(processEntry.name);
            holder.vsize.setText(Integer.toString(processEntry.vsize));
            holder.itemView.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (mListener == null) {
                                return ;
                            }

                            stopSearch();
                            mListener.onProcessSelected(processEntry);
                        }
                    });
        }

        @Override
        public int getItemCount() {
            return mFilteredProcessList != null ? mFilteredProcessList.size() : 0;
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
