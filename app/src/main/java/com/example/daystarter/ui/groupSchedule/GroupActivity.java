package com.example.daystarter.ui.groupSchedule;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
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
import com.example.daystarter.myClass.PersonalScheduleDBHelper;
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
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;
import com.prolificinteractive.materialcalendarview.spans.DotSpan;

import org.threeten.bp.DayOfWeek;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

public class GroupActivity extends AppCompatActivity {
    SimpleDateFormat sdf = new SimpleDateFormat("MM??? dd??? HH:mm", Locale.getDefault());
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

    //?????? ???????????? ????????? ???????????????, ????????? ??????????????? ?????? ??? ?????? ????????? ?????????
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
        binding.mcvViewGroup.calendar.addDecorators(
                new SundayDecorator(),
                new SaturdayDecorator(),
                new TodayDecorator(),
                new DaySelector(this));
        binding.groupScheduleFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
                dbRef.child("groups").child(groupId).child("members").child(FirebaseAuth.getInstance().getUid())
                        .get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                        if(task.isSuccessful()) {
                            Member member = task.getResult().getValue(Member.class);
                            if(member.status.equals("write") || member.status.equals("host"))
                                goWrite();
                            else{
                                showToast("?????? ????????? ????????????");
                            }
                        }
                    }
                });
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

    void goWrite(){
        Intent intent = new Intent(getBaseContext(), WritingGroupScheduleActivity.class);
        intent.putExtra("groupId", groupId);
        Calendar calendar = new GregorianCalendar();

        //????????? ?????? ????????? ???
        if(binding.mcvViewGroup.calendar.getSelectedDate().equals(CalendarDay.today())) {
            calendar.set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY)+1);
            calendar.set(Calendar.MINUTE, 0);
        }
        //????????? ?????? ????????? ?????? ???
        else{
            CalendarDay cd = binding.mcvViewGroup.calendar.getSelectedDate();
            calendar.set(cd.getYear(), cd.getMonth()-1, cd.getDay(), 8, 0);
                    /*
                    Note: ???????????? Calendar??? Month??? 0?????? ???????????????(?????????????????? ?????? ????????? ?????? ????????? ?????? ex)March
                    MCV??? Month??? 1?????? ?????????. ?????? ???????????? ????????? ???.
                    */
        }
        intent.putExtra("beforeLong", calendar.getTimeInMillis());

        calendar.add(Calendar.HOUR_OF_DAY, 1);
        intent.putExtra("afterLong", calendar.getTimeInMillis());

        startActivity(intent);
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

    private static class DaySelector implements DayViewDecorator {
        //selector decorator
        private final Drawable drawable;

        public DaySelector(Context context) {
            drawable = ContextCompat.getDrawable(context, R.drawable.mcv_selector);
        }

        // true??? ?????? ??? ?????? ????????? ?????? ????????? ??????????????? ????????????
        @Override
        public boolean shouldDecorate(CalendarDay day) {
            return true;
        }

        // ?????? ?????? ??? ?????? ????????? ??????????????? ??????????????? ??????
        @Override
        public void decorate(DayViewFacade view) {
            view.setSelectionDrawable(drawable);
//            view.addSpan(new StyleSpan(Typeface.BOLD));   // ?????? ?????? ?????? ???????????? ?????? ?????????
        }
    }

    private class SaturdayDecorator implements DayViewDecorator{
        //saturday decorator(make text blue)
        @Override
        public boolean shouldDecorate(CalendarDay day) {
            int saturday = day.getDate().with(DayOfWeek.SATURDAY).getDayOfMonth();
            return day.getDay() == saturday;
        }

        @Override
        public void decorate(DayViewFacade view) {
            view.addSpan(new ForegroundColorSpan(getResources().getColor(R.color.blue, GroupActivity.this.getTheme())));
        }
    }

    private class SundayDecorator implements DayViewDecorator{
        //sunday decorator(make text red)
        @Override
        public boolean shouldDecorate(CalendarDay day) {
            int sunday = day.getDate().with(DayOfWeek.SUNDAY).getDayOfMonth();
            return day.getDay() == sunday;
        }

        @Override
        public void decorate(DayViewFacade view) {
            view.addSpan(new ForegroundColorSpan(getResources().getColor(R.color.red, GroupActivity.this.getTheme())));
        }
    }

    private class TodayDecorator implements DayViewDecorator{
        @Override
        public boolean shouldDecorate(CalendarDay day) {
            return day.equals(CalendarDay.today());
        }

        @Override
        public void decorate(DayViewFacade view) {
            view.addSpan(new ForegroundColorSpan(getResources().getColor(R.color.green, GroupActivity.this.getTheme())));
            view.addSpan(new StyleSpan(Typeface.BOLD));
            view.addSpan(new UnderlineSpan());
        }
    }
}



