package com.garlicg.tiii;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.StateListAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipDescription;
import android.graphics.Canvas;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;

import com.garlicg.tiii.bubble.BubbleLayout;
import com.garlicg.tiii.util.Toaster;
import com.garlicg.tiii.util.ViewFinder;

import timber.log.Timber;

/**
 */
public class BubbleSampleActivity extends Activity {

    private Handler mHandler = new Handler(Looper.getMainLooper());

    private ObjectAnimator genRotateAnimation(View v ,long duration){
        ObjectAnimator anim = ObjectAnimator.ofFloat(v, View.ROTATION, v.getRotation()+360);
        anim.setRepeatCount(ValueAnimator.INFINITE);
        anim.setRepeatMode(ValueAnimator.RESTART);
        anim.setInterpolator(new LinearInterpolator());
        anim.setDuration(duration);
        return anim;
    }
    private ObjectAnimator mRotate1;
    private ObjectAnimator mRotate2;

    /**
     * onCreate
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.bubble_sample);
        BubbleLayout bl = ViewFinder.byId(this , R.id.bubbleLayout);


        bl.setOnBubbleClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                final ImageView image = ViewFinder.byId(v , R.id.bubbleImage);

                if (!v.isActivated()) {
                    v.setActivated(true);
                    image.setColorFilter(0xffff0000);
                    image.setRotation(image.getRotation() % 360);
                    mRotate1 = genRotateAnimation(image , 2000);
                    mRotate1.start();
                }
                else {
                    v.setEnabled(false);
                    v.setActivated(false);
                    image.setColorFilter(0xff333333);
                    float tmp = v.getRotation();
                    mRotate1.cancel();
                    mRotate2 = genRotateAnimation(image , 6000);
                    mRotate2.start();
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            v.setEnabled(true);
                            mRotate2.cancel();
                            image.setColorFilter(0xffffffff);
                        }
                    }, 3000);
                }
            }
        });

    }



}
