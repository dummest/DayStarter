package com.example.daystarter.ui.groupSchedule;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.daystarter.databinding.ActivityGroupBinding;
import com.example.daystarter.databinding.ActivityGroupSettingBinding;
import com.example.daystarter.ui.groupSchedule.myClass.GroupInfo;
import com.example.daystarter.ui.groupSchedule.myClass.Member;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class GroupSettingActivity extends AppCompatActivity {
    ActivityGroupSettingBinding binding;
    String groupId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGroupSettingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        init();

    }

    void init(){
        groupId = getIntent().getStringExtra("groupId");
        if(groupId == null || groupId.isEmpty()){
            showToast("오류");
            finish();
        }
        else{
            DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference()
                    .child("groups").child(groupId).child("members").child(FirebaseAuth.getInstance().getUid());
            dbRef.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    if(task.isSuccessful()) {
                        Member member = task.getResult().getValue(Member.class);
                        switch (member.status){
                            case "host":
                                initHost();
                                break;
                            case "write":
                            case "read":
                                initMember();
                                break;
                            default:
                                showToast("오류: 권한이 없습니다");
                                finish();
                        }
                    }
                }
            });
        }
    }

    void initHost(){
        binding.memberListLayout.setVisibility(View.VISIBLE);
        binding.copyGroupIdLayout.setVisibility(View.VISIBLE);
        binding.deleteGroupLayout.setVisibility(View.VISIBLE);
        binding.editInfoLayout.setVisibility(View.VISIBLE);
        binding.initialStatusLayout.setVisibility(View.VISIBLE);

        binding.groupIdTextView.setText(groupId);

        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference()
                .child("groups").child(groupId).child("initialStatus");
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String initialStatus = snapshot.getValue(String.class);
                if(initialStatus.equals("write"))
                    binding.initialStatusTextView.setText("현재 기본 권한: 쓰기");
                if(initialStatus.equals("read"))
                    binding.initialStatusTextView.setText("현재 기본 권한: 읽기");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        binding.memberListLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        binding.copyGroupIdLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ClipboardManager clipboard = (ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("groupId", groupId);
                clipboard.setPrimaryClip(clip);
                showToast("복사되었습니다");
            }
        });
        binding.deleteGroupLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext(),
                        android.R.style.ThemeOverlay_Material_Dialog)
                        .setTitle("경고")
                        .setMessage("정말로 해체하시겠습니까? (이 작업은 돌이킬 수 없습니다.)")
                        .setPositiveButton("그룹 해체", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                startDelete();
                            }
                        })
                        .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        });
                builder.show();
            }
        });

        binding.initialStatusLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final CharSequence[] items = {"읽기", "쓰기"};

                AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext(),
                        android.R.style.ThemeOverlay_Material_Dialog);

                builder.setTitle("기본 권한 설정")
                        .setItems(items, new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int position)
                            {
                                String initialStatus;
                                if(items[position].toString().trim().equals("읽기"))
                                    initialStatus = "read";
                                else
                                    initialStatus = "write";
                                DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference()
                                        .child("groups").child(groupId).child("initialStatus");
                                dbRef.setValue(initialStatus).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        showToast("설정이 적용되었습니다");
                                    }
                                });
                            }
                        })
                        .show();
            }
        });

        binding.editInfoLayout.setOnClickListener(new View.OnClickListener() {
            DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference()
                    .child("groups").child(groupId).child("members")
                    .child(FirebaseAuth.getInstance().getUid()).child("name");
            @Override
            public void onClick(View view) {
                final EditText editText = new EditText(view.getContext());
                dbRef.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                        editText.setText(task.getResult().getValue(String.class));
                    }
                });

                AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                builder.setTitle("내 이름 편집");
                builder.setView(editText);
                builder.setPositiveButton("저장",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                String strText = editText.getText().toString().trim();
                                dbRef.setValue(strText).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            showToast("저장되었습니다");
                                        }
                                        else{
                                            showToast("오류. 다시 시도해주세요");
                                        }
                                    }
                                });
                            }
                        });
                builder.setNegativeButton("취소",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });
                builder.show();
            }
        });

    }

    void startDelete(){
        deleteMember();
        deleteSchedule();
        deleteGroup();
    }
    void deleteMember(){
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference()
                .child("groups").child(groupId).child("members");
        dbRef.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if(task.isSuccessful()){
                    for (DataSnapshot ds: task.getResult().getChildren()) {
                        deleteHostParticipants(ds.getKey());
                    }
                }
            }
        });
    }
    void deleteHostParticipants(String uid){
        DatabaseReference hostRef = FirebaseDatabase.getInstance().getReference()
                .child("users").child(uid).child("hostingGroups");
        hostRef.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if(task.isSuccessful()){
                    for (DataSnapshot ds: task.getResult().getChildren()) {
                        GroupInfo groupInfo = ds.getValue(GroupInfo.class);
                        if(groupInfo.groupId.equals(groupId)){
                            hostRef.child(ds.getKey()).removeValue();
                        }
                    }
                }
            }
        });
        DatabaseReference participantsRef = FirebaseDatabase.getInstance().getReference()
                .child("users").child(FirebaseAuth.getInstance().getUid()).child("participatingGroups");
        participantsRef.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if(task.isSuccessful()){
                    for (DataSnapshot ds: task.getResult().getChildren()) {
                        GroupInfo groupInfo = ds.getValue(GroupInfo.class);
                        if(groupInfo.groupId.equals(groupId)){
                            participantsRef.child(ds.getKey()).removeValue();
                        }
                    }
                }
            }
        });
    }

    void deleteSchedule(){
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference()
                .child("schedules").child(groupId);
        dbRef.removeValue();
    }
    void deleteGroup(){
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference()
                .child("groups").child(groupId);
        dbRef.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                showToast("해체되었습니다");
                finish();
            }
        });
    }

    void initMember(){
        binding.memberListLayout.setVisibility(View.VISIBLE);
        binding.copyGroupIdLayout.setVisibility(View.VISIBLE);
        binding.editInfoLayout.setVisibility(View.VISIBLE);
        binding.withdrawalLayout.setVisibility(View.VISIBLE);
        binding.withdrawalLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }



    private void showToast(String str){
        Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
    }
}