package com.example.daystarter.ui.groupSchedule;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Database;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.daystarter.R;
import com.example.daystarter.databinding.ActivityWritingGroupScheduleBinding;
import com.example.daystarter.ui.groupSchedule.myClass.GroupScheduleModel;
import com.example.daystarter.ui.groupSchedule.myClass.Member;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

public class WritingGroupScheduleActivity extends AppCompatActivity  implements View.OnClickListener{
    ActivityWritingGroupScheduleBinding binding;
    String groupId;
    Calendar beforeCalendar = new GregorianCalendar(), afterCalendar = new GregorianCalendar();
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy년 MM월 dd일", Locale.getDefault());
    SimpleDateFormat timeFormat = new SimpleDateFormat("HH시 mm분", Locale.getDefault());

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWritingGroupScheduleBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setFirst();

        binding.saveButton.setOnClickListener(this);
        binding.cancelButton.setOnClickListener(this);
        binding.beforeDateTextView.setOnClickListener(this);
        binding.beforeTimeTextView.setOnClickListener(this);
        binding.afterDateTextView.setOnClickListener(this);
        binding.afterTimeTextView.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        int id = view.getId();

        if(id == binding.beforeDateTextView.getId()){
            new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                    setBeforeDate(new GregorianCalendar(i, i1, i2, beforeCalendar.get(Calendar.HOUR_OF_DAY), beforeCalendar.get(Calendar.MINUTE)));
                }
            }, beforeCalendar.get(Calendar.YEAR), beforeCalendar.get(Calendar.MONTH), beforeCalendar.get(Calendar.DAY_OF_MONTH)).show();
        }
        else if(id == binding.afterDateTextView.getId()){
            new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                    setAfterDate(new GregorianCalendar(i, i1, i2, afterCalendar.get(Calendar.HOUR_OF_DAY), afterCalendar.get(Calendar.MINUTE)));
                }
            }, afterCalendar.get(Calendar.YEAR), afterCalendar.get(Calendar.MONTH), afterCalendar.get(Calendar.DAY_OF_MONTH)).show();
        }

        else if(id == binding.beforeTimeTextView.getId()) {
            new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
                @Override
                public void onTimeSet(TimePicker timePicker, int i, int i1) {
                    setBeforeDate(new GregorianCalendar(beforeCalendar.get(Calendar.YEAR), beforeCalendar.get(Calendar.MONTH), beforeCalendar.get(Calendar.DAY_OF_MONTH), i, i1));
                }
            }, beforeCalendar.get(Calendar.HOUR_OF_DAY), beforeCalendar.get(Calendar.MINUTE), false).show();
        }

        else if(id == binding.afterTimeTextView.getId()){
            new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
                @Override
                public void onTimeSet(TimePicker timePicker, int i, int i1) {
                    setAfterDate(new GregorianCalendar(afterCalendar.get(Calendar.YEAR), afterCalendar.get(Calendar.MONTH), afterCalendar.get(Calendar.DAY_OF_MONTH), i, i1));
                }
            }, afterCalendar.get(Calendar.HOUR_OF_DAY), afterCalendar.get(Calendar.MINUTE), false).show();
        }

        //작성 권한 확인 후 저장 or 종료
        else if(id == binding.saveButton.getId()){
            checkStatus();
        }
        else if(id == binding.cancelButton.getId()){
            finish();
        }
    }

    void checkGroupId(){
        groupId = getIntent().getStringExtra("groupId");
        if(groupId == null){
            finish();
        }
    }

    void checkStatus(){
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
        dbRef.child("groups").child(groupId).child("members").child(FirebaseAuth.getInstance().getUid()).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                Member member = task.getResult().getValue(Member.class);
                if (member.status.equals("host") || member.status.equals("write")){
                    write();
                }
                else{
                    showToast("오류");
                    finish();
                }
            }
        });

    }

    void write(){
        String title = binding.titleEditText.getText().toString().trim();
        long beforeTime = beforeCalendar.getTimeInMillis();
        long afterTime = afterCalendar.getTimeInMillis();
        String contents = binding.contentsEditText.getText().toString().trim();

        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
        String key = dbRef.child("schedules").child(groupId).push().getKey();

        GroupScheduleModel gsm = new GroupScheduleModel(key, FirebaseAuth.getInstance().getUid(), Calendar.getInstance().getTimeInMillis(), title, beforeTime, afterTime, contents);
        dbRef.child("schedules").child(groupId).child(key).setValue(gsm).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()) {
                    showToast("저장되었습니다");
                }
                else {
                    showToast("에러. 다시 시도해주세요");
                }
                finish();
            }
        });
    }

    public void setBeforeDate(Calendar calendar) {
        beforeCalendar = calendar;

        binding.beforeDateTextView.setText(dateFormat.format(beforeCalendar.getTime()));
        binding.beforeTimeTextView.setText(timeFormat.format(beforeCalendar.getTime()));

        if(beforeCalendar.after(afterCalendar))
            setAfterDate(beforeCalendar);
    }

    public void setAfterDate(Calendar calendar) {
        afterCalendar = calendar;

        binding.afterDateTextView.setText(dateFormat.format(afterCalendar.getTime()));
        binding.afterTimeTextView.setText(timeFormat.format(afterCalendar.getTime()));

        if(afterCalendar.before(beforeCalendar))
            setBeforeDate(afterCalendar);
    }

    private void showToast(String str){
        Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
    }

    public void setFirst(){
        checkGroupId();
        Intent intent = getIntent();
        binding.titleEditText.setText(intent.getStringExtra("title"));
        beforeCalendar.setTimeInMillis(intent.getLongExtra("beforeLong", Calendar.getInstance().getTimeInMillis()));
        afterCalendar.setTimeInMillis(intent.getLongExtra("afterLong", beforeCalendar.getTimeInMillis()));
        setBeforeDate(beforeCalendar);
        setAfterDate(afterCalendar);
    }
}