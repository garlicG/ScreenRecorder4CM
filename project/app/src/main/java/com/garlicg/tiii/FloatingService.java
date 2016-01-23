package com.garlicg.tiii;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.garlicg.tiii.magnet.FloatingManager;
import com.garlicg.tiii.magnet.MagnetLayout;

import timber.log.Timber;

/**
 */
public class FloatingService extends Service implements MagnetLayout.OnMagnetEventListener{

    private FloatingManager mFloatingManager;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        // No bind
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mFloatingManager = new FloatingManager(this);
        mFloatingManager.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Timber.i("onStartCommand %s", intent);
        return START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mFloatingManager.onDestroy();
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
