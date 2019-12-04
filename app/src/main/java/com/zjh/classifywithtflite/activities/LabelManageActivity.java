package com.zjh.classifywithtflite.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
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

public class LabelManageActivity extends AppCompatActivity {

    private static final String TAG = "LabelManageActivity";
    // 用于保存收到的标签信息
    private List<Label> labels = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_label_manage);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        viewLabels(); // 从服务器获取标签列表
        Log.d(TAG, "onCreate: end");
    }

    /**
     * 点击左上角返回按钮返回上一级活动
     */
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }

    /**
     * 在右上角添加按钮
     *
     * @param menu 自定义的图形
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * 点击右上角按钮的动作
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.add) {
            Toast.makeText(this, "点击了标签界面右上角加号", Toast.LENGTH_SHORT).show();
            openAddLabelDialog();
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 取得列表后加载RecycleView
     */
    private void setRecycleView() {
        RecyclerView recyclerView = findViewById(R.id.labelRecycle);
        LinearLayoutManager layoutManager = new LinearLayoutManager(LabelManageActivity.this);
        recyclerView.setLayoutManager(layoutManager);
        LabelAdapter labelAdapter = new LabelAdapter(labels, LabelManageActivity.this);
        recyclerView.setAdapter(labelAdapter);
    }

    /**
     * 查看标签列表
     */
    private void viewLabels() {
        AsyncHttpClient client = new AsyncHttpClient();
        client.post(Constant.VIEW_LABEL_URL, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Toast.makeText(LabelManageActivity.this, "网络错误，查询标签失败", Toast.LENGTH_SHORT).show();
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

    /**
     * 删除标签
     *
     * @param labelId 标签id
     */
    public void deleteLabel(int labelId) {
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("labelId", labelId);
        client.post(Constant.DELETE_LABEL_URL, params, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Toast.makeText(LabelManageActivity.this, "网络错误，删除标签失败", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "onFailure: net error delete label fail");
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                if (responseString.equals("success")) {
                    Toast.makeText(LabelManageActivity.this, "删除成功", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "onSuccess: delete " + labelId + " success");
                    viewLabels();
                } else {
                    Toast.makeText(LabelManageActivity.this, "未知错误，删除失败", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * 添加标签
     *
     * @param labelName 标签名
     */
    public void addLabel(String labelName) {
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("name", labelName);
        client.post(Constant.ADD_LABEL_URL, params, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Toast.makeText(LabelManageActivity.this, "网络错误，添加标签失败", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "onFailure: net error add label fail");
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                if (responseString.equals("success")) {
                    Toast.makeText(LabelManageActivity.this, "添加成功", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "onSuccess: add label " + labelName + " success");
                    viewLabels();
                } else {
                    Toast.makeText(LabelManageActivity.this, "未知错误，添加失败", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * 打开添加标签的对话框
     */
    public void openAddLabelDialog() {
        // 新建EditText以在Dialog中输入信息
        final EditText edit = new EditText(this);
        // Dialog对话框
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("请输入标签名");
        builder.setView(edit);
        builder.setPositiveButton("确认", (dialog, which) -> {
            Toast.makeText(this, "你输入的是: " + edit.getText().toString(), Toast.LENGTH_SHORT).show();
            addLabel(edit.getText().toString());
        });
        builder.setNegativeButton("取消", (dialog, which) ->
                Toast.makeText(this, "你点了取消", Toast.LENGTH_SHORT).show());
        builder.show();
    }

    /**
     * 点击标签后跳转到图片管理界面查看此标签类别下的图片
     *
     * @param labelId 标签id
     */
    public void viewImageByLabel(int labelId) {
        Intent intent = new Intent(LabelManageActivity.this, ImageManageActivity.class);
        intent.putExtra("labelId", labelId);
        startActivity(intent);
    }
}
