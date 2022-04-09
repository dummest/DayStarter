package com.example.daystarter.ui.groupSchedule;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import com.example.daystarter.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;


public class GroupScheduleFragment extends Fragment {
    HostFragment hostFragment;
    ParticipantsFragment participantsFragment;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v;
        v = inflater.inflate(R.layout.fragment_group_schedule, container, false);

        hostFragment = new HostFragment();
        participantsFragment = new ParticipantsFragment();

        TabLayout tabLayout = v.findViewById(R.id.tabs);
        tabLayout.addTab(tabLayout.newTab().setText("관리 중인 그룹"));
        tabLayout.addTab(tabLayout.newTab().setText("참여 중인 그룹"));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();

                Fragment selected = null;
                if(position==0){
                    selected = hostFragment;
                }
                else{
                    selected = participantsFragment;
                }
                getChildFragmentManager().beginTransaction().replace(R.id.container, selected).commit();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        FloatingActionButton fab = v.findViewById(R.id.group_schedule_fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(tabLayout.getSelectedTabPosition() == 0){
                    Snackbar.make(view, "hostTab", Snackbar.LENGTH_SHORT).show();
                }
                else{
                    Snackbar.make(view, "participantsTab", Snackbar.LENGTH_SHORT).show();
                }
            }
        });

        getChildFragmentManager().beginTransaction().replace(R.id.container, hostFragment).commit();

        return v;
    }
}