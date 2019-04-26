package com.ams.sample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import com.ams.imageloader.ImageLoader;

public class MainActivity extends AppCompatActivity {

    // 图片来源：https://pixabay.com/
    private String url = "https://cdn.pixabay.com/photo/2016/07/01/07/51/tent-1490599__340.jpg";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageView imageView = findViewById(R.id.imageView);

        // 最基本的用法
        ImageLoader.getInstance().load(url).into(imageView);

    }
}
