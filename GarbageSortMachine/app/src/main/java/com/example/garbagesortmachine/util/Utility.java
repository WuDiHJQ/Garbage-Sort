package com.example.garbagesortmachine.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Base64;
import android.widget.Toast;

import com.example.garbagesortmachine.Constants.MyConstants;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Random;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class Utility {

    public static boolean JudgeNetWork(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());  //返回网络状态
    }

    //同步操作
    public static Response ExecuteHttpRequest(String address, FormBody formBody) throws IOException {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(address)
                .post(formBody)
                .build();
        Response response = client.newCall(request).execute();
        return response;
    }

    //异步操作
    public static void EnqueueHttpRequest(String address, FormBody formBody, okhttp3.Callback callback) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(address)
                .post(formBody)
                .build();
        client.newCall(request).enqueue(callback);
    }

    public static boolean isNumeric(String str) {
        for (int i = 0; i < str.length(); i++) {
            System.out.println(str.charAt(i));
            if (!Character.isDigit(str.charAt(i)))
                return false;
        }
        return true;
    }

    public static String EncodeBitmap(Bitmap bitmap) {   //将图片编码为字符串
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, output);  //对图片进行压缩
        String imageBase64 = new String(Base64.encode(output.toByteArray(), Base64.DEFAULT));  //转化为字符串
        return imageBase64;
    }

    public static Bitmap DecodeBitmap(String pic) {   //将字符串还原为图片
        byte[] bytes = Base64.decode(pic,Base64.DEFAULT);  //将字符串解码为字节数组
        Bitmap bitmap= BitmapFactory.decodeByteArray(bytes,0,bytes.length);  //还原图片
        return bitmap;
    }

    public static int WeighTrash() {   //对当前投放的垃圾进行称重
        int Weight = (1 + new Random().nextInt(50)) * 10;    //随机返回10~500克的垃圾重量
        return Weight;
    }

    public static int IdentifyTrash() {   //当前投放的垃圾进行识别
        int Type = new Random().nextInt(2);   //根据返回的0或1进行结果返回
        if (Type == 0)
            return MyConstants.EXPRESS_TYPE;
        else
            return MyConstants.TAKEAWAY_TYPE;
    }

    public static int WeighExpressBin() {   //对快递箱进行称重
        int Weight = new Random().nextInt(21) * 1000;    //随机返回0~20kg的垃圾桶重量
        return Weight;
    }

    public static int WeighTakeAwayBin() {   //对外卖箱进行称重
        int Weight = new Random().nextInt(21) * 1000;   //随机返回0~20kg的垃圾桶重量
        return Weight;
    }

    public static void OpenGate(Context context) {   //模拟打开箱门
        Toast.makeText(context,"垃圾箱门已打开",Toast.LENGTH_SHORT).show();
    }

    public static void CloseGate(Context context) {   //模拟关闭箱门
        Toast.makeText(context,"垃圾箱门已关闭",Toast.LENGTH_SHORT).show();
    }

    public static String FormatId(int machine_id){
        DecimalFormat df=new DecimalFormat("000000");
        return df.format(machine_id);
    }

}