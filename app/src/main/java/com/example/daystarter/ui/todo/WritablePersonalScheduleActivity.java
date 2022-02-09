package com.example.daystarter.ui.todo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.daystarter.R;
import com.example.daystarter.databinding.ActivityWritablePersonalScheduleBinding;

public class WritablePersonalScheduleActivity extends AppCompatActivity {
    private ActivityWritablePersonalScheduleBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWritablePersonalScheduleBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent intent = getIntent();

        int year = intent.getIntExtra("year", 2022);
        int month = intent.getIntExtra("month", 1);
        int day = intent.getIntExtra("day", 1);
        int hour = intent.getIntExtra("hour", 0);
        int minute = intent.getIntExtra("minute", 0);

        binding.beforeDateTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }
}