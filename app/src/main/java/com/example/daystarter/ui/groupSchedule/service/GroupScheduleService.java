package com.example.daystarter.ui.groupSchedule.service;

import static android.content.ContentValues.TAG;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.example.daystarter.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Random;

public class GroupScheduleService extends FirebaseMessagingService {
    private NotificationManager mNotificationManager;

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Log.d(TAG, "onMessageReceivedddddddddddddd: ");
        createNotificationChannel();
        sendNotification(remoteMessage);


    }

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference().child("users")
                .child(FirebaseAuth.getInstance().getUid()).child("firebaseMessagingToken");
        dbRef.setValue(s);
    }

    private void sendNotification(RemoteMessage remoteMessage) {
        /*
        Intent intent = new Intent(this, GroupSchedulePostActivity.class);
        intent.putExtra("key", "a");
        intent.putExtra("groupId", "b");
        Log.d(TAG, "sendNotification");
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT);

         */
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, getString(R.string.day_starter_group_notification_channel));
        builder.setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(remoteMessage.getData().get("title"))
                .setContentText(remoteMessage.getData().get("body"))
                .setPriority(NotificationManager.IMPORTANCE_HIGH)
                .setAutoCancel(true);// 사용자가 탭을 클릭하면 자동 제거


        mNotificationManager.notify((int) System.currentTimeMillis(), builder.build());
        Log.d(TAG, "sendNotification: " + remoteMessage.getMessageId());
    }

    void createNotificationChannel(){
        mNotificationManager = (NotificationManager)
                getSystemService(NOTIFICATION_SERVICE);
        // 기기(device)의 SDK 버전 확인 ( SDK 26 버전 이상인지 - VERSION_CODES.O = 26)
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            //Channel 정의 생성자( construct 이용 )
            NotificationChannel notificationChannel = new NotificationChannel(getString(R.string.day_starter_group_notification_channel)
                    ,"M Notification",NotificationManager.IMPORTANCE_HIGH);
            //Channel 에 대한 기본 설정
            notificationChannel.enableLights(true);
            notificationChannel.enableVibration(true);
            notificationChannel.setDescription("Notification from Day Starter");

            // Manager 를 이용하여 Channel 생성
            mNotificationManager.createNotificationChannel(notificationChannel);
        }
    }
}