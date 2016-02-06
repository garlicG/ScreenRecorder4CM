package com.garlicg.screenrecord4cm;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.media.MediaScannerConnection;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.util.DisplayMetrics;
import android.view.Surface;
import android.view.WindowManager;

import com.garlicg.screenrecord4cm.data.AppStorage;
import com.garlicg.screenrecord4cm.util.Cat;
import com.garlicg.screenrecord4cm.util.DisplayUtils;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class RecordHelper {

    static final String DISPLAY_NAME = "ScreenRecorder4CM";
    static final DateFormat FILE_FORMAT = new SimpleDateFormat("'Cutin'yyyyMMdd-HHmmss'.mp4'", Locale.US);

    static final String EXTRA_MEDIA_PROJECTION_RESULT ="MEDIA_PROJECTION_RESULT";
    static final String EXTRA_VIDEO_PERCENTAGE ="VIDEO_PERCENTAGE";

    public interface Listener {
        void onOutputVideo();
    }

    Context mContext;
    String mOutputFilePath;
    MediaRecorder mMediaRecorder;
    MediaProjectionManager mProjectionManager;
    MediaProjection mProjection;
    VirtualDisplay mDisplay;

    boolean mRunning = false;
    final Listener mListener;


    public RecordHelper(Context context, Listener listener) {
        mContext = context;
        mListener = listener;
        mProjectionManager = (MediaProjectionManager) context.getSystemService(Context.MEDIA_PROJECTION_SERVICE);
    }


    /**
     * Check if be possible to record.
     */
    public boolean checkEnableRecord(Intent data) {
        if (data == null) {
            Cat.sendE(new IllegalArgumentException("Recording intent is null."));
            return false;
        }

        if(!data.hasExtra(EXTRA_MEDIA_PROJECTION_RESULT)){
            Cat.sendE(new IllegalArgumentException("MediaProjection extra is none."));
            return false;
        }

        if(!data.hasExtra(EXTRA_VIDEO_PERCENTAGE)){
            Cat.sendE(new IllegalArgumentException("VideoPercentage extra is none."));
            return false;
        }

        if (AppStorage.Video.dir() == null) {
            Cat.sendE(new IllegalStateException("Can not access output directory"));
            return false;
        }

        return true;
    }


    boolean isRunning() {
        return mRunning;
    }


    void startRecord(Intent data) {
        if(mRunning)return;

        int videoSizePercentage = data.getIntExtra(EXTRA_VIDEO_PERCENTAGE , 100);
        Intent mmResult = data.getParcelableExtra(EXTRA_MEDIA_PROJECTION_RESULT);

        RecordingInfo recordingInfo = getRecordingInfo(videoSizePercentage);

        mMediaRecorder = new MediaRecorder();
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mMediaRecorder.setVideoFrameRate(recordingInfo.frameRate);
        mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        mMediaRecorder.setVideoSize(recordingInfo.width, recordingInfo.height);
        mMediaRecorder.setVideoEncodingBitRate(8 * 1000 * 1000);

        String outputName = FILE_FORMAT.format(new Date());
        mOutputFilePath = new File(AppStorage.Video.dir(), outputName).getAbsolutePath();
        mMediaRecorder.setOutputFile(mOutputFilePath);

        try {
            mMediaRecorder.prepare();
        } catch (IOException e) {
            throw new RuntimeException("Unable to prepare MediaRecorder.", e);
        }

        mProjection = mProjectionManager.getMediaProjection(Activity.RESULT_OK, mmResult);
        Surface surface = mMediaRecorder.getSurface();
        mDisplay = mProjection.createVirtualDisplay(
                DISPLAY_NAME
                , recordingInfo.width
                , recordingInfo.height
                , recordingInfo.density
                , DisplayManager.VIRTUAL_DISPLAY_FLAG_PRESENTATION
                , surface
                , null
                , null);

        mMediaRecorder.start();
        mRunning = true;
    }


    void stopRecording() {
        mRunning = false;

        mProjection.stop();
        mMediaRecorder.stop();
        mMediaRecorder.release();
        mDisplay.release();

        MediaScannerConnection.scanFile(mContext, new String[]{mOutputFilePath}, null,
                new MediaScannerConnection.OnScanCompletedListener() {
                    @Override
                    public void onScanCompleted(String path, final Uri uri) {
                        Cat.i("path:" + path + " uri:" + uri);
                        Bitmap thumbnail = AppStorage.Thumbnail.createVideoThumbnail(path);
                        if(thumbnail != null){
                            long videoId = ContentUris.parseId(uri);
                            AppStorage.Thumbnail.saveThumb(mContext , videoId ,thumbnail);
                        }
                        mListener.onOutputVideo();
                    }
                });
    }


    static final class RecordingInfo {
        final int width;
        final int height;
        final int frameRate;
        final int density;

        RecordingInfo(int width, int height, int frameRate, int density) {
            this.width = width;
            this.height = height;
            this.frameRate = frameRate;
            this.density = density;
        }
    }


    private RecordingInfo getRecordingInfo(int videoSizePercentage) {
        Point displaySize = DisplayUtils.getDisplaySize(mContext);
        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getRealMetrics(displayMetrics);

        int displayWidth = displayMetrics.widthPixels;
        int displayHeight = displayMetrics.heightPixels;
        int displayDensity = displayMetrics.densityDpi;
        Cat.i("displayWidth:" + displaySize.x + " ,displayHeight:" + displaySize.y + " ,displayDensity:" + displayDensity);

        CamcorderProfile camcorderProfile = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);
        int cameraFrameRate = camcorderProfile != null ? camcorderProfile.videoFrameRate : 30;

        return new RecordingInfo(
                displayWidth * videoSizePercentage / 100,
                displayHeight * videoSizePercentage / 100,
                cameraFrameRate,
                displayDensity
        );
    }


}
