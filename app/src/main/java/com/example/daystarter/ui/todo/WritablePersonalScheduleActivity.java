package com.example.daystarter.ui.todo;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TimePicker;

import com.example.daystarter.R;
import com.example.daystarter.databinding.ActivityWritablePersonalScheduleBinding;
import com.example.daystarter.myClass.PersonalScheduleDBHelper;
import com.example.daystarter.myClass.ScheduleData;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

public class WritablePersonalScheduleActivity extends AppCompatActivity implements View.OnClickListener {
    private ActivityWritablePersonalScheduleBinding binding;
    Calendar beforeCalendar = new GregorianCalendar(), afterCalendar = new GregorianCalendar();
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy년 MM월 dd일", Locale.getDefault());
    SimpleDateFormat timeFormat = new SimpleDateFormat("HH시 mm분", Locale.getDefault());
    int scheduleId = -1;


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
        /*
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
    */
        beforeCalendar.setTimeInMillis(intent.getLongExtra("beforeLong", Calendar.getInstance().getTimeInMillis()));
        afterCalendar.setTimeInMillis(intent.getLongExtra("afterLong", beforeCalendar.getTimeInMillis()));
        scheduleId = intent.getIntExtra("scheduleId", -1);

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
        if(view.getId() == R.id.before_date_text_view){
            new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                    setBeforeDate(new GregorianCalendar(i, i1, i2, beforeCalendar.get(Calendar.HOUR_OF_DAY), beforeCalendar.get(Calendar.MINUTE)));
                }
            }, beforeCalendar.get(Calendar.YEAR), beforeCalendar.get(Calendar.MONTH), beforeCalendar.get(Calendar.DAY_OF_MONTH)).show();
        }
        else if(view.getId() == R.id.after_date_text_view){
            new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                    setAfterDate(new GregorianCalendar(i, i1, i2, afterCalendar.get(Calendar.HOUR_OF_DAY), afterCalendar.get(Calendar.MINUTE)));
                }
            }, afterCalendar.get(Calendar.YEAR), afterCalendar.get(Calendar.MONTH), afterCalendar.get(Calendar.DAY_OF_MONTH)).show();
        }

        else if(view.getId() == R.id.before_time_text_view) {
            new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
                @Override
                public void onTimeSet(TimePicker timePicker, int i, int i1) {
                    setBeforeDate(new GregorianCalendar(beforeCalendar.get(Calendar.YEAR), beforeCalendar.get(Calendar.MONTH), beforeCalendar.get(Calendar.DAY_OF_MONTH), i, i1));
                }
            }, beforeCalendar.get(Calendar.HOUR_OF_DAY), beforeCalendar.get(Calendar.MINUTE), false).show();
        }

        else if(view.getId() == R.id.after_time_text_view){
            new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
                @Override
                public void onTimeSet(TimePicker timePicker, int i, int i1) {
                    setAfterDate(new GregorianCalendar(afterCalendar.get(Calendar.YEAR), afterCalendar.get(Calendar.MONTH), afterCalendar.get(Calendar.DAY_OF_MONTH), i, i1));
                }
            }, afterCalendar.get(Calendar.HOUR_OF_DAY), afterCalendar.get(Calendar.MINUTE), false).show();
        }

        else if(view.getId() == R.id.cancel_button){
            finish();
        }

        else if(view.getId() == R.id.save_button){
            ScheduleData data = new ScheduleData(
                    scheduleId,
                    binding.titleEditText.getText().toString(),
                    beforeCalendar.getTimeInMillis(),
                    afterCalendar.getTimeInMillis(),
                    binding.memoEditText.getText().toString(),
                    binding.locationEditText.getText().toString(),
                    binding.attachFileTextView.getText().toString());

            if(data.getScheduleId() == -1) //id로 편집인지 생성인지 판단
                saveNewSchedule(data);
            else
                editSchedule(data);
        }
    }

    public void saveNewSchedule(ScheduleData data){
        PersonalScheduleDBHelper myDBHelper = new PersonalScheduleDBHelper(getBaseContext());
        myDBHelper.insertSchedule(data);
        finish();
    }

    public void editSchedule(ScheduleData data) {
        PersonalScheduleDBHelper myDBHelper = new PersonalScheduleDBHelper(getBaseContext());
        myDBHelper.updateSchedule(data);
        finish();
    }
}