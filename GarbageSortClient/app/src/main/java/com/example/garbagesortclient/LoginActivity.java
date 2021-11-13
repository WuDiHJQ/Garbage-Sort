package com.example.garbagesortclient;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.example.garbagesortclient.Constants.MyConstants;
import com.example.garbagesortclient.Constants.URL;
import com.example.garbagesortclient.util.Utility;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Response;

public class LoginActivity extends BaseActivity {

    private ProgressDialog progressDialog;
    private FormBody formBody;
    private TextView TitleName;
    private ImageView Back;
    private EditText Account;
    private EditText Password;
    private CircleImageView LoginButton;
    private TextView Register;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        //初始化控件
        TitleName = findViewById(R.id.title_name);
        Back = findViewById(R.id.back);
        Account = findViewById(R.id.login_account);
        Password = findViewById(R.id.login_password);
        LoginButton = findViewById(R.id.login_button);
        Register = findViewById(R.id.register);
        progressDialog = new ProgressDialog(LoginActivity.this);
        progressDialog.setMessage("登陆中...");   //进度条开始加载
        progressDialog.setCancelable(false);
        //初始化控件
        TitleName.setText("登录");
        AddClickListener();  //注册控件点击监听事件
    }

    private void AddClickListener(){
        Back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        Register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this,RegisterActivity.class);
                startActivityForResult(intent,MyConstants.REGISTER_BACK);
                Account.setText("");    //清空账号和密码输入框
                Password.setText("");
            }
        });
        LoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Utility.JudgeNetWork(LoginActivity.this)) {  //若无网络 进行下段操作会抛出异常
                    String account = Account.getText().toString();      //获取用户输入的账号和密码
                    String password = Password.getText().toString();
                    if (account.isEmpty() || password.isEmpty())    //如果账号或密码其中有空
                        Toast.makeText(LoginActivity.this, "请输入账号和密码!", Toast.LENGTH_SHORT).show();
                    else {
                        formBody = new FormBody.Builder()    //生成表单数据
                                .add("account", account)
                                .add("password", password)
                                .build();
                        progressDialog.show();
                        Utility.EnqueueHttpRequest(URL.USERLOGIN, formBody, new Callback() {  //发起http请求
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
                                        final SharedPreferences datapref = getSharedPreferences("data",MODE_PRIVATE);
                                        SharedPreferences.Editor editor = datapref.edit();
                                        editor.putBoolean("Login_Status",true);
                                        editor.putInt("user_id",jsonObject.getInt("user_id"));
                                        editor.putInt("user_score",jsonObject.getInt("user_score"));
                                        editor.putString("user_account",jsonObject.getString("user_account"));
                                        editor.putString("user_nickname",jsonObject.getString("user_nickname"));
                                        editor.apply();
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                progressDialog.dismiss();  //在finish前需要关闭进度条  不然会报窗体泄露
                                            }
                                        });
                                        setResult(RESULT_OK);   //登录成功向上一页面传递登录返回确认码
                                        finish();
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == MyConstants.REGISTER_BACK) {
            Account.setText(data.getStringExtra("phone_num"));  //获取注册页面返回的手机号数据并设置在输入框
            Toast.makeText(LoginActivity.this, "注册成功！快去登陆吧！", Toast.LENGTH_SHORT).show();
        }
    }

}