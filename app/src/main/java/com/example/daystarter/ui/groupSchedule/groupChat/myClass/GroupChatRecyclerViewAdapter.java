package com.example.daystarter.ui.groupSchedule.groupChat.myClass;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.daystarter.R;
import com.example.daystarter.ui.groupSchedule.GroupActivity;
import com.example.daystarter.ui.groupSchedule.myClass.Member;
import com.example.daystarter.ui.groupSchedule.myClass.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class GroupChatRecyclerViewAdapter extends RecyclerView.Adapter<GroupChatRecyclerViewAdapter.ChatModelViewHolder> {
    SimpleDateFormat timeFormat = new SimpleDateFormat("MM/dd HH:mm", Locale.getDefault());
    ArrayList<User> userArrayList = new ArrayList<>();
    ArrayList<Member> memberArrayList = new ArrayList<>();
    ArrayList<ChatModel> chatArrayList = new ArrayList<>();
    RecyclerView chatRecyclerView;
    Context context;
    String groupId;

    public GroupChatRecyclerViewAdapter(Context context, String groupId, RecyclerView recyclerView){
        this.groupId = groupId;
        this.context = context;
        chatRecyclerView = recyclerView;
        loadMemberList();
    }

    @NonNull
    @Override
    public ChatModelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder: ");
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat, parent, false);
        return new ChatModelViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatModelViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder: ");
        int pos = holder.getAdapterPosition();
        String uid = chatArrayList.get(pos).writerUid;

        User user = null;
        Member member = null;
        
        for(int i =0; i < memberArrayList.size(); i++){
            Member m = memberArrayList.get(i);
            if(m.uid.equals(uid)){
                member = m;
                user = userArrayList.get(i);
            }
        }

        if(member.uid.equals(FirebaseAuth.getInstance().getUid())){
            holder.chatBodyLayout.setGravity(Gravity.END);
            holder.materialCardView.setCardBackgroundColor(context.getResources().getColor(R.color.teal_500, context.getTheme()));
            holder.textView.setTextColor(context.getResources().getColor(R.color.white, context.getTheme()));
        }
        else{
            holder.chatBodyLayout.setGravity(Gravity.START);
            holder.nameTextView.setText(member.name);
            Glide.with(holder.itemView.getContext()).load(user.profileImgPath)
                    .circleCrop().error(R.drawable.ic_baseline_person_24).into(holder.profileImageView);
        }
        holder.textView.setText(chatArrayList.get(pos).text);
        holder.timeTextView.setText(timeFormat.format(chatArrayList.get(pos).writingTime));
    }

    @Override
    public int getItemCount() {
        return chatArrayList.size();
    }

    void loadMemberList(){
        Log.d(TAG, "loadMemberList: ");
        DatabaseReference memRef = FirebaseDatabase.getInstance().getReference().child("groups").child(groupId)
                .child("members");
        memRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                memberArrayList.clear();
                for (DataSnapshot ds: snapshot.getChildren()) {
                    memberArrayList.add(ds.getValue(Member.class));
                }
                loadUserList();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

   void loadUserList(){
       Log.d(TAG, "loadUserList: ");
       userArrayList.clear();
       for(Member member : memberArrayList) {
           DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference().child("users")
                   .child(member.uid);
           dbRef.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
               @Override
               public void onComplete(@NonNull Task<DataSnapshot> task) {
                   userArrayList.add(task.getResult().getValue(User.class));
                   if(userArrayList.size() == memberArrayList.size())
                       getMessageList();
               }
           });
       }
   }

    void getMessageList(){
        Log.d(TAG, "getMessageList: ");
        DatabaseReference messageRef = FirebaseDatabase.getInstance().getReference().child("messages")
                .child(groupId);
        messageRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                chatArrayList.add(snapshot.getValue(ChatModel.class));
                notifyItemInserted(chatArrayList.size());
                Log.d(TAG, "onChildAdded: ");
                chatRecyclerView.scrollToPosition(chatArrayList.size()-1);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    public class ChatModelViewHolder extends RecyclerView.ViewHolder{
        public CircleImageView profileImageView;
        public MaterialCardView materialCardView;
        public TextView textView, nameTextView, timeTextView;
        public LinearLayout chatBodyLayout;
        public ChatModelViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImageView = itemView.findViewById(R.id.profile_image_view);
            materialCardView = itemView.findViewById(R.id.chat_card_view);
            textView = itemView.findViewById(R.id.chat_text_view);
            nameTextView = itemView.findViewById(R.id.name_text_view);
            timeTextView = itemView.findViewById(R.id.time_text_view);
            chatBodyLayout = itemView.findViewById(R.id.chat_body_layout);
        }
    }
}
