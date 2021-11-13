package com.example.garbagesortmachine;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.garbagesortmachine.Constants.URL;
import com.example.garbagesortmachine.util.Utility;

import org.jetbrains.annotations.NotNull;
import java.io.IOException;
import java.io.InputStream;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Response;

public class InitialActivity extends BaseActivity {

    private SharedPreferences datapref;
    private FormBody formBody;
    private ProgressDialog progressDialog;
    private EditText editText;
    private Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initial);
        //初始化控件
        editText = findViewById(R.id.Initial_EditText);
        button = findViewById(R.id.Initial_Button);
        datapref = getSharedPreferences("data",MODE_PRIVATE);  //获取自定义的SharedPreferences
        progressDialog = new ProgressDialog(InitialActivity.this);
        progressDialog.setMessage("Initializing...");
        progressDialog.setCancelable(false);
        //初始化控件
        AddClickListener();  //注册控件点击监听事件
    }

    private void AddClickListener(){
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Utility.JudgeNetWork(InitialActivity.this)){  //检查网络状态
                    String text = editText.getText().toString();
                    if (Utility.isNumeric(text) && text.length() != 0){  //处理非数字输入及空输入
                        final int id = Integer.parseInt(editText.getText().toString());
                        formBody = new FormBody.Builder()    //生成表单数据
                                .add("machine_id", text)
                                .build();
                        progressDialog.show();
                        Utility.EnqueueHttpRequest(URL.GETQRCODE, formBody, new Callback() {
                            @Override
                            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        progressDialog.dismiss();
                                        Toast.makeText(InitialActivity.this,"Initialization Exception", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }

                            @Override
                            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                                InputStream input = response.body().byteStream(); //获取服务器传输的流
                                final Bitmap bitmap = BitmapFactory.decodeStream(input);   //转化为图片
                                if (bitmap != null){    //若bitmap不为null则代表二维码图片存在
                                    SharedPreferences.Editor editor = datapref.edit();   //存入初始化数据
                                    editor.putBoolean("Initial",true);
                                    editor.putInt("machine_id",id);
                                    editor.putString("QRcode",Utility.EncodeBitmap(bitmap));
                                    editor.apply();
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            progressDialog.dismiss();   //关闭进度条
                                            finish();
                                            Intent intent = new Intent(InitialActivity.this, MainActivity.class);
                                            startActivity(intent);  //重新启动主活动
                                        }
                                    });
                                }
                                else{  //否则抛出异常
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            progressDialog.dismiss();
                                            Toast.makeText(InitialActivity.this,"Input Exception", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            }
                        });
                    }else{
                        Toast.makeText(InitialActivity.this,"Please enter six digits", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(InitialActivity.this,"NETWORK ERROR",Toast.LENGTH_SHORT).show();
                }
            }
        });


    }

}