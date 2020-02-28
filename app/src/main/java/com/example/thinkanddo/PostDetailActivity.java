package com.example.thinkanddo;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v4.view.PointerIconCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.example.thinkanddo.adapters.AdapterComments;
import com.example.thinkanddo.models.ModelComment;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class PostDetailActivity extends AppCompatActivity {

    String myUid, hisUid, myEmail, myName, myDp, pImage,
    postId, pLikes, hisDp, hisName;

    boolean mProcessComment = false;

    boolean mProcessLike=false;
    ProgressDialog pd;

    ImageView uPictureIv, pImageIv;
    TextView uNameTv, pTimeTiv, pTitleTv, pDescriptionTv, pLikesTv,pCommentsTv;
    ImageButton moreBtn;
    Button likeBtn, shareBtn;
    LinearLayout profileLayout;
    RecyclerView recyclerView;

    List<ModelComment> commentList;
    AdapterComments adapterComments;

    EditText commentEt;
    ImageButton sendBtn;
    ImageView cAvatarIv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Post Detail");
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        postId =intent.getStringExtra("postId");

        uPictureIv = findViewById(R.id.uPictureIv);
        pImageIv = findViewById(R.id.pImageIv);
        uNameTv = findViewById(R.id.uNameTv);
        pTimeTiv = findViewById(R.id.pTimeTv);
        pTitleTv = findViewById(R.id.pTitleTv);
        pDescriptionTv =findViewById(R.id.pDescriptionTv);
        pLikesTv = findViewById(R.id.pLikesTv);
        moreBtn = findViewById(R.id.moreBtn);
        likeBtn = findViewById(R.id.likeBtn);
        shareBtn = findViewById(R.id.shareBtn);
        profileLayout = findViewById(R.id.profileLayout);
        pCommentsTv = findViewById(R.id.pCommentsTv);
        recyclerView = findViewById(R.id.recyclerView);


        commentEt = findViewById(R.id.commentEt);
        sendBtn = findViewById(R.id.sendBtn);
        cAvatarIv = findViewById(R.id.cAvatarIv);

        loadPostInfo();

        checkUserStatus();

        loadUserInfo();

        setLikes();


        actionBar.setSubtitle("SignedIn as: "+myEmail);

        loadComments();
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postComment();
            }


        });

        likeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                likePost();
            }
        });

        moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMoreOptions();

            }
        });

        //share button click  handle
        shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String pTitle=pTitleTv.getText().toString().trim();
                String pDescription=pDescriptionTv.getText().toString().trim();

                BitmapDrawable bitmapDrawable =(BitmapDrawable)pImageIv.getDrawable();
                if(bitmapDrawable==null){
                    //post without image
                    shareTextOnly(pTitle, pDescription);
                }
                else{
                    //post with image
                    // convert image to bitmap
                    Bitmap bitmap =bitmapDrawable.getBitmap();
                    shareImageAndText(pTitle,pDescription,bitmap);
                }

            }
        });
    }
    private void shareTextOnly(String pTitle,String pDescription) {
        //concatenate title and description to share
        String shareBody= pTitle+"\n"+pDescription;
        //share intent
        Intent sIntent=new Intent(Intent.ACTION_SEND);
        sIntent.setType("text/plain");
        sIntent.putExtra(Intent.EXTRA_SUBJECT,"Subject Here"); // in case you share via email
        sIntent.putExtra(Intent.EXTRA_TEXT,shareBody);//text to share
        startActivity(Intent.createChooser(sIntent,"Share Via")); // message to show in share dialogue
    }

    private void shareImageAndText(String pTitle,String pDescription,Bitmap bitmap) {
        //concatenate title description to share
        String shareBody= pTitle+"\n"+pDescription;

        //first we will save this image in cache, get the saved image in uri
        Uri uri= saveImageToShare(bitmap);

        //share intent
        Intent sIntent = new Intent(Intent.ACTION_SEND);
        sIntent.putExtra(Intent.EXTRA_STREAM,uri);
        sIntent.putExtra(Intent.EXTRA_TEXT,shareBody);
        sIntent.putExtra(Intent.EXTRA_SUBJECT,"Subject Here");
        sIntent.setType("images/png");
        startActivity(Intent.createChooser(sIntent,"Share Via"));

    }

    private Uri saveImageToShare(Bitmap bitmap) {
        File imageFolder= new File(getCacheDir(),"images");
        Uri uri=null;
        try{
            imageFolder.mkdirs(); //create if not exists
            File file = new File(imageFolder,"shared_image.png");

            FileOutputStream stream =new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG,90,stream);
            stream.flush();
            stream.close();
            uri= FileProvider.getUriForFile(this,"com.example.thinkanddo.fileprovider",file);

        }
        catch (Exception e){
            Toast.makeText(this,""+e.getMessage(),Toast.LENGTH_SHORT).show();

        }
        return uri;
    }



    private void loadComments() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());

        recyclerView.setLayoutManager(layoutManager);

        commentList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts").child(postId).child("Comments");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                commentList.clear();
                for(DataSnapshot ds:dataSnapshot.getChildren()){
                    ModelComment modelComment=ds.getValue(ModelComment.class);
                    commentList.add(modelComment);
                    adapterComments = new AdapterComments(getApplicationContext(), commentList);
                    recyclerView.setAdapter(adapterComments);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void showMoreOptions() {
        PopupMenu popupMenu = new PopupMenu(this, moreBtn, Gravity.END);

        // show delete option in only post of currently signed in user.
        if(hisUid.equals(myUid)) {
            //add items in menu
            popupMenu.getMenu().add(Menu.NONE, 0, 0, "Delete");
            popupMenu.getMenu().add(Menu.NONE, 1, 0, "Edit");
        }

        // item click listner
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                if(id==0){
                    // delete is clicked
                    beginDelete();
                }
                else if(id==1){
                    // edit is clicked
                    // start AddPostActivity with key "editpost" and the id of the post clicked

                    Intent intent = new Intent(PostDetailActivity.this, AddPostActivity.class);
                    intent.putExtra("key","editPost");
                    intent.putExtra("editPostId", postId);
                    startActivity(intent);
                }
                return false;
            }
        });

        // show menu
        popupMenu.show();
    }

    private void beginDelete() {
        if(pImage.equals("noImage")){
            // Post without image.
            deleteWithoutImage();

        }
        else{
            // Post with image
            deleteWithImage();
        }
    }

    private void deleteWithImage() {
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Deleting... ");

        /*Steps:
        1) Delete image using url.
        2) Delete from database using post Id;
         */


        StorageReference picRef = FirebaseStorage.getInstance().getReferenceFromUrl(pImage);

        picRef.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        // image deleted, now delete database
                        Query fQuery = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(postId);
                        fQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for(DataSnapshot ds: dataSnapshot.getChildren()){

                                    ds.getRef().removeValue();  // remove values from firebase where pid matches
                                }
                                // deleted
                                Toast.makeText(PostDetailActivity.this,"Deleted Successfully",Toast.LENGTH_SHORT).show();
                                pd.dismiss();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // failed, can't go further

                pd.dismiss();
                Toast.makeText(PostDetailActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteWithoutImage() {
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Deleting... ");

        // image deleted, now delete database
        Query fQuery = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(postId);
        fQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds: dataSnapshot.getChildren()){

                    ds.getRef().removeValue();  // remove values from firebase where pid matches
                }
                // deleted
                Toast.makeText(PostDetailActivity.this,"Deleted Succesfully",Toast.LENGTH_SHORT).show();
                pd.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void setLikes() {
        final DatabaseReference likesRef = FirebaseDatabase.getInstance().getReference().child("Likes");

        likesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.child(postId).hasChild(myUid)){

                    //user has liked this post
                    /* To indicate that the post is liked by this signed user
                    Change drawable left icon of like button
                    Change text of like button from "like" to "liked".
                     */

                    likeBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_liked,0,0,0);
                    likeBtn.setText("Liked");
                }
                else{
                    // user has not liked this post

                    likeBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_like_black,0,0,0);
                    likeBtn.setText("Like");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void likePost() {
        // get number of likes for the post, whose like button clicked
        // if currently signed in user has not liked it before
        // increase value by 1, otherwise decrease value by 1

        mProcessLike = true;

        final DatabaseReference likesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
        final DatabaseReference postsRef = FirebaseDatabase.getInstance().getReference().child("Posts");

        // get id of the post clicked
        likesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(mProcessLike){

                    if(dataSnapshot.child(postId).hasChild(myUid)){

                        // already liked, so remove like

                        postsRef.child(postId).child("pLikes").setValue(""+(Integer.parseInt(pLikes)-1));
                        likesRef.child(postId).child(myUid).removeValue();
                        mProcessLike = false;

                    }
                    else{

                        // not Liked, like it
                        postsRef.child(postId).child("pLikes").setValue(""+(Integer.parseInt(pLikes)+1));
                        likesRef.child(postId).child(myUid).setValue("Liked"); // set any value
                        mProcessLike = false;

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void postComment() {
        pd = new ProgressDialog(this);
        pd.setMessage("Adding comment...");

        final String comment = commentEt.getText().toString().trim();

        if(TextUtils.isEmpty(comment)){
            Toast.makeText(this,"Comment is empty...", Toast.LENGTH_SHORT).show();
            return;
        }
        String timeStamp = String.valueOf(System.currentTimeMillis());

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts").child(postId).child("Comments");

        HashMap<String , Object> hashMap = new HashMap<>();
        hashMap.put("cId",timeStamp);
        hashMap.put("comment",comment);
        hashMap.put("timestamp",timeStamp);
        hashMap.put("uid",myUid);
        hashMap.put("uEmail",myEmail);
        hashMap.put("uDp",myDp);
        hashMap.put("uName",myName);

        ref.child(timeStamp).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                pd.dismiss();
                Toast.makeText(PostDetailActivity.this , "Comment Added...",Toast.LENGTH_SHORT).show();
                commentEt.setText("");
                updateCommentCount();
            }
        })
        .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pd.dismiss();
                Toast.makeText(PostDetailActivity.this , ""+e.getMessage(),Toast.LENGTH_SHORT).show();

            }
        });

        likeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                likePost();
            }
        });
    }


    private void updateCommentCount() {

        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts").child(postId);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(mProcessComment){
                    String comments = ""+dataSnapshot.child("pComments").getValue();
                    int newCommentVal = Integer.parseInt(comments) + 1;
                    ref.child("pComments").setValue(""+newCommentVal);
                    mProcessComment=false;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void loadUserInfo() {
        Query myRef = FirebaseDatabase.getInstance().getReference("Users");
        myRef.orderByChild("uid").equalTo(myUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds: dataSnapshot.getChildren()){
                    myName = ""+ds.child("name").getValue();
                    myDp = ""+ds.child("image").getValue();

                    try{
                        Picasso.get().load(myDp).placeholder(R.drawable.ic_default).into(cAvatarIv);
                    }catch (Exception e){
                        Picasso.get().load(R.drawable.ic_default).into(cAvatarIv);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void loadPostInfo() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        Query query = ref.orderByChild("pId").equalTo(postId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds: dataSnapshot.getChildren()){
                    String pTitle = ""+ds.child("pTitle").getValue();
                    String pDescr = ""+ds.child("pDescr").getValue();
                    pLikes = ""+ds.child("pLikes").getValue();
                    String pTimeStamp = ""+ds.child("pTime").getValue();
                    pImage = ""+ds.child("pImage").getValue();
                    hisDp = ""+ds.child("uDp").getValue();
                    hisUid = ""+ds.child("uid").getValue();
                    String uEmail = ""+ds.child("uEmail").getValue();
                    hisName = ""+ds.child("uName").getValue();
                    String commentCount = ""+ds.child("pComments").getValue();

                    Calendar calender = Calendar.getInstance(Locale.getDefault());
                    calender.setTimeInMillis(Long.parseLong(pTimeStamp));
                    String pTime = DateFormat.format("dd/MM/yyyy hh:mm aa", calender).toString();

                    pTitleTv.setText(pTitle);
                    pDescriptionTv.setText(pDescr);
                    pLikesTv.setText(pLikes+ "Likes");
                    pTimeTiv.setText(pTime);
                    pCommentsTv.setText(commentCount+" Comments");

                    uNameTv.setText(hisName);

                    if(pImage.equals("noImage")){

                        // hide imageview

                        pImageIv.setVisibility(View.GONE);
                    }
                    else{
                        // show imageview

                        pImageIv.setVisibility(View.VISIBLE);

                        try
                        {
                            Picasso.get().load(pImage).into(pImageIv);

                        }
                        catch (Exception ex){


                        }

                    }

                    // set User name in comment part
                    try{
                        Picasso.get().load(hisDp).placeholder(R.drawable.ic_default).into(uPictureIv);
                    }catch (Exception e){
                        Picasso.get().load(R.drawable.ic_default).into(uPictureIv);

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void checkUserStatus(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user!= null){
            myEmail = user.getEmail();
            myUid = user.getUid();
        }else {
            startActivity(new Intent(this,MainActivity.class));
            finish();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main,menu);
        menu.findItem(R.id.action_add_post).setVisible(false);
        menu.findItem(R.id.action_search).setVisible(false);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.action_logout){
            FirebaseAuth.getInstance().signOut();
            checkUserStatus();
        }
        return super.onOptionsItemSelected(item);
    }
}
