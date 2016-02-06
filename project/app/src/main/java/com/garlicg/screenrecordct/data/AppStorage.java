package com.garlicg.screenrecordct.data;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class AppStorage {


    public static boolean saveBitmap(File file , Bitmap bitmap){
        return saveBitmap(file , bitmap , Bitmap.CompressFormat.PNG);
    }


    public static boolean saveBitmap(File file , Bitmap bitmap , Bitmap.CompressFormat format){
        try {
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG ,90 , fos);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }


    /**
     * 動画ストレージ
     */
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


    /**
     * サムネイルストレージ
     */
    public static class Thumbnail{

        private static final String DIRECTORY_NAME = ".thumb";

        public static File dir(Context context){
            File parent = context.getCacheDir();
            File dir = new File(parent ,DIRECTORY_NAME);
            dir.mkdirs();
            return dir;
        }


        public static @Nullable Bitmap getThumbFromMem(long videoId){
            return ThumbCache.getInstance().get(videoId);
        }


        public static Bitmap getThumbFromDisk(Context context , long videoId , String path){
            Bitmap bitmap = getThumb(context, videoId);
            if(bitmap != null){
                ThumbCache.getInstance().put(videoId, bitmap);
            }
            else{
                if(path != null){
                    bitmap = ThumbnailUtils.createVideoThumbnail(path, MediaStore.Video.Thumbnails.MINI_KIND);
                }
                // failed to create thumbnail from video
                // 動画時間が短いときなどは作成できない
                if(bitmap == null){
                    bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
                }
                saveThumb(context ,videoId , bitmap);
                ThumbCache.getInstance().put(videoId, bitmap);
            }
            return bitmap;
        }


        public static @Nullable Bitmap getThumb(Context context ,long videoId){
            File dir = dir(context);
            File thumb = new File(dir , Long.toString(videoId));
            if(!thumb.exists())return null;
            return BitmapFactory.decodeFile(thumb.toString() , null);
        }


        public static boolean saveThumb(Context context , long videoId , Bitmap b){
            File dir = dir(context);
            File thumb = new File(dir , Long.toString(videoId));
            return saveBitmap(thumb , b);
        }


        public static boolean deleteThumb(Context context , long videoId){
            File dir = dir(context);
            File thumb = new File(dir , Long.toString(videoId));
            return thumb.delete();
        }

    }
}
