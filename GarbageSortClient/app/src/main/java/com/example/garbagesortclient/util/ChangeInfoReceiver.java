package com.example.garbagesortclient.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.TextView;

public class ChangeInfoReceiver extends BroadcastReceiver {

    private TextView ChangeText;

    public ChangeInfoReceiver(TextView textView){
        this.ChangeText = textView;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        ChangeText.setText(intent.getStringExtra("user_nickname"));
    }

}