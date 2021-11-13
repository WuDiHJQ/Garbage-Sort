package com.example.garbagesortclient.util;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.garbagesortclient.Constants.MyConstants;
import com.example.garbagesortclient.R;
import com.example.garbagesortclient.database.Record;
import java.util.List;

public class RecordAdapter extends RecyclerView.Adapter<RecordAdapter.ViewHolder> {

    private List<Record> RecordList;

    static class ViewHolder extends RecyclerView.ViewHolder{  //静态类 因为不引用外部类 所以可以声明为静态
        //使用static的内部类相对独立 因为不能引用外部类的成员变量和方法

        TextView TimeText;
        TextView LocText;
        TextView TypeText;
        TextView WeightText;

        public ViewHolder(@NonNull View View) {    //构造方法中传入最外层布局
            super(View);    //将布局载入ViewHolder 能让下次出现时快速加载
            TimeText = View.findViewById(R.id.time_text);
            LocText = View.findViewById(R.id.loc_text);
            TypeText = View.findViewById(R.id.type_text);
            WeightText = View.findViewById(R.id.weight_text);
        }
    }

    public RecordAdapter(List<Record> list){
        this.RecordList = list;
    }

    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {  //创建ViewHolder实例
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.record_item,parent,false);  //第一个参数要加载的布局 第二个参数加载到哪里
        ViewHolder holder = new ViewHolder(view);  //将View作为参数构造ViewHolder 使Holder获得控件
        //省去了每次findViewById的时间
        return holder;
    }  //载入内容时执行

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Record record = RecordList.get(position);    //将数据载入Holder中的布局
        holder.TimeText.setText(record.getTime());
        holder.LocText.setText(record.getBin_loc());
        if (record.getTrash_type() == MyConstants.EXPRESS_TYPE)
            holder.TypeText.setText("快递包装");
        else if (record.getTrash_type() == MyConstants.TAKEAWAY_TYPE)
            holder.TypeText.setText("外卖餐盒");
        holder.WeightText.setText("+" + record.getTrash_weight() + "g ");
    }//当内容滚入屏幕时创建ViewHolder

    @Override
    public int getItemCount() {
        return RecordList.size();
    }

}
