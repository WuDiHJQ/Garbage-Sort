package com.example.garbagesortmachine;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.garbagesortmachine.Constants.URL;
import com.example.garbagesortmachine.util.Utility;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.io.IOException;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Response;

public class ScanActivity extends BaseActivity {

    private final String MACHINE = "1";
    private SharedPreferences datapref;
    private FormBody formBody;
    private ImageView QRcode;
    private CountDownTimer ScanTimer;
    private TextView TimeShow;
    private TextView Back;
    private TextView machine_id;
    private boolean SendHttpFlag = false;  //每两秒发送一次请求 所以设立一个标记
    private int RemainTime = 30;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);
        //初始化控件
        datapref = getSharedPreferences("data",MODE_PRIVATE);  //获取自定义的SharedPreferences
        QRcode = findViewById(R.id.QRcode);
        Bitmap bitmap = Utility.DecodeBitmap(datapref.getString("QRcode",""));  //获取图片
        QRcode.setImageBitmap(bitmap);
        TimeShow = findViewById(R.id.time);
        machine_id = findViewById(R.id.machine_id);
        machine_id.setText("机器编号：" + Utility.FormatId(datapref.getInt("machine_id",0)));
        Back = findViewById(R.id.back);
        formBody = new FormBody.Builder()    //生成表单数据
                .add("From", MACHINE)   //设置请求来源为机器
                .add("machine_id", datapref.getInt("machine_id",0)+"")
                .build();
        //初始化控件
        AddClickListener();  //注册控件点击监听事件


        //设置一个定时器 对服务器进行轮询 查看二维码是否被扫描
        ScanTimer = new CountDownTimer(30000,1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                TimeShow.setText(RemainTime + "");
                RemainTime--;
                if (SendHttpFlag){
                    Utility.EnqueueHttpRequest(URL.SCANQRCODE, formBody, new Callback() {  //向服务器发送轮询
                        @Override
                        public void onFailure(@NotNull Call call, @NotNull IOException e) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() { Toast.makeText(ScanActivity.this,"NetWork Exception", Toast.LENGTH_SHORT).show(); }
                            });
                        }

                        @Override
                        public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                            try{
                                String result = response.body().string();   //获取服务器返回的json数据
                                JSONObject jsonObject = new JSONObject(result);
                                if (jsonObject.getBoolean("status") == true) {  //若二维码已被扫描
                                    Intent intent = new Intent(ScanActivity.this,ThrowActivity.class);
                                    intent.putExtra("user_id",jsonObject.getInt("user_id"));    //传输用户id和昵称 并启动下一个活动
                                    intent.putExtra("user_nickname",jsonObject.getString("user_nickname"));
                                    startActivity(intent);
                                    finish();   //销毁当前活动及倒计时
                                }   //未扫描则继续轮询
                            }catch (Exception e){   //若数据处理过程中出现异常
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() { Toast.makeText(ScanActivity.this,"Process Exception",Toast.LENGTH_SHORT).show(); }
                                });
                            }
                        }
                    });
                    SendHttpFlag = false;
                }else{
                    SendHttpFlag = true;
                }
            }

            @Override
            public void onFinish() {
                finish();  //当倒计时结束 还未扫码 则关闭该活动
            }
        }.start();   //启动倒计时任务


    }

    private void AddClickListener(){
        Back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }


    @Override
    protected void onDestroy() {
        if (ScanTimer != null){
            ScanTimer.cancel();  //取消倒计时 防止内存泄露
            ScanTimer = null;
        }
        super.onDestroy();
    }

}