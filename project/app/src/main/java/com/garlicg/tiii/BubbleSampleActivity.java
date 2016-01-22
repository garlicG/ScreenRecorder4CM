package com.garlicg.tiii;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;

import com.garlicg.tiii.bubble.MagnetLayout;
import com.garlicg.tiii.util.ViewFinder;

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
        final MagnetLayout bl = ViewFinder.byId(this , R.id.magnetLayout);


        bl.setOnBubbleClickListener(new MagnetLayout.OnMagnetClickListener() {
            @Override
            public void onMagnetClick(final View v, final ImageView image) {
                if (!v.isActivated()) {
                    v.setActivated(true);
                    image.setColorFilter(0xffff0000);
                    image.setRotation(image.getRotation() % 360);
                    mRotate1 = genRotateAnimation(image , 2000);
                    mRotate1.start();
                    bl.setTrashEnable(false);
                }
                else {
                    v.setEnabled(false);
                    v.setActivated(false);
                    image.setColorFilter(0xff333333);
                    mRotate1.cancel();
                    mRotate2 = genRotateAnimation(image , 6000);
                    mRotate2.start();
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            bl.setTrashEnable(true);
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
