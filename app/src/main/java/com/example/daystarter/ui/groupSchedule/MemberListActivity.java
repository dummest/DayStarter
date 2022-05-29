package com.example.daystarter.ui.groupSchedule;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.daystarter.R;
import com.example.daystarter.databinding.ActivityMemberListBinding;
import com.example.daystarter.ui.groupSchedule.myClass.GroupInfo;
import com.example.daystarter.ui.groupSchedule.myClass.Member;
import com.example.daystarter.ui.groupSchedule.myClass.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class MemberListActivity extends AppCompatActivity {
    ActivityMemberListBinding binding;
    String groupId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMemberListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        groupId = getIntent().getStringExtra("groupId");

        MemberListRecyclerViewAdapter adapter = new MemberListRecyclerViewAdapter();
        binding.memberRecyclerView.setLayoutManager(new LinearLayoutManager(getLayoutInflater().getContext()));
        binding.memberRecyclerView.setAdapter(adapter);

    }
    public class MemberListRecyclerViewAdapter extends RecyclerView.Adapter<MemberViewHolder>{
        ArrayList<User> userArrayList;
        ArrayList<Member> memberArrayList;

        MemberListRecyclerViewAdapter(){
            userArrayList = new ArrayList<>();
            memberArrayList = new ArrayList<>();
            loadMemberList();
        }
        @NonNull
        @Override
        public MemberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_member, parent, false);
            return new MemberViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull MemberViewHolder holder, int position) {
            DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference()
                    .child("groups").child(groupId).child("members").child(memberArrayList.get(position).uid).child("status");
            switch (memberArrayList.get(position).status){
                case "host":
                        holder.statusSwitch.setVisibility(View.GONE);
                case "write":
                    holder.statusSwitch.setText("읽기");
                    holder.statusSwitch.setChecked(false);
                case "read":
                    holder.statusSwitch.setText("쓰기");
                    holder.statusSwitch.setChecked(true);
                    if(!memberArrayList.get(position).status.equals("host")) {
                        holder.statusSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                            @Override
                            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                                if (compoundButton.isChecked())
                                    dbRef.setValue("write");
                                else
                                    dbRef.setValue("read");
                            }
                        });
                    }
                    break;
            }
            dbRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String status = snapshot.getValue(String.class);
                    if(status.equals("write")){
                        holder.statusSwitch.setChecked(true);
                        holder.statusSwitch.setText("쓰기");
                    }
                    else if(status.equals("read")){
                        holder.statusSwitch.setChecked(false);
                        holder.statusSwitch.setText("읽기");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
            Glide.with(holder.itemView.getContext()).load(userArrayList.get(position).profileImgPath)
                    .circleCrop().error(R.drawable.ic_baseline_person_24).into(holder.profileImageView);
            holder.nameTextView.setText(memberArrayList.get(position).name);
        }

        @Override
        public int getItemCount() {
            return memberArrayList.size();
        }

        public void loadMemberList(){
            DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference().child("groups").child(groupId).child("members");
            dbRef.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    memberArrayList.clear();
                    userArrayList.clear();
                    int i = 0;
                    for(DataSnapshot ds : task.getResult().getChildren()){
                        Member member = ds.getValue(Member.class);
                        memberArrayList.add(member);
                        loadUserList(i);
                        i++;
                    }
                    Log.d(TAG, "member length: " + memberArrayList.size());
                }
            });
        }

        public void loadUserList(int index){
            DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference()
                    .child("users").child(memberArrayList.get(index).uid);
            dbRef.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    if(task.isSuccessful()){
                        User user = task.getResult().getValue(User.class);
                        userArrayList.add(user);
                        Log.d(TAG, "user length: " + userArrayList.size());
                        if(index == memberArrayList.size()-1){
                            notifyDataSetChanged();
                        }
                    }
                }
            });
        }
    }
    private class MemberViewHolder extends RecyclerView.ViewHolder{
        public CircleImageView profileImageView;
        public TextView nameTextView;
        public SwitchMaterial statusSwitch;
        public MemberViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImageView = itemView.findViewById(R.id.profile_image_view);
            nameTextView = itemView.findViewById(R.id.name_text_view);
            statusSwitch = itemView.findViewById(R.id.status_switch);
        }
    }
}