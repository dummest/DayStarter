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
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.daystarter.R;
import com.example.daystarter.databinding.ActivityApplicantsBinding;
import com.example.daystarter.ui.groupSchedule.myClass.GroupInfo;
import com.example.daystarter.ui.groupSchedule.myClass.Member;
import com.example.daystarter.ui.groupSchedule.myClass.User;
import com.example.daystarter.ui.weather.ProgressDialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class ApplicantsActivity extends AppCompatActivity {
    ActivityApplicantsBinding binding;
    String groupId;
    ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityApplicantsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        groupId = getIntent().getStringExtra("groupId");
        progressDialog = new ProgressDialog(this);

        ApplicantsListRecyclerViewAdapter adapter = new ApplicantsListRecyclerViewAdapter();
        binding.applicantsRecyclerView.setLayoutManager(new LinearLayoutManager(getLayoutInflater().getContext()));
        binding.applicantsRecyclerView.setAdapter(adapter);

        setAutoApproveSwitch();
    }
    void setAutoApproveSwitch(){
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference().child("groups").child(groupId)
                .child("autoApprove");

        dbRef.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if(task.isSuccessful()){
                    binding.autoApproveSwitch.setChecked(task.getResult().getValue(boolean.class));
                }
            }
        });

        binding.autoApproveSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                dbRef.setValue(b);
            }
        });
    }

    public class ApplicantsListRecyclerViewAdapter extends RecyclerView.Adapter<ApplicantsViewHolder>{
        ArrayList<Member> applicantsList;
        ApplicantsListRecyclerViewAdapter(){
            applicantsList = new ArrayList<>();
            loadApplicantsList();
        }

        @NonNull
        @Override
        public ApplicantsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_applicant, parent, false);
            return new ApplicantsViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ApplicantsViewHolder holder, int position) {
            holder.nameTextView.setText(applicantsList.get(position).name);
            holder.emailTextView.setText(applicantsList.get(position).email);

            DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference().child("users")
                    .child(applicantsList.get(position).uid).child("profileImgPath");
            dbRef.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    if(task.isSuccessful()){
                        String imageUrl = task.getResult().getValue(String.class);
                        Glide.with(holder.itemView.getContext()).load(imageUrl).circleCrop()
                                .error(R.drawable.ic_baseline_person_24).into(holder.profileImageView);
                    }
                    else{
                        holder.profileImageView.setImageResource(R.drawable.ic_baseline_person_24);
                    }
                }
            });

            holder.approveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    progressDialog.show();
                    Member member = applicantsList.get(holder.getAdapterPosition());
                    DatabaseReference memberRef = FirebaseDatabase.getInstance().getReference().child("groups")
                            .child(groupId).child("members").child(member.uid);

                    DatabaseReference statusRef = FirebaseDatabase.getInstance().getReference().child("groups")
                            .child(groupId).child("initialStatus");

                    statusRef.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DataSnapshot> task) {
                            if(task.isSuccessful()){
                                String status = task.getResult().getValue(String.class);
                                Log.d(TAG, "initialStatus " + status);
                                member.status = status;

                                while(memberRef.setValue(member).isComplete()){}
                                progressDialog.dismiss();
                                approveApplicant(member.uid);
                            }
                        }
                    });
                }
            });

            holder.denialButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    denyApplicant(applicantsList.get(holder.getAdapterPosition()).uid);
                }
            });

            if(position == this.getItemCount()-1){
                progressDialog.dismiss();
            }
        }
        void approveApplicant(String uid){
            progressDialog.show();
            DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference().child("applicants")
                    .child(groupId).child(uid);
            while (dbRef.removeValue().isComplete()){}

            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users").child(uid);
            while(userRef.child("participatingGroups").child(groupId).setValue(new GroupInfo(groupId)).isComplete()){}
            progressDialog.dismiss();
            loadApplicantsList();
        }

        void denyApplicant(String uid){
            progressDialog.show();
            DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference().child("applicants")
                    .child(groupId).child(uid);
            while (dbRef.removeValue().isComplete()){}
            progressDialog.dismiss();
            loadApplicantsList();
        }

        @Override
        public int getItemCount() {
            return applicantsList.size();
        }

        void loadApplicantsList(){
            applicantsList.clear();
            progressDialog.show();
            DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference().child("applicants").child(groupId);
            dbRef.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    if(task.isSuccessful()) {
                        for (DataSnapshot ds : task.getResult().getChildren()) {
                            applicantsList.add(ds.getValue(Member.class));
                        }
                        progressDialog.dismiss();
                        notifyDataSetChanged();
                    }
                }
            });
        }

    }
    private class ApplicantsViewHolder extends RecyclerView.ViewHolder{
        public CircleImageView profileImageView;
        public TextView nameTextView;
        public TextView emailTextView;
        public MaterialButton approveButton, denialButton;
        public ApplicantsViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImageView = itemView.findViewById(R.id.profile_image_view);
            nameTextView = itemView.findViewById(R.id.name_text_view);
            emailTextView = itemView.findViewById(R.id.email_text_view);
            approveButton = itemView.findViewById(R.id.approve_button);
            denialButton = itemView.findViewById(R.id.denial_button);
        }
    }
}