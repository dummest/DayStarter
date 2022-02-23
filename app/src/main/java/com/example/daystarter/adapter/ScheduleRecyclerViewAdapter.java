package com.example.daystarter.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.daystarter.R;
import com.example.daystarter.myClass.ScheduleData;

import java.util.ArrayList;

public class ScheduleRecyclerViewAdapter extends RecyclerView.Adapter<ScheduleRecyclerViewAdapter.ScheduleViewHolder> {
    private ArrayList<ScheduleData> mData = null;
    private Context context;

    public ScheduleRecyclerViewAdapter(ArrayList<ScheduleData> data, Context context) {
        mData = data;
        this.context = context;
    }

    public ScheduleRecyclerViewAdapter(Context context) {
        mData = new ArrayList<ScheduleData>();
        this.context = context;
    }

    public void addItem(ScheduleData data){
        mData.add(data);
    }

    // onCreateViewHolder : 아이템 뷰를 위한 뷰홀더 객체를 생성하여 리턴
    @NonNull
    @Override
    public ScheduleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.schedule_list_item, parent, false);
        ScheduleRecyclerViewAdapter.ScheduleViewHolder svh = new ScheduleRecyclerViewAdapter.ScheduleViewHolder(view);
        return svh;
    }

    public void listClear(){
        mData.clear();
        notifyDataSetChanged();
    }

    // onBindViewHolder : position 에 해당하는 데이터를 뷰홀더의 아이템뷰에 표시
    @Override
    public void onBindViewHolder(@NonNull ScheduleRecyclerViewAdapter.ScheduleViewHolder holder, int position) {
        ScheduleData data = mData.get(position);
        holder.mainText.setText(data.getTitle());
        holder.subText.setText(data.getStartTime() + "a");
    }

    // getItemCount : 전체 데이터의 개수를 리턴
    @Override
    public int getItemCount() { return mData.size(); }

    // 아이템 뷰를 저장하는 뷰홀더 클래스
    public static class ScheduleViewHolder extends RecyclerView.ViewHolder {
        TextView mainText;
        TextView subText;

        public ScheduleViewHolder(View itemView) {
            super(itemView); // 뷰 객체에 대한 참조
            mainText = itemView.findViewById(R.id.main_text_view);
            subText = itemView.findViewById(R.id.sub_text_view);
        }
    }

}
