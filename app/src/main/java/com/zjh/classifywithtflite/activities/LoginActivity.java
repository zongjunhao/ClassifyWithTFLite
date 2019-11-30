package com.zjh.classifywithtflite.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;
import com.zjh.classifywithtflite.R;
import com.zjh.classifywithtflite.constant.Constant;

import cz.msebera.android.httpclient.Header;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        TabHost tab = (TabHost) findViewById(android.R.id.tabhost);
        EditText userEmail = findViewById(R.id.userEmail);
        EditText userPassword = findViewById(R.id.userPassword);
        EditText adminAccount = findViewById(R.id.adminAccount);
        EditText adminPassword = findViewById(R.id.adminPassword);

        //初始化TabHost容器
        tab.setup();
        //在TabHost创建标签，然后设置：标题／图标／标签页布局
        tab.addTab(tab.newTabSpec("tab1").setIndicator("用户", null).setContent(R.id.userLogin));
        tab.addTab(tab.newTabSpec("tab2").setIndicator("管理员", null).setContent(R.id.adminLogin));

        Button userLoginButton = findViewById(R.id.userLoginButton);
        userLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(LoginActivity.this, "用户登录", Toast.LENGTH_SHORT).show();
                userLogin(userEmail.getText().toString(), userPassword.getText().toString());
            }
        });

        Button adminLoginButton = findViewById(R.id.adminLoginButton);
        adminLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(LoginActivity.this, "管理员登录", Toast.LENGTH_SHORT).show();
                adminLogin(adminAccount.getText().toString(), adminPassword.getText().toString());
            }
        });

        TextView goToRegister = findViewById(R.id.goToRegister);
        goToRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(LoginActivity.this, "转去注册", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }

    private void userLogin(String email, String password) {
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.add("email", email);
        params.add("password", password);
        Log.d(TAG, "userLogin: "+Constant.USER_LOGIN_URL);
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
//                    Intent intent = new Intent(LoginActivity.this, ClassifierActivity.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(LoginActivity.this, "输入信息有误，请重新登录", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void adminLogin(String account, String password){
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.add("account", account);
        params.add("password", password);
        Log.d(TAG, "adminLogin: "+Constant.ADMIN_LOGIN_URL);
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
//                    Intent intent = new Intent(LoginActivity.this, ChooseModeActivity.class);
                    Intent intent = new Intent(LoginActivity.this, AdminManageActivity.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(LoginActivity.this, "输入信息有误，请重新登录", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
