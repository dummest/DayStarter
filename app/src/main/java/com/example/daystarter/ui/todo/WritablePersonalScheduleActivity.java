package com.example.daystarter.ui.todo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;

import com.example.daystarter.R;
import com.example.daystarter.databinding.ActivityWritablePersonalScheduleBinding;
import com.google.android.material.snackbar.Snackbar;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class WritablePersonalScheduleActivity extends AppCompatActivity implements View.OnClickListener {
    private ActivityWritablePersonalScheduleBinding binding;
    Calendar beforeCalendar, afterCalendar;
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy년 MM월 dd일");
    SimpleDateFormat timeFormat = new SimpleDateFormat("HH시 mm분");

    private final String TAG = "ActivityWritablePersonalScheduleBinding";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWritablePersonalScheduleBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setFirst();


        binding.beforeDateTextView.setOnClickListener(this);
        binding.beforeTimeTextView.setOnClickListener(this);
        binding.afterDateTextView.setOnClickListener(this);
        binding.afterTimeTextView.setOnClickListener(this);
        binding.cancelButton.setOnClickListener(this);
        binding.saveButton.setOnClickListener(this);
    }
    public void setFirst(){
        Intent intent = getIntent();
                beforeCalendar = new GregorianCalendar(
                intent.getIntExtra("beforeYear", 2022),
                intent.getIntExtra("beforeMonth", 1),
                intent.getIntExtra("beforeDay", 1),
                intent.getIntExtra("beforeHour", 0),
                intent.getIntExtra("beforeMinute", 0));

                afterCalendar = new GregorianCalendar(
                intent.getIntExtra("afterYear", beforeCalendar.get(Calendar.YEAR)),
                intent.getIntExtra("afterMonth", beforeCalendar.get(Calendar.MONTH)),
                intent.getIntExtra("afterDay", beforeCalendar.get(Calendar.DAY_OF_MONTH)),
                intent.getIntExtra("afterHour", beforeCalendar.get(Calendar.HOUR)),
                intent.getIntExtra("afterMinute", beforeCalendar.get(Calendar.MINUTE)));

        setBeforeDate(beforeCalendar);
        setAfterDate(afterCalendar);
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

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.before_date_text_view:
                new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                        setBeforeDate(new GregorianCalendar(i, i1, i2, beforeCalendar.get(Calendar.HOUR_OF_DAY), beforeCalendar.get(Calendar.MINUTE)));
                    }
                }, beforeCalendar.get(Calendar.YEAR), beforeCalendar.get(Calendar.MONTH), beforeCalendar.get(Calendar.DAY_OF_MONTH)).show();
                break;
            case R.id.after_date_text_view:
                new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                        setAfterDate(new GregorianCalendar(i, i1, i2, afterCalendar.get(Calendar.HOUR_OF_DAY), afterCalendar.get(Calendar.MINUTE)));
                    }
                }, afterCalendar.get(Calendar.YEAR), afterCalendar.get(Calendar.MONTH), afterCalendar.get(Calendar.DAY_OF_MONTH)).show();
                break;
            case R.id.before_time_text_view:
                new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int i, int i1) {
                        setBeforeDate(new GregorianCalendar(beforeCalendar.get(Calendar.YEAR), beforeCalendar.get(Calendar.MONTH), beforeCalendar.get(Calendar.DAY_OF_MONTH), i, i1));
                    }
                }, beforeCalendar.get(Calendar.HOUR_OF_DAY), beforeCalendar.get(Calendar.MINUTE), false).show();
                break;
            case R.id.after_time_text_view:
                new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int i, int i1) {
                        setAfterDate(new GregorianCalendar(afterCalendar.get(Calendar.YEAR), afterCalendar.get(Calendar.MONTH), afterCalendar.get(Calendar.DAY_OF_MONTH), i, i1));
                    }
                }, beforeCalendar.get(Calendar.HOUR_OF_DAY), beforeCalendar.get(Calendar.MINUTE), false).show();
                break;
            case R.id.cancel_button:
                finish();
                break;
            case R.id.save_button:
                Snackbar.make(binding.saveButton, "저장(미구현)", Snackbar.LENGTH_SHORT).show();
                finish();
        }
    }
}