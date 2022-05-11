package com.example.daystarter.ui.groupSchedule;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.daystarter.R;
import com.example.daystarter.ui.groupSchedule.myClass.Member;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class GroupActivity extends AppCompatActivity {
    FloatingActionButton floatingActionButton;

    String groupId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);
        groupId = getIntent().getStringExtra("groupId");
        validation();

        floatingActionButton = findViewById(R.id.group_schedule_fab);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getBaseContext(), WritingGroupScheduleActivity.class);
                startActivity(intent);
            }
        });
    }

    //그룹 구성원인 사람이 접속했는지, 그룹이 존재하는지 확인 후 진행 하거나 취소함
    void validation(){
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
        DatabaseReference memberRef = dbRef.child("groups").child(groupId).child("members").child(FirebaseAuth.getInstance().getUid());
        memberRef.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if(!task.getResult().exists()){
                    showToast("에러!");
                    finish();
                }
                else{
                    Log.d(TAG, "he's in the group");
                    loadData();
                }
            }
        });
    }

    private void loadData(){

    }

    private void showToast(String str){
        Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
    }
}