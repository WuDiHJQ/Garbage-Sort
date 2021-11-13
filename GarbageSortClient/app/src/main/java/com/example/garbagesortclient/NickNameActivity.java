package com.example.garbagesortclient;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.SyncStateContract;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.garbagesortclient.Constants.URL;
import com.example.garbagesortclient.util.Utility;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Response;

public class NickNameActivity extends BaseActivity {

    private SharedPreferences datapref;
    private ProgressDialog progressDialog;
    private FormBody formBody;
    private TextView TitleName;
    private ImageView Back;
    private EditText EditNickName;
    private Button Submit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nickname);
        //初始化控件
        TitleName = findViewById(R.id.title_name);
        Back = findViewById(R.id.back);
        EditNickName = findViewById(R.id.edit_nickname);
        Submit = findViewById(R.id.submit_nickname);
        progressDialog = new ProgressDialog(NickNameActivity.this);
        progressDialog.setMessage("更改中...");
        progressDialog.setCancelable(false);
        datapref = getSharedPreferences("data",MODE_PRIVATE);  //获取自定义的SharedPreferences
        //初始化控件
        TitleName.setText("昵称");
        AddClickListener();  //注册控件点击监听事件
        Intent intent = getIntent();  //获得上一个活动传入的当前用户昵称
        EditNickName.setText(intent.getStringExtra("user_nickname"));

    }

    private void AddClickListener(){
        Back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        Submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String newName = EditNickName.getText().toString();
                if (newName.length() != 0){    //判断输入是否为空
                    if (Utility.JudgeNetWork(NickNameActivity.this)){  //判断当前网络是否可用
                        progressDialog.show();
                        formBody = new FormBody.Builder()    //生成表单数据
                                .add("Key", "user_nickname")
                                .add("Value", newName)
                                .add("user_id",datapref.getInt("user_id",0)+"")
                                .build();
                        Utility.EnqueueHttpRequest(URL.INFOCHANGE, formBody, new Callback() {
                            @Override
                            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        progressDialog.dismiss();  //关闭进度条
                                        Toast.makeText(NickNameActivity.this, "信息修改异常", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }

                            @Override
                            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                                //解析json数据
                                try {
                                    String result = response.body().string();   //获取服务器返回的json数据
                                    JSONObject jsonObject = new JSONObject(result);
                                    if (jsonObject.getBoolean("status") == true) {   //若服务器返回修改成功
                                        SharedPreferences.Editor editor = datapref.edit();
                                        editor.putString("user_nickname",newName);
                                        editor.apply();
                                        Intent intent = new Intent("CHANGE_NAME_BROADCAST");  //生成一条广播
                                        intent.putExtra("user_nickname",datapref.getString("user_nickname",""));
                                        LocalBroadcastManager.getInstance(NickNameActivity.this).sendBroadcast(intent); //发送本地广播
                                        //通知其他界面修改数据
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                progressDialog.dismiss();  //在finish前需要关闭进度条  不然会报窗体泄露
                                                Toast.makeText(NickNameActivity.this,"信息提交成功",Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                        finish();
                                    } else {   //若返回修改失败则表明出现异常
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                progressDialog.dismiss();
                                                Toast.makeText(NickNameActivity.this,"信息修改异常",Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                } catch (Exception e) {   //若出现异常 则关闭进度条并提示用户 防止程序不能操作
                                    e.printStackTrace();
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            progressDialog.dismiss();
                                            Toast.makeText(NickNameActivity.this,"信息修改异常",Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            }
                        });
                    } else{
                        Toast.makeText(NickNameActivity.this,"当前网络不可用，请检查网络连接！",Toast.LENGTH_SHORT).show();
                    }
                } else{
                    Toast.makeText(NickNameActivity.this,"昵称不能为空",Toast.LENGTH_SHORT).show();
                }
            }
        });


    }


}
