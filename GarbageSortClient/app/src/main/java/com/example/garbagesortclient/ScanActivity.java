package com.example.garbagesortclient;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.journeyapps.barcodescanner.CaptureManager;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;

import de.hdodenhof.circleimageview.CircleImageView;

//重写条形码扫描活动用于添加自定义的属性
public class ScanActivity extends BaseActivity {

    private CaptureManager captureManager;
    private DecoratedBarcodeView DBV;   //条形码扫描视图
    private CircleImageView switchLight;
    private ImageView Back;
    private boolean LightOn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //隐藏状态栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_scan);
        //初始化控件
        switchLight = findViewById(R.id.SwitchLight);
        DBV = findViewById(R.id.DecoratedBarcodeView);
        Back = findViewById(R.id.back);
        LightOn = false;  //初始化手电筒为关闭状态
        //初始化控件
        AddClickListener();    //注册扫码界面监听事件
        //初始化捕获
        captureManager = new CaptureManager(this,DBV);
        captureManager.initializeFromIntent(getIntent(),savedInstanceState);
        captureManager.decode();

    }

    private void AddClickListener(){
        //设置系统手电筒监听
        DBV.setTorchListener(new DecoratedBarcodeView.TorchListener() {
            @Override
            public void onTorchOn() {
                LightOn = true;
                switchLight.setImageResource(R.drawable.light_on);
            }
            @Override
            public void onTorchOff() {
                LightOn = false;
                switchLight.setImageResource(R.drawable.light_off);
            }
        });
        //设置手电筒按钮监听
        switchLight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(LightOn)
                    DBV.setTorchOff();
                else
                    DBV.setTorchOn();
            }
        });
        Back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        captureManager.onSaveInstanceState(outState);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return DBV.onKeyDown(keyCode, event) || super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onPause() {
        super.onPause();
        captureManager.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        captureManager.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        captureManager.onDestroy();
    }

}
