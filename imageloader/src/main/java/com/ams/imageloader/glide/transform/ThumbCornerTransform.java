package com.ams.imageloader.glide.transform;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.NonNull;

import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;

import java.security.MessageDigest;

/**
 * author: Ams
 * Date: 2019/4/18
 * Description: 缩略图圆角处理
 */
public class ThumbCornerTransform extends BitmapTransformation {

    private static final String ID = ThumbCornerTransform.class.getName();
    private static final byte[] ID_BYTES = ID.getBytes(CHARSET);

    private int mLeftTopRadius;
    private int mLeftBottomRadius;
    private int mRightTopRadius;
    private int mRightBottomRadius;

    public ThumbCornerTransform(int leftTopRadius, int leftBottomRadius, int rightTopRadius, int rightBottomRadius) {
        this.mLeftTopRadius = leftTopRadius;
        this.mLeftBottomRadius = leftBottomRadius;
        this.mRightTopRadius = rightTopRadius;
        this.mRightBottomRadius = rightBottomRadius;
    }

    @Override
    protected Bitmap transform(@NonNull BitmapPool pool, @NonNull Bitmap toTransform, int outWidth, int outHeight) {
        return roundCrop(pool, toTransform, outWidth, outHeight);
    }

    private Bitmap roundCrop(BitmapPool pool, Bitmap source, int outWidth, int outHeight) {
        if (source == null) return null;

        int width = source.getWidth();
        int height = source.getHeight();

        // 获取与控件圆角缩放比例
        double scale = Math.max(width * 1.0 / outWidth, height * 1.0 / outHeight);
        mLeftTopRadius = (int) (mLeftTopRadius * scale);
        mLeftBottomRadius = (int) (mLeftBottomRadius * scale);
        mRightTopRadius = (int) (mRightTopRadius * scale);
        mRightBottomRadius = (int) (mRightBottomRadius * scale);

        // 修正圆角半径
        mLeftTopRadius = fixRadius(mLeftTopRadius, width, height);
        mLeftBottomRadius = fixRadius(mLeftBottomRadius, width, height);
        mRightTopRadius = fixRadius(mRightTopRadius, width, height);
        mRightBottomRadius = fixRadius(mRightBottomRadius, width, height);

        // 从BitmapPool获取给定配置只包含透明像素的Bitmap，结果返回可能为Null
        Bitmap result = pool.get(width, height, Bitmap.Config.ARGB_8888);
        if (result == null) {
            result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        }

        // 通过Shader画显示区域
        Canvas canvas = new Canvas(result);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setShader(new BitmapShader(source, BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP));
        drawRoundCorner(canvas, paint, width, height);

        return result;
    }

    /**
     * 修正圆角半径
     */
    private int fixRadius(int radius, int width, int height) {
        int maxRadius = Math.min(width, height) / 2;
        return radius > maxRadius ? maxRadius : radius;
    }

