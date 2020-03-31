package com.example.thinkanddo;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

public class GoalDescriptionActivity extends AppCompatActivity {

    FirebaseAuth firebaseAuth;
    DatabaseReference userDbRef;
    ActionBar actionBar;

    EditText titleEt, descriptionEt;
    Button saveBtn;

    //user info
    String name, email, uid, dp;

    ProgressDialog pd;

    // info of post to be edited
    String editTitle, editDescription;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_describe_goal);



        actionBar = getSupportActionBar();

        actionBar.setTitle("Add New Goal");
        //enable back button in action bar
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);


        pd=new ProgressDialog(this);

        firebaseAuth=FirebaseAuth.getInstance();
        checkUserStatus();


        titleEt=findViewById(R.id.p_goal_title_et);
        descriptionEt=findViewById(R.id.p_goal_description_et);
        saveBtn=findViewById(R.id.pSaveBtn);


        Intent intent = getIntent();
        final String isUpdateKey = ""+intent.getStringExtra("key");
        final String editGoalId  = ""+intent.getStringExtra("editGoalDescriptionId");

        // validate if we came here to update post i.e. came from AdapterPost

        if(isUpdateKey.equals("editGoal")){

            // update

            actionBar.setTitle("Update your goal");
            saveBtn.setText("Update");

        }
        else{

            //add

            actionBar.setTitle("New Goal");
            saveBtn.setText("Save");
        }


        saveBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                //get description from edit text
                String title=titleEt.getText().toString().trim();
                String description = descriptionEt.getText().toString().trim();

                if(TextUtils.isEmpty(title)){
                    Toast.makeText(GoalDescriptionActivity.this,"Enter Title....",Toast.LENGTH_SHORT).show();
                    return;
                }
                if(TextUtils.isEmpty(description)){
                    Toast.makeText(GoalDescriptionActivity.this,"Enter Description....",Toast.LENGTH_SHORT).show();
                    return;
                }

               if(isUpdateKey.equals("editGoal")){
                    // actionBar.setTitle("Update Post");
                    // uploadBtn.setText("Update");

                    beginUpdate(title, description, editGoalId);

                }
                else{

                    uploadData(title, description);
                }


            }
        });
    }

    private void beginUpdate(String title, String description, String editGoalId) {

        HashMap<String, Object> hashMap = new HashMap<>();
        // put post info
        hashMap.put("uid", uid);
        hashMap.put("uName", name);
        hashMap.put("uEmail", email);
        hashMap.put("uDp", dp);
        hashMap.put("gTitle",title);
        hashMap.put("gDescr",description);


        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Goal_Description");
        ref.child(editGoalId)
                .updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        pd.dismiss();
                        Toast.makeText(GoalDescriptionActivity.this,"Updated...",Toast.LENGTH_LONG).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pd.dismiss();
                Toast.makeText(GoalDescriptionActivity.this,""+e.getMessage(),Toast.LENGTH_LONG).show();
            }
        });
    }

    private void uploadData(final String title, final String description) {
        pd.setMessage("Saving your goal...");
        pd.show();

        final String timeStamp= String.valueOf(System.currentTimeMillis());
        String filePathAndName="Goal Description/" + "goalDes_" + timeStamp;



            //post without image
            HashMap<Object, String> hashMap=new HashMap<>();
            hashMap.put("uid",uid);
            hashMap.put("uName",name);
            hashMap.put("uEmail",email);
            hashMap.put("uDp",dp);
            hashMap.put("gdId",timeStamp);
            hashMap.put("gTitle",title);
            hashMap.put("gDescr",description);
            hashMap.put("gTime",timeStamp);



            //path to store post data
            DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Goal_Description");
            //
            ref.child(timeStamp).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    pd.dismiss();
                    Toast.makeText(GoalDescriptionActivity.this,"Goal Saved",Toast.LENGTH_SHORT).show();
                    titleEt.setText("");
                    descriptionEt.setText("");

                }
            })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            pd.dismiss();
                            Toast.makeText(GoalDescriptionActivity.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
                        }
                    });
        }


    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        menu.findItem(R.id.action_add_post).setVisible(false);
        menu.findItem(R.id.action_search).setVisible(false);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_logout) {
            firebaseAuth.signOut();
            checkUserStatus();
        }
        else if(id==R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }



    private void checkUserStatus(){
        FirebaseUser user =firebaseAuth.getCurrentUser();
        if(user!=null){
            //mprofileTv.setText(user.getEmail());
            email=user.getEmail();
            uid=user.getUid();

        }else{
            startActivity(new Intent(this,MainActivity.class));
            finish();
        }
    }

}

