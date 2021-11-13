package com.example.garbagesortclient;

import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import com.example.garbagesortclient.database.Record;
import com.example.garbagesortclient.util.ChangeInfoReceiver;

import org.litepal.LitePal;

public class InfoModifyActivity extends BaseActivity {

    private SharedPreferences datapref;
    private ImageView Back;
    private TextView SignOut;
    private RelativeLayout NickNameItem;
    private RelativeLayout PhoneNumItem;
    private TextView NickNameModify;
    private TextView PhoneNumModify;
    private LocalBroadcastManager localBroadcastManager;
    private ChangeInfoReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_modify);
        //初始化控件
        datapref = getSharedPreferences("data",MODE_PRIVATE);
        Back = findViewById(R.id.back);
        SignOut = findViewById(R.id.sign_out);
        NickNameItem = findViewById(R.id.NICK_NAME_ITEM);
        PhoneNumItem = findViewById(R.id.PHONE_NUM_ITEM);
        NickNameModify = findViewById(R.id.nick_name_modify);
        PhoneNumModify = findViewById(R.id.phone_num_modify);
        //初始化控件
        AddClickListener();  //注册控件点击监听事件
        NickNameModify.setText(datapref.getString("user_nickname",""));
        PhoneNumModify.setText(datapref.getString("user_account",""));
        //注册广播接收器
        localBroadcastManager = LocalBroadcastManager.getInstance(this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("CHANGE_NAME_BROADCAST");
        receiver = new ChangeInfoReceiver(NickNameModify);
        localBroadcastManager.registerReceiver(receiver,intentFilter);


    }

    private void AddClickListener(){
        Back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        SignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LitePal.deleteAll(Record.class);   //清空投放记录数据库
                SharedPreferences.Editor editor = datapref.edit();
                editor.clear();    //清空所有用户数据
                editor.putBoolean("Login_Status",false);   //更改登陆状态为false
                editor.apply();
                setResult(RESULT_OK);   //登出成功向上一页面传递确认码
                finish();
            }
        });
        NickNameItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(InfoModifyActivity.this,NickNameActivity.class);
                intent.putExtra("user_nickname",datapref.getString("user_nickname",""));
                startActivity(intent);    //向信息更改页面传递当前的用户昵称
            }
        });


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        localBroadcastManager.unregisterReceiver(receiver);  //注销广播
    }

}
