package com.zjh.classifywithtflite.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;
import com.zjh.classifywithtflite.R;
import com.zjh.classifywithtflite.base.Image;
import com.zjh.classifywithtflite.base.ImageAdapter;
import com.zjh.classifywithtflite.constant.Constant;

import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;

public class ImageManageActivity extends AppCompatActivity {

    private static final String TAG = "ImageManageActivity";
    // 用于保存收到的图片的信息
    private List<Image> images = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_manage);

        Intent intent = getIntent();
        int labelId = intent.getIntExtra("labelId", 0);

        viewImage(labelId); // 从服务器获取图片列表
        Log.d(TAG, "onCreate: end");
    }

    /**
     * 取得列表后加载RecycleView
     */
    private void setRecycleView() {
        RecyclerView recyclerView = findViewById(R.id.imageRecycle);
        LinearLayoutManager layoutManager = new LinearLayoutManager(ImageManageActivity.this);
        recyclerView.setLayoutManager(layoutManager);
        ImageAdapter imageAdapter = new ImageAdapter(images, ImageManageActivity.this);
        recyclerView.setAdapter(imageAdapter);
    }

    /**
     * 根据标签查看图片列表
     *
     * @param labelId 标签id
     */
    public void viewImage(int labelId) {
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("labelId", labelId);
        client.post(Constant.VIEW_IMAGES_URL, params, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Toast.makeText(ImageManageActivity.this, "网络错误，查询图片失败", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "onFailure: net error view image fail");
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                Log.d(TAG, "onSuccess: view label success");
                Log.d(TAG, "onSuccess: " + responseString);
                images = JSON.parseArray(responseString, Image.class);
                Log.d(TAG, "onSuccess: " + images);
                setRecycleView();
            }
        });
    }

    /**
     * 删除图片
     *
     * @param imageId 图片id
     */
    public void deleteImage(int imageId) {
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("imageId", imageId);
        client.post(Constant.DELETE_IMAGE_URL, params, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Toast.makeText(ImageManageActivity.this, "网络错误，删除标签失败", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "onFailure: net error delete label fail");
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                if (responseString.equals("success")) {
                    Toast.makeText(ImageManageActivity.this, "删除成功", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "onSuccess: delete " + imageId + " success");
//                    viewLabels();
                } else {
                    Toast.makeText(ImageManageActivity.this, "未知错误，删除失败", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
