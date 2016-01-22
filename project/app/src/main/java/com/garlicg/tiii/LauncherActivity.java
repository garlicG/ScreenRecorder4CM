package com.garlicg.tiii;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;

import com.garlicg.tiii.util.ExtDebugTree;
import com.garlicg.tiii.util.ViewFinder;

import timber.log.Timber;

/**
 */
public class LauncherActivity extends Activity {

    /**
     * シンプルなFloatingViewを表示するフローのパーミッション許可コード
     */
    private static final int CHATHEAD_OVERLAY_PERMISSION_REQUEST_CODE = 100;


    /**
     * onCreate
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Timber.plant(new ExtDebugTree());

        setContentView(R.layout.launcher);
        View startChatHead = ViewFinder.byId(this , R.id.startChatHead);
        startChatHead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext() , BubbleSampleActivity.class);
                startActivity(intent);
            }
        });

        View stopChatHead = ViewFinder.byId(this , R.id.stopChatHead);
        stopChatHead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CHATHEAD_OVERLAY_PERMISSION_REQUEST_CODE) {
            final Context context = this;
            final boolean canShow = showChatHead(context);
            if (!canShow) {
                Log.w("TODO", "permission denied");
            }
        }
    }

    /**
     * シンプルなFloatingViewの表示
     *
     * @param context Context
     * @return 表示できる場合はtrue, 表示できない場合はfalse
     */
    @SuppressLint("NewApi")
    private boolean showChatHead(Context context) {
        // API22以下かチェック
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1) {
            context.startService(new Intent(context, FloatingService.class));
            return true;
        }

        // 他のアプリの上に表示できるかチェック
        if (Settings.canDrawOverlays(context)) {
            context.startService(new Intent(context, FloatingService.class));
            return true;
        }

        return false;
    }
}
