package com.ams.imageloader.util;

import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;

/**
 * author: Ams
 * Date: 2019/4/12
 * Description:
 */
public class LargeScaleUtils {

    public static float getImageScale(View view, String imagePath) {
        if (TextUtils.isEmpty(imagePath)) {
            return 1.0f;
        }

        BitmapFactory.Options options = new BitmapFactory.Options();
        // 仅读取图片宽高
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imagePath, options);

        // 拿到图片宽高
        int dw = options.outWidth;
        int dh = options.outHeight;

        // 获取屏幕宽高
        Resources resources = view.getContext().getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();
        int width = dm.widthPixels;

        // 计算缩放率
        float scale = 1.0f;
        if (dw != width) {
            scale = width * 1.0f / dw;
        }

        return scale;
    }

}
