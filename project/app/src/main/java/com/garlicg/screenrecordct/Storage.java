package com.garlicg.screenrecordct;

import android.os.Environment;
import android.support.annotation.Nullable;

import java.io.File;

public class Storage {

    private static final String DIRECTORY_NAME = "ScreenRecorderCT";

    public static @Nullable File dir(){
        File parent = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
        File appDir = new File(parent ,DIRECTORY_NAME);
        if(appDir.mkdirs() || appDir.isDirectory()){
            return appDir;
        }
        return null;
    }


}
