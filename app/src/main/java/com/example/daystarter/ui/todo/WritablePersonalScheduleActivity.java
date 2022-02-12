package com.example.daystarter.ui.todo;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TimePicker;

import com.example.daystarter.R;
import com.example.daystarter.databinding.ActivityWritablePersonalScheduleBinding;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class WritablePersonalScheduleActivity extends AppCompatActivity implements View.OnClickListener {
    private ActivityWritablePersonalScheduleBinding binding;
    private final String TAG = "ActivityWritablePersonalScheduleBinding";
    int beforeYear, beforeMonth, beforeDay, beforeHour, beforeMinute;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWritablePersonalScheduleBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent intent = getIntent();
        Calendar calendar = Calendar.getInstance();

        //calendar.getTimeInMillis();

        beforeYear = intent.getIntExtra("year", 2022);
        beforeMonth = intent.getIntExtra("month", 1);
        beforeDay = intent.getIntExtra("day", 1);
        beforeHour = intent.getIntExtra("hour", calendar.get(Calendar.HOUR_OF_DAY + 1));
        beforeMinute = intent.getIntExtra("minute", 0);

        binding.beforeDateTextView.setOnClickListener(this);
        binding.beforeTimeTextView.setOnClickListener(this);
        binding.afterDateTextView.setOnClickListener(this);
        binding.afterTimeTextView.setOnClickListener(this);


    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.before_date_text_view:
                DatePickerDialog beforeDpd = new DatePickerDialog(WritablePersonalScheduleActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                        Log.d(TAG, "onDateSet: " + "i = " + i + ", i1 = " + i1 + ", i2 = " +i2);
                    }
                }, beforeYear, beforeMonth, beforeDay);
                beforeDpd.show();
                break;
            case R.id.before_time_text_view:
                TimePickerDialog beforeTpd = new TimePickerDialog(WritablePersonalScheduleActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int i, int i1) {
                        beforeHour = i;
                        beforeMinute = i1;
                        Log.d(TAG, "onDateSet: " + "i = " + i + ", i1 = " + i1);
                    }
                },beforeHour, beforeMinute, false);
                beforeTpd.show();
                break;
            case R.id.after_date_text_view:
                break;
            case R.id.after_time_text_view:
                break;
        }
    }
}