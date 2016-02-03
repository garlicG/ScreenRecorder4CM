package com.garlicg.screenrecordct.data;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.support.annotation.Nullable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class AppStorage {

    public static class Video{

        private static final String DIRECTORY_NAME = "ScreenRecorderCT";

        public static @Nullable File dir(){
            File parent = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
            File dir = new File(parent ,DIRECTORY_NAME);
            if(dir.mkdirs() || dir.isDirectory()){
                return dir;
            }
            return null;
        }
    }

    public static class Thumbnail{

        private static final String DIRECTORY_NAME = ".thumb";

        public static File dir(Context context){
            File parent = context.getCacheDir();
            File dir = new File(parent ,DIRECTORY_NAME);
            dir.mkdirs();
            return dir;
        }


        public static @Nullable Bitmap getThumb(Context context ,long videoId , @Nullable BitmapFactory.Options ops){
            File dir = dir(context);
            File thumb = new File(dir , Long.toString(videoId));
            if(!thumb.exists())return null;

            return BitmapFactory.decodeFile(thumb.toString() , ops);
        }


        public static boolean saveThumb(Context context , long videoId , Bitmap b){
            File dir = dir(context);
            File thumb = new File(dir , Long.toString(videoId));
            try {
                FileOutputStream fos = new FileOutputStream(thumb);
                b.compress(Bitmap.CompressFormat.PNG ,90 , fos);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }


        public static boolean deleteThumb(Context context , long videoId){
            File dir = dir(context);
            File thumb = new File(dir , Long.toString(videoId));
            return thumb.delete();
        }

    }

}
