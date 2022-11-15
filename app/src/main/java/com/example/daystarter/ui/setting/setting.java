package com.example.daystarter.ui.setting;

import static android.content.ContentValues.TAG;
import static android.view.WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.example.daystarter.R;
import com.example.daystarter.lockScreen.LockScreenService;
import com.google.android.material.switchmaterial.SwitchMaterial;

import butterknife.BindView;
import butterknife.ButterKnife;


public class setting extends Activity {
    @BindView(R.id.setting_close) Button setting_close;
    @BindView(R.id.rbDark) RadioButton dark;
    @BindView(R.id.rbLight) RadioButton light;
    @BindView(R.id.radioMode) RadioGroup group;
    @BindView(R.id.alarmsound) TextView sound;
    @BindView(R.id.alarm_seekbar) SeekBar alarm_seekbar;
    @BindView(R.id.lock_screen_switch) SwitchMaterial lockScreenSwitch;

    String color;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting);
        ButterKnife.bind(this);


        group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if(i== R.id.rbLight){
                    color =TimeUtil.Light;
                    TimeUtil.applyTheme(color);
                    TimeUtil.ModSave(getApplicationContext(),color);
                }
                else if(i== R.id.rbDark){
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

        SharedPreferences preferences = getSharedPreferences("lockScreenServiceRunning", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        Log.d(TAG, "onCreate: " + preferences.getString("lockScreenServiceRunning", "N"));
        if(preferences.getString("lockScreenServiceRunning", "N").equals("Y")){
            lockScreenSwitch.setChecked(true);
        }

        lockScreenSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(!Settings.canDrawOverlays(getApplicationContext())){
                    Intent overlayIntent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:" + getPackageName()));
                    startActivity(overlayIntent);
                }
                else {
                    Intent intent = new Intent(setting.this, LockScreenService.class);
                    if (b) {
                        startForegroundService(intent);
                        editor.putString("lockScreenServiceRunning", "Y");
                    } else {
                        stopService(intent);
                        editor.putString("lockScreenServiceRunning", "N");
                    }
                    editor.commit();
                }
            }
        });
    }
}
