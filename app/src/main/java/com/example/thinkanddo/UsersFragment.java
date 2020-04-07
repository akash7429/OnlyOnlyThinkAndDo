package com.example.thinkanddo;


import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.example.thinkanddo.adapters.AdapterUsers;
import com.example.thinkanddo.models.ModelUsers;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


/**
 * A simple {@link Fragment} subclass.
 */
public class UsersFragment extends Fragment {


    RecyclerView recyclerView;
    AdapterUsers adapterUsers;
    List<ModelUsers> usersList;
    FirebaseAuth firebaseAuth;
    public UsersFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_users, container, false);

        recyclerView = (RecyclerView) view.findViewById(R.id.users_recyclerView);

        // setting it's properties.
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        firebaseAuth = FirebaseAuth.getInstance();

        // init user list

        usersList = new ArrayList<>();

        getAllUsers();
        return view;
    }

    private void getAllUsers() {
        // get current user
        final FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();
        // get path of database named "Users" containing users info

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                usersList.clear();
                for(DataSnapshot ds: dataSnapshot.getChildren()){
                    ModelUsers modelUsers = ds.getValue(ModelUsers.class);

                    if(!modelUsers.getUid().equals(fUser.getUid()) && modelUsers.getEmail_verify().equals("true")){

                        usersList.add(modelUsers);
                    }

                    adapterUsers = new AdapterUsers(getActivity(), usersList);
                    // set adapter to recyclerview
                    recyclerView.setAdapter(adapterUsers);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void searchUsers(final String query) {

        // get current user
        final FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();
        // get path of database named "Users" containing users info

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                usersList.clear();
                for(DataSnapshot ds: dataSnapshot.getChildren()){
                    ModelUsers modelUsers = ds.getValue(ModelUsers.class);

                    // get all searched users except currently signed in user
                    if(!modelUsers.getUid().equals(fUser.getUid())){

                        if(modelUsers.getName().toLowerCase().contains(query.toLowerCase()) ||
                                modelUsers.getEmail().toLowerCase().contains(query.toLowerCase()))
                        {

                            usersList.add(modelUsers);
                        }

                    }
                    //adapter
                    adapterUsers = new AdapterUsers(getActivity(), usersList);
                    // set adapter to recyclerview

                    adapterUsers.notifyDataSetChanged();
                    recyclerView.setAdapter(adapterUsers);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void checkUserStatus(){
        FirebaseUser user =firebaseAuth.getCurrentUser();
        if(user!=null){
            //mprofileTv.setText(user.getEmail());

        }else{
            startActivity(new Intent(getActivity(), MainActivity.class));
            Objects.requireNonNull(getActivity()).finish();
        }
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        //inflating menu_main.
        inflater.inflate(R.menu.menu_main, menu);

        //hide addpost icon from this fragment
        menu.findItem(R.id.action_add_post).setVisible(false);

        //searchview
        MenuItem item = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);

        // search listener
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                //called when user press search button from keyboard
                //if search query is not empty then search

                if(!TextUtils.isEmpty(s.trim())){
                    // search text contains text, search it.
                    searchUsers(s);

                }
                else{
                    // search text empty, get all users
                    getAllUsers();

                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                //called when user press any single letter

                if(!TextUtils.isEmpty(s.trim())){
                    // search text contains text, search it.
                    searchUsers(s);

                }
                else{
                    // search text empty, get all users
                    getAllUsers();

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
            checkUserStatus();
        }

        else if(id == R.id.action_settings){

            startActivity(new Intent(getActivity(),SettingsActivity.class));
        }

        return super.onOptionsItemSelected(item);
    }

}

