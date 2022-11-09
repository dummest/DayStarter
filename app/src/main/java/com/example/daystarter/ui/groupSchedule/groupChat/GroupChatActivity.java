package com.example.daystarter.ui.groupSchedule.groupChat;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.daystarter.R;
import com.example.daystarter.databinding.ActivityGroupBinding;
import com.example.daystarter.databinding.ActivityGroupChatBinding;
import com.example.daystarter.model.NotificationModel;
import com.example.daystarter.ui.groupSchedule.cacheDBHelper.UnreadDBHelper;
import com.example.daystarter.ui.groupSchedule.groupChat.myClass.ChatModel;
import com.example.daystarter.ui.groupSchedule.groupChat.myClass.GroupChatRecyclerViewAdapter;
import com.example.daystarter.ui.groupSchedule.myClass.Member;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.Calendar;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class GroupChatActivity extends AppCompatActivity {
    ActivityGroupChatBinding binding;
    String groupId = "";
    GroupChatRecyclerViewAdapter adapter;
    int finishCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGroupChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        groupId = getIntent().getStringExtra("groupId");
        finishCount = 0;

        adapter = new GroupChatRecyclerViewAdapter(this, groupId, binding.chatRecyclerView);
        binding.chatRecyclerView.setLayoutManager(new LinearLayoutManager(getLayoutInflater().getContext()));
        binding.chatRecyclerView.setAdapter(adapter);

        binding.sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ChatModel chatModel = new ChatModel();
                chatModel.writerUid = FirebaseAuth.getInstance().getUid();
                chatModel.writingTime = Calendar.getInstance().getTimeInMillis();
                String text = binding.messageEditText.getText().toString().trim();
                chatModel.text = text;
                if(chatModel.text.length()>0) {
                    DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference().child("messages")
                            .child(groupId);
                    dbRef.push().setValue(chatModel).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                binding.messageEditText.setText("");
                                sendGcm(chatModel);
                            }
                        }
                    });
                }
                else{
                    binding.messageEditText.setText("");
                }
            }
        });
    }

    void sendGcm(ChatModel chatModel){
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
        dbRef.child("groups").child(groupId).child("members").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if(task.isSuccessful()) {
                    for (DataSnapshot ds : task.getResult().getChildren()) {
                        Member member = ds.getValue(Member.class);
                        String uid = member.uid;

                        Log.d(TAG, "member: " + member.name);
                        //자기자신에게 노티를 보내지는 않도록
                        if (member.alarmSet && !uid.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                            DatabaseReference tokenRef = FirebaseDatabase.getInstance().getReference();
                            tokenRef.child("users").child(uid).child("firebaseMessagingToken").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DataSnapshot> task) {
                                    if (task.isSuccessful()) {
                                        if(task.getResult().exists()) {
                                            sendNotification(task.getResult().getValue(String.class), chatModel, member);
                                        }
                                    }
                                }
                            });
                        }
                    }
                }
                else{
                    Log.d(TAG, "onComplete: error");
                }
            }
        });


    }

    void sendNotification(String token, ChatModel chatModel, Member member){
        Gson gson = new Gson();
        NotificationModel notificationModel = new NotificationModel();
        notificationModel.data.writerUid = FirebaseAuth.getInstance().getUid();
        notificationModel.data.type = "group_chat";
        notificationModel.data.title = member.name + "님이 보낸 채팅";
        notificationModel.data.body = chatModel.text;
        notificationModel.data.groupId = groupId;
        notificationModel.to = token;
        RequestBody requestBody = RequestBody.create(gson.toJson(notificationModel), MediaType.parse("application/json; charset=utf8"));

        Request request = new Request.Builder()
                .header("Content-Type", "application/json")
                .addHeader("Authorization", "key=" + getString(R.string.server_key))
                .url("https://fcm.googleapis.com/fcm/send")
                .post(requestBody)
                .build();

        OkHttpClient okHttpClient = new OkHttpClient();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {

            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {

            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();
    }
}