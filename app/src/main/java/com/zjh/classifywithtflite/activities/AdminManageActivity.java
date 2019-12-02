package com.zjh.classifywithtflite.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.TextHttpResponseHandler;
import com.zjh.classifywithtflite.R;
import com.zjh.classifywithtflite.base.Label;
import com.zjh.classifywithtflite.constant.Constant;

import org.json.JSONObject;

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

        viewLabels();
    }

    private void viewLabels() {
        AsyncHttpClient client = new AsyncHttpClient();
        client.post(Constant.VIEW_LABEL_URL, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Toast.makeText(AdminManageActivity.this, "网络错误，查询标签失败", Toast.LENGTH_SHORT ).show();
                Log.d(TAG, "onFailure: net error view label fail");
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                Log.d(TAG, "onSuccess: view label success");
                Log.d(TAG, "onSuccess: " + responseString);
                labels = JSON.parseArray(responseString, Label.class);
                Log.d(TAG, "onSuccess: " + labels);
            }
        });
    }
}
