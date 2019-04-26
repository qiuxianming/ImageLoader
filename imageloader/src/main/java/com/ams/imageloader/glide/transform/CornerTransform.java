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
 * Date: 2019/4/11
 * Description: Glide自定义圆角
 * 1、支持一个圆角、两个圆角、三个圆角、四个圆角
 * 2、支持为每个圆角设置不同的圆角半径
 * 3、圆角图片支持添加边框
 */
public class CornerTransform extends BitmapTransformation {

    private static final String ID = CornerTransform.class.getName();
    private static final byte[] ID_BYTES = ID.getBytes(CHARSET);

    // 各圆角半径
    private int mLeftTopRadius;
    private int mLeftBottomRadius;
    private int mRightTopRadius;
    private int mRightBottomRadius;

    // 边框
    private int mBorderWidth;
    private int mBorderColor;
    private int mHalfBorderWidth;

    // 画笔
    private Paint mPaint;
    private Paint mStrokePaint;

    // 是否是缩略图
    private boolean mIsThumbnail;

    public CornerTransform(boolean isThumbnail, int radius) {
        this.mLeftTopRadius = radius;
        this.mLeftBottomRadius = radius;
        this.mRightTopRadius = radius;
        this.mRightBottomRadius = radius;
        this.mIsThumbnail = isThumbnail;
    }

    public CornerTransform(boolean isThumbnail, int leftTopRadius, int leftBottomRadius, int rightTopRadius, int rightBottomRadius) {
        this.mLeftTopRadius = leftTopRadius;
        this.mLeftBottomRadius = leftBottomRadius;
        this.mRightTopRadius = rightTopRadius;
        this.mRightBottomRadius = rightBottomRadius;
        this.mIsThumbnail = isThumbnail;
    }

    public CornerTransform(boolean isThumbnail, int radius, int borderWidth, int borderColor) {
        this.mLeftTopRadius = radius;
        this.mLeftBottomRadius = radius;
        this.mRightTopRadius = radius;
        this.mRightBottomRadius = radius;
        this.mBorderWidth = borderWidth;
        this.mHalfBorderWidth = borderWidth / 2;
        this.mBorderColor = borderColor;
        this.mIsThumbnail = isThumbnail;
    }

    public CornerTransform(boolean isThumbnail, int leftTopRadius, int leftBottomRadius, int rightTopRadius,
                           int rightBottomRadius, int borderWidth, int borderColor) {
        this.mLeftTopRadius = leftTopRadius;
        this.mLeftBottomRadius = leftBottomRadius;
        this.mRightTopRadius = rightTopRadius;
        this.mRightBottomRadius = rightBottomRadius;
        this.mBorderWidth = borderWidth;
        this.mHalfBorderWidth = borderWidth / 2;
        this.mBorderColor = borderColor;
        this.mIsThumbnail = isThumbnail;
    }

    @Override
    protected Bitmap transform(@NonNull BitmapPool pool, @NonNull Bitmap toTransform, int outWidth, int outHeight) {
        return roundCrop(pool, toTransform, outWidth, outHeight);
    }

