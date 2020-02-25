package com.example.thinkanddo;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.example.thinkanddo.adapters.AdapterChatlist;
import com.example.thinkanddo.models.ModelChat;
import com.example.thinkanddo.models.ModelChatlist;
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


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatListFragment extends Fragment {
    FirebaseAuth firebaseAuth;
    RecyclerView recyclerView;
    List<ModelChatlist>chatlistList;
    List<ModelUsers> usersList;
    DatabaseReference reference;
    FirebaseUser currentUser;
    AdapterChatlist adapterChatlist;



    public ChatListFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_chat_list, container, false);

        firebaseAuth = FirebaseAuth.getInstance();
        currentUser=FirebaseAuth.getInstance().getCurrentUser();

        recyclerView=view.findViewById(R.id.recyclerView);
        chatlistList=new ArrayList<>();
        reference= FirebaseDatabase.getInstance().getReference("Chatlist").child(currentUser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                chatlistList.clear();
                for(DataSnapshot ds:dataSnapshot.getChildren()){
                    ModelChatlist chatlist=ds.getValue(ModelChatlist.class);
                    chatlistList.add(chatlist);
                }
                loadChats();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        return view;
    }

    private void loadChats() {
        usersList=new ArrayList<>();
        reference=FirebaseDatabase.getInstance().getReference("Users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                usersList.clear();
                for(DataSnapshot ds: dataSnapshot.getChildren()) {
                    ModelUsers users = ds.getValue(ModelUsers.class);
                    for (ModelChatlist chatlist : chatlistList) {
                        if (users.getUid() != null && users.getUid().equals(chatlist.getId())) {
                            usersList.add(users);
                            break;
                        }
                    }
                    //adapter
                    adapterChatlist = new AdapterChatlist(getContext(),usersList);
                    //set adapter
                    recyclerView.setAdapter(adapterChatlist);
                    // set last message
                    for (int i = 0; i < usersList.size(); i++) {
                           lastMessage(usersList.get(i).getUid());
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    private void lastMessage(final String userId){
        DatabaseReference reference=FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String theLastMessage="default";
                for(DataSnapshot ds: dataSnapshot.getChildren()){
                    ModelChat chat=ds.getValue(ModelChat.class);
                    if(chat==null){
                        continue;
                    }
                    String sender = chat.getSender();
                    String receiver=chat.getReceiver();
                    if(sender==null|| receiver==null){
                        continue;
                    }
                    if(chat.getReceiver().equals(currentUser.getUid())&& chat.getSender()
                            .equals(userId)||chat.getReceiver().equals(userId)&&
                    chat.getSender().equals(currentUser.getUid())){
                        theLastMessage=chat.getMessage();
                    }
                }
                adapterChatlist.setLastMessageMap(userId,theLastMessage);
                adapterChatlist.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

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
        //inflating menu_main.
        inflater.inflate(R.menu.menu_main, menu);

        //hide addpost icon from this fragment
        menu.findItem(R.id.action_add_post).setVisible(false);
        //searchview
        super.onCreateOptionsMenu(menu, inflater);
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.action_logout){
            firebaseAuth.signOut();
            checkUserStatus();
        }

        return super.onOptionsItemSelected(item);
    }

    private void checkUserStatus(){
        FirebaseUser user =firebaseAuth.getCurrentUser();

        if(user!=null){
            //mprofileTv.setText(user.getEmail());

        }else{
            startActivity(new Intent(getActivity(), MainActivity.class));
            getActivity().finish();
        }
    }

}
















/*   private void searchUsers(final String query) {

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
    }*/

