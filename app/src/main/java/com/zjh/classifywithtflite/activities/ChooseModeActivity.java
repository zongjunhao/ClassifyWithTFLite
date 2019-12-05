package com.zjh.classifywithtflite.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.FileProvider;

import com.zjh.classifywithtflite.R;
import com.zjh.classifywithtflite.kit.FileUtil;

import java.io.File;

public class ChooseModeActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "ChooseModeActivity";
    // 请求码
    private static final int TAKE_PHOTO_REQUEST_CODE = 120;
    private static final int PICTURE_REQUEST_CODE = 911;
    // 拍照所得图片的保存路径
    private static final String CURRENT_TAKE_PHOTO_URI = "currentTakePhotoUri";
    private Uri currentTakePhotoUri;

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_mode);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        findViewById(R.id.takePhotoByCamera).setOnClickListener(this);
        findViewById(R.id.chooseImageFromDCIM).setOnClickListener(this);
    }

    /**
     * 点击左上角返回按钮返回上一级活动
     */
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // 防止拍照后无法返回当前activity时数据丢失
        savedInstanceState.putParcelable(CURRENT_TAKE_PHOTO_URI, currentTakePhotoUri);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            currentTakePhotoUri = savedInstanceState.getParcelable(CURRENT_TAKE_PHOTO_URI);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.takePhotoByCamera:
                takePhotoByCamera();
                break;
            case R.id.chooseImageFromDCIM:
                chooseImageFromDCIM();
                break;
            default:
                break;
        }
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == PICTURE_REQUEST_CODE) {
                // 处理选择的图片
                assert data != null;
                handleInputPhoto(data.getData());
            } else if (requestCode == TAKE_PHOTO_REQUEST_CODE) {
                // 如果拍照成功，加载图片并识别
                handleInputPhoto(currentTakePhotoUri);
            }
        }
    }

    /**
     * 跳转到ClassifierActivity进行识别
     *
     * @param imageUri 图片Uri地址
     */
    private void handleInputPhoto(Uri imageUri) {
        Intent intent = new Intent(ChooseModeActivity.this, ClassifierActivity.class);
        intent.putExtra("imageUri", imageUri);
        startActivity(intent);
    }
}
