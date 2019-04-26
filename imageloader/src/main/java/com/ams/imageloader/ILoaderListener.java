package com.ams.imageloader;

/**
 * author: Ams
 * Date: 2019/4/11
 * Description: 图片加载回调
 */
public interface ILoaderListener {

    void onSuccess(DataSource dataSource);

    void onFailed();

    enum DataSource {
        LOCAL, REMOTE, DATA_DISK_CACHE, RESOURCE_DISK_CACHE, MEMORY_CACHE,
    }

}
