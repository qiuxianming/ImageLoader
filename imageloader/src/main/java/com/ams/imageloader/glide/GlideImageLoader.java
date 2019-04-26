package com.ams.imageloader.glide;

import android.content.Context;
import android.content.Intent;
import android.graphics.PointF;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.ams.imageloader.IImageLoaderStrategy;
import com.ams.imageloader.ILoaderListener;
import com.ams.imageloader.ImageLoaderOptions;
import com.ams.imageloader.LargeImageLoaderOptions;
import com.ams.imageloader.download.IDownloadListener;
import com.ams.imageloader.download.OkHttpDownloadManager;
import com.ams.imageloader.glide.transform.BlurTransform;
import com.ams.imageloader.glide.transform.CircleTransform;
import com.ams.imageloader.glide.transform.CornerTransform;
import com.ams.imageloader.progress.ProgressInterceptor;
import com.ams.imageloader.util.FileUtils;
import com.ams.imageloader.util.LargeScaleUtils;
import com.ams.imageloader.util.MediaFileUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.disklrucache.DiskLruCache;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.engine.cache.DiskCache;
import com.bumptech.glide.load.engine.cache.SafeKeyGenerator;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.bumptech.glide.signature.EmptySignature;
import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.ImageViewState;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

import java.io.File;
import java.io.IOException;

/**
 * author: Ams
 * Date: 2019/4/11
 * Description: Glide图片库加载
 */
public class GlideImageLoader implements IImageLoaderStrategy {

