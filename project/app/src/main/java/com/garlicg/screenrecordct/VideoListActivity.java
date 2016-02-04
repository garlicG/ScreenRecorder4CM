package com.garlicg.screenrecordct;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.garlicg.screenrecordct.data.AppStorage;
import com.garlicg.screenrecordct.data.AsyncExecutor;
import com.garlicg.screenrecordct.data.ContentAccessor;
import com.garlicg.screenrecordct.plate.Plate;
import com.garlicg.screenrecordct.plate.PlateAdapter;
import com.garlicg.screenrecordct.util.Cat;
import com.garlicg.screenrecordct.util.DisplayUtils;
import com.garlicg.screenrecordct.util.ViewFinder;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.garlicg.screenrecordct.plate.PlateAdapter.OnPlateClickListener;

public class VideoListActivity extends AppCompatActivity
        implements ActivityCompat.OnRequestPermissionsResultCallback{

    private RecyclerView mRecyclerView;
    private Handler mSubHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSubHandler = new Handler(getMainLooper());

        setContentView(R.layout.activity_videolist);

        // Toolbar
        Toolbar toolbar = ViewFinder.byId(this, R.id.toolbar);
        setSupportActionBar(toolbar);

        // RecyclerView
        RecyclerView recyclerView = ViewFinder.byId(this , R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager lm = new LinearLayoutManager(this , LinearLayoutManager.VERTICAL , false);
        recyclerView.setLayoutManager(lm);
        recyclerView.addItemDecoration(new Decoration(this));
        mRecyclerView = recyclerView;


        // 書き込み許可ないときはディレクトリを作成できない
        // 初回起動時でこの画面に遷移したときによくあるケースと想定される
        // ディレクトリ作成できない場合はユースケース的にアイテムなしでOK
        File videoDir = AppStorage.Video.dir();
        if(videoDir != null){
            loadVideoList(videoDir.toString());
        }
    }


    /**
     * Decoration for RecyclerView
     */
    static class Decoration extends RecyclerView.ItemDecoration{

        final int mVerticalSpace;
        final int mHorizontalSpace;

        public Decoration(Context context){
            mVerticalSpace = DisplayUtils.dpToPx(context.getResources() , 8);
            mHorizontalSpace = mVerticalSpace;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            if(parent.getChildAdapterPosition(view) == 0){
                outRect.top = mVerticalSpace;
            }
            outRect.bottom = mVerticalSpace;
            outRect.left = mHorizontalSpace;
        }
    }


    /**
     * 動画一覧を読み込む
     */
    void loadVideoList(@NonNull String dirPath){
        ContentAccessor ca = new ContentAccessor(this);
        ca.setQuery(VideoModel.PROJECTION
                , MediaStore.Video.VideoColumns.DATA + " LIKE ?"
                , new String[]{dirPath + "/%"}
                , MediaStore.Video.VideoColumns.DATE_ADDED + " desc");
        ca.startQuery(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, mVideoLoaded);
    }


    /**
     * 動画一覧読み込みのコールバック
     */
    AsyncExecutor.Listener<Cursor> mVideoLoaded = new AsyncExecutor.Listener<Cursor>() {
        @Override
        public void onPostExecute(Cursor c) {
            if(isFinishing()){
                if(c != null) c.close();
                return;
            }

            ArrayList<Plate> list = new ArrayList<>();
            if(c != null){
                while (c.moveToNext()){
                    VideoModel model = VideoModel.from(c);
                    list.add(VideoPlate.newInstance(model , mSubHandler));
                }
                c.close();
            }
            bindAdapter(list);
        }
    };


    /**
     * Adapterをセットする
     */
    void bindAdapter(List<Plate> list){
        PlateAdapter adapter = new PlateAdapter(this);
        adapter.setItems(list);
        adapter.setOnPlateClickListener(mPlateClick);
        mRecyclerView.setAdapter(adapter);
    }


    /**
     * アイテムのクリック
     */
    OnPlateClickListener mPlateClick = new OnPlateClickListener() {
        @Override
        public void onPlateClick(View v, int position, Plate plate) {
            if(!(plate instanceof VideoPlate))return;

            int id = v.getId();
            VideoPlate vp = (VideoPlate) plate;

            // サムネクリックで動画ViewIntent
            if(VideoPlate.VH.CLICK_THUMBNAIL == id){
                startViewVideo(vp);
            }
            // 削除
            else if(VideoPlate.VH.CLICK_DELETE == id){
                showDeleteConfirmation(vp);
            }
        }
    };


    void startViewVideo(VideoPlate vp){
        Uri uri = ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI , vp.video.id);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(uri , "video/*");
        try {
            startActivity(intent);
        }catch (ActivityNotFoundException e){
            e.printStackTrace();
            Cat.sendE(e);
        }
    }


    void showDeleteConfirmation(final VideoPlate vp){
        AlertDialog.Builder ab = new AlertDialog.Builder(this , R.style.DarkAlertDialogStyle);
        ab.setMessage(R.string.message_confirm_delete);
        ab.setNegativeButton(android.R.string.cancel, null);
        ab.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startDeleteVideo(vp);
            }
        });
        ab.create().show();
    }


    private boolean mDeleting = false;

    void startDeleteVideo(final VideoPlate vp){
        if(mDeleting)return;
        mDeleting = true;

        Uri uri = ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI , vp.video.id);
        ContentAccessor ca = new ContentAccessor(this);
        ca.setDeleteItem(uri);
        ca.startDeleteItem(new AsyncExecutor.Listener<Integer>() {
            @Override
            public void onPostExecute(Integer integer) {
                if(integer == 0)return;
                if(isFinishing())return;
                PlateAdapter adapter = (PlateAdapter) mRecyclerView.getAdapter();
                adapter.removeItem(vp);
                mDeleting = false;
            }
        });
    }
}
