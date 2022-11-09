package com.example.daystarter.ui.groupSchedule.service;

import static android.content.ContentValues.TAG;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.daystarter.R;
import com.example.daystarter.ui.groupSchedule.cacheDBHelper.UnreadDBHelper;
import com.example.daystarter.ui.groupSchedule.myClass.Member;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Random;

public class GroupScheduleService extends FirebaseMessagingService {
    private NotificationManager mNotificationManager;
    static String TYPE_GROUP_SCHEDULE = "group_schedule";
    static String TYPE_GROUP_CHAT = "group_chat";
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        String type = remoteMessage.getData().get("type");
        String groupId = remoteMessage.getData().get("groupId");
        UnreadDBHelper unreadDBHelper = new UnreadDBHelper(getBaseContext());
        if(type.equals(TYPE_GROUP_SCHEDULE)) {
            if (!unreadDBHelper.searchGroup(groupId))
                unreadDBHelper.insertGroup(groupId);
            unreadDBHelper.increaseScheduleCounter(groupId);

            createGroupNotificationChannel();
            sendGroupNotification(remoteMessage);
        }
        else if(type.equals(TYPE_GROUP_CHAT)){
            if(!unreadDBHelper.searchGroup(groupId))
                unreadDBHelper.insertGroup(groupId);
            unreadDBHelper.increaseChatCounter(groupId);

            createGroupChatNotificationChannel();
            sendGroupChatNotification(remoteMessage);
        }
    }

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
        if(FirebaseAuth.getInstance() != null) {
            DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference().child("users")
                    .child(FirebaseAuth.getInstance().getUid()).child("firebaseMessagingToken");
            dbRef.setValue(s);
        }
    }

    private void createGroupNotificationChannel(){
        mNotificationManager = (NotificationManager)
                getSystemService(NOTIFICATION_SERVICE);
        // 기기(device)의 SDK 버전 확인 ( SDK 26 버전 이상인지 - VERSION_CODES.O = 26)
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            //Channel 정의 생성자( construct 이용 )

            NotificationChannel notificationChannel = new NotificationChannel(getString(R.string.day_starter_group_notification_channel)
                    ,"group_notification_channel",NotificationManager.IMPORTANCE_HIGH);
            //Channel 에 대한 기본 설정
            notificationChannel.enableLights(true);
            notificationChannel.enableVibration(true);
            notificationChannel.setDescription("Notification from Day Starter");

            // Manager 를 이용하여 Channel 생성
            mNotificationManager.createNotificationChannel(notificationChannel);
        }
    }

    private void sendGroupNotification(RemoteMessage remoteMessage) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, getString(R.string.day_starter_group_notification_channel));
        builder.setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(remoteMessage.getData().get("title"))
                .setContentText(remoteMessage.getData().get("body"))
                .setPriority(NotificationManager.IMPORTANCE_HIGH)
                .setAutoCancel(true);// 사용자가 탭을 클릭하면 자동 제거
        mNotificationManager.notify((int)(remoteMessage.getSentTime()/7), builder.build());
        Log.d(TAG, "sendNotification: " + remoteMessage.getMessageId());
    }

    private void createGroupChatNotificationChannel(){
        mNotificationManager = (NotificationManager)
                getSystemService(NOTIFICATION_SERVICE);
        // 기기(device)의 SDK 버전 확인 ( SDK 26 버전 이상인지 - VERSION_CODES.O = 26)
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            //Channel 정의 생성자( construct 이용 )
            NotificationChannel notificationChannel = new NotificationChannel(getString(R.string.day_starter_group_chat_notification_channel)
                    ,"group_chat_notification_channel",NotificationManager.IMPORTANCE_HIGH);
            //Channel 에 대한 기본 설정
            notificationChannel.enableLights(true);
            notificationChannel.enableVibration(true);
            notificationChannel.setDescription("Notification from Day Starter");

            // Manager 를 이용하여 Channel 생성
            mNotificationManager.createNotificationChannel(notificationChannel);
        }
    }
    private void sendGroupChatNotification(RemoteMessage remoteMessage) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, getString(R.string.day_starter_group_chat_notification_channel));
        builder.setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(remoteMessage.getData().get("title"))
                .setContentText(remoteMessage.getData().get("body"))
                .setPriority(NotificationManager.IMPORTANCE_HIGH)
                .setAutoCancel(true);// 사용자가 탭을 클릭하면 자동 제거
        mNotificationManager.notify((int) remoteMessage.getSentTime(), builder.build());
    }
}