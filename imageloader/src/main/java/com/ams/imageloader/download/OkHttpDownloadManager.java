package com.ams.imageloader.download;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.SparseArray;

import com.ams.imageloader.util.MediaFileUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * author: Ams
 * Date: 2019/4/9
 * Description:
 * OkHttp下载器：支持断点续传
 */
public class OkHttpDownloadManager {

    private final OkHttpClient okHttpClient;
    private SparseArray<Call> mCallSparseArray;

    private OkHttpDownloadManager() {
        okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .build();
        mCallSparseArray = new SparseArray<>();
    }

    private static class SingletonHolder {
        private static final OkHttpDownloadManager INSTANCE = new OkHttpDownloadManager();
    }

    public static OkHttpDownloadManager getInstance() {
        return SingletonHolder.INSTANCE;
    }

    /**
     * 取消单条下载请求
     */
    public void cancelDownload(int what) {
        Call call = mCallSparseArray.get(what);
        if (call != null) {
            call.cancel();
        }
    }

    /**
     * 取消所有下载请求
     */
    public void cancelAllDownload() {
        for (int i = 0; i < mCallSparseArray.size(); i++) {
            Call call = mCallSparseArray.valueAt(i);
            call.cancel();
        }
    }

    /**
     * 添加下载任务
     *
     * @param url      请求地址
     * @param filePath 文件保存地址
     * @param what     请求码，以区分不同的请求
     * @param listener 下载监听
     */
    public void startDownload(final Context context, final String url, final String filePath, final int what, final IDownloadListener listener) {
        final Handler mainHandler = new Handler(Looper.getMainLooper());
        ThreadPoolManager.getInstance().executeTask(new Runnable() {
            @Override
            public void run() {
                InputStream is = null;
                RandomAccessFile saveFile = null;
                Call call = null;
                long downloadLength = 0; // 已经下载的长度
                final File file = new File(filePath);
                if (file.exists()) {
                    downloadLength = file.length();
                }
                try {
                    long contentLength = getContentLength(url);
                    if (downloadLength == contentLength) {
                        // 长度相等代表已下载完成
                        if (listener != null) {
                            mainHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    listener.onFinish(what, filePath);
                                }
                            });
                        }
                    } else {
                        // 支持断点续传
                        Request request = new Request.Builder().url(url).addHeader("RANGE", "bytes=" + downloadLength + "-").build();
                        call = okHttpClient.newCall(request);
                        mCallSparseArray.put(what, call);
                        Response response = call.execute();
                        if (response != null) {
                            is = response.body().byteStream();
                            saveFile = new RandomAccessFile(file, "rw");
                            saveFile.seek(downloadLength); // 跳过已下载的字节

                            byte[] b = new byte[1024];
                            int total = 0;
                            int len;
                            while ((len = is.read(b)) != -1) {
                                if (!call.isCanceled()) {
                                    total += len;
                                    saveFile.write(b, 0, len);
                                    // 计算下载进度
                                    final int progress = (int) ((total + downloadLength) * 100 / contentLength);
                                    if (listener != null) {
                                        mainHandler.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                listener.onProgress(what, progress);
                                            }
                                        });
                                    }
                                }
                            }
                            // 下载完成
                            if (listener != null) {
                                mainHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (MediaFileUtils.isImageFileType(filePath) || MediaFileUtils.isVideoFileType(filePath)) {
                                            // 图片或视频通知图库刷新
                                            // 这里用filePath去判断是因为，假如下载的是图片但是保存没有以图片的后缀名去保存，那么通知图库刷新也是无效的。
                                            context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                                                    Uri.fromFile(new File(filePath))));
                                        }
                                        listener.onFinish(what, filePath);
                                    }
                                });
                            }
                        } else {
                            // 下载失败
                            if (listener != null) {
                                mainHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        listener.onFailed(what, "http connect error");
                                    }
                                });
                            }
                        }
                    }
                } catch (final Exception e) {
                    e.printStackTrace();
                    if (call != null && call.isCanceled()) return;
                    // 下载失败
                    if (listener != null) {
                        mainHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                listener.onFailed(what, e.getMessage());
                            }
                        });
                    }
                } finally {
                    try {
                        if (is != null) is.close();
                        if (saveFile != null) saveFile.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    /**
     * 获取图片的总长度
     */
    private long getContentLength(String downloadUrl) throws IOException {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(downloadUrl)
                .build();
        Response response = client.newCall(request).execute();
        if (response != null && response.isSuccessful()) {
            long contentLength = response.body().contentLength();
            response.body().close();
            return contentLength;
        }
        return 0;
    }

}
