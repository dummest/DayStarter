package com.example.daystarter.ui.groupSchedule;

import static android.content.ContentValues.TAG;

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
import com.example.daystarter.ui.groupSchedule.myClass.Group;
import com.example.daystarter.ui.groupSchedule.myClass.GroupId;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseError;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.CountDownLatch;


public class ParticipantsFragment extends Fragment {
    View view;
    RecyclerView recyclerView;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_participants, container, false);
        recyclerView = view.findViewById(R.id.participants_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(inflater.getContext()));
        ParticipantsRecyclerViewAdapter adapter = new ParticipantsRecyclerViewAdapter();
        recyclerView.setAdapter(adapter);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();


    }

    public class ParticipantsRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        ArrayList<Group> groupList;
        ArrayList<GroupId> groupIdList;
        FirebaseUser user;

        public ParticipantsRecyclerViewAdapter(){
            groupList = new ArrayList<Group>();
            groupIdList = new ArrayList<GroupId>();
            user = FirebaseAuth.getInstance().getCurrentUser();
            DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();


            readId(dbRef.child("users").child(user.getUid()).child("participatingGroups"), new IdCallback() {
                @Override
                public void onCallback(GroupId groupId) {
                    groupIdList.add(groupId);
                    Log.d(TAG, "groupId: " + groupId.groupId);

                    readGroup(dbRef.child("groups").child(groupId.groupId), new GroupCallback() {
                        @Override
                        public void onCallback(Group group) {
                            groupList.add(group);
                            Log.d(TAG, "groupName: " + group.groupName);
                            notifyDataSetChanged();
                        }
                    });
                }
            });
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.group_item, parent, false);

            return new GroupViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

            Glide.with(holder.itemView.getContext()).load(groupList.get(position).imagePath).circleCrop()
                    .into(((GroupViewHolder)holder).imageView);

            ((GroupViewHolder)holder).textView.setText(groupList.get(position).groupName);
        }

        @Override
        public int getItemCount() {
            return groupList.size();
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

    public interface IdCallback {
        void onCallback(GroupId groupId);
    }

    public void readId(DatabaseReference dbRef, IdCallback idCallback) {
        dbRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                GroupId groupId = snapshot.getValue(GroupId.class);
                Log.d(TAG, "readId: " +  groupId.groupId);
                idCallback.onCallback(groupId);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                GroupId groupId = snapshot.getValue(GroupId.class);
                Log.d(TAG, "readId: " +  groupId.groupId);
                idCallback.onCallback(groupId);
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public interface GroupCallback {
        void onCallback(Group group);
    }

    public void readGroup(DatabaseReference dbRef, GroupCallback groupCallback) {
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Group group = dataSnapshot.getValue(Group.class);
                Log.d(TAG, "readGroup: " + group.groupName);
                groupCallback.onCallback(group);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

}