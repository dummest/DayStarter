package com.example.daystarter.lockScreen;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
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
    LockScreenAdapter adapter;
    public static LockScreenActivity activity;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLockScreenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        adapter = new LockScreenAdapter(this);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        binding.scheduleRecyclerView.setLayoutManager(layoutManager);
        binding.scheduleRecyclerView.setAdapter(adapter);
        activity = this;

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
                adapter.init();
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        activity = null;
    }
}