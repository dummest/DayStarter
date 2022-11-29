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

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.androidnetworking.model.Progress;
import com.bumptech.glide.Glide;
import com.example.daystarter.R;
import com.example.daystarter.databinding.ActivityGroupSchedulePostBinding;
import com.example.daystarter.model.NotificationModel;
import com.example.daystarter.ui.groupSchedule.myClass.Comment;
import com.example.daystarter.ui.groupSchedule.myClass.GroupScheduleModel;
import com.example.daystarter.ui.groupSchedule.myClass.Member;
import com.example.daystarter.ui.weather.ProgressDialog;
import com.example.daystarter.ui.weather.WeatherData;
import com.example.daystarter.ui.weather.WeatherDayAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class GroupSchedulePostActivity extends AppCompatActivity {
    ActivityGroupSchedulePostBinding binding;
    String scheduleKey;
    String groupId;
    CommentsRecyclerViewAdapter adapter;
    WeatherDayAdapter weatherDayAdapter;
    InputMethodManager imm;
    ProgressDialog progressDialog;
    GroupScheduleModel post;
    ArrayList<WeatherData> arrayWeatherData = new ArrayList<>();
    long finishCount;
    long currentCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGroupSchedulePostBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        progressDialog = new ProgressDialog(this);
        init();

        binding.commentsRecyclerView.setLayoutManager(new LinearLayoutManager(getLayoutInflater().getContext()));
        binding.commentsRecyclerView.setAdapter(adapter);

        //오류시 adapter 땡겨오기
        weatherDayAdapter = new WeatherDayAdapter(arrayWeatherData, getApplicationContext());
        binding.weatherDayRecyclerview.setLayoutManager(new LinearLayoutManager(getApplicationContext(), RecyclerView.HORIZONTAL,false));
        binding.weatherDayRecyclerview.setAdapter(weatherDayAdapter);
        binding.weatherDayRecyclerview.setVisibility(View.GONE);
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
        binding.showWeatherImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.weatherDayRecyclerview.setVisibility(View.VISIBLE);
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
                    binding.addressTextView.setText(post.address);
                    double latitude = post.latitude;
                    double longitude = post.longitude;
                    loadProfileImage(post.writerUid);
                    loadName(post.writerUid);
                    GroupSchedulePostActivity.this.post = post;
                    progressDialog.dismiss();
                    DayWeather(latitude,longitude);
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
                    binding.writerTextView.setText("탈퇴한 멤버");
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
            showToast("댓글은 최소 2자 이상 적어야 합니다");
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
                .setTitle("확인")
                .setMessage("스케줄을 지우시겠습니까?")
                .setPositiveButton("예", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference()
                                .child("groups").child(groupId).child("members").child(FirebaseAuth.getInstance().getUid());
                        dbRef.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DataSnapshot> task) {
                                if(task.isSuccessful()) {
                                    Member member = task.getResult().getValue(Member.class);
                                    if(member.status.equals("host") || member.uid.equals(post.writerUid)){
                                        Log.d(TAG, "status " + member.status);
                                        deleteSchedule();
                                    }
                                    else{
                                        showToast("삭제할 수 있는 권한이 없습니다");
                                    }
                                }
                                else{
                                    showToast("오류! 잠시 후 다시 시도해주세요");
                                }
                            }
                        });
                    }
                })
                .setNegativeButton("아니요", new DialogInterface.OnClickListener() {
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
                    showToast("삭제되었습니다");
                    sendGcm();
                }
                else{
                    showToast("삭제에 실패했습니다");
                }
            }
        });
    }

    void sendGcm(){
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
        dbRef.child("groups").child(groupId).child("members").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if(task.isSuccessful()) {
                    finishCount = task.getResult().getChildrenCount();
                    for (DataSnapshot ds : task.getResult().getChildren()) {
                        Member member = ds.getValue(Member.class);
                        String uid = member.uid;

                        //자기자신에게 노티를 보내지는 않도록
                        if (member.alarmSet && !uid.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                            DatabaseReference tokenRef = FirebaseDatabase.getInstance().getReference();
                            tokenRef.child("users").child(uid).child("firebaseMessagingToken").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DataSnapshot> task) {
                                    if (task.isSuccessful()) {
                                        Log.d(TAG, "onComplete: " + task.getResult().getValue(String.class));
                                        if(task.getResult().exists()) {
                                            sendNotification(task.getResult().getValue(String.class));
                                        }
                                    }
                                }
                            });
                        }
                        else{
                            checkCount();
                        }
                    }
                }
                else{
                    Log.d(TAG, "onComplete: error");
                }
            }
        });



    }

    void sendNotification(String token){
        Gson gson = new Gson();
        NotificationModel notificationModel = new NotificationModel();
        notificationModel.data.groupId = groupId;
        notificationModel.data.title = binding.startTimeTextView.getText().toString() + "의 일정이 삭제되었습니다";
        notificationModel.data.body = "'" + binding.titleTextView.getText().toString() + "'";
        notificationModel.data.type = "group_schedule";
        notificationModel.to = token;
        RequestBody requestBody = RequestBody.create(gson.toJson(notificationModel), MediaType.parse("application/json; charset=utf8"));

        Request request =  new Request.Builder()
                .header("Content-Type", "application/json")
                .addHeader("Authorization", "key=" + getString(R.string.server_key))
                .url("https://fcm.googleapis.com/fcm/send")
                .post(requestBody)
                .build();

        OkHttpClient okHttpClient = new OkHttpClient();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.d(TAG, "onFailure: " + e.getMessage());
                checkCount();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                Log.d(TAG, "onResponse: " + response.toString());
                checkCount();
            }
        });
    }
    void checkCount(){
        currentCount++;
        if(currentCount == finishCount)
            finish();
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
                        binding.commentsCountTextView.setText("댓글 " + commentArrayList.size() + "개");
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
                            holder.nameTextView.setText("탈퇴한 멤버");
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
                    .setTitle("확인")
                    .setMessage("댓글을 지우시겠습니까?")
                    .setPositiveButton("예", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference().child("schedules").child(groupId).child(scheduleKey).child("comments")
                                    .child(key);
                            dbRef.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        showToast("삭제되었습니다");
                                        loadComments();
                                    }
                                    else{
                                        showToast("실패");
                                    }
                                }
                            });
                        }
                    })
                    .setNegativeButton("아니요", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    });
            builder.create().show();
        }
    }

    public void DayWeather(double latitude,double longitude) {
        Log.d("DayWeather", "DayWeather의 위도는: "+latitude+"경도는 :"+longitude);
        //https:api.openweathermap.org/data/2.5/forecast?lat=37.2635727&lon=127.0286009&units=metric&appid=7e818b3bfae91bb6fcbe3d382b6c3448
        AndroidNetworking.get("https://api.openweathermap.org/data/2.5/forecast?lat="+latitude+"&lon="+longitude+"&units=metric&appid=7e818b3bfae91bb6fcbe3d382b6c3448")
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            Log.d("weather_onResponse", "onResponse_success: ");
                            JSONArray jsonArray = response.getJSONArray("list");
                            /*
                            시작시간으로 잡아보기 for(int i =0 ; i

                             */
                            for(int i =0;i<6;i++){
                                WeatherData weatherData = new WeatherData();
                                JSONObject list = jsonArray.getJSONObject(i);
                                JSONObject Main = list.getJSONObject("main");
                                JSONArray MainArray = list.getJSONArray("weather");
                                JSONObject Weather = MainArray.getJSONObject(0);
                                String CurrentTime = list.getString("dt_txt");
                                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                SimpleDateFormat formatTime = new SimpleDateFormat("kk:mm");

                                try{
                                    Date time = format.parse(CurrentTime);
                                    CurrentTime =formatTime.format(time);
                                }
                                catch (ParseException e){
                                    e.printStackTrace();
                                }
                                Log.d("onResponse", "onResponse_addData: ");
                                //현재시간

                                weatherData.setTime(CurrentTime);
                                //평균 온도
                                weatherData.setTemp(Main.getDouble("temp"));
                                weatherData.setDescription(Weather.getString("description"));
                                weatherData.setMinTemp(Main.getDouble("temp_min"));
                                weatherData.setMaxTemp(Main.getDouble("temp_max"));

                                arrayWeatherData.add(weatherData);
                            }
                            weatherDayAdapter.notifyDataSetChanged();


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(ANError anError) {

                    }
                });
    }

}