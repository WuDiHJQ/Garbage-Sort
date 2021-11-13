package com.example.garbagesortrecycler.util;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.garbagesortrecycler.R;
import com.example.garbagesortrecycler.database.BinInfo;

import java.text.DecimalFormat;
import java.util.List;

public class BinInfoAdapter extends RecyclerView.Adapter<BinInfoAdapter.ViewHolder> {

private List<BinInfo> BinInfoList;

static class ViewHolder extends RecyclerView.ViewHolder{  //静态类 因为不引用外部类 所以可以声明为静态
    //使用static的内部类相对独立 因为不能引用外部类的成员变量和方法

    TextView IdText;
    TextView LocText;
    TextView TakeAwayText;
    TextView ExpressText;
    ProgressBar TakeAwayBar;
    ProgressBar ExpressBar;

    public ViewHolder(@NonNull View View) {    //构造方法中传入最外层布局
        super(View);    //将布局载入ViewHolder 能让下次出现时快速加载
        IdText = View.findViewById(R.id.bin_id_text);
        LocText = View.findViewById(R.id.bin_loc_text);
        TakeAwayText = View.findViewById(R.id.takeaway_weight_text);
        ExpressText = View.findViewById(R.id.express_weight_text);
        TakeAwayBar = View.findViewById(R.id.takeaway_bar);
        ExpressBar = View.findViewById(R.id.express_bar);;
    }
}

    public BinInfoAdapter(List<BinInfo> list){
        this.BinInfoList = list;
    }

    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {  //创建ViewHolder实例
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.bin_info_item,parent,false);  //第一个参数要加载的布局 第二个参数加载到哪里
        ViewHolder holder = new ViewHolder(view);  //将View作为参数构造ViewHolder 使Holder获得控件
        //省去了每次findViewById的时间
        return holder;
    }  //载入内容时执行

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BinInfo binInfo = BinInfoList.get(position);    //将数据载入Holder中的布局
        DecimalFormat df=new DecimalFormat("000000");
        holder.IdText.setText("ID." + df.format(binInfo.getBin_id()));
        if (binInfo.getBin_loc().length() <= 6)
            holder.LocText.setText(binInfo.getBin_loc());
        else
            holder.LocText.setText(binInfo.getBin_loc().substring(0, 6) + "…");
        df = new DecimalFormat("0.00");  //保留两位小数显示
        holder.TakeAwayText.setText(df.format(binInfo.getTakeaway_weight()/1000.0)+"kg");
        holder.ExpressText.setText(df.format(binInfo.getExpress_weight()/1000.0)+"kg");
        holder.TakeAwayBar.setProgress(binInfo.getTakeaway_weight()/250);
        holder.ExpressBar.setProgress(binInfo.getExpress_weight()/250);
    }//当内容滚入屏幕时创建ViewHolder

    @Override
    public int getItemCount() {
        return BinInfoList.size();
    }

}