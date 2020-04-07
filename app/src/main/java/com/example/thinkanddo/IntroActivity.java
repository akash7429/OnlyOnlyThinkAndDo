package com.example.thinkanddo;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;

import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

public class IntroActivity extends AppCompatActivity {

    private ViewPager screenPager;

    IntroViewPagerAdapter introViewPagerAdapter;
    TabLayout tabIndicator;
    Button btnSkip;
    Button btnGetStarted;
    int position=0;
    Animation btnAnim,btnAnimReset;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // make the activity on full screen

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        getSupportActionBar().hide();


        // Checking if user had already visited slides.
        if(restorePrefData()){

            Intent mainActivity = new Intent(getApplicationContext(),DashboardActivity.class);
            startActivity(mainActivity);
            finish();
        }

        setContentView(R.layout.activity_intro);

        // ini views
        tabIndicator = findViewById(R.id.tab_indicator);
        btnSkip = findViewById(R.id.btn_skip);
        btnGetStarted = findViewById(R.id.btnGetStarted);
        Context context;
        btnAnim = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.button_animation);
        btnAnimReset = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.button_animation_reset );


        final List<ScreenItem> mList = new ArrayList<>();
        mList.add(new ScreenItem("Welcome \nto \nAmplifate","",R.mipmap.ic_logo_4));
        mList.add(new ScreenItem("Connect with people","Think & Do is a great platform to interact with an enthusiastic community",R.drawable.ic_connect_people));
        mList.add(new ScreenItem("Create Goals","Make goals that you want to accomplish in present or future",R.drawable.ic_goal));
        mList.add(new ScreenItem("Post Your Achievements","Achieved your goal? Post a video and share your journey",R.drawable.ic_achieve_post));

        screenPager = findViewById(R.id.screen_viewpager);
        introViewPagerAdapter = new IntroViewPagerAdapter(this,mList);
        screenPager.setAdapter(introViewPagerAdapter);

        // setup tablayout with viewpager

        tabIndicator.setupWithViewPager(screenPager);

        btnSkip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                startActivity(intent);
                savePrefsData();
                finish();
            }
        });


        tabIndicator.addOnTabSelectedListener(new TabLayout.BaseOnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

                if(tab.getPosition() == mList.size()-1){
                    loadLastScreen();
                }

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {


            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        btnGetStarted.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                startActivity(intent);

                // we need to save boolean value to storage so next time user login we know that user already gone through slides

                savePrefsData();
                finish();
            }
        });
    }

    private boolean restorePrefData() {

        SharedPreferences pref = getApplicationContext().getSharedPreferences("myPrefs",MODE_PRIVATE);
        Boolean isIntroActivityOpened = pref.getBoolean("isIntroOpened",false);
        return isIntroActivityOpened;
    }

    private void savePrefsData() {

        SharedPreferences pref = getApplicationContext().getSharedPreferences("myPrefs",MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();

        editor.putBoolean("isIntroOpened",true);
        editor.commit();
    }


    private void loadLastScreen(){

        btnSkip.setVisibility(View.INVISIBLE);
        btnGetStarted.setVisibility(View.VISIBLE);

        btnGetStarted.setAnimation(btnAnim);


    }

    private void loadNormalScreen(){

        btnSkip.setVisibility(View.VISIBLE);
        btnGetStarted.setVisibility(View.INVISIBLE);

        btnGetStarted.setAnimation(btnAnimReset);

    }
}