    private void drawRoundCorner(Canvas canvas, Paint paint, int width, int height) {
        if (mLeftTopRadius == mLeftBottomRadius && mLeftTopRadius == mRightTopRadius && mLeftTopRadius == mRightBottomRadius) {
            // 四角相等，画圆角矩形
            canvas.drawRoundRect(new RectF(0, 0, width, height), mLeftTopRadius, mLeftTopRadius, paint);
        } else if (mLeftTopRadius != 0 && mLeftBottomRadius != 0 && mRightTopRadius != 0 && mRightBottomRadius != 0) {
            // 四角
            drawAllCorner(canvas, paint, width, height);
        } else if (mLeftBottomRadius != 0 && mRightTopRadius != 0 && mRightBottomRadius != 0) {
            // 三角：左上不设
            drawWithoutLeftTopCorner(canvas, paint, width, height);
        } else if (mLeftTopRadius != 0 && mRightTopRadius != 0 && mRightBottomRadius != 0) {
            // 三角：左下不设
            drawWithoutLeftBottomCorner(canvas, paint, width, height);
        } else if (mLeftTopRadius != 0 && mLeftBottomRadius != 0 && mRightBottomRadius != 0) {
            // 三角：右上不设
            drawWithoutRightTopCorner(canvas, paint, width, height);
        } else if (mLeftTopRadius != 0 && mLeftBottomRadius != 0 && mRightTopRadius != 0) {
            // 三角：右下不设
            drawWithoutRightBottomCorner(canvas, paint, width, height);
        } else if (mLeftTopRadius != 0 && mLeftBottomRadius != 0) {
            // 两角：左边
            drawLeftCorner(canvas, paint, width, height);
        } else if (mRightTopRadius != 0 && mRightBottomRadius != 0) {
            // 两角：右边
            drawRightCorner(canvas, paint, width, height);
        } else if (mLeftTopRadius != 0 && mRightTopRadius != 0) {
            // 两角：上边
            drawTopCorner(canvas, paint, width, height);
        } else if (mLeftBottomRadius != 0 && mRightBottomRadius != 0) {
            // 两角：下边
            drawBottomCorner(canvas, paint, width, height);
        } else if (mLeftTopRadius != 0 && mRightBottomRadius != 0) {
            // 两角：左上和右下
            drawLeftTopAndRightBottomCorner(canvas, paint, width, height);
        } else if (mLeftBottomRadius != 0 && mRightTopRadius != 0) {
            // 两角：左下和右上
            drawLeftBottomAndRightTopCorner(canvas, paint, width, height);
        } else if (mLeftTopRadius != 0) {
            // 单角：左上
            drawLeftTopCorner(canvas, paint, width, height);
        } else if (mLeftBottomRadius != 0) {
            // 单角：左下
            drawLeftBottomCorner(canvas, paint, width, height);
        } else if (mRightTopRadius != 0) {
            // 单角：右上
            drawRightTopCorner(canvas, paint, width, height);
        } else {
            // 单角：右下
            drawRightBottomCorner(canvas, paint, width, height);
        }
    }

    /**
     * 单圆角：左上
     */
    private void drawLeftTopCorner(Canvas canvas, Paint paint, int width, int height) {
        canvas.drawRect(new RectF(mLeftTopRadius, 0, width, height), paint);
        canvas.drawRect(new RectF(0, mLeftTopRadius, width, height), paint);
        drawLeftTopArc(canvas, paint, width, height);
    }

    /**
     * 单圆角：右上
     */
    private void drawRightTopCorner(Canvas canvas, Paint paint, int width, int height) {
        canvas.drawRect(new RectF(0, 0, width - mRightTopRadius, height), paint);
        canvas.drawRect(new RectF(width - mRightTopRadius, mRightTopRadius, width, height), paint);
        drawRightTopArc(canvas, paint, width, height);
    }

    /**
     * 单圆角：左下
     */
    private void drawLeftBottomCorner(Canvas canvas, Paint paint, int width, int height) {
        canvas.drawRect(new RectF(0, 0, width, height - mLeftBottomRadius), paint);
        canvas.drawRect(new RectF(mLeftBottomRadius, height - mLeftBottomRadius, width, height), paint);
        drawLeftBottomArc(canvas, paint, width, height);
    }

    /**
     * 单圆角：右下
     */
    private void drawRightBottomCorner(Canvas canvas, Paint paint, int width, int height) {
        canvas.drawRect(new RectF(0, 0, width - mRightBottomRadius, height), paint);
        canvas.drawRect(new RectF(width - mRightBottomRadius, 0, width, height - mRightBottomRadius), paint);
        drawRightBottomArc(canvas, paint, width, height);
    }

