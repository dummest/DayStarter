package com.example.daystarter;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

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
                Intent intent;
                FirebaseAuth auth = FirebaseAuth.getInstance();
                if(auth.getCurrentUser().getUid() != null && !auth.getCurrentUser().getUid().isEmpty()) {
                    intent = new Intent(IntroActivity.this, MainActivity.class);
                }
                else
                    intent = new Intent(IntroActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
                overridePendingTransition(R.anim.view_come_from_down, R.anim.none);
            }
        }, 1500);
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }
}