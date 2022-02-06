package com.example.daystarter;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import com.example.daystarter.databinding.ActivityMainBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;

    private NavigationView navigationView;
    private Button signOutButton;
    View headerView;
    FirebaseUser user;
    Bitmap profileBitmap;

    private static String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarMain.toolbar);


        DrawerLayout drawer = binding.drawerLayout; //drawer = 메뉴
        navigationView = binding.navView; //navigation = 메뉴 내부 선택지들

        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_personal_schedule, R.id.nav_group_schedule, R.id.nav_news, R.id.nav_weather)
                .setOpenableLayout(drawer)
                .build();


        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);


        navController.addOnDestinationChangedListener(new NavController.OnDestinationChangedListener() {
            @Override
            public void onDestinationChanged(@NonNull NavController controller, @NonNull NavDestination destination, @Nullable Bundle arguments) {
                switch (destination.getId()) {
                    case R.id.nav_personal_schedule:
                    case R.id.nav_group_schedule:
                        binding.appBarMain.fab.setVisibility(View.VISIBLE);
                        binding.appBarMain.fab.invalidate();
                        Log.d(TAG, "onDestinationChanged: "+ destination.getLabel());
                        break;
                    default:
                        binding.appBarMain.fab.setVisibility(View.GONE);
                        binding.appBarMain.fab.invalidate();
                        Log.d(TAG, "onDestinationChanged: "+ destination.getLabel());
                        break;
                }
            }
        });

        binding.appBarMain.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String text;
                switch (navigationView.getCheckedItem().getItemId()) {
                    case R.id.nav_personal_schedule:
                        text = "개인 스케줄 추가";
                        break;
                    case R.id.nav_group_schedule:
                        text = "그룹 스케줄";
                        break;
                    default:
                        text = "오류";
                }
                Snackbar.make(view, text, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        headerView = navigationView.getHeaderView(0);//헤더뷰 (전연변수로 접근)

        signOutButton = headerView.findViewById(R.id.signOutButton);
        signOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signOut();
                updateUI();
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();
        updateUI();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    private void updateUI(){
        ImageView profileImageView;
        TextView emailTextView;

        user = FirebaseAuth.getInstance().getCurrentUser();
        if(user != null) {
            Log.d(TAG, "유저 이메일 = " + user.getEmail());

            profileImageView = headerView.findViewById(R.id.profilePhoto);

            if(user.getPhotoUrl()!=null) {
                Thread profileThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "run: start");
                        profileBitmap = getImageBitmap(user.getPhotoUrl().toString());
                    }
                });

                profileThread.start();
                try {
                    Log.d(TAG, "updateUI: join");
                    profileThread.join();
                    profileImageView.setImageBitmap(profileBitmap);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            emailTextView = headerView.findViewById(R.id.emailTextView);
            emailTextView.setText(user.getEmail());

            signOutButton.setVisibility(View.VISIBLE);
        }
    }

    private void signOut(){
        FirebaseAuth.getInstance().signOut();
    }

    private Bitmap getImageBitmap(String url) {
        Bitmap bm = null;
        try {
            Log.d(TAG, "getImageBitmap: " + url);
            URL aURL = new URL(url);
            URLConnection conn = aURL.openConnection();
            conn.connect();
            InputStream is = conn.getInputStream();
            BufferedInputStream bis = new BufferedInputStream(is);
            bm = BitmapFactory.decodeStream(bis);
            bis.close();
            is.close();
        } catch (IOException e) {
            Log.e(TAG, "Error getting bitmap", e);
        }
        return bm;
    }

}