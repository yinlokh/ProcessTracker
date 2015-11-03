package com.example.eric.processtracker.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.example.eric.processtracker.Constants;
import com.example.eric.processtracker.R;
import com.example.eric.processtracker.model.VSizeEntry;

import java.util.ArrayList;

/**
 * View to display vsize graph
 */
public class VSizeGraphView extends View {

    public interface Listener {
        void onSizeUpdate(int size);
    }

    private static final int GRID_SPACING = 100;

    private Listener mListener;
    private ArrayList<VSizeEntry> mVSizes;

    // ui related stuff
    private Paint mBackgroundPaint;
    private Paint mLinePaint;
    private Paint mMarkerPaint;
    private Paint mDatapointPaint;
    private Paint mMinorGridPaint;
    private Paint mMajorGridPaint;
    private float mMarkerPositionX;

    public VSizeGraphView(Context context) {
        super(context);
        init();
    }

    public VSizeGraphView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public VSizeGraphView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mBackgroundPaint = new Paint();
        mBackgroundPaint.setColor(getResources().getColor(R.color.graph_background_color));
        mBackgroundPaint.setStyle(Paint.Style.FILL);

        mLinePaint = new Paint();
        mLinePaint.setColor(getResources().getColor(R.color.graph_line_color));
        mLinePaint.setStrokeWidth(3);

        mMarkerPaint = new Paint();
        mMarkerPaint.setColor(getResources().getColor(R.color.graph_marker_color));
        mMarkerPaint.setStrokeWidth(1);
        mMarkerPaint.setTextSize(30);

        mDatapointPaint = new Paint();
        mDatapointPaint.setColor(getResources().getColor(R.color.graph_datapoint_color));
        mDatapointPaint.setStyle(Paint.Style.FILL);

        mMinorGridPaint = new Paint();
        mMinorGridPaint.setColor(getResources().getColor(R.color.graph_grid_color));
        mMinorGridPaint.setStrokeWidth(2);

        mMajorGridPaint = new Paint();
        mMajorGridPaint.setColor(getResources().getColor(R.color.graph_major_grid_color));
        mMajorGridPaint.setStrokeWidth(3);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mMarkerPositionX = (int) event.getX();
        invalidate();
        return true;
    }

    public void setListener(Listener listener) {
        mListener = listener;
    }

    public void setVSizes(ArrayList<VSizeEntry> vsizes) {
        mVSizes = vsizes;
        invalidate();
    }

    public void appendVSize(int vsize, long timestamp) {
        if (vsize >= 0) {
            mVSizes.add(new VSizeEntry(vsize,timestamp));
            long minTimestamp = timestamp - Constants.MAX_TIME_HISTORY_MS;
            while (mVSizes.size() > 0 && mVSizes.get(0).timestamp < minTimestamp) {
                mVSizes.remove(0);
            }
            invalidate();
        }
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        mMarkerPositionX = mMarkerPositionX < 0
                ? 0
                : mMarkerPositionX;
        mMarkerPositionX = mMarkerPositionX > canvas.getWidth()
                ? canvas.getWidth()
                : mMarkerPositionX;
        canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), mBackgroundPaint);

        int axis_y = (int) (canvas.getHeight() * 0.9);
        int min_y = (int) (canvas.getHeight() * 0.8);

        for (int x = 0; x < canvas.getWidth(); x += GRID_SPACING) {
            canvas.drawLine(x, 0, x, canvas.getHeight(), mMinorGridPaint);
        }
        for (int y = axis_y; y >= 0; y -= GRID_SPACING) {
            canvas.drawLine(0, y, canvas.getWidth(), y, mMinorGridPaint);
        }

        if (mVSizes != null && !mVSizes.isEmpty()) {
            float xPerMs = (float) canvas.getWidth() / Constants.MAX_TIME_HISTORY_MS;
            int vertical_space = (int) (canvas.getHeight() * 0.7);
            int vsizeMax = mVSizes.get(0).vsize;
            int vsizeMin = mVSizes.get(0).vsize;
            long minTimestampMs = mVSizes.get(0).timestamp;

            // first pass to check range that we need to display
            for (VSizeEntry vsize : mVSizes) {
                vsizeMax = vsizeMax > vsize.vsize ? vsizeMax : vsize.vsize;
                vsizeMin = vsizeMin < vsize.vsize ? vsizeMin : vsize.vsize;
            }

            // second pass to render the graph
            long markerTimestamp = (long) (mMarkerPositionX / xPerMs) + minTimestampMs;
            int closestToMarker = 0;
            long minDistanceToMarker = Constants.MAX_TIME_HISTORY_MS;
            float yMultiplier = (float) vertical_space / (vsizeMax - vsizeMin + 100);
            for (int i = 1; i < mVSizes.size(); i++) {
                VSizeEntry currentVSize = mVSizes.get(i);
                VSizeEntry lastVSize = mVSizes.get(i-1);

                long distanceToMarker = Math.abs(currentVSize.timestamp - markerTimestamp);
                if (distanceToMarker < minDistanceToMarker) {
                    closestToMarker = i;
                    minDistanceToMarker = distanceToMarker;
                }

                float lastY = min_y - (lastVSize.vsize - vsizeMin) * yMultiplier;
                float lastX = (lastVSize.timestamp - minTimestampMs) * xPerMs;
                float curY = min_y - (currentVSize.vsize - vsizeMin) * yMultiplier;
                float curX = (currentVSize.timestamp - minTimestampMs) * xPerMs;
                canvas.drawLine(
                        lastX,
                        lastY,
                        curX,
                        curY,
                        mLinePaint);
            }

            // draw marker
            VSizeEntry closest = mVSizes.get(closestToMarker);
            int markerVSize = closest.vsize;
            int markerHeight = (int) (min_y - (markerVSize - vsizeMin) * yMultiplier);
            canvas.drawLine(mMarkerPositionX, 0, mMarkerPositionX, canvas.getHeight(), mMarkerPaint);
            canvas.drawLine(0, markerHeight, canvas.getWidth(), markerHeight, mMarkerPaint);

            float curY = min_y - (closest.vsize - vsizeMin) * yMultiplier;
            float curX = (closest.timestamp - minTimestampMs) * xPerMs;
            canvas.drawRect(curX - 5, curY - 5, curX + 5, curY + 5, mDatapointPaint);
            if (mListener != null) {
                mListener.onSizeUpdate(markerVSize);
            }
        }

        canvas.drawLine(0, axis_y, canvas.getWidth(), axis_y, mMajorGridPaint);
    }
}
