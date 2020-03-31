package com.example.thinkanddo.adapters;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.example.thinkanddo.GoalDescriptionActivity;
import com.example.thinkanddo.R;
import com.example.thinkanddo.models.ModelGoalDescription;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class AdapterGoalDescription extends RecyclerView.Adapter<AdapterGoalDescription.MyHolder> {


    Context context;
    List<ModelGoalDescription> modelGoalDescriptionList;

    String myUid;
    Uri uri;

    private DatabaseReference Goal_Description;

    Boolean mProcessLike = false;

    public AdapterGoalDescription(Context context, List<ModelGoalDescription> modelGoalDescriptionList) {
        this.context = context;
        this.modelGoalDescriptionList = modelGoalDescriptionList;

        myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Goal_Description = FirebaseDatabase.getInstance().getReference().child("Goal_Description");

    }



    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.row_goals_list, parent, false);
        return new MyHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull final MyHolder myHolder, int i) {

        //get data

        final String uId = modelGoalDescriptionList.get(i).getUid();
        String uEmail = modelGoalDescriptionList.get(i).getuEmail();
        String uName = modelGoalDescriptionList.get(i).getuName();
        String uDp = modelGoalDescriptionList.get(i).getuDp();


        final String gId = modelGoalDescriptionList.get(i).getgId();
        final String gTitle = modelGoalDescriptionList.get(i).getgTitle();
        final String gDescription = modelGoalDescriptionList.get(i).getgDescr();
        String gTimeStamp = modelGoalDescriptionList.get(i).getgTime();



        // convert timestamp to dd/mm/yyyy hh:mm: am/pm

        Calendar calender = Calendar.getInstance(Locale.getDefault());
        calender.setTimeInMillis(Long.parseLong(gTimeStamp));
        String pTime = DateFormat.format("dd/MM/yyyy hh:mm aa", calender).toString();

        // set data


        myHolder.gTimeTv.setText(pTime);
        myHolder.gTitleTv.setText(gTitle);
        myHolder.gDescriptionTv.setText(gDescription);

        // set post image
        // if there is no image i.e. pImage.equals("noImage" )then hide imageview
        // handle "more" button click
        myHolder.moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                showMoreOptions(myHolder.moreBtn, uId, myUid, gId, gTitle);
                Toast.makeText(context, "More", Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public int getItemCount() {
        return modelGoalDescriptionList.size();
    }

    private void showMoreOptions(ImageButton moreBtn, String uid, String myUid, final String gId, final String gTitle) {

        // Creating popup menu currently having option Delete

        PopupMenu popupMenu = new PopupMenu(context, moreBtn, Gravity.END);

        // show delete option in only post of currently signed in user.
        if (uid.equals(myUid)) {
            //add items in menu
            popupMenu.getMenu().add(Menu.NONE, 0, 0, "Delete");
            popupMenu.getMenu().add(Menu.NONE, 1, 0, "Edit");
        }

        //popupMenu.getMenu().add(Menu.NONE, 2, 0, "View Detail");

        // item click listener
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                if (id == 0) {
                    // delete is clicked
                    beginDelete(gId, gTitle);
                } else if (id == 1) {
                    // edit is clicked
                    // start AddPostActivity with key "editpost" and the id of the post clicked

                    Intent intent = new Intent(context, GoalDescriptionActivity.class);
                    intent.putExtra("key", "editGoal");
                    intent.putExtra("editGoalDescriptionId", gId);
                    context.startActivity(intent);
                }

                return false;
            }
        });

        // show menu
        popupMenu.show();

    }

    private void beginDelete(String gId, String gTitle) {

        final ProgressDialog pd = new ProgressDialog(context);
        pd.setMessage("Deleting... ");
        // image deleted, now delete database
        Query fQuery = FirebaseDatabase.getInstance().getReference("Goal_Description").orderByChild("gId").equalTo(gId);
        fQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {

                    ds.getRef().removeValue();  // remove values from firebase where pid matches
                }
                // deleted
                Toast.makeText(context, "Deleted Successfully", Toast.LENGTH_SHORT).show();
                pd.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


// view holder class

    class MyHolder extends RecyclerView.ViewHolder {


        TextView  gTimeTv, gTitleTv, gDescriptionTv;

        ImageButton moreBtn;
        LinearLayout profileLayout;

        public MyHolder(@NonNull View itemView) {
            super(itemView);



            gTimeTv = itemView.findViewById(R.id.gTimeTv);
            gTitleTv = itemView.findViewById(R.id.goal_title_Tv);
            gDescriptionTv = itemView.findViewById(R.id.goal_description_Tv);
            moreBtn = itemView.findViewById(R.id.moreBtn);

            profileLayout = itemView.findViewById(R.id.profileLayout);
        }
    }
}
