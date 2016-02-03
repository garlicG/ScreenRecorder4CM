/*
 * Plate
 * Plate realize multi model a adapter works on RecyclerView
 *
 * Copyright (C) 2015 Takahiro GOTO
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.garlicg.screenrecordct.plate;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

public class PlateAdapter extends RecyclerView.Adapter{

    private Context mContext;
    private LayoutInflater mInflater;
    private SparseArray<Class<? extends Plate>> mTypes;
    private List<Plate> mItems;

    public PlateAdapter(Context context) {
        mContext = context;
        mInflater = LayoutInflater.from(context);
        mTypes = new SparseArray<>();
    }

    // items
    public void setItems(List<Plate> items){
        mItems = items;
    }

    @Override
    public int getItemViewType(int position) {
        Plate item = mItems.get(position);
        int index =  mTypes.indexOfValue(item.getClass());
        if(index == -1){
            index = mTypes.size();
            mTypes.put(index , item.getClass());
        }
        return index;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int itemViewType) {
        Class<? extends Plate> clazz = mTypes.get(itemViewType);

        //noinspection TryWithIdenticalCatches
        try {
            Plate layout = clazz.newInstance();
            RecyclerView.ViewHolder vh = layout.onCreateViewHolder(mInflater, viewGroup);
            bindOnItemClickListener(vh);
            return vh;
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        throw new IllegalStateException("Illegal view holder class:" + clazz);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        //noinspection unchecked
        mItems.get(position).onBind(mContext , viewHolder);
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public interface OnItemClickListener{
        void onItemClick(View v, Plate panel);
    }

    private OnItemClickListener mItemClickListener;

    public void setOnItemClickListener(OnItemClickListener listener){
        mItemClickListener = listener;
    }

    private void bindOnItemClickListener(final RecyclerView.ViewHolder vh){
        if(mItemClickListener == null)return;
        vh.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Plate panel = mItems.get(vh.getAdapterPosition());
                mItemClickListener.onItemClick(v , panel);
            }
        });
    }
}