    /**
     * 两圆角：左边
     */
    private void drawLeftCorner(Canvas canvas, Paint paint, int width, int height) {
        canvas.drawRect(new RectF(mLeftTopRadius, 0, width, mLeftTopRadius), paint);
        canvas.drawRect(new RectF(0, mLeftTopRadius, width, height - mLeftBottomRadius), paint);
        canvas.drawRect(new RectF(mLeftBottomRadius, height - mLeftBottomRadius, width, height), paint);
        drawLeftTopArc(canvas, paint, width, height);
        drawLeftBottomArc(canvas, paint, width, height);
    }

    /**
     * 两圆角：右边
     */
    private void drawRightCorner(Canvas canvas, Paint paint, int width, int height) {
        canvas.drawRect(new RectF(0, 0, width - mRightTopRadius, mRightTopRadius), paint);
        canvas.drawRect(new RectF(0, mRightTopRadius, width, height - mRightBottomRadius), paint);
        canvas.drawRect(new RectF(0, mRightBottomRadius, width - mRightTopRadius, height), paint);
        drawRightTopArc(canvas, paint, width, height);
        drawRightBottomArc(canvas, paint, width, height);
    }

    /**
     * 两圆角：上边
     */
    private void drawTopCorner(Canvas canvas, Paint paint, int width, int height) {
        canvas.drawRect(new RectF(0, mLeftTopRadius, mLeftTopRadius, height), paint);
        canvas.drawRect(new RectF(mLeftTopRadius, 0, width - mRightTopRadius, height), paint);
        canvas.drawRect(new RectF(width - mRightTopRadius, mRightTopRadius, width, height), paint);
        drawLeftTopArc(canvas, paint, width, height);
        drawRightTopArc(canvas, paint, width, height);
    }

    /**
     * 两圆角：下边
     */
    private void drawBottomCorner(Canvas canvas, Paint paint, int width, int height) {
        canvas.drawRect(new RectF(0, 0, mLeftBottomRadius, height - mLeftBottomRadius), paint);
        canvas.drawRect(new RectF(mLeftBottomRadius, 0, width - mRightBottomRadius, height), paint);
        canvas.drawRect(new RectF(width - mRightBottomRadius, 0, width, height - mRightBottomRadius), paint);
        drawLeftBottomArc(canvas, paint, width, height);
        drawRightBottomArc(canvas, paint, width, height);
    }

    /**
     * 两圆角：左上和右下
     */
    private void drawLeftTopAndRightBottomCorner(Canvas canvas, Paint paint, int width, int height) {
        canvas.drawRect(new RectF(0, mLeftTopRadius, mLeftTopRadius, height), paint);
        canvas.drawRect(new RectF(mLeftTopRadius, 0, width - mRightBottomRadius, height), paint);
        canvas.drawRect(new RectF(width - mRightBottomRadius, 0, width, height - mRightBottomRadius), paint);
        drawLeftTopArc(canvas, paint, width, height);
        drawRightBottomArc(canvas, paint, width, height);
    }

    /**
     * 两圆角：左下和右上
     */
    private void drawLeftBottomAndRightTopCorner(Canvas canvas, Paint paint, int width, int height) {
        canvas.drawRect(new RectF(0, 0, mLeftBottomRadius, height - mLeftBottomRadius), paint);
        canvas.drawRect(new RectF(mLeftBottomRadius, 0, width - mRightTopRadius, height), paint);
        canvas.drawRect(new RectF(width - mRightTopRadius, mRightTopRadius, width, height), paint);
        drawLeftBottomArc(canvas, paint, width, height);
        drawRightTopArc(canvas, paint, width, height);
    }

