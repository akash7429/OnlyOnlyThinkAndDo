package com.example.thinkanddo.notifications;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import com.example.thinkanddo.ChatActivity;
import com.example.thinkanddo.PostDetailActivity;
import com.example.thinkanddo.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Random;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

public class FirebaseMessaging extends FirebaseMessagingService {

    private static final String ADMIN_CHANNEL_ID = "admin_channel";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);



        //get current user from shared preferences
        SharedPreferences sp=getSharedPreferences("SP_USER",MODE_PRIVATE);
        String savedCurrentUser=sp.getString("Current_USERID","None");


        /* There are two types of notifications chat and post.*/

        Object object;
        String notificationType = remoteMessage.getData().get("notificationType");

        if(notificationType.equals("PostNotification")){
            String sender = remoteMessage.getData().get("sender");
            String pId = remoteMessage.getData().get("pId");
            String pTitle = remoteMessage.getData().get("pTitle");
            String pDescription = remoteMessage.getData().get("pDescription");
            
            //if user is same that has posted don't show notification
            
            if(!sender.equals(savedCurrentUser)){
                
                showPostNotification(""+pId, ""+pTitle,""+pDescription);
            }
        }
        else if(notificationType.equals("ChatNotification")){


            String sent = remoteMessage.getData().get("sent");
            String user = remoteMessage.getData().get("user");
            FirebaseUser fUser= FirebaseAuth.getInstance().getCurrentUser();
            if(fUser != null && sent.equals(fUser.getUid())){
                if(!savedCurrentUser.equals(user)){
                    if(Build.VERSION.SDK_INT >=Build.VERSION_CODES.O){
                        sendOAndAboveNotificaition(remoteMessage);
                    }
                    else {
                        sendNormalNotification(remoteMessage);
                    }
                }
            }
        }


    }

    private void showPostNotification(String pId, String pTitle, String pDescription) {

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);


        int notificationID = new Random().nextInt(3000);

        /* Apps targeting SDK 26 or above must implement notification channels
        *  and add its notification to at least one of them
           Let's add check if version is Oreo or higher the setup notification channel*/

        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){

            setupPostNotificationChannel(notificationManager);
        }

        // show post detail activity using post id when notification clicked

        Intent intent = new Intent(this, PostDetailActivity.class);
        intent.putExtra("postId",pId);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        Context context;
        PendingIntent pendingIntent = PendingIntent.getActivity(this,0 , intent,PendingIntent.FLAG_ONE_SHOT );

        // LargeIcon

        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher_logo);

        //sound for notification
        Uri notificationSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this,""+ADMIN_CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher_logo)
                .setLargeIcon(largeIcon)
                .setContentTitle(pTitle)
                .setContentText(pDescription)
                .setSound(notificationSoundUri)
                .setContentIntent(pendingIntent);
        //show notification
        notificationManager.notify(notificationID, notificationBuilder.build());
    }

    private void setupPostNotificationChannel(NotificationManager notificationManager) {

        CharSequence channelName = "New Notification";

        String channelDescription = "Device to device post notification";

        NotificationChannel adminChannel = new NotificationChannel(ADMIN_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_HIGH);

        adminChannel.setDescription(channelDescription);
        adminChannel.enableLights(true);
        adminChannel.setLightColor(Color.RED);
        adminChannel.enableVibration(true);

        if(notificationManager!=null){

            notificationManager.createNotificationChannel(adminChannel);
        }
    }

    private void sendNormalNotification(RemoteMessage remoteMessage) {
        String user= remoteMessage.getData().get("user");
        String icon= remoteMessage.getData().get("icon");
        String title= remoteMessage.getData().get("title");
        String body= remoteMessage.getData().get("body");

        RemoteMessage.Notification notification=remoteMessage.getNotification();
        int i= Integer.parseInt(user.replaceAll("[\\D]",""));
        Intent intent = new Intent(this, ChatActivity.class);
        Bundle bundle=new Bundle();
        bundle.putString("hisUid",user);
        intent.putExtras(bundle);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pIntent=PendingIntent.getActivity(this,i,intent,PendingIntent.FLAG_ONE_SHOT);

        Uri defSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder builder= new NotificationCompat.Builder(this)
                .setSmallIcon(Integer.parseInt(icon))
                .setContentText(body)
                .setAutoCancel(true)
                .setSound(defSoundUri)
                .setContentIntent(pIntent);

        NotificationManager notificationManager=(NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        int j = 0;
        if(i > 0){
            j=i;
        }
        notificationManager.notify(j,builder.build());
    }

    private void sendOAndAboveNotificaition(RemoteMessage remoteMessage) {
        String user= remoteMessage.getData().get("user");
        String icon= remoteMessage.getData().get("icon");
        String title= remoteMessage.getData().get("title");
        String body= remoteMessage.getData().get("body");

        RemoteMessage.Notification notification=remoteMessage.getNotification();
        int i= Integer.parseInt(user.replaceAll("[\\D]",""));
        Intent intent = new Intent(this, ChatActivity.class);
        Bundle bundle=new Bundle();
        bundle.putString("hisUid",user);
        intent.putExtras(bundle);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pIntent=PendingIntent.getActivity(this,i,intent,PendingIntent.FLAG_ONE_SHOT);

        Uri defSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        OreoAndAboveNotification notification1=new OreoAndAboveNotification(this);
        Notification.Builder builder= notification1.getONotifications(title,body,pIntent,defSoundUri,icon);

        int j = 0;
        if(i > 0){
            j=i;
        }
        notification1.getManager().notify(j,builder.build());
    }

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user!=null){
            updateToken(s);
        }
    }

    private void updateToken(String tokenRefresh) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Tokens");
        Token token = new Token(tokenRefresh);
        ref.child(user.getUid()).setValue(token);
    }
}

