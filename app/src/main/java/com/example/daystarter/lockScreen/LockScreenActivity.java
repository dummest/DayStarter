package com.example.daystarter.lockScreen;

import androidx.appcompat.app.AppCompatActivity;

import android.app.KeyguardManager;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.example.daystarter.R;
import com.example.daystarter.databinding.ActivityGroupBinding;
import com.example.daystarter.databinding.ActivityLockScreenBinding;

public class LockScreenActivity extends AppCompatActivity {
    ActivityLockScreenBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLockScreenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        KeyguardManager keyguardManager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
        setShowWhenLocked(true);


        binding.quitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        binding.refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });


    }
}