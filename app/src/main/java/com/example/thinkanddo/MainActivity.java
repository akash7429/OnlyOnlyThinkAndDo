package com.example.thinkanddo;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    Button mRegisterBtn, mLoginBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRegisterBtn=findViewById(R.id.register_btn);
        mLoginBtn=findViewById(R.id.login_btn);

        mRegisterBtn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                //start Register Activity
                startActivity(new Intent(MainActivity.this, RegisterActivity.class));

            }
        });
    }
}
