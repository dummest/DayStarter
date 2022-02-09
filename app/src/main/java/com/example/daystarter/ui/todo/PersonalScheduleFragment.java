package com.example.daystarter.ui.todo;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.daystarter.R;
import com.example.daystarter.databinding.FragmentPersonalScheduleBinding;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;


public class PersonalScheduleFragment extends Fragment {
    private FragmentPersonalScheduleBinding binding;
    private static final String TAG = "PersonalScheduleFragment";

    int year;
    int month;
    int day;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentPersonalScheduleBinding.inflate(inflater, container, false);
        View v = binding.getRoot();

        MaterialCalendarView calendar = v.findViewById(R.id.calendar);

        calendar.setOnDateChangedListener(new OnDateSelectedListener() {
            @Override
            public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {
                year = calendar.getSelectedDate().getYear();
                month = calendar.getSelectedDate().getMonth();
                day = calendar.getSelectedDate().getDay();
                Log.d(TAG, "onDateSelected: ");
            }
        });
        calendar.setSelectedDate(CalendarDay.today());
        year = calendar.getSelectedDate().getYear();
        month = calendar.getSelectedDate().getMonth();
        day = calendar.getSelectedDate().getDay();

        binding.personalScheduleFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Snackbar.make(view, "" + year + "/" + month + "/" + day, Snackbar.LENGTH_SHORT).show();
                Intent intent = new Intent(getContext(), WritablePersonalScheduleActivity.class);
                intent.putExtra("year", year);
                intent.putExtra("month", month);
                intent.putExtra("day", day);
                startActivity(intent);
            }
        });



        return v;
    }

    @Override
    public void onResume() {
        super.onResume();

    }
}