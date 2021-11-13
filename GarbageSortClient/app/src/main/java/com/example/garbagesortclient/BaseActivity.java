package com.example.garbagesortclient;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.garbagesortclient.util.Utility;

public class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= 21){
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        if (!Utility.JudgeNetWork(BaseActivity.this))  //检测网络状态
            Toast.makeText(BaseActivity.this,"当前网络不可用，请检查网络连接！",Toast.LENGTH_SHORT).show();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);//强制应用竖屏
    }

    protected void onRestart() {
        super.onRestart();
        if (!Utility.JudgeNetWork(BaseActivity.this))  //检测网络状态
            Toast.makeText(BaseActivity.this,"当前网络不可用，请检查网络连接！",Toast.LENGTH_SHORT).show();
    }

    //设置字体大小不随系统变化
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        if (newConfig.fontScale != 1)  //若缩放不为默认值
            getResources();
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public Resources getResources() {
        Resources res = super.getResources();
        if (res.getConfiguration().fontScale != 1) {  //若缩放不为默认值
            Configuration newConfig = new Configuration();
            newConfig.setToDefaults();   //设置为默认值
            res.updateConfiguration(newConfig, res.getDisplayMetrics());
        }
        return res;
    }


}