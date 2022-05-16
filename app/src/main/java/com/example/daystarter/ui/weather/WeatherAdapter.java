package com.example.daystarter.ui.weather;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.daystarter.R;

import java.util.ArrayList;

public class WeatherAdapter extends RecyclerView.Adapter{
    ArrayList<weatherData> item;

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_weather,parent,false);
        WeatherViewHolder weatherViewHolder = new WeatherViewHolder(v);
        return weatherViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        WeatherViewHolder wv =(WeatherViewHolder)holder;

        weatherData data =item.get(position);

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    class WeatherViewHolder extends RecyclerView.ViewHolder{

        TextView weather_day,weather_week,highest_temperature,lowest_temperature,humidity;

        public WeatherViewHolder(@NonNull View itemView) {
            super(itemView);
            weather_day=itemView.findViewById(R.id.weather_day);
            weather_week=itemView.findViewById(R.id.weather_week);
            highest_temperature =itemView.findViewById(R.id.highest_temperature);
            lowest_temperature =itemView.findViewById(R.id.lowest_temperature);
            humidity=itemView.findViewById(R.id.humidity);
        }
    }
}
