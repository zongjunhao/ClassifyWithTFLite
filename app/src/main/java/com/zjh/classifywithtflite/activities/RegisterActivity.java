package com.zjh.classifywithtflite.activities;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;
import com.zjh.classifywithtflite.R;
import com.zjh.classifywithtflite.constant.Constant;

import cz.msebera.android.httpclient.Header;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rigister);

        Button returnToLogin = findViewById(R.id.returnToLogin);
        Button register = findViewById(R.id.register);
        EditText registerUserName = findViewById(R.id.registerUserName);
        EditText registerPassword = findViewById(R.id.registerPassword);
        EditText registerConfirmPassword = findViewById(R.id.registerConfirmPassword);
        EditText registerEmail = findViewById(R.id.registerEmail);

        returnToLogin.setOnClickListener(v -> finish());

        register.setOnClickListener(v -> {
            String account = registerUserName.getText().toString();
            String password = registerPassword.getText().toString();
            String confirmPassword = registerConfirmPassword.getText().toString();
            String email = registerEmail.getText().toString();
            register(account, password, confirmPassword, email);
        });
    }

    private void register(String userName, String password, String confirmPassword, String email) {
        if (password.equals(confirmPassword)) {
            AsyncHttpClient client = new AsyncHttpClient();
            RequestParams params = new RequestParams();
            params.put("account", userName);
            params.put("password", password);
            params.put("email", email);
            client.post(Constant.USER_REGISTER_URL, params, new TextHttpResponseHandler() {
                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    Log.d(TAG, "register: register fail net error");
                    Toast.makeText(RegisterActivity.this, "网络错误，注册失败", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, String responseString) {
                    if (responseString.equals("success")) {
                        Log.d(TAG, "register: register success");
                        Toast.makeText(RegisterActivity.this, "注册成功", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Log.d(TAG, "register: register fail unknown error");
                        Toast.makeText(RegisterActivity.this, "未知错误，注册失败", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            Log.d(TAG, "register: register fail password different");
            Toast.makeText(RegisterActivity.this, "两次密码输入不一致，请重新输入", Toast.LENGTH_SHORT).show();
        }
    }
}
