package com.example.daystarter.ui.groupSchedule;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.daystarter.databinding.ActivityGroupBinding;
import com.example.daystarter.databinding.ActivityGroupSettingBinding;
import com.example.daystarter.ui.groupSchedule.myClass.GroupInfo;
import com.example.daystarter.ui.groupSchedule.myClass.Member;
import com.example.daystarter.ui.weather.ProgressDialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class GroupSettingActivity extends AppCompatActivity {
    ActivityGroupSettingBinding binding;
    String groupId;
    ProgressDialog progressDialog;
    boolean groupDeleted = false;
    boolean imageDeleted = false;
    boolean scheduleDeleted = false;
    boolean hostingGroupDeleted = false;
    boolean participatingGroupDeleted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGroupSettingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        init();

    }

    void init(){
        progressDialog = new ProgressDialog(this);
        groupId = getIntent().getStringExtra("groupId");
        if(groupId == null || groupId.isEmpty()){
            showToast("??????");
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
                                showToast("??????: ????????? ????????????");
                                finish();
                        }
                        binding.groupIdTextView.setText(groupId);
                        setClickListener();
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
        binding.joinLayout.setVisibility(View.VISIBLE);
        loadInitialStatus();



    }
    void loadInitialStatus(){
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference()
                .child("groups").child(groupId).child("initialStatus");

        dbRef.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                String initialStatus = task.getResult().getValue(String.class);
                if(initialStatus.equals("write"))
                    binding.initialStatusTextView.setText("?????? ?????? ??????: ??????");
                if(initialStatus.equals("read"))
                    binding.initialStatusTextView.setText("?????? ?????? ??????: ??????");
            }
        });
    }

    void startDelete(){
        progressDialog.show();
        loopMember();
    }
    void loopMember(){
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference()
                .child("groups").child(groupId).child("members");
        dbRef.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if(task.isSuccessful()){
                    long size = task.getResult().getChildrenCount();
                    int i = 0;
                    for (DataSnapshot ds: task.getResult().getChildren()) {
                        deleteHostParticipants(ds.getKey(), groupId, i, size);
                        i++;
                    }
                    deleteSchedule();
                }
                Log.d(TAG, "loopMember done");
            }
        });
    }
    void deleteHostParticipants(String uid, String groupId, long index, long size){
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference()
                .child("users").child(uid);
        usersRef.child("hostingGroups").child(groupId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(index == size-1){
                    hostingGroupDeleted = true;
                    checkFinished();
                }
            }
        });
        usersRef.child("participatingGroups").child(groupId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(index == size-1){
                    participatingGroupDeleted = true;
                    checkFinished();
                }
            }
        });
    }

    void deleteSchedule(){
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference()
                .child("schedules").child(groupId);
        dbRef.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    deleteGroup();
                }
                scheduleDeleted = true;
            }
        });
    }
    void deleteGroup(){
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference()
                .child("groups").child(groupId);
        dbRef.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()) {
                    deleteImage();
                }
                groupDeleted = true;
            }
        });
    }

    void deleteImage(){
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        storageRef.child("groupImages").child(groupId).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                imageDeleted = true;
                checkFinished();
            }
        });
    }

    void deleteMember(){
        progressDialog.show();
        DatabaseReference memberRef = FirebaseDatabase.getInstance().getReference().child("groups").
                child(groupId).child("members").child(FirebaseAuth.getInstance().getUid());
        DatabaseReference participateRef = FirebaseDatabase.getInstance().getReference().child("users")
                .child(FirebaseAuth.getInstance().getUid()).child("participatingGroups").child(groupId);
        memberRef.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    participateRef.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            showToast("?????? ??????");
                            if(progressDialog.isShowing())
                                progressDialog.dismiss();
                            finish();
                        }
                    });
                }
            }
        });
    }

    void initMember(){
        binding.copyGroupIdLayout.setVisibility(View.VISIBLE);
        binding.editInfoLayout.setVisibility(View.VISIBLE);
        binding.withdrawalLayout.setVisibility(View.VISIBLE);
    }

    private void showToast(String str){
        Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
    }

    void setClickListener(){
        binding.memberListLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(GroupSettingActivity.this, MemberListActivity.class);
                intent.putExtra("groupId", groupId);
                startActivity(intent);
            }
        });

        binding.withdrawalLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteMember();
            }
        });

        binding.copyGroupIdLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ClipboardManager clipboard = (ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("groupId", groupId);
                clipboard.setPrimaryClip(clip);
                showToast("?????????????????????");
            }
        });
        binding.deleteGroupLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext(),
                        android.R.style.ThemeOverlay_Material_Dialog)
                        .setTitle("??????")
                        .setMessage("????????? ????????????????????????? (??? ????????? ????????? ??? ????????????.)")
                        .setPositiveButton("?????? ??????", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                startDelete();
                            }
                        })
                        .setNegativeButton("??????", new DialogInterface.OnClickListener() {
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
                final CharSequence[] items = {"??????", "??????"};

                AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext(),
                        android.R.style.ThemeOverlay_Material_Dialog);

                builder.setTitle("?????? ?????? ??????")
                        .setItems(items, new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int position)
                            {
                                String initialStatus;
                                if(items[position].toString().trim().equals("??????"))
                                    initialStatus = "read";
                                else
                                    initialStatus = "write";
                                DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference()
                                        .child("groups").child(groupId).child("initialStatus");
                                dbRef.setValue(initialStatus).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        showToast("????????? ?????????????????????");
                                        loadInitialStatus();
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
                builder.setTitle("??? ?????? ??????");
                builder.setView(editText);
                builder.setPositiveButton("??????",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                String strText = editText.getText().toString().trim();
                                dbRef.setValue(strText).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            showToast("?????????????????????");
                                        }
                                        else{
                                            showToast("??????. ?????? ??????????????????");
                                        }
                                    }
                                });
                            }
                        });
                builder.setNegativeButton("??????", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });
                builder.show();
            }
        });

        binding.joinLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(GroupSettingActivity.this, ApplicantsActivity.class);
                intent.putExtra("groupId", groupId);
                startActivity(intent);
            }
        });
    }

    void checkFinished(){
        if(groupDeleted && imageDeleted && scheduleDeleted && hostingGroupDeleted && participatingGroupDeleted){
            if(progressDialog.isShowing())
                progressDialog.dismiss();

            showToast("?????????????????????");
            finish();
        }
    }
}