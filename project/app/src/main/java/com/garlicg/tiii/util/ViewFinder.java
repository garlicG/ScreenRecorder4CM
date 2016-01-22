package com.garlicg.tiii.util;

import android.app.Activity;
import android.view.View;

public class ViewFinder {

    @SuppressWarnings("unchecked")
    public static <T extends View> T byId(View view , int id){
        return (T)view.findViewById(id);
    }

    @SuppressWarnings("unchecked")
    public static <T extends View> T byId(Activity activity, int id){
        return (T)activity.findViewById(id);
    }

}
