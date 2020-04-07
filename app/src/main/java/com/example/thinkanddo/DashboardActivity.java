package com.example.thinkanddo;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.MenuItem;

import com.example.thinkanddo.notifications.Token;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

public class DashboardActivity extends AppCompatActivity {

    FirebaseAuth firebaseAuth;

    ActionBar actionBar;

    String mUID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);


        actionBar = getSupportActionBar();

        firebaseAuth = FirebaseAuth.getInstance();

        BottomNavigationView navigationView = findViewById(R.id.navigation);
        navigationView.setOnNavigationItemSelectedListener(selectedListener);

        navigationView.getMenu().findItem(R.id.nav_home).setChecked(true);

        actionBar.setTitle("Amplifate");
        HomeFragment fragment1 = new HomeFragment();

        FragmentTransaction ft1 =getSupportFragmentManager().beginTransaction();
        ft1.replace(R.id.content, fragment1, "");
        ft1.commit();

        checkUserStatus();

       final ColorDrawable colorDrawable
                = new ColorDrawable(Color.parseColor("#0F9D58"));





    }
    @Override
    protected void onResume(){
        checkUserStatus();
        super.onResume();
    }
    public void updateToken(String token){
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Tokens");
        Token mToken=new Token(token);
        ref.child(mUID).setValue(mToken);
    }

    private BottomNavigationView.OnNavigationItemSelectedListener selectedListener =new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
            switch (menuItem.getItemId()){

                case R.id.nav_profile:
                    actionBar.setTitle("Profile");
                    ProfileFragment fragment2 = new ProfileFragment();
                    FragmentTransaction ft2 =getSupportFragmentManager().beginTransaction();
                    ft2.replace(R.id.content, fragment2, "");
                    ft2.commit();
                    return true;
                case R.id.nav_home:
                    actionBar.setTitle("Amplifate");
                    HomeFragment fragment1 = new HomeFragment();
                    FragmentTransaction ft1 =getSupportFragmentManager().beginTransaction();
                    ft1.replace(R.id.content, fragment1, "");
                    ft1.commit();
                    return true;
                case R.id.nav_users:
                    actionBar.setTitle("Users");
                    UsersFragment fragment3 = new UsersFragment();
                    FragmentTransaction ft3 =getSupportFragmentManager().beginTransaction();
                    ft3.replace(R.id.content, fragment3, "");
                    ft3.commit();
                    return true;
                case R.id.nav_chat:
                    actionBar.setTitle("Chats");
                    ChatListFragment fragment4 = new ChatListFragment();
                    FragmentTransaction ft4 =getSupportFragmentManager().beginTransaction();
                    ft4.replace(R.id.content, fragment4, "");
                    ft4.commit();
                    return true;
                case R.id.nav_goals:
                    actionBar.setTitle("All Goals");
                    MyGoalFragment fragment5 = new MyGoalFragment();
                    FragmentTransaction ft5 =getSupportFragmentManager().beginTransaction();
                    ft5.replace(R.id.content, fragment5, "");
                    ft5.commit();
                    return true;
            }

            return false;
        }
    };

    private void checkUserStatus(){
        FirebaseUser user =firebaseAuth.getCurrentUser();
        if(user!=null){
            //mprofileTv.setText(user.getEmail());
        mUID= user.getUid();

        //save Uid of currently signed in user in Shared preferences
        SharedPreferences sp=getSharedPreferences("SP_USER",MODE_PRIVATE);
        SharedPreferences.Editor editor=sp.edit();
        editor.putString("Current_USERID",mUID);
        editor.apply();

        // update tokken

        updateToken(FirebaseInstanceId.getInstance().getToken());

        }else{
            startActivity(new Intent(DashboardActivity.this, MainActivity.class));
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.navigation);
        int seletedItemId = bottomNavigationView.getSelectedItemId();
        if (R.id.nav_home != seletedItemId) {
            setHomeItem(DashboardActivity.this);
        } else {
            super.onBackPressed();
        }
    }

    public static void setHomeItem(Activity activity) {
        BottomNavigationView bottomNavigationView = (BottomNavigationView)
                activity.findViewById(R.id.navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_home);
    }

    @Override
    protected void onStart() {
        checkUserStatus();

        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Users").child(mUID);
        HashMap<String, Object> hashMap =new HashMap<>();
        hashMap.put("email_verify","true");
        dbRef.updateChildren(hashMap);

        super.onStart();
    }

}
