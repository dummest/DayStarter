package com.example.daystarter.ui.groupSchedule;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.daystarter.R;
import com.example.daystarter.ui.groupSchedule.myClass.Group;
import com.example.daystarter.ui.groupSchedule.myClass.Member;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

public class ParticipationActivity extends AppCompatActivity {
    EditText groupCode, name;
    MaterialButton button;
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_participation);

        groupCode = findViewById(R.id.group_code_edit_text);
        name = findViewById(R.id.name_edit_text);
        button = findViewById(R.id.participate_group_button);



        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String code = groupCode.getText().toString().trim();
                if(code.length() == 0) {
                    showToast("그룹 코드를 입력하세요");
                    return;
                }
                String nameText = name.getText().toString().trim();
                if(nameText.length() < 2){
                    showToast("이름은 최소 2자여야 합니다");
                    return;
                }

                DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
                DatabaseReference childRef = dbRef.child("groups");
                DatabaseReference groupRef = childRef.child(code);

                groupRef.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                        if(task.isSuccessful()){
                            Group group = task.getResult().getValue(Group.class);
                            //만약 그룹을 받았을때 널이 아니면(해당 그룹코드를 가진 그룹이 존재하면)
                            if(group != null){
                                AlertDialog.Builder builder =  new AlertDialog.Builder(ParticipationActivity.this)
                                        .setTitle("알림")
                                        .setMessage("그룹 명 '" + group.groupName + "' 에 참가하시겠습니까?")
                                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                groupRef.child("members").child(user.getUid()).setValue(new Member(nameText, "read", user.getEmail()));
                                                dbRef.child("users").child(user.getUid()).child("participatingGroups").push().child("groupId").setValue(group.groupId);
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
                            //해당 그룹 코드를 가진 그룹이 없으면
                            else{
                                showToast("해당하는 코드를 가진 그룹이 존재하지 않습니다");
                            }
                        }
                    }
                });
                /*
                groupRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Group group = snapshot.getValue(Group.class);
                        if(group != null) {
                            AlertDialog.Builder builder =  new AlertDialog.Builder(ParticipationActivity.this)
                                    .setTitle("알림")
                                    .setMessage("그룹 명 '" + group.groupName + "' 에 참가하시겠습니까?")
                                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            groupRef.child("members").child(user.getUid()).setValue(new Member(nameText, "read", user.getEmail()));
                                            dbRef.child("users").child(user.getUid()).child("participatingGroups").push().child("groupId").setValue(group.groupId);
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
                        else{
                            showToast("해당하는 코드를 가진 그룹이 존재하지 않습니다");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
                */
            }
        });
    }


    private void showToast(String str){
        Toast.makeText(this, str, Toast.LENGTH_LONG).show();
    }


}