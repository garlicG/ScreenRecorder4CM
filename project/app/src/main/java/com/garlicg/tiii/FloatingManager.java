package com.garlicg.tiii;

import android.content.Context;
import android.graphics.Point;
import android.graphics.PointF;
import android.os.Handler;
import android.os.Looper;
import android.view.WindowManager;

import com.garlicg.tiii.magnet.DecorDummy;
import com.garlicg.tiii.magnet.MagnetWindow;
import com.garlicg.tiii.magnet.TrashWindow;

import timber.log.Timber;

/**
 */
public class FloatingManager implements MagnetWindow.Listener {

    private final Context mContext;
    private final WindowManager mWindowManager;
    private MagnetWindow mMagnet;
    private TrashWindow mTrash;
    private DecorDummy mDecorDummy;
    private Listener mListener;
    private Handler mHandler = new Handler(Looper.getMainLooper());

    public interface Listener{
        void onStartRecord();
        void onStopRecord();
        void onFinishFloating();
    }

    public FloatingManager(Context context , Listener listener) {
        mContext = context;
        mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        mListener = listener;
    }


    public void onCreate() {
        Timber.i("onCreate!");
        mDecorDummy = DecorDummy.createInstance(mContext);
        mWindowManager.addView(mDecorDummy, DecorDummy.createWindowParams());

        mTrash = TrashWindow.createInstance(mContext);
        mTrash.setDecorDummy(mDecorDummy);
        mWindowManager.addView(mTrash, TrashWindow.createWindowParams(mContext));

        mMagnet = MagnetWindow.createInstance(mContext);
        mMagnet.setDecorDummy(mDecorDummy);
        mMagnet.setListener(this);
        mWindowManager.addView(mMagnet, MagnetWindow.createWindowParams());

    }


    public void onDestroy() {
        Timber.i("onDestroy!");
        mWindowManager.removeView(mTrash);
        mTrash = null;

        mWindowManager.removeView(mMagnet);
        mMagnet = null;

        mWindowManager.removeView(mDecorDummy);
        mDecorDummy = null;
    }


    @Override
    public void onClick(MagnetWindow window) {
        // TODO start とstopの条件わけとか
        mListener.onStartRecord();
    }


    @Override
    public void onTouchMoveStart(MagnetWindow window) {
        // TODO start中はゴミ箱ださない
        mTrash.show();
    }


    @Override
    public void onTouchMoving(MagnetWindow window ,Point decor, PointF touchPoint) {
        // TODO start中は計算しない

        boolean isHit = mTrash.isHit(decor, touchPoint);
        mTrash.setScaleUp(isHit);
    }


    @Override
    public boolean onTouchMoveEnd(MagnetWindow window ,Point decor, PointF touchPoint) {
        // TODO start中は計算しない

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
}
