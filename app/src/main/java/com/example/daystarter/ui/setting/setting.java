package com.example.daystarter.ui.setting;

import android.app.Activity;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.daystarter.R;

import butterknife.BindView;
import butterknife.ButterKnife;


public class setting extends Activity {
    @BindView(R.id.setting_close) Button setting_close;
    @BindView(R.id.rbDark) RadioButton dark;
    @BindView(R.id.rbLight) RadioButton light;
    @BindView(R.id.radioMode) RadioGroup group;
    @BindView(R.id.alarmsound) TextView sound;
    @BindView(R.id.alarm_seekbar) SeekBar alarm_seekbar;

    String color;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting);
        ButterKnife.bind(this);


        group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if(i==R.id.rbLight){
                    color =TimeUtil.Light;
                    TimeUtil.applyTheme(color);
                    TimeUtil.ModSave(getApplicationContext(),color);
                }
                else if(i==R.id.rbDark){
                    color =TimeUtil.Dark;
                    TimeUtil.applyTheme(color);
                    TimeUtil.ModSave(getApplicationContext(),color);
                }
            }
        });
        AudioManager audioManager =(AudioManager)getSystemService(AUDIO_SERVICE);
        int max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int current =audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        alarm_seekbar.setMax(max);
        alarm_seekbar.setProgress(current);

        alarm_seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                sound.setText(""+i+"%");
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,i,0);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        setting_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    }

}
