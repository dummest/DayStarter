package com.example.daystarter.ui.groupSchedule;

import static android.content.ContentValues.TAG;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Calendar;

public class MakeGroupActivity extends AppCompatActivity {
    FirebaseUser user;
    MaterialButton button;
    EditText groupNameEditText, userNameEditText;
    ImageView groupImage;
    Uri uri;
    ActivityResultLauncher<Intent> resultLauncher;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make_group);

        progressDialog = new ProgressDialog(this);
        progressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        button = findViewById(R.id.make_group_button);
        groupNameEditText = findViewById(R.id.group_name_edit_text);
        userNameEditText = findViewById(R.id.name_edit_text);
        groupImage = findViewById(R.id.group_image_view);
        resultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if(result.getResultCode() == RESULT_OK) {
                            Intent intent = result.getData();
                            uri = intent.getData();
                            groupImage.setImageURI(uri);
                        }
                    }
                });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                makeGroup();
            }
        });

        groupImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                intent.setAction(Intent.ACTION_PICK);

                resultLauncher.launch(intent);
            }
        });
    }

    void makeGroup(){
        //EditText ?????? ?????? ?????? ?????? ??? ?????? ??????
        String groupName = groupNameEditText.getText().toString().trim();
        Log.d(TAG, "makeGroup: " + groupName);
        String userName = userNameEditText.getText().toString().trim();
        //???????????? ???????????? ?????? ??????
        if(groupName.length() < 1) {
            Toast.makeText(getBaseContext(), "???????????? ???????????????", Toast.LENGTH_SHORT).show();
            return;
        }
        if(userName.length() < 1){
            Toast.makeText(getBaseContext(), "????????? ???????????????", Toast.LENGTH_SHORT).show();
            return;
        }
            progressDialog.show();
            DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
            user = FirebaseAuth.getInstance().getCurrentUser();

            //?????? ????????? ??????(???????????? ????????? ?????? uid ??? ?????? ???????????? ??????)
            GroupInfo groupInfo = new GroupInfo(FirebaseAuth.getInstance().getCurrentUser().getUid() + Calendar.getInstance().getTimeInMillis());
            Log.d(TAG, "before Id: " + groupInfo.groupId);
            //????????? ????????? ????????? ????????? ?????????
            if (uri != null) {
                StorageReference storageRef = FirebaseStorage.getInstance().getReference();
                storageRef.child("groupImages").child(groupInfo.groupId).putFile(uri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        String imageUrl;
                        final Task<Uri> uriTask = task.getResult().getStorage().getDownloadUrl();
                        while(!uriTask.isComplete()){}
                        imageUrl = uriTask.getResult().toString();
                        Group group = new Group(groupInfo.groupId, groupName, user.getEmail(), imageUrl, "read", false);
                        dbRef.child("groups").child(groupInfo.groupId).setValue(group);
                        dbRef.child("users").child(user.getUid()).child("hostingGroups").child(groupInfo.groupId).setValue(groupInfo);

                        //group ?????? ?????? ???????????? ?????????
                        dbRef.child("groups").child(group.groupId).child("members").child(user.getUid()).setValue(new Member(userName, "host", user.getEmail(), FirebaseAuth.getInstance().getUid()));
                        progressDialog.dismiss();
                        finish();
                    }
                });
            }
            //???????????? ?????? ????????? ??????
            else{
                Group group = new Group(groupInfo.groupId, groupName, user.getEmail(), null, "read", false);
                dbRef.child("groups").child(groupInfo.groupId).setValue(group);
                dbRef.child("users").child(user.getUid()).child("hostingGroups").child(group.groupId).setValue(groupInfo);

                dbRef.child("groups").child(group.groupId).child("members").child(user.getUid()).setValue(new Member(userName, "host", user.getEmail(), FirebaseAuth.getInstance().getUid()));
                progressDialog.dismiss();
                finish();
            }
    }
}