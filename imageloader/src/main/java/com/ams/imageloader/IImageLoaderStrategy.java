package com.ams.imageloader;

import android.content.Context;

import com.ams.imageloader.download.IDownloadListener;

/**
 * author: Ams
 * Date: 2019/4/11
 * Description: 图片加载库接口
 */
public interface IImageLoaderStrategy {

    /**
     * 加载图片
     */
    void loadImage(ImageLoaderOptions options);

    /**
     * 加载大图
     */
    void loadLargeImage(LargeImageLoaderOptions options);

    /**
     * 清理内存缓存
     */
    void clearMemoryCache(Context context);

    /**
     * 清理磁盘缓存
     */
    void clearDiskCache(Context context);

    /**
     * 单独删除某个缓存
     */
    void clearDiskCache(Context context, String url);

    /**
     * 下载原图
     */
    void downloadOriginal(Context context, String url, String filePath, int what, IDownloadListener listener);

    /**
     * 取消下载
     */
    void cancelDownload(int what);

    /**
     * 取消所有下载
     */
    void cancelAllDownload();

}
