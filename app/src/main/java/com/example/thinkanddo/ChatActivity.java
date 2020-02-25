package com.example.thinkanddo;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.thinkanddo.adapters.AdapterChat;
import com.example.thinkanddo.models.ModelChat;
import com.example.thinkanddo.models.ModelUsers;
import com.example.thinkanddo.notifications.APIService;
import com.example.thinkanddo.notifications.Client;
import com.example.thinkanddo.notifications.Data;
import com.example.thinkanddo.notifications.Response;
import com.example.thinkanddo.notifications.Sender;
import com.example.thinkanddo.notifications.Token;
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
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;

public class ChatActivity extends AppCompatActivity {

    Toolbar toolbar;
    RecyclerView recyclerView;
    ImageButton sendBtn;
    ImageView profileIv;
    TextView nameTv, userStatusTv;
    EditText messageEt;
    FirebaseAuth firebaseAuth;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference usersDbRef;
    String hisUid;
    String myUid;
    String hisImage;


    APIService apiService;
    boolean notify= false;

    //for checking if the uer has seen message or not.

    ValueEventListener seenListener;
    DatabaseReference userRefForSeen;

    List<ModelChat> chatList;
    AdapterChat adapterChat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("");
        recyclerView = findViewById(R.id.chat_recyclerView);
        sendBtn = findViewById(R.id.sendBtn);
        profileIv = findViewById(R.id.profileIv);
        nameTv = findViewById(R.id.nameTv);
        userStatusTv = findViewById(R.id.userStatusTv);
        messageEt = findViewById(R.id.messageEt);
        firebaseAuth=firebaseAuth.getInstance();
        Context context;
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        //create api service
        apiService= Client.getRetrofit("https://fcm.googleapis.com/").create(APIService.class);

        /*on Clicking user from users list we have passed that uid using intent
        So get the uid here to get user image, name and start chat with that user.
         */

        Intent intent = getIntent();

        hisUid = intent.getStringExtra("hisUid");

        firebaseDatabase = firebaseDatabase.getInstance();
        usersDbRef = firebaseDatabase.getReference("Users");

        //search user to get that users info
        Query userQuery = usersDbRef.orderByChild("uid").equalTo(hisUid);

