package com.example.daystarter.ui.weather;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.daystarter.R;

import java.util.ArrayList;
import java.util.Locale;

public class WeatherAdapter extends RecyclerView.Adapter{
    ArrayList<WeatherWeekData> ArrayData;

    public WeatherAdapter(ArrayList<WeatherWeekData> data, FragmentActivity activity){
        this.ArrayData = data;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_weather,parent,false);
        WeatherViewHolder weatherViewHolder = new WeatherViewHolder(v);
        return weatherViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Log.d("onBindViewHolder", "onBindViewHolder: ");
        WeatherViewHolder wv =(WeatherViewHolder)holder;
        WeatherWeekData weatherdata =ArrayData.get(position);

        wv.weather_week.setText(weatherdata.getNameDate());
        wv.highest_temperature.setText(String.format(Locale.getDefault(),"%.0f°C",weatherdata.MaxTemp));
        wv.lowest_temperature.setText(String.format(Locale.getDefault(),"%.0f°C",weatherdata.MinTemp));


         //weather을 가져와 적절한 아이콘 집어넣기
        if(weatherdata.getWeather().equals("fog")){
            wv.weather_img.setText("안개");
        }
        else if (weatherdata.getWeather().equals("clouds")){
            wv.weather_img.setText("구름");
        }
        else if (weatherdata.getWeather().equals("few clouds")){
            wv.weather_img.setText("구름 조금");
        }
        else if (weatherdata.getWeather().equals("scattered clouds")){
            wv.weather_img.setText("구름 낌");
        }
        else if (weatherdata.getWeather().equals("broken clouds")){
            wv.weather_img.setText("구름 많음");
        }
        else if (weatherdata.getWeather().equals("overcast clouds")){
            wv.weather_img.setText("구름 많음");
        }
        else if(weatherdata.getWeather().equals("clear sky")){
            wv.weather_img.setText("맑음");
        }
        else
            wv.weather_img.setText("");
    }

    @Override
    public int getItemCount() {
        return ArrayData.size();
    }

    class WeatherViewHolder extends RecyclerView.ViewHolder{

        TextView weather_week,highest_temperature,lowest_temperature,weather_img;

        public WeatherViewHolder(@NonNull View itemView) {
            super(itemView);
            weather_week=itemView.findViewById(R.id.weather_week);
            highest_temperature =itemView.findViewById(R.id.highest_temperature);
            lowest_temperature =itemView.findViewById(R.id.lowest_temperature);
            weather_img =itemView.findViewById(R.id.weather_img);
        }
    }

}
