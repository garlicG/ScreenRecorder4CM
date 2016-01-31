package com.garlicg.screenrecordct.magnet;

import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.PointF;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.garlicg.screenrecordct.R;
import com.garlicg.screenrecordct.util.DisplayUtils;
import com.garlicg.screenrecordct.util.Interpolators;
import com.garlicg.screenrecordct.util.ViewFinder;

public class MagnetWindow extends FrameLayout{

    @SuppressLint("InflateParams")
    public static MagnetWindow createInstance(Context context){
        final LayoutInflater inflater = LayoutInflater.from(context);
        return (MagnetWindow) inflater.inflate(R.layout.widget_magnet_window, null, false);
    }


    @SuppressLint("RtlHardcoded")
    public static WindowManager.LayoutParams createWindowParams(){
        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_TOAST,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                        | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                , PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.LEFT | Gravity.TOP;
        return params;
    }


    private static final String KEY_X = "x";
    private static final String KEY_Y = "y";
    private WindowManager mWindowManager;
    private View mView;
    private int mTouchYDiff = 0;
    VelocityTracker mVelocityTracker = null;
    private int mMaxVelocity;
    private int mSlop;
    private int mTapTimeout;
    private DecorDummy mDecorDummy;
    private float mInitialTouchX;
    private float mInitialTouchY;
    private long mInitialTapTime;



    public MagnetWindow(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }


    public MagnetWindow(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }


    public void init(Context context){
        mWindowManager = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
        mSlop = ViewConfiguration.get(context).getScaledTouchSlop();
//        mTapTimeout = ViewConfiguration.getTapTimeout(); // bit long time...
        mTapTimeout = 60;
        mMaxVelocity = ViewConfiguration.get(context).getScaledMaximumFlingVelocity();
    }


    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mView = ViewFinder.byId(this, R.id.viewRoot);
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if(isInEditMode())return;

