package com.example.daystarter.ui.groupSchedule;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.daystarter.R;
import com.example.daystarter.ui.groupSchedule.myClass.Group;
import com.example.daystarter.ui.groupSchedule.myClass.GroupInfo;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class HostFragment extends Fragment {
    View view;
    RecyclerView recyclerView;
    HostFragment.HostRecyclerViewAdapter adapter = new HostFragment.HostRecyclerViewAdapter();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_host, container, false);
        recyclerView = view.findViewById(R.id.host_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(inflater.getContext()));
        recyclerView.setAdapter(adapter);
        return view;
    }

    public class HostRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        ArrayList<Group> groupList;
        ArrayList<GroupInfo> groupInfoList;

        public HostRecyclerViewAdapter(){
            groupList = new ArrayList<Group>();
            groupInfoList = new ArrayList<GroupInfo>();
            loadGroupIdList();
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.group_item, parent, false);
            return new HostFragment.GroupViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            Glide.with(holder.itemView.getContext()).load(groupList.get(position).imagePath).circleCrop()
                    .into(((HostFragment.GroupViewHolder)holder).imageView);
            ((HostFragment.GroupViewHolder)holder).textView.setText(groupList.get(position).groupName);

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(view.getContext(), GroupActivity.class);
                    intent.putExtra("groupId", groupList.get(holder.getAdapterPosition()).groupId);
                    startActivity(intent);
                }
            });
        }

        @Override
        public int getItemCount() {
            return groupList.size();
        }

        public void loadGroupIdList() {
            DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference().child("users").child(FirebaseAuth.getInstance().getUid()).child("hostingGroups");
            dbRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    groupInfoList.clear();
                    groupList.clear();
                    for(DataSnapshot ds : snapshot.getChildren()){
                        GroupInfo groupInfo = ds.getValue(GroupInfo.class);
                        groupInfoList.add(groupInfo);
                        Log.d(TAG, "groupId add: " + groupInfo.groupId);
                        notifyDataSetChanged();
                    }
                    loadGroupList();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

        public void loadGroupList(){
            for(GroupInfo groupInfo : groupInfoList) {
                DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference().child("groups").child(groupInfo.groupId);
                dbRef.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                        if(task.isSuccessful()){
                            Group group = task.getResult().getValue(Group.class);
                            groupList.add(group);
                            Log.d(TAG, "group add: " + group.groupName);
                            notifyDataSetChanged();
                        }
                    }
                });
            }
        }
    }
    private class GroupViewHolder extends RecyclerView.ViewHolder{
        public ImageView imageView;
        public TextView textView;
        public GroupViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.group_image_view);
            textView = itemView.findViewById(R.id.group_name);
        }
    }
}