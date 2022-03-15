package com.example.daystarter.adapter;

import android.content.Context;
import android.content.Intent;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.daystarter.R;
import com.example.daystarter.databinding.ScheduleListItemBinding;
import com.example.daystarter.myClass.ScheduleData;
import com.example.daystarter.ui.todo.WritablePersonalScheduleActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class ScheduleListViewAdapter extends BaseAdapter {
    private static final String TAG = "ScheduleListViewAdapter";
    LayoutInflater inflater;
    Context context;
    ArrayList<ScheduleData> scheduleDataArrayList = new ArrayList<>();

    public ScheduleListViewAdapter(Context context){
        this.context = context;
        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void addItem(ScheduleData scheduleData){
        scheduleDataArrayList.add(scheduleData);
    }

    public void removeItem(ScheduleData data){
        scheduleDataArrayList.remove(data);
    }

    public void setList(ArrayList<ScheduleData> list){
        this.scheduleDataArrayList = list;
    }

    @Override
    public int getCount() {
        return scheduleDataArrayList.size();
    }

    @Override
    public Object getItem(int i) {
        return scheduleDataArrayList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return scheduleDataArrayList.get(i).getScheduleId();
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if(view == null){
            view = inflater.inflate(R.layout.schedule_list_item, viewGroup, false);
        }
        ScheduleData myData = scheduleDataArrayList.get(i);
        TextView titleTextView = view.findViewById(R.id.main_text_view);
        TextView timeTextView = view.findViewById(R.id.sub_text_view);

        titleTextView.setText(myData.getTitle());

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        String text = sdf.format(new Date(myData.getStartTime())) + " - " +
                sdf.format(new Date(myData.getEndTime()));
        timeTextView.setText(text);

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, WritablePersonalScheduleActivity.class);
                intent.putExtra("beforeLong", myData.getStartTime());
                intent.putExtra("afterLong", myData.getEndTime());
                context.startActivity(intent);
            }
        });
        Log.d(TAG, "getView: ");
        return view;
    }
}
