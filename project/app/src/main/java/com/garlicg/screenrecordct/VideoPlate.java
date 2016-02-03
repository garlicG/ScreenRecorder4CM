package com.garlicg.screenrecordct;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.garlicg.screenrecordct.plate.Plate;
import com.garlicg.screenrecordct.util.ViewFinder;

import java.text.SimpleDateFormat;
import java.util.Date;

public class VideoPlate extends Plate<VideoPlate.VH>{

    public static VideoPlate newInstance(VideoModel video){
        VideoPlate plate = new VideoPlate();
        plate.video = video;
        return plate;
    }

    public static class VH extends RecyclerView.ViewHolder{
        final ImageView thumbnail;
        final TextView title;
        final TextView duration;
        final TextView size;
        final TextView wh;
        final View delete;

        public VH(View itemView) {
            super(itemView);

            thumbnail = ViewFinder.byId(itemView , R.id.thumbnail);
            title = ViewFinder.byId(itemView , R.id.title);
            duration = ViewFinder.byId(itemView , R.id.duration);
            size = ViewFinder.byId(itemView , R.id.size);
            wh = ViewFinder.byId(itemView , R.id.wh);
            delete = ViewFinder.byId(itemView , R.id.delete);
        }
    }


    @Override
    protected VH onCreateViewHolder(LayoutInflater inflater, ViewGroup parent) {
        return new VH(inflater.inflate(R.layout.plate_video ,parent , false));
    }


    public VideoModel video;

    @Override
    protected void onBind(Context context, VH vh) {
        super.onBind(context, vh);

        String added = DateFormat.getDateFormat(context).format(new Date(video.dataAdded * 1000));
        vh.title.setText("" + added);
        vh.duration.setText("" + video.duration);
        vh.size.setText("" + video.size);
        vh.wh.setText("w" + video.width + "x h" + video.height);
    }


}
