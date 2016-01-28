package com.garlicg.screenrecordct;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import com.garlicg.screenrecordct.util.Toaster;
import com.garlicg.screenrecordct.util.ViewFinder;

public class SettingsActivity extends AppCompatActivity
        implements ActivityCompat.OnRequestPermissionsResultCallback
{

    private static final int REQUEST_CAPTURE = 1;
    private static final int REQUEST_WRITE_EXTERNAL_STORAGE = 2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        RecordService.requestQuit(this);
        AppPrefs prefs = new AppPrefs(this);

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

        createVideoSize(savedInstanceState, prefs);
        createFireCutin(savedInstanceState, prefs);
        createAutoStop(savedInstanceState, prefs);
        createTriggerTitle(savedInstanceState , prefs);
        createTriggerMessage(savedInstanceState , prefs);
        createInvisibleRecord(savedInstanceState, prefs);
        createVideoList(savedInstanceState , prefs);
    }


    private void createVideoSize(Bundle savedInstanceState , AppPrefs prefs){
        int vp = prefs.getVideoPercentage();
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


    private void createFireCutin(Bundle savedInstanceState , AppPrefs prefs){
        TextView valueView = ViewFinder.byId(this , R.id.fireCutinValue);
        valueView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO
            }
        });
    }


    private void createAutoStop(Bundle savedInstanceState , AppPrefs prefs){
        TextView valueView = ViewFinder.byId(this , R.id.autoStopValue);
        valueView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO
            }
        });
    }


    private void createTriggerTitle(Bundle savedInstanceState , AppPrefs prefs){
        TextView valueView = ViewFinder.byId(this , R.id.triggerTitleValue);
        valueView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO
            }
        });
    }


    private void createTriggerMessage(Bundle savedInstanceState , AppPrefs prefs){
        TextView valueView = ViewFinder.byId(this , R.id.triggerMessageValue);
        valueView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO
            }
        });
    }


    private void createInvisibleRecord(Bundle savedInstanceState , AppPrefs prefs){
        Switch sw = ViewFinder.byId(this , R.id.invisibleRecord);
        sw.setChecked(false);
        sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // TODO
            }
        });
    }


    private void createVideoList(Bundle savedInstanceState , AppPrefs prefs){
        View view = ViewFinder.byId(this , R.id.videoList);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO
            }
        });
    }


    /**
     * 録画サービス立ち上げるけどパーミッションの壁が立ちはだかる
     *
     * RuntimePermission (ExternalStorage)
     * -> request intent for MEDIA_PROJECTION_SERVICE
     * -> start RecordService
     */
    private void tryRecordService() {
        final String permission = Manifest.permission.WRITE_EXTERNAL_STORAGE;
        final int requestCode = REQUEST_WRITE_EXTERNAL_STORAGE;

        if (isGrantedPermission(permission)) {
            requestCapture();
        } else {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                // FIXME Show dialog before show activity_settings Activity.
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivity(intent);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{permission}, requestCode);
            }
        }
    }

    boolean isGrantedPermission(String permission) {
        return ContextCompat.checkSelfPermission(this, permission)
                == PackageManager.PERMISSION_GRANTED;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_WRITE_EXTERNAL_STORAGE) {
            if (grantResults.length == 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED)
                return;
            requestCapture();
        }
    }


    void requestCapture() {
        MediaProjectionManager mm = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
        Intent intent = mm.createScreenCaptureIntent();
        startActivityForResult(intent, REQUEST_CAPTURE);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // from #requestCapture
        if (requestCode == REQUEST_CAPTURE) {
            if (resultCode != RESULT_OK || data == null) return;
            Intent intent = RecordService.newStartIntent(this, data);
            startService(intent);
            finish();
        }
    }


}
