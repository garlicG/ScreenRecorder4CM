package com.garlicg.screenrecord4cm;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import com.garlicg.cutin.triggerextension.ResultBundleBuilder;
import com.garlicg.screenrecord4cm.data.AppPrefs;
import com.garlicg.screenrecord4cm.data.AppStorage;
import com.garlicg.screenrecord4cm.data.AsyncExecutor;
import com.garlicg.screenrecord4cm.data.ContentAccessor;
import com.garlicg.screenrecord4cm.util.ViewFinder;

import java.io.File;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class SettingsActivity extends AppCompatActivity
        implements ActivityCompat.OnRequestPermissionsResultCallback {

    private static final int REQUEST_CAPTURE = 1;
    private static final int REQUEST_STICKY_CAPTURE = 2;
    private static final int REQUEST_STORAGE_PERMISSION_FOR_RECORD = 3;
    private static final int REQUEST_STORAGE_PERMISSION_FOR_VIDEO_LIST = 4;

    private AppPrefs mPrefs;

    /**
     * 録画サービスが存在する状態からこのActivityが起動された場合は
     * ・Activity終了時に再度録画サービスが起動される
     * ・起動ボタンが表示されない
     * ただしパーミッションがある場合に限る
     */
    private static final String KEY_STICKY = "STICKY";
    private boolean mSticky = false;


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_STICKY , mSticky);
    }


    void restore(Bundle savedInstanceState){
        mSticky = savedInstanceState.getBoolean(KEY_STICKY , false);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(savedInstanceState == null){
            mSticky = RecordService.requestQuit(this);
        }else {
            restore(savedInstanceState);
        }


        mPrefs = new AppPrefs(this);

        setContentView(R.layout.activity_settings);

        // Toolbar
        Toolbar toolbar = ViewFinder.byId(this, R.id.toolbar);
        setSupportActionBar(toolbar);

        // launchButton
        View launchButton = ViewFinder.byId(this, R.id.startFloating);
        launchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tryRecordService();
            }
        });
        launchButton.setVisibility(mSticky && isGrantedStoragePermission()
                ? View.GONE
                : View.VISIBLE);

        createVideoSize(savedInstanceState);
        createFireCutin(savedInstanceState);
        createAutoStop(savedInstanceState);
        createTriggerTitle(savedInstanceState);
        createTriggerMessage(savedInstanceState);
        createInvisibleRecord(savedInstanceState);
        createVideoList(savedInstanceState);
    }


    @Override
    protected void onStart() {
        super.onStart();
        invalidateVideoCount();
    }


    @Override
    public void onBackPressed() {
        if(mSticky && isGrantedStoragePermission()){
            requestCapture(REQUEST_STICKY_CAPTURE);
        }
        else{
            super.onBackPressed();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // from #requestCapture onClick LaunchButton
        if (requestCode == REQUEST_CAPTURE) {
            if (resultCode != RESULT_OK || data == null) return;
            Intent intent = RecordService.newStartIntent(this, data);
            startService(intent);
            finish();
        }
        // from #requestCapture onBackPress
        else if(requestCode == REQUEST_STICKY_CAPTURE){
            if(resultCode == RESULT_OK && data != null){
                Intent intent = RecordService.newStartIntent(this, data);
                startService(intent);
            }
            finish();
        }
    }


    @Override
    public void finish() {
        // カットインマネージャーからの起動のみ想定
        // RecentTask起動とかはたぶんもんだいない
        ResultBundleBuilder builder = new ResultBundleBuilder(this);
        builder.add(new StartRecordTrigger(this));

        Intent intent = new Intent();
        intent.putExtras(builder.build());
        setResult(RESULT_OK, intent);

        super.finish();
    }


    /**
     * Create video size setting view
     */
    private void createVideoSize(Bundle savedInstanceState) {
        int vp = mPrefs.getVideoPercentage();
        final VideoPercentage[] spinnerItems = AppPrefs.videoPercentages();
        int spinnerSelection = AppPrefs.findVideoPercentagePosition(spinnerItems, vp);

        ArrayAdapter<VideoPercentage> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, spinnerItems);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        final Spinner spinner = ViewFinder.byId(this, R.id.videoPercentageSpinner);
        spinner.setAdapter(adapter);

        spinner.setSelection(spinnerSelection);
        spinner.post(new Runnable() {
            @Override
            public void run() {
                // setSelection later
                spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        if (view == null) return;

                        VideoPercentage item = (VideoPercentage) parent.getItemAtPosition(position);
                        new AppPrefs(view.getContext()).saveVideoPercentage(item);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                });
            }
        });
    }


    /**
     * Create auto stop setting view
     */
    private void createFireCutin(Bundle savedInstanceState){

        // init value setup
        final TextView valueView = ViewFinder.byId(this, R.id.fireCutinValue);
        int value = mPrefs.getFireCutinOffsetMilliSec();
        valueView.setText(getString(R.string.x_ms_later, value));

        // handle value from dialog callback
        final InputSecondDialogBuilder.Callback callback = new InputSecondDialogBuilder.Callback() {
            @Override
            public boolean onValidate(int value) {
                int msec = value * 100;
                //noinspection PointlessArithmeticExpression
                return msec >= 0 && msec <= 1 * 1000 * 1000 - 1; // 999秒
            }
            @Override
            public void onOk(int value) {
                int msec = value * 100;
                valueView.setText(getString(R.string.x_ms_later, msec));
                mPrefs.saveFireCutinOffsetMilliSec(msec);
            }
        };

        // show dialog on click
        valueView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int value = mPrefs.getFireCutinOffsetMilliSec() / 100;
                AlertDialog ad = InputSecondDialogBuilder.build(v.getContext(), value, getString(R.string.unit_00ms_later), callback);
                ad.show();
            }
        });
    }


    /**
     * Create auto stop setting view
     */
    private void createAutoStop(Bundle savedInstanceState){

        // init value setup
        final TextView valueView = ViewFinder.byId(this, R.id.autoStopValue);
        int value = mPrefs.getAutoStopMilliSec();
        valueView.setText(value == 0
                        ? getString(R.string.no_seconds_only_manual_stop)
                        : getString(R.string.plus_x_ms_later, value)
        );

        // handle value from dialog callback
        final InputSecondDialogBuilder.Callback callback = new InputSecondDialogBuilder.Callback() {
            @Override
            public boolean onValidate(int value) {
                int msec = value * 100;
                //noinspection PointlessArithmeticExpression
                return msec >= 0 && msec <= 1 * 1000 * 1000 - 1; // 999秒
            }
            @Override
            public void onOk(int value) {
                int msec = value * 100;
                valueView.setText(msec == 0
                        ? getString(R.string.no_seconds_only_manual_stop)
                        : getString(R.string.plus_x_ms_later, msec)
                );

                mPrefs.saveAutoStopMilliSec(msec);
            }
        };

        // show dialog on click
        valueView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int value = mPrefs.getAutoStopMilliSec() / 100;
                AlertDialog ad = InputSecondDialogBuilder.build(v.getContext(), value, getString(R.string.unit_00ms_later), callback);
                ad.show();
            }
        });
    }


    /**
     * TriggerTitle
     */
    private void createTriggerTitle(Bundle savedInstanceState) {
        final TextView valueView = ViewFinder.byId(this, R.id.triggerTitleValue);
        String value = mPrefs.getTriggerTitle();
        valueView.setText(value);

        // handle value from dialog callback
        final ValidateTextDialogBuilder.Callback callback = new ValidateTextDialogBuilder.Callback() {
            @Override
            public boolean onValidate(CharSequence value) {
                return !TextUtils.isEmpty(value);
            }
            @Override
            public void onOk(CharSequence value) {
                valueView.setText(value);
                mPrefs.saveTriggerTitle(value.toString());
            }
        };

        // show dialog on click
        valueView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String value = mPrefs.getTriggerTitle();
                AlertDialog ad = ValidateTextDialogBuilder.build(v.getContext(), value, null, 50, callback);
                ad.show();
            }
        });
    }


    /**
     * TriggerMessage
     */
    private void createTriggerMessage(Bundle savedInstanceState) {
        final TextView valueView = ViewFinder.byId(this, R.id.triggerMessageValue);
        String value = mPrefs.getTriggerMessage();
        valueView.setText(value);

        // handle value from dialog callback
        final ValidateTextDialogBuilder.Callback callback = new ValidateTextDialogBuilder.Callback() {
            @Override
            public boolean onValidate(CharSequence value) {
                return !TextUtils.isEmpty(value);
            }
            @Override
            public void onOk(CharSequence value) {
                valueView.setText(value);
                mPrefs.saveTriggerMessage(value.toString());
            }
        };

        // show dialog on click
        valueView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String value = mPrefs.getTriggerMessage();
                AlertDialog ad = ValidateTextDialogBuilder.build(v.getContext(), value, null, 100, callback);
                ad.show();
            }
        });
    }


    /**
     * InvisibleRecord
     */
    private void createInvisibleRecord(Bundle savedInstanceState) {
        boolean value = mPrefs.getInvisibleRecord();
        final Switch sw = ViewFinder.byId(this, R.id.invisibleRecord);
        sw.setChecked(value);
        sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mPrefs.saveInvisibleRecord(isChecked);
            }
        });
    }


    /**
     * VideoList
     */
    private void createVideoList(Bundle savedInstanceState) {
        View touchFrame = ViewFinder.byId(this, R.id.videoList);
        touchFrame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isGrantedStoragePermission()) {
                    startVideoListActivity();
                } else {
                    requestStoragePermission(REQUEST_STORAGE_PERMISSION_FOR_VIDEO_LIST);
                }
            }
        });

        // init count
        TextView titleView = ViewFinder.byId(this, R.id.videoListTitle);
        titleView.setText(getString(R.string.video_list_x, 0));
    }


    /**
     * 動画数表示を更新する
     */
    private void invalidateVideoCount(){
        final TextView titleView = ViewFinder.byId(this , R.id.videoListTitle);
        titleView.setText(getString(R.string.video_list));

        // アクセスできる場合のみカウントを取得
        File videoDir = AppStorage.Video.dir();
        if(videoDir != null && isGrantedStoragePermission()){

            ContentAccessor ca = new ContentAccessor(this);
            ca.setQuery(new String[]{MediaStore.Video.VideoColumns._ID}
                    , MediaStore.Video.VideoColumns.DATA + " LIKE ?"
                    , new String[]{videoDir + "/%"}
                    , null);

            ca.startQuery(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, new AsyncExecutor.Listener<Cursor>() {
                @Override
                public void onPostExecute(final Cursor c) {
                    int videoNum = 0;
                    if(c != null){
                        videoNum = c.getCount();
                        c.close();
                    }
                    if(isFinishing())return;
                    titleView.setText(getString(R.string.video_list_x, videoNum));
                }
            });
        }
    }


    /**
     * 録画サービス立ち上げるけどパーミッションの壁が立ちはだかる
     * <p/>
     * RuntimePermission (ExternalStorage)
     * -> request intent for MEDIA_PROJECTION_SERVICE
     * -> start RecordService
     */
    private void tryRecordService() {
        if (isGrantedStoragePermission()) {
            requestCapture(REQUEST_CAPTURE);
        } else {
            requestStoragePermission(REQUEST_STORAGE_PERMISSION_FOR_RECORD);
        }
    }


    /**
     * 録画サービスを開始する
     */
    void requestCapture(int requestCode) {
        MediaProjectionManager mm = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
        Intent intent = mm.createScreenCaptureIntent();
        startActivityForResult(intent, requestCode);
    }

    /**
     * 動画一覧Activityを表示する
     */
    void startVideoListActivity(){
        Intent intent = new Intent(SettingsActivity.this, VideoListActivity.class);
        startActivity(intent);
    }


    /**
     * ストレージ権限をリクエストする
     */
    void requestStoragePermission(int requestCode){
        ActivityCompat.requestPermissions(this, new String[]{WRITE_EXTERNAL_STORAGE}, requestCode);
    }


    /**
     * ストレージ権限があるか
     */
    boolean isGrantedStoragePermission(){
        return ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED;
    }


    @SuppressWarnings("ConstantConditions")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // 許可されなかった
        if (grantResults.length == 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            // 理由を表示するパターンのとき
            if (!ActivityCompat.shouldShowRequestPermissionRationale(this, WRITE_EXTERNAL_STORAGE)) {
                String message = requestCode == REQUEST_STORAGE_PERMISSION_FOR_RECORD
                        ? getString(R.string.message_reason_record_storage)
                        : getString(R.string.message_reason_view_videolist);
                showPermissionRationale(message);
            }
            return;
        }

        // next step
        if(requestCode == REQUEST_STORAGE_PERMISSION_FOR_RECORD){
            requestCapture(REQUEST_CAPTURE);
        }
        else if(requestCode == REQUEST_STORAGE_PERMISSION_FOR_VIDEO_LIST){
            startVideoListActivity();
        }

    }


    /**
     * 権限理由を説明して設定に飛ばすダイアログを表示する
     */
    private void showPermissionRationale(String reason){
        AlertDialog.Builder ab = new AlertDialog.Builder(this);
        ab.setMessage(reason);
        ab.setPositiveButton(R.string.settings, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivity(intent);
            }
        });
        ab.create().show();
    }
}
