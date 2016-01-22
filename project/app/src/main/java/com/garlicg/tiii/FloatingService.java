package com.garlicg.tiii;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

/**
 */
public class FloatingService extends Service {


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        // No bind
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // none
    }

    @SuppressLint("InflateParams")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//        if (mFloatingViewManager != null) {
//            return START_STICKY;
//        }

        final LayoutInflater inflater = LayoutInflater.from(this);
        final ImageView iconView = (ImageView) inflater.inflate(R.layout.widget_chathead, null, false);
        iconView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });


//        mFloatingViewManager = new FloatingViewManager(this, mFloatingViewListener);
//        mFloatingViewManager.setFixedTrashIconImage(android.R.drawable.ic_menu_delete);
//        mFloatingViewManager.setActionTrashIconImage(android.R.drawable.ic_menu_rotate);
//        final FloatingViewManager.Options options = new FloatingViewManager.Options();
//        options.shape = FloatingViewManager.SHAPE_CIRCLE;
//        options.overMargin = DisplayUtils.dpToPx(getResources(), 16);
//        mFloatingViewManager.setDisplayMode(FloatingViewManager.DISPLAY_MODE_SHOW_ALWAYS);
//        mFloatingViewManager.addViewToWindow(iconView, options);


        Log.i("TAG", "launch!");
        startForeground(2525, createNotification());

//        return START_STICKY;
        return START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
//        mFloatingViewManager.removeAllViewToWindow();
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
