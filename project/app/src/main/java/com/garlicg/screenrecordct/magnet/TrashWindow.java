package com.garlicg.screenrecordct.magnet;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.garlicg.screenrecordct.R;
import com.garlicg.screenrecordct.util.DisplayUtils;
import com.garlicg.screenrecordct.util.Interpolators;
import com.garlicg.screenrecordct.util.ViewFinder;

public class TrashWindow extends FrameLayout{

    @SuppressLint("InflateParams")
    public static TrashWindow createInstance(Context context){
        final LayoutInflater inflater = LayoutInflater.from(context);
        return (TrashWindow) inflater.inflate(R.layout.widget_trash_window , null , false);
    }


    @SuppressLint("RtlHardcoded")
    public static WindowManager.LayoutParams createWindowParams(Context context){
        final int windowSize = DisplayUtils.dpToPx(context.getResources(), WINDOW_SIZE_DP);
        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                windowSize,
                windowSize,
                WindowManager.LayoutParams.TYPE_TOAST,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                , PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM;
        return params;
    }

    public static int WINDOW_SIZE_DP = 120;
    private int mWindowSize;
    private View mTrashImage;


    public TrashWindow(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }


    public TrashWindow(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }


    public void init(Context context){
        final Resources res = context.getResources();
        mWindowSize = DisplayUtils.dpToPx(res, WINDOW_SIZE_DP);
    }


    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mTrashImage = ViewFinder.byId(this , R.id.trashImage);
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mTrashImage.setTranslationY(mWindowSize);
    }


    public void show(){
        mTrashImage.animate()
                .translationY(0)
                .setDuration(200)
                .setInterpolator(Interpolators.DECELERATE)
                .start();
    }


    public void hide(){
        mTrashImage.animate()
                .translationY(mWindowSize)
                .setDuration(200)
                .setInterpolator(Interpolators.ACCELERATE)
                .start();
    }


    public boolean mScaledUp = false;

    public void setScaleUp(boolean scaleUp){
        if(mScaledUp == scaleUp) return;
        mScaledUp = scaleUp;
        if (scaleUp) scaleUp();
        else scaleNormal();
    }


    private void scaleUp(){
        mTrashImage.animate()
                .scaleX(1.8f)
                .scaleY(1.8f)
                .setDuration(100)
                .setInterpolator(Interpolators.DECELERATE)
                .start();
    }


    private void scaleNormal(){
        mTrashImage.animate()
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(100)
                .setInterpolator(Interpolators.DECELERATE)
                .start();
    }


    public void disappear(long duration){
        mTrashImage.setScaleX(1.8f);
        mTrashImage.setScaleY(1.8f);
        mTrashImage.animate()
                .scaleX(0f)
                .scaleY(0f)
                .setDuration(duration)
                .setInterpolator(Interpolators.ANTICIPATE)
                .start();
    }


    public boolean isHit(Point decor, PointF touchPoint){
        // Gravity BOTTOM | CENTER_HORIZONTALでのこのView位置
        int left = decor.x /2 - getWidth()/2;
        int right = decor.x /2 + getWidth()/2;
        int top = decor.y - getHeight();
        int bottom = decor.y;
//        Timber.i("left:" + left + " right:" + right + " top:" + top + " bottom:" + bottom);

        boolean hitX = touchPoint.x > left && touchPoint.x < right;
        boolean hitY = touchPoint.y > top && touchPoint.y < bottom;
        return hitX && hitY;
    }


}
