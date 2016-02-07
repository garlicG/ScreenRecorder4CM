package com.garlicg.screenrecord4cm;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.PointF;
import android.os.Handler;
import android.os.Looper;
import android.os.Vibrator;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;

import com.garlicg.screenrecord4cm.magnet.DecorDummy;
import com.garlicg.screenrecord4cm.magnet.MagnetWindow;
import com.garlicg.screenrecord4cm.magnet.IndicatorWindow;

/**
 */
public class FloatingManager implements MagnetWindow.Listener {

    static final String EXTRA_INVISIBLE_RECORD ="EXTRA_INVISIBLE_RECORD";
    private boolean mInvisibleRecord = false;

    private final Context mContext;
    private final Vibrator mVibrator;
    private final WindowManager mWindowManager;
    private MagnetWindow mMagnet;
    private IndicatorWindow mTrash;
    private DecorDummy mDecorDummy;
    private Listener mListener;
    private Handler mHandler = new Handler(Looper.getMainLooper());

    private ObjectAnimator mRotate1;

    private static final int STATE_CONTROLLABLE = 0;
    private static final int STATE_COUNT_DOWN = 1;
    private static final int STATE_RECORDING = 2;
    private static final int STATE_STOPPING = 3;
    private int mState = STATE_CONTROLLABLE;


    public interface Listener{
        void onRequestStartRecord();
        void onRequestStopRecord();
        boolean onHandleTrashDrop();
        boolean onHandleSettingsDrop();
    }


    public FloatingManager(Context context , Listener listener) {
        mContext = context;
        mVibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        mListener = listener;
    }


    void setupWindows() {
        mDecorDummy = DecorDummy.createInstance(mContext);
        mWindowManager.addView(mDecorDummy, DecorDummy.createWindowParams());

        mTrash = IndicatorWindow.createInstance(mContext);
        mWindowManager.addView(mTrash, IndicatorWindow.createWindowParams(mContext));

        mMagnet = MagnetWindow.createInstance(mContext);
        mMagnet.setDecorDummy(mDecorDummy);
        mMagnet.setListener(this);
        mWindowManager.addView(mMagnet, MagnetWindow.createWindowParams());

    }


    public void onDestroy() {
        mHandler.removeCallbacks(mOKDismiss);

        mWindowManager.removeView(mMagnet);
        mMagnet = null;

        mWindowManager.removeView(mTrash);
        mTrash = null;

        mWindowManager.removeView(mDecorDummy);
        mDecorDummy = null;
    }


    void updateParams(Intent intent){
        mInvisibleRecord = intent.getBooleanExtra(EXTRA_INVISIBLE_RECORD , false);
    }


    public void initState(){
        mState = STATE_CONTROLLABLE;
        mVibrator.vibrate(800);
        mMagnet.getMagnetFrame().setColorFilter(null);
        mMagnet.getMagnetText().setText("OK");
        mHandler.postDelayed(mOKDismiss, 1000);
    }


    public void changeToStoppingState(){
        if(mState == STATE_STOPPING) return;
        mState = STATE_STOPPING;

        mVibrator.vibrate(15);
        mListener.onRequestStopRecord();

        ImageView frame = mMagnet.getMagnetFrame();
        frame.setColorFilter(0xffFFFFFF);

        if(mInvisibleRecord){
            mMagnet.setNightmareMode(false);
        }
        else{
            if(mRotate1 != null && mRotate1.isStarted()){
                mRotate1.cancel();
            }
        }
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
            changeToStoppingState();
        }
    }


    private void startCountDown(){
        mHandler.removeCallbacks(mOKDismiss);
        mMagnet.getMagnetIcon().setVisibility(View.INVISIBLE);
        mMagnet.getMagnetText().setText("3");
        mHandler.postDelayed(mCountDown2, 1000);
        mHandler.postDelayed(mCountDown1, 2000);
        mHandler.postDelayed(mCountDown0, 3000);
    }


    private void cancelCountDown(){
        mMagnet.getMagnetIcon().setVisibility(View.VISIBLE);
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

            mMagnet.getMagnetText().setText("");

            if(mInvisibleRecord){
                mMagnet.setNightmareMode(true);
            }
            else{
                ImageView image = mMagnet.getMagnetFrame();
                image.setColorFilter(0xffff0000);
                image.setRotation(image.getRotation() % 360);
                mRotate1 = genRotateAnimation(image, 2000);
                mRotate1.start();
            }

            mListener.onRequestStartRecord();
        }
    };


    private Runnable mOKDismiss = new Runnable() {
        @Override
        public void run() {
            mMagnet.getMagnetText().setText("");
            mMagnet.getMagnetIcon().setVisibility(View.VISIBLE);
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

        if(mTrash.isHitTrash(decor , touchPoint)){
            mTrash.requestScaleUpTrash();
        }
        else if(mTrash.isHitSettings(decor , touchPoint)){
            mTrash.requestScaleUpSettings();
        }
        else{
            mTrash.requestScaleUpCancel();
        }

    }


    @Override
    public boolean onDrop(MagnetWindow window, Point decor, PointF touchPoint) {
        if(mState != STATE_CONTROLLABLE) return false;

        if(mTrash.isHitTrash(decor, touchPoint) && mListener.onHandleTrashDrop()){
            return true;
        }
        else if(mTrash.isHitSettings(decor , touchPoint) && mListener.onHandleSettingsDrop()){
            return true;
        }

        mTrash.hide();
        return false;
    }


    public void dismissAsHitTrash(long duration){
        mTrash.disappearHitAnimate(mTrash.getTrash() , duration);
        mTrash.disappearNoHitAnimate(mTrash.getSettings() ,duration);
        mMagnet.disappear(200);
    }


    public void dismissAsHitSettings(long duration){
        mTrash.disappearHitAnimate(mTrash.getSettings(), duration);
        mTrash.disappearNoHitAnimate(mTrash.getTrash(), duration);
        mMagnet.disappear(duration);
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
