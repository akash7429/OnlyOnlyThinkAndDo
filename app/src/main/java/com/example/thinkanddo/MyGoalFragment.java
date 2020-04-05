package com.example.thinkanddo;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.thinkanddo.adapters.AdapterGoalDescription;
import com.example.thinkanddo.models.ModelGoalDescription;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MyGoalFragment extends Fragment {

        RecyclerView recyclerView;
        List<ModelGoalDescription> goalDescriptionList;
        AdapterGoalDescription adapterGoalDescription;

        FirebaseAuth firebaseAuth;

    public MyGoalFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view= inflater.inflate(R.layout.activity_all_goal_fragment, container, false);



        firebaseAuth = FirebaseAuth.getInstance();

        // recyclerview and it's properties

        recyclerView = view.findViewById(R.id.goalsRecyclerView);
        Context context;

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());

        // show the newest post first, for this load from last.
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);

        recyclerView.setLayoutManager(layoutManager);

        goalDescriptionList = new ArrayList<>();
        loadGoals();


        return view;
    }

    private void loadGoals() {

            // path of all goals

            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Goal_Description");

            // get all data from this ref.

            ref.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    goalDescriptionList.clear();
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {

                        ModelGoalDescription modelGoal = ds.getValue(ModelGoalDescription.class);

                        goalDescriptionList.add(modelGoal);


                        //adapter

                        adapterGoalDescription = new AdapterGoalDescription(getActivity(), goalDescriptionList);
                        // set adapter to recyclerview
                        recyclerView.setAdapter(adapterGoalDescription);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                    Toast.makeText(getActivity(), "" + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }


    private void searchGoals(final String searchQuery){


        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Goal_Description");

        // get all data from this ref.

        ref.addValueEventListener(new ValueEventListener() {
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

                    adapterGoalDescription= new AdapterGoalDescription(getActivity(), goalDescriptionList);
                    // set adapter to recyclerview
                    recyclerView.setAdapter(adapterGoalDescription);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

                Toast.makeText(getActivity(),""+databaseError.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);

        // searchview to serach posts
        MenuItem item = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);

        // search listener

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                if(!TextUtils.isEmpty(query)){
                    searchGoals(query);
                }
                else{

                    loadGoals();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if(!TextUtils.isEmpty(newText)){
                    searchGoals(newText);
                }
                else{

                    loadGoals();
                }
                return false;
            }
        });

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.action_logout){
            firebaseAuth.signOut();

        }
        else if(id == R.id.action_add_post){
            startActivity(new Intent(getActivity(),GoalDescriptionActivity.class));
        }
        else if(id == R.id.action_settings){

            startActivity(new Intent(getActivity(),SettingsActivity.class));
        }

        return super.onOptionsItemSelected(item);
    }

    }

