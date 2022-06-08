package com.example.daystarter.ui;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.daystarter.MainActivity;
import com.example.daystarter.R;
import com.example.daystarter.databinding.ActivityEditUserInfoBinding;
import com.example.daystarter.ui.weather.ProgressDialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class EditUserInfoActivity extends AppCompatActivity {
    ActivityEditUserInfoBinding binding;
    ActivityResultLauncher<Intent> resultLauncher;
    Uri profileUri;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditUserInfoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        init();
    }
    void init(){
        progressDialog = new ProgressDialog(this);
        setResultLauncher();
        downloadImage();

        binding.profileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pickImage();
            }
        });

        binding.saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                check();
            }
        });
    }

    void check(){
        if(binding.nameEditText.getText().toString().trim().length() <2){
            return;
        }
        progressDialog.show();
        uploadImage();

    }

    void setResultLauncher(){
        resultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if(result.getResultCode() == RESULT_OK) {
                            Intent intent = result.getData();
                            profileUri = intent.getData();
                            binding.profileImageView.setImageURI(profileUri);
                        }
                    }
                });
    }

    void pickImage(){
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        intent.setAction(Intent.ACTION_PICK);

        resultLauncher.launch(intent);
    }

    void downloadImage(){
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference().child("users")
                .child(FirebaseAuth.getInstance().getUid()).child("profileImgPath");
        dbRef.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                String imagePath = task.getResult().getValue(String.class);
                Glide.with(EditUserInfoActivity.this).load(imagePath).circleCrop().error(R.drawable.ic_baseline_person_24).into(binding.profileImageView);
            }
        });
    }

    void uploadImage(){
        StorageReference imageRef = FirebaseStorage.getInstance().getReference()
                .child("profileImages").child(FirebaseAuth.getInstance().getUid());
        if(profileUri != null) {
            imageRef.putFile(profileUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    final Task<Uri> uriTask = task.getResult().getStorage().getDownloadUrl();
                    while(!uriTask.isComplete()){}
                    DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference().child("users")
                            .child(FirebaseAuth.getInstance().getUid()).child("profileImgPath");
                    dbRef.setValue(uriTask.getResult().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            changeName();
                        }
                    });
                }
            });
        }
        else
            changeName();

    }

    void changeName(){
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference().child("users")
                .child(FirebaseAuth.getInstance().getUid()).child("name");
        dbRef.setValue(binding.nameEditText.getText().toString().trim()).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                progressDialog.dismiss();
                showToast("변경 완료");
                finish();
            }
        });
    }

    private void showToast(String str){
        Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
    }
}