package com.example.daystarter.ui.alarm.service;

import static com.example.daystarter.ui.alarm.application.App.CHANNEL_ID;
import static com.example.daystarter.ui.alarm.broadcastreceiver.AlarmBroadcastReceiver.TITLE;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.Vibrator;
import android.view.KeyEvent;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.daystarter.R;
import com.example.daystarter.ui.alarm.activities.RingActivity;
import com.example.daystarter.ui.alarm.rock.RockReceiver;
import com.example.daystarter.ui.alarm.rock.ScreenService;

public class AlarmService extends Service {
    private MediaPlayer mediaPlayer;
    private Vibrator vibrator;
    //WakeLock을 사용하기 위해 정의
    PowerManager powerManager;
    PowerManager.WakeLock wakeLock;
    AudioManager mAudioManager;
    String sound;


    @SuppressLint("InvalidWakeLockTag")
    @Override
    public void onCreate() {
        super.onCreate();
        powerManager = (PowerManager)getSystemService(Context.POWER_SERVICE);
        mAudioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, "WAKELOCK");

        mediaPlayer = MediaPlayer.create(this, R.raw.alarm);
        mediaPlayer.setLooping(true);
        //mediaPlayer.setVolume();
        /*
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // MusicPlayer를 방해하지 않음
            setAudioFocusRequest(AudioManager.AUDIOFOCUS_NONE)
        }
        */
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            AlarmManager alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
            String CHANNEL_ID = createNotificationChannel();
            Intent notificationIntent = new Intent(this, RingActivity.class);
            //sound=notificationIntent.getStringExtra("sound");

            notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent,PendingIntent.FLAG_UPDATE_CURRENT);

            String alarmTitle = String.format("%s Alarm", intent.getStringExtra(TITLE));

            //notification
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle(alarmTitle)
                    .setContentText("Ring Ring .. Ring Ring")
                    .setSmallIcon(R.drawable.ic_alarm_black_24dp)
                    .setContentIntent(pendingIntent)
                    .setCategory(NotificationCompat.CATEGORY_ALARM)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setFullScreenIntent(pendingIntent,true);

            //Intent fullScreenIntent = new Intent(this,RingActivity.class);
            //PendingIntent pendingIntent1 =PendingIntent.getActivity(this,0,fullScreenIntent,PendingIntent.FLAG_UPDATE_CURRENT);
            //builder.setFullScreenIntent(pendingIntent1,true);
            mediaPlayer.start();
            wakeLock.acquire(); // WakeLock 깨우기
            long[] pattern = { 0, 100, 1000 };
            //진동
            vibrator.vibrate(pattern, 0);
             startForeground(1, builder.build());
             /*
             Intent intent1 = new Intent(getApplicationContext(),ScreenService.class);
             startService(intent1);
*/
        }
        return START_STICKY;
    }
    private String createNotificationChannel() {
        String channelId ="Day_Starter";
        String channelName = getString(R.string.app_name);
        NotificationChannel channel = new NotificationChannel(channelId,channelName, NotificationManager.IMPORTANCE_NONE);
        channel.setSound(null,null);
        channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

        NotificationManager manager =(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.createNotificationChannel(channel);

        return channelId;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mediaPlayer.stop();
        vibrator.cancel();
        wakeLock.release(); // WakeLock 해제
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    public boolean onKeyDown(int keyCode, KeyEvent event) {
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
