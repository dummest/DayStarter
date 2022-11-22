package com.example.daystarter.ui.home;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
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
import com.example.daystarter.MainActivity;
import com.example.daystarter.R;
import com.example.daystarter.ui.news.NewData;
import com.example.daystarter.ui.weather.RequestHttpUrlConnection;
import com.example.daystarter.ui.weather.WeatherData;
import com.example.daystarter.ui.weather.WeatherDayAdapter;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;

public class HomeFragment extends Fragment implements  OnBackPressedListener{
    @BindView(R.id.new_recyclerview) RecyclerView recyclerView;
    @BindView(R.id.home_schedule_recycler_view) RecyclerView homeScheduleRecyclerView;
    @BindView(R.id.weatherday_recyclerview) RecyclerView WeatherRecyclerView;

    private HomeNewAdapter newAdapter;
    WeatherDayAdapter weatherDayAdapter;
    ArrayList<NewData> items = new ArrayList<>();
    ArrayList<WeatherData> arrayWeatherData = new ArrayList<>();

    MainActivity mainActivity;
    LocationManager locationManager;
    private static final int REQUEST_CODE_LOCATION = 2;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_home, container, false);
        ButterKnife.bind(this,v);
        setHomeScheduleRecyclerView();
        setWeatherRecyclerView();
        setNewRecyclerView();
        mainActivity = (MainActivity) getActivity();
        locationManager =(LocationManager)getActivity().getSystemService(Context.LOCATION_SERVICE);
        MyLocation();

        return v;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        readRss();
    }

    void readRss() {
        Log.d("Rss", "startRSS");
        try {
            //URL url = new URL("https://rss.joins.com/sonagi/joins_sonagi_total_list.xml");
            URL url = new URL("https://rss.donga.com/total.xml");

            //네트워크 작업을 Thread 객체 생성해서 해결
            RssFeedTask task = new RssFeedTask();
            task.execute(url); //doInBackground()메소드가 발동[thread의 start()와 같은 역할]
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    //Thread class
    class RssFeedTask extends AsyncTask<URL, Void, String> {
        @Override
        protected String doInBackground(URL... urls) {
            Log.d("Thread", "thread");
            URL url = urls[0];
            try {
                InputStream is = url.openStream();
                //파싱해주는 객체 생성
                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                XmlPullParser xpp = factory.newPullParser();

                //인코딩 방식(한글)
                xpp.setInput(is, "utf-8");
                int eventType = xpp.getEventType();

                NewData item = null;
                String tagName = null;
                int j=0;
                while (eventType != XmlPullParser.END_DOCUMENT && j<2) {
                    switch (eventType) {
                        case XmlPullParser.START_DOCUMENT:
                            break;
                        case XmlPullParser.START_TAG:
                            tagName = xpp.getName();
                            Log.d("TAG", tagName);
                            if (tagName.equals("item")) {
                                item = new NewData();
                            } else if (tagName.equals("title")) {
                                xpp.next();
                                if (item != null) item.setTitle(xpp.getText());
                            } else if (tagName.equals("link")) {
                                xpp.next();
                                if (item != null) item.setLink(xpp.getText());
                            } else if (tagName.equals("description")) {
                                xpp.next();
                                Log.d("descripition", "description: ");
                                if (item != null) {
                                    String a = xpp.getText();
                                    String[] splitText = a.split(">");
                                    for (int i = 0; i < splitText.length; i++) {
                                        Log.d("split", "split: ");
                                        item.setDesc(splitText[i]);
                                    }
                                    //item.setDesc(xpp.getText());
                                }
                            } else if (tagName.equals("media:content")) {
                                Log.d("0", "attributeCount: " + xpp.getAttributeCount());
                                xpp.getAttributeValue(null, "url");
                                xpp.next();
                                if (item != null)
                                    item.setImgUrl(xpp.getAttributeValue(null, "url"));
                                Log.d("image", "image: " + xpp.getText());
                            } else if (tagName.equals("pubDate")) {
                                xpp.next();
                                if (item != null) item.setDate(xpp.getText());
                            }
                            break;
                        case XmlPullParser.TEXT:
                            break;
                        case XmlPullParser.END_TAG:
                            tagName = xpp.getName();
                            if (tagName.equals("item")) {
                                Log.d("RSS", item.getTitle());
                                //읽어온 기사 한개를 대량의 데이터에 추가
                                items.add(item);
                                item = null;

                                //ui변경사항
                                j++;
                                publishProgress();
                            }
                            break;
                    }
                    eventType = xpp.next();
                }


            } catch (IOException e) {
                e.printStackTrace();
            } catch (XmlPullParserException e) {
                e.printStackTrace();
            }
            //return 값은 onPostExecute에 들어간다
            return "파싱종료";
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
            //ui 변경시 여기서 작업
            newAdapter.notifyItemInserted(items.size());//새로 추가한 것은 마지막에 추가하는 내용
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
        }
    }
    @Override
    public void onBackPressed() {
        showDialog();
    }

    public void showDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("종료");
        builder.setMessage("종료하시겠습니까?");
        builder.setPositiveButton("아니오", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        builder.setNegativeButton("예", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mainActivity.finish();
            }
        });
        builder.show();
    }
    @Override
    public void onResume() {
        super.onResume();
        mainActivity.setOnBackPressedListener(this);
    }

    public void setHomeScheduleRecyclerView(){
        HomeScheduleAdapter adapter = new HomeScheduleAdapter(getContext());
        LinearLayoutManager manager = new LinearLayoutManager(getLayoutInflater().getContext());
        manager.setOrientation(RecyclerView.HORIZONTAL);
        homeScheduleRecyclerView.setLayoutManager(manager);
        homeScheduleRecyclerView.setAdapter(adapter);
    }

    public void setWeatherRecyclerView(){
        weatherDayAdapter = new WeatherDayAdapter(arrayWeatherData, getContext());
        LinearLayoutManager manger = new LinearLayoutManager(getLayoutInflater().getContext());
        manger.setOrientation(RecyclerView.HORIZONTAL);
        WeatherRecyclerView.setLayoutManager(manger);
        WeatherRecyclerView.setAdapter(weatherDayAdapter);
    }
    public void setNewRecyclerView(){
        newAdapter = new HomeNewAdapter(items,getContext());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(newAdapter);
    }

    private void MyLocation(){
        String Fine_location= Manifest.permission.ACCESS_FINE_LOCATION;
        String Coarse_location= Manifest.permission.ACCESS_COARSE_LOCATION;
        //위치 정보 권한
        if(ActivityCompat.checkSelfPermission(getActivity(), Fine_location)!= PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getActivity(),Coarse_location)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(getActivity(),new String[]{Manifest.permission.ACCESS_FINE_LOCATION},this.REQUEST_CODE_LOCATION);
            Log.d("get GPS", "Return MyLocation ");
        }
        else {
            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (location != null) {
                double longitude = location.getLongitude(); //경도
                double latitude = location.getLatitude(); //위도
                DayWeather(latitude,longitude);
                Log.d("location", "위도: "+longitude+" 경도: "+latitude);
            }
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    1000,
                    1,
                    gpsLocationListener);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                    1000,
                    1,
                    gpsLocationListener);
        }
    }
    final LocationListener gpsLocationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            // 위치 리스너는 위치정보를 전달할 때 호출되므로 onLocationChanged()메소드 안에 위지청보를 처리를 작업을 구현 해야합니다.
            double longitude = location.getLongitude(); // 위도
            double latitude = location.getLatitude(); // 경도
            DayWeather(latitude,longitude);
            Log.d("onLocationChanger", "위도 경도 교체 위도: "+ latitude +"경도 :"+ longitude);
            //txtResult.setText("위치정보 : " + provider + "\n" + "위도 : " + longitude + "\n" + "경도 : " + latitude + "\n" + "고도 : " + altitude);
        } public void onStatusChanged(String provider, int status, Bundle extras) {

        } public void onProviderEnabled(String provider) {

        } public void onProviderDisabled(String provider) {

        }
    };

    public void DayWeather(double latitude,double longitude) {
        Log.d("DayWeather", "DayWeather의 위도는: "+latitude+"경도는 :"+longitude);
        AndroidNetworking.get("https://api.openweathermap.org/data/2.5/forecast?lat="+latitude+"&lon="+longitude+"&units=metric&appid=7e818b3bfae91bb6fcbe3d382b6c3448")
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            Log.d("weather_onResponse", "onResponse_success: ");
                            JSONArray jsonArray = response.getJSONArray("list");
                            /*
                            시작시간으로 잡아보기 for(int i =0 ; i

                             */
                            for(int i =0;i<6;i++){
                                WeatherData weatherData = new WeatherData();
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

                                arrayWeatherData.add(weatherData);
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