        if(oldw == 0 && w > 0){
            // 右上に配置
            WindowManager.LayoutParams lp = (WindowManager.LayoutParams) getLayoutParams();
            lp.x = DisplayUtils.getDisplaySize(getContext()).x - w;
            mWindowManager.updateViewLayout(this, lp);

            ScaleAnimation scale = new ScaleAnimation(0f, 1f, 0f, 1f , w/2 , h/2);
            scale.setDuration(300);
            scale.setInterpolator(new DecelerateInterpolator());
            mView.startAnimation(scale);
        }
    }


    public void setDecorDummy(DecorDummy decorDummy){
        mDecorDummy = decorDummy;
    }


    public View getView(){
        return mView;
    }


    public ImageView getMagnetFrame(){
        return (ImageView) findViewById(R.id.innerFrame);
    }


    public ImageView getMagnetIcon(){
        return (ImageView)findViewById(R.id.magnetIcon);
    }


    public TextView getMagnetText(){
        return (TextView) findViewById(R.id.magnetText);
    }





    //////////////////
    // View位置の補完

    private final Point mDisplaySizeCache = new Point();
    private final Point mDecorSizeCache = new Point();
    private final PointF mTouchPointCache = new PointF();

    private void updateWindowSize(){
        // ステータス分のずれがある場合はタッチをズラす
//        Timber.i("dl:" + mDecorDummy.getHeight());
        mWindowManager.getDefaultDisplay().getSize(mDisplaySizeCache);

        mDecorSizeCache.x = mDecorDummy.getWidth();
        mDecorSizeCache.y = mDecorDummy.getHeight();

        mTouchYDiff = mDisplaySizeCache.y - mDecorDummy.getHeight();
    }


    private int getParentWidth(){
        return mDecorSizeCache.x;
    }


    private int getParentHeight(){
        return mDecorSizeCache.y;
    }


    private PointF getTouchPoint(MotionEvent event){
        mTouchPointCache.x = event.getRawX();
        mTouchPointCache.y = event.getRawY() - mTouchYDiff;
        return mTouchPointCache;
    }

    void locate(float toX, float toY) {
        WindowManager.LayoutParams lp = (WindowManager.LayoutParams) getLayoutParams();
        lp.x = (int) toX;
        lp.y = (int) toY;
        mWindowManager.updateViewLayout(this, lp);
    }


    void locateAnimation(float toX, float toY) {
        final WindowManager.LayoutParams lp = (WindowManager.LayoutParams) getLayoutParams();
        final View self = this;

        PropertyValuesHolder pX = PropertyValuesHolder.ofInt(KEY_X, lp.x, (int) toX);
        PropertyValuesHolder pY = PropertyValuesHolder.ofInt(KEY_Y, lp.y, (int) toY);
        ValueAnimator animXY = ValueAnimator.ofPropertyValuesHolder(pX, pY);
        animXY.setDuration(200);
        animXY.setInterpolator(Interpolators.OVER_SHOOT);
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
    // 状態管理とか

    private boolean mQuiting = false;
    private boolean mMoving = false;


    public void disappear(long duration){
        mQuiting = true;
        ScaleAnimation scale = new ScaleAnimation(1f , 0f ,1f , 0f , getWidth()/2,getHeight()/2);
        scale.setFillAfter(true);
        scale.setInterpolator(Interpolators.ACCELERATE);
        scale.setDuration(duration);
        mView.startAnimation(scale);
    }


    ///////////////
    // タッチイベント

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        if(mQuiting)return false;

        final int action = event.getAction();

        if (action == MotionEvent.ACTION_DOWN) {
            updateWindowSize();

            final PointF touchPoint = getTouchPoint(event);
            mInitialTouchX = touchPoint.x;
            mInitialTouchY = touchPoint.y;
            mInitialTapTime = System.currentTimeMillis();
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
                    mListener.onDragStart(this);
                    mMoving = true;
                }
            }
            else{
                 float toX = touchPoint.x - getWidth() /2;
                 float toY = touchPoint.y - getHeight()/2;
                 locate(toX, toY);

                 if(mInitialTapTime + mTapTimeout >= System.currentTimeMillis()){
                     mListener.onDragging(this, mDecorSizeCache, touchPoint);
                 }

                 event.offsetLocation(toX, toY);
                 mVelocityTracker.addMovement(event);
            }
            return true;
        }
        else if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
            final PointF touchPoint = getTouchPoint(event);

            // クリックで終了 (動きなし)
            if(!mMoving){
                mListener.onClick(this);
                return false;
            }
            // クリックで終了 (動いたけどすぐ離した)
            else if(mInitialTapTime + mTapTimeout >= System.currentTimeMillis()){
                mListener.onClick(this);
                wallOn(touchPoint);
                mVelocityTracker.recycle();
                mVelocityTracker = null;
                return false;
            }

            // リスナー側のドロップハンドリング
            if(mListener.onDrop(this, mDecorSizeCache, touchPoint)){
                mVelocityTracker.recycle();
                mVelocityTracker = null;
            }
            // 右が左にWindowを移動する
            else {
                wallOn(touchPoint);
                mVelocityTracker.recycle();
                mVelocityTracker = null;
            }
            return false;
        }

        return false;
    }


    /**
     * 壁に吸い付くアニメーション
     */
    private void wallOn(PointF touchPoint){
        mVelocityTracker.computeCurrentVelocity(50, mMaxVelocity);
        // X位置をこのParentの右端か左端に調整する
        float toX = touchPoint.x + mVelocityTracker.getXVelocity() < getParentWidth() / 2 ?
                0 :
                getParentWidth() - getWidth();

        // Y位置をこのParentの範囲内に調整する
        final int height = getHeight();
        float toY = (touchPoint.y - height / 2) + mVelocityTracker.getYVelocity();
        toY = Math.max(0, toY);
        toY = Math.min(getParentHeight() - height, toY);

        // アニメで動かす
        locateAnimation(toX, toY);
    }


    //////////////
    // Listener

    public interface Listener{
        void onClick(MagnetWindow window);
        void onDragStart(MagnetWindow window);
        void onDragging(MagnetWindow window, Point decor, PointF touchPoint);
        boolean onDrop(MagnetWindow window, Point decor, PointF touchPoint);
    }


    public Listener mListener;

    public void setListener(Listener listener){
        mListener = listener;
    }
}
