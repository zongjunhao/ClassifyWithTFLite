package com.zjh.classifywithtflite.activities;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSON;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;
import com.zjh.classifywithtflite.R;
import com.zjh.classifywithtflite.base.Label;
import com.zjh.classifywithtflite.base.LabelAdapter;
import com.zjh.classifywithtflite.constant.Constant;

import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;

public class AdminManageActivity extends AppCompatActivity {

    private static final String TAG = "AdminManageActivity";
    // 用于保存收到的标签信息
    private List<Label> labels = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_manage);

        viewLabels(); // 从服务器获取标签列表
        Log.d(TAG, "onCreate: end");
    }

    /**
     * 取得列表后加载RecycleView
     */
    private void setRecycleView() {
        RecyclerView recyclerView = findViewById(R.id.labelRecycle);
        LinearLayoutManager layoutManager = new LinearLayoutManager(AdminManageActivity.this);
        recyclerView.setLayoutManager(layoutManager);
        LabelAdapter labelAdapter = new LabelAdapter(labels, AdminManageActivity.this);
        recyclerView.setAdapter(labelAdapter);
    }

    private void viewLabels() {
        AsyncHttpClient client = new AsyncHttpClient();
        client.post(Constant.VIEW_LABEL_URL, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Toast.makeText(AdminManageActivity.this, "网络错误，查询标签失败", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "onFailure: net error view label fail");
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                Log.d(TAG, "onSuccess: view label success");
                Log.d(TAG, "onSuccess: " + responseString);
                labels = JSON.parseArray(responseString, Label.class);
                Log.d(TAG, "onSuccess: " + labels);
                setRecycleView();
            }
        });
    }

    public void deleteLabel(int labelId) {
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("labelId", labelId);
        client.post(Constant.DELETE_LABEL_URL, params, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Toast.makeText(AdminManageActivity.this, "网络错误，删除标签失败", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "onFailure: net error delete label fail");
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                if (responseString.equals("success")) {
                    Toast.makeText(AdminManageActivity.this, "删除成功", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "onSuccess: delete " + labelId + " success");
                    viewLabels();
                } else {
                    Toast.makeText(AdminManageActivity.this, "未知错误，删除失败", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void addLabel(String labelName) {
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("name", labelName);
        client.post(Constant.ADD_LABEL_URL, params, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Toast.makeText(AdminManageActivity.this, "网络错误，添加标签失败", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "onFailure: net error add label fail");
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                if (responseString.equals("success")) {
                    Toast.makeText(AdminManageActivity.this, "添加成功", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "onSuccess: add label " + labelName + " success");
                    viewLabels();
                } else {
                    Toast.makeText(AdminManageActivity.this, "未知错误，添加失败", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
