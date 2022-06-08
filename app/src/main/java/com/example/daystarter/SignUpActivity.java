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
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.internal.api.FirebaseNoSignedInUserException;
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
    ProgressDialog progressDialog;
    String email, password, passwordCheck, name;

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
        name = binding.nameEditText.getText().toString().trim();
        //이름 칸에 2자 이상 기입하지 않을 시
        if(name.length() < 2){
            showToast("이름을 2자 이상 입력해 주세요");
            return;
        }

        email = String.valueOf(((EditText)findViewById(R.id.sign_up_email_edit_text)).getText());
        password = String.valueOf(((EditText)findViewById(R.id.sign_up_password_edit_text)).getText());
        passwordCheck = String.valueOf(((EditText)findViewById(R.id.sign_up_password_check_Edit_Text)).getText());
        if(password.equals(passwordCheck)) {
            progressDialog.show();
            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    //성공시
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        uploadImage(user);
                    }
                    else {
                        progressDialog.dismiss();
                        try {
                            throw task.getException();
                        } catch(FirebaseAuthWeakPasswordException e) {
                            showToast("weekPassword");
                        } catch(FirebaseAuthInvalidCredentialsException e) {
                            showToast("invalidCredential");
                        } catch(FirebaseAuthUserCollisionException e) {
                            showToast("usercollisionexception");
                        } catch (FirebaseNoSignedInUserException e){
                            showToast("noSignedInUserException");
                        }
                        catch(Exception e) {
                            showToast(e.getMessage());
                        }
                    }
                }
            });
        }else
            showToast("비밀번호가 일치하지 않습니다");
    }

    void uploadImage(FirebaseUser user){
        storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        if(uri != null) {
            storageRef.child("profileImages").child(user.getUid()).putFile(uri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if (task.isSuccessful()) {
                        final Task<Uri> uriTask = task.getResult().getStorage().getDownloadUrl();
                        while (!uriTask.isComplete()) {
                        }
                        String imageUrl = uriTask.getResult().toString();
                        makeUserDB(user, imageUrl);
                    } else {
                        progressDialog.dismiss();
                        try {
                            throw task.getException();
                        } catch (Exception e) {
                            showToast(e.getMessage());
                        }
                    }
                }
            });
        }
        else{
            makeUserDB(user, null);
        }
    }

    void makeUserDB(FirebaseUser user, String imageUrl){
        User userData;
        if(imageUrl != null)
            userData = new User(user.getUid(), user.getEmail(), name, imageUrl);
        else
            userData = new User(user.getUid(), user.getEmail(), name, null);
        dbRef = FirebaseDatabase.getInstance().getReference();

        dbRef.child("users").child(user.getUid()).setValue(userData).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()) {
                    progressDialog.dismiss();
                    showToast("회원가입 완료.");
                    finish();
                }
                else{
                    progressDialog.dismiss();
                    try {
                        throw task.getException();
                    }
                    catch(Exception e) {
                        showToast(e.getMessage());
                    }
                }
            }
        });
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