package com.example.daystarter.ui.alarm.service;

import static com.example.daystarter.ui.alarm.application.App.CHANNEL_ID;
import static com.example.daystarter.ui.alarm.broadcastreceiver.AlarmBroadcastReceiver.TITLE;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.IBinder;
import android.os.Vibrator;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.daystarter.R;
import com.example.daystarter.ui.alarm.activities.RingActivity;

public class AlarmService extends Service {
    private MediaPlayer mediaPlayer;
    private Vibrator vibrator;

    @Override
    public void onCreate() {
        Log.d("service","service");
        super.onCreate();

        mediaPlayer = MediaPlayer.create(this, R.raw.alarm);
        mediaPlayer.setLooping(true);

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            String CHANNEL_ID = createNotificationChannel();
            Intent notificationIntent = new Intent(this, RingActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

            String alarmTitle = String.format("%s Alarm", intent.getStringExtra(TITLE));
            //notification
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle(alarmTitle)
                    .setContentText("Ring Ring .. Ring Ring")
                    .setSmallIcon(R.drawable.ic_alarm_black_24dp)
                    .setContentIntent(pendingIntent);

            Intent intent1 = new Intent(this,RingActivity.class);
            PendingIntent pendingIntent1 =PendingIntent.getActivity(this,0,intent1,PendingIntent.FLAG_UPDATE_CURRENT);
            builder.setFullScreenIntent(pendingIntent1,true);

            mediaPlayer.start();

            long[] pattern = { 0, 100, 1000 };
            //진동
            vibrator.vibrate(pattern, 0);

            startForeground(1, builder.build());
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
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
