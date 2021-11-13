package com.example.garbagesortmachine;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.TextView;

import com.example.garbagesortmachine.Constants.MyConstants;
import com.example.garbagesortmachine.util.Utility;

public class SuccessActivity extends BaseActivity {

    private TextView machine_id;
    private SharedPreferences datapref;
    private TextView TypeText;
    private TextView WeightText;
    private CountDownTimer FinishTimer;
    private TextView TimeShow;
    private int RemainTime = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_success);
        //获取上一个活动传入的数据
        int type = getIntent().getIntExtra("trash_type",0);
        int weight = getIntent().getIntExtra("trash_weight",0);
        //初始化控件
        machine_id = findViewById(R.id.machine_id);
        datapref = getSharedPreferences("data",MODE_PRIVATE);  //获取自定义的SharedPreferences
        machine_id.setText("机器编号：" + Utility.FormatId(datapref.getInt("machine_id",0)));
        TypeText = findViewById(R.id.trash_type);
        WeightText = findViewById(R.id.trash_weight);
        if (type == MyConstants.EXPRESS_TYPE)
            TypeText.setText("快递包装");
        else if (type == MyConstants.TAKEAWAY_TYPE)
            TypeText.setText("外卖餐盒");
        WeightText.setText(weight + "g");
        TimeShow = findViewById(R.id.time);
        FinishTimer = new CountDownTimer(10000,1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                TimeShow.setText(RemainTime + "");
                RemainTime--;
            }

            @Override
            public void onFinish() {
                finish();  //倒计时结束关闭该活动
            }
        }.start();   //启动倒计时任务

    }

    @Override
    protected void onDestroy() {
        if (FinishTimer != null){
            FinishTimer.cancel();  //取消倒计时 防止内存泄露
            FinishTimer = null;
        }
        super.onDestroy();
    }

}
