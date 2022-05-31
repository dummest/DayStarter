package com.example.daystarter.ui.home;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.daystarter.R;
import com.example.daystarter.myClass.PersonalScheduleDBHelper;
import com.example.daystarter.myClass.ScheduleData;
import com.example.daystarter.ui.groupSchedule.GroupActivity;
import com.example.daystarter.ui.groupSchedule.myClass.Group;
import com.example.daystarter.ui.groupSchedule.myClass.GroupInfo;
import com.example.daystarter.ui.groupSchedule.myClass.GroupScheduleModel;
import com.example.daystarter.ui.home.myClass.HomeSchedule;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;

public class HomeScheduleAdapter extends RecyclerView.Adapter<HomeScheduleAdapter.HomeScheduleViewHolder> {
    ArrayList<HomeSchedule> homeScheduleArrayList;
    ArrayList<String> groupIdArrayList;
    Calendar startTime, endTime;
    Context context;
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");

    public HomeScheduleAdapter(Context context){
        this.context = context;
        homeScheduleArrayList = new ArrayList<>();
        groupIdArrayList = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        setSearchingDay(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        loadPersonalSchedule(startTime.getTimeInMillis());
    }

    public void setSearchingDay(int year, int month, int day){
        startTime = Calendar.getInstance();
        endTime = Calendar.getInstance();
        startTime.set(year, month, day, 0, 0, 0);
        endTime.setTimeInMillis(startTime.getTimeInMillis());
        endTime.add(Calendar.DAY_OF_MONTH, 1);
    }

    @NonNull
    @Override
    public HomeScheduleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_home_schedule, parent, false);
        return new HomeScheduleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HomeScheduleViewHolder holder, int position) {
        holder.titleTextView.setText(homeScheduleArrayList.get(position).title);
        holder.startTimeTextView.setText("시작: " + simpleDateFormat.format(homeScheduleArrayList.get(position).startTime));
        holder.endTimeTextView.setText("종료: " + simpleDateFormat.format(homeScheduleArrayList.get(position).endTime));
        if(homeScheduleArrayList.get(position).groupCode == null) {
            holder.categoryTextView.setText("개인 스케줄");
        }
        else{
            DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference().child("groups")
                    .child(homeScheduleArrayList.get(position).groupCode).child("groupName");
            dbRef.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    String groupName = task.getResult().getValue(String.class);
                    holder.categoryTextView.setText(groupName);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return homeScheduleArrayList.size();
    }

    public void loadPersonalSchedule(long time){
        PersonalScheduleDBHelper dbHelper = new PersonalScheduleDBHelper(context);
        ArrayList<ScheduleData> list = dbHelper.getScheduleList(time);

        for(ScheduleData sd : list){
            HomeSchedule homeSchedule = new HomeSchedule(sd.getTitle(), sd.getStartTime(),
                    sd.getEndTime(), "개인 스케줄", String.valueOf(sd.getScheduleId()));
            homeScheduleArrayList.add(homeSchedule);
        }
        loadHostingGroupInfo();
    }

    public void loadHostingGroupInfo(){
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference().child("users").
                child(FirebaseAuth.getInstance().getUid()).child("hostingGroups");
        dbRef.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if(task.isSuccessful()){
                    for(DataSnapshot ds : task.getResult().getChildren()){
                        GroupInfo info = ds.getValue(GroupInfo.class);
                        groupIdArrayList.add(info.groupId);
                    }
                    loadParticipatingGroupInfo();
                }
            }
        });
    }

    public void loadParticipatingGroupInfo(){
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference().child("users").
                child(FirebaseAuth.getInstance().getUid()).child("participatingGroups");
        dbRef.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if(task.isSuccessful()){
                    for(DataSnapshot ds : task.getResult().getChildren()){
                        GroupInfo info = ds.getValue(GroupInfo.class);
                        groupIdArrayList.add(info.groupId);
                        loadTodaySchedules();
                    }
                }
            }
        });
    }

    public void loadTodaySchedules(){
        for(int i = 0; i < groupIdArrayList.size(); i++){
            final int index = i;
            DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference().child("schedules")
                    .child(groupIdArrayList.get(i));
            dbRef.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    if(task.isSuccessful()){
                        for(DataSnapshot ds: task.getResult().getChildren()){
                            GroupScheduleModel gsm = ds.getValue(GroupScheduleModel.class);
                            if(gsm.startTime >= startTime.getTimeInMillis() && gsm.endTime < endTime.getTimeInMillis()){
                                homeScheduleArrayList.add(new HomeSchedule(gsm.title, gsm.startTime, gsm.endTime,
                                        "groupSchedule", groupIdArrayList.get(index), gsm.key));
                            }
                        }
                        Collections.sort(homeScheduleArrayList);
                        notifyDataSetChanged();
                    }
                }
            });
        }
    }

    public class HomeScheduleViewHolder extends RecyclerView.ViewHolder{
        TextView titleTextView, startTimeTextView, endTimeTextView, categoryTextView;
        public HomeScheduleViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.title_text_view);
            startTimeTextView = itemView.findViewById(R.id.start_time_text_view);
            endTimeTextView = itemView.findViewById(R.id.end_time_text_view);
            categoryTextView = itemView.findViewById(R.id.category_text_view);
        }
    }

}
