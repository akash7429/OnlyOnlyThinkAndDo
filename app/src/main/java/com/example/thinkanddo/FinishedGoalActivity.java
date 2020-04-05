package com.example.thinkanddo;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.thinkanddo.adapters.AdapterGoalFinish;
import com.example.thinkanddo.models.ModelGoalFinish;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class FinishedGoalActivity extends AppCompatActivity {

    FirebaseAuth firebaseAuth;
    DatabaseReference userDbRef;
    ActionBar actionBar;

    EditText titleEt, descriptionEt;
    Button saveBtn;

    TextView no_finished_goals;
    //user info
    String name, email, uid, dp;

    ProgressDialog pd;

    // info of post to be edited
    String editTitle, editDescription;

    RecyclerView goalfinishrecyclerview;
    List<ModelGoalFinish> goalDescriptionList;
    AdapterGoalFinish adapterGoalDescription;
    String goal_accomplish;
    String gid,gTitle,gDescr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finished_goal);


        goalfinishrecyclerview=findViewById(R.id.recyclerview_finished_goals);
        no_finished_goals = findViewById(R.id.no_goal_finish_tv);

        actionBar = getSupportActionBar();

        actionBar.setTitle("Goals Achieved");
        //enable back button in action bar
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        pd=new ProgressDialog(this);

        firebaseAuth=FirebaseAuth.getInstance();

        /*Intent intent = getIntent();
        gid = intent.getStringExtra("finishGoalId");
        gTitle = intent.getStringExtra("GoalTitle");
        gDescr = intent.getStringExtra("GoalDescr");
        name = intent.getStringExtra("UserName");
        uid = intent.getStringExtra("Uid");*/
        goalDescriptionList= new ArrayList<>();

        checkUserStatus();


        loadFinishGoal();


    }

    private void loadFinishGoal() {

        LinearLayoutManager layoutManager = new LinearLayoutManager(FinishedGoalActivity.this);
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        goalfinishrecyclerview.setHasFixedSize(true);
        goalfinishrecyclerview.setLayoutManager(layoutManager);

        // path of al
        // l goals

        FirebaseUser user = firebaseAuth.getCurrentUser();
        uid=user.getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Goal_Finished");

        Query query= ref.orderByChild("uid").equalTo(uid);
        // get all data from this ref.

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                goalDescriptionList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {

                    ModelGoalFinish modelGoal = ds.getValue(ModelGoalFinish.class);

                    goalDescriptionList.add(modelGoal);

                    //adapter

                    adapterGoalDescription = new AdapterGoalFinish(FinishedGoalActivity.this, goalDescriptionList);
                    // set adapter to recyclerview
                    goalfinishrecyclerview.setAdapter(adapterGoalDescription);
                }


                goal_accomplish = String.valueOf(goalDescriptionList.size());

                if(goalDescriptionList.size()==0){


                    no_finished_goals.setVisibility(View.VISIBLE);
                    no_finished_goals.setText("No goals added");
                }
                else{
                    no_finished_goals.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

                Toast.makeText(FinishedGoalActivity.this, "" + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
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
            //uid=user.getUid();
           // name=user.getDisplayName();

        }else{
            startActivity(new Intent(FinishedGoalActivity.this,MainActivity.class));
            finish();
        }
    }
}
