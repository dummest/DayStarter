package com.example.daystarter.ui.home;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.daystarter.LoginActivity;
import com.example.daystarter.MainActivity;
import com.example.daystarter.R;
import com.example.daystarter.SignUpActivity;
import com.example.daystarter.ui.groupSchedule.GroupActivity;
import com.example.daystarter.ui.news.NewAdapter;
import com.example.daystarter.ui.news.NewData;
import com.example.daystarter.ui.news.NewsFragment;
import com.example.daystarter.ui.weather.RequestHttpUrlConnection;
import com.example.daystarter.ui.weather.WeatherFragment;
import com.example.daystarter.ui.weather.WeatherWeekData;
import com.example.daystarter.ui.weather.weatherData;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.zip.Inflater;

import butterknife.BindView;
import butterknife.ButterKnife;
import kotlin.jvm.internal.Intrinsics;

public class HomeFragment extends Fragment implements  OnBackPressedListener{
    @BindView(R.id.iv_weather)ImageView iv_weather;
    @BindView(R.id.tv_temp)TextView tv_temp;
    @BindView(R.id.tv_description)TextView tv_description;
    @BindView(R.id.new_recyclerview) RecyclerView recyclerView;
    @BindView(R.id.home_schedule_recycler_view) RecyclerView homeScheduleRecyclerView;

    private HomeNewAdapter newAdapter;
    ArrayList<NewData> items = new ArrayList<>();
    String strUrl = "https://api.openweathermap.org/data/2.5/weather";  //통신할 URL
    NetworkTask networkTask = null;
    Context context;
    private long back_time = 0;
    private long Finish_back_time=2000;
    MainActivity mainActivity;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        //LayoutInflater layoutInflater = (LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.fragment_home, container, false);
        ButterKnife.bind(this,v);
        setHomeScheduleRecyclerView();
        newAdapter = new HomeNewAdapter(items,getActivity());
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(newAdapter);
        //tv_name = (TextView) v.findViewById(R.id.tv_name);
        //tv_country = (TextView) v.findViewById(R.id.tv_country);
        //tv_main = (TextView) v.findViewById(R.id.tv_main);
        mainActivity = (MainActivity) getActivity();
        requestNetwork();
        return v;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        readRss();
    }

    /* NetworkTask 를 요청하기 위한 메소드 */
    private void requestNetwork() {
        ContentValues values = new ContentValues();
        //기본값 =서울날씨(바꾸고 싶으면 여기서 교채하면된다.)
        values.put("q", "Seoul");
        values.put("appid", getString(R.string.weather_app_id));

        networkTask = new NetworkTask(context, strUrl, values);
        networkTask.execute();
    }


    /* 비동기 처리를 위해 AsyncTask 상속한 NetworkTask 클래스 */
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
            result = requestHttpUrlConnection.request(url, values, "GET");  //HttpURLConnection 통신 요청

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
                //파싱
                JsonParser jp = new JsonParser();
                JsonObject jsonObject = (JsonObject) jp.parse(result);
                JsonObject jsonObjectSys = (JsonObject) jp.parse(jsonObject.get("sys").getAsJsonObject().toString());
                JsonObject jsonObjectWeather = (JsonObject) jp.parse(jsonObject.get("weather").getAsJsonArray().get(0).toString());
                JsonObject jsonObjectMain = (JsonObject) jp.parse(jsonObject.get("main").getAsJsonObject().toString());
                JsonObject jsonObjectWind = (JsonObject) jp.parse(jsonObject.get("wind").getAsJsonObject().toString());
                JsonObject jsonObjectClouds = (JsonObject) jp.parse(jsonObject.get("clouds").getAsJsonObject().toString());

                weatherData model = new weatherData();
                //날씨등을 한글로 표시
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

                setWeatherData(model);  //UI 업데이트

            } else {
                showFailPop();
            }
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }

    }  //NetworkTask End

    /* 통신하여 받아온 날씨 데이터를 통해 UI 업데이트 메소드 */
    private void setWeatherData(weatherData model) {
        Log.d("Weather", "setWeatherData");
        //tv_name.setText(model.getName());
        //tv_country.setText(model.getCountry());
        Glide.with(this).load(model.getIcon())  //Glide 라이브러리를 이용하여 ImageView 에 url 로 이미지 지정
                .into(iv_weather);
        tv_temp.setText(doubleToStrFormat(2, model.getTemp()) + " 'C");  //소수점 2번째 자리까지 반올림하기
        //tv_main.setText(model.getMain());
        tv_description.setText(model.getDescription());
        //tv_wind.setText(doubleToStrFormat(2, model.getWind()) + " m/s");
        //tv_cloud.setText(doubleToStrFormat(2, model.getClouds()) + " %");
        //tv_humidity.setText(doubleToStrFormat(2, model.getHumidity()) + " %");
    }


    /* 통신 실패시 AlertDialog 표시하는 메소드 */
    private void showFailPop() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Title").setMessage("통신실패");

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


    /* 소수점 n번째 자리까지 반올림하기 */
    private String doubleToStrFormat(int n, double value) {
        return String.format("%." + n + "f", value);
    }

    private String transferWeather(String weather) {
        weather = weather.toLowerCase();
        if (weather.equals("haze"))
            return "안개";
        else if (weather.equals("fog"))
            return "안개";
        else if (weather.equals("clouds"))
            return "구름";
        else if (weather.equals("few clouds"))
            return "구름 조금";
        else if (weather.equals("scattered clouds"))
            return "구름 낌";
        else if (weather.equals("broken clouds"))
            return "구름 많음";
        else if (weather.equals("overcast clouds"))
            return "구름 많음";
        else if (weather.equals("clear sky"))
            return "맑음";
        else if(weather.equals("moderate rain"))
            return  "비";
        return "";
    }



    void readRss() {
        Log.d("Rss", "startRSS");
        try {
            //url 주소, http은 보안성 사용x

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

            //해임달(URL)에게 무지개로드(Stream) 열도록..
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
        /*
        if(System.currentTimeMillis() > back_time +2000){
            back_time= System.currentTimeMillis();
            Toast.makeText(getActivity(), "한번 더 누르면 종료됩니다.", Toast.LENGTH_SHORT).show();
        }
        if(System.currentTimeMillis() <=back_time +2000){
            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            fragmentManager.beginTransaction().remove(this).commit();
            fragmentManager.popBackStack();
        }

         */
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
}
