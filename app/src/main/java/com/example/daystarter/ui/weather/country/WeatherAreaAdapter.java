package com.example.daystarter.ui.weather.country;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.daystarter.R;
import com.example.daystarter.ui.weather.RequestHttpUrlConnection;
import com.example.daystarter.ui.weather.WeatherData;
import com.example.daystarter.ui.weather.country.data.WeatherViewModel;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.List;

public class WeatherAreaAdapter extends RecyclerView.Adapter {
    String strUrl = "https://api.openweathermap.org/data/2.5/weather";  //통신할 URL
    Context context;
    NetworkTask networkTask = null;
    ArrayList<WeatherData> ArrayWeatherData;
    WeatherData weatherData;
    WeatherAreaData weatherAreaData;
    WeatherAreaViewHolder wv;
    WeatherViewModel weatherViewModel;
    List<WeatherData>  ListWeatherAreaData =new ArrayList<WeatherData>();

    String  area,areas;
    String TAG="WeatherAreaAdapter";
    double lat,lng;
    Intent intent;

    public WeatherAreaAdapter(ArrayList<WeatherData> arrayWeatherData, Context context) {
        this.ArrayWeatherData = arrayWeatherData;
        this.context=context;
    }

    public void add(WeatherData data){
        ArrayWeatherData.add(data);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder: ");
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_weather_area,parent,false);
        WeatherAreaViewHolder weatherAreaViewHolder = new WeatherAreaViewHolder(v);
        intent = new Intent(context,WeatherAreaActivity.class);
        return weatherAreaViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        weatherData = ArrayWeatherData.get(position);
        requestNetwork();
         wv= (WeatherAreaViewHolder)holder;
        Log.d(TAG, "onBindViewHolder: ");

    }
    @Override
    public int getItemCount() {
        return ArrayWeatherData.size();
    }

    class WeatherAreaViewHolder extends RecyclerView.ViewHolder{
        TextView weather_area_description,weather_area_temp,weather_area_textview;
        ImageView weather_area_weather;

        public WeatherAreaViewHolder(@NonNull View itemView) {
            super(itemView);
            weather_area_description=itemView.findViewById(R.id.weather_area_description);
            weather_area_textview =itemView.findViewById(R.id.weather_area_textview);
            weather_area_temp =itemView.findViewById(R.id.weather_area_temp);
            weather_area_weather =itemView.findViewById(R.id.weather_area_weather);

            //아이템 클릭시 선택한 뉴스로 이동(
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    intent.putExtra("area",area);
                    intent.putExtra("lat",lat);
                    intent.putExtra("lng",lng);
                    Log.d("item btn click ", "lat"+lat+"lng"+lng);
                    context.startActivity(intent);
                    /*
                    int pos = getAdapterPosition();
                    if(pos != RecyclerView.NO_POSITION){
                        Navigation.findNavController(view).navigate(R.id.nav_weather);
                    }
                     */

                }
            });
        }
    }


    /* NetworkTask 를 요청하기 위한 메소드 */
    public void requestNetwork() {
        ContentValues values = new ContentValues();
        //기본값 =서울날씨(바꾸고 싶으면 여기서 교채하면된다.)
        //area=changeName(area);
        lat =weatherAreaData.getLat();
        lng=weatherAreaData.getLng();
        area=weatherAreaData.getArea();
        areas=weatherAreaData.getAreas();
        Log.d(TAG, "requestNetworks: "+lat +lng);
        values.put("q", area);
        //values.put("q", "Seoul");  //여기에 지역 넣기
        values.put("appid", "7e818b3bfae91bb6fcbe3d382b6c3448");

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
            Log.d(TAG, "onPostExecute: ");

            if (result != null && !result.equals("")) {
                //파싱
                JsonParser jp = new JsonParser();
                JsonObject jsonObject = (JsonObject) jp.parse(result);
                JsonObject jsonObjectSys = (JsonObject) jp.parse(jsonObject.get("sys").getAsJsonObject().toString());
                JsonObject jsonObjectWeather = (JsonObject) jp.parse(jsonObject.get("weather").getAsJsonArray().get(0).toString());
                JsonObject jsonObjectMain = (JsonObject) jp.parse(jsonObject.get("main").getAsJsonObject().toString());
                JsonObject jsonObjectWind = (JsonObject) jp.parse(jsonObject.get("wind").getAsJsonObject().toString());
                JsonObject jsonObjectClouds = (JsonObject) jp.parse(jsonObject.get("clouds").getAsJsonObject().toString());

                WeatherData model = new WeatherData();
                //날씨등을 한글로 표시
                String description = jsonObjectWeather.get("description").toString().replaceAll("\"", "");
                description = transferWeather(description);
                model.setName(jsonObject.get("name").toString().replaceAll("\"", ""));
                model.setCountry(jsonObjectSys.get("country").toString().replaceAll("\"", ""));
                Log.d("Icon", "Icon: https://api.openweathermap.org img/w/" + jsonObjectWeather.get("icon").toString().replaceAll("\"", "") + ".png");
                model.setIcon("https://api.openweathermap.org img/w/" + jsonObjectWeather.get("icon").toString().replaceAll("\"", "") + ".png");
                model.setTemp(jsonObjectMain.get("temp").getAsDouble() - 273.15);
                model.setMain(jsonObjectWeather.get("main").toString().replaceAll("\"", ""));
                model.setDescription(description);

                setWeatherData(model);  //UI 업데이트

            } else {
                //showFailPop();
            }
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }

    }  //NetworkTask End

    /* 통신하여 받아온 날씨 데이터를 통해 UI 업데이트 메소드 */
    private void setWeatherData(WeatherData model) {
        Log.d("Weather", "setWeatherData");
        wv.weather_area_textview.setText(areas);
                wv.weather_area_temp.setText(doubleToStrFormat(2, model.getTemp()) + " 'C");  //소수점 2번째 자리까지 반올림하기
                wv.weather_area_description.setText(model.getDescription());
        Glide.with(wv.itemView.getContext()).load(model.getIcon())  //Glide 라이브러리를 이용하여 ImageView 에 url 로 이미지 지정
                //.placeholder(R.drawable.icon_image)
                //.error(R.drawable.icon_image)
                .into(wv.weather_area_weather);
        Log.d("weatherArea", " "+areas);

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
        return "비";
    }

    public void setWeathers(List<WeatherData> weatherAreaData) {
        this.ListWeatherAreaData = weatherAreaData;
        notifyDataSetChanged();
    }
}
