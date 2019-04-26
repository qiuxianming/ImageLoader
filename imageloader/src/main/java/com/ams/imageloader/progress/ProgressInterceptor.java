package com.ams.imageloader.progress;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * author: Ams
 * Date: 2019/4/19
 * Description: 拦截器监听加载进度
 */
public class ProgressInterceptor implements Interceptor {

    static final Map<String, IProgressListener> LISTENER_MAP = new HashMap<>();

    public static void addListener(String url, IProgressListener listener) {
        LISTENER_MAP.put(url, listener);
    }

    public static void removeListener(String url) {
        LISTENER_MAP.remove(url);
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        Response response = chain.proceed(request);
        String url = request.url().toString();
        ResponseBody body = response.body();
        Response newResponse = response.newBuilder().body(new ProgressResponseBody(url, body)).build();
        return newResponse;
    }

}
