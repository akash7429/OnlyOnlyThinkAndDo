package com.example.thinkanddo.adapters;

import android.app.ProgressDialog;
import android.content.Context;
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

import com.example.thinkanddo.R;
import com.example.thinkanddo.models.ModelDefineStep;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class AdapterDefineStep extends RecyclerView.Adapter<AdapterDefineStep.MyHolder> {


    Context context;
    List<ModelDefineStep> modelDefineStepList;

    String myUid;
    Uri uri;

    private DatabaseReference Goal_Define_Step;

    //Boolean mProcessLike = false;

    public AdapterDefineStep(Context context, List<ModelDefineStep> modelDefineStepList) {
        this.context = context;
        this.modelDefineStepList = modelDefineStepList;

        myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Goal_Define_Step = FirebaseDatabase.getInstance().getReference().child("Goal_Define_Step");

    }


    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.row_define_step, parent, false);
        return new MyHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull final MyHolder myHolder, int i) {

        //get data

        final String uId = modelDefineStepList.get(i).getUid();
        final String uEmail = modelDefineStepList.get(i).getuEmail();
        final String uName = modelDefineStepList.get(i).getuName();
        String uDp = modelDefineStepList.get(i).getuDp();
        final String gId = modelDefineStepList.get(i).getgId();
        final String gTitle = modelDefineStepList.get(i).getgTitle();
        final String gDescription = modelDefineStepList.get(i).getgDescr();
        String gTimeStamp = modelDefineStepList.get(i).getgTime();
        final String dsId = modelDefineStepList.get(i).getDsId();
        final String dsTitle = modelDefineStepList.get(i).getDsTitle();
        String dsTime = modelDefineStepList.get(i).getDsTime();


        // convert timestamp to dd/mm/yyyy hh:mm: am/pm

        Calendar calender = Calendar.getInstance(Locale.getDefault());
        calender.setTimeInMillis(Long.parseLong(dsTime));
        String pTime = DateFormat.format("dd MMM yyyy\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\thh:mm aa", calender).toString();

        // set data


        myHolder.dsTimeTv.setText(pTime);
        myHolder.dsTitleTv.setText(dsTitle);


        // set post image
        // if there is no image i.e. pImage.equals("noImage" )then hide imageview
        // handle "more" button click
        myHolder.moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                showMoreOptions(myHolder.moreBtn,dsId,dsTitle, uId, myUid, gId, gTitle, gDescription, uName, uEmail);
                //Toast.makeText(context, "More", Toast.LENGTH_SHORT).show();
            }
        });

     /*   myHolder.profileLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, DefineStepActivity.class);
                intent.putExtra("uId", uId);
                intent.putExtra("gId",gId);
                intent.putExtra("gTitle", gTitle);
                intent.putExtra("gDescr", gDescription);
                intent.putExtra("uName", uName);
                intent.putExtra("uEmail", uEmail);
                context.startActivity(intent);
            }
        });*/

    }

    @Override
    public int getItemCount() {
        return modelDefineStepList.size();
    }

    private void showMoreOptions(ImageButton moreBtn, final String dsId,final String dsTitle,final String uid, String myUid, final String gId, final String gTitle, final String gDescription, final String uName, final String uEmail) {

        // Creating popup menu currently having option Delete

        final PopupMenu popupMenu = new PopupMenu(context, moreBtn, Gravity.END);

        // show delete option in only post of currently signed in user.
        if (uid.equals(myUid)) {
            //add items in menu
           // popupMenu.getMenu().add(Menu.NONE, 2, 0, "Finish");
            popupMenu.getMenu().add(Menu.NONE, 0, 0, "Delete");
          //  popupMenu.getMenu().add(Menu.NONE, 1, 0, "Edit");
        }

       // popupMenu.getMenu().add(Menu.NONE, 3, 0, "View Detail");
        //popupMenu.getMenu().add(Menu.NONE, 2, 0, "View Detail");

        // item click listener
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                if (id == 0) {
                    // delete is clicked
                    beginDelete(dsId, dsTitle);
                }

                return false;
            }
        });

        // show menu
        popupMenu.show();

    }

    private void uploadData(final String title, final String description, final String gid, final String uid, final String name, final String email) {
        final ProgressDialog pd = new ProgressDialog(context);
        pd.setMessage("Finishing...");
        pd.show();

        final String timeStamp = String.valueOf(System.currentTimeMillis());
        //String filePathAndName="Goal Description/" + "goalDes_" + timeStamp;

        //post without image
        HashMap<Object, String> hashMap = new HashMap<>();
        hashMap.put("uid", uid);
        hashMap.put("uName", name);
        hashMap.put("uEmail", email);
        hashMap.put("gId", gid);
        hashMap.put("gTitle", title);
        hashMap.put("gDescr", description);
        hashMap.put("gTime", timeStamp);


        //path to store post data
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Goal_Finished");
        //
        ref.child(timeStamp).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

                pd.dismiss();
                Toast.makeText(context, "Goal Achieved", Toast.LENGTH_SHORT).show();

            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        pd.dismiss();
                        Toast.makeText(context, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void beginDelete(String dsId, String dsTitle) {

        final ProgressDialog pd = new ProgressDialog(context);
        pd.setMessage("Deleting... ");
        // image deleted, now delete database
        Query fQuery = FirebaseDatabase.getInstance().getReference("Goal_Define_Step").orderByChild("dsId").equalTo(dsId);
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


        TextView dsTimeTv, dsTitleTv;

        ImageButton moreBtn;
        LinearLayout profileLayout;

        public MyHolder(@NonNull View itemView) {
            super(itemView);


            dsTimeTv = itemView.findViewById(R.id.define_step_time_Tv);
            dsTitleTv = itemView.findViewById(R.id.define_step_title_Tv);

            moreBtn = itemView.findViewById(R.id.moreBtn);

        }
    }
}
