package com.zjh.classifywithtflite.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;
import com.zjh.classifywithtflite.R;
import com.zjh.classifywithtflite.constant.Constant;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private static final String PACKAGE_URL_SCHEME = "package:";
    // 权限请求码
    private static final int PERMISSIONS_REQUEST = 108;
    private static final int OPEN_SETTING_REQUEST_COED = 110;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        TabHost tab = findViewById(android.R.id.tabhost);               // tab容器
        EditText userEmail = findViewById(R.id.userEmail);              // 用户邮箱
        EditText userPassword = findViewById(R.id.userPassword);        // 用户密码
        EditText adminAccount = findViewById(R.id.adminAccount);        // 管理员账号
        EditText adminPassword = findViewById(R.id.adminPassword);      // 管理员密码
        Button userLoginButton = findViewById(R.id.userLoginButton);    // 用户登录按钮
        Button adminLoginButton = findViewById(R.id.adminLoginButton);  // 管理员登录按钮
        TextView goToRegister = findViewById(R.id.goToRegister);        // 转去注册页面

        // 初始化TabHost容器
        tab.setup();
        // 在TabHost创建标签，然后设置：标题／图标／标签页布局
        tab.addTab(tab.newTabSpec("tab1").setIndicator("用户", null).setContent(R.id.userLogin));
        tab.addTab(tab.newTabSpec("tab2").setIndicator("管理员", null).setContent(R.id.adminLogin));

        userLoginButton.setOnClickListener(v -> {
            Toast.makeText(LoginActivity.this, "用户登录", Toast.LENGTH_SHORT).show();
            userLogin(userEmail.getText().toString(), userPassword.getText().toString());
        });

        adminLoginButton.setOnClickListener(v -> {
            Toast.makeText(LoginActivity.this, "管理员登录", Toast.LENGTH_SHORT).show();
            adminLogin(adminAccount.getText().toString(), adminPassword.getText().toString());
        });

        goToRegister.setOnClickListener(v -> {
            Toast.makeText(LoginActivity.this, "转去注册", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        // 请求存储和相机权限
        requestMultiplePermissions();

        checkVersion();
    }

    /**
     * 用户登录
     *
     * @param email    邮箱
     * @param password 密码
     */
    private void userLogin(String email, String password) {
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.add("email", email);
        params.add("password", password);
        Log.d(TAG, "userLogin: " + Constant.USER_LOGIN_URL);
        client.post(Constant.USER_LOGIN_URL, params, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.d(TAG, "onFailure: user login fail net error");
                Toast.makeText(LoginActivity.this, "网络错误，登录失败", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                Log.d(TAG, "onSuccess: " + responseString);
                if (responseString.equals("success")) {
                    Log.d(TAG, "onSuccess: login success flag");
                    Intent intent = new Intent(LoginActivity.this, ChooseModeActivity.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(LoginActivity.this, "输入信息有误，请重新登录", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * 管理员登录
     *
     * @param account  账号
     * @param password 密码
     */
    private void adminLogin(String account, String password) {
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.add("account", account);
        params.add("password", password);
        Log.d(TAG, "adminLogin: " + Constant.ADMIN_LOGIN_URL);
        client.post(Constant.ADMIN_LOGIN_URL, params, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.d(TAG, "onFailure: admin login fail net error");
                Toast.makeText(LoginActivity.this, "网络错误，登录失败", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                Log.d(TAG, "onSuccess: " + responseString);
                if (responseString.equals("success")) {
                    Log.d(TAG, "onSuccess: login success flag");
                    Intent intent = new Intent(LoginActivity.this, LabelManageActivity.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(LoginActivity.this, "输入信息有误，请重新登录", Toast.LENGTH_SHORT).show();
                }
            }
        });
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
            if (Manifest.permission.WRITE_EXTERNAL_STORAGE.equals(permissions[0]) && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                //permission denied 显示对话框告知用户必须打开权限 (storagePermission )
                // Should we show an explanation?
                // 当app完全没有机会被授权的时候，调用shouldShowRequestPermissionRationale() 返回false
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    // 系统弹窗提示授权
                    showNeedStoragePermissionDialog();
                } else {
                    // 已经被禁止的状态，比如用户在权限对话框中选择了"不再显示”，需要自己弹窗解释
                    showMissingStoragePermissionDialog();
                }
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
                .setNegativeButton("取消", (dialog, which) -> dialog.cancel())
                .setPositiveButton("确定", (dialog, which) ->
                        ActivityCompat.requestPermissions(
                                LoginActivity.this,
                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                PERMISSIONS_REQUEST))
                .setCancelable(false)
                .show();
    }

    /**
     * 显示权限被拒提示，只能进入设置手动改
     */
    private void showMissingStoragePermissionDialog() {
        new AlertDialog.Builder(this)
                .setTitle("权限获取失败")
                .setMessage("必须要有存储权限才能正常运行")
                .setNegativeButton("取消", (dialog, which) -> LoginActivity.this.finish())
                .setPositiveButton("去设置", (dialog, which) -> startAppSettings())
                .setCancelable(false)
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

    private void checkVersion() {
        // 读取本地版本
        StringBuilder clientVersion = new StringBuilder();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(this.getAssets().open("version.txt")));
            String line;
            while ((line = reader.readLine()) != null) {
                clientVersion.append(line);
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "checkVersion: clientVersion" + clientVersion.toString());

        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.add("clientVersion", clientVersion.toString());
        Log.d(TAG, "checkVersion: " + Constant.CHECK_VERSION_URL);
        client.post(Constant.CHECK_VERSION_URL, params, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.d(TAG, "onFailure: check version fail net error");
                Toast.makeText(LoginActivity.this, "网络错误，检查版本失败", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                Log.d(TAG, "onSuccess: " + responseString);
                if (responseString.equals("update")) {
                    Log.d(TAG, "onSuccess: check version flag");
                    openUpdateDialog();
                }
            }
        });
    }

    private void openUpdateDialog() {
        // Dialog对话框
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("提示");
        builder.setMessage("有新版本可用，点击更新");
        builder.setPositiveButton("确认", (dialog, which) -> {
            Uri uri = Uri.parse(Constant.APK_DOWNLOAD_URL);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        });
        builder.setNegativeButton("取消", (dialog, which) ->
                Toast.makeText(this, "你点了取消", Toast.LENGTH_SHORT).show());
        builder.show();
    }
}
