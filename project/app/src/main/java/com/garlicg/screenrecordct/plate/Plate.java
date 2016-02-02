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
import android.view.LayoutInflater;
import android.view.ViewGroup;

public class Plate <VH extends RecyclerView.ViewHolder>{

    protected Plate(){}

    protected VH onCreateViewHolder(LayoutInflater inflater, ViewGroup parent) {
        return null;
    }

    protected void onBind(Context context, VH vh) {
    }

}