    @Override
    public void loadImage(final ImageLoaderOptions options) {
        RequestOptions requestOptions = new RequestOptions();
        // 磁盘缓存策略
        if (options.diskCacheStrategy != ImageLoaderOptions.DiskCacheStrategy.DEFAULT) {
            if (options.diskCacheStrategy == ImageLoaderOptions.DiskCacheStrategy.ALL) {
                requestOptions = requestOptions.diskCacheStrategy(DiskCacheStrategy.ALL);
            } else if (options.diskCacheStrategy == ImageLoaderOptions.DiskCacheStrategy.NONE) {
                requestOptions = requestOptions.diskCacheStrategy(DiskCacheStrategy.NONE);
            } else if (options.diskCacheStrategy == ImageLoaderOptions.DiskCacheStrategy.SOURCE) {
                requestOptions = requestOptions.diskCacheStrategy(DiskCacheStrategy.RESOURCE);
            } else if (options.diskCacheStrategy == ImageLoaderOptions.DiskCacheStrategy.RESULT) {
                requestOptions = requestOptions.diskCacheStrategy(DiskCacheStrategy.DATA);
            }
        }

        if (options.loadAnimId != 0) {
            // 自定义加载动画
            final AnimationDrawable animationDrawable = (AnimationDrawable) options.view.getResources().getDrawable(options.loadAnimId);
            if (animationDrawable != null) {
                animationDrawable.start();
            }
            requestOptions = requestOptions.placeholder(animationDrawable);
        } else if (options.placeholderId != 0) {
            // 占位图
            requestOptions = requestOptions.placeholder(options.resourceId);
        }
        // 异常图
        if (options.errorId != 0) {
            requestOptions = requestOptions.error(options.errorId);
        }
        // 大小设置
        if (options.overrideWidth != 0 || options.overrideHeight != 0) {
            requestOptions = requestOptions.override(options.overrideWidth, options.overrideHeight);
        }
        // 跳过内存缓存
        requestOptions = requestOptions.skipMemoryCache(options.skipMemoryCache);
        // 过度动画
        if (!options.crossFade) {
            requestOptions = requestOptions.dontAnimate();
        }
        // 图片缩放模式
        if (options.centerCrop) {
            requestOptions = requestOptions.centerCrop();
        } else if (options.centerInside) {
            requestOptions = requestOptions.centerInside();
        } else if (options.fitCenter) {
            requestOptions = requestOptions.fitCenter();
        }
        // 图片形状处理，目前有圆角和圆形实现
        requestOptions = transform(false, requestOptions, options);

        RequestBuilder<Drawable> requestBuilder = getRequestBuilder(options);

        if (options.loadAnimId != 0) {
            // 自定义加载动画，这里不要做任何操作，因为自定义加载动画以占位符的形式处理了
        } else if (options.loadAnimGifId != 0) {
            // GIF加载动画
            requestBuilder = requestBuilder.thumbnail(Glide.with(options.view).load(options.loadAnimGifId)
                    .apply(transform(true, new RequestOptions(), options)));
        } else if (!TextUtils.isEmpty(options.thumbnail)) {
            // 缩略图
            requestBuilder = requestBuilder.thumbnail(Glide.with(options.view).load(options.thumbnail)
                    .apply(transform(true, new RequestOptions(), options)));
        }

        // 加载进度监听
        ProgressInterceptor.addListener(options.url, options.progressListener);

        requestBuilder.apply(requestOptions).listener(new RequestListener<Drawable>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                ProgressInterceptor.removeListener(options.url);
                if (options.loaderListener != null) options.loaderListener.onFailed();
                return false;
            }

            @Override
            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target,
                                           DataSource dataSource, boolean isFirstResource) {
                ProgressInterceptor.removeListener(options.url);
                // 加载来源监听
                if (options.loaderListener != null) {
                    if (dataSource == DataSource.LOCAL) {
                        options.loaderListener.onSuccess(ILoaderListener.DataSource.LOCAL);
                    } else if (dataSource == DataSource.REMOTE) {
                        options.loaderListener.onSuccess(ILoaderListener.DataSource.REMOTE);
                    } else if (dataSource == DataSource.DATA_DISK_CACHE) {
                        options.loaderListener.onSuccess(ILoaderListener.DataSource.DATA_DISK_CACHE);
                    } else if (dataSource == DataSource.RESOURCE_DISK_CACHE) {
                        options.loaderListener.onSuccess(ILoaderListener.DataSource.RESOURCE_DISK_CACHE);
                    } else {
                        options.loaderListener.onSuccess(ILoaderListener.DataSource.MEMORY_CACHE);
                    }
                }
                return false;
            }
        }).into(options.view);
    }

    /**
     * 图片形状变换
     */
    private RequestOptions transform(boolean isThumbnail, RequestOptions requestOptions, ImageLoaderOptions options) {
        BlurTransform blurTransform = new BlurTransform(options.blurRadius);
        if (options.transformations != null) {
            // 多个transform叠加
            requestOptions = requestOptions.transforms(options.transformations);
        } else if (options.transformation != null) {
            // 自定义transform
            if (options.blurRadius != 0) {
                requestOptions = requestOptions.transforms(blurTransform, options.transformation);
            } else {
                requestOptions = requestOptions.transform(options.transformation);
            }
        } else if (options.isCircle) {
            // 圆形图片
            CircleTransform circleTransform = new CircleTransform(isThumbnail, options.borderWidth, options.borderColor);
            if (options.blurRadius != 0) {
                requestOptions = requestOptions.transforms(blurTransform, circleTransform);
            } else {
                requestOptions = requestOptions.transform(circleTransform);
            }
        } else if (options.roundedCorners != null) {
            // 圆角图片
            CornerTransform roundTransform = new CornerTransform(isThumbnail, options.roundedCorners.leftTopRadius,
                    options.roundedCorners.leftBottomRadius, options.roundedCorners.rightTopRadius,
                    options.roundedCorners.rightBottomRadius, options.borderWidth, options.borderColor);
            if (options.blurRadius != 0) {
                requestOptions = requestOptions.transforms(blurTransform, roundTransform);
            } else {
                requestOptions = requestOptions.transform(roundTransform);
            }
        } else if (options.blurRadius != 0) {
            // 高斯模糊
            requestOptions = requestOptions.transform(blurTransform);
        }
        return requestOptions;
    }

    @Override
    public void loadLargeImage(final LargeImageLoaderOptions options) {
        Glide.with(options.view).load(options.url).downloadOnly(new SimpleTarget<File>() {
            @Override
            public void onResourceReady(File resource, Transition<? super File> transition) {
                float scale = LargeScaleUtils.getImageScale(options.view, resource.getAbsolutePath());
                // 设置最小缩放率
                options.view.setMinimumScaleType(SubsamplingScaleImageView.SCALE_TYPE_CUSTOM);
                options.view.setMinScale(scale);
                options.view.setImage(ImageSource.uri(resource.getAbsolutePath()),
                        new ImageViewState(scale, new PointF(0, 0), SubsamplingScaleImageView.ORIENTATION_0));
                options.view.setDoubleTapZoomScale(SubsamplingScaleImageView.ZOOM_FOCUS_CENTER_IMMEDIATE);
            }
        });
    }

    public RequestBuilder<Drawable> getRequestBuilder(ImageLoaderOptions options) {
        RequestBuilder<Drawable> requestBuilder;

        if (!TextUtils.isEmpty(options.url)) {
            requestBuilder = Glide.with(options.view.getContext()).load(options.url);
        } else {
            requestBuilder = Glide.with(options.view.getContext()).load(options.resourceId);
        }

        return requestBuilder;
    }

    @Override
    public void clearMemoryCache(final Context context) {
        // 清除内存缓存只能在主线程执行
        if (Looper.myLooper() == Looper.getMainLooper()) {
            Glide.get(context).clearMemory();
        } else {
            Handler mainHandler = new Handler(Looper.getMainLooper());
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    Glide.get(context).clearMemory();
                }
            });
        }
    }

    @Override
    public void clearDiskCache(final Context context) {
        File file = Glide.getPhotoCacheDir(context);
        if (file != null) {
            FileUtils.deleteFilesInDir(file);
        }
        // 清除磁盘缓存需要在子线程执行
        if (Looper.myLooper() == Looper.getMainLooper()) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Glide.get(context).clearDiskCache();
                }
            }).start();
        } else {
            Glide.get(context).clearDiskCache();
        }
    }

    @Override
    public void clearDiskCache(Context context, String url) {
        File cacheFile = getCacheFile(context, url);
        if (cacheFile != null && cacheFile.exists()) {
            cacheFile.delete();
        }
    }

    // 获取Glide本地缓存路径
    private File getCacheFile(Context context, String url) {
        DataCacheKey dataCacheKey = new DataCacheKey(new GlideUrl(url), EmptySignature.obtain());
        SafeKeyGenerator safeKeyGenerator = new SafeKeyGenerator();
        String safeKey = safeKeyGenerator.getSafeKey(dataCacheKey);
        try {
            int cacheSize = 100 * 1000 * 1000;
            DiskLruCache diskLruCache = DiskLruCache.open(new File(context.getCacheDir(),
                    DiskCache.Factory.DEFAULT_DISK_CACHE_DIR), 1, 1, cacheSize);
            DiskLruCache.Value value = diskLruCache.get(safeKey);
            if (value != null) {
                return value.getFile(0);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }


    @Override
    public void downloadOriginal(Context context, String url, String filePath, int what, IDownloadListener listener) {
        // 1、查找缓存是否有此路径缓存
        File cacheFile = getCacheFile(context, url);
        if (cacheFile != null) {
            // 2、如果有缓存，将缓存文件复制到目标位置
            boolean copyResult = FileUtils.copyFile(cacheFile.getAbsolutePath(), filePath);
            if (copyResult) {
                // 3、通知图库刷新
                if (MediaFileUtils.isImageFileType(filePath)) {
                    context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                            Uri.fromFile(new File(filePath))));
                }
                if (listener != null) listener.onFinish(what, filePath);
            } else {
                // 4、拷贝失败，重新下载
                OkHttpDownloadManager.getInstance().startDownload(context, url, filePath, what, listener);
            }
        } else {
            // 5、没有缓存，需要从服务器下载
            OkHttpDownloadManager.getInstance().startDownload(context, url, filePath, what, listener);
        }
    }

    @Override
    public void cancelDownload(int what) {
        OkHttpDownloadManager.getInstance().cancelDownload(what);
    }

    @Override
    public void cancelAllDownload() {
        OkHttpDownloadManager.getInstance().cancelAllDownload();
    }

}
