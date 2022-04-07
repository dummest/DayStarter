package com.example.daystarter.ui.alarm.alarmslist;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.daystarter.R;
import com.example.daystarter.ui.alarm.data.Alarm;

import java.util.ArrayList;
import java.util.List;

public class AlarmRecyclerViewAdapter extends RecyclerView.Adapter<AlarmViewHolder>{
     List<Alarm> alarms =new ArrayList<Alarm>();
    private OnToggleAlarmListener listener;
    Context context;
    int lastPosition = -1;


    public AlarmRecyclerViewAdapter(OnToggleAlarmListener listener) {
        this.alarms = new ArrayList<Alarm>();
        this.listener = listener;
    }

    @NonNull
    @Override
    public AlarmViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_alarm, parent, false);
        context=  parent.getContext();
        return new AlarmViewHolder(itemView, listener,itemClickListener,itemLongClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull AlarmViewHolder holder, int position) {
        Alarm alarm = alarms.get(position);
        holder.bind(alarm);
    }


    //클릭시////////////////////////////////////////////////////////////////
    public interface OnItemClickListener{
        void onItemClick(View view, int position);
    }

    private OnItemClickListener itemClickListener;

    public void setOnItemClickListener(OnItemClickListener ClickListener){
        itemClickListener = ClickListener;
    }

    //길게 누를때
    public interface OnItemLongClickListener{
        void onItemLongClick(View view, int position);
    }

    private OnItemLongClickListener itemLongClickListener;

    public void setOnItemLongClickListener(OnItemLongClickListener longClickListener){
        itemLongClickListener = longClickListener;
    }
   //////////////////////////////////////////////////////////

    public void addItem(Alarm alarm){
        alarms.add(alarm);
    }
    //아이템 추가
    public  void addItem(int position,Alarm alarm){
        alarms.add(position,alarm);
        Log.d("add","데이터 추ㅣ");
    }

    public void removeAllItem(){
        alarms.clear();
    }

    public void removeItem(int position){
        alarms.remove(position);
        Log.d("remove","데이터 삭제");
    }
    //위치 가져오기
    public Alarm getItem(int i){
        return alarms.get(i);
    }





    @Override
    public int getItemCount() {
        return alarms.size();
    }

    public void setAlarms(List<Alarm> alarms) {
        this.alarms = alarms;
        notifyDataSetChanged();
    }

    @Override
    public void onViewRecycled(@NonNull AlarmViewHolder holder) {
        super.onViewRecycled(holder);
        holder.alarmStarted.setOnCheckedChangeListener(null);
    }
}

