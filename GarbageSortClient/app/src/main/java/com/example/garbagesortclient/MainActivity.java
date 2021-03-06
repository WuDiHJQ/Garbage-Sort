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
        //???????????????
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
        //???????????????
        AddClickListener();  //??????????????????????????????
        LitePal.getDatabase();   //???????????????
        datapref = getSharedPreferences("data",MODE_PRIVATE);  //??????????????????SharedPreferences
        //?????????????????????
        localBroadcastManager = LocalBroadcastManager.getInstance(this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("CHANGE_NAME_BROADCAST");
        receiver = new ChangeInfoReceiver(NickName);
        localBroadcastManager.registerReceiver(receiver,intentFilter);
        //?????????????????????
        progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setMessage("?????????...");
        progressDialog.setCancelable(false);
        if (!Utility.JudgeNetWork(MainActivity.this)){   //??????????????????
            SharedPreferences.Editor editor = datapref.edit();
            editor.clear();    //????????????????????????
            editor.putBoolean("Login_Status",false);   //?????????????????????false
            editor.apply();
            AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);  //??????????????????
            dialog.setTitle("????????????");
            dialog.setMessage("????????????????????????????????????????????????");
            dialog.setCancelable(false);
            dialog.setPositiveButton("????????????", new DialogInterface.OnClickListener(){
                @Override
                public void onClick(DialogInterface dialog, int which) { }
            });
            dialog.show();  //??????????????????
        }
        if (datapref.getBoolean("Login_Status", false)) {  //?????????????????? ????????????false
            NickName.setText(datapref.getString("user_nickname","?????????"));     //?????????????????????
            SetHistoryInfo();
        }
        else{    //?????????????????????drawer????????????
            NickName.setText("????????????");
            JIFEN.setVisibility(View.INVISIBLE);
            userScore.setVisibility(View.INVISIBLE);
            History.setVisibility(View.GONE);
        }

        //?????????????????????????????????????????????????????????????????????????????????????????????



    }

    private void SetHistoryInfo(){    //????????????????????????
        progressDialog.show();
        History.setVisibility(View.VISIBLE);
        FormBody formBody = new FormBody.Builder()    //??????????????????
                .add("user_id",datapref.getInt("user_id",0)+"")
                .build();
        Utility.EnqueueHttpRequest(URL.QUERYRECORD, formBody, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.dismiss();  //???????????????
                        Toast.makeText(MainActivity.this, "??????????????????", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                //??????json??????
                try{
                    final List<Record> records;
                    String result = response.body().string();   //????????????????????????json??????
                    JSONObject jsonObject = new JSONObject(result);
                    int num = jsonObject.getInt("count");
                    final int score = jsonObject.getInt("user_score");
                    SharedPreferences.Editor editor = datapref.edit();
                    if (num > 0) {    //?????????????????????0????????????data??????
                        String data = jsonObject.getString("data");
                        Gson gson = new Gson();  //??????GSON??????data??????
                        if (num == 1){   //??????????????????????????? ?????????TypeToken????????????
                            Record r = gson.fromJson(data,Record.class);
                            records = new ArrayList<>();  //?????????????????????????????????????????????
                            records.add(r);
                        }
                        else   //??????2??????????????????????????????
                            records = gson.fromJson(data,new TypeToken<List<Record>>(){}.getType());  //???json???????????????list??????
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //????????????????????????
                                int sumtime = records.size();
                                int sumweight = 0;
                                for (Record r : records)
                                    sumweight += r.getTrash_weight();
                                TimeHistory.setText(sumtime + " ");   //?????????????????????????????? ?????????padding
                                DecimalFormat df = new DecimalFormat("0.00");  //????????????????????????
                                WeightHistory.setText(df.format(sumweight/1000.0) + " ");  //?????????????????????
                                NowScore.setText(score+"");    //?????????????????????
                                userScore.setText(score+"");    //???????????????????????????
                                progressDialog.dismiss();  //?????????????????????????????????
                            }
                        });
                    }
                    else{    //???????????????0?????????????????????????????????
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() { progressDialog.dismiss(); }
                        });
                    }
                }catch (Exception e){
                    e.printStackTrace();   //??????????????????????????????
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressDialog.dismiss();
                            Toast.makeText(MainActivity.this,"??????????????????",Toast.LENGTH_SHORT).show();
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
                if (datapref.getBoolean("Login_Status", false)){    //?????????????????????
                    //Zxing???????????????????????????Intent
                    IntentIntegrator integrator = new IntentIntegrator(MainActivity.this);
                    integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);  //????????????????????????Quick Response Code
                    integrator.setPrompt("");  //????????????????????????
                    integrator.setCameraId(0); //?????????????????????????????????
                    integrator.setBeepEnabled(false);  //????????????????????????????????????
                    integrator.setCaptureActivity(ScanActivity.class); //????????????Activity???????????????????????????
                    integrator.initiateScan();  //????????????Activity
                }
                else{
                    AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);  //??????????????????
                    dialog.setMessage("???????????????");
                    dialog.setCancelable(false);
                    dialog.setPositiveButton("????????????", new DialogInterface.OnClickListener(){public void onClick(DialogInterface dialog, int which) {}});
                    dialog.show();  //??????????????????
                }
            }
        });
        drawerButton.setOnClickListener(new View.OnClickListener() {  //??????????????????
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);  //????????????????????????
            }
        });
        headericon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent;
                if (datapref.getBoolean("Login_Status",false)) {//?????????????????? ??????????????????????????????
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
                if (datapref.getBoolean("Login_Status",false)){ //?????????????????? ??????????????????????????????
                    intent = new Intent(MainActivity.this, InfoModifyActivity.class);
                    startActivityForResult(intent,MyConstants.SIGN_OUT);
                } else{
                    intent = new Intent(MainActivity.this, LoginActivity.class);
                    startActivityForResult(intent,MyConstants.LOGIN_BACK);
                }
            }
        });


    }

    @Override  //startActivityForResult????????????????????????????????????????????????????????????
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);  //????????????????????????????????????
        if(result != null && result.getContents() != null) {
            String machine_id = result.getContents();
            if (Utility.CheckQRcode(machine_id)){   //??????????????????????????????????????? ????????????
                machine_id = machine_id.substring(machine_id.length() - 6);   //??????????????????????????????
                FormBody formBody = new FormBody.Builder()    //??????????????????
                        .add("From", USER)   //???????????????????????????
                        .add("user_id", datapref.getInt("user_id",0)+"")
                        .add("user_nickname", datapref.getString("user_nickname",""))
                        .add("machine_id", machine_id)
                        .build();
                Utility.EnqueueHttpRequest(URL.SCANQRCODE, formBody, new Callback() {   //??????????????????????????????????????????
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this,"????????????????????????????????????????????????",Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                        try{
                            String result = response.body().string();   //????????????????????????json??????
                            JSONObject jsonObject = new JSONObject(result);
                            if (jsonObject.getBoolean("status") == true) {  //???????????????????????????
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(MainActivity.this, "???????????????", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }catch (Exception e){   //????????????????????????????????????
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() { Toast.makeText(MainActivity.this,"????????????",Toast.LENGTH_SHORT).show(); }
                            });
                        }
                    }
                });
            }
            else{
                Toast.makeText(MainActivity.this,"?????????????????????????????????",Toast.LENGTH_SHORT).show();
            }
        }
        if (resultCode == RESULT_OK)
            switch (requestCode){
                case MyConstants.LOGIN_BACK:{   //?????????????????????????????????????????????
                        JIFEN.setVisibility(View.VISIBLE);
                        userScore.setVisibility(View.VISIBLE);
                        NickName.setText(datapref.getString("user_nickname", "?????????"));
                        userScore.setText(datapref.getInt("user_score", 0)+"");
                        SetHistoryInfo();
                        break;
                }
                case MyConstants.SIGN_OUT:{
                    NickName.setText("????????????");
                    JIFEN.setVisibility(View.INVISIBLE);
                    userScore.setVisibility(View.INVISIBLE);
                    History.setVisibility(View.GONE);
                    SharedPreferences.Editor editor = datapref.edit();
                    editor.clear();    //????????????????????????
                    editor.putBoolean("Login_Status",false);   //?????????????????????false
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
        localBroadcastManager.unregisterReceiver(receiver);  //????????????
    }

}
