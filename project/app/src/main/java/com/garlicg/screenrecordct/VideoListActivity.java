package com.garlicg.screenrecordct;

import android.content.ContentResolver;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import com.garlicg.screenrecordct.data.Storage;
import com.garlicg.screenrecordct.plate.Plate;
import com.garlicg.screenrecordct.plate.PlateAdapter;
import com.garlicg.screenrecordct.util.Cat;
import com.garlicg.screenrecordct.util.ViewFinder;

import java.util.ArrayList;
import java.util.List;

public class VideoListActivity extends AppCompatActivity
        implements ActivityCompat.OnRequestPermissionsResultCallback{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_videolist);

        // Toolbar
        Toolbar toolbar = ViewFinder.byId(this, R.id.toolbar);
        setSupportActionBar(toolbar);

        // RecyclerView
        RecyclerView recyclerView = ViewFinder.byId(this , R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager lm = new LinearLayoutManager(this , LinearLayoutManager.VERTICAL , false);
        recyclerView.setLayoutManager(lm);

        PlateAdapter adapter = new PlateAdapter(this);
        adapter.setItems(createItems());
        recyclerView.setAdapter(adapter);
        hoge();
    }


    private void hoge(){
        String projection[] = new String[]{
                MediaStore.Video.VideoColumns._ID,
                MediaStore.Video.VideoColumns.DATE_ADDED,
                MediaStore.Video.VideoColumns.DURATION,
                MediaStore.Video.VideoColumns.SIZE,
                MediaStore.Video.VideoColumns.WIDTH,
                MediaStore.Video.VideoColumns.HEIGHT,
        };
//        ThumbnailUtils.createVideoThumbnail()  // 毎回つくってるくさいが自分でファイルキャッシュ用意する？
//        MediaStore.Video.Thumbnails.getThumbnail()

        ContentResolver cr = getContentResolver();
        Cursor c = cr.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                ,projection
                ,MediaStore.Video.VideoColumns.DATA + " LIKE ?"
                ,new String[]{Storage.dir().toString() + "/%"}
                ,null
                );

        if(c != null && !c.isClosed()){
            Cat.i("count:" + c.getCount());
            while (c.moveToNext()){
                Cat.i("---");
                Cat.i("added:" + c.getLong(1));
                Cat.i("duration:" + c.getLong(2));
                Cat.i("size:" + c.getLong(3));
                Cat.i("width:" + c.getLong(4));
                Cat.i("height:" + c.getLong(5));
            }
            Cat.i("count:" + c.getCount());
            c.close();
        }
    }

    /**
     * 表示するアイテムを作成する
     */
    private List<Plate> createItems(){
        ArrayList<Plate> list = new ArrayList<>();
        list.add(VideoPlate.newInstance());
        list.add(VideoPlate.newInstance());
        list.add(VideoPlate.newInstance());
        list.add(VideoPlate.newInstance());
        list.add(VideoPlate.newInstance());
        return list;
    }
}
