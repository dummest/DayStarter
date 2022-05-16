package com.example.daystarter.ui.groupSchedule;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.daystarter.R;
import com.example.daystarter.databinding.ActivityGroupSchedulePostBinding;

public class GroupSchedulePostActivity extends AppCompatActivity {
    ActivityGroupSchedulePostBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGroupSchedulePostBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


    }
}