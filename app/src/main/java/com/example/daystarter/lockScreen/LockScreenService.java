package com.example.daystarter.lockScreen;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

public class LockScreenService extends Service {
    ScreenLockReceiver receiver;
    private static final String CHANNEL_ID = "LockScreenServiceChannel";

    public LockScreenService() {

    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (receiver == null){
            createReceiver();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        if(intent != null && receiver == null){
            createReceiver();
        }
        NotificationChannel notificationChannel= new NotificationChannel(
                CHANNEL_ID, "LockScreenService", NotificationManager.IMPORTANCE_NONE);
        notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.createNotificationChannel(notificationChannel);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentText("잠금화면 서비스 실행 중");
        Notification notification = builder.build();

        startForeground(65535, notification);
        return Service.START_REDELIVER_INTENT;
    }

    void createReceiver(){
        receiver = new ScreenLockReceiver();
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
        registerReceiver(receiver, intentFilter);
    }

    @Override
    public void onDestroy() {
        if(receiver != null){
            unregisterReceiver(receiver);
        }
        super.onDestroy();
    }
}