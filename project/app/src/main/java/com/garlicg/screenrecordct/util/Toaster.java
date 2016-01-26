package com.garlicg.screenrecordct.util;

import android.content.Context;
import android.widget.Toast;

public class Toaster {

    public static void show(Context context , String message){
        Toast.makeText(context , message , Toast.LENGTH_SHORT).show();
    }

    public static void showLong(Context context ,String message){
        Toast.makeText(context , message , Toast.LENGTH_LONG).show();
    }
}
