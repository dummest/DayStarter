package com.example.daystarter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.example.daystarter.ui.groupSchedule.myClass.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.RuntimeExecutionException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.internal.zzx;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class IntroActivity extends AppCompatActivity {
    private static String TAG = "IntroActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_intro);
        setContentView(R.layout.activity_animation_intro);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if(user != null) {
                    DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference()
                            .child("users").child(FirebaseAuth.getInstance().getUid());
                    dbRef.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DataSnapshot> task) {
                            Intent intent;
                            if (task.getResult().exists()) {
                                try {
                                    throw task.getException();
                                } catch (Exception e) {
                                    Log.d(TAG, e.toString());
                                }
                                intent = new Intent(IntroActivity.this, MainActivity.class);
                            }
                            else {
                                intent = new Intent(IntroActivity.this, LoginActivity.class);
                            }
                            startActivity(intent);
                            finish();
                        }
                    });
                }
                else{
                    Intent intent = new Intent(IntroActivity.this, LoginActivity.class);
                    startActivity(intent);
                }
            }
        }, 1000);
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }
}