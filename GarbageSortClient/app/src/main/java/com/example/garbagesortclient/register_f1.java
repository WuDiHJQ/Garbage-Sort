package com.example.garbagesortclient;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.garbagesortclient.Constants.MyConstants;
import com.example.garbagesortclient.Constants.URL;
import com.example.garbagesortclient.util.Utility;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Response;

public class register_f1 extends Fragment {

    private EditText editText;
    private Button button;
    private ProgressDialog progressDialog;
    private String Account;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register_f1, container, false);
        editText = view.findViewById(R.id.account_edit);
        button = view.findViewById(R.id.account_button);
        progressDialog = new ProgressDialog(getContext());   //初始化一个加载框
        progressDialog.setMessage("加载中...");
        progressDialog.setCancelable(false);
        editText.addTextChangedListener(new TextWatcher() {   //设置editText监听
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            public void afterTextChanged(Editable s) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 11) {   //验证手机号长度
                    button.setEnabled(true);
                    button.setBackgroundColor(Color.parseColor("#FF0081EE"));
                }else{
                    button.setEnabled(false);
                    button.setBackgroundColor(Color.parseColor("#FFD6D7D7"));
                }
            }
        });
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Utility.JudgeNetWork(getContext())) {
                    Account = editText.getText().toString();
                    FormBody formBody = new FormBody.Builder()    //生成表单数据
                            .add("step", MyConstants.STEP_1 + "")
                            .add("account", Account)
                            .build();
                    progressDialog.show();
                    Utility.EnqueueHttpRequest(URL.REGISTER, formBody, new Callback() {
                        @Override
                        public void onFailure(@NotNull Call call, @NotNull IOException e) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    progressDialog.dismiss();  //关闭进度条
                                    Toast.makeText(getContext(), "注册异常", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }

                        @Override
                        public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                            //解析json数据
                            try {
                                String result = response.body().string();   //获取服务器返回的json数据
                                JSONObject jsonObject = new JSONObject(result);
                                if (jsonObject.getBoolean("status")){  //若返回为true 则该账号未被注册
                                    getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            progressDialog.dismiss();
                                            FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager()
                                                    .beginTransaction()    //加载第二个碎片
                                                    .setCustomAnimations(R.anim.in,R.anim.out);
                                            fragmentTransaction.replace(R.id.container, new register_f2(Account)).commit();
                                        }
                                    });
                                }else{  //若为false 则代表该账号已被注册
                                    getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            progressDialog.dismiss();
                                            Toast.makeText(getContext(),"该手机号已被注册",Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            }catch (Exception e){   //若出现异常 则关闭进度条并提示用户 防止程序不能操作
                                e.printStackTrace();
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        progressDialog.dismiss();
                                        Toast.makeText(getContext(),"注册异常",Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }
                    });
                }else{   //若无网络 则提醒用户
                    Toast.makeText(getContext(),"当前网络不可用，请检查网络连接！",Toast.LENGTH_SHORT).show();
                }
            }
        });
        return view;
    }

}
