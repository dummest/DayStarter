package com.example.daystarter.ui.weather;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
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
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.daystarter.R;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class WeatherFragment extends Fragment {
    //@BindView(R.id.tv_name) TextView tv_name;
    //@BindView(R.id.tv_country) TextView tv_country;
    Context context;

    TextView tv_name, tv_country;
    ImageView iv_weather;
    TextView tv_temp, tv_main, tv_description;
    TextView tv_wind, tv_cloud, tv_humidity;

    String strUrl = "https://api.openweathermap.org/data/2.5/weather";  //통신할 URL
    NetworkTask networkTask = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v;
        v = inflater.inflate(R.layout.fragment_weather, container, false);
        //ButterKnife.bind(this,v);

        tv_name = (TextView) v.findViewById(R.id.tv_name);
        tv_country = (TextView) v.findViewById(R.id.tv_country);
        iv_weather = (ImageView) v.findViewById(R.id.iv_weather);
        tv_temp = (TextView) v.findViewById(R.id.tv_temp);
        tv_main = (TextView) v.findViewById(R.id.tv_main);
        tv_description = (TextView) v.findViewById(R.id.tv_description);
        tv_wind = (TextView) v.findViewById(R.id.tv_wind);
        tv_cloud = (TextView) v.findViewById(R.id.tv_cloud);
        tv_humidity = (TextView) v.findViewById(R.id.tv_humidity);
        requestNetwork();
        return v;
    }


    /* NetworkTask 를 요청하기 위한 메소드 */
    private void requestNetwork() {
        ContentValues values = new ContentValues();
        //인천 날씨(바꾸고 싶으면 여기서 교채하면된다.)
        values.put("q", "Incheon");
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
                String description = jsonObjectWeather.get("description").toString().replaceAll("\"","");
                description=transferWeather(description);
                model.setName(jsonObject.get("name").toString().replaceAll("\"",""));
                model.setCountry(jsonObjectSys.get("country").toString().replaceAll("\"",""));
                Log.d("Icon", "Icon: "+getString(R.string.weather_url)+"img/w/" + jsonObjectWeather.get("icon").toString().replaceAll("\"","") + ".png");
                model.setIcon(getString(R.string.weather_url)+"img/w/" + jsonObjectWeather.get("icon").toString().replaceAll("\"","") + ".png");
                model.setTemp(jsonObjectMain.get("temp").getAsDouble() - 273.15);
                model.setMain(jsonObjectWeather.get("main").toString().replaceAll("\"",""));
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
        tv_name.setText(model.getName());
        tv_country.setText(model.getCountry());
        Glide.with(this).load(model.getIcon())  //Glide 라이브러리를 이용하여 ImageView 에 url 로 이미지 지정
                //.placeholder(R.drawable.icon_image)
                //.error(R.drawable.icon_image)
                .into(iv_weather);
        tv_temp.setText(doubleToStrFormat(2, model.getTemp()) + " 'C");  //소수점 2번째 자리까지 반올림하기
        tv_main.setText(model.getMain());
        tv_description.setText(model.getDescription());
        tv_wind.setText(doubleToStrFormat(2, model.getWind()) + " m/s");
        tv_cloud.setText(doubleToStrFormat(2, model.getClouds()) + " %");
        tv_humidity.setText(doubleToStrFormat(2, model.getHumidity()) + " %");
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
        return String.format("%."+n+"f", value);
    }

    private String transferWeather(String weather){
        weather = weather.toLowerCase();
        if(weather.equals("haze"))
            return "안개";
        else if(weather.equals("fog"))
            return "안개";
        else if(weather.equals("clouds"))
            return "구름";
        else if(weather.equals("few clouds"))
            return "구름 조금";
        else if(weather.equals("scattered clouds"))
            return "구름 낌";
        else if(weather.equals("broken cludes"))
            return "구름 많음";
        else if(weather.equals("overcast clouds"))
            return "구름 많음";
        else if(weather.equals("clear sky"))
            return "맑음";
        return "";
    }



}