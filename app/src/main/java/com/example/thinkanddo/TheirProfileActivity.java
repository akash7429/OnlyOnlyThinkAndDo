package com.example.thinkanddo;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.thinkanddo.adapters.AdapterGoalDescription;
import com.example.thinkanddo.adapters.AdapterPost;
import com.example.thinkanddo.models.ModelGoalDescription;
import com.example.thinkanddo.models.ModelPost;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class TheirProfileActivity extends AppCompatActivity {

    FirebaseAuth firebaseAuth;
    ImageView avatarIv, coverIv,arrow_iv;
    TextView nameTv,my_post_tv,num_of_posts ;
    TextView no_post_tv, num_of_goals;
    RecyclerView postsRecyclerView,goalsRecyclerView;

    LinearLayout goals_accom_ll, posts_accom_ll;
    List<ModelPost> postList;
    AdapterPost adapterPost;

    List<ModelGoalDescription> goalDescriptionList;
    AdapterGoalDescription adapterGoalDescription;
    String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_their_profile);

        firebaseAuth = FirebaseAuth.getInstance();
        final ActionBar actionBar = getSupportActionBar();

        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);


       // coverIv = findViewById(R.id.coverIv);
        avatarIv = findViewById(R.id.avatarIv);
        nameTv = findViewById(R.id.nameTv);
        no_post_tv=findViewById(R.id.no_post_tv);
        posts_accom_ll=findViewById(R.id.post_accomplish_ll);
        goals_accom_ll=findViewById(R.id.goals_their_accomplish_ll);

        num_of_goals =findViewById(R.id.number_of_goals_tv);
        num_of_posts = findViewById(R.id.number_of_posts_tv);

        //emailTv = findViewById(R.id.emailTv);
      //  phoneTv = findViewById(R.id.phoneTv);
        postsRecyclerView= findViewById(R.id.recyclerview_posts);
        goalsRecyclerView = findViewById(R.id.recyclerview_goals);




        Intent intent = getIntent();
        uid = intent.getStringExtra("uId");


        Query query = FirebaseDatabase.getInstance().getReference("Users").orderByChild("uid").equalTo(uid);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    String name = ""+ds.child("name").getValue();
                    String email = ""+ds.child("email").getValue();
                    String phone = ""+ds.child("phone").getValue();
                    String image = ""+ds.child("image").getValue();
                    String cover = ""+ds.child("cover").getValue();

                    nameTv.setText(name);
                    //emailTv.setText(email);
                    actionBar.setTitle(name);

                    try{

                        Picasso.get().load(image).into(avatarIv);

                    }catch (Exception e){

                        Picasso.get().load(R.drawable.ic_user_dp).into(avatarIv);
                    }

                    try{

                        Picasso.get().load(cover).into(coverIv);

                    }catch (Exception e){

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        postList= new ArrayList<>();
        goalDescriptionList= new ArrayList<>();

        checkUserStatus();
        loadGoals();
        loadHisPost();

       posts_accom_ll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                loadHisPost();
            }
        });

        goals_accom_ll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                loadGoals();
            }
        });



    }
    private void searchHisPosts(final String searchQuery){
        LinearLayoutManager layoutManager= new LinearLayoutManager(TheirProfileActivity.this);
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        postsRecyclerView.setLayoutManager(layoutManager);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        Query query= ref.orderByChild("uid").equalTo(uid);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                postList.clear();
                for (DataSnapshot ds: dataSnapshot.getChildren()){

                    ModelPost myPosts = ds.getValue(ModelPost.class);

                    if(myPosts.getpTitle().toLowerCase().contains(searchQuery.toLowerCase()) ||
                            myPosts.getpDescr().toLowerCase().contains(searchQuery.toLowerCase())){
                        postList.add(myPosts);
                    }

                    adapterPost = new AdapterPost(TheirProfileActivity.this,postList);

                    postsRecyclerView.setAdapter(adapterPost);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

                Toast.makeText(TheirProfileActivity.this,""+databaseError.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadHisPost() {
        LinearLayoutManager layoutManager= new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        postsRecyclerView.setLayoutManager(layoutManager);
        postsRecyclerView.setVisibility(View.VISIBLE);
        goalsRecyclerView.setVisibility(View.GONE);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        Query query= ref.orderByChild("uid").equalTo(uid);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                postList.clear();
                for (DataSnapshot ds: dataSnapshot.getChildren()){

                    ModelPost myPosts = ds.getValue(ModelPost.class);

                    postList.add(myPosts);

                    adapterPost = new AdapterPost(TheirProfileActivity.this,postList);

                    postsRecyclerView.setAdapter(adapterPost);

                }

                num_of_posts.setText(String.valueOf(postList.size()));
                if(postList.size()==0){

                    no_post_tv.setVisibility(View.VISIBLE);
                    no_post_tv.setText("No posts Added");
                }
                else{
                    no_post_tv.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

                Toast.makeText(TheirProfileActivity.this,""+databaseError.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadGoals() {


        LinearLayoutManager layoutManager = new LinearLayoutManager(TheirProfileActivity.this);
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        goalsRecyclerView.setHasFixedSize(true);
        goalsRecyclerView.setLayoutManager(layoutManager);
        goalsRecyclerView.setVisibility(View.VISIBLE);
        postsRecyclerView.setVisibility(View.GONE);

        // path of al
        // l goals

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Goal_Description");

        Query query= ref.orderByChild("uid").equalTo(uid);
        // get all data from this ref.

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                goalDescriptionList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {

                    ModelGoalDescription modelGoal = ds.getValue(ModelGoalDescription.class);

                    goalDescriptionList.add(modelGoal);

                    //adapter

                    adapterGoalDescription = new AdapterGoalDescription(TheirProfileActivity.this, goalDescriptionList);
                    // set adapter to recyclerview
                    goalsRecyclerView.setAdapter(adapterGoalDescription);
                }

                num_of_goals.setText(String.valueOf(goalDescriptionList.size()));
                if(goalDescriptionList.size()==0){


                    no_post_tv.setVisibility(View.VISIBLE);
                    no_post_tv.setText("No goals added");
                }
                else{
                    no_post_tv.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

                Toast.makeText(TheirProfileActivity.this, "" + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void searchGoals(final String searchQuery){

        LinearLayoutManager layoutManager = new LinearLayoutManager(TheirProfileActivity.this);
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        goalsRecyclerView.setHasFixedSize(true);
        goalsRecyclerView.setLayoutManager(layoutManager);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Goal_Description");

        // get all data from this ref.
        Query query= ref.orderByChild("uid").equalTo(uid);
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

                    adapterGoalDescription= new AdapterGoalDescription(TheirProfileActivity.this, goalDescriptionList);
                    // set adapter to recyclerview
                    goalsRecyclerView.setAdapter(adapterGoalDescription);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

                Toast.makeText(TheirProfileActivity.this,""+databaseError.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });

    }


    private void checkUserStatus(){
        FirebaseUser user =firebaseAuth.getCurrentUser();
        if(user!=null){
            //mprofileTv.setText(user.getEmail());

        }else{
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_main, menu);
        menu.findItem(R.id.action_add_post).setVisible(false);

        MenuItem item = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                if(!TextUtils.isEmpty(s)){
                    searchHisPosts(s);
                    searchGoals(s);
                }else {
                    loadHisPost();
                    loadGoals();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                if(!TextUtils.isEmpty(s)){
                    searchHisPosts(s);
                    searchGoals(s);
                }else {
                    loadHisPost();
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
        if(id == R.id.action_settings){

            startActivity(new Intent(TheirProfileActivity.this,SettingsActivity.class));
        }

        else if(id == R.id.action_logout){
            firebaseAuth.signOut();
            checkUserStatus();
        }

        return super.onOptionsItemSelected(item);
    }
}