    /**
     * 三圆角：左上不设
     */
    private void drawWithoutLeftTopCorner(Canvas canvas, Paint paint, int width, int height) {
        float rightMin = Math.min(mRightTopRadius, mRightBottomRadius);
        float rightMax = Math.max(mRightTopRadius, mRightBottomRadius);
        canvas.drawRect(new RectF(width - rightMin, mRightTopRadius, width, height - mRightBottomRadius), paint);
        if (mRightTopRadius > mRightBottomRadius) {
            canvas.drawRect(new RectF(width - mRightTopRadius, mRightTopRadius, width - mRightBottomRadius, height), paint);
        } else {
            canvas.drawRect(new RectF(width - mRightBottomRadius, 0, width - mRightTopRadius, height - mRightBottomRadius), paint);
        }
        canvas.drawRect(new RectF(mLeftBottomRadius, 0, width - rightMax, height), paint);
        canvas.drawRect(new RectF(0, 0, mLeftBottomRadius, height - mLeftBottomRadius), paint);

        drawLeftBottomArc(canvas, paint, width, height);
        drawRightTopArc(canvas, paint, width, height);
        drawRightBottomArc(canvas, paint, width, height);
    }

    /**
     * 三圆角：右上不设
     */
    private void drawWithoutRightTopCorner(Canvas canvas, Paint paint, int width, int height) {
        float leftMin = Math.min(mLeftTopRadius, mLeftBottomRadius);
        float leftMax = Math.max(mLeftTopRadius, mLeftBottomRadius);
        canvas.drawRect(new RectF(0, mLeftTopRadius, leftMin, height - mLeftBottomRadius), paint);
        if (mLeftTopRadius > mLeftBottomRadius) {
            canvas.drawRect(new RectF(mLeftBottomRadius, mLeftTopRadius, mLeftTopRadius, height), paint);
        } else {
            canvas.drawRect(new RectF(mLeftTopRadius, 0, mLeftBottomRadius, height - mLeftBottomRadius), paint);
        }
        canvas.drawRect(new RectF(leftMax, 0, width - mRightBottomRadius, height), paint);
        canvas.drawRect(new RectF(width - mRightBottomRadius, 0, width, height - mRightBottomRadius), paint);

        drawLeftTopArc(canvas, paint, width, height);
        drawLeftBottomArc(canvas, paint, width, height);
        drawRightBottomArc(canvas, paint, width, height);
    }

    /**
     * 三圆角：左下不设
     */
    private void drawWithoutLeftBottomCorner(Canvas canvas, Paint paint, int width, int height) {
        float rightMin = Math.min(mRightTopRadius, mRightBottomRadius);
        float rightMax = Math.max(mRightTopRadius, mRightBottomRadius);
        canvas.drawRect(new RectF(width - rightMin, mRightTopRadius, width, height - mRightBottomRadius), paint);
        if (mRightTopRadius > mRightBottomRadius) {
            canvas.drawRect(new RectF(width - mRightTopRadius, mRightTopRadius, width - mRightBottomRadius, height), paint);
        } else {
            canvas.drawRect(new RectF(width - mRightBottomRadius, 0, width - mRightTopRadius, height - mRightBottomRadius), paint);
        }
        canvas.drawRect(new RectF(mLeftTopRadius, 0, width - rightMax, height), paint);
        canvas.drawRect(new RectF(0, mLeftTopRadius, mLeftTopRadius, height), paint);

        drawLeftTopArc(canvas, paint, width, height);
        drawRightTopArc(canvas, paint, width, height);
        drawRightBottomArc(canvas, paint, width, height);
    }

    /**
     * 三圆角：右下不设
     */
    private void drawWithoutRightBottomCorner(Canvas canvas, Paint paint, int width, int height) {
        float leftMin = Math.min(mLeftTopRadius, mLeftBottomRadius);
        float leftMax = Math.max(mLeftTopRadius, mLeftBottomRadius);
        canvas.drawRect(new RectF(0, mLeftTopRadius, leftMin, height - mLeftBottomRadius), paint);
        if (mLeftTopRadius > mLeftBottomRadius) {
            canvas.drawRect(new RectF(mLeftBottomRadius, mLeftTopRadius, mLeftTopRadius, height), paint);
        } else {
            canvas.drawRect(new RectF(mLeftTopRadius, 0, mLeftBottomRadius, height - mLeftBottomRadius), paint);
        }
        canvas.drawRect(new RectF(leftMax, 0, width - mRightTopRadius, height), paint);
        canvas.drawRect(new RectF(width - mRightTopRadius, mRightTopRadius, width, height), paint);

        drawLeftTopArc(canvas, paint, width, height);
        drawLeftBottomArc(canvas, paint, width, height);
        drawRightTopArc(canvas, paint, width, height);
    }

