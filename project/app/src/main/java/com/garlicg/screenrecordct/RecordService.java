package com.garlicg.screenrecordct;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.Nullable;

import com.garlicg.screenrecordct.util.Cat;

/**
 * 録画を行うことができるFloatingService
 */
public class RecordService extends Service implements FloatingManager.Listener , RecordHelper.Listener{

    /** 命令 */
    public static final String EXTRA_ORDER = "ORDER";
    public static final int ORDER_START = 1;
    public static final int ORDER_QUIT = 2;


    /**
     * このサービスを開始するIntentを生成する
     *
     * 仕様として、このサービスIntentが重複して呼ばれることは許容している。
     * 非録画中に再度startされた場合は設定値を更新する
     * 録画中に再度startした場合はATフィールド
     */
    public static Intent newStartIntent(Context context, Intent mediaProjectionResult){
        AppPrefs prefs = new AppPrefs(context);
        Intent intent = new Intent(context , RecordService.class);
        intent.putExtra(RecordHelper.EXTRA_VIDEO_PERCENTAGE , prefs.getVideoPercentage());
        intent.putExtra(RecordHelper.EXTRA_MEDIA_PROJECTION_RESULT, mediaProjectionResult);
        intent.putExtra(EXTRA_ORDER, ORDER_START);
        return intent;
    }


    /**
     * サービス終了をリクエストする
     *
     * 仕様として、設定画面開いたときに録画サービス表示があれば閉じる。
     * 録画中の終了リクエストはATフィールド
     */
    public static void requestQuit(Context context){
        if(!isRunning(context)) return;

        Intent intent = new Intent(context , RecordService.class);
        intent.putExtra(EXTRA_ORDER , ORDER_QUIT);
        context.startService(intent);
    }

    private static boolean isRunning(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo rsi : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (RecordService.class.getName().equals(rsi.service.getClassName())) {
                return true;
            }
        }
        return false;
    }


    private Intent mIntent;
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private FloatingManager mFloatingManager;
    private RecordHelper mRecordHelper;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not support binding");
    }


    @Override
    public void onCreate() {
        super.onCreate();
        mFloatingManager = new FloatingManager(this , this);
        mFloatingManager.setupWindows();
        mRecordHelper = new RecordHelper(this , this);
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int order = intent.getIntExtra(EXTRA_ORDER , -1);

        if(order == ORDER_START){
            // 録画中は処理をしない
            if(mRecordHelper.isRunning()) return START_NOT_STICKY;

            if(mRecordHelper.checkEnableRecord(intent)){
                mIntent = intent;
            }
            else{
                stopSelf();
            }
        }
        else if(order == ORDER_QUIT){
            // 録画中は処理をしない
            if(mRecordHelper.isRunning()) return START_NOT_STICKY;

            stopSelf();
        }
        else{
            Cat.e("Unsupported order " + order);
            // TODO Send fabric
        }

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mFloatingManager.onDestroy();
    }


    @Override
    public void onClickStartRecord() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mRecordHelper.startRecord(mIntent);
            }
        });
    }


    @Override
    public void onClickStopRecord() {
        mRecordHelper.stopRecording();
    }


    @Override
    public void onHitFinishFloating() {
        // TODO return boolean
        stopSelf();
    }


    @Override
    public void onOutputVideo() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mFloatingManager.initState();
            }
        });
    }
}
