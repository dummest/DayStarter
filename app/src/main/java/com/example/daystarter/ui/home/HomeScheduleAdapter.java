package com.example.daystarter.ui.home;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.daystarter.R;
import com.example.daystarter.myClass.PersonalScheduleDBHelper;
import com.example.daystarter.myClass.ScheduleData;
import com.example.daystarter.ui.groupSchedule.GroupActivity;
import com.example.daystarter.ui.groupSchedule.GroupSchedulePostActivity;
import com.example.daystarter.ui.groupSchedule.HostFragment;
import com.example.daystarter.ui.groupSchedule.myClass.Group;
import com.example.daystarter.ui.groupSchedule.myClass.GroupInfo;
import com.example.daystarter.ui.groupSchedule.myClass.GroupScheduleModel;
import com.example.daystarter.ui.home.myClass.HomeSchedule;
import com.example.daystarter.ui.todo.WritablePersonalScheduleActivity;
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

import de.hdodenhof.circleimageview.CircleImageView;

public class HomeScheduleAdapter extends RecyclerView.Adapter<HomeScheduleAdapter.HomeScheduleViewHolder> {
    ArrayList<HomeSchedule> homeScheduleArrayList;
    ArrayList<String> groupIdArrayList;
    Calendar startTime, endTime;
    Context context;
    SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
    SimpleDateFormat dateFormat = new SimpleDateFormat("MM월 dd일");
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
        HomeSchedule homeSchedule = homeScheduleArrayList.get(position);

        holder.titleTextView.setText(homeSchedule.title);
        String startString, endString;
        //
        if(dateFormat.format(homeSchedule.startTime).equals(dateFormat.format(homeSchedule.endTime))) {
            startString = "시작: " + timeFormat.format(homeSchedule.startTime);
            endString = "종료: " + timeFormat.format(homeSchedule.endTime);
        }
        else{
            startString = "시작: " + dateFormat.format(homeSchedule.startTime) + " " + timeFormat.format(homeSchedule.startTime);
            endString = "종료: " + dateFormat.format(homeSchedule.endTime) + " " + timeFormat.format(homeSchedule.endTime);
        }
        holder.startTimeTextView.setText(startString);
        holder.endTimeTextView.setText(endString);


        //그룹 스케줄이 아닐 경우
        if(homeSchedule.groupCode == null) {
            holder.categoryTextView.setText("개인 스케줄");
        }
        //그룹 스케줄일 경우
        else{
            DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference().child("groups")
                    .child(homeSchedule.groupCode);
            dbRef.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    Group group = task.getResult().getValue(Group.class);
                    holder.categoryTextView.setText(group.groupName);

                    Glide.with(holder.itemView.getContext()).load(group.imagePath).circleCrop().error(R.drawable.ic_outline_group_24)
                            .into((holder).circleImageView);
                }
            });
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent;
                if(homeSchedule.groupCode == null){
                    intent = new Intent(context, WritablePersonalScheduleActivity.class);
                    intent.putExtra("scheduleId", Integer.parseInt(homeSchedule.scheduleId));
                    Log.d(TAG, "onClick: " + homeSchedule.scheduleId);
                }
                else{
                    intent = new Intent(context, GroupSchedulePostActivity.class);
                    intent.putExtra("key", homeSchedule.scheduleId);
                    intent.putExtra("groupId", homeSchedule.groupCode);
                }
                context.startActivity(intent);
            }
        });
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
                    }
                    loadTodaySchedules();
                }
            }
        });
    }

    public void loadTodaySchedules(){
        for(int i = 0; i < groupIdArrayList.size(); i++){
            Log.d(TAG, "loadTodaySchedules: " + groupIdArrayList.size());
            final int index = i;
            DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference().child("schedules")
                    .child(groupIdArrayList.get(i));
            dbRef.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    if(task.isSuccessful()){
                        for(DataSnapshot ds: task.getResult().getChildren()){
                            GroupScheduleModel gsm = ds.getValue(GroupScheduleModel.class);
                            if(gsm.startTime < endTime.getTimeInMillis() && gsm.endTime >= startTime.getTimeInMillis()){
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
        CircleImageView circleImageView;
        public HomeScheduleViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.title_text_view);
            startTimeTextView = itemView.findViewById(R.id.start_time_text_view);
            endTimeTextView = itemView.findViewById(R.id.end_time_text_view);
            categoryTextView = itemView.findViewById(R.id.category_text_view);
            circleImageView = itemView.findViewById(R.id.group_image_view);
        }
    }

}
