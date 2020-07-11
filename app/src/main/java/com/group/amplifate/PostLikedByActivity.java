package com.group.amplifate;

import android.content.Intent;
import android.os.Bundle;

import com.group.amplifate.adapters.AdapterUsers;
import com.group.amplifate.models.ModelUsers;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

public class PostLikedByActivity extends AppCompatActivity {

    String postId;
    RecyclerView recyclerView;

    private List<ModelUsers> userList;
    private AdapterUsers adapterUsers;

    private FirebaseAuth firebaseAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_liked_by);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Post likes");

        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        recyclerView = findViewById(R.id.recyclerView);
        firebaseAuth= FirebaseAuth.getInstance();

        //get tje psot id
        Intent intent = getIntent();
        postId = intent.getStringExtra("postId");


        userList = new ArrayList<>();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Likes");
        ref.child(postId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                userList.clear();

                for(DataSnapshot ds: dataSnapshot.getChildren()){

                    String hisUid = ""+ ds.getRef().getKey();

                    //get user info from
                    
                    getUsers(hisUid);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getUsers(String hisUid) {

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");

        ref.orderByChild("uid").equalTo(hisUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        for(DataSnapshot ds: dataSnapshot.getChildren()){
                        ModelUsers modelUsers = ds.getValue(ModelUsers.class);
                        userList.add(modelUsers);
                        }
                        adapterUsers = new AdapterUsers(PostLikedByActivity.this,userList);

                        recyclerView.setAdapter(adapterUsers);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}
