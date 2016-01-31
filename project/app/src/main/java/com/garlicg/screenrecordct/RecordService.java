package com.garlicg.screenrecordct;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.Nullable;

import com.garlicg.cutin.triggerextension.FireIntentBuilder;
import com.garlicg.screenrecordct.util.Cat;

/**
 * 録画を行うことができるFloatingService
 */
public class RecordService extends Service implements FloatingManager.Listener , RecordHelper.Listener{

    /** 命令 */
    public static final String EXTRA_ORDER = "ORDER";
    public static final int ORDER_START = 1;
    public static final int ORDER_QUIT = 2;
    public static final String EXTRA_FIRE_CUTIN_OFFSET = "FIRE_CUTIN_OFFSET";
    public static final String EXTRA_AUTO_STOP = "AUTO_STOP";
    public static final String EXTRA_TRIGGER_TITLE = "TRIGGER_TITLE";
    public static final String EXTRA_TRIGGER_MESSAGE = "TRIGGER_MESSAGE";


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
        intent.putExtra(EXTRA_ORDER, ORDER_START);
        intent.putExtra(EXTRA_FIRE_CUTIN_OFFSET, prefs.getFireCutinOffsetMilliSec());
        intent.putExtra(EXTRA_AUTO_STOP, prefs.getAutoStopMilliSec());
        intent.putExtra(EXTRA_TRIGGER_TITLE, prefs.getTriggerTitle());
        intent.putExtra(EXTRA_TRIGGER_MESSAGE, prefs.getTriggerMessage());
        intent.putExtra(RecordHelper.EXTRA_VIDEO_PERCENTAGE , prefs.getVideoPercentage());
        intent.putExtra(RecordHelper.EXTRA_MEDIA_PROJECTION_RESULT, mediaProjectionResult);
        intent.putExtra(FloatingManager.EXTRA_INVISIBLE_RECORD , prefs.getInvisibleRecord());

        return intent;
    }


    /**
     * サービス終了をリクエストする
     *
     * 仕様として、設定画面開いたときに録画サービス表示があれば閉じる。
     * 録画中の終了リクエストはATフィールド
     *
     * @return 終了リクエストを投げたか
     */
    public static boolean requestQuit(Context context){
        if(!isRunning(context)) return false;

        Intent intent = new Intent(context , RecordService.class);
        intent.putExtra(EXTRA_ORDER , ORDER_QUIT);
        context.startService(intent);
        return true;
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
                mFloatingManager.updateParams(intent);
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
    public void onRequestStartRecord() {

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mRecordHelper.startRecord(mIntent);
                mHandler.postDelayed(mCutinFire
                        , mIntent.getIntExtra(EXTRA_FIRE_CUTIN_OFFSET, 0));
            }
        }, 10);
    }


    @Override
    public void onRequestStopRecord() {
        if(mRecordHelper.isRunning()){
            mRecordHelper.stopRecording();
            mHandler.removeCallbacks(mCutinFire);
            mHandler.removeCallbacks(mAutoStop);
        }
    }


    private Runnable mCutinFire = new Runnable() {
        @Override
        public void run() {
            FireIntentBuilder builder = new FireIntentBuilder(RecordService.this, StartRecordTrigger.ID);
            builder.setContentTitle(mIntent.getStringExtra(EXTRA_TRIGGER_TITLE));
            builder.setContentMessage(mIntent.getStringExtra(EXTRA_TRIGGER_MESSAGE));
            sendBroadcast(builder.intent());

            int autoStop = mIntent.getIntExtra(EXTRA_AUTO_STOP , 0);
            if(autoStop >0){
                mHandler.postDelayed(mAutoStop , autoStop);
            }
        }
    };


    private Runnable mAutoStop = new Runnable() {
        @Override
        public void run() {
            mFloatingManager.changeToStoppingState();
            // -> onRequestStopRecord
        }
    };


    @Override
    public boolean onHandleTrashDrop() {
        if(mRecordHelper.isRunning()) return false;

        mFloatingManager.dismissAsHitTrash(200);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                stopSelf();
            }
        },200);
        return true;
    }


    @Override
    public boolean onHandleSettingsDrop() {
        if(mRecordHelper.isRunning()) return false;

        mFloatingManager.dismissAsHitSettings(200);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(RecordService.this , SettingsActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        }, 200);
        return true;

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
