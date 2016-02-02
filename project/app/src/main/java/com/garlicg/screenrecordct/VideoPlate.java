package com.garlicg.screenrecordct;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.garlicg.screenrecordct.plate.Plate;
import com.garlicg.screenrecordct.util.ViewFinder;

public class VideoPlate extends Plate<VideoPlate.VH>{

    public static VideoPlate newInstance(){
        return new VideoPlate();
    }

    public static class VH extends RecyclerView.ViewHolder{
        final ImageView thumbnail;
        final TextView title;
        final TextView time;
        final TextView size;
        final TextView wh;
        final View delete;

        public VH(View itemView) {
            super(itemView);

            thumbnail = ViewFinder.byId(itemView , R.id.thumbnail);
            title = ViewFinder.byId(itemView , R.id.title);
            time = ViewFinder.byId(itemView , R.id.time);
            size = ViewFinder.byId(itemView , R.id.size);
            wh = ViewFinder.byId(itemView , R.id.wh);
            delete = ViewFinder.byId(itemView , R.id.delete);
        }
    }


    @Override
    protected VH onCreateViewHolder(LayoutInflater inflater, ViewGroup parent) {
        return new VH(inflater.inflate(R.layout.plate_video ,parent , false));
    }


    @Override
    protected void onBind(Context context, VH vh) {
        super.onBind(context, vh);
    }


}
