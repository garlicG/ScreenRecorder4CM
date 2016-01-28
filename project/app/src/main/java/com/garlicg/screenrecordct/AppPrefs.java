package com.garlicg.screenrecordct;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class AppPrefs {

    private SharedPreferences mPrefs;
    private Context mContext;

    public AppPrefs(Context context) {
        mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        mContext = context;
    }


    private final static String VIDEO_PERCENTAGE_V1="VIDEO_PERCENTAGE_V1";

    public static final VideoPercentage[] videoPercentages(){
        return new VideoPercentage[]{
                new VideoPercentage(100),
                new VideoPercentage(75),
                new VideoPercentage(50),
        };
    }

    public static int findVideoPercentagePosition(VideoPercentage[] values , int value){
        for(int i = 0 ; i < values.length ; i++){
            if (values[i].percentage == value) return i;
        }
        return 0;
    }

    public int getVideoPercentage(){
        return mPrefs.getInt(VIDEO_PERCENTAGE_V1 , 100);
    }

    public void saveVideoPercentage(VideoPercentage value) {
        mPrefs.edit().putInt(VIDEO_PERCENTAGE_V1, value.percentage).apply();
    }


    private final static String FIRE_CUTIN_OFFSET_MSEC ="FIRE_CUTIN_OFFSET_MSEC";

    public int getFireCutinOffsetSec() {
        return mPrefs.getInt(FIRE_CUTIN_OFFSET_MSEC, 1000) / 1000;
    }

    public void saveFireCutinOffsetSec(int sec) {
        mPrefs.edit().putInt(FIRE_CUTIN_OFFSET_MSEC, sec * 1000).apply();
    }



}
