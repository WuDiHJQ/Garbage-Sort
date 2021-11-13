package com.example.garbagesortmachine;


import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.garbagesortmachine.Constants.URL;
import com.example.garbagesortmachine.util.Utility;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Response;

public class ThrowActivity extends BaseActivity {

    private int UserId;
    private SharedPreferences datapref;
    private TextView NickName;
    private TextView Greeting;
    private TextView machine_id;
    private TextView Back;
    private TextView TimeShow;
    private CountDownTimer ThrowTimer;
    private CircleImageView FinishThrow;
    private int RemainTime = 30;
    private ProgressDialog progressDialog;
    private FormBody formBody;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_throw);
        UserId = getIntent().getIntExtra("user_id",0);
        //设置用户昵称
        NickName = findViewById(R.id.user_nickname);
        NickName.setText(getIntent().getStringExtra("user_nickname"));
        //设置问候语
        Greeting = findViewById(R.id.Greeting);
        Calendar calendar=Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);   //获取当前的时间为几点
        if (hour >= 6 && hour <= 11)  //若为上午6点~11点  提示早上好
            Greeting.setText("上午好，");
        else if (hour >= 12 && hour <= 17)    //若为下午12点~17点  提示早上好
            Greeting.setText("下午好，");
        else
            Greeting.setText("晚上好，");
        //设置机器编号
        datapref = getSharedPreferences("data",MODE_PRIVATE);  //获取自定义的SharedPreferences
        machine_id = findViewById(R.id.machine_id);
        SharedPreferences datapref = getSharedPreferences("data",MODE_PRIVATE);  //获取自定义的SharedPreferences
        machine_id.setText("机器编号：" + Utility.FormatId(datapref.getInt("machine_id",0)));
        Back = findViewById(R.id.back);
        TimeShow = findViewById(R.id.time);
        FinishThrow = findViewById(R.id.finish_throw);
        AddClickListener();  //注册控件点击监听事件
        Utility.OpenGate(ThrowActivity.this);  //打开垃圾箱门
        ThrowTimer = new CountDownTimer(30000,1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                TimeShow.setText(RemainTime + "");
                RemainTime--;
            }

            @Override
            public void onFinish() {   //当倒计时结束 还未投放垃圾
                Utility.CloseGate(ThrowActivity.this);  //关闭垃圾箱门并结束活动
                finish();
            }
        }.start();   //启动倒计时任务



    }

    private void AddClickListener(){
        Back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utility.CloseGate(ThrowActivity.this);
                finish();
            }
        });
        FinishThrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ThrowTimer.cancel();    //取消倒计时
                Utility.CloseGate(ThrowActivity.this);    //关闭垃圾箱门
                progressDialog = new ProgressDialog(ThrowActivity.this);
                progressDialog.setMessage("识别并称重中...");
                progressDialog.setCancelable(false);
                progressDialog.show();
                //开始识别称重并上传记录
                final int Type = Utility.IdentifyTrash();
                final int Weight = Utility.WeighTrash();
                SimpleDateFormat dateformat =new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String time = dateformat.format(new Date(System.currentTimeMillis()));  //格式化当前时间
                formBody = new FormBody.Builder()    //生成表单数据
                        .add("user_id", UserId+"")
                        .add("bin_id", datapref.getInt("machine_id",0)+"")
                        .add("time", time)
                        .add("trash_weight", Weight+"")
                        .add("trash_type", Type+"")
                        .build();
                Utility.EnqueueHttpRequest(URL.SUBMITRECORD, formBody, new Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progressDialog.dismiss();
                                Toast.makeText(ThrowActivity.this,"记录上传失败",Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                        try {
                            String result = response.body().string();   //获取服务器返回的json数据
                            JSONObject jsonObject = new JSONObject(result);
                            if (jsonObject.getBoolean("status") == true) {   //若记录上传成功
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        progressDialog.dismiss();   //关闭进度条
                                        Intent intent = new Intent(ThrowActivity.this,SuccessActivity.class);
                                        intent.putExtra("trash_type",Type);    //传输类型和重量跳转到投放成功页面
                                        intent.putExtra("trash_weight",Weight);
                                        startActivity(intent);
                                        finish();   //销毁当前活动及倒计时
                                    }
                                });
                            } else {   //若返回status为false
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        progressDialog.dismiss();
                                        Toast.makeText(ThrowActivity.this,"上传记录失败",Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    progressDialog.dismiss();
                                    Toast.makeText(ThrowActivity.this,"数据处理异常",Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                });
            }
        });




    }

    @Override
    protected void onDestroy() {
        if (ThrowTimer != null){
            ThrowTimer.cancel();  //取消倒计时 防止内存泄露
            ThrowTimer = null;
        }
        super.onDestroy();
    }

}
