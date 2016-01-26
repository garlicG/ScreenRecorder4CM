package com.garlicg.tiii.magnet;

import android.content.Context;
import android.graphics.PixelFormat;
import android.view.View;
import android.view.WindowManager;

/**
 */
public class DecorDummy extends View {

    public DecorDummy(Context context) {
        super(context);
    }


    public static DecorDummy createInstance(Context context){
        return new DecorDummy(context);
    }


    public static WindowManager.LayoutParams createWindowParams(){
        return new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_TOAST,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                , PixelFormat.TRANSLUCENT);
    }

}
