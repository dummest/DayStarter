package com.example.daystarter.ui.groupSchedule;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import com.example.daystarter.R;
import com.example.daystarter.databinding.ActivityScheduleSearchBinding;
import com.example.daystarter.ui.groupSchedule.myClass.GroupScheduleModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

public class ScheduleSearchActivity extends AppCompatActivity {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy년 MM월 dd일", Locale.getDefault());

    GroupScheduleRecyclerViewAdapter adapter;
    String groupId;
    ArrayList<GroupScheduleModel> scheduleList = new ArrayList<GroupScheduleModel>();
    ActivityScheduleSearchBinding binding;
    InputMethodManager imm;
    Calendar calendar = new GregorianCalendar();
    long beforeDate = 0;
    long afterDate = Long.MAX_VALUE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityScheduleSearchBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        groupId = getIntent().getStringExtra("groupId");
        if(groupId.isEmpty())
            finish();

        initScheduleList();
        setOn();
    }

    void setOn(){
        imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        binding.searchEditText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if(i == KeyEvent.KEYCODE_ENTER){
                    imm.hideSoftInputFromWindow(binding.searchEditText.getWindowToken(), 0);
                    search(binding.searchEditText.getText().toString());
                }
                return true;
            }
        });
        binding.searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                search(binding.searchEditText.getText().toString());
            }
        });

        binding.beforeDateTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DatePickerDialog(ScheduleSearchActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                        Calendar calendar = new GregorianCalendar(i, i1, i2);
                        setBeforeDate(calendar.getTimeInMillis());
                    }
                }, calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH),calendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });


        binding.afterDateTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DatePickerDialog(ScheduleSearchActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                        Calendar calendar = new GregorianCalendar(i, i1, i2);
                        setAfterDate(calendar.getTimeInMillis());
                    }
                }, calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH),calendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

    }

    void setBeforeDate(long date){
        beforeDate = date;
        if(beforeDate > afterDate){
            setAfterDate(date);
        }
        binding.beforeDateTextView.setText(sdf.format(new Date(date)));
        calendar.setTimeInMillis(date);
    }

    //TODO 날짜 검색은 아마 일자
    void setAfterDate(long date){
        calendar.setTimeInMillis(date);
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        afterDate = calendar.getTimeInMillis();

        if(afterDate < beforeDate){
            setBeforeDate(date);
        }

        binding.afterDateTextView.setText(sdf.format(new Date(date)));
    }

    void search(String searchingText){
        ArrayList<GroupScheduleModel> arrayList = new ArrayList<>();

        for (GroupScheduleModel gsm:scheduleList) {
            if((gsm.title.contains(searchingText) || gsm.contents.contains(searchingText)) && (gsm.startTime>beforeDate && gsm.endTime<afterDate)){
                arrayList.add(gsm);
            }
        }
        adapter.setScheduleList(arrayList);
    }

    void initScheduleList(){
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference()
                .child("schedules").child(groupId);
        dbRef.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                for (DataSnapshot ds: task.getResult().getChildren()) {
                    GroupScheduleModel gsm = ds.getValue(GroupScheduleModel.class);
                    scheduleList.add(gsm);
                }
                binding.scheduleRecyclerView.setLayoutManager(new LinearLayoutManager(getLayoutInflater().getContext()));
                adapter = new GroupScheduleRecyclerViewAdapter(scheduleList);
                binding.scheduleRecyclerView.setAdapter(adapter);
            }
        });
    }

    public class GroupScheduleRecyclerViewAdapter extends RecyclerView.Adapter<ScheduleSearchActivity.GroupScheduleViewHolder> {
        ArrayList<GroupScheduleModel> scheduleList;

        public GroupScheduleRecyclerViewAdapter(ArrayList<GroupScheduleModel> arrayList){
            setScheduleList(arrayList);
        }
        @NonNull
        @Override
        public ScheduleSearchActivity.GroupScheduleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.schedule_list_item, parent, false);
            return new ScheduleSearchActivity.GroupScheduleViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ScheduleSearchActivity.GroupScheduleViewHolder holder, int position) {
            holder.mainTextView.setText(scheduleList.get(position).title);
            String start = sdf.format(scheduleList.get(position).startTime);
            String end = sdf.format(scheduleList.get(position).endTime);
            if(start.equals(end))
                holder.subTextView.setText(start);
            else
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
        void setScheduleList(ArrayList<GroupScheduleModel> arrayList) {
            scheduleList = arrayList;
            if(getItemCount() == 0)
                binding.showEmptyTextView.setVisibility(View.VISIBLE);
            else
                binding.showEmptyTextView.setVisibility(View.GONE);
            notifyDataSetChanged();
            Toast.makeText(ScheduleSearchActivity.this, "검색 완료", Toast.LENGTH_SHORT);
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