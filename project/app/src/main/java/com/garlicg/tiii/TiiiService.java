package com.garlicg.tiii;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.Nullable;

import com.garlicg.tiii.util.Toaster;

import timber.log.Timber;

/**
 */
public class TiiiService extends Service implements FloatingManager.Listener{

    private FloatingManager mFloatingManager;
    private Handler mHandler = new Handler(Looper.getMainLooper());

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        // No bind
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mFloatingManager = new FloatingManager(this , this);
        mFloatingManager.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Timber.i("onStartCommand %s", intent);
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Timber.i("TiiiService Destroy!");
        mFloatingManager.onDestroy();
    }


    //////////////
    // FloatingManagerからのコールバック

    @Override
    public void onStartRecord() {
        Timber.i("onStartRecord!");
    }


    @Override
    public void onStopRecord() {
        Timber.i("onStopRecord!");
        Toaster.show(this, "stopRecord");
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mFloatingManager.initState();
            }
        },3000);
    }

    @Override
    public void onFinishFloating() {
        stopSelf();
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

}
