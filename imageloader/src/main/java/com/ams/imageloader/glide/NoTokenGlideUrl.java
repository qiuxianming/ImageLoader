package com.ams.imageloader.glide;

import com.bumptech.glide.load.model.GlideUrl;

/**
 * author: Ams
 * Date: 2019/4/19
 * Description: 解决因图片路径带token，缓存失效问题
 * sample：ImageLoader.getInstance().load(new NoTokenGlideUrl(url)).into(imageView);
 */
public class NoTokenGlideUrl extends GlideUrl {

    private String mUrl;

    public NoTokenGlideUrl(String url) {
        super(url);
        mUrl = url;
    }

    @Override
    public String getCacheKey() {
        return getNoTokenUrl();
    }

    private String getNoTokenUrl() {
        if (mUrl == null) return "";
        int tokenIndex = mUrl.contains("?token=") ? mUrl.indexOf("?token=") : mUrl.indexOf("&token=");
        if (tokenIndex >= 0) {
            return mUrl.substring(tokenIndex);
        }
        return mUrl;
    }

}
