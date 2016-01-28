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
import android.widget.Spinner;

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
        View launchButton = ViewFinder.byId(this, R.id.startChatHead);
        launchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tryRecordService();
            }
        });

        // video size
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
                spinner.setOnItemSelectedListener(mVideoSizeSelected);
            }
        });
    }


    private AdapterView.OnItemSelectedListener mVideoSizeSelected = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            if (view == null) return;

            VideoPercentage item = (VideoPercentage) parent.getItemAtPosition(position);
            new AppPrefs(SettingsActivity.this).saveVideoPercentage(item);
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            // none
        }
    };


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
