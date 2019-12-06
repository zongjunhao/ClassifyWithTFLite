package com.zjh.classifywithtflite.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSON;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;
import com.zjh.classifywithtflite.R;
import com.zjh.classifywithtflite.base.Image;
import com.zjh.classifywithtflite.base.ImageAdapter;
import com.zjh.classifywithtflite.constant.Constant;
import com.zjh.classifywithtflite.kit.FileUtil;
import com.zjh.classifywithtflite.kit.ImageKit;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;

public class ImageManageActivity extends AppCompatActivity {

    private static final String TAG = "ImageManageActivity";
    // 请求码
    private static final int TAKE_PHOTO_REQUEST_CODE = 120;
    private static final int PICTURE_REQUEST_CODE = 911;
    private Uri currentTakePhotoUri;

    private List<Image> images = new ArrayList<>();    // 用于保存收到的图片的信息
    private int labelId;    // 保存当前图片所属的标签序号

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_manage);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        labelId = intent.getIntExtra("labelId", 0);

        viewImage(labelId); // 从服务器获取图片列表
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
        switch (item.getItemId()) {
            case R.id.add:
                chooseImageFromDCIM();
                break;
            case R.id.generate:
                Toast.makeText(this, "服务器正在生成模型，请耐心等待", Toast.LENGTH_SHORT).show();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
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
        Log.d(TAG, "viewImage: " + Constant.VIEW_IMAGES_URL);
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
        Log.d(TAG, "deleteImage: " + Constant.DELETE_IMAGE_URL);
        client.post(Constant.DELETE_IMAGE_URL, params, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Toast.makeText(ImageManageActivity.this, "网络错误，删除图片失败", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "onFailure: net error delete image fail");
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                if (responseString.equals("success")) {
                    Toast.makeText(ImageManageActivity.this, "删除成功", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "onSuccess: delete " + imageId + " success");
                    viewImage(labelId);
                } else {
                    Toast.makeText(ImageManageActivity.this, "未知错误，删除失败", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * 添加图片的http请求
     *
     * @param labelId   标签ID
     * @param imageFile 图片文件
     * @throws FileNotFoundException 文件未找到异常
     */
    public void addImage(int labelId, File imageFile) throws FileNotFoundException {
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("file", imageFile, "Content-Type");
        params.put("labelId", labelId);
        Log.d(TAG, "addImage: " + Constant.ADD_IMAGE_URL);
        client.post(Constant.ADD_IMAGE_URL, params, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Toast.makeText(ImageManageActivity.this, "网络错误，添加图片失败", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "onFailure: net error add image fail");

            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                if (responseString.equals("success")) {
                    Toast.makeText(ImageManageActivity.this, "添加成功", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "onSuccess: add image success");
                    viewImage(labelId);
                } else {
                    Toast.makeText(ImageManageActivity.this, "未知错误，添加图片失败", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * 打开添加图片的提示框，选择添加方式
     */
    public void openAddImageDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("选择上传方式");
        builder.setPositiveButton("相册选取", (dialog, which) -> {
            Toast.makeText(this, "点击了从相册选取", Toast.LENGTH_SHORT).show();
            chooseImageFromDCIM();
        });
        builder.setNegativeButton("拍照上传", (dialog, which) -> {
            Toast.makeText(this, "点击了拍照上传", Toast.LENGTH_SHORT).show();
            takePhotoByCamera();
        });
        builder.show();
    }

    /**
     * 使用系统相机拍照
     */
    private void takePhotoByCamera() {
        openSystemCamera();
    }

    /**
     * 选择一张图片并裁剪获得一个小图
     */
    private void chooseImageFromDCIM() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, PICTURE_REQUEST_CODE);
    }

    /**
     * 打开系统相机
     */
    private void openSystemCamera() {
        // 调用系统相机
        Intent takePhotoIntent = new Intent();
        takePhotoIntent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);

        // 这句的作用是如果没有相机则该应用不会闪退，要是不加这句则当系统没有相机应用的时候该应用会闪退
        if (takePhotoIntent.resolveActivity(getPackageManager()) == null) {
            Toast.makeText(this, "当前系统没有可用的相机应用", Toast.LENGTH_SHORT).show();
            return;
        }

        String fileName = "TF_" + System.currentTimeMillis() + ".jpg";
        File photoFile = new File(FileUtil.getPhotoCacheFolder(), fileName);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // 通过FileProvider创建一个content类型的Uri
            currentTakePhotoUri = FileProvider.getUriForFile(this, "com.zjh.classifywithtflite.fileprovider", photoFile);
            takePhotoIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Log.d(TAG, "openSystemCamera: 11111111111111111111111111111111111");
        } else {
            currentTakePhotoUri = Uri.fromFile(photoFile);
            Log.d(TAG, "openSystemCamera: 22222222222222222222222222222222222");
        }

        // 将拍照结果保存至outputFile的Uri中，不保留在相册中
        takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, currentTakePhotoUri);
        startActivityForResult(takePhotoIntent, TAKE_PHOTO_REQUEST_CODE);
    }

    /**
     * 拍照或选择图片的回调函数
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == PICTURE_REQUEST_CODE) {
                // 处理选择的图片
                assert data != null;
//                handleInputPhoto(data.getData());
                try {
                    openConfirmDialog(data.getData());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (requestCode == TAKE_PHOTO_REQUEST_CODE) {
                // 如果拍照成功，加载图片并识别
//                handleInputPhoto(currentTakePhotoUri);
                try {
                    openConfirmDialog(currentTakePhotoUri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 打开确认上传的对话框
     *
     * @param uri 图片uri
     */
    public void openConfirmDialog(Uri uri) throws IOException {
        Log.d(TAG, "openConfirmDialog: " + uri);

        // 新建ImageView，在Dialog中预览要上传的图片
        final ImageView imageView = new ImageView(this);
        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
        imageView.setImageBitmap(bitmap);

        // Dialog对话框
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("确认上传");
        builder.setView(imageView);
        builder.setPositiveButton("确认", (dialog, which) -> {
            Toast.makeText(this, "你点了确认", Toast.LENGTH_SHORT).show();
            String path = ImageKit.getRealPathFromUri(ImageManageActivity.this, uri);
            Log.d(TAG, "openConfirmDialog: " + path);
            File file = new File(path);
            try {
                addImage(labelId, file);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        });
        builder.setNegativeButton("取消", (dialog, which) ->
                Toast.makeText(this, "你点了取消", Toast.LENGTH_SHORT).show());
        builder.show();
    }
}
