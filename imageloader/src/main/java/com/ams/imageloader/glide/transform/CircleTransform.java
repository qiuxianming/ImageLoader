package com.ams.imageloader.glide.transform;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.NonNull;

import com.bumptech.glide.load.Key;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;

import java.security.MessageDigest;

/**
 * author: Ams
 * Date: 2019/4/23
 * Description: 圆形图片支持设置边框半径，边框颜色
 */
public class CircleTransform extends BitmapTransformation {

    private static final String ID = CircleTransform.class.getName();
    private static final byte[] ID_BYTES = ID.getBytes(Key.CHARSET);

    // 边框
    private int borderWidth;
    private int borderColor;

    // 是否是缩略图
    private boolean mIsThumbnail;

    // 无边框
    public CircleTransform(boolean isThumbnail) {
        this.mIsThumbnail = isThumbnail;
    }

    // 有边框
    public CircleTransform(boolean isThumbnail, int borderWidth, int borderColor) {
        this.borderWidth = borderWidth;
        this.borderColor = borderColor;
        this.mIsThumbnail = isThumbnail;
    }

    @Override
    protected Bitmap transform(@NonNull BitmapPool pool, @NonNull Bitmap toTransform, int outWidth, int outHeight) {
        return circleTransform(pool, toTransform, outWidth, outHeight);
    }

    private Bitmap circleTransform(BitmapPool pool, Bitmap source, int outWidth, int outHeight) {
        if (source == null) return null;

        int srcWidth = source.getWidth();
        int srcHeight = source.getHeight();

        int minSrc = Math.min(srcWidth, srcHeight);
        int minDest = Math.min(outWidth, outHeight);

        int destMinEdge = minDest;

        if (minSrc < minDest) {
            destMinEdge = minSrc;
            borderWidth = (int) (borderWidth * (minSrc * 1.0 / minDest));
        }

        int radius = destMinEdge / 2;
        if (borderWidth >= radius || borderWidth < 0) borderWidth = 1;

        // 从BitmapPool获取给定配置只包含透明像素的Bitmap，结果返回可能为Null
        Bitmap result = pool.get(destMinEdge, destMinEdge, Bitmap.Config.ARGB_8888);
        if (result == null) {
            result = Bitmap.createBitmap(destMinEdge, destMinEdge, Bitmap.Config.ARGB_8888);
        }

        // 通过Shader画显示区域
        Canvas canvas = new Canvas(result);
        Paint paint = new Paint(Paint.DITHER_FLAG | Paint.FILTER_BITMAP_FLAG | Paint.ANTI_ALIAS_FLAG);
        paint.setShader(new BitmapShader(source, BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP));
        canvas.drawCircle(radius, radius, radius - borderWidth, paint);

        // 画边框
        if (borderWidth > 0) {
            Paint strokePaint = new Paint(Paint.DITHER_FLAG | Paint.FILTER_BITMAP_FLAG | Paint.ANTI_ALIAS_FLAG);
            strokePaint.setStyle(Paint.Style.STROKE);
            strokePaint.setStrokeWidth(borderWidth);
            strokePaint.setColor(borderColor);
            // 这里的半径减去边框的一半是因为，stroke样式画笔有一半在外面有一半在里面
            canvas.drawCircle(radius, radius, radius - borderWidth, strokePaint);
        }

        return result;
    }

    @Override
    public void updateDiskCacheKey(MessageDigest messageDigest) {
        messageDigest.update(ID_BYTES);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof CircleTransform;
    }

    @Override
    public int hashCode() {
        return ID.hashCode();
    }
}
