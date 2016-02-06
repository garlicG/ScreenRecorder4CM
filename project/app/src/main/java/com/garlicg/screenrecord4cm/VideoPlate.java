package com.garlicg.screenrecord4cm;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.TextView;

import com.garlicg.screenrecord4cm.data.AppStorage;
import com.garlicg.screenrecord4cm.plate.Plate;
import com.garlicg.screenrecord4cm.util.ViewFinder;

public class VideoPlate extends Plate<VideoPlate.VH>{


    public static VideoPlate newInstance(VideoModel video , Handler handler){
        VideoPlate plate = new VideoPlate();
        plate.video = video;
        plate.setHandler(handler);
        return plate;
    }


    public static class VH extends RecyclerView.ViewHolder{
        public static final int CLICK_THUMBNAIL = R.id.thumbnailFrame;
        public static final int CLICK_DELETE = R.id.delete;

        final View thumbnailFrame;
        final ImageView thumbnail;
        final View delete;
        final TextView addedDay;
        final TextView addedTime;
        final TextView duration;
        final TextView size;
        final TextView wh;


        public VH(View itemView, View.OnClickListener listener) {
            super(itemView);

            thumbnailFrame = ViewFinder.byId(itemView , R.id.thumbnailFrame);
            thumbnailFrame.setOnClickListener(listener);
            thumbnail = ViewFinder.byId(itemView , R.id.thumbnail);
            addedDay = ViewFinder.byId(itemView , R.id.addedDay);
            addedTime = ViewFinder.byId(itemView , R.id.addedTime);
            duration = ViewFinder.byId(itemView , R.id.duration);
            size = ViewFinder.byId(itemView , R.id.size);
            wh = ViewFinder.byId(itemView , R.id.wh);
            delete = ViewFinder.byId(itemView , R.id.delete);
            delete.setOnClickListener(listener);
        }
    }


    @Override
    protected VH onCreateViewHolder(LayoutInflater inflater, ViewGroup parent , View.OnClickListener listener) {
        VH vh = new VH(inflater.inflate(R.layout.plate_video ,parent , false) , listener);
        return vh;
    }


    public VideoModel video;


    @Override
    protected void onBind(Context context, VH vh) {

        vh.addedDay.setText(video.getDataAddedDay(context));
        vh.addedTime.setText(video.getDataAddedTime(context));
        vh.duration.setText(video.getDurationText(context));
        vh.size.setText(video.getSizeText());
        vh.wh.setText(video.getWidthHeightText());

        bindThumbnail(context, vh.thumbnail);
    }


    void bindThumbnail(final Context context ,final ImageView appIconView) {
        // 重複排除
        Object tag = appIconView.getTag();
        if(tag != null && tag.equals(video.id)){
            return;
        }

        // タグ付け
        appIconView.setTag(video.id);
        Bitmap bitmap = AppStorage.Thumbnail.getThumbFromMem(video.id);
        if(bitmap != null){
            appIconView.setImageBitmap(bitmap);
            return;
        }

        // loading(clear)
        appIconView.setImageBitmap(null);

        new Thread(new Runnable() {
            @Override
            public void run() {

                final Bitmap thumbnail = AppStorage.Thumbnail.getThumbFromDisk(context , video.id , video.data);
                getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        setImage(appIconView ,thumbnail , true);
                    }
                });
            }
        }).start();
    }


    void setImage(final ImageView iv, final Bitmap image , boolean animate){
        Object tag = iv.getTag();
        if(tag != null && !tag.equals(video.id)){
            return;
        }

        iv.setImageBitmap(image);
        if(animate){
            Animation anim = new AlphaAnimation(0f , 1f);
            anim.setDuration(200);
            iv.startAnimation(anim);
        }
    }
}
