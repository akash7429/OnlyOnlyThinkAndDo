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

        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, LoginActivity.class));

            }
        });
    }
}
/* in this part(17)
* Publish Post to firebase.
* Post will contain user name, email, uid, dp, time of publish, title description, image
* user can publish post with or without image
* create AddPostActivity
* images can we imported from gallery or taken from camera
* */
