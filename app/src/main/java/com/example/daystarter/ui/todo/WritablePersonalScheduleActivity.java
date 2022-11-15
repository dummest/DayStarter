package com.example.daystarter.ui.todo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TimePicker;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.example.daystarter.R;
import com.example.daystarter.databinding.ActivityWritablePersonalScheduleBinding;
import com.example.daystarter.myClass.PersonalScheduleDBHelper;
import com.example.daystarter.myClass.ScheduleData;
import com.example.daystarter.ui.weather.ProgressDialog;
import com.example.daystarter.ui.weather.WeatherAdapter;
import com.example.daystarter.ui.weather.WeatherDayAdapter;
import com.example.daystarter.ui.weather.WeatherFragment;
import com.example.daystarter.ui.weather.WeatherWeekData;
import com.example.daystarter.ui.weather.country.WeatherAreaActivity;
import com.example.daystarter.ui.weather.weatherData;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

public class WritablePersonalScheduleActivity extends AppCompatActivity implements View.OnClickListener {
    private ActivityWritablePersonalScheduleBinding binding;
    Calendar beforeCalendar = new GregorianCalendar(), afterCalendar = new GregorianCalendar();
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy년 MM월 dd일", Locale.getDefault());
    SimpleDateFormat timeFormat = new SimpleDateFormat("HH시 mm분", Locale.getDefault());
    int scheduleId = -1;
    double latitude,longitude;
    WeatherFragment weatherFragment;
    public ArrayList<weatherData> ArrayWeatherData = new ArrayList<>();
    WeatherDayAdapter weatherDayAdapter;


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

        weatherDayAdapter = new WeatherDayAdapter(ArrayWeatherData, getApplicationContext());
        binding.weatherdayRecyclerview.setLayoutManager(new LinearLayoutManager(getApplicationContext(), RecyclerView.HORIZONTAL,false));
        binding.weatherdayRecyclerview.setAdapter(weatherDayAdapter);



        /*
        weatherFragment.weatherDayAdapter = new WeatherDayAdapter(weatherFragment.ArrayWeatherData, getApplicationContext());
        binding.weatherdayRecyclerview.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        binding.weatherdayRecyclerview.setAdapter(weatherFragment.weatherDayAdapter);
         */
    }
    public void setFirst(){
        Intent intent = getIntent();
        scheduleId = intent.getIntExtra("scheduleId", -1);
        Log.d(TAG, "schedule id = " + scheduleId);

        if(scheduleId == -1){
            beforeCalendar.setTimeInMillis(intent.getLongExtra("beforeLong", Calendar.getInstance().getTimeInMillis()));
            afterCalendar.setTimeInMillis(intent.getLongExtra("afterLong", beforeCalendar.getTimeInMillis()));
            setBeforeDate(beforeCalendar);
            setAfterDate(afterCalendar);
        }
        else{
            PersonalScheduleDBHelper dbHelper = new PersonalScheduleDBHelper(this);
            ScheduleData scheduleData = dbHelper.getSchedule(scheduleId);

            binding.titleEditText.setText(scheduleData.getTitle());
            beforeCalendar.setTimeInMillis(scheduleData.getStartTime());
            afterCalendar.setTimeInMillis(scheduleData.getEndTime());
            setBeforeDate(beforeCalendar);
            setAfterDate(afterCalendar);
            binding.contentsEditText.setText(scheduleData.getMemo());
            binding.weatherEditText.setText(scheduleData.getAddress());
            latitude=scheduleData.getLatitude();
            longitude=scheduleData.getLongitude();
            DayWeather(latitude,longitude);
        }

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

        ///////////////////////////////////////////////
        else if(view.getId() == R.id.save_button) {
            Geocoder geocoder = new Geocoder(this);
            List<Address> list = null;
            String area = binding.weatherEditText.getText().toString();
            try {
                list = geocoder.getFromLocationName(area, 10);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (list != null) {
                if (list.size() == 0) {
                    Toast.makeText(getApplicationContext(), "해당 주소 정보를 가져 오지 못했습니다.", Toast.LENGTH_SHORT).show();
                } else {
                    Address address = list.get(0);
                    latitude = address.getLatitude();
                    longitude = address.getLongitude();
                    Toast.makeText(getApplicationContext(), area + "의 위도는 " + latitude + "이고 경도는 " + longitude + "이다", Toast.LENGTH_SHORT).show();
                }
            }
            ScheduleData data = new ScheduleData(
                    scheduleId,
                    binding.titleEditText.getText().toString(),
                    beforeCalendar.getTimeInMillis(),
                    afterCalendar.getTimeInMillis(),
                    binding.contentsEditText.getText().toString(),
                    area, //주소
                    latitude,
                    longitude,
                    null);
            //////////////////////////////////////////////
            Log.d(TAG, "onClick_data: 위도: " + latitude + " 경도: " + longitude);
            if (data.getScheduleId() == -1){ //id로 편집인지 생성인지 판단
                saveNewSchedule(data);
            }
            else {
                editSchedule(data);
            }
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

    public void DayWeather(double latitude,double longitude) {
        Log.d("DayWeather", "DayWeather의 위도는: "+latitude+"경도는 :"+longitude);
        https://api.openweathermap.org/data/2.5/forecast?lat=37.2635727&lon=127.0286009&units=metric&appid=7e818b3bfae91bb6fcbe3d382b6c3448
        AndroidNetworking.get("https://api.openweathermap.org/data/2.5/forecast?lat="+latitude+"&lon="+longitude+"&units=metric&appid=7e818b3bfae91bb6fcbe3d382b6c3448")
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            Log.d("weather_onResponse", "onResponse_success: ");
                            JSONArray jsonArray = response.getJSONArray("list");
                            for(int i =0;i<6;i++){
                                weatherData weatherData = new weatherData();
                                JSONObject list = jsonArray.getJSONObject(i);
                                JSONObject Main = list.getJSONObject("main");
                                JSONArray MainArray = list.getJSONArray("weather");
                                JSONObject Weather = MainArray.getJSONObject(0);
                                String CurrentTime = list.getString("dt_txt");
                                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                SimpleDateFormat formatTime = new SimpleDateFormat("kk:mm");

                                try{
                                    Date time = format.parse(CurrentTime);
                                    CurrentTime =formatTime.format(time);
                                }
                                catch (ParseException e){
                                    e.printStackTrace();
                                }
                                Log.d("onResponse", "onResponse_addData: ");
                                //현재시간

                                weatherData.setTime(CurrentTime);
                                //평균 온도
                                weatherData.setTemp(Main.getDouble("temp"));
                                weatherData.setDescription(Weather.getString("description"));
                                weatherData.setMinTemp(Main.getDouble("temp_min"));
                                weatherData.setMaxTemp(Main.getDouble("temp_max"));

                                ArrayWeatherData.add(weatherData);
                            }
                            weatherDayAdapter.notifyDataSetChanged();


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(ANError anError) {

                    }
                });
    }




}