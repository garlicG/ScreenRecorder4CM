package com.garlicg.tiii.magnet;

import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.widget.FrameLayout;

import com.garlicg.tiii.R;
import com.garlicg.tiii.util.DisplayUtils;

import timber.log.Timber;

/**
 */
public class MagnetView extends FrameLayout{

    private static final String KEY_X = "x";
    private static final String KEY_Y = "y";
    private static final OvershootInterpolator OVER_SHOOT_INTERPOLATOR = new OvershootInterpolator();

    private WindowManager mWindowManager;
    private int mTouchYDiff = 0;
    VelocityTracker mVelocityTracker = null;
    private int mMaxVelocity;
    private int mSlop;
    private DecorDummy mDecorDummy;
    private float mInitialTouchX;
    private float mInitialTouchY;


    @SuppressLint("RtlHardcoded")
    public static WindowManager.LayoutParams createWindowParams(){
        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PRIORITY_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                        | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
//                        | WindowManager.LayoutParams.FLAG_FULLSCREEN
//                        | WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR
                , PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.LEFT | Gravity.TOP;
        return params;
    }


    public MagnetView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }


    public MagnetView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }


    public void init(Context context){
        mWindowManager = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
        mSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        mMaxVelocity = ViewConfiguration.get(context).getScaledMaximumFlingVelocity();
    }


    public void setDecorDummy(DecorDummy decorDummy){
        mDecorDummy = decorDummy;
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        if(oldw == 0 && w > 0){
            // 右上に配置
            WindowManager.LayoutParams lp = (WindowManager.LayoutParams) getLayoutParams();
            lp.x = DisplayUtils.getWindowSize(getContext()).x - w;
            mWindowManager.updateViewLayout(this, lp);

            ScaleAnimation scale = new ScaleAnimation(0f, 1f, 0f, 1f , w/2 , h/2);
            scale.setDuration(300);
            scale.setInterpolator(new DecelerateInterpolator());
            RotateAnimation rotate = new RotateAnimation(-180 , 0 , w/2 , h/2);
            rotate.setDuration(300);
            rotate.setInterpolator(new DecelerateInterpolator());
            AnimationSet set = new AnimationSet(false);
            set.addAnimation(scale);
            set.addAnimation(rotate);
            findViewById(R.id.magnetFrame).startAnimation(set);
        }
    }


    //////////////////
    // View位置の補完

    private final Point mDisplaySizeCache = new Point();
    private final Point mWindowSizeCache = new Point();
    private final PointF mTouchPointCache = new PointF();

    private void updateWindowSize(){
        // ステータス分のずれがある場合はタッチをズラす
//        Timber.i("dl:" + mDecorDummy.getHeight());
        mWindowManager.getDefaultDisplay().getSize(mDisplaySizeCache);

        mWindowSizeCache.x = mDecorDummy.getWidth();
        mWindowSizeCache.y = mDecorDummy.getHeight();

        mTouchYDiff = mDisplaySizeCache.y - mDecorDummy.getHeight();
        Timber.i("displaySize:" + mDisplaySizeCache.y + " ,decorHeight:" + mDecorDummy.getHeight());
    }


    private int getWindowWidth(){
        return mWindowSizeCache.x;
    }


    private int getWindowHeight(){
        return mWindowSizeCache.y;
    }


    private PointF getTouchPoint(MotionEvent event){
//        Timber.i("rawY:" + event.getRawY());
        mTouchPointCache.x = event.getRawX();
        mTouchPointCache.y = event.getRawY() - mTouchYDiff;
        return mTouchPointCache;
    }

    void locateOnWindow(float toX, float toY) {
        WindowManager.LayoutParams lp = (WindowManager.LayoutParams) getLayoutParams();
        lp.x = (int) toX;
        lp.y = (int) toY;
        mWindowManager.updateViewLayout(this, lp);
    }


    void animOnWindow(float toX, float toY) {
        final WindowManager.LayoutParams lp = (WindowManager.LayoutParams) getLayoutParams();
        final View self = this;

        PropertyValuesHolder pX = PropertyValuesHolder.ofInt(KEY_X, lp.x, (int) toX);
        PropertyValuesHolder pY = PropertyValuesHolder.ofInt(KEY_Y, lp.y, (int) toY);
        ValueAnimator animXY = ValueAnimator.ofPropertyValuesHolder(pX, pY);
        animXY.setDuration(200);
        animXY.setInterpolator(OVER_SHOOT_INTERPOLATOR);
        animXY.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                lp.x = (int) valueAnimator.getAnimatedValue(KEY_X);
                lp.y = (int) valueAnimator.getAnimatedValue(KEY_Y);
                mWindowManager.updateViewLayout(self, lp);
            }
        });
        animXY.start();
    }


    ///////////////
    // タッチイベント

    private boolean mMoving = false;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final int action = event.getAction();

        if (action == MotionEvent.ACTION_DOWN) {
            updateWindowSize();
            final PointF touchPoint = getTouchPoint(event);
            mInitialTouchX = touchPoint.x;
            mInitialTouchY = touchPoint.y;
            mMoving = false;
            return true;
        }
        else if (event.getAction() == MotionEvent.ACTION_MOVE) {
            final PointF touchPoint = getTouchPoint(event);
             if(!mMoving){
                float xDiff = Math.abs(mInitialTouchX - event.getX());
                float yDiff = Math.abs(mInitialTouchY - event.getY());
                if ((xDiff + yDiff) / 2 > mSlop) {
                    if (mVelocityTracker == null) mVelocityTracker = VelocityTracker.obtain();
                    else mVelocityTracker.clear();
                    mMoving = true;
                }
            }
            else{
                 float toX = touchPoint.x - getWidth() /2;
                 float toY = touchPoint.y - getHeight()/2;
                 locateOnWindow(toX , toY);
                 event.offsetLocation(toX, toY);
                 mVelocityTracker.addMovement(event);
            }
            return true;
        }
        else if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
            final PointF touchPoint = getTouchPoint(event);

            if(!mMoving){
            }
            else{
                // FIXME 初速をアニメーションに適用できてない。Npx/50msはてきとう
                mVelocityTracker.computeCurrentVelocity(50, mMaxVelocity);

                // おしまい
                if (/* mTrashEnable && hitTrashCircle(x, y)*/ false) {
//                mQuiting = true;
//                dismissHitTrash();
                }
                // 継続
                else {
                    // BubbleのX位置をこのViewの右端か左端に調整する
                    float toX = touchPoint.x + mVelocityTracker.getXVelocity() < getWindowWidth() / 2 ?
                            0 :
                            getWindowWidth() - getWidth();

                    // BubbleのY位置をこのViewの範囲内に調整する
                    final int height = getHeight();
                    float toY = (touchPoint.y - height / 2) + mVelocityTracker.getYVelocity();
                    toY = Math.max(0, toY);
                    toY = Math.min(getWindowHeight() - height, toY);
                    Timber.i("YVelocity:" + mVelocityTracker.getYVelocity());

                    // アニメで動かす
                    animOnWindow(toX, toY);

                    // ゴミ箱を隠す
//                hideTrashToBottom(false);
                }
                mVelocityTracker.recycle();
                mVelocityTracker = null;
            }

        }

        return false;
    }

}