    private Bitmap roundCrop(BitmapPool pool, Bitmap source, int outWidth, int outHeight) {
        if (source == null) return null;

        int srcWidth = source.getWidth();
        int srcHeight = source.getHeight();

        int minSrc = Math.min(srcWidth, srcHeight);
        int minDest = Math.min(outWidth, outHeight);

        // 获取与控件圆角缩放比例
        if (mIsThumbnail && minSrc < minDest) {
            double scale = Math.max(srcWidth * 1.0 / outWidth, srcHeight * 1.0 / outHeight);
            mLeftTopRadius = (int) (mLeftTopRadius * scale);
            mLeftBottomRadius = (int) (mLeftBottomRadius * scale);
            mRightTopRadius = (int) (mRightTopRadius * scale);
            mRightBottomRadius = (int) (mRightBottomRadius * scale);
            mBorderWidth = (int) (mBorderWidth * (minSrc * 1.0 / minDest));
            mHalfBorderWidth = mBorderWidth / 2;

            // 按照控件宽高比，以目标宽度为准，计算缩放高度
            outHeight = (int) (srcWidth * (outHeight * 1.0 / outWidth));
            outWidth = srcWidth;

        }

        // 修正圆角半径
        mLeftTopRadius = fixRadius(mLeftTopRadius, outWidth, outHeight);
        mLeftBottomRadius = fixRadius(mLeftBottomRadius, outWidth, outHeight);
        mRightTopRadius = fixRadius(mRightTopRadius, outWidth, outHeight);
        mRightBottomRadius = fixRadius(mRightBottomRadius, outWidth, outHeight);

        if (mBorderWidth >= Math.min(outWidth, outHeight) / 2 || mBorderWidth < 0) mBorderWidth = 1;

        // 从BitmapPool获取给定配置只包含透明像素的Bitmap，结果返回可能为Null
        Bitmap result = pool.get(outWidth, outHeight, Bitmap.Config.ARGB_8888);
        if (result == null) {
            result = Bitmap.createBitmap(outWidth, outHeight, Bitmap.Config.ARGB_8888);
        }

        // 通过Shader画显示区域
        Canvas canvas = new Canvas(result);
        mPaint = new Paint(Paint.DITHER_FLAG | Paint.FILTER_BITMAP_FLAG | Paint.ANTI_ALIAS_FLAG);
        mPaint.setShader(new BitmapShader(source, BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP));

        // 边框画笔
        mStrokePaint = new Paint(Paint.DITHER_FLAG | Paint.FILTER_BITMAP_FLAG | Paint.ANTI_ALIAS_FLAG);
        mStrokePaint.setColor(mBorderColor);
        mStrokePaint.setStyle(Paint.Style.STROKE);
        mStrokePaint.setStrokeWidth(mBorderWidth);

        drawRoundCorner(canvas, mPaint, mStrokePaint, outWidth, outHeight);

        return result;
    }

    /**
     * 修正圆角半径
     */
    private int fixRadius(int radius, int width, int height) {
        int maxRadius = Math.min(width, height) / 2;
        return radius > maxRadius ? maxRadius : radius;
    }

