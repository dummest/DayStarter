package com.example.daystarter.ui.alarm.activities;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.daystarter.R;
import com.example.daystarter.ui.alarm.data.Alarm;
import com.example.daystarter.ui.alarm.service.AlarmService;

import java.util.Calendar;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;

public class RingActivity extends AppCompatActivity {
    @BindView(R.id.activity_ring_dismiss) Button dismiss;
    @BindView(R.id.activity_ring_snooze) Button snooze;
    @BindView(R.id.activity_ring_clock) ImageView clock;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ring);

        ButterKnife.bind(this);


        //잠금화면 위에서도 알람울리게
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);

        dismiss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentService = new Intent(getApplicationContext(), AlarmService.class);
                getApplicationContext().stopService(intentService);
                finish();
            }
        });

        snooze.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(System.currentTimeMillis());
                calendar.add(Calendar.MINUTE, 10);

                Alarm alarm = new Alarm(
                        calendar.get(Calendar.HOUR_OF_DAY),
                        calendar.get(Calendar.MINUTE),
                        "Snooze",
                        System.currentTimeMillis(),
                        true,
                        false,
                        false,
                        false,
                        false,
                        false,
                        false,
                        false,
                        false
                );

                alarm.schedule(getApplicationContext());

                Intent intentService = new Intent(getApplicationContext(), AlarmService.class);
                getApplicationContext().stopService(intentService);
                finish();
            }
        });

        animateClock();
    }

    private void animateClock() {
        ObjectAnimator rotateAnimation = ObjectAnimator.ofFloat(clock, "rotation", 0f, 20f, 0f, -20f, 0f);
        rotateAnimation.setRepeatCount(ValueAnimator.INFINITE);
        rotateAnimation.setDuration(800);
        rotateAnimation.start();
    }
    //음향 조절
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        AudioManager mAudioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);//선언 후
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP :
                mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI);
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:

                mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI);
                return true;
            case KeyEvent.KEYCODE_BACK:
                return true;
        }
        return false;
    }

}
