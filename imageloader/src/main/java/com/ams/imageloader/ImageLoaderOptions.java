package com.ams.imageloader;

import android.graphics.Bitmap;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.widget.ImageView;

import com.ams.imageloader.progress.IProgressListener;
import com.bumptech.glide.load.Transformation;
import com.bumptech.glide.load.model.GlideUrl;

import java.util.Arrays;
import java.util.Collection;

/**
 * author: Ams
 * Date: 2019/4/11
 * Description: 图片加载参数设置
 */
public class ImageLoaderOptions {

    public ImageView view;
    public String url;                 // 图片地址
    public GlideUrl glideUrl;          // 图片地址
    public int resourceId;             // 图片资源ID
    public int overrideWidth;          // 图片显示宽度
    public int overrideHeight;         // 图片显示高度
    public int placeholderId;          // 占位图
    public int errorId;                // 异常图
    public String thumbnail;           // 缩略图
    public int loadAnimId;             // 自定义加载动画
    public int loadAnimGifId;          // Gif加载动画
    public boolean crossFade = true;   // 渐变平滑显示图片
    public boolean skipMemoryCache;    // 跳过内存缓存
    public boolean centerCrop;         // centerCrop显示
    public boolean centerInside;       // centerCrop显示
    public boolean fitCenter;          // fixCenter显示
    public boolean isCircle;           // 圆形
    public RoundedCorners roundedCorners;// 圆角
    public Transformation<Bitmap> transformation;// 自定义形状
    public Transformation<Bitmap>[] transformations;// 自定义形状
    public int blurRadius;             // 高斯模糊程度 1-25
    public int borderColor;            // 边框颜色
    public int borderWidth;            // 边框粗细

    public IImageLoaderStrategy loader; // 图片加载库
    public DiskCacheStrategy diskCacheStrategy = DiskCacheStrategy.DEFAULT;
    public ILoaderListener loaderListener; // 加载结果监听器
    public IProgressListener progressListener; // 加载进度监听器

    public ImageLoaderOptions(GlideUrl glideUrl) {
        this.glideUrl = glideUrl;
    }

    public ImageLoaderOptions(String url) {
        this.url = url;
    }

    public ImageLoaderOptions(int resourceId) {
        this.resourceId = resourceId;
    }

    public void into(@NonNull ImageView view) {
        this.view = view;
        ImageLoader.getInstance().show(this);
    }

    public ImageLoaderOptions loader(IImageLoaderStrategy loader) {
        this.loader = loader;
        return this;
    }

    public ImageLoaderOptions placeHolder(@DrawableRes int placeholderId) {
        this.placeholderId = placeholderId;
        return this;
    }

    public ImageLoaderOptions thumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
        return this;
    }

    public ImageLoaderOptions diskCacheStrategy(DiskCacheStrategy diskCacheStrategy) {
        this.diskCacheStrategy = diskCacheStrategy;
        return this;
    }

    public ImageLoaderOptions loadAnim(@DrawableRes int loadAnimId) {
        this.loadAnimId = loadAnimId;
        return this;
    }

    public ImageLoaderOptions loadAnimGif(@DrawableRes int loadAnimGifId) {
        this.loadAnimGifId = loadAnimGifId;
        return this;
    }

    public ImageLoaderOptions error(@DrawableRes int errorId) {
        this.errorId = errorId;
        return this;
    }

    public ImageLoaderOptions listener(ILoaderListener loaderListener) {
        this.loaderListener = loaderListener;
        return this;
    }

    public ImageLoaderOptions progress(IProgressListener progressListener) {
        this.progressListener = progressListener;
        return this;
    }

    public ImageLoaderOptions crossFade() {
        this.crossFade = true;
        return this;
    }

    public ImageLoaderOptions centerCrop() {
        this.centerCrop = true;
        return this;
    }

    public ImageLoaderOptions centerInside() {
        this.centerInside = true;
        return this;
    }

    public ImageLoaderOptions fitCenter() {
        this.fitCenter = true;
        return this;
    }

    public ImageLoaderOptions skipMemoryCache() {
        this.skipMemoryCache = true;
        return this;
    }

    public ImageLoaderOptions override(int width, int height) {
        this.overrideWidth = width;
        this.overrideHeight = height;
        return this;
    }

    public ImageLoaderOptions isCircle() {
        this.isCircle = true;
        return this;
    }

    public ImageLoaderOptions fastBlur(int radius) {
        this.blurRadius = radius;
        return this;
    }

    public ImageLoaderOptions borderWidth(int borderWidth) {
        this.borderWidth = borderWidth;
        return this;
    }

    public ImageLoaderOptions borderColor(int borderColor) {
        this.borderColor = borderColor;
        return this;
    }

    public ImageLoaderOptions corner(RoundedCorners roundedCorners) {
        this.roundedCorners = roundedCorners;
        return this;
    }

    public ImageLoaderOptions transform(Transformation<Bitmap> transformation) {
        this.transformation = transformation;
        return this;
    }

    public ImageLoaderOptions transforms(Transformation<Bitmap>... transformations) {
        this.transformations = transformations;
        return this;
    }

    // 圆角图片
    public static class RoundedCorners {
        public int leftTopRadius;
        public int leftBottomRadius;
        public int rightTopRadius;
        public int rightBottomRadius;

        public RoundedCorners(int radius) {
            this.leftTopRadius = radius;
            this.leftBottomRadius = radius;
            this.rightTopRadius = radius;
            this.rightBottomRadius = radius;
        }

        public RoundedCorners(int leftTopRadius, int leftBottomRadius, int rightTopRadius, int rightBottomRadius) {
            this.leftTopRadius = leftTopRadius;
            this.leftBottomRadius = leftBottomRadius;
            this.rightTopRadius = rightTopRadius;
            this.rightBottomRadius = rightBottomRadius;
        }
    }

    // 磁盘缓存策略
    public enum DiskCacheStrategy {
        ALL, NONE, SOURCE, RESULT, DEFAULT
    }

}
