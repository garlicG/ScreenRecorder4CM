package com.garlicg.tiii;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.garlicg.tiii.magnet.DecorDummyView;
import com.garlicg.tiii.magnet.MagnetLayout;
import com.garlicg.tiii.magnet.MagnetView;
import com.garlicg.tiii.util.ExtDebugTree;
import com.garlicg.tiii.util.ViewFinder;

import timber.log.Timber;

/**
 */
public class FloatingService extends Service implements MagnetLayout.OnMagnetEventListener{

    private WindowManager mWindowManager;
    private MagnetView mMagnetView;
    private DecorDummyView mDecorDummy;

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
        mDecorDummy = new DecorDummyView(this);
        mDecorDummy.attachToWindow();

        mMagnetView = (MagnetView) inflater.inflate(R.layout.widget_magnet, null, false);
        mMagnetView.attachToWindow(mDecorDummy);
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
        if(mMagnetView != null){
            mWindowManager.removeView(mMagnetView);
            mMagnetView = null;
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
