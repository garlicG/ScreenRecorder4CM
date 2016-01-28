package com.garlicg.screenrecordct.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.view.WindowManager;

public class DisplayUtils {
	
	public static Point getDisplaySize(Context context){
		WindowManager manager = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
		Point outSize = new Point();
		manager.getDefaultDisplay().getSize(outSize);
		return outSize;
	}

    public static int dpToPx(Resources res, int dp) {
        return (int) (res.getDisplayMetrics().density * dp + 0.5f);
    }
}