        //get user picture and name
        userQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                //check until required info is received.
                for(DataSnapshot ds:dataSnapshot.getChildren()){

                    //get data
                    String name ="" + ds.child("name").getValue();
                    hisImage ="" + ds.child("image").getValue();
                    String typingStatus ="" + ds.child("typingTo").getValue();

                    if (typingStatus.equals(myUid)){
                        userStatusTv.setText("typing...");
                    }
                    else{
                        String onineStatus = "" + ds.child("onlineStatus").getValue();
                        if(onineStatus.equals("online")){
                            userStatusTv.setText(onineStatus);
                        }
                        else{
                            Calendar cal = Calendar.getInstance(Locale.ENGLISH);

                            cal.setTimeInMillis(Long.parseLong(onineStatus));

                            String dateTime = DateFormat.format("dd/mm/yyyy hh:mm aa",cal).toString();
                            userStatusTv.setText("Last seen at: "+ dateTime);
                        }
                    }

                    //set data
                    nameTv.setText(name);
                    try{


                        Picasso.get().load(hisImage).placeholder(R.drawable.ic_default).into(profileIv);
                    }
                    catch(Exception e){

                        Picasso.get().load(R.drawable.ic_default).into(profileIv);
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        sendBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                 notify=true;
                // get text from edittext

                String message = messageEt.getText().toString().trim();

                if(TextUtils.isEmpty(message)){

                    Toast.makeText(ChatActivity.this,"Cannot send empty mess..",Toast.LENGTH_LONG).show();
                }
                else{

                    sendMessage(message);
                }
                //reset edittext after sending message
                messageEt.setText("");
            }

        });

        messageEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.toString().trim().length() == 0){
                    checkTypingStatus("noOne");
                }
                else {
                    checkTypingStatus(hisUid);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        messageEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.toString().trim().length() == 0){
                    checkTypingStatus("noOne");
                }
                else {
                    checkTypingStatus(hisUid);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        readMessages();
        seenMessage();
    }

    private void seenMessage() {

        userRefForSeen = FirebaseDatabase.getInstance().getReference("Chats");
        seenListener = userRefForSeen.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds: dataSnapshot.getChildren()){

                    ModelChat chat = ds.getValue(ModelChat.class);

                    if(chat.getReceiver().equals(myUid)&&chat.getSender().equals(hisUid)){

                        HashMap<String, Object> hasSeenHashMap = new HashMap<>();
                        hasSeenHashMap.put("isSeen", true);
                        ds.getRef().updateChildren(hasSeenHashMap);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void readMessages() {

        chatList = new ArrayList<>();
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Chats");
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                chatList.clear();
                for(DataSnapshot ds: dataSnapshot.getChildren()){

                    ModelChat chat = ds.getValue(ModelChat.class);
                    if(chat.getReceiver().equals(myUid) && chat.getSender().equals(hisUid)||
                            chat.getReceiver().equals(hisUid) && chat.getSender().equals(myUid)){
                        chatList.add(chat);
                    }

                    adapterChat = new AdapterChat(ChatActivity.this,chatList,hisImage);
                    adapterChat.notifyDataSetChanged();

                    //set Adapter to recyclerview
                    recyclerView.setAdapter(adapterChat);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void sendMessage(final String message) {
        /* "Chats" node will be created that will contains all chats
        Whenever user sends message it will create new child in "Chats" node and that child will contain
        sender: UID of sender
        receiver: UID of receiver
        message: the actual message
         */

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

        String timestamp = String.valueOf(System.currentTimeMillis());

        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("sender",myUid);
        hashMap.put("receiver",hisUid);
        hashMap.put("message",message);
        hashMap.put("timestamp",timestamp);
        hashMap.put("isSeen",false);
        databaseReference.child("Chats").push().setValue(hashMap);


       final DatabaseReference database=FirebaseDatabase.getInstance().getReference("Users").child(myUid);
        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ModelUsers user= dataSnapshot.getValue(ModelUsers.class);

                if(notify){
                    sentNotification(hisUid,user.getName(), message);
                }
                notify=false;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        // create ChatList node/child in firebase database.
        final DatabaseReference chatRef1=FirebaseDatabase.getInstance().getReference("Chatlist")
                .child(myUid)
                .child(hisUid);

        chatRef1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!dataSnapshot.exists()){
                    chatRef1.child("id").setValue(hisUid);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        final DatabaseReference chatRef2=FirebaseDatabase.getInstance().getReference("Chatlist")
                .child(hisUid)
                .child(myUid);

        chatRef2.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!dataSnapshot.exists()){
                    chatRef2.child("id").setValue(myUid);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



    }

    private void sentNotification(final String hisUid, final String name, final String message) {
        DatabaseReference allTokens= FirebaseDatabase.getInstance().getReference("Tokens");
        Query query=allTokens.orderByKey().equalTo(hisUid);
        query.addValueEventListener(new ValueEventListener(){
           @Override
           public void onDataChange(@NonNull DataSnapshot dataSnapshot){
               for(DataSnapshot ds: dataSnapshot.getChildren()){
                   Token token=ds.getValue(Token.class);
                   Data data= new Data(myUid, name+":"+message,"New Message",hisUid, R.drawable.ic_default);  //R.drawable.ic_default_img;
                   Sender sender=new Sender(data,token.getToken());
                   apiService.sendNotification(sender)
                           .enqueue(new Callback<Response>() {
                               @Override
                               public void onResponse(Call<Response> call, retrofit2.Response<Response> response) {
                                 Toast.makeText(ChatActivity.this,""+response.message(),Toast.LENGTH_SHORT).show();
                               }

                               @Override
                               public void onFailure(Call<Response> call, Throwable t) {

                               }
                           });
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
            myUid = user.getUid();

        }else{
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }

    private void checkOnlineStatus(String status){
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Users").child(myUid);
        HashMap<String, Object> hashMap =new HashMap<>();
        hashMap.put("onlineStatus", status);
        dbRef.updateChildren(hashMap);
    }

    private void checkTypingStatus(String typing){
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Users").child(myUid);
        HashMap<String, Object> hashMap =new HashMap<>();
        hashMap.put("typingTo", typing);
        dbRef.updateChildren(hashMap);
    }

    @Override
    protected void onStart() {
        checkUserStatus();
        checkOnlineStatus("online");
        checkTypingStatus("noOne");
        super.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();

        String timestamp = String.valueOf(System.currentTimeMillis());

        checkOnlineStatus(timestamp);

        userRefForSeen.removeEventListener(seenListener);
    }

    @Override
    protected void onResume() {
        checkOnlineStatus("online");
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main,menu);

        menu.findItem(R.id.action_search).setVisible(false);
        menu.findItem(R.id.action_add_post).setVisible(false);
        return super.onCreateOptionsMenu(menu);
        //x+y=1;
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
}
