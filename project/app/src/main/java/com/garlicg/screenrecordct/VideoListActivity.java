package com.garlicg.screenrecordct;

import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
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
import com.garlicg.screenrecordct.util.Toaster;
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
        mRecyclerView = recyclerView;


        // 書き込み許可ないときはディレクトリを作成できない
        // 初回起動時でこの画面に遷移したときによくあるケースと想定される
        // ディレクトリ作成できない場合はユースケース的にアイテムなしでOK
        File videoDir = AppStorage.Video.dir();
        if(videoDir != null){
            loadVideoList(videoDir.toString());
        }
    }


    void loadVideoList(@NonNull String dirPath){
        ContentAccessor ca = new ContentAccessor(this);
        ca.setQuery(VideoModel.PROJECTION
                , MediaStore.Video.VideoColumns.DATA + " LIKE ?"
                , new String[]{dirPath + "/%"}
                , MediaStore.Video.VideoColumns.DATE_ADDED + " desc");
        ca.startQuery(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, mVideoLoaded);
    }


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


    void bindAdapter(List<Plate> list){
        PlateAdapter adapter = new PlateAdapter(this);
        adapter.setItems(list);
        adapter.setOnPlateClickListener(mPlateClick);
        mRecyclerView.setAdapter(adapter);
    }


    OnPlateClickListener mPlateClick = new OnPlateClickListener() {
        @Override
        public void onPlateClick(View v, int position, Plate plate) {
            int id = v.getId();

            if (plate instanceof VideoPlate) {
                VideoPlate vp = (VideoPlate) plate;

                switch (id){
                    case VideoPlate.VH.CLICK_THUMBNAIL:
                        Toaster.show(v.getContext() , "THUMB:" + vp.video.duration);

                    case VideoPlate.VH.CLICK_DELETE:
                        Toaster.show(v.getContext() , "DELETE"+ vp.video.duration);
                }
            }
        }
    };



}
