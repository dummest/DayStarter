package com.example.daystarter.ui.todo;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.daystarter.R;
import com.example.daystarter.databinding.FragmentPersonalScheduleBinding;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;


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

        MaterialCalendarView mcv = binding.mcvLayout.calendar;

        binding.mcvLayout.calendar.setOnDateChangedListener(new OnDateSelectedListener() {
            @Override
            public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {
                year = mcv.getSelectedDate().getYear();
                month = mcv.getSelectedDate().getMonth();
                day = mcv.getSelectedDate().getDay();
                Log.d(TAG, "onDateSelected: ");
            }
        });

        mcv.addDecorator(new DayDecorator(getContext()));

        mcv.setSelectedDate(CalendarDay.today());

        year = mcv.getSelectedDate().getYear();
        month = mcv.getSelectedDate().getMonth();
        day = mcv.getSelectedDate().getDay();



        binding.personalScheduleFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Snackbar.make(view, "" + year + "/" + month + "/" + day, Snackbar.LENGTH_SHORT).show();
                Intent intent = new Intent(getContext(), WritablePersonalScheduleActivity.class);
                intent.putExtra("beforeYear", year);
                intent.putExtra("beforeMonth", month);
                intent.putExtra("beforeDay", day);
                Calendar calendar1 = new GregorianCalendar();
                intent.putExtra("beforeHour", calendar1.get(Calendar.HOUR_OF_DAY));
                intent.putExtra("beforeMinute", calendar1.get(Calendar.MINUTE));
                startActivity(intent);
            }
        });



        return v;
    }

    private static class DayDecorator implements DayViewDecorator {

        private final Drawable drawable;

        public DayDecorator(Context context) {
            drawable = ContextCompat.getDrawable(context, R.drawable.mcv_selector);
        }

        // true를 리턴 시 모든 요일에 내가 설정한 드로어블이 적용된다
        @Override
        public boolean shouldDecorate(CalendarDay day) {
            return true;
        }

        // 일자 선택 시 내가 정의한 드로어블이 적용되도록 한다
        @Override
        public void decorate(DayViewFacade view) {
            view.setSelectionDrawable(drawable);
//            view.addSpan(new StyleSpan(Typeface.BOLD));   // 달력 안의 모든 숫자들이 볼드 처리됨
        }
    }

    @Override
    public void onResume() {
        super.onResume();

    }
}