    private void drawRoundCorner(Canvas canvas, Paint paint, Paint strokePaint, int width, int height) {
        if (mLeftTopRadius == mLeftBottomRadius && mLeftTopRadius == mRightTopRadius && mLeftTopRadius == mRightBottomRadius) {
            // 四角相等，画圆角矩形
            RectF rectF = new RectF(mBorderWidth / 2, mBorderWidth / 2, width - mBorderWidth / 2, height - mBorderWidth / 2);
            canvas.drawRoundRect(rectF, mLeftTopRadius, mLeftTopRadius, paint);
            canvas.drawRoundRect(rectF, mLeftTopRadius, mLeftTopRadius, strokePaint);
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
        canvas.drawRect(new RectF(mLeftTopRadius, mBorderWidth, width - mBorderWidth, height - mBorderWidth), paint);
        canvas.drawRect(new RectF(mBorderWidth, mLeftTopRadius, mLeftTopRadius, height - mBorderWidth), paint);

        canvas.drawLine(mHalfBorderWidth, mLeftTopRadius, mHalfBorderWidth, height, mStrokePaint); // 左边框线
        canvas.drawLine(mLeftTopRadius, mHalfBorderWidth, width, mHalfBorderWidth, mStrokePaint); // 上边框线
        canvas.drawLine(0, height - mHalfBorderWidth, width, height - mHalfBorderWidth, mStrokePaint); // 下边框线
        canvas.drawLine(width - mHalfBorderWidth, 0, width - mHalfBorderWidth, height, mStrokePaint); // 右边框线

        drawLeftTopArc(canvas, paint, width, height);
    }

    /**
     * 单圆角：右上
     */
    private void drawRightTopCorner(Canvas canvas, Paint paint, int width, int height) {
        canvas.drawRect(new RectF(mBorderWidth, mBorderWidth, width - mRightTopRadius, height - mBorderWidth), paint);
        canvas.drawRect(new RectF(width - mRightTopRadius, mRightTopRadius, width - mBorderWidth, height - mBorderWidth), paint);

        canvas.drawLine(mHalfBorderWidth, 0, mHalfBorderWidth, height, mStrokePaint); // 左边框线
        canvas.drawLine(0, mHalfBorderWidth, width - mRightTopRadius, mHalfBorderWidth, mStrokePaint); // 上边框线
        canvas.drawLine(0, height - mHalfBorderWidth, width, height - mHalfBorderWidth, mStrokePaint); // 下边框线
        canvas.drawLine(width - mHalfBorderWidth, mRightTopRadius, width - mHalfBorderWidth, height, mStrokePaint); // 右边框线

        drawRightTopArc(canvas, paint, width, height);
    }

    /**
     * 单圆角：左下
     */
    private void drawLeftBottomCorner(Canvas canvas, Paint paint, int width, int height) {
        canvas.drawRect(new RectF(mBorderWidth, mBorderWidth, width - mBorderWidth, height - mLeftBottomRadius), paint);
        canvas.drawRect(new RectF(mLeftBottomRadius, mBorderWidth, width - mBorderWidth, height - mBorderWidth), paint);

        canvas.drawLine(mHalfBorderWidth, 0, mHalfBorderWidth, height - mLeftBottomRadius, mStrokePaint); // 左边框线
        canvas.drawLine(0, mHalfBorderWidth, width, mHalfBorderWidth, mStrokePaint); // 上边框线
        canvas.drawLine(mLeftBottomRadius, height - mHalfBorderWidth, width, height - mHalfBorderWidth, mStrokePaint); // 下边框线
        canvas.drawLine(width - mHalfBorderWidth, 0, width - mHalfBorderWidth, height, mStrokePaint); // 右边框线

        drawLeftBottomArc(canvas, paint, width, height);
    }

    /**
     * 单圆角：右下
     */
    private void drawRightBottomCorner(Canvas canvas, Paint paint, int width, int height) {
        canvas.drawRect(new RectF(mBorderWidth, mBorderWidth, width - mRightBottomRadius, height - mBorderWidth), paint);
        canvas.drawRect(new RectF(width - mRightBottomRadius, mBorderWidth, width - mBorderWidth, height - mRightBottomRadius), paint);

        canvas.drawLine(mHalfBorderWidth, 0, mHalfBorderWidth, height, mStrokePaint); // 左边框线
        canvas.drawLine(0, mHalfBorderWidth, width, mHalfBorderWidth, mStrokePaint); // 上边框线
        canvas.drawLine(0, height - mHalfBorderWidth, width - mRightBottomRadius, height - mHalfBorderWidth, mStrokePaint); // 下边框线
        canvas.drawLine(width - mHalfBorderWidth, 0, width - mHalfBorderWidth, height - mRightBottomRadius, mStrokePaint); // 右边框线

        drawRightBottomArc(canvas, paint, width, height);
    }

    /**
     * 两圆角：左边
     */
    private void drawLeftCorner(Canvas canvas, Paint paint, int width, int height) {
        canvas.drawRect(new RectF(mLeftTopRadius, mBorderWidth, width - mBorderWidth, mLeftTopRadius), paint);
        canvas.drawRect(new RectF(mBorderWidth, mLeftTopRadius, width - mBorderWidth, height - mLeftBottomRadius), paint);
        canvas.drawRect(new RectF(mLeftBottomRadius, height - mLeftBottomRadius, width - mBorderWidth, height - mBorderWidth), paint);

        canvas.drawLine(mHalfBorderWidth, mLeftTopRadius, mHalfBorderWidth, height - mLeftBottomRadius, mStrokePaint); // 左边框线
        canvas.drawLine(mLeftTopRadius, mHalfBorderWidth, width, mHalfBorderWidth, mStrokePaint); // 上边框线
        canvas.drawLine(mLeftBottomRadius, height - mHalfBorderWidth, width, height - mHalfBorderWidth, mStrokePaint); // 下边框线
        canvas.drawLine(width - mHalfBorderWidth, 0, width - mHalfBorderWidth, height, mStrokePaint); // 右边框线

        drawLeftTopArc(canvas, paint, width, height);
        drawLeftBottomArc(canvas, paint, width, height);
    }

    /**
     * 两圆角：右边
     */
    private void drawRightCorner(Canvas canvas, Paint paint, int width, int height) {
        canvas.drawRect(new RectF(mBorderWidth, mBorderWidth, width - mRightTopRadius, mRightTopRadius), paint);
        canvas.drawRect(new RectF(mBorderWidth, mRightTopRadius, width - mBorderWidth, height - mRightBottomRadius), paint);
        canvas.drawRect(new RectF(mBorderWidth, mRightBottomRadius, width - mRightTopRadius, height - mBorderWidth), paint);

        canvas.drawLine(mHalfBorderWidth, 0, mHalfBorderWidth, height, mStrokePaint); // 左边框线
        canvas.drawLine(0, mHalfBorderWidth, width - mRightTopRadius, mHalfBorderWidth, mStrokePaint); // 上边框线
        canvas.drawLine(0, height - mHalfBorderWidth, width - mRightBottomRadius, height - mHalfBorderWidth, mStrokePaint); // 下边框线
        canvas.drawLine(width - mHalfBorderWidth, mRightTopRadius, width - mHalfBorderWidth, height - mRightBottomRadius, mStrokePaint); // 右边框线

        drawRightTopArc(canvas, paint, width, height);
        drawRightBottomArc(canvas, paint, width, height);
    }

    /**
     * 两圆角：上边
     */
    private void drawTopCorner(Canvas canvas, Paint paint, int width, int height) {
        canvas.drawRect(new RectF(mBorderWidth, mLeftTopRadius, mLeftTopRadius, height - mBorderWidth), paint);
        canvas.drawRect(new RectF(mLeftTopRadius, mBorderWidth, width - mRightTopRadius, height - mBorderWidth), paint);
        canvas.drawRect(new RectF(width - mRightTopRadius, mRightTopRadius, width - mBorderWidth, height - mBorderWidth), paint);

        canvas.drawLine(mHalfBorderWidth, mLeftTopRadius, mHalfBorderWidth, height, mStrokePaint); // 左边框线
        canvas.drawLine(mLeftTopRadius, mHalfBorderWidth, width - mRightTopRadius, mHalfBorderWidth, mStrokePaint); // 上边框线
        canvas.drawLine(0, height - mHalfBorderWidth, width, height - mHalfBorderWidth, mStrokePaint); // 下边框线
        canvas.drawLine(width - mHalfBorderWidth, mRightTopRadius, width - mHalfBorderWidth, height, mStrokePaint); // 右边框线

        drawLeftTopArc(canvas, paint, width, height);
        drawRightTopArc(canvas, paint, width, height);
    }

    /**
     * 两圆角：下边
     */
    private void drawBottomCorner(Canvas canvas, Paint paint, int width, int height) {
        canvas.drawRect(new RectF(mBorderWidth, mBorderWidth, mLeftBottomRadius, height - mLeftBottomRadius), paint);
        canvas.drawRect(new RectF(mLeftBottomRadius, mBorderWidth, width - mRightBottomRadius, height - mBorderWidth), paint);
        canvas.drawRect(new RectF(width - mRightBottomRadius, mBorderWidth, width - mBorderWidth, height - mRightBottomRadius), paint);

        canvas.drawLine(mHalfBorderWidth, 0, mHalfBorderWidth, height - mLeftBottomRadius, mStrokePaint); // 左边框线
        canvas.drawLine(0, mHalfBorderWidth, width, mHalfBorderWidth, mStrokePaint); // 上边框线
        canvas.drawLine(mLeftBottomRadius, height - mHalfBorderWidth, width - mRightBottomRadius, height - mHalfBorderWidth, mStrokePaint); // 下边框线
        canvas.drawLine(width - mHalfBorderWidth, 0, width - mHalfBorderWidth, height - mRightBottomRadius, mStrokePaint); // 右边框线

        drawLeftBottomArc(canvas, paint, width, height);
        drawRightBottomArc(canvas, paint, width, height);
    }

    /**
     * 两圆角：左上和右下
     */
    private void drawLeftTopAndRightBottomCorner(Canvas canvas, Paint paint, int width, int height) {
        canvas.drawRect(new RectF(mBorderWidth, mLeftTopRadius, mLeftTopRadius, height - mBorderWidth), paint);
        canvas.drawRect(new RectF(mLeftTopRadius, mBorderWidth, width - mRightBottomRadius, height - mBorderWidth), paint);
        canvas.drawRect(new RectF(width - mRightBottomRadius, mBorderWidth, width - mBorderWidth, height - mRightBottomRadius), paint);

        canvas.drawLine(mHalfBorderWidth, mLeftTopRadius, mHalfBorderWidth, height, mStrokePaint); // 左边框线
        canvas.drawLine(mLeftTopRadius, mHalfBorderWidth, width, mHalfBorderWidth, mStrokePaint); // 上边框线
        canvas.drawLine(0, height - mHalfBorderWidth, width - mRightBottomRadius, height - mHalfBorderWidth, mStrokePaint); // 下边框线
        canvas.drawLine(width - mHalfBorderWidth, 0, width - mHalfBorderWidth, height - mRightBottomRadius, mStrokePaint); // 右边框线

        drawLeftTopArc(canvas, paint, width, height);
        drawRightBottomArc(canvas, paint, width, height);
    }

    /**
     * 两圆角：左下和右上
     */
    private void drawLeftBottomAndRightTopCorner(Canvas canvas, Paint paint, int width, int height) {
        canvas.drawRect(new RectF(mBorderWidth, mBorderWidth, mLeftBottomRadius, height - mLeftBottomRadius), paint);
        canvas.drawRect(new RectF(mLeftBottomRadius, mBorderWidth, width - mRightTopRadius, height - mBorderWidth), paint);
        canvas.drawRect(new RectF(width - mRightTopRadius, mRightTopRadius, width - mBorderWidth, height - mBorderWidth), paint);

        canvas.drawLine(mHalfBorderWidth, 0, mHalfBorderWidth, height - mLeftBottomRadius, mStrokePaint); // 左边框线
        canvas.drawLine(0, mHalfBorderWidth, width - mRightTopRadius, mHalfBorderWidth, mStrokePaint); // 上边框线
        canvas.drawLine(mLeftBottomRadius, height - mHalfBorderWidth, width, height - mHalfBorderWidth, mStrokePaint); // 下边框线
        canvas.drawLine(width - mHalfBorderWidth, mRightTopRadius, width - mHalfBorderWidth, height, mStrokePaint); // 右边框线

        drawLeftBottomArc(canvas, paint, width, height);
        drawRightTopArc(canvas, paint, width, height);
    }

    /**
     * 三圆角：左上不设
     */
    private void drawWithoutLeftTopCorner(Canvas canvas, Paint paint, int width, int height) {
        float rightMin = Math.min(mRightTopRadius, mRightBottomRadius);
        float rightMax = Math.max(mRightTopRadius, mRightBottomRadius);
        canvas.drawRect(new RectF(width - rightMin, mRightTopRadius, width - mBorderWidth, height - mRightBottomRadius), paint);
        if (mRightTopRadius > mRightBottomRadius) {
            canvas.drawRect(new RectF(width - mRightTopRadius, mRightTopRadius, width - mRightBottomRadius, height - mBorderWidth), paint);
        } else {
            canvas.drawRect(new RectF(width - mRightBottomRadius, mBorderWidth, width - mRightTopRadius, height - mRightBottomRadius), paint);
        }
        canvas.drawRect(new RectF(mLeftBottomRadius, mBorderWidth, width - rightMax, height - mBorderWidth), paint);
        canvas.drawRect(new RectF(mBorderWidth, mBorderWidth, mLeftBottomRadius, height - mLeftBottomRadius), paint);

        canvas.drawLine(mHalfBorderWidth, 0, mHalfBorderWidth, height - mLeftBottomRadius, mStrokePaint); // 左边框线
        canvas.drawLine(0, mHalfBorderWidth, width - mRightTopRadius, mHalfBorderWidth, mStrokePaint); // 上边框线
        canvas.drawLine(mLeftBottomRadius, height - mHalfBorderWidth, width - mRightBottomRadius, height - mHalfBorderWidth, mStrokePaint); // 下边框线
        canvas.drawLine(width - mHalfBorderWidth, mRightTopRadius, width - mHalfBorderWidth, height - mRightBottomRadius, mStrokePaint); // 右边框线

        drawLeftBottomArc(canvas, paint, width, height);
        drawRightTopArc(canvas, paint, width, height);
        drawRightBottomArc(canvas, paint, width, height);
    }

    /**
     * 三圆角：左下不设
     */
    private void drawWithoutLeftBottomCorner(Canvas canvas, Paint paint, int width, int height) {
        float rightMin = Math.min(mRightTopRadius, mRightBottomRadius);
        float rightMax = Math.max(mRightTopRadius, mRightBottomRadius);
        canvas.drawRect(new RectF(width - rightMin, mRightTopRadius, width - mBorderWidth, height - mRightBottomRadius), paint);
        if (mRightTopRadius > mRightBottomRadius) {
            canvas.drawRect(new RectF(width - mRightTopRadius, mRightTopRadius, width - mRightBottomRadius, height - mBorderWidth), paint);
        } else {
            canvas.drawRect(new RectF(width - mRightBottomRadius, mBorderWidth, width - mRightTopRadius, height - mRightBottomRadius), paint);
        }
        canvas.drawRect(new RectF(mLeftTopRadius, mBorderWidth, width - rightMax, height - mBorderWidth), paint);
        canvas.drawRect(new RectF(mBorderWidth, mLeftTopRadius, mLeftTopRadius, height - mBorderWidth), paint);

        canvas.drawLine(mHalfBorderWidth, mLeftTopRadius, mHalfBorderWidth, height, mStrokePaint); // 左边框线
        canvas.drawLine(mLeftTopRadius, mHalfBorderWidth, width - mRightTopRadius, mHalfBorderWidth, mStrokePaint); // 上边框线
        canvas.drawLine(0, height - mHalfBorderWidth, width - rightMax, height - mHalfBorderWidth, mStrokePaint); // 下边框线
        canvas.drawLine(width - mHalfBorderWidth, mRightTopRadius, width - mHalfBorderWidth, height - mRightBottomRadius, mStrokePaint); // 右边框线

        drawLeftTopArc(canvas, paint, width, height);
        drawRightTopArc(canvas, paint, width, height);
        drawRightBottomArc(canvas, paint, width, height);
    }

    /**
     * 三圆角：右上不设
     */
    private void drawWithoutRightTopCorner(Canvas canvas, Paint paint, int width, int height) {
        float leftMin = Math.min(mLeftTopRadius, mLeftBottomRadius);
        float leftMax = Math.max(mLeftTopRadius, mLeftBottomRadius);
        canvas.drawRect(new RectF(mBorderWidth, mLeftTopRadius, leftMin, height - mLeftBottomRadius), paint);
        if (mLeftTopRadius > mLeftBottomRadius) {
            canvas.drawRect(new RectF(mLeftBottomRadius, mLeftTopRadius, mLeftTopRadius, height - mBorderWidth), paint);
        } else {
            canvas.drawRect(new RectF(mLeftTopRadius, mBorderWidth, mLeftBottomRadius, height - mLeftBottomRadius), paint);
        }
        canvas.drawRect(new RectF(leftMax, mBorderWidth, width - mRightBottomRadius, height - mBorderWidth), paint);
        canvas.drawRect(new RectF(width - mRightBottomRadius, mBorderWidth, width - mBorderWidth, height - mRightBottomRadius), paint);

        canvas.drawLine(mHalfBorderWidth, mLeftTopRadius, mHalfBorderWidth, height - mLeftBottomRadius, mStrokePaint); // 左边框线
        canvas.drawLine(mLeftTopRadius, mHalfBorderWidth, width, mHalfBorderWidth, mStrokePaint); // 上边框线
        canvas.drawLine(mLeftBottomRadius, height - mHalfBorderWidth, width - mRightBottomRadius, height - mHalfBorderWidth, mStrokePaint); // 下边框线
        canvas.drawLine(width - mHalfBorderWidth, 0, width - mHalfBorderWidth, height - mRightBottomRadius, mStrokePaint); // 右边框线

        drawLeftTopArc(canvas, paint, width, height);
        drawLeftBottomArc(canvas, paint, width, height);
        drawRightBottomArc(canvas, paint, width, height);
    }

    /**
     * 三圆角：右下不设
     */
    private void drawWithoutRightBottomCorner(Canvas canvas, Paint paint, int width, int height) {
        float leftMin = Math.min(mLeftTopRadius, mLeftBottomRadius);
        float leftMax = Math.max(mLeftTopRadius, mLeftBottomRadius);
        canvas.drawRect(new RectF(mBorderWidth, mLeftTopRadius, leftMin, height - mLeftBottomRadius), paint);
        if (mLeftTopRadius > mLeftBottomRadius) {
            canvas.drawRect(new RectF(mLeftBottomRadius, mLeftTopRadius, mLeftTopRadius, height - mBorderWidth), paint);
        } else {
            canvas.drawRect(new RectF(mLeftTopRadius, mBorderWidth, mLeftBottomRadius, height - mLeftBottomRadius), paint);
        }
        canvas.drawRect(new RectF(leftMax, mBorderWidth, width - mRightTopRadius, height - mBorderWidth), paint);
        canvas.drawRect(new RectF(width - mRightTopRadius, mRightTopRadius, width - mBorderWidth, height - mBorderWidth), paint);

        canvas.drawLine(mHalfBorderWidth, mLeftTopRadius, mHalfBorderWidth, height - mLeftBottomRadius, mStrokePaint); // 左边框线
        canvas.drawLine(mLeftTopRadius, mHalfBorderWidth, width - mRightTopRadius, mHalfBorderWidth, mStrokePaint); // 上边框线
        canvas.drawLine(mLeftBottomRadius, height - mHalfBorderWidth, width, height - mHalfBorderWidth, mStrokePaint); // 下边框线
        canvas.drawLine(width - mHalfBorderWidth, mRightTopRadius, width - mHalfBorderWidth, height, mStrokePaint); // 右边框线

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

        canvas.drawRect(new RectF(mBorderWidth, mLeftTopRadius, leftMin, height - mLeftBottomRadius), paint);
        if (mLeftTopRadius > mLeftBottomRadius) {
            canvas.drawRect(new RectF(mLeftBottomRadius, mLeftTopRadius, mLeftTopRadius, height - mBorderWidth), paint);
        } else {
            canvas.drawRect(new RectF(mLeftTopRadius, mBorderWidth, mLeftBottomRadius, height - mLeftBottomRadius), paint);
        }
        if (mRightTopRadius > mRightBottomRadius) {
            canvas.drawRect(new RectF(width - mRightTopRadius, mRightTopRadius, width - mRightBottomRadius, height - mBorderWidth), paint);
        } else {
            canvas.drawRect(new RectF(width - mRightBottomRadius, mBorderWidth, width - mRightTopRadius, height - mRightBottomRadius), paint);
        }
        canvas.drawRect(new RectF(leftMax, mBorderWidth, width - rightMax, height - mBorderWidth), paint);
        canvas.drawRect(new RectF(width - rightMin, mRightTopRadius, width - mBorderWidth, height - mRightBottomRadius), paint);

        canvas.drawLine(mHalfBorderWidth, mLeftTopRadius, mHalfBorderWidth, height - mLeftBottomRadius, mStrokePaint); // 左边框线
        canvas.drawLine(mLeftTopRadius, mHalfBorderWidth, width - mRightTopRadius, mHalfBorderWidth, mStrokePaint); // 上边框线
        canvas.drawLine(mLeftBottomRadius, height - mHalfBorderWidth, width - mRightBottomRadius,
                height - mHalfBorderWidth, mStrokePaint); // 下边框线
        canvas.drawLine(width - mHalfBorderWidth, mRightTopRadius, width - mHalfBorderWidth,
                height - mRightBottomRadius, mStrokePaint); // 右边框线

        drawLeftTopArc(canvas, paint, width, height);
        drawLeftBottomArc(canvas, paint, width, height);
        drawRightTopArc(canvas, paint, width, height);
        drawRightBottomArc(canvas, paint, width, height);
    }

    /**
     * 画左上角扇形区域
     */
    private void drawLeftTopArc(Canvas canvas, Paint paint, int width, int height) {
        RectF bitmapRectF = new RectF(mBorderWidth, mBorderWidth,
                2 * mLeftTopRadius - mBorderWidth, 2 * mLeftTopRadius - mBorderWidth);
        RectF strokeRectF = new RectF(mHalfBorderWidth, mHalfBorderWidth,
                2 * mLeftTopRadius - mHalfBorderWidth, 2 * mLeftTopRadius - mHalfBorderWidth);
        canvas.drawArc(bitmapRectF, 180, 90, true, paint);
        canvas.drawArc(strokeRectF, 180, 90, false, mStrokePaint);
    }

    /**
     * 画右上角扇形区域
     */
    private void drawRightTopArc(Canvas canvas, Paint paint, int width, int height) {
        RectF bitmapRectF = new RectF(width - 2 * mRightTopRadius + mBorderWidth, mBorderWidth,
                width - mBorderWidth, 2 * mLeftTopRadius - mBorderWidth);
        RectF strokeRectF = new RectF(width - 2 * mRightTopRadius + mHalfBorderWidth, mHalfBorderWidth,
                width - mHalfBorderWidth, 2 * mLeftTopRadius - mHalfBorderWidth);
        canvas.drawArc(bitmapRectF, 270, 90, true, paint);
        canvas.drawArc(strokeRectF, 270, 90, false, mStrokePaint);
    }

    /**
     * 画左下角扇形区域
     */
    private void drawLeftBottomArc(Canvas canvas, Paint paint, int width, int height) {
        RectF bitmapRectF = new RectF(mBorderWidth, height - 2 * mLeftBottomRadius + mBorderWidth,
                2 * mLeftBottomRadius - mBorderWidth, height - mBorderWidth);
        RectF strokeRectF = new RectF(mHalfBorderWidth, height - 2 * mLeftBottomRadius + mHalfBorderWidth,
                2 * mLeftBottomRadius - mHalfBorderWidth, height - mHalfBorderWidth);
        canvas.drawArc(bitmapRectF, 90, 90, true, paint);
        canvas.drawArc(strokeRectF, 90, 90, false, mStrokePaint);
    }

    /**
     * 画右下角扇形区域
     */
    private void drawRightBottomArc(Canvas canvas, Paint paint, int width, int height) {
        RectF bitmapRectF = new RectF(width - 2 * mRightBottomRadius + mBorderWidth, height - 2 * mRightBottomRadius + mBorderWidth,
                width - mBorderWidth, height - mBorderWidth);
        RectF strokeRectF = new RectF(width - 2 * mRightBottomRadius + mHalfBorderWidth, height - 2 * mRightBottomRadius + mHalfBorderWidth,
                width - mHalfBorderWidth, height - mHalfBorderWidth);
        canvas.drawArc(bitmapRectF, 0, 90, true, paint);
        canvas.drawArc(strokeRectF, 0, 90, false, mStrokePaint);
    }

    @Override
    public void updateDiskCacheKey(MessageDigest messageDigest) {
        messageDigest.update(ID_BYTES);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof CornerTransform;
    }

    @Override
    public int hashCode() {
        return ID.hashCode();
    }
}
