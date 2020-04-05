package com.example.thinkanddo;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.thinkanddo.adapters.AdapterDefineMyGoals;
import com.example.thinkanddo.models.ModelGoalDescription;
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
import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class DefineMyGoalsActivity extends AppCompatActivity {

    FirebaseAuth firebaseAuth;
    DatabaseReference userDbRef;
    ActionBar actionBar;

    RecyclerView recyclerView;
    EditText titleEt, descriptionEt;
    Button saveBtn;

    ProgressDialog pd;

    List<ModelGoalDescription> goalDescriptionList;
    AdapterDefineMyGoals adapterGoalDescription;
    String uID;

    // info of post to be edited
    String editTitle, editDescription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_goals);


        actionBar = getSupportActionBar();

        actionBar.setTitle("Select a goal");
        //enable back button in action bar
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        firebaseAuth = FirebaseAuth.getInstance();

        // recyclerview and it's properties

        recyclerView = findViewById(R.id.mygoalsRecyclerView);
        goalDescriptionList = new ArrayList<>();
        checkUserStatus();
        loadGoals();


    }

    private void loadGoals() {


        LinearLayoutManager layoutManager = new LinearLayoutManager(DefineMyGoalsActivity.this);
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);
        // path of al
        // l goals

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Goal_Description");

        Query query= ref.orderByChild("uid").equalTo(uID);
        // get all data from this ref.

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                goalDescriptionList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {

                    ModelGoalDescription modelGoal = ds.getValue(ModelGoalDescription.class);

                    goalDescriptionList.add(modelGoal);

                    //adapter

                    adapterGoalDescription = new AdapterDefineMyGoals(DefineMyGoalsActivity.this, goalDescriptionList);
                    // set adapter to recyclerview
                    recyclerView.setAdapter(adapterGoalDescription);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

                Toast.makeText(DefineMyGoalsActivity.this, "" + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void searchGoals(final String searchQuery){

        LinearLayoutManager layoutManager = new LinearLayoutManager(DefineMyGoalsActivity.this);
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Goal_Description");

        // get all data from this ref.
        Query query= ref.orderByChild("uid").equalTo(uID);
        // get all data from this ref.

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                goalDescriptionList.clear();
                for(DataSnapshot ds : dataSnapshot.getChildren()){

                    ModelGoalDescription modelGoal = ds.getValue(ModelGoalDescription.class);

                    if(modelGoal.getgTitle().toLowerCase().contains(searchQuery.toLowerCase())||
                            modelGoal.getgDescr().toLowerCase().contains(searchQuery.toLowerCase())) {

                        goalDescriptionList.add(modelGoal);
                    }


                    //adapter

                    adapterGoalDescription= new AdapterDefineMyGoals(DefineMyGoalsActivity.this, goalDescriptionList);
                    // set adapter to recyclerview
                    recyclerView.setAdapter(adapterGoalDescription);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

                Toast.makeText(DefineMyGoalsActivity.this,""+databaseError.getMessage(),Toast.LENGTH_SHORT).show();
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
        MenuItem item = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                if(!TextUtils.isEmpty(s)){
                    searchGoals(s);
                }else {
                    loadGoals();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                if(!TextUtils.isEmpty(s)){
                    searchGoals(s);
                }else {
                    loadGoals();
                }
                return false;
            }
        });
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

            uID = user.getUid();

        }else{
            startActivity(new Intent(DefineMyGoalsActivity.this,MainActivity.class));
            finish();
        }
    }
}
