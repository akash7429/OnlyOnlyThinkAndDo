package com.group.amplifate;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.group.amplifate.adapters.AdapterDefineStep;
import com.group.amplifate.models.ModelDefineStep;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class DefineStepActivity extends AppCompatActivity {

    FirebaseAuth firebaseAuth;
    DatabaseReference userDbRef;
    ActionBar actionBar;

    RecyclerView dsRecycler_view;

    String uId,gId,gTitle,gDescr,uName,uEmail;
    EditText dstitleEt;
    Button dsAddBtn;

    List<ModelDefineStep> defineStepList;
    AdapterDefineStep adapterDefineStep;
   TextView goal_set_description,my_step_tv;

    ProgressDialog pd;

    // info of post to be edited
    String editTitle, editDescription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_define_step);

        Intent intent = getIntent();
        //uId =intent.getStringExtra("uId");
        gId = intent.getStringExtra("gId");
        gTitle = intent.getStringExtra("gTitle");
        gDescr = intent.getStringExtra("gDescr");
        uName = intent.getStringExtra("uName");
        uEmail = intent.getStringExtra("uEmail");


        actionBar = getSupportActionBar();
        actionBar.setTitle(gTitle);
        //enable back button in action bar
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);


        pd=new ProgressDialog(this);

        firebaseAuth=FirebaseAuth.getInstance();


        goal_set_description = findViewById(R.id.goal_set_description);
        dstitleEt=findViewById(R.id.dsTitleEt);
        dsAddBtn=findViewById(R.id.dsAddBtn);
        dsRecycler_view=findViewById(R.id.ds_recyclerview_goals);

        my_step_tv=findViewById(R.id.my_step_tv);

        goal_set_description.setText(gDescr);

        defineStepList = new ArrayList<>();
        checkUserStatus();
        dsAddBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                //get description from edit text
                String title=dstitleEt.getText().toString().trim();


                if(TextUtils.isEmpty(title)){
                    Toast.makeText(DefineStepActivity.this,"Enter Step....",Toast.LENGTH_SHORT).show();
                    return;
                }
                uploadStep(title);

            }
        });


        loadDefineStep();



        //Toast.makeText(this,"Check"+uId.toString(),Toast.LENGTH_LONG).show();


    }

    private void loadDefineStep() {


        LinearLayoutManager layoutManager = new LinearLayoutManager(DefineStepActivity.this);
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(false);
        dsRecycler_view.setHasFixedSize(true);
        dsRecycler_view.setLayoutManager(layoutManager);
        //dsRecycler_view.setVisibility(View.VISIBLE);
        // path of al
        // l goals

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Goal_Define_Step");

        Query query= ref.orderByChild("gId").equalTo(gId);
        // get all data from this ref.

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                defineStepList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {

                    ModelDefineStep modelGoal = ds.getValue(ModelDefineStep.class);

                    defineStepList.add(modelGoal);

                    //adapter

                    adapterDefineStep = new AdapterDefineStep(DefineStepActivity.this, defineStepList);
                    // set adapter to recyclerview
                    dsRecycler_view.setAdapter(adapterDefineStep);
                }

                if(defineStepList.size()==0){

                    my_step_tv.setVisibility(View.GONE);
                    dsRecycler_view.setVisibility(View.GONE);
                }
                else{

                    my_step_tv.setVisibility(View.VISIBLE);
                    dsRecycler_view.setVisibility(View.VISIBLE);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

                Toast.makeText(DefineStepActivity.this, "" + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void uploadStep(String title) {


        pd.setMessage("Adding your step...");
        pd.show();

        final String timeStamp= String.valueOf(System.currentTimeMillis());
       // String filePathAndName="Goal Description/" + "goalDes_" + timeStamp;


        //post without image
        HashMap<Object, String> hashMap=new HashMap<>();
        hashMap.put("uid",uId);
        hashMap.put("uName",uName);
        hashMap.put("uEmail",uEmail);
        hashMap.put("gId",gId);
        hashMap.put("gTitle",gTitle);
        hashMap.put("gDescr",gDescr);
        hashMap.put("dsTime",timeStamp);
        hashMap.put("dsId",timeStamp);
        hashMap.put("dsTitle",title);


        //path to store post data
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Goal_Define_Step");
        //
        ref.child(timeStamp).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                pd.dismiss();
                Toast.makeText(DefineStepActivity.this,"Step Saved",Toast.LENGTH_SHORT).show();
                dstitleEt.setText("");


            }
        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        pd.dismiss();
                        Toast.makeText(DefineStepActivity.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                });
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
        else if(id==R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }

    private void checkUserStatus(){
        FirebaseUser user =firebaseAuth.getCurrentUser();
        if(user!=null){
            //mprofileTv.setText(user.getEmail());

            uId=user.getUid();

        }else{
            startActivity(new Intent(DefineStepActivity.this,MainActivity.class));
            finish();
        }
    }
}


