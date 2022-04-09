package com.example.daystarter.ui.groupSchedule;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.daystarter.R;
import com.example.daystarter.ui.groupSchedule.myClass.Group;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
    private DatabaseReference dbRef;
    private String key;
    private FirebaseStorage storage;
    FirebaseUser user;

    MaterialButton button;
    EditText edt;
    ImageView groupImage;
    Uri uri;
    ActivityResultLauncher<Intent> resultLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make_group);

        button = findViewById(R.id.make_group_button);
        edt = findViewById(R.id.group_name_edit_text);
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
                //그룹 기본키 설정(기본키는 임시로 유저 uid와 생성 시간으로 결정)
                dbRef = FirebaseDatabase.getInstance().getReference();
                user = FirebaseAuth.getInstance().getCurrentUser();
                key = FirebaseAuth.getInstance().getCurrentUser().getUid();

                Calendar calendar = Calendar.getInstance();
                key += calendar.getTimeInMillis();
                String groupName = edt.getText().toString().trim();

                //그룹명을 기입한 경우
                if(groupName.length() > 0) {
                    //이미지 넣었을 경우만 이미지 업로드
                    if (uri != null) {

                        storage = FirebaseStorage.getInstance();
                        StorageReference storageRef = storage.getReference();
                        StorageReference pathRef = storageRef.child("group_image/" + key);


                        /*TODO 액티비티에서 이미지 다운 후 띄워주는 코드?
                        pathRef.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {
                                Glide.with(getBaseContext()).load(pathRef).into(groupImage);
                            }

                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                            }
                        });
                        */

                        UploadTask uploadTask = pathRef.putFile(uri);
                        uploadTask.addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(getBaseContext(), "이미지 업로드 실패, 다시 시도해보세요", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                Group group = new Group(key, groupName, user.getEmail());
                                dbRef.child("groups").child(key).setValue(group);
                                finish();
                            }
                        });
                    }
                    //이미지를 넣지 않았을 경우
                    else{
                        Group group = new Group(key, groupName, user.getEmail());
                        dbRef.child("groups").child(key).setValue(group);
                        finish();
                    }
                }
                //그룹명을 기입하지 않은 경우
                else{
                    Toast.makeText(getBaseContext(), "그룹명은 필수입니다", Toast.LENGTH_SHORT).show();
                }
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
}