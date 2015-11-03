package com.example.eric.processtracker.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;

/**
 * FrameLayout that will detect when user interacts with elements in container.
 */
public class InteractionFrameLayout extends FrameLayout {

    private boolean mIsInteracting;

    public InteractionFrameLayout(Context context) {
        super(context);
    }

    public InteractionFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public InteractionFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void requestDisallowInterceptTouchEvent(boolean disallow) {
        // we want to track touch events always
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        int action = event.getActionMasked();
        switch(action) {
            case MotionEvent.ACTION_UP:
                mIsInteracting = false;
                break;
            case MotionEvent.ACTION_DOWN:
                mIsInteracting = true;
                break;
        }
        return super.dispatchTouchEvent(event);
    }

    public boolean isInteracting() {
        return mIsInteracting;
    }
}
