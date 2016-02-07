package com.garlicg.screenrecord4cm.magnet;

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

import com.garlicg.screenrecord4cm.R;
import com.garlicg.screenrecord4cm.util.DisplayUtils;
import com.garlicg.screenrecord4cm.util.Interpolators;
import com.garlicg.screenrecord4cm.util.ViewFinder;

public class IndicatorWindow extends FrameLayout {

    @SuppressLint("InflateParams")
    public static IndicatorWindow createInstance(Context context) {
        final LayoutInflater inflater = LayoutInflater.from(context);
        return (IndicatorWindow) inflater.inflate(R.layout.widget_indicator_window, null, false);
    }


    @SuppressLint("RtlHardcoded")
    public static WindowManager.LayoutParams createWindowParams(Context context) {
        final int windowWidth = DisplayUtils.dpToPx(context.getResources(), WINDOW_WIDTH_DP);
        final int windowHeight = DisplayUtils.dpToPx(context.getResources(), WINDOW_HEIGHT_DP);
        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                windowWidth,
                windowHeight,
                WindowManager.LayoutParams.TYPE_TOAST,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                , PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM;
        return params;
    }

    /**
     * normal 210 width position
     * 32 (64) 18 (64) 32
     *
     * scaled 210 width position
     * 2 (120) 2 (64) 32
     */
    public static final int WINDOW_WIDTH_DP = 210;
    public static final int WINDOW_HEIGHT_DP = 120;
    private int mWindowHeight;

    private static final int REQUEST_SCALE_UP_NONE = 0;
    private static final int REQUEST_SCALE_UP_TRASH = 1;
    private static final int REQUEST_SCALE_UP_SETTINGS = 2;
    private int mRequestScaleUp = 0;

    private View mTrashImage;
    private View mSettingsImage;


    public IndicatorWindow(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }


    public IndicatorWindow(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }


    public void init(Context context) {
        final Resources res = context.getResources();
        mWindowHeight = DisplayUtils.dpToPx(res, WINDOW_HEIGHT_DP);
    }


    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mTrashImage = ViewFinder.byId(this, R.id.trashImage);
        mSettingsImage = ViewFinder.byId(this, R.id.settingsImage);
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mTrashImage.setTranslationY(mWindowHeight);
        mSettingsImage.setTranslationY(mWindowHeight);
    }

    public View getTrash(){
        return mTrashImage;
    }


    public View getSettings(){
        return mSettingsImage;
    }


    public void show() {
        showAnimate(mTrashImage);
        showAnimate(mSettingsImage);
    }


    public void hide() {
        hideAnimate(mTrashImage);
        hideAnimate(mSettingsImage);
    }


    public void requestScaleUpTrash(){
        if(mRequestScaleUp == REQUEST_SCALE_UP_TRASH) return;
        mRequestScaleUp = REQUEST_SCALE_UP_TRASH;

        hitAnimate(mTrashImage);
        initAnimate(mSettingsImage);
    }


    public void requestScaleUpSettings(){
        if(mRequestScaleUp == REQUEST_SCALE_UP_SETTINGS) return;
        mRequestScaleUp = REQUEST_SCALE_UP_SETTINGS;

        initAnimate(mTrashImage);
        hitAnimate(mSettingsImage);
    }


    public void requestScaleUpCancel(){
        if(mRequestScaleUp == REQUEST_SCALE_UP_NONE) return;
        mRequestScaleUp = REQUEST_SCALE_UP_NONE;

        initAnimate(mTrashImage);
        initAnimate(mSettingsImage);
    }


    /**
     * 左半分だとtrashということでごり押し判定している
     */
    public boolean isHitTrash(Point decor, PointF touchPoint){
        // Gravity BOTTOM | CENTER_HORIZONTALでのこのWindow位置
        int left = decor.x / 2 - getWidth() / 2;
        int top = decor.y - getHeight();
        int bottom = decor.y;

        boolean hitX = touchPoint.x > left && touchPoint.x < decor.x / 2;
        boolean hitY = touchPoint.y > top && touchPoint.y < bottom;
        return hitX && hitY;
    }


    /**
     * 右半分だとsettingsということでごり押し判定している
     */
    public boolean isHitSettings(Point decor, PointF touchPoint){
        // Gravity BOTTOM | CENTER_HORIZONTALでのこのWindow位置
        int right = decor.x / 2 + getWidth() / 2;
        int top = decor.y - getHeight();
        int bottom = decor.y;

        boolean hitX = touchPoint.x > decor.x /2 && touchPoint.x < right;
        boolean hitY = touchPoint.y > top && touchPoint.y < bottom;
        return hitX && hitY;
    }


    private void showAnimate(View v){
        v.animate()
                .translationY(0)
                .setDuration(200)
                .setInterpolator(Interpolators.DECELERATE)
                .start();
        v.animate()
                .translationY(0)
                .setDuration(200)
                .setInterpolator(Interpolators.DECELERATE)
                .start();
    }


    private void hideAnimate(View view){
        view.animate()
                .translationY(mWindowHeight)
                .setDuration(200)
                .setInterpolator(Interpolators.ACCELERATE)
                .start();
    }


    private void hitAnimate(View view){
        view.animate()
                .scaleX(1.8f)
                .scaleY(1.8f)
                .setDuration(100)
                .setInterpolator(Interpolators.DECELERATE)
                .start();
    }


    private void initAnimate(View view){
        view.animate()
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(100)
                .setInterpolator(Interpolators.DECELERATE)
                .start();
    }


    public void disappearHitAnimate(View view , long duration){
        view.setScaleX(1.8f);
        view.setScaleY(1.8f);
        view.animate()
                .scaleX(0f)
                .scaleY(0f)
                .setDuration(duration)
                .setInterpolator(Interpolators.ACCELERATE)
                .start();
    }


    public void disappearNoHitAnimate(View view , long duration){
        view.setScaleX(1f);
        view.setScaleY(1f);
        view.animate()
                .translationY(mWindowHeight)
                .setDuration(duration)
                .setInterpolator(Interpolators.ACCELERATE)
                .start();
    }


}
