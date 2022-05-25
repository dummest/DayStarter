package com.example.daystarter.ui.groupSchedule;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.daystarter.R;
import com.example.daystarter.adapter.ScheduleViewHolder;
import com.example.daystarter.databinding.ActivityGroupBinding;
import com.example.daystarter.databinding.ActivityWritingGroupScheduleBinding;
import com.example.daystarter.ui.groupSchedule.adapter.GroupRecyclerViewAdapter;
import com.example.daystarter.ui.groupSchedule.myClass.GroupScheduleModel;
import com.example.daystarter.ui.groupSchedule.myClass.Member;
import com.example.daystarter.ui.todo.WritablePersonalScheduleActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

public class GroupActivity extends AppCompatActivity {
    SimpleDateFormat sdf = new SimpleDateFormat("MM월 dd일 HH:mm", Locale.getDefault());
    ActivityGroupBinding binding;
    String groupId;
    GroupScheduleRecyclerViewAdapter adapter = new GroupScheduleRecyclerViewAdapter();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGroupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        groupId = getIntent().getStringExtra("groupId");
        binding.scheduleRecyclerView.setLayoutManager(new LinearLayoutManager(getLayoutInflater().getContext()));
        binding.scheduleRecyclerView.setAdapter(adapter);
        validation();
        init();

    }

    @Override
    protected void onResume() {
        super.onResume();
        validation();
    }

    //그룹 구성원인 사람이 접속했는지, 그룹이 존재하는지 확인 후 진행 하거나 취소함
    void validation(){
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
        DatabaseReference memberRef = dbRef.child("groups").child(groupId).child("members").child(FirebaseAuth.getInstance().getUid());
        memberRef.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if(!task.getResult().exists()){
                    finish();
                }
            }
        });
    }
    private void init(){
        binding.groupScheduleFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getBaseContext(), WritingGroupScheduleActivity.class);
                intent.putExtra("groupId", groupId);
                Calendar calendar = new GregorianCalendar();

                //선택한 날이 오늘일 시
                if(binding.mcvViewGroup.calendar.getSelectedDate().equals(CalendarDay.today())) {
                    calendar.set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY)+1);
                    calendar.set(Calendar.MINUTE, 0);
                }
                //선택한 날이 오늘이 아닐 시
                else{
                    CalendarDay cd = binding.mcvViewGroup.calendar.getSelectedDate();
                    calendar.set(cd.getYear(), cd.getMonth()-1, cd.getDay(), 8, 0);
                    /*
                    Note: 자바에서 Calendar는 Month가 0부터 시작하지만(영어권에서는 달을 숫자가 아닌 영어로 부름 ex)March
                    MCV는 Month가 1부터 시작함. 고로 연산할때 주의할 것.
                    */
                }
                intent.putExtra("beforeLong", calendar.getTimeInMillis());

                calendar.add(Calendar.HOUR_OF_DAY, 1);
                intent.putExtra("afterLong", calendar.getTimeInMillis());

                startActivity(intent);
            }
        });
        binding.mcvViewGroup.calendar.setOnDateChangedListener(new OnDateSelectedListener() {
            @Override
            public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {
                adapter.loadGroupScheduleList(date.getYear(), date.getMonth()-1, date.getDay());
            }
        });
        binding.settingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getBaseContext(), GroupSettingActivity.class);
                intent.putExtra("groupId", groupId);
                startActivity(intent);
            }
        });
        CalendarDay today = CalendarDay.today();
        binding.mcvViewGroup.calendar.setSelectedDate(today);
        adapter.loadGroupScheduleList(today.getYear(), today.getMonth()-1, today.getDay());
    }

    private void showToast(String str){
        Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
    }

    public class GroupScheduleRecyclerViewAdapter extends RecyclerView.Adapter<GroupScheduleViewHolder> {
        ArrayList<GroupScheduleModel> scheduleList;

        public GroupScheduleRecyclerViewAdapter(){
            scheduleList  = new ArrayList<>();
        }
        @NonNull
        @Override
        public GroupScheduleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.schedule_list_item, parent, false);
            return new GroupActivity.GroupScheduleViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull GroupScheduleViewHolder holder, int position) {
            holder.mainTextView.setText(scheduleList.get(position).title);
            String start = sdf.format(scheduleList.get(position).startTime);
            String end = sdf.format(scheduleList.get(position).endTime);
            holder.subTextView.setText(start + " ~ " + end);
            //holder.subTextView.setText(scheduleList.get(position).);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(view.getContext(), GroupSchedulePostActivity.class);
                    intent.putExtra("key", scheduleList.get(holder.getAdapterPosition()).key);
                    intent.putExtra("groupId", groupId);
                    startActivity(intent);
                }
            });
        }
        void loadGroupScheduleList(int year, int month, int day) {
            Calendar startTime = Calendar.getInstance();
            Calendar endTime = Calendar.getInstance();
            startTime.set(year, month, day, 0, 0, 0);
            endTime.setTimeInMillis(startTime.getTimeInMillis());
            endTime.add(Calendar.DAY_OF_MONTH, 1);

            DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
            DatabaseReference scheduleRef = dbRef.child("schedules").child(groupId);
            scheduleRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    scheduleList.clear();
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        GroupScheduleModel model = ds.getValue(GroupScheduleModel.class);
                        if (model.startTime >= startTime.getTimeInMillis() && model.endTime < endTime.getTimeInMillis()) {
                            scheduleList.add(model);
                            Log.d(TAG, "add: " + model.title);
                        }
                        if(scheduleList.size()>0)
                            binding.showEmptyTextView.setVisibility(View.GONE);
                        else
                            binding.showEmptyTextView.setVisibility(View.VISIBLE);
                    }
                    notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

        @Override
        public int getItemCount() {
            return scheduleList.size();
        }
    }

    public class GroupScheduleViewHolder extends RecyclerView.ViewHolder{
        public TextView mainTextView, subTextView;
        public GroupScheduleViewHolder(@NonNull View itemView) {
            super(itemView);
            mainTextView = itemView.findViewById(R.id.main_text_view);
            subTextView = itemView.findViewById(R.id.sub_text_view);
        }
    }
}



