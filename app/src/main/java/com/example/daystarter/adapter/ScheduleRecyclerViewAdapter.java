package com.example.daystarter.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.daystarter.R;
import com.example.daystarter.myClass.ScheduleData;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

public class ScheduleRecyclerViewAdapter extends RecyclerView.Adapter<ScheduleViewHolder> {
    private ArrayList<ScheduleData> mData;
    private Context context;
    SimpleDateFormat dateText = new SimpleDateFormat("MM/dd HH:mm", Locale.getDefault());
    SimpleDateFormat timeText = new SimpleDateFormat("HH:mm", Locale.getDefault());

    public ScheduleRecyclerViewAdapter(ArrayList<ScheduleData> data, Context context) {
        mData = data;
        this.context = context;
    }

    public ScheduleRecyclerViewAdapter(Context context) {
        mData = new ArrayList<ScheduleData>();
        this.context = context;
    }

    // onCreateViewHolder : 아이템 뷰를 위한 뷰홀더 객체를 생성하여 리턴
    @NonNull
    @Override
    public ScheduleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.schedule_list_item, parent, false);
        ScheduleViewHolder svh = new ScheduleViewHolder(view, itemClickListener, itemLongClickListener);
        return svh;
    }

    public interface OnItemLongClickListener{
        void onItemLongClick(View view, int position);
    }

    private OnItemLongClickListener itemLongClickListener;

    public void setOnItemLongClickListener(OnItemLongClickListener longClickListener){
        itemLongClickListener = longClickListener;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    public interface OnItemClickListener{
        void onItemClick(View view, int position);
    }

    private OnItemClickListener itemClickListener;

    public void setOnItemClickListener(OnItemClickListener ClickListener){
        itemClickListener = ClickListener;
    }

    // onBindViewHolder : position 에 해당하는 데이터를 뷰홀더의 아이템뷰에 표시
    @Override
    public void onBindViewHolder(@NonNull ScheduleViewHolder holder, int position) {
        ScheduleData data = mData.get(position);
        holder.mainText.setText(data.getTitle());

        Calendar startCalendar = new GregorianCalendar();
        startCalendar.setTimeInMillis(data.getStartTime());
        Calendar endCalendar = new GregorianCalendar();
        endCalendar.setTimeInMillis(data.getEndTime());

        holder.subText.setText(timeText.format(startCalendar.getTime()));
        if(startCalendar.get(Calendar.YEAR) == endCalendar.get(Calendar.YEAR) && startCalendar.get(Calendar.DAY_OF_YEAR) == endCalendar.get(Calendar.DAY_OF_YEAR)) {
            holder.subText.setText(timeText.format(startCalendar.getTime()) + " - "  + timeText.format(endCalendar.getTime()));
            //당일 끝이면 시작,끝시간 전부 표시
        }
        else {
            holder.subText.setText(dateText.format(startCalendar.getTime()) + " - " + dateText.format(endCalendar.getTime()));
        }
    }

    // getItemCount : 전체 데이터의 개수를 리턴
    @Override
    public int getItemCount() { return mData.size(); }

    public void listClear(){
        mData.clear();
        notifyDataSetChanged();
    }

    public ScheduleData getItem(int position){
        return mData.get(position);
    }

    public void addItem(ScheduleData data){
        mData.add(data);
    }

    // 아이템 뷰를 저장하는 뷰홀더 클래스

}
