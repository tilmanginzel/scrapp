package de.medieninf.mobcomp.scrapp.view.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.LinearLayout;

import de.medieninf.mobcomp.scrapp.R;

/**
 * Custom Widget for expandable panels.
 */
public class ExpandablePanel extends LinearLayout {

    private final int mHandleId;
    private final int mContentId;

    // contains references to the handle and content views
    private View mHandle;
    private View mContent;

    // start expanded or not
    private boolean mExpanded = false;
    // height of content when collapsed
    private int mCollapsedHeight = 0;
    // full expanded height of the content
    private int mContentHeight = 0;
    // time of expand animation
    private int mAnimationDuration = 0;

    // listener for onExpand and onCollapse
    private OnExpandListener mListener;

    /**
     * Constructor to set the context.
     * @param context context
     */
    public ExpandablePanel(Context context) {
        this(context, null);
    }

    /**
     * The constructor simply validates the arguments being passed in and
     * sets the global variables accordingly. Required attributes are
     * 'handle' and 'content'
     */
    public ExpandablePanel(Context context, AttributeSet attrs) {
        super(context, attrs);
        mListener = new DefaultOnExpandListener();

        TypedArray a = context.obtainStyledAttributes(
                attrs, R.styleable.ExpandablePanel, 0, 0);

        // content height in collapsed state
        mCollapsedHeight = (int) a.getDimension(
                R.styleable.ExpandablePanel_collapsedHeight, 0.0f);

        // time of animation
        mAnimationDuration = a.getInteger(
                R.styleable.ExpandablePanel_animationDuration, 500);

        int handleId = a.getResourceId(
                R.styleable.ExpandablePanel_handle, 0);

        if (handleId == 0) {
            throw new IllegalArgumentException(
                    "The handle attribute is required and must refer "
                            + "to a valid child.");
        }

        int contentId = a.getResourceId(
                R.styleable.ExpandablePanel_content, 0);
        if (contentId == 0) {
            throw new IllegalArgumentException(
                    "The content attribute is required and must " +
                            "refer to a valid child.");
        }

        mHandleId = handleId;
        mContentId = contentId;

        a.recycle();
    }

    /**
     * Listener if panel expands.
     * @param listener listener
     */
    public void setOnExpandListener(OnExpandListener listener) {
        mListener = listener;
    }

    /**
     * Setter for collapsed height.
     * @param collapsedHeight collapsed height
     */
    public void setCollapsedHeight(int collapsedHeight) {
        mCollapsedHeight = collapsedHeight;
    }

    /**
     * Setter for animation time
     * @param animationDuration animation duration
     */
    public void setAnimationDuration(int animationDuration) {
        mAnimationDuration = animationDuration;
    }

    /**
     * This method gets called when the View is physically
     * visible to the user
     */
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mHandle = findViewById(mHandleId);
        boolean visible = mHandle.getVisibility() == View.VISIBLE;
        int height = mHandle.getLayoutParams().height;
        int width = mHandle.getLayoutParams().width;
        if (mHandle == null) {
            throw new IllegalArgumentException(
                    "The handle attribute is must refer to an"
                            + " existing child.");
        }

        mContent = findViewById(mContentId);
        if (mContent == null) {
            throw new IllegalArgumentException(
                    "The content attribute must refer to an"
                            + " existing child.");
        }

        // This changes the height of the content such that it
        // starts off collapsed
        android.view.ViewGroup.LayoutParams lp =
                mContent.getLayoutParams();
        lp.height = mCollapsedHeight;
        mContent.setLayoutParams(lp);

        // Set the OnClickListener of the handle view
        mHandle.setOnClickListener(new PanelToggler());
    }

    /**
     * This is where the magic happens for measuring the actual
     * (un-expanded) height of the content. If the actual height
     * is less than the collapsedHeight, the handle will be hidden.
     */
    @Override
    protected void onMeasure(int widthMeasureSpec,
                             int heightMeasureSpec) {
        // First, measure how high content wants to be
        mContent.measure(widthMeasureSpec, MeasureSpec.UNSPECIFIED);
        mContentHeight = mContent.getMeasuredHeight();

        if (mContentHeight < mCollapsedHeight) {
            mHandle.setVisibility(View.GONE);
        } else {
            mHandle.setVisibility(View.VISIBLE);
        }

        // Then let the usual thing happen
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    /**
     * This is the on click listener for the handle.
     * It basically just creates a new animation instance and fires
     * animation.
     */
    private class PanelToggler implements OnClickListener {
        public void onClick(View v) {
            Animation a;
            if (mExpanded) {
                a = new ExpandAnimation(mContentHeight, mCollapsedHeight);
                mListener.onCollapse(mHandle, mContent);
            } else {
                a = new ExpandAnimation(mCollapsedHeight, mContentHeight);
                mListener.onExpand(mHandle, mContent);
            }
            a.setDuration(mAnimationDuration);
            mContent.startAnimation(a);
            mExpanded = !mExpanded;
        }
    }

    /**
     * This is a private animation class that handles the expand/collapse
     * animations. It uses the animationDuration attribute for the length
     * of time it takes.
     */
    private class ExpandAnimation extends Animation {
        private final int mStartHeight;
        private final int mDeltaHeight;

        public ExpandAnimation(int startHeight, int endHeight) {
            mStartHeight = startHeight;
            mDeltaHeight = endHeight - startHeight;
        }

        @Override
        protected void applyTransformation(float interpolatedTime,
                                           Transformation t) {
            android.view.ViewGroup.LayoutParams lp =
                    mContent.getLayoutParams();
            lp.height = (int) (mStartHeight + mDeltaHeight *
                    interpolatedTime);
            mContent.setLayoutParams(lp);
        }

        @Override
        public boolean willChangeBounds() {
            return true;
        }
    }

    /**
     * Simple OnExpandListener interface
     */
    public interface OnExpandListener {
        void onExpand(View handle, View content);
        void onCollapse(View handle, View content);
    }

    private class DefaultOnExpandListener implements OnExpandListener {
        public void onCollapse(View handle, View content) {}
        public void onExpand(View handle, View content) {}
    }
}

