package com.example.daystarter.ui.groupSchedule;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Database;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.daystarter.R;
import com.example.daystarter.databinding.ActivityWritingGroupScheduleBinding;
import com.example.daystarter.model.NotificationModel;
import com.example.daystarter.ui.groupSchedule.myClass.GroupScheduleModel;
import com.example.daystarter.ui.groupSchedule.myClass.Member;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class WritingGroupScheduleActivity extends AppCompatActivity  implements View.OnClickListener{
    ActivityWritingGroupScheduleBinding binding;
    String groupId;
    Calendar beforeCalendar = new GregorianCalendar(), afterCalendar = new GregorianCalendar();
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy년 MM월 dd일", Locale.getDefault());
    SimpleDateFormat timeFormat = new SimpleDateFormat("HH시 mm분", Locale.getDefault());
    long finishCount;
    long currentCount = 0;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWritingGroupScheduleBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setFirst();

        binding.saveButton.setOnClickListener(this);
        binding.cancelButton.setOnClickListener(this);
        binding.beforeDateTextView.setOnClickListener(this);
        binding.beforeTimeTextView.setOnClickListener(this);
        binding.afterDateTextView.setOnClickListener(this);
        binding.afterTimeTextView.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        int id = view.getId();

        if(id == binding.beforeDateTextView.getId()){
            new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                    setBeforeDate(new GregorianCalendar(i, i1, i2, beforeCalendar.get(Calendar.HOUR_OF_DAY), beforeCalendar.get(Calendar.MINUTE)));
                }
            }, beforeCalendar.get(Calendar.YEAR), beforeCalendar.get(Calendar.MONTH), beforeCalendar.get(Calendar.DAY_OF_MONTH)).show();
        }
        else if(id == binding.afterDateTextView.getId()){
            new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                    setAfterDate(new GregorianCalendar(i, i1, i2, afterCalendar.get(Calendar.HOUR_OF_DAY), afterCalendar.get(Calendar.MINUTE)));
                }
            }, afterCalendar.get(Calendar.YEAR), afterCalendar.get(Calendar.MONTH), afterCalendar.get(Calendar.DAY_OF_MONTH)).show();
        }

        else if(id == binding.beforeTimeTextView.getId()) {
            new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
                @Override
                public void onTimeSet(TimePicker timePicker, int i, int i1) {
                    setBeforeDate(new GregorianCalendar(beforeCalendar.get(Calendar.YEAR), beforeCalendar.get(Calendar.MONTH), beforeCalendar.get(Calendar.DAY_OF_MONTH), i, i1));
                }
            }, beforeCalendar.get(Calendar.HOUR_OF_DAY), beforeCalendar.get(Calendar.MINUTE), false).show();
        }

        else if(id == binding.afterTimeTextView.getId()){
            new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
                @Override
                public void onTimeSet(TimePicker timePicker, int i, int i1) {
                    setAfterDate(new GregorianCalendar(afterCalendar.get(Calendar.YEAR), afterCalendar.get(Calendar.MONTH), afterCalendar.get(Calendar.DAY_OF_MONTH), i, i1));
                }
            }, afterCalendar.get(Calendar.HOUR_OF_DAY), afterCalendar.get(Calendar.MINUTE), false).show();
        }

        //작성 권한 확인 후 저장 or 종료
        else if(id == binding.saveButton.getId()){
            checkStatus();
        }
        else if(id == binding.cancelButton.getId()){
            finish();
        }
    }

    void checkGroupId(){
        groupId = getIntent().getStringExtra("groupId");
        if(groupId == null){
            finish();
        }
    }

    void checkStatus(){
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
        dbRef.child("groups").child(groupId).child("members").child(FirebaseAuth.getInstance().getUid()).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                Member member = task.getResult().getValue(Member.class);
                if (member.status.equals("host") || member.status.equals("write")){
                    write();
                }
                else{
                    showToast("오류");
                    finish();
                }
            }
        });

    }

    void write(){
        String title = binding.titleEditText.getText().toString().trim();
        long beforeTime = beforeCalendar.getTimeInMillis();
        long afterTime = afterCalendar.getTimeInMillis();
        String contents = binding.contentsEditText.getText().toString().trim();

        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
        String key = dbRef.child("schedules").child(groupId).push().getKey();

        GroupScheduleModel gsm = new GroupScheduleModel(key, FirebaseAuth.getInstance().getUid(), Calendar.getInstance().getTimeInMillis(), title, beforeTime, afterTime, contents);
        dbRef.child("schedules").child(groupId).child(key).setValue(gsm).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()) {
                    showToast("저장되었습니다");
                    sendGcm();
                }
                else {
                    showToast("에러. 다시 시도해주세요");
                }
            }
        });
    }

    public void setBeforeDate(Calendar calendar) {
        beforeCalendar = calendar;

        binding.beforeDateTextView.setText(dateFormat.format(beforeCalendar.getTime()));
        binding.beforeTimeTextView.setText(timeFormat.format(beforeCalendar.getTime()));

        if(beforeCalendar.after(afterCalendar))
            setAfterDate(beforeCalendar);
    }

    public void setAfterDate(Calendar calendar) {
        afterCalendar = calendar;

        binding.afterDateTextView.setText(dateFormat.format(afterCalendar.getTime()));
        binding.afterTimeTextView.setText(timeFormat.format(afterCalendar.getTime()));

        if(afterCalendar.before(beforeCalendar))
            setBeforeDate(afterCalendar);
    }

    private void showToast(String str){
        Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
    }

    public void setFirst(){
        checkGroupId();
        Intent intent = getIntent();
        binding.titleEditText.setText(intent.getStringExtra("title"));
        beforeCalendar.setTimeInMillis(intent.getLongExtra("beforeLong", Calendar.getInstance().getTimeInMillis()));
        afterCalendar.setTimeInMillis(intent.getLongExtra("afterLong", beforeCalendar.getTimeInMillis()));
        setBeforeDate(beforeCalendar);
        setAfterDate(afterCalendar);
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

                        Log.d(TAG, "member: " + member.name);
                        //자기자신에게 노티를 보내지는 않도록
                        if (member.alarmSet && !uid.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                            Log.d(TAG, "go");
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
                            Log.d(TAG, "can't go");
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
        notificationModel.data.title = binding.beforeDateTextView.getText().toString() + "에 새로운 일정이 등록되었습니다.";
        notificationModel.data.body = "'" + binding.titleEditText.getText().toString() + "'";
        notificationModel.data.type = "group_schedule";
        notificationModel.data.groupId = groupId;
        notificationModel.to = token;
        RequestBody requestBody = RequestBody.create(gson.toJson(notificationModel), MediaType.parse("application/json; charset=utf8"));

        Request request = new Request.Builder()
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
}