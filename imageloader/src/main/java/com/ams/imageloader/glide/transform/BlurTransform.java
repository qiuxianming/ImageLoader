package com.ams.imageloader.glide.transform;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;

import com.ams.imageloader.util.FastBlurUtils;
import com.bumptech.glide.load.Key;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;

import java.security.MessageDigest;

/**
 * author: Ams
 * Date: 2019/4/25
 * Description: 高斯模糊处理
 */
public class BlurTransform extends BitmapTransformation {

    private static final String ID = BlurTransform.class.getName();
    private static final byte[] ID_BYTES = ID.getBytes(Key.CHARSET);

    private int mRadius;    // 模糊半径

    public BlurTransform(int radius) {
        mRadius = radius;
    }

    @Override
    protected Bitmap transform(@NonNull BitmapPool pool, @NonNull Bitmap toTransform, int outWidth, int outHeight) {
        return FastBlurUtils.doBlur(toTransform, mRadius, true);
    }

    @Override
    public void updateDiskCacheKey(MessageDigest messageDigest) {
        messageDigest.update(ID_BYTES);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof BlurTransform;
    }

    @Override
    public int hashCode() {
        return ID.hashCode();
    }
}
