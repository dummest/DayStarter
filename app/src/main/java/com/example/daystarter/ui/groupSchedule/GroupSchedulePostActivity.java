package com.example.daystarter.ui.groupSchedule;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.androidnetworking.model.Progress;
import com.bumptech.glide.Glide;
import com.example.daystarter.R;
import com.example.daystarter.databinding.ActivityGroupSchedulePostBinding;
import com.example.daystarter.ui.groupSchedule.myClass.Comment;
import com.example.daystarter.ui.groupSchedule.myClass.GroupScheduleModel;
import com.example.daystarter.ui.groupSchedule.myClass.Member;
import com.example.daystarter.ui.weather.ProgressDialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class GroupSchedulePostActivity extends AppCompatActivity {
    ActivityGroupSchedulePostBinding binding;
    String scheduleKey;
    String groupId;
    CommentsRecyclerViewAdapter adapter;
    InputMethodManager imm;
    ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGroupSchedulePostBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        progressDialog = new ProgressDialog(this);
        init();

        binding.commentsRecyclerView.setLayoutManager(new LinearLayoutManager(getLayoutInflater().getContext()));
        binding.commentsRecyclerView.setAdapter(adapter);
    }
    void init(){
        progressDialog.show();
        scheduleKey = getIntent().getStringExtra("key");
        groupId = getIntent().getStringExtra("groupId");
        imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if((scheduleKey == null || scheduleKey.isEmpty()) || (groupId == null || groupId.isEmpty()))
            finish();
        else
            adapter = new CommentsRecyclerViewAdapter();
            loadPost();

        binding.commentPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               writeComment();
           }
        });

        binding.deleteImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                makeScheduleDeleteDialog();
            }
        });
    }

    void loadPost(){
        DatabaseReference postRef = FirebaseDatabase.getInstance().getReference().child("schedules").child(groupId).child(scheduleKey);
        postRef.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if(task.isSuccessful()){
                    GroupScheduleModel post = task.getResult().getValue(GroupScheduleModel.class);

                    binding.titleTextView.setText(post.title);
                    binding.writingTimeTextView.setText(changeLongToSdf(post.writingTime));
                    binding.startTimeTextView.setText(changeLongToSdf(post.startTime));
                    binding.endTimeTextView.setText(changeLongToSdf(post.endTime));
                    binding.contentsTextView.setText(post.contents);
                    loadProfileImage(post.writerUid);
                    loadName(post.writerUid);
                    progressDialog.dismiss();
                }
                else{
                    showToast("Data load fail");
                }
            }
        });
    }
    void loadProfileImage(String uid){
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference()
                .child("users").child(uid).child("profileImgPath");
        dbRef.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if(task.isSuccessful()){
                    String path = task.getResult().getValue(String.class);
                    Glide.with(getBaseContext()).load(path).circleCrop().error(R.drawable.ic_baseline_person_24)
                            .into(binding.profileImageView);
                }
            }
        });
    }

    void loadName(String uid){
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference().child("groups").child(groupId).child("members").child(uid);
        dbRef.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if(task.getResult().exists()){
                    binding.writerTextView.setText(task.getResult().getValue(Member.class).name);
                }
                else {
                    binding.writerTextView.setText("????????? ??????");
                    binding.writerTextView.setTextColor(ContextCompat.getColor(getBaseContext(), R.color.gray));
                }
            }
        });
    }

    private void showToast(String str){
        Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
    }

    String changeLongToSdf(long time){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm");
        return sdf.format(time);
    }

    void writeComment() {
        String str = binding.commentEditText.getText().toString().trim();
        if (str.length() < 3) {
            showToast("????????? ?????? 2??? ?????? ????????? ?????????");
            return;
        }
        Comment comment = new Comment(FirebaseAuth.getInstance().getUid(), str, Calendar.getInstance().getTimeInMillis());
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference().child("schedules").child(groupId).child(scheduleKey).child("comments");
        dbRef.push().setValue(comment).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                adapter.loadComments();
                imm.hideSoftInputFromWindow(binding.commentEditText.getWindowToken(), 0);
                binding.commentEditText.setText("");
            }
        });
    }

    void makeScheduleDeleteDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(GroupSchedulePostActivity.this)
                .setTitle("??????")
                .setMessage("???????????? ??????????????????????")
                .setPositiveButton("???", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference()
                                .child("groups").child(groupId).child("members").child(FirebaseAuth.getInstance().getUid());
                        dbRef.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DataSnapshot> task) {
                                if(task.isSuccessful()) {
                                    Member member = task.getResult().getValue(Member.class);
                                    if(member.status.equals("host") || member.uid.equals(FirebaseAuth.getInstance().getUid())){
                                        deleteSchedule();
                                    }
                                    else{
                                        showToast("????????? ??? ?????? ????????? ????????????");
                                    }
                                }
                                else{
                                    showToast("??????! ?????? ??? ?????? ??????????????????");
                                }
                            }
                        });
                    }
                })
                .setNegativeButton("?????????", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
        builder.create().show();
    }

    void deleteSchedule(){
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference().child("schedules").child(groupId).child(scheduleKey);
        dbRef.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    showToast("?????????????????????");
                }
                else{
                    showToast("????????? ??????????????????");
                }
                finish();
            }
        });
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public class CommentsRecyclerViewAdapter extends RecyclerView.Adapter<CommentsRecyclerViewAdapter.CommentsViewHolder>{
        ArrayList<Comment> commentArrayList;
        ArrayList<String> keyArrayList;
        public CommentsRecyclerViewAdapter(){
            commentArrayList = new ArrayList<>();
            keyArrayList = new ArrayList<>();
            loadComments();
        }

        void loadComments(){
            keyArrayList.clear();
            commentArrayList.clear();
            Log.d(TAG, "loadComments: start");
            DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference().child("schedules").child(groupId).child(scheduleKey).child("comments");
            dbRef.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    if(task.isSuccessful()){
                        for (DataSnapshot ds: task.getResult().getChildren()) {
                            Comment comment = ds.getValue(Comment.class);
                            commentArrayList.add(comment);
                            keyArrayList.add(ds.getKey());
                        }
                        binding.commentsCountTextView.setText("?????? " + commentArrayList.size() + "???");
                    }
                    notifyDataSetChanged();
                    Log.d(TAG, "onComplete: done");
                }
            });
        }

        @NonNull
        @Override
        public CommentsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            Log.d(TAG, "onCreateViewHolder");
            View view = getLayoutInflater().inflate(R.layout.item_comment, parent, false);
            return new CommentsViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull CommentsViewHolder holder, int position) {
            Comment comment = commentArrayList.get(position);
            holder.key = keyArrayList.get(holder.getAdapterPosition());
            DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference().child("groups")
                    .child(groupId).child("members").child(comment.writerUid);
            dbRef.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    if(task.isSuccessful()) {
                        if(task.getResult().exists()) {
                            Member member = task.getResult().getValue(Member.class);
                            holder.nameTextView.setText(member.name);
                        }
                        else{
                            holder.nameTextView.setText("????????? ??????");
                            holder.nameTextView.setTextColor(ContextCompat.getColor(getBaseContext(), R.color.gray));
                        }
                    }
                }
            });
            holder.contentsTextView.setText(comment.contents);
            holder.writingTimeTextView.setText(changeLongToSdf(comment.writingTime));

            if(comment.writerUid.equals(FirebaseAuth.getInstance().getUid())) {
                holder.deleteImageView.setVisibility(View.VISIBLE);
                holder.deleteImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        makeCommentRemoveDialog(holder.key);
                    }
                });
            }
        }

        @Override
        public int getItemCount() {
            return commentArrayList.size();
        }

        public class CommentsViewHolder extends RecyclerView.ViewHolder{
            public TextView nameTextView, contentsTextView, writingTimeTextView;
            public ImageView deleteImageView;
            public String key;
            public CommentsViewHolder(@NonNull View itemView) {
                super(itemView);
                nameTextView = itemView.findViewById(R.id.name_text_view);
                contentsTextView = itemView.findViewById(R.id.contents_text_view);
                writingTimeTextView = itemView.findViewById(R.id.writing_time_text_view);
                deleteImageView = itemView.findViewById(R.id.delete_image_view);
            }
        }

        void makeCommentRemoveDialog(String key){
            Log.d(TAG, "key: " + key);
            AlertDialog.Builder builder = new AlertDialog.Builder(GroupSchedulePostActivity.this)
                    .setTitle("??????")
                    .setMessage("????????? ??????????????????????")
                    .setPositiveButton("???", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference().child("schedules").child(groupId).child(scheduleKey).child("comments")
                                    .child(key);
                            dbRef.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        showToast("?????????????????????");
                                        loadComments();
                                    }
                                    else{
                                        showToast("??????");
                                    }
                                }
                            });
                        }
                    })
                    .setNegativeButton("?????????", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    });
            builder.create().show();
        }
    }
}