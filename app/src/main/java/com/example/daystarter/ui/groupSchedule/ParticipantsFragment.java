package com.example.daystarter.ui.groupSchedule;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import com.example.daystarter.ui.groupSchedule.cacheDBHelper.UnreadDBHelper;
import com.example.daystarter.ui.groupSchedule.myClass.Group;
import com.example.daystarter.ui.groupSchedule.myClass.GroupInfo;

import com.example.daystarter.ui.weather.ProgressDialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ParticipantsFragment extends Fragment {
    View view;
    RecyclerView recyclerView;
    ParticipantsRecyclerViewAdapter adapter;
    ProgressDialog progressDialog;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_participants, container, false);
        progressDialog = new ProgressDialog(getActivity());

        adapter = new ParticipantsRecyclerViewAdapter();
        recyclerView = view.findViewById(R.id.participants_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(inflater.getContext()));
        recyclerView.setAdapter(adapter);
        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        progressDialog = new ProgressDialog(getActivity());
    }

    @Override
    public void onResume() {
        super.onResume();
        adapter.notifyDataSetChanged();
    }

    public class ParticipantsRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        ArrayList<Group> groupList;
        ArrayList<GroupInfo> groupInfoList;

        public ParticipantsRecyclerViewAdapter(){
            groupList = new ArrayList<Group>();
            groupInfoList = new ArrayList<GroupInfo>();
            loadGroupIdList();
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.group_item, parent, false);
            return new GroupViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            int pos = holder.getAdapterPosition();
            Glide.with(holder.itemView.getContext()).load(groupList.get(position).imagePath).circleCrop().error(R.drawable.ic_outline_group_24)
                    .into(((GroupViewHolder)holder).imageView);
            ((GroupViewHolder)holder).textView.setText(groupList.get(position).groupName);
            DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference()
                    .child("groups").child(groupList.get(pos).groupId).child("members");
            dbRef.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    ((ParticipantsFragment.GroupViewHolder) holder).memberCountTextView.setText(task.getResult().getChildrenCount() + "명");
                }
            });

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(view.getContext(), GroupActivity.class);
                    intent.putExtra("groupId", groupList.get(holder.getAdapterPosition()).groupId);
                    startActivity(intent);
                }
            });

            Group group = groupList.get(pos);
            UnreadDBHelper unreadDBHelper = new UnreadDBHelper(getContext());
            if(!unreadDBHelper.searchGroup(group.groupId))
                unreadDBHelper.insertGroup(group.groupId);

            int count = unreadDBHelper.getUnreadChatCount(group.groupId) + unreadDBHelper.getUnreadScheduleCount(group.groupId);

            ((ParticipantsFragment.GroupViewHolder) holder).unreadCountTextView.setText(count + "");
        }

        @Override
        public int getItemCount() {
            return groupList.size();
        }

        public void loadGroupIdList() {
            progressDialog.show();
            DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference().child("users").child(FirebaseAuth.getInstance().getUid()).child("participatingGroups");
            dbRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    groupInfoList.clear();
                    groupList.clear();
                    for(DataSnapshot ds : snapshot.getChildren()){
                        GroupInfo groupInfo = ds.getValue(GroupInfo.class);
                        groupInfoList.add(groupInfo);
                    }
                    if(groupInfoList.size()>0)
                        loadGroupList();
                    else
                        progressDialog.dismiss();
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
                        if(progressDialog.isShowing())
                            progressDialog.dismiss();
                        if(task.isSuccessful()){
                            Group group = task.getResult().getValue(Group.class);
                            groupList.add(group);
                        }
                        notifyDataSetChanged();
                    }
                });
            }
        }
    }

    private class GroupViewHolder extends RecyclerView.ViewHolder{
        public ImageView imageView;
        public TextView textView, memberCountTextView, unreadCountTextView;
        public MaterialCardView cardView;
        public GroupViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.group_image_view);
            textView = itemView.findViewById(R.id.group_name);
            cardView = itemView.findViewById(R.id.count_card_view);
            memberCountTextView = itemView.findViewById(R.id.member_count_text_view);
            unreadCountTextView = itemView.findViewById(R.id.unread_count_text_view);
        }
    }
}