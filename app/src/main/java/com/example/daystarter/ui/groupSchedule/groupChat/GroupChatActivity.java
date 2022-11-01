package com.example.daystarter.ui.groupSchedule.groupChat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.os.Bundle;
import android.view.View;

import com.example.daystarter.R;
import com.example.daystarter.databinding.ActivityGroupBinding;
import com.example.daystarter.databinding.ActivityGroupChatBinding;
import com.example.daystarter.ui.groupSchedule.groupChat.myClass.ChatModel;
import com.example.daystarter.ui.groupSchedule.groupChat.myClass.GroupChatRecyclerViewAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;

public class GroupChatActivity extends AppCompatActivity {
    ActivityGroupChatBinding binding;
    String groupId = "";
    GroupChatRecyclerViewAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGroupChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        groupId = getIntent().getStringExtra("groupId");

        adapter = new GroupChatRecyclerViewAdapter(this, groupId, binding.chatRecyclerView);
        binding.chatRecyclerView.setLayoutManager(new LinearLayoutManager(getLayoutInflater().getContext()));
        binding.chatRecyclerView.setAdapter(adapter);

        binding.sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ChatModel chatModel = new ChatModel();
                chatModel.writerUid = FirebaseAuth.getInstance().getUid();
                chatModel.writingTime = Calendar.getInstance().getTimeInMillis();
                chatModel.text = binding.messageEditText.getText().toString().trim();
                if(chatModel.text.length()>1) {
                    DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference().child("messages")
                            .child(groupId);
                    dbRef.push().setValue(chatModel).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                binding.messageEditText.setText("");
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

    @Override
    protected void onResume() {
        super.onResume();
    }
}