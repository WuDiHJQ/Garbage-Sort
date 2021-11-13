package com.example.garbagesortclient;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class HelpActivity extends BaseActivity {

    private TextView TitleName;
    private ImageView Back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        //初始化控件
        TitleName = findViewById(R.id.title_name);
        Back = findViewById(R.id.back);
        //初始化控件
        TitleName.setText("帮助");
        AddClickListener();  //注册控件点击监听事件
    }

    private void AddClickListener(){
        Back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
        }
        });


    }


}
