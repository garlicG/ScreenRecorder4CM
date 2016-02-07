package com.garlicg.screenrecord4cm;

public class VideoPercentage {

    private final static String UNIT_TEXT = "%";
    public final int percentage;


    public VideoPercentage(int percentage) {
        this.percentage = percentage;
    }


    @Override
    public String toString() {
        return Integer.toString(percentage) + UNIT_TEXT;
    }


}
