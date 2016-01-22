package com.garlicg.tiii.bubble;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;

import com.garlicg.tiii.R;
import com.garlicg.tiii.util.ViewFinder;

import timber.log.Timber;

/**
 */
public class BubbleLayout extends FrameLayout {

    public BubbleLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public BubbleLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public void init(Context context) {
        mSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        mMaxVelocity = ViewConfiguration.get(context).getScaledMaximumFlingVelocity();
        setMotionEventSplittingEnabled(false);
    }


    @Nullable View mBubbleView;
    @Nullable View mTrashCircle;
    @Nullable private OnClickListener mClickListener;

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mBubbleView = ViewFinder.byId(this, R.id.bubble);
        mBubbleView.setOnClickListener(mClickListener);
        mTrashCircle = ViewFinder.byId(this , R.id.trashCircle);
    }

    public void setOnBubbleClickListener(OnClickListener listener) {
        mClickListener = listener;
        if (mBubbleView != null) {
            mBubbleView.setOnClickListener(listener);
        }
    }

    VelocityTracker mVelocityTracker = null;
    private int mSlop;
    private int mMaxVelocity;
    private float mInitialTouchX;
    private float mInitialTouchY;

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        final float x = event.getX();
        final float y = event.getY();
        final int action = event.getAction();

        if (action == MotionEvent.ACTION_DOWN) {
            mInitialTouchX = x;
            mInitialTouchY = y;
        }
        else if (action == MotionEvent.ACTION_MOVE) {
            if (hitBubble(mInitialTouchX, mInitialTouchY)) {
                float xDiff = Math.abs(mInitialTouchX - event.getX());
                float yDiff = Math.abs(mInitialTouchY - event.getY());
                if ((xDiff + yDiff) / 2 > mSlop) {
                    if (mVelocityTracker == null) mVelocityTracker = VelocityTracker.obtain();
                    else mVelocityTracker.clear();
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mBubbleView == null) return false;
        final View target = mBubbleView;

        final float x = event.getX();
        final float y = event.getY();
        final int action = event.getAction();

        if (action == MotionEvent.ACTION_DOWN) {
            Timber.i("ACTION_DOWN " + action);
        }
        else if (event.getAction() == MotionEvent.ACTION_MOVE) {
            Timber.i("ACTION_MOVE " + action);
            mVelocityTracker.addMovement(event);
            final int width = target.getWidth();
            final int height = target.getHeight();
            target.setTranslationX(x - width / 2);
            target.setTranslationY(y - height / 2);

            hitTrashCircle(x , y);
            return true;
        }
        else if(action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP){
            Timber.i("ACTION_OTHER " + action);
            // FIXME 初速をアニメーションに適用できてない。アニメ時間200、Npx/50msはてきとう
            final long animTime = 200;
            mVelocityTracker.computeCurrentVelocity(50, mMaxVelocity);

            // BubbleのX位置をこのViewの右端か左端に調整する
            final int width = target.getWidth();
            float toX = event.getX() + mVelocityTracker.getXVelocity() < getWidth() / 2 ?
                    0 :
                    getWidth() - width;

            // BubbleのY位置をこのViewの範囲内に調整する
            final int height = target.getHeight();
            float toY = (y - height / 2) + mVelocityTracker.getYVelocity();
            if (toY < 0) toY = 0;
            if (toY > getHeight() - height) toY = getHeight() - height;

            target.animate()
                    .translationX(toX)
                    .translationY(toY)
                    .setDuration(animTime)
                    .setInterpolator(new OvershootInterpolator())
                    .start();

            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
        return false;
    }

    boolean hitBubble(float x, float y) {
        if (mBubbleView == null) return false;
        final View target = mBubbleView;
        boolean hitX = x > target.getX() && x < target.getX() + target.getWidth();
        boolean hitY = y > target.getY() && y < target.getY() + target.getHeight();
        return hitX && hitY;
    }

    boolean hitTrashCircle(float x, float y) {
        if (mTrashCircle == null) return false;
        final View target = mTrashCircle;
        float scaleSlop = 1.6f;
        float areaSlopX = (target.getWidth() * scaleSlop - target.getWidth()) / 2;
        float areaSlopY = (target.getHeight() * scaleSlop - target.getHeight()) / 2;
        boolean hitX = x > target.getLeft() - areaSlopX && x < target.getRight() + areaSlopX;
        boolean hitY = y > target.getTop() - areaSlopY && y < target.getBottom() + areaSlopY;
        boolean hit = hitX && hitY;
        target.setScaleX(hit ? scaleSlop : 1f);
        target.setScaleY(hit ? scaleSlop : 1f);
        return hit;
    }

}
