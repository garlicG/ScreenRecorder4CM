package com.garlicg.screenrecord4cm.data;

import android.graphics.Bitmap;
import android.util.LruCache;

public class ThumbCache {

    private static final int PERCENT = 40;

    private static ThumbCache sInstance;

    public static ThumbCache getInstance() {
        if (sInstance == null) {
            sInstance = new ThumbCache();
        }
        return sInstance;
    }

    private LruCache<Long, Bitmap> mMemCache;

    public ThumbCache() {

        // Get max available VM memory, exceeding this amount will throw an
        // OutOfMemory exception. Stored in kilobytes as LruCache takes an
        // int in its constructor.
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

        // Use percent of the available memory for this memory cache.
        final int cacheSize = maxMemory * PERCENT / 100;

        mMemCache = new LruCache<Long, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(Long key, Bitmap cache) {
                // The cache size will be measured in kilobytes rather than
                // number of items.
                return cache.getByteCount() / 1024;
            }
        };
    }

    public Bitmap get(Long key) {
        return mMemCache.get(key);
    }

    public void put(Long key, Bitmap value) {
        if(get(key) == null){
            mMemCache.put(key, value);
        }
    }

    public void clear() {
        mMemCache.evictAll();
    }

    public Bitmap remove(long key) {
        return mMemCache.remove(key);
    }

}
