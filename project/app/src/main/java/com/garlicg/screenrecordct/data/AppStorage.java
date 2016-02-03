package com.garlicg.screenrecordct.data;

import android.os.Environment;
import android.support.annotation.Nullable;

import java.io.File;

public class AppStorage {

    private static final String DIRECTORY_NAME = "ScreenRecorderCT";

    public static @Nullable File videoDir(){
        File parent = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
        File dir = new File(parent ,DIRECTORY_NAME);
        if(dir.mkdirs() || dir.isDirectory()){
            return dir;
        }
        return null;
    }


}
