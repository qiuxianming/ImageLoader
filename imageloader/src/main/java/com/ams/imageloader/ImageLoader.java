package com.ams.imageloader;

import android.content.Context;

import com.ams.imageloader.download.IDownloadListener;
import com.ams.imageloader.glide.GlideImageLoader;
import com.bumptech.glide.load.model.GlideUrl;

/**
 * author: Ams
 * Date: 2019/4/11
 * Description: 图片加载控制
 */
public class ImageLoader {

    private static ImageLoader instance;
    private IImageLoaderStrategy loader;

    private ImageLoader() {
    }

    public static ImageLoader getInstance() {
        if (instance == null) {
            synchronized (ImageLoader.class) {
                if (instance == null) {
                    instance = new ImageLoader();
                }
            }
        }
        return instance;
    }

    public void setImageLoaderStrategy(IImageLoaderStrategy loader) {
        this.loader = loader;
    }

    public ImageLoaderOptions load(GlideUrl glideUrl) {
        return new ImageLoaderOptions(glideUrl);
    }

    public ImageLoaderOptions load(String url) {
        return new ImageLoaderOptions(url);
    }

    public ImageLoaderOptions load(int resourceId) {
        return new ImageLoaderOptions(resourceId);
    }

    public LargeImageLoaderOptions loadLarge(String url) {
        return new LargeImageLoaderOptions(url);
    }

    public void show(ImageLoaderOptions options) {
        if (options.loader != null) {
            options.loader.loadImage(options);
        } else {
            checkNotNull();
            loader.loadImage(options);
        }
    }

    public void show(LargeImageLoaderOptions options) {
        if (options.loader != null) {
            options.loader.loadLargeImage(options);
        } else {
            checkNotNull();
            loader.loadLargeImage(options);
        }
    }

    /**
     * 清除内存缓存
     */
    public void clearMemoryCache(Context context) {
        checkNotNull();
        loader.clearMemoryCache(context);
    }

    /**
     * 清除磁盘缓存
     */
    public void clearDiskCache(Context context) {
        checkNotNull();
        loader.clearDiskCache(context);
    }

    /**
     * 清除单张图片磁盘缓存
     */
    public void clearDiskCache(Context context, String url) {
        checkNotNull();
        loader.clearDiskCache(context, url);
    }

    /**
     * 下载原图
     *
     * @param url      原图下载地址
     * @param filePath 图片存储地址
     * @param what     请求标记
     * @param listener 下载监听
     */
    public void downloadOriginal(Context context, String url, String filePath, int what, IDownloadListener listener) {
        checkNotNull();
        loader.downloadOriginal(context, url, filePath, what, listener);
    }

    /**
     * 取消单张图片下载
     */
    public void cancelDownload(int what) {
        checkNotNull();
        loader.cancelDownload(what);
    }

    /**
     * 取消所有图片下载
     */
    public void cancelAllDownload() {
        checkNotNull();
        loader.cancelAllDownload();
    }

    public void checkNotNull() {
        if (loader == null) {
            // 默认使用Glide加载，如需更换其他加载库，使用setImageLoaderStrategy
            loader = new GlideImageLoader();
        }
    }

}
