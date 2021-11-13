package com.example.garbagesortclient;

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
import android.widget.TextView;
import android.widget.Toast;

import com.example.garbagesortclient.util.Utility;


public class register_f2 extends Fragment {

    private String Account;  //用于保存传入的账号
    private EditText editText;
    private Button button;
    private TextView account_text;

    public register_f2(String Account) {
        this.Account = Account;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register_f2, container, false);
        editText = view.findViewById(R.id.password1_edit);
        button = view.findViewById(R.id.password1_button);
        account_text = view.findViewById(R.id.account1_text);
        account_text.setText(Account);
        editText.addTextChangedListener(new TextWatcher() {   //设置editText监听
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            public void afterTextChanged(Editable s) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() >= 8) {   //核对密码长度
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
                    String Password = editText.getText().toString();
                    if (!Utility.CheckPassword(Password)){   //如果密码组成不正确 则提示用户并清空输入框
                        Toast.makeText(getContext(),"密码由8~16位字母和数字组成!",Toast.LENGTH_SHORT).show();
                        editText.setText("");
                    }else {  //若密码组成正确
                        FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager()
                                .beginTransaction()    //加载第三个碎片
                                .setCustomAnimations(R.anim.in,R.anim.out);
                        fragmentTransaction.replace(R.id.container, new register_f3(Account, Password)).commit();
                    }
                }else{   //若无网络 则提醒用户
                    Toast.makeText(getContext(),"当前网络不可用，请检查网络连接！",Toast.LENGTH_SHORT).show();
                }
            }
        });
        return view;
    }

}
