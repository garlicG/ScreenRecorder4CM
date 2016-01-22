package com.garlicg.tiii.magnet;

import android.content.Context;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

/**
 * Created by goto.t on 2016/01/23.
 */
public class DecorDummyView extends View {
    private WindowManager mWindowManager;

    public DecorDummyView(Context context) {
        super(context);
        mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
    }

    public void attachToWindow() {
        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_PRIORITY_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                , PixelFormat.TRANSLUCENT);
        mWindowManager.addView(this, params);
    }

}
