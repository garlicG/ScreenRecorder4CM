package com.garlicg.screenrecordct;

import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import com.garlicg.screenrecordct.plate.Plate;
import com.garlicg.screenrecordct.plate.PlateAdapter;
import com.garlicg.screenrecordct.util.ViewFinder;

import java.util.ArrayList;

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
        ArrayList<Plate> list = new ArrayList<>();
        list.add(VideoPlate.newInstance());
        list.add(VideoPlate.newInstance());
        list.add(VideoPlate.newInstance());
        list.add(VideoPlate.newInstance());
        list.add(VideoPlate.newInstance());
        adapter.setItems(list);
        recyclerView.setAdapter(adapter);
    }
}
