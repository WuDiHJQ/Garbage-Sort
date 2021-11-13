package com.example.garbagesortclient;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.garbagesortclient.Constants.MyConstants;
import com.example.garbagesortclient.Constants.URL;
import com.example.garbagesortclient.database.Record;
import com.example.garbagesortclient.util.ChangeInfoReceiver;
import com.example.garbagesortclient.util.RecordAdapter;
import com.example.garbagesortclient.util.Utility;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import org.litepal.LitePal;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Response;

public class MainActivity extends BaseActivity {

    private final String USER = "0";
    private SharedPreferences datapref;
    private TextView menuScore;
    private TextView menuRecord;
    private TextView menuHelp;
    private ImageView drawerButton;
    private DrawerLayout drawerLayout;
    private CircleImageView headericon;
    private TextView NickName;
    private TextView JIFEN;
    private TextView userScore;
    private LocalBroadcastManager localBroadcastManager;
    private ChangeInfoReceiver receiver;
    private CardView ScanQRcode;
    private ProgressDialog progressDialog;
    private TextView TimeHistory;
    private TextView WeightHistory;
    private TextView NowScore;
    private LinearLayout History;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //初始化控件
        menuScore = findViewById(R.id.menu_score);
        menuRecord = findViewById(R.id.menu_record);
        menuHelp = findViewById(R.id.menu_help);
        drawerButton = findViewById(R.id.drawer_icon);
        drawerLayout = findViewById(R.id.drawer_layout);
        headericon = findViewById(R.id.header_icon);
        NickName = findViewById(R.id.nick_name);
        JIFEN = findViewById(R.id.JI_FEN);
        userScore = findViewById(R.id.user_score);
        ScanQRcode = findViewById(R.id.Scan_QR);
        History = findViewById(R.id.history_info);
        TimeHistory = findViewById(R.id.history_time);
        WeightHistory = findViewById(R.id.history_weight);
        NowScore = findViewById(R.id.my_socre);
        //初始化控件
        AddClickListener();  //注册控件点击监听事件
        LitePal.getDatabase();   //启用数据库
        datapref = getSharedPreferences("data",MODE_PRIVATE);  //获取自定义的SharedPreferences
        //注册广播接收器
        localBroadcastManager = LocalBroadcastManager.getInstance(this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("CHANGE_NAME_BROADCAST");
        receiver = new ChangeInfoReceiver(NickName);
        localBroadcastManager.registerReceiver(receiver,intentFilter);
        //注册广播接收器
        progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setMessage("加载中...");
        progressDialog.setCancelable(false);
        if (!Utility.JudgeNetWork(MainActivity.this)){   //若无网络连接
            SharedPreferences.Editor editor = datapref.edit();
            editor.clear();    //清空所有用户数据
            editor.putBoolean("Login_Status",false);   //更改登陆状态为false
            editor.apply();
            AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);  //创建警告窗口
            dialog.setTitle("温馨提示");
            dialog.setMessage("当前网络不可用，请检查网络连接！");
            dialog.setCancelable(false);
            dialog.setPositiveButton("我知道了", new DialogInterface.OnClickListener(){
                @Override
                public void onClick(DialogInterface dialog, int which) { }
            });
            dialog.show();  //显示警告窗口
        }
        if (datapref.getBoolean("Login_Status", false)) {  //判断登陆状态 默认返回false
            NickName.setText(datapref.getString("user_nickname","新用户"));     //获取保存的昵称
            SetHistoryInfo();
        }
        else{    //若未登录则更改drawer窗口数据
            NickName.setText("登陆去！");
            JIFEN.setVisibility(View.INVISIBLE);
            userScore.setVisibility(View.INVISIBLE);
            History.setVisibility(View.GONE);
        }

