package com.garlicg.screenrecordct;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Point;
import android.graphics.PointF;
import android.os.Handler;
import android.os.Looper;
import android.os.Vibrator;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;

import com.garlicg.screenrecordct.magnet.DecorDummy;
import com.garlicg.screenrecordct.magnet.MagnetWindow;
import com.garlicg.screenrecordct.magnet.TrashWindow;

import timber.log.Timber;

/**
 */
public class FloatingManager implements MagnetWindow.Listener {

    private final Context mContext;
    private final Vibrator mVibrator;
    private final WindowManager mWindowManager;
    private MagnetWindow mMagnet;
    private TrashWindow mTrash;
    private DecorDummy mDecorDummy;
    private Listener mListener;
    private Handler mHandler = new Handler(Looper.getMainLooper());

    private ObjectAnimator mRotate1;
    private ObjectAnimator mRotate2;

    private static final int STATE_CONTROLLABLE = 0;
    private static final int STATE_COUNT_DOWN = 1;
    private static final int STATE_RECORDING = 2;
    private static final int STATE_STOPPING = 3;
    private int mState = STATE_CONTROLLABLE;


    public interface Listener{
        void onStartRecord();
        void onStopRecord();
        void onFinishFloating();
    }


    public FloatingManager(Context context , Listener listener) {
        mContext = context;
        mVibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        mListener = listener;
    }


    public void onCreate() {
        Timber.i("onCreate!");
        mDecorDummy = DecorDummy.createInstance(mContext);
        mWindowManager.addView(mDecorDummy, DecorDummy.createWindowParams());

        mTrash = TrashWindow.createInstance(mContext);
        mWindowManager.addView(mTrash, TrashWindow.createWindowParams(mContext));

        mMagnet = MagnetWindow.createInstance(mContext);
        mMagnet.setDecorDummy(mDecorDummy);
        mMagnet.setListener(this);
        mWindowManager.addView(mMagnet, MagnetWindow.createWindowParams());

    }


    public void onDestroy() {
        Timber.i("onDestroy!");

        mWindowManager.removeView(mMagnet);
        mMagnet = null;

        mWindowManager.removeView(mTrash);
        mTrash = null;

        mWindowManager.removeView(mDecorDummy);
        mDecorDummy = null;
    }


    public void initState(){
        mState = STATE_CONTROLLABLE;
        mVibrator.vibrate(800);
        mMagnet.getView().setEnabled(true);
        mMagnet.getMagnetImage().setColorFilter(0xffffffff);
        mRotate2.cancel();
    }


    ////////////////
    // MagnetViewからのコールバック

    @Override
    public void onClick(MagnetWindow window) {

        if(mState == STATE_CONTROLLABLE ){
            mState = STATE_COUNT_DOWN;
            mVibrator.vibrate(15);
            startCountDown();
        }
        else if(mState == STATE_COUNT_DOWN){
            mState = STATE_CONTROLLABLE;
            mVibrator.vibrate(15);
            cancelCountDown();
        }
        else if(mState == STATE_RECORDING){
            mState = STATE_STOPPING;
            mVibrator.vibrate(15);
            mListener.onStopRecord();

            View v = window.getView();
            v.setEnabled(false);
            v.setActivated(false);
            ImageView image = window.getMagnetImage();
            image.setColorFilter(0xff333333);
            mRotate1.cancel();
            mRotate2 = genRotateAnimation(image , 6000);
            mRotate2.start();
        }
    }


    private void startCountDown(){
        mMagnet.getMagnetText().setText("3");
        mHandler.postDelayed(mCountDown2, 1000);
        mHandler.postDelayed(mCountDown1, 2000);
        mHandler.postDelayed(mCountDown0, 3000);
    }


    private void cancelCountDown(){
        mMagnet.getMagnetText().setText("");
        mHandler.removeCallbacks(mCountDown2);
        mHandler.removeCallbacks(mCountDown1);
        mHandler.removeCallbacks(mCountDown0);
    }


    private Runnable mCountDown2 = new Runnable() {
        @Override
        public void run() {
            mMagnet.getMagnetText().setText("2");
        }
    };


    private Runnable mCountDown1 = new Runnable() {
        @Override
        public void run() {
            mMagnet.getMagnetText().setText("1");
        }
    };


    private Runnable mCountDown0 = new Runnable() {
        @Override
        public void run() {
            mState = STATE_RECORDING;
            mListener.onStartRecord();

            View v = mMagnet.getView();
            v.setActivated(true);

            mMagnet.getMagnetText().setText("");

            ImageView image = mMagnet.getMagnetImage();
            image.setColorFilter(0xffff0000);
            image.setRotation(image.getRotation() % 360);
            mRotate1 = genRotateAnimation(image, 2000);
            mRotate1.start();
        }
    };



    @Override
    public void onDragStart(MagnetWindow window) {
        if(mState != STATE_CONTROLLABLE) return;
        mTrash.show();
    }


    @Override
    public void onDragging(MagnetWindow window, Point decor, PointF touchPoint) {
        if(mState != STATE_CONTROLLABLE) return;

        boolean isHit = mTrash.isHit(decor, touchPoint);
        mTrash.setScaleUp(isHit);
    }


    @Override
    public boolean onDrop(MagnetWindow window, Point decor, PointF touchPoint) {
        if(mState != STATE_CONTROLLABLE) return false;

        if(mTrash.isHit(decor, touchPoint)){
            mTrash.disappear(200);
            mMagnet.disappear(200);
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mListener.onFinishFloating();
                }
            },200);
            return true;
        }

        mTrash.hide();
        return false;
    }


    private ObjectAnimator genRotateAnimation(View v ,long duration){
        ObjectAnimator anim = ObjectAnimator.ofFloat(v, View.ROTATION, v.getRotation() + 360);
        anim.setRepeatCount(ValueAnimator.INFINITE);
        anim.setRepeatMode(ValueAnimator.RESTART);
        anim.setInterpolator(new LinearInterpolator());
        anim.setDuration(duration);
        return anim;
    }


}
