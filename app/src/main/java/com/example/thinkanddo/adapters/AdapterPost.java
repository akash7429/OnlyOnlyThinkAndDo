package com.example.thinkanddo.adapters;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.example.thinkanddo.AddPostActivity;
import com.example.thinkanddo.PostDetailActivity;
import com.example.thinkanddo.R;
import com.example.thinkanddo.TheirProfileActivity;
import com.example.thinkanddo.models.ModelPost;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
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
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

public class AdapterPost extends RecyclerView.Adapter<AdapterPost.MyHolder> {

    Context context;
    List<ModelPost> postList;

    String myUid;
    Uri uri;

    private DatabaseReference likesRef;
    private DatabaseReference postsRef;
    private DatabaseReference videoRef;

    Boolean mProcessLike = false;

    public AdapterPost(Context context, List<ModelPost> postList) {
        this.context = context;
        this.postList = postList;

        myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        likesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
        postsRef = FirebaseDatabase.getInstance().getReference().child("Posts");
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        // inflate layout row.xml

        View view = LayoutInflater.from(context).inflate(R.layout.row_posts, viewGroup, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyHolder myHolder, final int i) {
        //get data

        final String uId = postList.get(i).getUid();
        String uEmail = postList.get(i).getuEmail();
        String uName = postList.get(i).getuName();
        final String pId = postList.get(i).getpId();
        String uDp = postList.get(i).getuDp();
        final String pTitle = postList.get(i).getpTitle();
        final String pDescription = postList.get(i).getpDescr();
        final String pImage = postList.get(i).getpImage();
        final String pVideo = postList.get(i).getpVideo();
        String pTimeStamp = postList.get(i).getpTime();
        String pLikes = postList.get(i).getpLikes(); // Contains total number of likes for post
        final String pComments = postList.get(i).getpComments();

        setLikes(myHolder, pId);

        // convert timestamp to dd/mm/yyyy hh:mm: am/pm

        Calendar calender = Calendar.getInstance(Locale.getDefault());
        calender.setTimeInMillis(Long.parseLong(pTimeStamp));
        String pTime = DateFormat.format("dd/MM/yyyy hh:mm aa", calender).toString();

        // set data

        myHolder.uNameTv.setText(uName);
        myHolder.pTimeTv.setText(pTime);
        myHolder.pTitleTv.setText(pTitle);
        myHolder.pDescriptionTv.setText(pDescription);
        myHolder.pLikesTv.setText(pLikes+" Likes");
        myHolder.pCommentsTv.setText(pComments+" Comments");


        // set user dp
        try {
            Picasso.get().load(uDp).placeholder(R.drawable.ic_face_black_img).into(myHolder.uPictureIv);

        } catch (Exception ex) {


        }

        // set post image
        // if there is no image i.e. pImage.equals("noImage" )then hide imageview

        if (pImage.equals("noImage") && pVideo.equals("noVideo")) {

            // hide imageview

            myHolder.pImageIv.setVisibility(View.GONE);
            myHolder.pVideovv.setVisibility(View.GONE);
        }
        else if (!pImage.equals("noImage") && pVideo.equals("noVideo")) {
            // show imageview

            myHolder.pImageIv.setVisibility(View.VISIBLE);
            myHolder.pVideovv.setVisibility(View.GONE);
            try {
                Picasso.get().load(pImage).into(myHolder.pImageIv);

            } catch (Exception ex) {

            }

        } else if (pImage.equals("noImage") && !pVideo.equals("noVideo")){

            myHolder.pImageIv.setVisibility(View.GONE);
            myHolder.pVideovv.setVisibility(View.VISIBLE);
            myHolder.pVideovv.setVideoURI(Uri.parse(pVideo));
            myHolder.pVideovv.start();


            MediaController mediaController = new MediaController(this.context);
            myHolder.pVideovv.setMediaController(mediaController);
            mediaController.setAnchorView(myHolder.pVideovv);


        }

        // handle "more" button click
        myHolder.moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                showMoreOptions(myHolder.moreBtn, uId, myUid, pId, pImage);
                Toast.makeText(context, "More", Toast.LENGTH_SHORT).show();
            }
        });

        // handle "like" button click
        myHolder.likeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // get number of likes for the post, whose like button clicked
                // if currently signed in user has not liked it before
                // increase value by 1, otherwise decrease value by 1

                final int pLikes = Integer.parseInt(postList.get(i).getpLikes());
                mProcessLike = true;

                // get id of the post clicked
                final String postIde = postList.get(i).getpId();

                likesRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (mProcessLike) {

                            if (dataSnapshot.child(postIde).hasChild(myUid)) {

                                // already liked, so remove like

                                postsRef.child(postIde).child("pLikes").setValue("" + (pLikes - 1));
                                likesRef.child(postIde).child(myUid).removeValue();
                                mProcessLike = false;
                            } else {

                                // not Liked, like it
                                postsRef.child(postIde).child("pLikes").setValue("" + (pLikes + 1));
                                likesRef.child(postIde).child(myUid).setValue("Liked"); // set any value
                                mProcessLike = false;
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });

        // handle "comment" button click
        myHolder.commentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, PostDetailActivity.class);
                intent.putExtra("postId", pId);
                context.startActivity(intent);
            }
        });

        // handle "share" button click
        myHolder.shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*Some post contain only text some contain texts so we will handle them both*/
                BitmapDrawable bitmapDrawable =(BitmapDrawable)myHolder.pImageIv.getDrawable();
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
        myHolder.profileLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, TheirProfileActivity.class);
                intent.putExtra("uId", uId);
                context.startActivity(intent);
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
        context.startActivity(Intent.createChooser(sIntent,"Share Via")); // message to show in share dialogue
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
        context.startActivity(Intent.createChooser(sIntent,"Share Via"));

    }

    private Uri saveImageToShare(Bitmap bitmap) {
        File imageFolder= new File(context.getCacheDir(),"images");
        Uri uri=null;
        try{
            imageFolder.mkdirs(); //create if not exists
            File file = new File(imageFolder,"shared_image.png");

            FileOutputStream stream =new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG,90,stream);
            stream.flush();
            stream.close();
            uri= FileProvider.getUriForFile(context,"com.example.thinkanddo.fileprovider",file);

        }
        catch (Exception e){
            Toast.makeText(context,""+e.getMessage(),Toast.LENGTH_SHORT).show();

        }
        return uri;
    }



    private void setLikes(final MyHolder myHolder, final String postKey) {

        likesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(postKey).hasChild(myUid)) {

                    //user has liked this post
                    /* To indicate that the post is liked by this signed user
                    Change drawable left icon of like button
                    Change text of like button from "like" to "liked".
                     */

                    myHolder.likeBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_liked, 0, 0, 0);
                    myHolder.likeBtn.setText("Liked");
                } else {
                    // user has not liked this post

                    myHolder.likeBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_like_black, 0, 0, 0);
                    myHolder.likeBtn.setText("Like");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void showMoreOptions(ImageButton moreBtn, String uid, String myUid, final String pId, final String pImage) {

        // Creating popup menu currently having option Delete

        PopupMenu popupMenu = new PopupMenu(context, moreBtn, Gravity.END);

        // show delete option in only post of currently signed in user.
        if (uid.equals(myUid)) {
            //add items in menu
            popupMenu.getMenu().add(Menu.NONE, 0, 0, "Delete");
            popupMenu.getMenu().add(Menu.NONE, 1, 0, "Edit");
        }

        popupMenu.getMenu().add(Menu.NONE, 2, 0, "View Detail");

        // item click listner
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                if (id == 0) {
                    // delete is clicked
                    beginDelete(pId, pImage);
                } else if (id == 1) {
                    // edit is clicked
                    // start AddPostActivity with key "editpost" and the id of the post clicked

                    Intent intent = new Intent(context, AddPostActivity.class);
                    intent.putExtra("key", "editPost");
                    intent.putExtra("editPostId", pId);
                    context.startActivity(intent);
                }
                else if(id==2){
                    Intent intent = new Intent(context, PostDetailActivity.class);
                    intent.putExtra("postId", pId);
                    context.startActivity(intent);
                }
                return false;
            }
        });

        // show menu
        popupMenu.show();


    }

    private void beginDelete(String pId, String pImage) {

        // psot can be with or without image.

        if (pImage.equals("noImage")) {
            // Post without image.
            deleteWithoutImage(pId);

        } else {
            // Post with image
            deleteWithImage(pId, pImage);
        }
    }

    private void deleteWithImage(final String pId, String pImage) {
        // progree bar

        final ProgressDialog pd = new ProgressDialog(context);
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
                Query fQuery = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(pId);
                fQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot ds : dataSnapshot.getChildren()) {

                            ds.getRef().removeValue();  // remove values from firebase where pid matches
                        }
                        // deleted
                        Toast.makeText(context,"Deleted Successfully",Toast.LENGTH_SHORT).show();
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
                Toast.makeText(context, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });


    }

    private void deleteWithoutImage(String pId) {

        final ProgressDialog pd = new ProgressDialog(context);
        pd.setMessage("Deleting... ");
        // image deleted, now delete database
        Query fQuery = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(pId);
        fQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {

                    ds.getRef().removeValue();  // remove values from firebase where pid matches
                }
                // deleted
                Toast.makeText(context, "Deleted Succesfully", Toast.LENGTH_SHORT).show();
                pd.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    @Override
    public int getItemCount() {
        return postList.size();
    }
// view holder class

    class MyHolder extends RecyclerView.ViewHolder {


        // views from rowPost.xml

        ImageView uPictureIv, pImageIv;

        VideoView pVideovv;
        TextView uNameTv, pTimeTv, pTitleTv, pDescriptionTv, pLikesTv,pCommentsTv;
        ImageButton moreBtn;
        Button likeBtn, shareBtn, commentBtn;
        LinearLayout profileLayout;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            uPictureIv = itemView.findViewById(R.id.uPictureIv);
            pImageIv = itemView.findViewById(R.id.pImageIv);
            uNameTv = itemView.findViewById(R.id.uNameTv);
            pTimeTv = itemView.findViewById(R.id.pTimeTv);
            pTitleTv = itemView.findViewById(R.id.pTitleTv);
            pVideovv = itemView.findViewById(R.id.pVideoVv);
            pDescriptionTv = itemView.findViewById(R.id.pDescriptionTv);
            pLikesTv = itemView.findViewById(R.id.pLikesTv);
            pCommentsTv = itemView.findViewById(R.id.pCommentsTv);
            moreBtn = itemView.findViewById(R.id.moreBtn);
            likeBtn = itemView.findViewById(R.id.likeBtn);
            shareBtn = itemView.findViewById(R.id.shareBtn);
            commentBtn = itemView.findViewById(R.id.commentBtn);
            profileLayout = itemView.findViewById(R.id.profileLayout);
        }
    }

}