        //主页完毕！！！！！！！！！！！！！！！！！！！！！！！！！！！



    }

    private void SetHistoryInfo(){    //设置用户历史信息
        progressDialog.show();
        History.setVisibility(View.VISIBLE);
        FormBody formBody = new FormBody.Builder()    //生成表单数据
                .add("user_id",datapref.getInt("user_id",0)+"")
                .build();
        Utility.EnqueueHttpRequest(URL.QUERYRECORD, formBody, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.dismiss();  //关闭进度条
                        Toast.makeText(MainActivity.this, "数据获取异常", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                //解析json数据
                try{
                    final List<Record> records;
                    String result = response.body().string();   //获取服务器返回的json数据
                    JSONObject jsonObject = new JSONObject(result);
                    int num = jsonObject.getInt("count");
                    final int score = jsonObject.getInt("user_score");
                    SharedPreferences.Editor editor = datapref.edit();
                    if (num > 0) {    //若投放记录大于0条则解析data数据
                        String data = jsonObject.getString("data");
                        Gson gson = new Gson();  //利用GSON解析data数据
                        if (num == 1){   //如果记录中只有一条 无法用TypeToken解析数组
                            Record r = gson.fromJson(data,Record.class);
                            records = new ArrayList<>();  //则需要单独解析出来存入一个数组
                            records.add(r);
                        }
                        else   //如果2条以上就可以直接解析
                            records = gson.fromJson(data,new TypeToken<List<Record>>(){}.getType());  //将json数组转化为list类型
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //计算并设置统计值
                                int sumtime = records.size();
                                int sumweight = 0;
                                for (Record r : records)
                                    sumweight += r.getTrash_weight();
                                TimeHistory.setText(sumtime + " ");   //加空格防止斜体被挡住 也可用padding
                                DecimalFormat df = new DecimalFormat("0.00");  //保留两位小数显示
                                WeightHistory.setText(df.format(sumweight/1000.0) + " ");  //转化为千克显示
                                NowScore.setText(score+"");    //为主页积分赋值
                                userScore.setText(score+"");    //为滑动窗口积分赋值
                                progressDialog.dismiss();  //数据显示完成关闭进度条
                            }
                        });
                    }
                    else{    //若记录数为0或其他情况均关闭进度条
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() { progressDialog.dismiss(); }
                        });
                    }
                }catch (Exception e){
                    e.printStackTrace();   //生吞异常并关闭进度条
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressDialog.dismiss();
                            Toast.makeText(MainActivity.this,"数据获取异常",Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });

    }

    private void AddClickListener(){
        menuScore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent;
                if (datapref.getBoolean("Login_Status", false))
                    intent = new Intent(MainActivity.this,ScoreActivity.class);
                else
                    intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivityForResult(intent,MyConstants.LOGIN_BACK);
            }
        });
        menuRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent;
                if (datapref.getBoolean("Login_Status", false))
                    intent = new Intent(MainActivity.this,RecordActivity.class);
                else
                    intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivityForResult(intent,MyConstants.LOGIN_BACK);
            }
        });
        menuHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent;
                if (datapref.getBoolean("Login_Status", false))
                    intent = new Intent(MainActivity.this,HelpActivity.class);
                else
                    intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivityForResult(intent,MyConstants.LOGIN_BACK);
            }
        });
        ScanQRcode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (datapref.getBoolean("Login_Status", false)){    //若未登录则跳转
                    //Zxing简化的二维码扫描的Intent
                    IntentIntegrator integrator = new IntentIntegrator(MainActivity.this);
                    integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);  //只允许扫描二维码Quick Response Code
                    integrator.setPrompt("");  //设置提示字符为空
                    integrator.setCameraId(0); //设置摄像头为前置摄像头
                    integrator.setBeepEnabled(false);  //关闭扫描成功的「哔哔」声
                    integrator.setCaptureActivity(ScanActivity.class); //把扫描的Activity改为自定义扫描活动
                    integrator.initiateScan();  //启动扫码Activity
                }
                else{
                    AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);  //创建警告窗口
                    dialog.setMessage("请先登录！");
                    dialog.setCancelable(false);
                    dialog.setPositiveButton("我知道了", new DialogInterface.OnClickListener(){public void onClick(DialogInterface dialog, int which) {}});
                    dialog.show();  //显示警告窗口
                }
            }
        });
        drawerButton.setOnClickListener(new View.OnClickListener() {  //开启滑动菜单
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);  //与布局文件中一致
            }
        });
        headericon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent;
                if (datapref.getBoolean("Login_Status",false)) {//如果已经登陆 则切换到信息修改页面
                    intent = new Intent(MainActivity.this, InfoModifyActivity.class);
                    startActivityForResult(intent,MyConstants.SIGN_OUT);
                } else{
                    intent = new Intent(MainActivity.this, LoginActivity.class);
                    startActivityForResult(intent,MyConstants.LOGIN_BACK);
                }
            }
        });
        NickName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent;
                if (datapref.getBoolean("Login_Status",false)){ //如果已经登陆 则切换到信息修改页面
                    intent = new Intent(MainActivity.this, InfoModifyActivity.class);
                    startActivityForResult(intent,MyConstants.SIGN_OUT);
                } else{
                    intent = new Intent(MainActivity.this, LoginActivity.class);
                    startActivityForResult(intent,MyConstants.LOGIN_BACK);
                }
            }
        });


    }

    @Override  //startActivityForResult启动的活动销毁后会回调上一个活动的此方法
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);  //处理扫描二维码返回的数据
        if(result != null && result.getContents() != null) {
            String machine_id = result.getContents();
            if (Utility.CheckQRcode(machine_id)){   //对扫码得到的字符串进行检验 是否标准
                machine_id = machine_id.substring(machine_id.length() - 6);   //解析扫码获得的字符串
                FormBody formBody = new FormBody.Builder()    //生成表单数据
                        .add("From", USER)   //设置请求来源为用户
                        .add("user_id", datapref.getInt("user_id",0)+"")
                        .add("user_nickname", datapref.getString("user_nickname",""))
                        .add("machine_id", machine_id)
                        .build();
                Utility.EnqueueHttpRequest(URL.SCANQRCODE, formBody, new Callback() {   //告知服务器扫码成功并传输数据
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this,"当前网络不可用，请检查网络连接！",Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                        try{
                            String result = response.body().string();   //获取服务器返回的json数据
                            JSONObject jsonObject = new JSONObject(result);
                            if (jsonObject.getBoolean("status") == true) {  //若递交扫描数据成功
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(MainActivity.this, "扫描成功！", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }catch (Exception e){   //若数据处理过程中出现异常
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() { Toast.makeText(MainActivity.this,"返回异常",Toast.LENGTH_SHORT).show(); }
                            });
                        }
                    }
                });
            }
            else{
                Toast.makeText(MainActivity.this,"请扫描机器上的二维码！",Toast.LENGTH_SHORT).show();
            }
        }
        if (resultCode == RESULT_OK)
            switch (requestCode){
                case MyConstants.LOGIN_BACK:{   //验证是否由登录界面成功登录返回
                        JIFEN.setVisibility(View.VISIBLE);
                        userScore.setVisibility(View.VISIBLE);
                        NickName.setText(datapref.getString("user_nickname", "新用户"));
                        userScore.setText(datapref.getInt("user_score", 0)+"");
                        SetHistoryInfo();
                        break;
                }
                case MyConstants.SIGN_OUT:{
                    NickName.setText("登陆去！");
                    JIFEN.setVisibility(View.INVISIBLE);
                    userScore.setVisibility(View.INVISIBLE);
                    History.setVisibility(View.GONE);
                    SharedPreferences.Editor editor = datapref.edit();
                    editor.clear();    //清空所有用户数据
                    editor.putBoolean("Login_Status",false);   //更改登陆状态为false
                    editor.apply();
                    break;
                }
                default:
                    break;
            }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        localBroadcastManager.unregisterReceiver(receiver);  //注销广播
    }

}
