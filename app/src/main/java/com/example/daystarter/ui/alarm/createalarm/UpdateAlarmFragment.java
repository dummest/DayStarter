package com.example.daystarter.ui.alarm.createalarm;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.example.daystarter.R;
import com.example.daystarter.ui.alarm.alarmslist.AlarmRecyclerViewAdapter;
import com.example.daystarter.ui.alarm.alarmslist.AlarmViewHolder;
import com.example.daystarter.ui.alarm.alarmslist.AlarmsListViewModel;
import com.example.daystarter.ui.alarm.data.Alarm;

import butterknife.BindView;
import butterknife.ButterKnife;

public class UpdateAlarmFragment extends Fragment {
    @BindView(R.id.fragment_createalarm_timePicker) TimePicker timePicker;
    @BindView(R.id.fragment_createalarm_title) EditText title;
    @BindView(R.id.fragment_createalarm_scheduleAlarm) Button scheduleAlarm;
    @BindView(R.id.fragment_createalarm_recurring) CheckBox recurring;
    @BindView(R.id.fragment_createalarm_checkMon) CheckBox mon;
    @BindView(R.id.fragment_createalarm_checkTue) CheckBox tue;
    @BindView(R.id.fragment_createalarm_checkWed) CheckBox wed;
    @BindView(R.id.fragment_createalarm_checkThu) CheckBox thu;
    @BindView(R.id.fragment_createalarm_checkFri) CheckBox fri;
    @BindView(R.id.fragment_createalarm_checkSat) CheckBox sat;
    @BindView(R.id.fragment_createalarm_checkSun) CheckBox sun;
    @BindView(R.id.fragment_createalarm_recurring_options) LinearLayout recurringOptions;
    private CreateAlarmViewModel createAlarmViewModel;
    AlarmRecyclerViewAdapter alarmRecyclerViewAdapter;
    int notId = 0;
    AlarmViewHolder alarmViewHolder = null;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        createAlarmViewModel = new ViewModelProvider(this).get(CreateAlarmViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_createalarm, container, false);

        ButterKnife.bind(this, view);

        recurring.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    recurringOptions.setVisibility(View.VISIBLE);
                } else {
                    recurringOptions.setVisibility(View.GONE);
                }
            }
        });

        scheduleAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("변경","update");
                    scheduleUpdateAlarm();
                Navigation.findNavController(v).navigate(R.id.action_updateAlarmFragment_to_nav_alarm);
            }
        });

        return view;
    }

    private void scheduleUpdateAlarm() {

        int alarmId=getId();
        Alarm alarm = new Alarm(
                alarmId,
                TimePickerUtil.getTimePickerHour(timePicker),
                TimePickerUtil.getTimePickerMinute(timePicker),
                title.getText().toString(),
                System.currentTimeMillis(),
                true,
                recurring.isChecked(),
                mon.isChecked(),
                tue.isChecked(),
                wed.isChecked(),
                thu.isChecked(),
                fri.isChecked(),
                sat.isChecked(),
                sun.isChecked()
        );
        createAlarmViewModel.update(alarm);
        alarm.schedule(getContext());
    }
}