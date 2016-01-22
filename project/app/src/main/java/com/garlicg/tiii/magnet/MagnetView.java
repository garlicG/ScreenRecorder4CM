package com.garlicg.tiii.magnet;

import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.garlicg.tiii.R;

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
    private DecorDummyView mDecorDummy;
    private float mInitialTouchX;
    private float mInitialTouchY;

    public MagnetView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public MagnetView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
        setFitsSystemWindows(true);
    }

    public void init(Context context){
        mWindowManager = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
        mSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        mMaxVelocity = ViewConfiguration.get(context).getScaledMaximumFlingVelocity();
    }

    //////////////////
    // windowAttach
    public void attachToWindow(DecorDummyView dummy){
        mDecorDummy = dummy;

        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PRIORITY_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                        | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                        | WindowManager.LayoutParams.FLAG_FULLSCREEN
                        | WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR
                , PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.LEFT | Gravity.TOP;
        mWindowManager.addView(this,params);
    }

    //////////////////
    // View位置の補完

    private final Point mDisplaySizeCache = new Point();
    private final Rect mWindowSizeCache = new Rect();
    private void updateWindowSize(){
        // ステータス分のずれがある場合はタッチをズラす
//        Timber.i("dl:" + mDecorDummy.getHeight());
        mWindowManager.getDefaultDisplay().getSize(mDisplaySizeCache);
//        getWindowVisibleDisplayFrame(mWindowSizeCache);
//        Timber.i("d:" + mDisplaySizeCache.y + " ,w:" + mWindowSizeCache.height());
//        mWindowSizeCache.top=0;
//        mWindowSizeCache.left = 0;
//        mWindowSizeCache.right = mDisplaySizeCache.x;
//        mWindowSizeCache.bottom = mDisplaySizeCache.y;
//        mTouchYDiff = mDisplaySizeCache.y - mWindowSizeCache.height();

        mWindowSizeCache.top=0;
        mWindowSizeCache.left = 0;
        mWindowSizeCache.right = mDecorDummy.getWidth();
        mWindowSizeCache.bottom = mDecorDummy.getHeight();

        mTouchYDiff = mDisplaySizeCache.y - mDecorDummy.getHeight();
        Timber.i("d:" + mDisplaySizeCache.y + " ,deccorHeigth:" + mDecorDummy.getHeight());

//        int[] locationWindow = new int[2];
//        int[] locationOnScreen = new int[2];
//        getLocationInWindow(locationWindow);
//        getLocationOnScreen(locationOnScreen);
//        Timber.i("parent:" + getParent().getClass());
//        Timber.i("locationWindow:" + locationWindow[1] + " ,locationOnScreen:" + locationOnScreen[1]);


    }

    private Rect getWindowSize(){
        return mWindowSizeCache;
    }

    private final PointF mTouchPointCache = new PointF();
    private PointF getTouchPoint(MotionEvent event){
//        Timber.i("rawY:" + event.getRawY());
        mTouchPointCache.x = event.getRawX();
        mTouchPointCache.y = event.getRawY() - mTouchYDiff;
        return mTouchPointCache;
    }

    void locateOnWindow(float x , float y){
        WindowManager.LayoutParams lp = (WindowManager.LayoutParams) getLayoutParams();
        lp.x = (int)x;
        lp.y = (int)y;
        mWindowManager.updateViewLayout(this, lp);
    }

    void animOnWindow(float x , float y){
        final WindowManager.LayoutParams lp = (WindowManager.LayoutParams) getLayoutParams();
        final View self = this;

        PropertyValuesHolder pX = PropertyValuesHolder.ofInt(KEY_X, lp.x, (int) x);
        PropertyValuesHolder pY = PropertyValuesHolder.ofInt(KEY_Y, lp.y, (int)y);
        ValueAnimator animXY = ValueAnimator.ofPropertyValuesHolder(pX, pY);
        animXY.setDuration(200);
        animXY.setInterpolator(OVER_SHOOT_INTERPOLATOR);
        animXY.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                lp.x = (int)valueAnimator.getAnimatedValue(KEY_X);
                lp.y = (int)valueAnimator.getAnimatedValue(KEY_Y);
//                Timber.i("x:" + lp.x);
                mWindowManager.updateViewLayout(self, lp);
            }
        });
        animXY.start();
    }

    public ImageView getMagnetImageView(){
        return (ImageView) findViewById(R.id.magnetImage);
    }


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
                    final int width = getWidth();
                    float toX = touchPoint.x + mVelocityTracker.getXVelocity() < getWindowSize().width() / 2 ?
                            0 :
                            getWindowSize().width() - width;

                    // BubbleのY位置をこのViewの範囲内に調整する
                    final int height = getHeight();
                    float toY = (touchPoint.y - height / 2) + mVelocityTracker.getYVelocity();
                    toY = Math.max(0, toY);
                    toY = Math.min(getWindowSize().height() - height, toY);
                    Timber.i("YVelovity:" + mVelocityTracker.getYVelocity());

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
