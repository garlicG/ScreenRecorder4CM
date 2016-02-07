package com.garlicg.screenrecord4cm.util;

import android.view.animation.AccelerateInterpolator;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;

public class Interpolators {

    public static final LinearInterpolator LINEAR = new LinearInterpolator();
    public static final AccelerateInterpolator ACCELERATE = new AccelerateInterpolator();
    public static final DecelerateInterpolator DECELERATE = new DecelerateInterpolator();
    public static final AnticipateInterpolator ANTICIPATE = new AnticipateInterpolator();
    public static final OvershootInterpolator OVER_SHOOT = new OvershootInterpolator();
}
