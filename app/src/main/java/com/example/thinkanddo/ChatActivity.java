package com.example.thinkanddo;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;

import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.thinkanddo.adapters.AdapterChat;
import com.example.thinkanddo.models.ModelChat;
import com.example.thinkanddo.models.ModelUsers;
import com.example.thinkanddo.notifications.Data;
import com.example.thinkanddo.notifications.Sender;
import com.example.thinkanddo.notifications.Token;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class ChatActivity extends AppCompatActivity {

    Toolbar toolbar;
    RecyclerView recyclerView;
    ImageButton sendBtn, attachBtn;
    ImageView profileIv;
    TextView nameTv, userStatusTv;
    EditText messageEt;
    FirebaseAuth firebaseAuth;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference usersDbRef;
    String hisUid;
    String myUid;
    String hisImage;


    private RequestQueue requestQueue;
    private boolean notify= false;


    //permission constants
    private static final int CAMERA_REQUEST_CODE=100;
    private static final int STORAGE_REQUEST_CODE=200;

    //image pick constant
    private static final int VIDEO_PICK_CAMERA_CODE=350;
    private static final int IMAGE_PICK_CAMERA_CODE=300;
    private static final int IMAGE_PICK_GALLERY_CODE=400;
    /////

    //PERMISSION ARRAY

    String[] cameraPermissions;
    String[] storagePermissions;

    Uri image_rui=null;
    Uri video_rui=null;

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
        attachBtn = findViewById(R.id.attachBtn);
        profileIv = findViewById(R.id.profileIv);
        nameTv = findViewById(R.id.nameTv);
        userStatusTv = findViewById(R.id.userStatusTv);
        messageEt = findViewById(R.id.messageEt);
        firebaseAuth=firebaseAuth.getInstance();

        cameraPermissions=new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions=new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        requestQueue = Volley.newRequestQueue(getApplicationContext());

        Context context;
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        //create api service

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
                        String onlineStatus = "" + ds.child("onlineStatus").getValue();
                        if(onlineStatus.equals("online")){
                            userStatusTv.setText(onlineStatus);
                        }
                        else{
                            Calendar cal = Calendar.getInstance(Locale.ENGLISH);

                            cal.setTimeInMillis(Long.parseLong(onlineStatus));

                            String dateTime = DateFormat.format("dd/MM/yyyy hh:mm aa",cal).toString();
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
        //click btn to import image
        attachBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //show Image
                showImagePickDialog();

            }
        });

        //cpoy the code of pick imag eand handle permissions from add post activity


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


    private void showImagePickDialog() {
    }
    private void pickFromGallery() {

        Intent intent= new Intent(Intent.ACTION_PICK);
        intent.setType("image/* video/*");
        startActivityForResult(intent,IMAGE_PICK_GALLERY_CODE);
    }

    private void pickFromImage() {
        //INTENT TO PICK AN IMAG EFROM CAMERA
        ContentValues cv=new ContentValues();
        cv.put(MediaStore.Images.Media.TITLE,"Temp Pick");
        cv.put(MediaStore.Images.Media.DESCRIPTION,"Temp Descr");
        image_rui=getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,cv);

        Intent intent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, image_rui);
        startActivityForResult(intent, IMAGE_PICK_CAMERA_CODE);
    }

    private void pickFromVideo() {
        //INTENT TO PICK AN VIDEO FROM CAMERA
        ContentValues cv=new ContentValues();
        cv.put(MediaStore.Images.Media.TITLE,"Temp Pick");
        cv.put(MediaStore.Images.Media.DESCRIPTION,"Temp Descr");
        video_rui=getContentResolver().insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,cv);

        Intent intent=new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, video_rui);
        startActivityForResult(intent, VIDEO_PICK_CAMERA_CODE);
    }

    private boolean checkStoragePermission(){
        boolean result= ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)==(PackageManager.PERMISSION_GRANTED);
        return result;
    }

    private void requestStoragePermission(){
        //request runtime storage
        ActivityCompat.requestPermissions(this, storagePermissions, STORAGE_REQUEST_CODE);
    }

    private boolean checkCameraPermission(){
        boolean result= ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA)==(PackageManager.PERMISSION_GRANTED);
        boolean result1= ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)==(PackageManager.PERMISSION_GRANTED);
        return result && result1;
    }

    private void requestCameraPermission(){
        //request runtime storage
        ActivityCompat.requestPermissions(this, cameraPermissions, CAMERA_REQUEST_CODE);
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
        hashMap.put("type","text");
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

    private void sendImageMessage(Uri image_rui) throws IOException {
        notify=true;

        //progress dialog
        final ProgressDialog progressDialog=new ProgressDialog(this);
        progressDialog.setMessage("Sending Image");
        progressDialog.show();
        final String timeStamp=""+System.currentTimeMillis();
        String fileNameAndPath="ChatImages,"+"post_"+timeStamp;

        //chat nodes will be created that will contain all images sent via chat

        //get bitmap from image uri
        Bitmap bitmap=MediaStore.Images.Media.getBitmap(this.getContentResolver(),image_rui);
        ByteArrayOutputStream baos=new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG,100,baos);
        byte[] data=baos.toByteArray();// convert image to byte
        StorageReference ref= FirebaseStorage.getInstance().getReference().child(fileNameAndPath);
        ref.putBytes(data).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                //image upload
                progressDialog.dismiss();
                //get url of upload image
                Task<Uri> uriTask=taskSnapshot.getStorage().getDownloadUrl();
                while(!uriTask.isSuccessful());
                String downloadUri =uriTask.getResult().toString();

                if(uriTask.isSuccessful()){
                    //add image uri and other info to database
                    DatabaseReference databaseReference =FirebaseDatabase.getInstance().getReference();
                    //setup required data
                    HashMap<String, Object>hashMap=new HashMap<>();
                    hashMap.put("sender",myUid);
                    hashMap.put("receiver",hisUid);
                    hashMap.put("message",downloadUri);
                    hashMap.put("timestamp",timeStamp);
                    hashMap.put("type","image");
                    hashMap.put("isSeen",false);

                    //put this  data to firebase
                    databaseReference.child("Chats").push().setValue(hashMap);
                    //send notification
                    DatabaseReference database=FirebaseDatabase.getInstance().getReference("Users").child(myUid);
                    database.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            ModelUsers user=dataSnapshot.getValue(ModelUsers.class);
                            if(notify){
                                sentNotification(hisUid,user.getName(),"Sent you a photo");
                            }
                            notify=false;
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                        }
                    });
                }

            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();

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
                   Data data= new Data(myUid, name+": "+message,"New Message",hisUid, R.drawable.ic_default);  //R.drawable.ic_default_img;
                   Sender sender=new Sender(data,token.getToken());
                   try{
                       JSONObject senderJsonObj = new JSONObject(new Gson().toJson(sender));
                       JsonObjectRequest jsonObjectRequest = new JsonObjectRequest("https://fcm.googleapis.com/fcm/send", senderJsonObj,
                               new Response.Listener<JSONObject>() {
                                   @Override
                                   public void onResponse(JSONObject response) {
                                       Log.d("JSON_RESPONSE", "onResponse: "+response.toString());

                                   }
                               }, new Response.ErrorListener() {
                           @Override
                           public void onErrorResponse(VolleyError error) {
                               Log.d("JSON_RESPONSE", "onResponse: "+error.toString());

                           }
                       }){
                           @Override
                           public Map<String, String> getHeaders() throws AuthFailureError {

                               Map<String, String> headers = new HashMap<>();
                               headers.put("Content-Type", "application/json");
                               headers.put("Authorization", "key=AAAAMMIph68:APA91bFJrgbfCwd6gELs7d0ffLALkdvST16p3u4xEpQBQ0J0hmlCdDR6u5GQCu9V1hdL8CPsL5HiyH_pD9Zua7_ZKsFPOrLG-HqAvkbWv_-UIviIAIb7U6XsmLN4iJl9Acq6eo9Px757");

                               return headers;
                           }
                       };

                       requestQueue.add(jsonObjectRequest);

                   } catch (JSONException e){
                       e.printStackTrace();
                   }
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch(requestCode){
            case CAMERA_REQUEST_CODE:{
                if(grantResults.length>0) {
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted && storageAccepted) {
                        pickFromImage();
                        pickFromVideo();
                    }
                    else {
                        Toast.makeText(this, "Camera & Storage Both permissions are necessary...", Toast.LENGTH_SHORT).show();
                    }
                }
                else{

                }
            }
            break;
            case STORAGE_REQUEST_CODE:{
                if(grantResults.length>0) {
                    boolean storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (storageAccepted) {
                        pickFromGallery();
                    }
                    else {
                        Toast.makeText(this, " Storage  permissions necessary...", Toast.LENGTH_SHORT).show();
                    }
                }
                else{

                }

            }
            break;
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(resultCode==RESULT_OK) {

            if(requestCode==IMAGE_PICK_GALLERY_CODE){
                image_rui=data.getData();

                video_rui=data.getData();

                if(image_rui.toString().contains("image"))
                {

                   // imageIv.setImageURI(image_rui);

                 //    pVideoVv.setVisibility(View.GONE);
                 //   imageIv.setVisibility(View.VISIBLE);
                    sendImageMessage(image_rui);


                }
                else if(video_rui.toString().contains("video"))
                {

                    //VideoDisplay(video_rui);
                    sendImageMessage(image_rui);


                }
            }
            else if(requestCode==IMAGE_PICK_CAMERA_CODE){

               // imageIv.setImageURI(image_rui);
              //  pVideoVv.setVisibility(View.GONE);
              //  imageIv.setVisibility(View.VISIBLE);

            }
            else if(requestCode==VIDEO_PICK_CAMERA_CODE){

              //  VideoDisplay(video_rui);

            }

        }

        super.onActivityResult(requestCode, resultCode, data);
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
