package com.example.thinkanddo.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.thinkanddo.ChatActivity;
import com.example.thinkanddo.R;
import com.example.thinkanddo.models.ModelUsers;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;
//import com.google.firebase.database.core.view.View;

public class AdapterChatlist extends RecyclerView.Adapter<AdapterChatlist.MyHolder> {

    Context context;
    List<ModelUsers> usersList ; // get user info
    private HashMap<String, String> lastMessageMap;

    public AdapterChatlist(Context context,List<ModelUsers> usersList) {  //,HashMap<String, String> lastMessageMap
        this.context = context;
        this.usersList = usersList;
        lastMessageMap = new HashMap<>();
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup viewGroup,int i) {
        //inflate layout row_chatlist.xml
        View view= LayoutInflater.from(context).inflate(R.layout.row_chatlist,viewGroup,false);

        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder myHolder,int i) {
        //get data
        final String hisUid=usersList.get(i).getUid();
        String usersImage=usersList.get(i).getImage();
        String usersName=usersList.get(i).getName();
        String lastMessage=lastMessageMap.get(hisUid);

        //set data
        myHolder.nameTv.setText(usersName);
        if (lastMessage==null || lastMessage.equals("default")){
            myHolder.lastMessageTv.setVisibility(View.GONE);
        }
        else{
            myHolder.lastMessageTv.setVisibility(View.VISIBLE);
            myHolder.lastMessageTv.setText(lastMessage);
        }
        try{
            Picasso.get().load(usersImage).placeholder(R.drawable.ic_default).into(myHolder.profileIv);
        }
        catch (Exception e){
            Picasso.get().load(R.drawable.ic_default).into(myHolder.profileIv);

        }
        if (usersList.get(i).getOnlineStatus().equals("Online")){
            //online
           myHolder.onlineStatusIv.setImageResource(R.drawable.circle_online);
        }
        else{
            //offline
            myHolder.onlineStatusIv.setImageResource(R.drawable.circle_offline);
        }
        //handle click of users in chatlist

        myHolder.itemView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                //start chat activity with that user
                Intent intent=new Intent(context,ChatActivity.class);
                intent.putExtra("hisUid",hisUid);
                context.startActivity(intent);
            }
        });

    }
    public void setLastMessageMap(String userId, String lastMessage){

        lastMessageMap.put(userId,lastMessage);

    }

    @Override
    public int getItemCount() {
        return usersList.size();   //size of the list
    }

    class MyHolder extends RecyclerView.ViewHolder{

        ImageView profileIv,onlineStatusIv ;
        TextView nameTv,lastMessageTv;

        public MyHolder(@NonNull View itemView){
            super(itemView);
            //int viewa
            profileIv= itemView.findViewById(R.id.profileIv);
            onlineStatusIv= itemView.findViewById(R.id.onlineStatusIv);
            nameTv=itemView.findViewById(R.id.nameTv);
           lastMessageTv=itemView.findViewById(R.id.lastMessageTv);





        }
    }
}
