package com.example.daystarter.ui.alarm.createalarm;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.daystarter.ui.alarm.data.Alarm;
import com.example.daystarter.ui.alarm.data.AlarmRepository;

import java.util.List;

public class CreateAlarmViewModel extends AndroidViewModel {
    private AlarmRepository alarmRepository;
    private LiveData<List<Alarm>> alarmsLiveData;

    public CreateAlarmViewModel(@NonNull Application application) {
        super(application);

        alarmRepository = new AlarmRepository(application);
        alarmsLiveData = alarmRepository.getAlarmsLiveData();
    }

    public void insert(Alarm alarm) {
        alarmRepository.insert(alarm);
    }

    public void update(Alarm alarm){
        alarmRepository.update(alarm);
    }

    public void delete(Alarm alarm){alarmRepository.delete(alarm);}
}
