package com.example.thinkanddo;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {
    EditText mEmailEt, mPasswordEt, mNameEt;
    Button mRegisterBtn;
    TextView mHaveAccountTv;
    TextView register_title_tv;

    TextInputLayout password_show_hide;
    ProgressDialog progressDialog;

    Animation title_anim, edittext_anim, remaining_anim;
    Drawable password;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        /*ActionBar actionBar=getSupportActionBar();
        actionBar.setTitle("Create an account");
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);*/

        password_show_hide = findViewById(R.id.password_show_hide);
        register_title_tv = findViewById(R.id.tvRegister);
        mEmailEt = findViewById(R.id.emailET);
        mNameEt = findViewById(R.id.nameEt);
        mPasswordEt = findViewById(R.id.passwordET);
        mRegisterBtn = findViewById(R.id.register_btn);
        mHaveAccountTv =findViewById(R.id.have_accountTv);
        title_anim = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.title_anim);
        edittext_anim = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.edittext_anim);
        remaining_anim = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.remaining_anim);

        mAuth = FirebaseAuth.getInstance();

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Registering User....");

        // Setting register animation

        register_title_tv.setAnimation(title_anim);
        mEmailEt.setAnimation(edittext_anim);
        mPasswordEt.setAnimation(edittext_anim);
        password_show_hide.setAnimation(edittext_anim);
        mNameEt.setAnimation(edittext_anim);
        mRegisterBtn.setAnimation(remaining_anim);
        mHaveAccountTv.setAnimation(remaining_anim);



        mRegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = mEmailEt.getText().toString().trim();
                String password = mPasswordEt.getText().toString().trim();
                final String fullname = mNameEt.getText().toString().trim();

                if(TextUtils.isEmpty(fullname)){
                    mNameEt.setError("Please enter your name");
                    mNameEt.setFocusable(true);
                }
                else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                    mEmailEt.setError("Invalid Email");
                    mEmailEt.setFocusable(true);
                }
                else if (password.length()<6){
                    mPasswordEt.setError("Password length must at least 6 characters");
                    mPasswordEt.setFocusable(true);
                }
                else {
                    registerUser(fullname,email, password);
                }
            }
        });

        mHaveAccountTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                finish();
            }
        });


    }

    private void registerUser(final String fullname, String email, final String password) {

        progressDialog.show();
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            progressDialog.dismiss();
                            final FirebaseUser user = mAuth.getCurrentUser();

                            final String email = user.getEmail();

                            String uid = user.getUid();
                            Uri image = user.getPhotoUrl();
                            String imageString= image.toString();

                            // String name = user.getDisplayName();

                            HashMap<Object, String> hashMap =new HashMap<>();
                            hashMap.put("email", email);
                            hashMap.put("name", fullname);
                            hashMap.put("password", password);
                            hashMap.put("uid", uid);
                            hashMap.put("onlineStatus", "online");
                            hashMap.put("typingTo", "noOne");
                            hashMap.put("phone", "");
                            hashMap.put("uDp","");
                            hashMap.put("image", imageString);
                            hashMap.put("email_verify","false");


                            FirebaseDatabase database = FirebaseDatabase.getInstance();

                            DatabaseReference reference = database.getReference("Users");

                            reference.child(uid).setValue(hashMap);



                            Toast.makeText(RegisterActivity.this, "Registering..\n"+user.getEmail(), Toast.LENGTH_LONG).show();



                            user.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(RegisterActivity.this,"Verification email has been send to: "+user.getEmail(), Toast.LENGTH_LONG).show();
                                    startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                                    finish();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {

                                    Toast.makeText(RegisterActivity.this,"On Failure: Email not sent"+e.getMessage(),Toast.LENGTH_LONG).show();
                                }
                            });

                        } else {
                            // If sign in fails, display a message to the user.
                            progressDialog.dismiss();
                            Toast.makeText(RegisterActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                        }

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(RegisterActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }
    public void finish(){

        super.finish();
        overridePendingTransition(R.anim.activity_move_in_right,R.anim.activity_move_out_left);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();

        return super.onSupportNavigateUp();

    }
}
