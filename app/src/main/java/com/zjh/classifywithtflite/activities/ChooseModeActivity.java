package com.zjh.classifywithtflite.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.zjh.classifywithtflite.kit.FileUtil;
import com.zjh.classifywithtflite.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ChooseModeActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "ChooseModeActivity";
    private static final String PACKAGE_URL_SCHEME = "package:";
    // 权限请求码
    private static final int PERMISSIONS_REQUEST = 108;
    private static final int OPEN_SETTING_REQUEST_COED = 110;
    private static final int CAMERA_PERMISSIONS_REQUEST_CODE = 119;
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

        if (!isTaskRoot()) {
            finish();
        }


        setContentView(R.layout.activity_choose_mode);

        findViewById(R.id.takePhotoByCamera).setOnClickListener(this);
        findViewById(R.id.chooseImageFromDCIM).setOnClickListener(this);
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


    /**
     * 请求存储和相机权限
     */
    private void requestMultiplePermissions() {
        String storagePermission = Manifest.permission.WRITE_EXTERNAL_STORAGE;
        String cameraPermission = Manifest.permission.CAMERA;

        // 判断是否有两种权限
        int hasStoragePermission = ActivityCompat.checkSelfPermission(this, storagePermission);
        int hasCameraPermission = ActivityCompat.checkSelfPermission(this, cameraPermission);

        // 将没有的权限加入到列表中，用于申请权限使用
        List<String> permissions = new ArrayList<>();
        if (hasStoragePermission != PackageManager.PERMISSION_GRANTED) {
            permissions.add(storagePermission);
        }
        if (hasCameraPermission != PackageManager.PERMISSION_GRANTED) {
            permissions.add(cameraPermission);
        }

        // permissions非空（有需要申请的权限）
        if (!permissions.isEmpty()) {
            String[] params = permissions.toArray(new String[permissions.size()]);
            ActivityCompat.requestPermissions(this, params, PERMISSIONS_REQUEST);
        }
    }

    /**
     * 请求权限的回调函数
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST) {
            // permission denied 显示对话框告知用户必须打开权限 (storagePermission )
            // Should we show an explanation?
            // 当app完全没有机会被授权的时候，调用shouldShowRequestPermissionRationale() 返回false
            if (Manifest.permission.WRITE_EXTERNAL_STORAGE.equals(permissions[0])
                    && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                showNeedStoragePermissionDialog();
            } else {
                // 已经被禁止的状态，比如用户在权限对话框中选择了“不再显示”，需要自己弹窗解释
                showMissingStoragePermissionDialog();
            }
        } else if (requestCode == CAMERA_PERMISSIONS_REQUEST_CODE) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                showNeedCameraPermissionDialog();
            } else {
                openSystemCamera();
            }
        }
    }

    /**
     * 显示权限缺失提示，可再次请求动态权限
     */
    private void showNeedStoragePermissionDialog() {
        new AlertDialog.Builder(this)
                .setTitle("权限获取提示")
                .setMessage("必须要有存储权限才能获取到图片")
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions(ChooseModeActivity.this,
                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST);
                    }
                }).setCancelable(false)
                .show();
    }

    /**
     * 显示权限被拒提示，只能进入设置手动改
     */
    private void showMissingStoragePermissionDialog() {
        new AlertDialog.Builder(this)
                .setTitle("权限获取失败")
                .setMessage("必须要有存储权限才能正常运行")
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ChooseModeActivity.this.finish();
                    }
                })
                .setPositiveButton("去设置", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startAppSettings();
                    }
                })
                .setCancelable(false)
                .show();
    }

    private void showNeedCameraPermissionDialog() {
        new AlertDialog.Builder(this)
                .setMessage("摄像头权限被关闭，请开启权限后重试")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create()
                .show();
    }

    /**
     * 启动应用的设置进行授权
     */
    private void startAppSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse(PACKAGE_URL_SCHEME + getPackageName()));
        startActivityForResult(intent, OPEN_SETTING_REQUEST_COED);
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
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSIONS_REQUEST_CODE);
        } else {
            openSystemCamera();
        }
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
                handleInputPhoto(data.getData());
            } else if (requestCode == OPEN_SETTING_REQUEST_COED) {
                requestMultiplePermissions();
            } else if (requestCode == TAKE_PHOTO_REQUEST_CODE) {
                // 如果拍照成功，加载图片并识别
                handleInputPhoto(currentTakePhotoUri);
            }
        }
    }

    /**
     * 跳转到ClassifierActivity进行识别
     * @param imageUri 图片Uri地址
     */
    private void handleInputPhoto(Uri imageUri) {
        Intent intent = new Intent(ChooseModeActivity.this, ClassifierActivity.class);
        intent.putExtra("imageUri", imageUri);
        startActivity(intent);
    }
}
