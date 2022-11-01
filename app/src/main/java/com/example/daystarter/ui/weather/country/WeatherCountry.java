package com.example.daystarter.ui.weather.country;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.daystarter.R;
import com.example.daystarter.ui.alarm.alarmslist.AlarmRecyclerViewAdapter;
import com.example.daystarter.ui.alarm.alarmslist.AlarmsListViewModel;
import com.example.daystarter.ui.alarm.createalarm.CreateAlarmViewModel;
import com.example.daystarter.ui.alarm.data.Alarm;
import com.example.daystarter.ui.weather.country.data.WeatherRepository;
import com.example.daystarter.ui.weather.country.data.WeatherViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;


public class WeatherCountry extends Fragment {
    @BindView(R.id.editText_Weather) EditText edit;
    @BindView(R.id.search_weather_button) Button button;
    //@BindView(R.id.weather_area_recycler_view) RecyclerView weatherRecyclerView;
    /*
    @BindView(R.id.weather_area_weather)ImageView weather_area_weather;
    @BindView(R.id.weather_area_textview)TextView weather_area_textview;
    @BindView(R.id.weather_area_temp)TextView weather_area_temp;
   @BindView(R.id.weather_area_description)TextView weather_area_description;
     */
    private  Context context;
    private static final String TAG = "WeatherCountry";
    private WeatherViewModel weatherViewModel;
    private ArrayList<WeatherAreaData> ArrayWeatherData = new ArrayList<WeatherAreaData>();
    String strUrl = "https://api.openweathermap.org/data/2.5/weather";  //통신할 URL
    WeatherAreaAdapter weatherAreaAdapter;
    //NetworkTask networkTask = null;

    double lat;
    double lng;
    String  area,areas;
    RecyclerView recyclerView;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        weatherAreaAdapter = new  WeatherAreaAdapter(ArrayWeatherData,context);

        weatherViewModel = new ViewModelProvider(this).get(WeatherViewModel.class);
        weatherViewModel.getWeatherLiveData().observe(this, new Observer<List<WeatherAreaData>>() {
            @Override
            public void onChanged(List<WeatherAreaData> weatherAreaData) {
                    weatherAreaAdapter.setWeathers(weatherAreaData);
                    recyclerView.startLayoutAnimation();

            }
        });
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_weather_country, container, false);
        ButterKnife.bind(this, v);
        recyclerView=v.findViewById(R.id.weather_area_recycler_view);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(weatherAreaAdapter);
        Log.d(TAG, "onCreateView: ");

        //Location location = new Location("");
        Geocoder geocoder = new Geocoder(context);


        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                List<Address> list = null;
                area = edit.getText().toString(); //지역
                areas=edit.getText().toString();



                try {
                    list = geocoder.getFromLocationName(area,10);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if(list !=null){
                    if(list.size() ==0){
                        Toast.makeText(context, "해당 주소 정보를 가져 오지 못했습니다.", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        Address address = list.get(0);
                        lat=address.getLatitude();
                        lng = address.getLongitude();
                        area=changeName(area);
                        int WeatherId = new Random().nextInt(Integer.MAX_VALUE);
                        WeatherAreaData weatherAreaData = new WeatherAreaData(
                                WeatherId,
                                area,
                                areas,
                                lat,
                                lng
                        );
                        WeatherAreaData data =new WeatherAreaData(WeatherId,area,areas,lat,lng);
                        ArrayWeatherData.add(data);
                        weatherAreaAdapter.notifyItemInserted(ArrayWeatherData.size());
                        weatherViewModel.insert(weatherAreaData);

                        Log.d(TAG, "weatherAreaAdapter.notifyItemInserted ");
                        //location.setLatitude(lat);
                        //location.setLongitude(lon);
                        //requestNetwork();

                        Log.d(TAG, "lat= "+address.getLatitude() +"long= "+address.getLongitude());
                    }
                }
            }
        });
        return v;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context= context;
    }

    public String changeName(String name){
        if(name.contains("서울"))
            return "Seoul";
        else if(name.contains("인천"))
            return "Incheon";
        else if(name.contains("대구"))
            return "Daegu";
        else if(name.contains("제주도"))
            return "Jeju";
        else if(name.contains("대전"))
            return "Daejeon";
        else if(name.contains("경기도"))
            return "Gyeonggi-do";
        else if(name.contains("부산"))
            return "Busan";
        else if(name.contains("광주"))
            return "Gwangju";
        else if(name.contains("울산"))
            return "Ulsan";
        else if(name.contains("강원도"))
            return "Gangwon-do";
        else {
            return "Seoul"; //기본값
        }
    }


}