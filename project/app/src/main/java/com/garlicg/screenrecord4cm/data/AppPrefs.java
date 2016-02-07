package com.garlicg.screenrecord4cm.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.garlicg.screenrecord4cm.R;
import com.garlicg.screenrecord4cm.VideoPercentage;

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

    public int getFireCutinOffsetMilliSec() {
        return mPrefs.getInt(FIRE_CUTIN_OFFSET_MSEC , 100);
    }

    public void saveFireCutinOffsetMilliSec(int value) {
        mPrefs.edit().putInt(FIRE_CUTIN_OFFSET_MSEC, value).apply();
    }



    private final static String AUTO_STOP_MSEC ="AUTO_STOP_MSEC";

    public int getAutoStopMilliSec() {
        return mPrefs.getInt(AUTO_STOP_MSEC, 3000);
    }

    public void saveAutoStopMilliSec(int value) {
        mPrefs.edit().putInt(AUTO_STOP_MSEC , value).apply();
    }


    private final static String TRIGGER_TITLE = "TRIGGER_TITLE";

    public String getTriggerTitle() {
        return mPrefs.getString(TRIGGER_TITLE, mContext.getString(R.string.trigger_title_default));
    }

    public void saveTriggerTitle(String value) {
        mPrefs.edit().putString(TRIGGER_TITLE, value).apply();
    }


    private final static String TRIGGER_MESSAGE = "TRIGGER_MESSAGE";

    public String getTriggerMessage() {
        return mPrefs.getString(TRIGGER_MESSAGE, mContext.getString(R.string.trigger_message_default));
    }

    public void saveTriggerMessage(String value) {
        mPrefs.edit().putString(TRIGGER_MESSAGE, value).apply();
    }


    private final static String INVISIBLE_RECORD = "INVISIBLE_RECORD";

    public boolean getInvisibleRecord() {
        return mPrefs.getBoolean(INVISIBLE_RECORD, true);
    }

    public void saveInvisibleRecord(boolean value) {
        mPrefs.edit().putBoolean(INVISIBLE_RECORD, value).apply();
    }



}
