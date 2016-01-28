package com.garlicg.screenrecordct.practice;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.garlicg.screenrecordct.R;
import com.garlicg.screenrecordct.util.DisplayUtils;
import com.garlicg.screenrecordct.util.ViewFinder;


/**
 * 上位レイヤーだとボタン以外のタッチは拾えないので使えない子
 */
@Deprecated
public class MagnetLayout extends FrameLayout {

    private boolean mTrashEnable = true;
    private boolean mQuiting = false;

    public MagnetLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public MagnetLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public void init(Context context) {
        mSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        mMaxVelocity = ViewConfiguration.get(context).getScaledMaximumFlingVelocity();
        setMotionEventSplittingEnabled(false);
    }


    @Nullable
    View mMagnetCircleView;
    @Nullable
    View mTrashView;

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mMagnetCircleView = ViewFinder.byId(this, R.id.magnetCircle);
        mMagnetCircleView.setOnClickListener(mClickListener);
        mTrashView = ViewFinder.byId(this, R.id.trash);
        hideTrashToBottom(true);
    }

    ////////////////////////
    // Magnet Click Listener

    @Nullable
    private OnClickListener mClickListener;

    public interface OnMagnetClickListener {
        /**
         * @param magnetCircle 丸のフレームView
         * @param magnetImage  丸内の画像 アニメーションするときに丸枠に対しての座標になるので便利
         */
        void onMagnetClick(View magnetCircle, ImageView magnetImage);
    }

    public void setOnBubbleClickListener(final OnMagnetClickListener listener) {
        mClickListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onMagnetClick(v, (ImageView) ViewFinder.byId(v, R.id.magnetImage));
            }
        };
        if (mMagnetCircleView != null) {
            mMagnetCircleView.setOnClickListener(mClickListener);
        }
    }


    ////////////////////////
    // Lifecycle Listener

    @Nullable
    private OnMagnetEventListener mEventListener;

    public interface OnMagnetEventListener{
        void onMagnetQuit();
    }

    public void setOnMagnetEventListener(OnMagnetEventListener listener){
        mEventListener = listener;
    }


    //////////////////
    // 処理

    VelocityTracker mVelocityTracker = null;
    private int mSlop;
    private int mMaxVelocity;
    private float mInitialTouchX;
    private float mInitialTouchY;

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (mQuiting) return false;

        final float x = event.getX();
        final float y = event.getY();
        final int action = event.getAction();

        if (action == MotionEvent.ACTION_DOWN) {
            mInitialTouchX = x;
            mInitialTouchY = y;
        } else if (action == MotionEvent.ACTION_MOVE) {

            if (hitBubble(mInitialTouchX, mInitialTouchY)) {
                float xDiff = Math.abs(mInitialTouchX - event.getX());
                float yDiff = Math.abs(mInitialTouchY - event.getY());
                if ((xDiff + yDiff) / 2 > mSlop) {

                    // ゴミ箱を表示する
                    if(mTrashEnable){
                        showTrashFromBottom();
                    }

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
        if (mQuiting) return false;
        if (mMagnetCircleView == null) return false;
        final View target = mMagnetCircleView;

        final float x = event.getX();
        final float y = event.getY();
        final int action = event.getAction();

        if (action == MotionEvent.ACTION_DOWN) {
            // none
        }
        else if (event.getAction() == MotionEvent.ACTION_MOVE) {
            mVelocityTracker.addMovement(event);
            final int width = target.getWidth();
            final int height = target.getHeight();
            target.setTranslationX(x - width / 2);
            target.setTranslationY(y - height / 2);

            if(mTrashEnable){
                hitTrashCircle(x, y);
            }
            return true;
        }
        else if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
            // FIXME 初速をアニメーションに適用できてない。アニメ時間200、Npx/50msはてきとう
            final long animTime = 200;
            mVelocityTracker.computeCurrentVelocity(50, mMaxVelocity);

            // おしまい
            if (mTrashEnable && hitTrashCircle(x, y)) {
                mQuiting = true;
                dismissHitTrash();
            }
            // 継続
            else {
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

                // ゴミ箱を隠す
                hideTrashToBottom(false);
            }

            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
        return false;
    }

    boolean hitBubble(float x, float y) {
        if (mMagnetCircleView == null) return false;
        final View target = mMagnetCircleView;
        boolean hitX = x > target.getX() && x < target.getX() + target.getWidth();
        boolean hitY = y > target.getY() && y < target.getY() + target.getHeight();
        return hitX && hitY;
    }

    boolean hitTrashCircle(float x, float y) {
        if (mTrashView == null) return false;
        final View target = mTrashView;
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

    void showTrashFromBottom() {
        if (mTrashView == null) return;
        mTrashView.animate()
                .translationY(0)
                .setDuration(200)
                .setInterpolator(new DecelerateInterpolator())
                .start();
    }

    void hideTrashToBottom(boolean immediate) {
        if (mTrashView == null) return;
        float toY = DisplayUtils.dpToPx(mTrashView.getContext().getResources(), 100);
        if (immediate) {
            mTrashView.setTranslationY(toY);
        } else {
            mTrashView.animate()
                    .translationY(toY)
                    .setDuration(200)
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .start();
        }
    }

    void dismissHitTrash() {
        if (mMagnetCircleView == null || mTrashView == null) {
            throw new IllegalStateException("View is null.");
        }
        mTrashView.animate()
                .scaleY(0f)
                .scaleX(0f)
                .setInterpolator(new AnticipateInterpolator())
                .setDuration(200)
                .start();
        mMagnetCircleView.animate()
                .scaleX(0f)
                .scaleY(0f)
                .setInterpolator(new AccelerateInterpolator())
                .setDuration(200)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        if (mEventListener != null) {
                            mEventListener.onMagnetQuit();
                        }
                    }
                })
                .start();

    }

    public void setTrashEnable(boolean enable){
        mTrashEnable = enable;
    }
}
