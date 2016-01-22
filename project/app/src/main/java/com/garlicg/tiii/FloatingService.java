package com.garlicg.tiii;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;

import com.garlicg.tiii.bubble.MagnetLayout;
import com.garlicg.tiii.util.ViewFinder;

import timber.log.Timber;

/**
 */
public class FloatingService extends Service implements MagnetLayout.OnMagnetEventListener{

    private WindowManager mWindowManager;
    private View mRoot;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        // No bind
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        final LayoutInflater inflater = LayoutInflater.from(this);
        mRoot = inflater.inflate(R.layout.bubble_sample, null, false);
        MagnetLayout magnetLayout = ViewFinder.byId(mRoot ,R.id.magnetLayout);
        magnetLayout.setOnMagnetEventListener(this);
        mWindowManager = (WindowManager)getSystemService(WINDOW_SERVICE);
        mWindowManager.addView(mRoot, createLayoutParams());
    }

    private static WindowManager.LayoutParams createLayoutParams() {
        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_SYSTEM_ERROR,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                , PixelFormat.TRANSLUCENT);
        return params;

    }

    @SuppressLint("InflateParams")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Timber.i("onStartCommand %s" ,intent);
        return START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mRoot != null){
            mWindowManager.removeView(mRoot);
            mRoot = null;
        }
    }


    /**
     * 通知を表示します。
     */
    private Notification createNotification() {
        final Notification.Builder builder = new Notification.Builder(this);
        builder.setWhen(System.currentTimeMillis());
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setContentTitle("TITLE");
        builder.setContentText("TEXT");
        builder.setOngoing(true);
        builder.setPriority(Notification.PRIORITY_MIN);
        builder.setCategory(Notification.CATEGORY_SERVICE);

        // PendingIntent作成
        final Intent notifyIntent = new Intent(this, LauncherActivity.class);
        PendingIntent notifyPendingIntent = PendingIntent.getActivity(this, 0, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(notifyPendingIntent);
        return null;
    }


    @Override
    public void onMagnetQuit() {
        stopSelf();
    }

}
