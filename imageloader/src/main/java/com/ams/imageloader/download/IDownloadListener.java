package com.ams.imageloader.download;

/**
 * author: Ams
 * Date: 2019/4/18
 * Description: 下载监听
 */
public interface IDownloadListener {

    void onProgress(int what, int progress);

    void onFinish(int what, String filePath);

    void onFailed(int what, String msg);

}
