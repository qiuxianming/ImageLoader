package com.ams.imageloader;

import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

/**
 * author: Ams
 * Date: 2019/4/12
 * Description:
 */
public class LargeImageLoaderOptions {

    public SubsamplingScaleImageView view;
    public String url; // 图片地址
    public IImageLoaderStrategy loader; // 图片加载库

    public LargeImageLoaderOptions(String url) {
        this.url = url;
    }

    public LargeImageLoaderOptions loader(IImageLoaderStrategy loader) {
        this.loader = loader;
        return this;
    }

    public void into(SubsamplingScaleImageView view){
        this.view = view;
        ImageLoader.getInstance().show(this);
    }
}
