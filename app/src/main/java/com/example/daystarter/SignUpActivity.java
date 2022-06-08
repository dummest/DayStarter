package com.example.daystarter;

import static android.content.ContentValues.TAG;

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
import android.widget.Toast;

import com.example.daystarter.databinding.ActivitySignUpBinding;
import com.example.daystarter.ui.groupSchedule.myClass.User;
import com.example.daystarter.ui.weather.ProgressDialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class SignUpActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    ActivitySignUpBinding binding;
    ActivityResultLauncher<Intent> resultLauncher;
    Uri uri;
    FirebaseStorage storage;
    DatabaseReference dbRef;
    String imageUrl;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();

        binding.signUpButton.setOnClickListener(onClickListener);
        binding.profileImageView.setOnClickListener(onClickListener);

        setResultLauncher();
        progressDialog = new ProgressDialog(this);
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.sign_up_button:
                    signUp();
                    break;
                case R.id.profile_image_view:
                    pickImage();
                    break;
            }
        }
    };

    private void signUp() {
        progressDialog.show();
        String name = binding.nameEditText.getText().toString().trim();
        //이름 칸에 2자 이상 기입하지 않을 시
        if(name.length() < 2){
            showToast("이름을 2자 이상 입력해 주세요");
            return;
        }

        String email = String.valueOf(((EditText)findViewById(R.id.sign_up_email_edit_text)).getText());
        String password = String.valueOf(((EditText)findViewById(R.id.sign_up_password_edit_text)).getText());
        String passwordCheck = String.valueOf(((EditText)findViewById(R.id.sign_up_password_check_Edit_Text)).getText());
        if(password.equals(passwordCheck)) {
            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    //성공시
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        //프로필 사진 있을 시 uid 와 같은 이름으로 사진을 위치 profileImage/에 업로드 해줌

                            //storage = 파이어베이스 스토리지 참조 객체
                            storage = FirebaseStorage.getInstance();
                            StorageReference storageRef = storage.getReference();
                            //파이어베이스 스토리지 안의
                            storageRef.child("profileImages").child(user.getUid()).putFile(uri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                    final Task<Uri> uriTask = task.getResult().getStorage().getDownloadUrl();
                                    while(!uriTask.isComplete()){}

                                    imageUrl = uriTask.getResult().toString();
                                    Log.d(TAG, "onComplete: " + imageUrl);

                                    User userData = new User(user.getUid(), user.getEmail(), name, imageUrl);
                                    dbRef = FirebaseDatabase.getInstance().getReference();
                                    dbRef.child("users").child(user.getUid()).setValue(userData).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            progressDialog.dismiss();
                                            showToast("회원가입 완료.");
                                            finish();
                                        }
                                    });
                                }
                            });

                        /*
                        User userData = new User(user.getUid(), user.getEmail(), name, null);
                        dbRef = FirebaseDatabase.getInstance().getReference();
                        dbRef.child("users").child(user.getUid()).setValue(userData);
                         */
                    }
                    else {
                        showToast(task.getException().toString());
                    }
                }
            });
        }else
            showToast("비밀번호가 일치하지 않습니다");
    }

    void setResultLauncher(){
        resultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if(result.getResultCode() == RESULT_OK) {
                            Intent intent = result.getData();
                            uri = intent.getData();
                            binding.profileImageView.setImageURI(uri);
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

    private void showToast(String str){
        Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
    }
}