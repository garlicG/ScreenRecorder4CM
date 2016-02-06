package com.garlicg.screenrecord4cm;

import android.content.Context;
import android.support.annotation.Nullable;

import com.garlicg.cutin.triggerextension.TriggerSetting;
import com.garlicg.screenrecord4cm.data.AppPrefs;

public class StartRecordTrigger implements TriggerSetting {

    public static final long ID = 1;
    private final String mTriggerName;
    private final String mContentTitleHint;
    private final String mContentMessageHint;

    public StartRecordTrigger(Context context) {
        AppPrefs prefs = new AppPrefs(context);
        mTriggerName = context.getString(R.string.when_start_record);
        mContentTitleHint = prefs.getTriggerTitle();
        mContentMessageHint = prefs.getTriggerMessage();
    }


    @Override
    public long getId() {
        return ID;
    }


    @Override
    public String getTriggerName() {
        return mTriggerName;
    }


    @Nullable
    @Override
    public String getContentTitleHint() {
        return mContentTitleHint;
    }


    @Nullable
    @Override
    public String getContentMessageHint() {
        return mContentMessageHint;
    }

}
