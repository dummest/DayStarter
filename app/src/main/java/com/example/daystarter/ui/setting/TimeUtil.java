package com.example.daystarter.ui.setting;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.appcompat.app.AppCompatDelegate;

public class TimeUtil {
    public static final String Light="light";
    public static final String Dark="dark";
    public static final String Tag="DarkMode";

    public static void applyTheme(String Color){
        switch (Color){
            case Light:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                Log.d("TAG","라이트 모드");
                break;
            case Dark:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                Log.d("TAG","다크 모드");
                break;

        }
    }

    public static  void ModSave(Context context,String mod){
        SharedPreferences sp;
        sp=context.getSharedPreferences("mod",context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("mod",mod);
        editor.commit();
    }


    public static String ModLoad(Context context){
        SharedPreferences sp;
        sp= context.getSharedPreferences("mod",context.MODE_PRIVATE);
        String load = sp.getString("mod","light");
        return load;
    }
    }
