package com.example.garbagesortmachine;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.garbagesortmachine.util.Utility;

import java.text.DecimalFormat;

import de.hdodenhof.circleimageview.CircleImageView;


public class MainActivity extends BaseActivity {

    private SharedPreferences datapref;
    private TextView machine_id;
    private CircleImageView ToThrow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        datapref = getSharedPreferences("data",MODE_PRIVATE);  //获取自定义的SharedPreferences
        if (!datapref.getBoolean("Initial",false)){   //若机器未进行初始化
            finish();
            Intent intent = new Intent(MainActivity.this, InitialActivity.class);
            startActivity(intent);    //启动初始化活动
        }else{   //若机器已经初始化完成
            //初始化控件
            machine_id = findViewById(R.id.machine_id);
            ToThrow = findViewById(R.id.ToThrow);
            //初始化控件
            AddClickListener();  //注册控件点击监听事件

            machine_id.setText("机器编号：" + Utility.FormatId(datapref.getInt("machine_id",0)));
        }

    }

    private void AddClickListener(){
        ToThrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Utility.JudgeNetWork(MainActivity.this)){   //检测网络状态
                    Intent intent = new Intent(MainActivity.this, ScanActivity.class);
                    startActivity(intent);
                }else{    //若无网络则不允许启动投放
                    Toast.makeText(MainActivity.this,"NETWORK ERROR",Toast.LENGTH_SHORT).show();
                }
            }
        });



    }


}
