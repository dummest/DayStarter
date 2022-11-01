package com.example.daystarter.ui.weather;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;
import com.example.daystarter.R;
import java.util.ArrayList;
import java.util.Locale;

public class WeatherDayAdapter  extends RecyclerView.Adapter {
    ArrayList<weatherData> ArrayWeatherData;
    Context context;
    public WeatherDayAdapter(ArrayList<weatherData> arrayWeatherData, Context context) {
        this.ArrayWeatherData =arrayWeatherData;
        this.context = context;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.weatherday_row,parent,false);
        WeatherDayViewHolder weatherDayViewHolder = new WeatherDayViewHolder(v);
        return weatherDayViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Log.d("WeatherDayAdapter", "onBindViewHolder: ");
        weatherData weatherData = ArrayWeatherData.get(position);
        WeatherDayViewHolder wv = (WeatherDayViewHolder)holder;

       wv.weather_hour.setText(weatherData.getTime());
       wv.weather_temp.setText(String.format(Locale.getDefault(), "%.0f°C", weatherData.temp));
        wv.weather_temp_max.setText(String.format(Locale.getDefault(),"%.0f°C",weatherData.maxTemp));
        wv.weather_temp_min.setText(String.format(Locale.getDefault(),"%.0f°C",weatherData.minTemp));
        //weather을 가져와 적절한 아이콘 집어넣기
        if(weatherData.getDescription().equals("haze")){
            wv.weather_img.setImageResource(R.drawable.weatehr_rain);
        }
        if(weatherData.getDescription().equals("fog")){
            wv.weather_img.setImageResource(R.drawable.weatehr_rain);
        }
        else if (weatherData.getDescription().equals("clouds")){
            wv.weather_img.setImageResource(R.drawable.weather_clouds);
        }
        else if (weatherData.getDescription().equals("few clouds")){
            wv.weather_img.setImageResource(R.drawable.weather_windcloud);
        }
        else if (weatherData.getDescription().equals("scattered clouds")){
            wv.weather_img.setImageResource(R.drawable.weather_lowclouds);
        }
        else if (weatherData.getDescription().equals("broken clouds")){
            wv.weather_img.setImageResource(R.drawable.weather_moreclouds);
        }
        else if (weatherData.getDescription().equals("overcast clouds")){
            wv.weather_img.setImageResource(R.drawable.weather_moreclouds);
        }
        else if(weatherData.getDescription().equals("clear sky")){
            wv.weather_img.setImageResource(R.drawable.weather_clearsky);
        }
        else if(weatherData.getDescription().equals("moderate rain")){
            wv.weather_img.setImageResource(R.drawable.weatehr_rain);
        }
        else if(weatherData.getDescription().equals("Rain")){
            wv.weather_img.setImageResource(R.drawable.weatehr_rain);
        }else
            wv.weather_img.setImageResource(R.drawable.weather_clearsky);
    }

    @Override
    public int getItemCount() {
        return ArrayWeatherData.size();
    }


    class WeatherDayViewHolder extends RecyclerView.ViewHolder{
            CardView weather_CardView;
            TextView weather_hour,weather_temp,weather_temp_min,weather_temp_max;
            ImageView weather_img;

        public WeatherDayViewHolder(@NonNull View itemView) {
            super(itemView);
            weather_CardView =itemView.findViewById(R.id.weather_cardview);
            weather_hour = itemView.findViewById(R.id.weather_hour);
            weather_temp = itemView.findViewById(R.id.weather_temp);
            weather_temp_max= itemView.findViewById(R.id.weather_temp_max);
            weather_temp_min= itemView.findViewById(R.id.weather_temp_min);
            weather_img = itemView.findViewById(R.id.weatherDay_img);
        }
    }
}
