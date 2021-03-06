package com.example.daystarter.ui.weather;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.bumptech.glide.Glide;
import com.example.daystarter.R;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import kotlin.jvm.internal.Intrinsics;

public class WeatherFragment extends Fragment {
    //@BindView(R.id.tv_name) TextView tv_name;
    //@BindView(R.id.tv_country) TextView tv_country;
    @BindView(R.id.weather_recyclerview) RecyclerView recyclerView;
    @BindView(R.id.weatherday_recyclerview)RecyclerView DayRecyclerView;
    @BindView(R.id.tv_temp)TextView tv_temp;
    @BindView(R.id.tv_description)TextView tv_description;
    @BindView(R.id.iv_weather)ImageView iv_weather;
    Context context;

    TextView tv_wind, tv_cloud, tv_humidity,tv_name, tv_country;
    ArrayList<weatherData> ArrayWeatherData = new ArrayList<>();
    ArrayList<WeatherWeekData> weatherWeekData = new ArrayList<>();
    WeatherAdapter weatherAdapter;
    WeatherDayAdapter weatherDayAdapter;
    LocationManager locationManager;
    ProgressDialog progressDialog;
    private static final int REQUEST_CODE_LOCATION = 2;
    double lat = 0;
    double lng = 0;

    String strUrl = "https://api.openweathermap.org/data/2.5/weather";  //????????? URL
    NetworkTask networkTask = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v;
        v = inflater.inflate(R.layout.fragment_weather, container, false);
        ButterKnife.bind(this, v);
        weatherDayAdapter = new WeatherDayAdapter(ArrayWeatherData, getActivity());
        DayRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(),RecyclerView.HORIZONTAL,false));
        DayRecyclerView.setAdapter(weatherDayAdapter);

        weatherAdapter = new WeatherAdapter(weatherWeekData, getActivity());
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(weatherAdapter);
        locationManager =(LocationManager)getActivity().getSystemService(Context.LOCATION_SERVICE);
        Location location = MyLocation();
        if(location != null){
            lat = location.getLatitude();
            lng = location.getLongitude();
            Log.d("lng", "lng: "+lng);
        }
        DayWeather();
        getLocation();

        //tv_name = (TextView) v.findViewById(R.id.tv_name);
        //tv_country = (TextView) v.findViewById(R.id.tv_country);
        //tv_main = (TextView) v.findViewById(R.id.tv_main);

        /*
         ?????? ???????????? ?????? ?????? ????????? ????????????????????? bug??? ?????? ?????? ??????
        locationManager = (LocationManager)getActivity().getSystemService(Context.LOCATION_SERVICE);
        @SuppressLint("MissingPermission") Location current = locationManager. getLastKnownLocation(LocationManager.GPS_PROVIDER);
        //?????? ??????
        lat =current.getLongitude();
        //??????
        lng = current.getLatitude();
        */
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        progressDialog.show();
        //DayWeather();
        //????????? ??????
        //getLocation();
        //?????? ??????
        return v;
    }
    private Location MyLocation(){
        Location MyLocation = null;
        String Fine_location=Manifest.permission.ACCESS_FINE_LOCATION;
        String Coarse_location= Manifest.permission.ACCESS_COARSE_LOCATION;
        //?????? ?????? ??????
        if(ActivityCompat.checkSelfPermission(getActivity(), Fine_location)!= PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getActivity(),Coarse_location)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(getActivity(),new String[]{Manifest.permission.ACCESS_FINE_LOCATION},this.REQUEST_CODE_LOCATION);
            MyLocation();
        }
        else{
            String locationProvider=LocationManager.GPS_PROVIDER;
            MyLocation= locationManager.getLastKnownLocation(locationProvider);
            if(MyLocation != null){
                double leg= MyLocation.getLongitude();
                double lat = MyLocation.getLatitude();
            }
        }
        return MyLocation;
    }


    /* NetworkTask ??? ???????????? ?????? ????????? */
    public void requestNetwork() {
        ContentValues values = new ContentValues();
        //????????? =????????????(????????? ????????? ????????? ??????????????????.)
        values.put("q", "Seoul");
        values.put("appid", getString(R.string.weather_app_id));

        networkTask = new NetworkTask(context, strUrl, values);
        networkTask.execute();
    }


    /* ????????? ????????? ?????? AsyncTask ????????? NetworkTask ????????? */
    public class NetworkTask extends AsyncTask<Void, Void, String> {
        Context context;
        String url = "";
        ContentValues values;

        public NetworkTask(Context context, String url, ContentValues values) {
            this.context = context;
            this.url = url;
            this.values = values;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Void... params) {
            String result = "";

            RequestHttpUrlConnection requestHttpUrlConnection = new RequestHttpUrlConnection();
            result = requestHttpUrlConnection.request(url, values, "GET");  //HttpURLConnection ?????? ??????

            Log.d("weather", "NetworkTask" + result);
            return result;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }

        @Override
        protected void onPostExecute(String result) {
            Log.d("weather", "onPostExecute()" + result);

            if (result != null && !result.equals("")) {
                //??????
                JsonParser jp = new JsonParser();
                JsonObject jsonObject = (JsonObject) jp.parse(result);
                JsonObject jsonObjectSys = (JsonObject) jp.parse(jsonObject.get("sys").getAsJsonObject().toString());
                JsonObject jsonObjectWeather = (JsonObject) jp.parse(jsonObject.get("weather").getAsJsonArray().get(0).toString());
                JsonObject jsonObjectMain = (JsonObject) jp.parse(jsonObject.get("main").getAsJsonObject().toString());
                JsonObject jsonObjectWind = (JsonObject) jp.parse(jsonObject.get("wind").getAsJsonObject().toString());
                JsonObject jsonObjectClouds = (JsonObject) jp.parse(jsonObject.get("clouds").getAsJsonObject().toString());

                weatherData model = new weatherData();
                //???????????? ????????? ??????
                String description = jsonObjectWeather.get("description").toString().replaceAll("\"", "");
                description = transferWeather(description);
                model.setName(jsonObject.get("name").toString().replaceAll("\"", ""));
                model.setCountry(jsonObjectSys.get("country").toString().replaceAll("\"", ""));
                Log.d("Icon", "Icon: " + getString(R.string.weather_url) + "img/w/" + jsonObjectWeather.get("icon").toString().replaceAll("\"", "") + ".png");
                model.setIcon(getString(R.string.weather_url) + "img/w/" + jsonObjectWeather.get("icon").toString().replaceAll("\"", "") + ".png");
                model.setTemp(jsonObjectMain.get("temp").getAsDouble() - 273.15);
                model.setMain(jsonObjectWeather.get("main").toString().replaceAll("\"", ""));
                model.setDescription(description);
                model.setWind(jsonObjectWind.get("speed").getAsDouble());
                model.setClouds(jsonObjectClouds.get("all").getAsDouble());
                model.setHumidity(jsonObjectMain.get("humidity").getAsDouble());

                setWeatherData(model);  //UI ????????????

            } else {
                showFailPop();
            }
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }

    }  //NetworkTask End

    /* ???????????? ????????? ?????? ???????????? ?????? UI ???????????? ????????? */
    private void setWeatherData(weatherData model) {
        Log.d("Weather", "setWeatherData");
        //tv_name.setText(model.getName());
        //tv_country.setText(model.getCountry());
        Glide.with(this).load(model.getIcon())  //Glide ?????????????????? ???????????? ImageView ??? url ??? ????????? ??????
                //.placeholder(R.drawable.icon_image)
                //.error(R.drawable.icon_image)
                .into(iv_weather);
        tv_temp.setText(doubleToStrFormat(2, model.getTemp()) + " 'C");  //????????? 2?????? ???????????? ???????????????
        //tv_main.setText(model.getMain());
        tv_description.setText(model.getDescription());
        //tv_wind.setText(doubleToStrFormat(2, model.getWind()) + " m/s");
        //tv_cloud.setText(doubleToStrFormat(2, model.getClouds()) + " %");
        //tv_humidity.setText(doubleToStrFormat(2, model.getHumidity()) + " %");
    }


    /* ?????? ????????? AlertDialog ???????????? ????????? */
    private void showFailPop() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Title").setMessage("????????????");

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                Toast.makeText(getActivity(), "OK Click", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                Toast.makeText(getActivity(), "Cancel Click", Toast.LENGTH_SHORT).show();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }


    /* ????????? n?????? ???????????? ??????????????? */
    private String doubleToStrFormat(int n, double value) {
        return String.format("%." + n + "f", value);
    }

    private String transferWeather(String weather) {
        weather = weather.toLowerCase();
        if (weather.equals("haze"))
            return "??????";
        else if (weather.equals("fog"))
            return "??????";
        else if (weather.equals("clouds"))
            return "??????";
        else if (weather.equals("few clouds"))
            return "?????? ??????";
        else if (weather.equals("scattered clouds"))
            return "?????? ???";
        else if (weather.equals("broken clouds"))
            return "?????? ??????";
        else if (weather.equals("overcast clouds"))
            return "?????? ??????";
        else if (weather.equals("clear sky"))
            return "??????";
        else if(weather.equals("moderate rain"))
            return  "???";
        return "";
    }

    private void getLocation() {
        Log.d("getLocation", "getLocation: ");
        //https://api.openweathermap.org/data/2.5/onecall?lat=37.5683&lon=126.977&exclude=current,minutely,hourly,alerts&units=metric&appid=7e818b3bfae91bb6fcbe3d382b6c3448
        requestNetwork();
        // android-networking ?????? ????????? ?????? ?????? ?????????(????????? ??????)
        AndroidNetworking.get("https://api.openweathermap.org/data/2.5/onecall?lat="+lat+"&lon="+lng+"&exclude=current,minutely,hourly,alerts&units=metric&appid=7e818b3bfae91bb6fcbe3d382b6c3448")
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Intrinsics.checkNotNullParameter(response, "response");
                        Log.d("onResponse", "onResponse:");
                        try {
                            //daily ???????????? ??????
                            JSONArray jsonArray = response.getJSONArray("daily");
                            for (int i = 0; i < jsonArray.length(); i++) {
                                WeatherWeekData data = new WeatherWeekData();
                                JSONObject objectList = jsonArray.getJSONObject(i);
                                //daily ?????? ?????? temp??? ?????? ?????? ??????
                                JSONObject jsonObjectOne = objectList.getJSONObject("temp");
                                JSONArray jsonArrayOne = objectList.getJSONArray("weather");
                                JSONObject jsonObjectTwo = jsonArrayOne.getJSONObject(0);
                                //?????????????????? xx???
                                long Day = objectList.optLong("dt");
                                SimpleDateFormat formatDate = new java.text.SimpleDateFormat("dd???");
                                String readableDate = formatDate.format(new java.util.Date(Day * (long) 1000));
                                //?????? ???????????? ex) ?????????
                                long weekday = objectList.optLong("dt");
                                SimpleDateFormat format = new SimpleDateFormat("EEEE");
                                String readableDay = format.format(new Date(weekday * (long) 1000));

                                //???????????? ??????+??????, ?????? ??????,????????????,????????????
                                Log.d("weather", "setWeatherData: "+jsonObjectTwo.getString("description"));
                                data.setNameDate(readableDate + ' ' + readableDay);
                                data.setWeather(jsonObjectTwo.getString("description"));
                                data.setMinTemp(jsonObjectOne.getDouble("min"));
                                data.setMaxTemp(jsonObjectOne.getDouble("max"));

                                Log.d("weatherData", "addData: ");
                                weatherWeekData.add(data);
                            }
                            weatherAdapter.notifyDataSetChanged();

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        Toast.makeText(getActivity(), "???????????? ???????????? ??????????????????.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void setWeekWeatherData(weatherData model) {
        Log.d("Weather", "setWeatherData");
        tv_name.setText(model.getName());
        tv_country.setText(model.getCountry());

    }

    private void DayWeather() {
        Log.d("DayWeather", "DayWeather: ");
        AndroidNetworking.get("https://api.openweathermap.org/data/2.5/forecast?lat="+lat+"&lon="+lng+"&units=metric&appid=7e818b3bfae91bb6fcbe3d382b6c3448")
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
                                //????????????
                                weatherData.setTime(CurrentTime);
                                //?????? ??????
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
                        progressDialog.dismiss();
                    }

                    @Override
                    public void onError(ANError anError) {

                    }
                });
    }





}