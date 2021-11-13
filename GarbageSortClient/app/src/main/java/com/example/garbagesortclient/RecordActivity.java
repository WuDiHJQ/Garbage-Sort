package com.example.garbagesortclient;

        import android.app.ProgressDialog;
        import android.content.SharedPreferences;
        import android.os.Bundle;
        import android.view.View;
        import android.widget.ImageView;
        import android.widget.TextView;
        import android.widget.Toast;

        import androidx.recyclerview.widget.DividerItemDecoration;
        import androidx.recyclerview.widget.LinearLayoutManager;
        import androidx.recyclerview.widget.RecyclerView;

        import com.example.garbagesortclient.Constants.URL;
        import com.example.garbagesortclient.database.Record;
        import com.example.garbagesortclient.util.RecordAdapter;
        import com.example.garbagesortclient.util.Utility;
        import com.google.gson.Gson;
        import com.google.gson.reflect.TypeToken;

        import org.jetbrains.annotations.NotNull;
        import org.json.JSONObject;

        import java.io.IOException;
        import java.text.DecimalFormat;
        import java.util.ArrayList;
        import java.util.List;

        import okhttp3.Call;
        import okhttp3.Callback;
        import okhttp3.FormBody;
        import okhttp3.Response;

public class RecordActivity extends BaseActivity {

    private ProgressDialog progressDialog;
    private SharedPreferences datapref;
    private ImageView Back;
    private RecyclerView RecordRecyclerView;
    private TextView TimeSum;
    private TextView WeightSum;
    private List<Record> records;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);
        //因为Record页面背景色不一致 需要单独设置
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        //初始化控件
        Back = findViewById(R.id.back);
        RecordRecyclerView = findViewById(R.id.record_recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);  //获得线性布局
        RecordRecyclerView.setLayoutManager(layoutManager);  //设置布局
        RecordRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        //为RecyclerView添加分割线
        TimeSum = findViewById(R.id.sum_time);
        WeightSum = findViewById(R.id.sum_weight);
        progressDialog = new ProgressDialog(RecordActivity.this);
        datapref = getSharedPreferences("data",MODE_PRIVATE);  //获取自定义的SharedPreferences
        //初始化控件
        AddClickListener();  //注册控件点击监听事件
        FormBody formBody = new FormBody.Builder()    //生成表单数据
                .add("user_id",datapref.getInt("user_id",0)+"")
                .build();
        progressDialog.setMessage("加载中...");   //进度条开始加载
        progressDialog.setCancelable(false);
        progressDialog.show();
        Utility.EnqueueHttpRequest(URL.QUERYRECORD, formBody, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.dismiss();  //关闭进度条
                        Toast.makeText(RecordActivity.this, "数据获取异常", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                //解析json数据
                try{
                    String result = response.body().string();   //获取服务器返回的json数据
                    JSONObject jsonObject = new JSONObject(result);
                    int num = jsonObject.getInt("count");
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
                                //RecyclerView设置适配器
                                RecordAdapter adapter = new RecordAdapter(records);
                                RecordRecyclerView.setAdapter(adapter);
                                //计算并设置统计值
                                int sumtime = records.size();
                                int sumweight = 0;
                                for (Record r : records)
                                    sumweight += r.getTrash_weight();
                                TimeSum.setText(sumtime + " ");   //加空格防止斜体被挡住 也可用padding
                                DecimalFormat df = new DecimalFormat("0.00");  //保留两位小数显示
                                WeightSum.setText(df.format(sumweight/1000.0) + " ");  //转化为千克显示
                                progressDialog.dismiss();  //数据显示完成关闭进度条
                            }
                        });
                    }
                    else{    //若记录数为0或其他情况均关闭进度条
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progressDialog.dismiss();
                            }
                        });
                    }
                }catch (Exception e){
                    e.printStackTrace();   //生吞异常并关闭进度条
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressDialog.dismiss();
                            Toast.makeText(RecordActivity.this,"数据获取异常",Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });



    }

    private void AddClickListener(){
        Back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

}