package com.example.daystarter.ui.setting;

import android.app.Activity;
import android.os.Bundle;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;

import com.example.daystarter.R;


public class setting extends Activity {
    RadioGroup group;
    RadioButton dark,light;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting);

        group=findViewById(R.id.radioMode);
        dark=findViewById(R.id.rbDark);
        light=findViewById(R.id.rbLight);

        group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if(i==R.id.rbLight){
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    finish();
                }
                else if(i==R.id.rbDark){
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    finish();
                }
            }
        });
    }
}
