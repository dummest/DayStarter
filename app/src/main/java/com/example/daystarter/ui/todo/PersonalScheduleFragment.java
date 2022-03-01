package com.example.daystarter.ui.todo;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.daystarter.R;
import com.example.daystarter.adapter.ScheduleListViewAdapter;
import com.example.daystarter.adapter.ScheduleRecyclerViewAdapter;
import com.example.daystarter.databinding.FragmentPersonalScheduleBinding;
import com.example.daystarter.myClass.PersonalScheduleDBHelper;
import com.example.daystarter.myClass.ScheduleData;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;

import org.threeten.bp.DayOfWeek;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;


public class PersonalScheduleFragment extends Fragment {
    private FragmentPersonalScheduleBinding binding;
    Context context;
    private static final String TAG = "PersonalScheduleFragment";
    ScheduleRecyclerViewAdapter adapter;
    MaterialCalendarView mcv;
    Calendar SelectedCalendar;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentPersonalScheduleBinding.inflate(inflater, container, false);

        ///////////////////////////////////////MaterialCalendarView set///////////////////////////////////
        ///////////////////////////////////////MaterialCalendarView set///////////////////////////////////
        mcv = binding.mcvViewGroup.calendar;

        setFirst();
        mcv.setOnDateChangedListener(new OnDateSelectedListener() {
            /*
                    Note: 자바에서 Calendar는 Month가 0부터 시작하지만(영어권에서는 달을 숫자가 아닌 영어로 부름 ex)March
                    MCV는 Month가 1부터 시작함. 고로 연산할때 주의할 것.
                    */
            @Override
            public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {
                SelectedCalendar = new GregorianCalendar(date.getYear(), date.getMonth()-1, date.getDay());
                listViewLoad(SelectedCalendar.getTimeInMillis());
            }
        });
        mcv.addDecorators(
                new DaySelector(getContext()),
                new SaturdayDecorator(),
                new SundayDecorator(),
                new TodayDecorator());
        ///////////////////////////////////////MaterialCalendarView set///////////////////////////////////
        ///////////////////////////////////////MaterialCalendarView set///////////////////////////////////


        binding.personalScheduleFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), WritablePersonalScheduleActivity.class);
                Calendar calendar = new GregorianCalendar();

                //선택한 날이 오늘일 시
                if(mcv.getSelectedDate().equals(CalendarDay.today())) {
                    calendar.set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY)+1);
                    calendar.set(Calendar.MINUTE, 0);
                }
                //선택한 날이 오늘이 아닐 시
                else{
                    CalendarDay cd = mcv.getSelectedDate();
                    calendar.set(cd.getYear(), cd.getMonth()-1, cd.getDay(), 8, 0);
                    /*
                    Note: 자바에서 Calendar는 Month가 0부터 시작하지만(영어권에서는 달을 숫자가 아닌 영어로 부름 ex)March
                    MCV는 Month가 1부터 시작함. 고로 연산할때 주의할 것.
                    */
                }
                intent.putExtra("beforeLong", calendar.getTimeInMillis());

                calendar.add(Calendar.HOUR_OF_DAY, 1);
                intent.putExtra("afterLong", calendar.getTimeInMillis());

                startActivity(intent);
            }
        });
        return binding.getRoot();
    }
    //초기설정, mcv의 처음 선택을 오늘로, 리스트뷰도 불러옴
    public void setFirst(){
        CalendarDay today = CalendarDay.today();
        mcv.setSelectedDate(today);
        SelectedCalendar = new GregorianCalendar(today.getYear(), today.getMonth()-1, today.getDay());
        listViewLoad(SelectedCalendar.getTimeInMillis());
    }


    public void listViewLoad(long time){
        PersonalScheduleDBHelper dbHelper = new PersonalScheduleDBHelper(context);
        ArrayList<ScheduleData> list = dbHelper.getScheduleList(time);
        adapter = new ScheduleRecyclerViewAdapter(list, context);
        adapter.setOnItemClickListener(new ScheduleRecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                ScheduleData data = adapter.getItem(position);
                Intent intent = new Intent(context, WritablePersonalScheduleActivity.class);
                intent.putExtra("scheduleId", data.getScheduleId());
                intent.putExtra("title", data.getTitle());
                intent.putExtra("beforeLong", data.getStartTime());
                intent.putExtra("afterLong", data.getEndTime());
                intent.putExtra("memo", data.getMemo());
                intent.putExtra("address", data.getAddress());
                intent.putExtra("imgPath", data.getImgPath());
                context.startActivity(intent);
            }
        });

        adapter.setOnItemLongClickListener(new ScheduleRecyclerViewAdapter.OnItemLongClickListener() {
            @Override
            public void onItemLongClick(View view, int position) {
                ScheduleData data = adapter.getItem(position);
                dbHelper.deleteSchedule(data.getScheduleId());
                listViewLoad(SelectedCalendar.getTimeInMillis());
            }
        });

        LinearLayoutManager manager = new LinearLayoutManager(getContext());
        adapter.notifyDataSetChanged();
        binding.scheduleRecyclerView.setAdapter(adapter);
        binding.scheduleRecyclerView.setLayoutManager(manager);
        binding.scheduleRecyclerView.invalidate();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onResume() {
        super.onResume();
        listViewLoad(SelectedCalendar.getTimeInMillis());
    }

    ///////////////////////////mcv Decorator/////////////////////////////
    ///////////////////////////mcv Decorator/////////////////////////////
    private static class DaySelector implements DayViewDecorator {
        //selector decorator
        private final Drawable drawable;

        public DaySelector(Context context) {
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

    private class SaturdayDecorator implements DayViewDecorator{
        //saturday decorator(make text blue)
        @Override
        public boolean shouldDecorate(CalendarDay day) {
            int saturday = day.getDate().with(DayOfWeek.SATURDAY).getDayOfMonth();
            return day.getDay() == saturday;
        }

        @Override
        public void decorate(DayViewFacade view) {
            view.addSpan(new ForegroundColorSpan(getResources().getColor(R.color.blue, getActivity().getTheme())));
        }
    }

    private class SundayDecorator implements DayViewDecorator{
        //sunday decorator(make text red)
        @Override
        public boolean shouldDecorate(CalendarDay day) {
            int sunday = day.getDate().with(DayOfWeek.SUNDAY).getDayOfMonth();
            return day.getDay() == sunday;
        }

        @Override
        public void decorate(DayViewFacade view) {
            view.addSpan(new ForegroundColorSpan(getResources().getColor(R.color.red, getActivity().getTheme())));
        }
    }

    private class TodayDecorator implements DayViewDecorator{
        @Override
        public boolean shouldDecorate(CalendarDay day) {
            return day.equals(CalendarDay.today());
        }

        @Override
        public void decorate(DayViewFacade view) {
            view.addSpan(new ForegroundColorSpan(getResources().getColor(R.color.green, getActivity().getTheme())));
            view.addSpan(new StyleSpan(Typeface.BOLD));
            view.addSpan(new UnderlineSpan());
        }
    }
    ///////////////////////////mcv Decorator/////////////////////////////
    ///////////////////////////mcv Decorator/////////////////////////////
}