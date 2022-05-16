package com.example.daystarter.ui.weather;

import android.content.ContentValues;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

//http 통신을 위해서 사용한 클래스
public class RequestHttpUrlConnection {
    public String TAG = RequestHttpUrlConnection.class.getSimpleName();
    public String request(String _url, ContentValues _params, String method) {

        String result = "";
        try{
            Log.d(TAG, "http_request"+_url);

            StringBuffer sbParams = new StringBuffer();
            String data = "";
            //보낼 데이터가 없음면 파라미터를 비움
            if (_params == null){
                sbParams.append("");
            } else {
                //파람미터가 2개 이상이면 파라미터에 연결에 &기 필요하여 변수 생성
                boolean isAnd = false;
                //파라미터 키와 값
                String key;
                String value;

                for(Map.Entry<String, Object> parameter : _params.valueSet()){
                    key = parameter.getKey();
                    value = parameter.getValue().toString();
                    //파라미터가 두개 이상일때, 파라미터 사이에 &로 연결
                    //https://api.openweathermap.org/data/2.5/weather?q=Seoul&appid=7e818b3bfae91bb6fcbe3d382b6c3448
                    //형식으로 만들기 위해서 사용
                    if (isAnd){
                        sbParams.append("&");
                    }
                    sbParams.append(key).append("=").append(value);
                    if (!isAnd){
                        if (_params.size() >= 2){
                            isAnd = true;
                        }
                    }
                }
            }
            data = sbParams.toString();
            URL url = new URL(method == "POST" ? _url : _url+"?"+data);  //URL 문자열을 이용해 URL 객체 생성
            HttpURLConnection conn = (HttpURLConnection)url.openConnection();  //URL 객체를 이용해 HttpUrlConnection 객체 생성
            conn.setConnectTimeout(3000);
            conn.setReadTimeout(3000);
            conn.setRequestProperty("Cache-Control", "no-cache");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty("Accept", "application/json");
            conn.setDefaultUseCaches(false);
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setRequestMethod(method);
//            conn.setRequestMethod("POST");
//            conn.setRequestMethod("GET");

            Log.d(TAG, "request1"+url+"?"+data);

            // 서버로 값 전송
            OutputStream outputStream = conn.getOutputStream();
            outputStream.write(data.getBytes("UTF-8"));
            outputStream.flush();

            // Response 데이터 처리(연결 요청 확인)
            int responseCode = conn.getResponseCode();
            Log.d(TAG, "request2 "+responseCode);
            if(responseCode == HttpURLConnection.HTTP_OK) {
                StringBuilder builder = new StringBuilder();
                try {
                    InputStreamReader in = new InputStreamReader(conn.getInputStream(), "UTF-8");
                    BufferedReader reader = new BufferedReader(in);  //응답 결과를 읽기 위한 스트림 객체 생성
                    String line = "";
                    while((line = reader.readLine()) != null) {
                        builder.append(line + "\n");
                    }
                    result = builder.toString();
                } catch(IOException e) {
                    e.printStackTrace();
                }
            } else {
                result = conn.getResponseMessage();
            }
        }catch(Exception e){
            e.printStackTrace();
        }

        Log.d(TAG, "http_result"+result.trim());
        return result.trim();
    }

}
