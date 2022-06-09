package com.example.daystarter.ui.groupSchedule;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.daystarter.R;
import com.example.daystarter.ui.groupSchedule.myClass.Group;
import com.example.daystarter.ui.groupSchedule.myClass.GroupInfo;
import com.example.daystarter.ui.groupSchedule.myClass.Member;
import com.example.daystarter.ui.weather.ProgressDialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


//이 액티비티는 중복을 체크하기위해 버튼을 누를 시 onClick -> checkHosting -> checkParticipating -> participateGroup
//순으로 작동함
public class ParticipationActivity extends AppCompatActivity {
    EditText groupCodeEdt, nameEdt;
    MaterialButton button;
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    String nameText;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_participation);
        groupCodeEdt = findViewById(R.id.group_code_edit_text);
        nameEdt = findViewById(R.id.name_edit_text);
        button = findViewById(R.id.participate_group_button);
        progressDialog = new ProgressDialog(this);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String groupId = groupCodeEdt.getText().toString().trim();

                if(groupId.length() == 0) {
                    showToast("그룹 코드를 입력하세요");
                    return;
                }
                nameText = nameEdt.getText().toString().trim();
                if(nameText.length() < 1){
                    showToast("이름은 최소 1자여야 합니다");
                    return;
                }
                checkHosting(groupId);
            }
        });
    }
    void participateGroup(String groupId){
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
        DatabaseReference groupRef = dbRef.child("groups").child(groupId);
        progressDialog.show();
        groupRef.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if(task.isSuccessful()){
                    progressDialog.dismiss();
                    Group group = task.getResult().getValue(Group.class);
                    //만약 그룹을 받았을때 널이 아니면(해당 그룹코드를 가진 그룹이 존재하면) 가입 확인 Alert 창 표시
                    if(group != null){
                        //그룹이 자동가입일 경우
                        if(group.autoApprove)
                            joinGroup(group);
                        else
                            proposeGroup(group);
                    }
                    //해당 그룹 코드를 가진 그룹이 없으면 토스트 출력
                    else{
                        showToast("그룹을 찾을 수 없습니다");
                    }
                }
            }
        });
    }

    // TODO: 2022/05/02 그룹에 참여해 있는지 여부를 확인해서 진행해야
    void checkHosting(String groupId){
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
        dbRef.child("users").child(FirebaseAuth.getInstance().getUid()).child("hostingGroups").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                Log.d(TAG, "hosting onComplete");
                for(DataSnapshot snapshot : task.getResult().getChildren()){
                    Log.d(TAG, "hosting snapshot: " + snapshot.getValue(GroupInfo.class).groupId);
                    if(snapshot.getValue(GroupInfo.class).groupId.equals(groupId)){
                        showToast("해당 그룹에 이미 가입되어 있습니다");
                        return;
                    }
                }
                checkParticipating(groupId);
            }
        });
        return;
    }

    void checkParticipating(String groupId){
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
        dbRef.child("users").child(FirebaseAuth.getInstance().getUid()).child("participatingGroups").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                for(DataSnapshot snapshot : task.getResult().getChildren()){
                    if(snapshot.getValue(GroupInfo.class).groupId.equals(groupId)){
                        showToast("해당 그룹에 이미 가입되어 있습니다");
                        return;
                    }
                }
                participateGroup(groupId);
            }
        });
    }

    void joinGroup(Group group){
        AlertDialog.Builder builder =  new AlertDialog.Builder(ParticipationActivity.this)
                .setTitle("알림")
                .setMessage("그룹 명 '" + group.groupName + "' 에 참가하시겠습니까?")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        progressDialog.show();
                        DatabaseReference memberRef = FirebaseDatabase.getInstance().getReference().child("groups").child(group.groupId).child("members").child(user.getUid());
                        while(memberRef.setValue(new Member(nameText, group.initialStatus, user.getEmail(), FirebaseAuth.getInstance().getUid())).isComplete()){};

                        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users").child(user.getUid());
                        while(userRef.child("participatingGroups").child(group.groupId).setValue(new GroupInfo(group.groupId)).isComplete()){}
                        showToast("가입이 완료되었습니다");
                        progressDialog.dismiss();
                        finish();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    void proposeGroup(Group group){
        AlertDialog.Builder builder =  new AlertDialog.Builder(ParticipationActivity.this)
                .setTitle("알림")
                .setMessage("그룹 명 '" + group.groupName + "' 에 가입을 신청하시겠습니까?")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        progressDialog.show();
                        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference()
                                .child("applicants").child(group.groupId).child(user.getUid());
                        dbRef.setValue(new Member(nameText, user.getEmail(), user.getUid())).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                progressDialog.dismiss();
                                showToast("가입 신청이 완료되었습니다");
                                finish();
                            }
                        });
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    void showToast(String str){
        Toast.makeText(this, str, Toast.LENGTH_LONG).show();
    }
}