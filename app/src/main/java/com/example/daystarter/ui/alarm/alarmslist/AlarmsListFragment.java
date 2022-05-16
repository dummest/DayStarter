package com.example.daystarter.ui.alarm.alarmslist;

import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.daystarter.R;
import com.example.daystarter.ui.alarm.data.Alarm;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

public class AlarmsListFragment extends Fragment implements OnToggleAlarmListener {
    private AlarmRecyclerViewAdapter alarmRecyclerViewAdapter;
    private AlarmsListViewModel alarmsListViewModel;
    private RecyclerView alarmsRecyclerView;
    private Button addAlarm,searchAlarm;

    //swipeHelperCallback= new SwipeHelperCallback(alarmRecyclerViewAdapter);
//itemTouchHelper = new ItemTouchHelper(swipeHelperCallback);
//itemTouchHelper.attachToRecyclerView(alarmsRecyclerView);


    ArrayList<Alarm> alarms = new ArrayList<Alarm>();


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        alarmRecyclerViewAdapter = new AlarmRecyclerViewAdapter(this);
        alarmsListViewModel = new ViewModelProvider(this).get(AlarmsListViewModel.class);
        alarmsListViewModel.getAlarmsLiveData().observe(this, new Observer<List<Alarm>>() {
            @Override
            public void onChanged(List<Alarm> alarms) {
                if (alarms != null) {
                    alarmRecyclerViewAdapter.setAlarms(alarms);
                    alarmsRecyclerView.startLayoutAnimation();
                }
            }

        });

    /*
        LinearLayoutManager manager = new LinearLayoutManager(getContext());
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        alarmsRecyclerView.setLayoutManager(manager);

        alarmsRecyclerView.setAdapter(alarmRecyclerViewAdapter);

        touchHelper = new ItemTouchHelper(new SwipeHelperCallback(alarmRecyclerViewAdapter));
        touchHelper.attachToRecyclerView(alarmsRecyclerView);
    }
        private void setUpRecyclerView(){
            alarmsRecyclerView.addItemDecoration(new RecyclerView.ItemDecoration()
            {
                @Override public void onDraw(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state)
                {
                    touchHelper.onDraw(c,parent, state);
                }
            });
        }
        */
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d("msg","oncreate 실행2");
        View view = inflater. inflate(R.layout.fragment_listalarms, container, false);

        alarmsRecyclerView = view.findViewById(R.id.fragment_alarms_View);
        alarmsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        alarmRecyclerViewAdapter = new AlarmRecyclerViewAdapter(this);
        alarmsRecyclerView.setAdapter(alarmRecyclerViewAdapter);

        addAlarm = view.findViewById(R.id.fragment_listalarms_addAlarm);
        searchAlarm =view.findViewById(R.id.search_button);

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                Log.d("swipe", "스와이프");
                int position = viewHolder.getAdapterPosition();
                switch (direction) {
                    case ItemTouchHelper.LEFT:
                        try {
                            Alarm delete = alarmRecyclerViewAdapter.getItem(position);
                            //Alarm delete = alarms.get(position);
                            alarmsListViewModel.delete(delete);
                            delete.cancelAlarm(getActivity());
                            alarmRecyclerViewAdapter.removeItem(position);
                            alarmRecyclerViewAdapter.notifyItemRemoved(position);

                            Snackbar.make(alarmsRecyclerView, delete.getTitle(), Snackbar.LENGTH_LONG)
                                    .setAction("복구", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {

                                            alarms.add(position, delete);
                                            alarmRecyclerViewAdapter.addItem(position, delete);
                                            alarmRecyclerViewAdapter.notifyItemInserted(position);
                                        }
                                    }).show();
                            break;
                        }catch(IndexOutOfBoundsException e){
                            e.printStackTrace();
                        }
                }
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                new RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                        .addSwipeLeftBackgroundColor(Color.RED)
                        .addSwipeLeftLabel("삭제")
                        .setSwipeLeftLabelColor(Color.WHITE)
                        .create()
                        .decorate();
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        }).attachToRecyclerView(alarmsRecyclerView);

        searchAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //조회전 화면 클리어
                alarmsRecyclerView.startLayoutAnimation();
            }
        });
        addAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(v).navigate(R.id.action_alarmsListFragment_to_createAlarmFragment);
                Log.d("msg","oncreate 실행4");
            }
        });

        alarmRecyclerViewAdapter.setOnItemClickListener(new AlarmRecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View v, int position) {
                //Navigation.findNavController(v).navigate(R.id.action_nav_alarm_to_updateAlarmFragment);
                Log.d("ItemClick","item클릭시");
            }
        });
        return view;
    }

    @Override
    public void onToggle(Alarm alarm) {
        if (alarm.isStarted()) {
            alarm.cancelAlarm(getContext());
            alarmsListViewModel.update(alarm);
        } else {
            alarm.schedule(getContext());
            alarmsListViewModel.update(alarm);
        }
    }
}