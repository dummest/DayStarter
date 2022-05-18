package com.example.daystarter.ui.groupSchedule;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.daystarter.R;
import com.example.daystarter.databinding.ActivityGroupSchedulePostBinding;
import com.example.daystarter.ui.groupSchedule.myClass.Comment;
import com.example.daystarter.ui.groupSchedule.myClass.GroupScheduleModel;
import com.example.daystarter.ui.groupSchedule.myClass.Member;
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
    String key;
    String groupId;
    CommentsRecyclerViewAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGroupSchedulePostBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        init();

        binding.commentsRecyclerView.setLayoutManager(new LinearLayoutManager(getLayoutInflater().getContext()));
        binding.commentsRecyclerView.setAdapter(adapter);


    }
    void init(){
       key = getIntent().getStringExtra("key");
       groupId = getIntent().getStringExtra("groupId");
       if((key == null || key.isEmpty()) || (groupId == null || groupId.isEmpty()))
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
    }

    void loadPost(){
        DatabaseReference postRef = FirebaseDatabase.getInstance().getReference().child("schedules").child(groupId).child(key);
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

                    loadName(post.writerUid);
                }
                else{
                    showToast("Data load fail");
                }
            }
        });
    }
    void loadName(String uid){
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference().child("groups").child(groupId).child("members").child(uid);
        dbRef.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                String name = task.getResult().getValue(Member.class).name;
                if(name == null || name.isEmpty()){
                    name = "탈퇴한 멤버입니다.";
                }
                binding.writerTextView.setText(name);
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

    void loadComment(){
        adapter.notifyDataSetChanged();
    }

    void writeComment(){
        String str = binding.commentEditText.getText().toString().trim();
        if(str.length() < 3){
            showToast("댓글은 최소 2자 이상 적어야 합니다");
            return;
        }
        Comment comment = new Comment(FirebaseAuth.getInstance().getUid(), str, Calendar.getInstance().getTimeInMillis());
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference().child("schedules").child(groupId).child(key).child("comments");
        dbRef.push().setValue(comment).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                loadComment();
            }
        });
    }

    public class CommentsRecyclerViewAdapter extends RecyclerView.Adapter<CommentsRecyclerViewAdapter.CommentsViewHolder>{
        ArrayList<Comment> commentArrayList;

        public CommentsRecyclerViewAdapter(){
            commentArrayList = new ArrayList<>();

            DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference().child("schedules").child(groupId).child(key).child("comments");
            dbRef.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    if(task.isSuccessful()){
                        for (DataSnapshot ds: task.getResult().getChildren()) {
                            commentArrayList.add(ds.getValue(Comment.class));
                        }
                        notifyDataSetChanged();
                    }
                }
            });
        }
        @NonNull
        @Override
        public CommentsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.item_comment, parent, false);
            return new CommentsViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull CommentsViewHolder holder, int position) {
            //holder.nameTextView.setText(commentArrayList.get(position).writerName);
            holder.contentsTextView.setText(commentArrayList.get(position).contents);
            holder.writingTimeTextView.setText(changeLongToSdf(commentArrayList.get(position).writingTime));
        }

        @Override
        public int getItemCount() {
            return commentArrayList.size();
        }

        public class CommentsViewHolder extends RecyclerView.ViewHolder{
            public TextView nameTextView, contentsTextView, writingTimeTextView;
            public CommentsViewHolder(@NonNull View itemView) {
                super(itemView);
                //nameTextView = findViewById(R.id.name_text_view);
                contentsTextView = findViewById(R.id.contents_text_View);
                writingTimeTextView = findViewById(R.id.writing_time_text_view);
            }
        }
    }
}