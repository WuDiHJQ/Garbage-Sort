package com.example.garbagesortclient.util;
        import android.content.Context;
        import android.net.ConnectivityManager;
        import android.net.NetworkInfo;
        import java.io.IOException;
        import okhttp3.FormBody;
        import okhttp3.OkHttpClient;
        import okhttp3.Request;
        import okhttp3.Response;

public class Utility {   //工具类

    public static boolean JudgeNetWork(Context context){
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
    public static void EnqueueHttpRequest(String address, FormBody formBody, okhttp3.Callback callback){
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(address)
                .post(formBody)
                .build();
        client.newCall(request).enqueue(callback);
    }

    public static boolean CheckPassword(String password){
        boolean Letter = false;
        boolean Digit = false;
        for (int i = 0; i < password.length();i++){
            char c = password.charAt(i);
            if (!IsAsciiAlpha(c) && !Character.isDigit(c))   //既不是字母也不是数字
                return false;
            else if (Letter == false && IsAsciiAlpha(c))
                Letter = true;
            else if (Digit == false && Character.isDigit(c))
                Digit = true;
        }
        return (Letter && Digit);
    }

    private static boolean IsAsciiAlpha(char c){   //因为Character.isLetter()方法会把汉字也看作字母
        return ((c >= 65 && c <= 90) || (c >= 97 && c <= 122));  //自定义IsAsciiAlpha()方法
    }

    public static boolean CheckQRcode(String QRstring){
        int len = QRstring.length();
        if (len != 33 || !QRstring.substring(0,len - 6).equals("zut.garbagesort.machine.id."))
            return false;
        String id = QRstring.substring(len - 6);
        for (int i = 0; i < id.length();i++){
            if (!Character.isDigit(id.charAt(i)))
                return false;
        }
        return true;
    }


}
