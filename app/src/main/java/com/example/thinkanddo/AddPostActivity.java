package com.example.thinkanddo;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
//import android.support.v7.widget.ActivityChooserModel;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

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
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;

public class AddPostActivity extends AppCompatActivity {

    FirebaseAuth firebaseAuth;
    DatabaseReference userDbRef;
    ActionBar actionBar;

    //permission constants
    private static final int CAMERA_REQUEST_CODE=100;
    private static final int STORAGE_REQUEST_CODE=200;

    //image pick constant
    private static final int IMAGE_PICK_CAMERA_CODE=300;
    private static final int IMAGE_PICK_GALLERY_CODE=400;
    /////

    //PERMISSION ARRAY

        String[] cameraPermissions;
        String[] storagePermissions;

    //views

    EditText titleEt, descriptionEt;
    ImageView imageIv;
    Button  uploadBtn;

    //user info
    String name, email, uid, dp;

    // info of post to be edited
    String editTitle, editDescription, editImage;

    Uri image_rui=null;

    //progress bsr
    ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);

        actionBar = getSupportActionBar();
        actionBar.setTitle("Add New Post");
        //enable back button in action bar
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        cameraPermissions=new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions=new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        pd=new ProgressDialog(this);

        firebaseAuth=FirebaseAuth.getInstance();
        checkUserStatus();


        //init views
        titleEt=findViewById(R.id.pTitleEt);
        descriptionEt=findViewById(R.id.pDescriptionEt);
        imageIv=findViewById(R.id.pImageIv);
        uploadBtn=findViewById(R.id.pUploadBtn);

        // get data through intent from adapterPost .

        Intent intent = getIntent();
        final String isUpdateKey = ""+intent.getStringExtra("key");
        final String editPostId  = ""+intent.getStringExtra("editPostId");

        // validate if we came here to update post i.e. came from AdapterPost

        if(isUpdateKey.equals("editPost")){

            // update

            actionBar.setTitle("Update Post");
            uploadBtn.setText("Update");
            loadPostData(editPostId);
        }
        else{

            //add

            actionBar.setTitle("Add new Post");
            uploadBtn.setText("Upload");
        }


        actionBar.setSubtitle(email);

        //get some info of current user to include in post
        userDbRef= FirebaseDatabase.getInstance().getReference("Users");

        Query query=userDbRef.orderByChild("email").equalTo(email);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds: dataSnapshot.getChildren()){
                    name=""+ds.child("name").getValue();
                    email=""+ds.child("email").getValue();
                    dp=""+ds.child("image").getValue();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



        //get image from camera gallery
        imageIv.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                showImagePickDialog();
            }
        });


        //upload button click listener'

        uploadBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                //get description from edit text
                String title=titleEt.getText().toString().trim();
                String description = descriptionEt.getText().toString().trim();

                if(TextUtils.isEmpty(title)){
                    Toast.makeText(AddPostActivity.this,"Enter Title....",Toast.LENGTH_SHORT).show();
                    return;
                }
                if(TextUtils.isEmpty(description)){
                    Toast.makeText(AddPostActivity.this,"Enter Description....",Toast.LENGTH_SHORT).show();
                    return;
                }

                if(isUpdateKey.equals("editPost")){
                   // actionBar.setTitle("Update Post");
                   // uploadBtn.setText("Update");

                    beginUpdate(title, description, editPostId);

                }
                else{

                    uploadData(title, description);
                }


            }
        });
    }

    private void beginUpdate(String title, String description, String editPostId) {

        pd.setMessage("Updating Post...");
        pd.show();

        if(!editImage.equals("noImage")){
            // with image

            updateWasWithImage(title,description,editPostId);
        }
        else if(imageIv.getDrawable()!=null){
            // with image
            updateWithNowImage(title,description, editPostId);

        }
        else{
            // without image
            updateWithoutImage(title,description, editPostId);
        }
    }

    private void updateWithoutImage(String title, String description, String editPostId) {

        HashMap<String, Object> hashMap = new HashMap<>();
        // put post info
        hashMap.put("uid", uid);
        hashMap.put("uName", name);
        hashMap.put("uEmail", email);
        hashMap.put("uDp", dp);
        hashMap.put("pTitle",title);
        hashMap.put("pDescr",description);
        hashMap.put("pImage","noImage");

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        ref.child(editPostId)
                .updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        pd.dismiss();
                        Toast.makeText(AddPostActivity.this,"Updated...",Toast.LENGTH_LONG).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pd.dismiss();
                Toast.makeText(AddPostActivity.this,""+e.getMessage(),Toast.LENGTH_LONG).show();
            }
        });

    }

    private void updateWithNowImage(final String title, final String description, final String editPostId) {

        String timestamp = String.valueOf(System.currentTimeMillis());
        String filePathAndName = "Posts/"+"post_"+timestamp;

        // get image from imageview
        Bitmap bitmap = ((BitmapDrawable)imageIv.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        // image compress
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] data = baos.toByteArray();

        StorageReference ref = FirebaseStorage.getInstance().getReference().child(filePathAndName);
        ref.putBytes(data).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                // image uploaded get its url
                Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                while(!uriTask.isSuccessful());

                String downloadUri = uriTask.getResult().toString();

                if(uriTask.isSuccessful()){

                    // url is recieved, upload to firebase database

                    HashMap<String, Object> hashMap = new HashMap<>();
                    // put post info
                    hashMap.put("uid", uid);
                    hashMap.put("uName", name);
                    hashMap.put("uEmail", email);
                    hashMap.put("uDp", dp);
                    hashMap.put("pTitle",title);
                    hashMap.put("pDescr",description);
                    hashMap.put("pImage",downloadUri);

                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
                    ref.child(editPostId)
                            .updateChildren(hashMap)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    pd.dismiss();
                                    Toast.makeText(AddPostActivity.this,"Updated...",Toast.LENGTH_LONG).show();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            pd.dismiss();
                            Toast.makeText(AddPostActivity.this,""+e.getMessage(),Toast.LENGTH_LONG).show();
                        }
                    });


                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                // image not uploaded
                pd.dismiss();
                Toast.makeText(AddPostActivity.this,""+e.getMessage(),Toast.LENGTH_LONG).show();
            }
        });


    }

    private void updateWasWithImage(final String title, final String description, final String editPostId) {

        //post is with image, delete previous image first...

        StorageReference mPictureRef = FirebaseStorage.getInstance().getReferenceFromUrl(editImage);

        mPictureRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

                // image deleted succesfully, upload new image.

                String timestamp = String.valueOf(System.currentTimeMillis());
                String filePathAndName = "Posts/"+"post_"+timestamp;

                // get image from imageview
                Bitmap bitmap = ((BitmapDrawable)imageIv.getDrawable()).getBitmap();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();

                // image compress
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
                byte[] data = baos.toByteArray();

                StorageReference ref = FirebaseStorage.getInstance().getReference().child(filePathAndName);
                ref.putBytes(data).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        // image uploaded get its url
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while(!uriTask.isSuccessful());

                        String downloadUri = uriTask.getResult().toString();

                        if(uriTask.isSuccessful()){

                            // url is recieved, upload to firebase database

                            HashMap<String, Object> hashMap = new HashMap<>();
                            // put post info
                            hashMap.put("uid", uid);
                            hashMap.put("uName", name);
                            hashMap.put("uEmail", email);
                            hashMap.put("uDp", dp);
                            hashMap.put("pTitle",title);
                            hashMap.put("pDescr",description);
                            hashMap.put("pImage",downloadUri);

                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
                            ref.child(editPostId)
                                    .updateChildren(hashMap)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            pd.dismiss();
                                            Toast.makeText(AddPostActivity.this,"Updated...",Toast.LENGTH_LONG).show();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    pd.dismiss();
                                    Toast.makeText(AddPostActivity.this,""+e.getMessage(),Toast.LENGTH_LONG).show();
                                }
                            });


                        }

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        // image not uploaded
                        pd.dismiss();
                        Toast.makeText(AddPostActivity.this,""+e.getMessage(),Toast.LENGTH_LONG).show();
                    }
                });


            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                pd.dismiss();
                Toast.makeText(AddPostActivity.this,""+e.getMessage(),Toast.LENGTH_LONG).show();
            }
        });

    }

    private void loadPostData(String editPostId) {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");

        // get detail of post using id of post
        Query fquery = reference.orderByChild("pId").equalTo(editPostId);

        fquery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds: dataSnapshot.getChildren()){

                    // get data
                    editTitle = ""+ds.child("pTitle").getValue();
                    editDescription = ""+ds.child("pDescr").getValue();
                    editImage = ""+ds.child("pImage").getValue();

                    // set Data

                    titleEt.setText(editTitle);
                    descriptionEt.setText(editDescription);


                    // set image

                    if(!editImage.equals("noImage")){

                        try{
                            Picasso.get().load(editImage).into(imageIv);
                        }
                        catch (Exception ex){}
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void uploadData(final String title, final String description) {
        pd.setMessage("Publishing Post...");
        pd.show();

        final String timeStamp= String.valueOf(System.currentTimeMillis());
        String filePathAndName="Posts/" + "post_" + timeStamp;

        if(imageIv.getDrawable()!=null){

            // get image from imageview
            Bitmap bitmap = ((BitmapDrawable)imageIv.getDrawable()).getBitmap();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            // image compress
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
            byte[] data = baos.toByteArray();

            // post with image
            StorageReference ref= FirebaseStorage.getInstance().getReference().child(filePathAndName);
            ref.putBytes(data)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Task<Uri> uriTask=taskSnapshot.getStorage().getDownloadUrl();
                            while (! uriTask.isSuccessful());
                            String downloadUri = uriTask.getResult().toString();
                            if(uriTask.isSuccessful()){
                                HashMap<Object, String> hashMap = new HashMap<>();
                                hashMap.put("uid",uid);
                                hashMap.put("uName",name);
                                hashMap.put("uEmail",email);
                                hashMap.put("uDp",dp);
                                hashMap.put("pId",timeStamp);
                                hashMap.put("pTitle",title);
                                hashMap.put("pDescr",description);
                                hashMap.put("pImage",downloadUri);
                                hashMap.put("pTime",timeStamp);
                                hashMap.put("pLikes","0");

                                //path to store post data
                                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
                                //
                                ref.child(timeStamp).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        pd.dismiss();
                                        Toast.makeText(AddPostActivity.this,"Post Published",Toast.LENGTH_SHORT).show();
                                        titleEt.setText("");
                                        descriptionEt.setText("");
                                        imageIv.setImageURI(null);
                                        image_rui=null;


                                    }
                                })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                pd.dismiss();
                                                Toast.makeText(AddPostActivity.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
                                            }
                                        });


                            }

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    pd.dismiss();
                    Toast.makeText(AddPostActivity.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
                }
            });


        }
        else{
            //post without image
            HashMap<Object, String> hashMap=new HashMap<>();
            hashMap.put("uid",uid);
            hashMap.put("uName",name);
            hashMap.put("uEmail",email);
            hashMap.put("uDp",dp);
            hashMap.put("pId",timeStamp);
            hashMap.put("pTitle",title);
            hashMap.put("pDescr",description);
            hashMap.put("pImage","noImage");
            hashMap.put("pTime",timeStamp);
            hashMap.put("pLikes","0");

            //path to store post data
            DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Posts");
            //
            ref.child(timeStamp).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    pd.dismiss();
                    Toast.makeText(AddPostActivity.this,"Post Published",Toast.LENGTH_SHORT).show();
                    titleEt.setText("");
                    descriptionEt.setText("");
                    imageIv.setImageURI(null);
                    image_rui=null;

                }
            })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            pd.dismiss();
                            Toast.makeText(AddPostActivity.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
                        }
                    });



        }


    }


    private void showImagePickDialog() {
        String[] options={"Camera","Gallery"};

        //dialog
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setTitle("Choose Image From");

        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(which==0){
                    //camera clicked
                    if(!checkCameraPermission()){
                        requestCameraPermission();
                    }
                    else{
                        pickFromCamera();
                    }
                }
                if(which==1){
                    // gallery clicked
                    if(!checkStoragePermission()){
                        requestStoragePermission();
                    }
                    else{
                        pickFromGallery();
                    }
                }

            }
        });
        //create show dialog
        builder.create().show();
    }

    private void pickFromGallery() {

        Intent intent= new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent,IMAGE_PICK_GALLERY_CODE);
    }

    private void pickFromCamera() {
        //INTENT TO PICK AN IMAG EFROM CAMERA
        ContentValues cv=new ContentValues();
        cv.put(MediaStore.Images.Media.TITLE,"Temp Pick");
        cv.put(MediaStore.Images.Media.DESCRIPTION,"Temp Descr");
        image_rui=getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,cv);

        Intent intent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, image_rui);
        startActivityForResult(intent, IMAGE_PICK_CAMERA_CODE);
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


    @Override
    protected void onStart() {
        super.onStart();
        checkUserStatus();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkUserStatus();
    }

    private void checkUserStatus(){
        FirebaseUser user =firebaseAuth.getCurrentUser();
        if(user!=null){
            //mprofileTv.setText(user.getEmail());
            email=user.getEmail();
            uid=user.getUid();

        }else{
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
        return super.onOptionsItemSelected(item);
    }
        // handle permission result
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch(requestCode){
            case CAMERA_REQUEST_CODE:{
                if(grantResults.length>0) {
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted && storageAccepted) {
                        pickFromCamera();
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
                imageIv.setImageURI(image_rui);
            }
            else if(requestCode==IMAGE_PICK_CAMERA_CODE){
                imageIv.setImageURI(image_rui);
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }
}














