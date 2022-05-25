package com.example.daystarter.ui.groupSchedule;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.daystarter.R;
import com.example.daystarter.databinding.ActivityMemberListBinding;
import com.example.daystarter.ui.groupSchedule.myClass.User;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class MemberListActivity extends AppCompatActivity {
    ActivityMemberListBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMemberListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());



    }
    public class MemberListRecyclerViewAdapter extends RecyclerView.Adapter{
        ArrayList<User> userArrayList;

        MemberListRecyclerViewAdapter(){
            userArrayList = new ArrayList<>();
        }
        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.group_item, parent, false);
            return new MemberViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        }

        @Override
        public int getItemCount() {
            return userArrayList.size();
        }
    }
    private class MemberViewHolder extends RecyclerView.ViewHolder{
        public CircleImageView imageView;
        public TextView textView;
        public MemberViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.profile_image_view);
            textView = itemView.findViewById(R.id.name_text_view);
        }
    }

}