    /**
     * 四圆角
     */
    private void drawAllCorner(Canvas canvas, Paint paint, int width, int height) {
        float leftMin = Math.min(mLeftTopRadius, mLeftBottomRadius);
        float leftMax = Math.max(mLeftTopRadius, mLeftBottomRadius);
        float rightMin = Math.min(mRightTopRadius, mRightBottomRadius);
        float rightMax = Math.max(mRightTopRadius, mRightBottomRadius);

        canvas.drawRect(new RectF(0, mLeftTopRadius, leftMin, height - mLeftBottomRadius), paint);
        if (mLeftTopRadius > mLeftBottomRadius) {
            canvas.drawRect(new RectF(mLeftBottomRadius, mLeftTopRadius, mLeftTopRadius, height), paint);
        } else {
            canvas.drawRect(new RectF(mLeftTopRadius, 0, mLeftBottomRadius, height - mLeftBottomRadius), paint);
        }
        if (mRightTopRadius > mRightBottomRadius) {
            canvas.drawRect(new RectF(width - mRightTopRadius, mRightTopRadius, width - mRightBottomRadius, height), paint);
        } else {
            canvas.drawRect(new RectF(width - mRightBottomRadius, 0, width - mRightTopRadius, height - mRightBottomRadius), paint);
        }
        canvas.drawRect(new RectF(leftMax, 0, width - rightMax, height), paint);
        canvas.drawRect(new RectF(width - rightMin, mRightTopRadius, width, height - mRightBottomRadius), paint);

        drawLeftTopArc(canvas, paint, width, height);
        drawLeftBottomArc(canvas, paint, width, height);
        drawRightTopArc(canvas, paint, width, height);
        drawRightBottomArc(canvas, paint, width, height);
    }

    /**
     * 画左上角扇形区域
     */
    private void drawLeftTopArc(Canvas canvas, Paint paint, int width, int height) {
        RectF leftTopRectF = new RectF(0, 0, mLeftTopRadius * 2, mLeftTopRadius * 2);
        canvas.drawArc(leftTopRectF, 180, 90, true, paint);
    }

    /**
     * 画右上角扇形区域
     */
    private void drawRightTopArc(Canvas canvas, Paint paint, int width, int height) {
        RectF rightTopRectF = new RectF(width - 2 * mRightTopRadius, 0, width, mRightTopRadius * 2);
        canvas.drawArc(rightTopRectF, 270, 90, true, paint);
    }

    /**
     * 画左下角扇形区域
     */
    private void drawLeftBottomArc(Canvas canvas, Paint paint, int width, int height) {
        RectF leftBottomRectF = new RectF(0, height - 2 * mLeftBottomRadius, 2 * mLeftBottomRadius, height);
        canvas.drawArc(leftBottomRectF, 90, 90, true, paint);
    }

    /**
     * 画右下角扇形区域
     */
    private void drawRightBottomArc(Canvas canvas, Paint paint, int width, int height) {
        RectF rightBottomRectF = new RectF(width - 2 * mRightBottomRadius, height - 2 * mRightBottomRadius, width, height);
        canvas.drawArc(rightBottomRectF, 0, 90, true, paint);
    }

    @Override
    public void updateDiskCacheKey(MessageDigest messageDigest) {
        messageDigest.update(ID_BYTES);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof ThumbCornerTransform;
    }

    @Override
    public int hashCode() {
        return ID.hashCode();
    }
}
