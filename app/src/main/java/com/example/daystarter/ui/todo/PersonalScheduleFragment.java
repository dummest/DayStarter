package com.example.daystarter.ui.todo;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.daystarter.MainActivity;
import com.example.daystarter.R;
import com.example.daystarter.databinding.ActivityMainBinding;
import com.example.daystarter.databinding.FragmentPersonalScheduleBinding;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.snackbar.SnackbarContentLayout;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;

public class PersonalScheduleFragment extends Fragment {
    private FragmentPersonalScheduleBinding binding;

    private static final String TAG = "PersonalScheduleFragment";
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentPersonalScheduleBinding.inflate(inflater, container, false);
        View v = binding.getRoot();

        MaterialCalendarView calendar = v.findViewById(R.id.calendar);

        calendar.setOnDateChangedListener(new OnDateSelectedListener() {
            @Override
            public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {
                Log.d(TAG, "onDateSelected: ");
            }
        });

        binding.personalScheduleFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int year = calendar.getSelectedDate().getYear();
                int month = calendar.getSelectedDate().getMonth();
                int day = calendar.getSelectedDate().getDay();
                Snackbar.make(view, "" + year + "/" + month + "/" + day, Snackbar.LENGTH_SHORT).show();
            }
        });



        return v;
    }

    @Override
    public void onResume() {
        super.onResume();

    }
}