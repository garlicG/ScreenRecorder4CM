package com.garlicg.screenrecord4cm;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.text.format.DateFormat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

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


    private String mDataAddedDayText;

    /**
     * 追加日のテキストを取得する
     */
    public String getDataAddedDay(Context context){
        if(mDataAddedDayText == null){
            mDataAddedDayText = DateFormat.getDateFormat(context).format(new Date(dataAdded * 1000));
        }
        return mDataAddedDayText;
    }


    private String mDataAddedTimeText;

    /**
     * 追加時間のテキストを取得する
     */
    public String getDataAddedTime(Context context){
        if(mDataAddedTimeText == null){
            SimpleDateFormat sdf = new SimpleDateFormat("E H:MM" , Locale.ENGLISH);
            mDataAddedTimeText = sdf.format(new Date(dataAdded * 1000));
        }
        return mDataAddedTimeText;
    }


    private String mDurationText;

    /**
     * 動画時間のテキストを取得する
     */
    public String getDurationText(Context context){
        if(mDurationText == null){
            long s = duration / 1000;
            mDurationText = String.format("%02d:%02d", (s % 3600) / 60, (s % 60));
        }
        return mDurationText;
    }


    private String mSizeText;

    /**
     * 動画サイズのテキストを取得する
     */
    public String getSizeText(){
        if(mSizeText == null){
            // http://stackoverflow.com/questions/3758606/how-to-convert-byte-size-into-human-readable-format-in-java
            int unit = 1024;
            if (size < unit) return size + " B";
            int exp = (int) (Math.log(size) / Math.log(unit));
            char pre = ("KMGTPE").charAt(exp - 1);
            mSizeText = String.format("%.1f %sB", size / Math.pow(unit, exp), pre);
        }
        return mSizeText;
    }


    private String mWidthHeightText;

    /**
     * 横×高さのテキストを取得する
     */
    public String getWidthHeightText(){
        if(mWidthHeightText == null){
            mWidthHeightText = Integer.toString(width) + " x " + height;
        }
        return mWidthHeightText;
    }





}
