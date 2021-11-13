package com.example.garbagesortrecycler;

import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.garbagesortrecycler.Constans.URL;
import com.example.garbagesortrecycler.database.BinInfo;
import com.example.garbagesortrecycler.util.BinInfoAdapter;
import com.example.garbagesortrecycler.util.Utility;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Response;

public class MainActivity extends BaseActivity {

    private FormBody formBody;
    private SharedPreferences datapref;
    private TextView TitleName;
    private RecyclerView BinInfoRecyclerView;
    public SwipeRefreshLayout swipeRefresh;
    private List<BinInfo> BinInfoList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //初始化控件
        TitleName = findViewById(R.id.title_name);
        swipeRefresh = findViewById(R.id.swipe_refresh);
        BinInfoRecyclerView = findViewById(R.id.bin_info_recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);  //获得线性布局
        BinInfoRecyclerView.setLayoutManager(layoutManager);  //设置布局
//        BinInfoRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        Typeface typeface=Typeface.createFromAsset(getAssets(),"mytypeface.TTF");
        TitleName.setTypeface(typeface);
        //为RecyclerView添加分割线
        //初始化控件
        TitleName.setText("垃圾桶信息");
        datapref = getSharedPreferences("admin",MODE_PRIVATE);  //获取自定义的SharedPreferences
        AddClickListener();  //注册控件点击监听事件
        swipeRefresh.setRefreshing(true);
        formBody = new FormBody.Builder()    //生成表单数据
                .add("admin_id",datapref.getInt("admin_id",0)+"")
                .build();
        GetBinInfo();

    }

    private void AddClickListener(){
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                GetBinInfo();
            }
        });

    }

    private void GetBinInfo(){
        Utility.EnqueueHttpRequest(URL.QUERYBININFO, formBody, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        swipeRefresh.setRefreshing(false);
                        Toast.makeText(MainActivity.this, "数据获取异常", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                //解析json数据
                try{
                    final String result = response.body().string();   //获取服务器返回的json数据
                    JSONObject jsonObject = new JSONObject(result);
                    int num = jsonObject.getInt("count");
                    if (num > 0) {    //若投放记录大于0条则解析data数据
                        String data = jsonObject.getString("data");
                        Gson gson = new Gson();  //利用GSON解析data数据
                        if (num == 1){   //如果记录中只有一条 无法用TypeToken解析数组
                            BinInfo b = gson.fromJson(data,BinInfo.class);
                            BinInfoList = new ArrayList<>();  //则需要单独解析出来存入一个数组
                            BinInfoList.add(b);
                        }
                        else   //如果2条以上就可以直接解析
                            BinInfoList = gson.fromJson(data,new TypeToken<List<BinInfo>>(){}.getType());  //将json数组转化为list类型
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //RecyclerView设置适配器
                                BinInfoAdapter adapter = new BinInfoAdapter(BinInfoList);
                                BinInfoRecyclerView.setAdapter(adapter);
                                swipeRefresh.setRefreshing(false);
                            }
                        });
                    }
                    else{    //若记录数为0或其他情况均关闭进度条
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                swipeRefresh.setRefreshing(false);
                            }
                        });
                    }
                }catch (Exception e){
                    e.printStackTrace();   //生吞异常并关闭进度条
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            swipeRefresh.setRefreshing(false);
                            Toast.makeText(MainActivity.this,"数据获取异常",Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });

    }

}