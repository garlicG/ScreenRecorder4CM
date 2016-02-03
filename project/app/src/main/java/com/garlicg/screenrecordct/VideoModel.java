package com.garlicg.screenrecordct;

import android.database.Cursor;
import android.provider.MediaStore;

public class VideoModel {

    public long id;
    public long dataAdded;
    public long duration;
    public long size;
    public int width;
    public int height;
    public String data;


    public static final String[] PROJECTION = new String[]{
            MediaStore.Video.VideoColumns._ID,
            MediaStore.Video.VideoColumns.DATE_ADDED,
            MediaStore.Video.VideoColumns.DURATION,
            MediaStore.Video.VideoColumns.SIZE,
            MediaStore.Video.VideoColumns.WIDTH,
            MediaStore.Video.VideoColumns.HEIGHT,
            MediaStore.Video.VideoColumns.DATA,
    };


    public static VideoModel from(Cursor c){
        VideoModel model = new VideoModel();
        model.id = c.getLong(0);
        model.dataAdded = c.getLong(1);
        model.duration = c.getLong(2);
        model.size = c.getLong(3);
        model.width = c.getInt(4);
        model.height = c.getInt(5);
        model.data = c.getString(6);
        return model;
    }

}
