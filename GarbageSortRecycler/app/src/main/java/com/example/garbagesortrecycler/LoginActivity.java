package com.example.garbagesortrecycler;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import com.example.garbagesortrecycler.Constans.URL;
import com.example.garbagesortrecycler.util.Utility;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Response;

public class LoginActivity extends BaseActivity {

    private ProgressDialog progressDialog;
    private TextView TitleName;
    private FormBody formBody;
    private EditText Account;
    private EditText Password;
    private Button LoginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        //初始化控件
        TitleName = findViewById(R.id.title_name);
        Account = findViewById(R.id.login_account);
        Password = findViewById(R.id.login_password);
        LoginButton = findViewById(R.id.login_button);
        progressDialog = new ProgressDialog(LoginActivity.this);
        progressDialog.setMessage("登陆中...");   //进度条开始加载
        progressDialog.setCancelable(false);
        //初始化控件
        TitleName.setText("登录");
        AddClickListener();  //注册控件点击监听事件
    }

    private void AddClickListener(){
        LoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Utility.JudgeNetWork(LoginActivity.this)) {  //若无网络 进行下段操作会抛出异常
                    String account = Account.getText().toString();      //获取输入的账号和密码
                    String password = Password.getText().toString();
                    if (account.isEmpty() || password.isEmpty())    //如果账号或密码其中有空
                        Toast.makeText(LoginActivity.this, "请输入账号和密码!", Toast.LENGTH_SHORT).show();
                    else {
                        formBody = new FormBody.Builder()    //生成表单数据
                                .add("account", account)
                                .add("password", password)
                                .build();
                        progressDialog.show();
                        Utility.EnqueueHttpRequest(URL.ADMINLOGIN, formBody, new Callback() {  //发起http请求
                            @Override
                            public void onFailure(@NotNull Call call, @NotNull IOException e) {   //失败则提醒用户
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        progressDialog.dismiss();  //关闭进度条
                                        Toast.makeText(LoginActivity.this, "登陆异常", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }

                            @Override
                            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                                //解析json数据
                                try {
                                    String result = response.body().string();   //获取服务器返回的json数据
                                    JSONObject jsonObject = new JSONObject(result);
                                    if (jsonObject.getBoolean("status") == true) {   //若账号密码匹配则status为true
                                        final SharedPreferences datapref = getSharedPreferences("admin",MODE_PRIVATE);
                                        SharedPreferences.Editor editor = datapref.edit();
                                        editor.putInt("admin_id",jsonObject.getInt("admin_id"));
                                        editor.apply();
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                progressDialog.dismiss();  //在finish前需要关闭进度条  不然会报窗体泄露
                                                Toast.makeText(LoginActivity.this,"登陆成功",Toast.LENGTH_SHORT).show();
                                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                                startActivity(intent);
                                                finish();
                                            }
                                        });
                                    } else {   //若返回status为false
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                progressDialog.dismiss();  //关闭进度条
                                                AlertDialog.Builder dialog = new AlertDialog.Builder(LoginActivity.this);  //创建警告窗口
                                                dialog.setTitle("登录失败");
                                                dialog.setMessage("请确认你输入了正确的账号和密码!");
                                                dialog.setCancelable(false);
                                                dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        Password.setText("");   //清空密码输入框
                                                    }
                                                });
                                                dialog.show();  //显示警告窗口
                                            }
                                        });
                                    }
                                } catch (Exception e) {   //若出现异常 则关闭进度条并提示用户 防止程序不能操作
                                    e.printStackTrace();
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            progressDialog.dismiss();
                                            Toast.makeText(LoginActivity.this,"登录异常",Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            }
                        });
                    }
                }else{   //若无网络 则提醒用户
                    Toast.makeText(LoginActivity.this,"当前网络不可用，请检查网络连接！",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


}