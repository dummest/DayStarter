package com.example.daystarter.lockScreen;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.daystarter.R;
import com.example.daystarter.databinding.ItemLockScreenScheduleBinding;
import com.example.daystarter.myClass.PersonalScheduleDBHelper;
import com.example.daystarter.myClass.ScheduleData;
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
import java.util.Collections;

public class LockScreenAdapter extends RecyclerView.Adapter<LockScreenAdapter.LockScreenScheduleViewHolder> {
    Context context;
    ArrayList<HomeSchedule> homeScheduleArrayList;
    ArrayList<String> groupIdArrayList;
    Calendar startTime, endTime;
    SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
    SimpleDateFormat dateFormat = new SimpleDateFormat("MM월 dd일");


    public LockScreenAdapter(Context context) {
        this.context = context;
        homeScheduleArrayList = new ArrayList<>();
        groupIdArrayList = new ArrayList<>();
        init();
    }

    void init(){
        homeScheduleArrayList.clear();
        groupIdArrayList.clear();
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

    public void loadPersonalSchedule(long time){
        PersonalScheduleDBHelper dbHelper = new PersonalScheduleDBHelper(context);
        ArrayList<ScheduleData> list = dbHelper.getScheduleList(time);

        for(ScheduleData sd : list){
            HomeSchedule homeSchedule = new HomeSchedule(sd.getTitle(), sd.getStartTime(),
                    sd.getEndTime(), "개인 스케줄", String.valueOf(sd.getScheduleId()), sd.getLatitude(),sd.getLongitude(), sd.getAddress());
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
                                        "groupSchedule", groupIdArrayList.get(index), gsm.key, gsm.latitude, gsm.longitude, gsm.address));
                                Log.d(TAG, "onComplete: " + gsm.title + ", latitude: " + gsm.latitude + ", longitude: " + gsm.longitude);
                            }
                        }
                        Collections.sort(homeScheduleArrayList);
                        notifyDataSetChanged();
                    }
                }
            });
        }
        Log.d(TAG, "homeScheduleSize: " + homeScheduleArrayList.size());
    }

    @NonNull
    @Override
    public LockScreenScheduleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_lock_screen_schedule, parent, false);
        return new LockScreenScheduleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LockScreenScheduleViewHolder holder, int position) {
        int pos = holder.getAdapterPosition();
        HomeSchedule hs = homeScheduleArrayList.get(pos);
        holder.titleTextView.setText(hs.title);
        holder.addressTextView.setText(hs.address);
        holder.temperatureTextView.setText("lat: " + hs.latitude + ", long: " + hs.longitude);

        String startTime, endTime;
        if(dateFormat.format(hs.startTime).equals(dateFormat.format(Calendar.getInstance().getTimeInMillis()))){
            startTime = timeFormat.format(hs.startTime);
        }
        else{
            startTime = dateFormat.format(hs.startTime) + timeFormat.format(hs.startTime);
        }
        if(dateFormat.format(hs.endTime).equals(dateFormat.format(Calendar.getInstance().getTimeInMillis()))){
            endTime = timeFormat.format(hs.startTime);
        }
        else{
            endTime = dateFormat.format(hs.endTime) + timeFormat.format(hs.endTime);
        }

        holder.timeTextView.setText(startTime + " ~ " + endTime);

    }

    @Override
    public int getItemCount() {
        return homeScheduleArrayList.size();
    }

    public class LockScreenScheduleViewHolder extends RecyclerView.ViewHolder{
        public TextView titleTextView, timeTextView, temperatureTextView, addressTextView;
        public LockScreenScheduleViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.title_text_view);
            timeTextView = itemView.findViewById(R.id.time_text_view);
            temperatureTextView = itemView.findViewById(R.id.temperature_text_view);
            addressTextView = itemView.findViewById(R.id.address_text_view);
        }
    }
}
