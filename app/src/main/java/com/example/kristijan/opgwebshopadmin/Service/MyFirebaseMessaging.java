package com.example.kristijan.opgwebshopadmin.Service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import com.example.kristijan.opgwebshopadmin.Helper.NotificationHelper;
import com.example.kristijan.opgwebshopadmin.Model.Token;
import com.example.kristijan.opgwebshopadmin.Orders.NewOrders;
import com.example.kristijan.opgwebshopadmin.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;
import java.util.Random;

public class MyFirebaseMessaging extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        if(remoteMessage.getData() !=null)
        {
            if(Build.VERSION.SDK_INT >=Build.VERSION_CODES.O)
            {
                sendNotificationAPI26(remoteMessage);
            }
            else
            {
                sendNotification(remoteMessage);
            }
        }
    }

    @Override
    public void onNewToken(String tokenRefreshed)
    {
        super.onNewToken(tokenRefreshed);

        if (FirebaseAuth.getInstance().getCurrentUser() != null)
        {
            updateTokenToFirebase(tokenRefreshed);
        }
    }

    private void updateTokenToFirebase(String tokenRefreshed)
    {
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference tokens=db.getReference("Tokens");
        Token token=new Token(tokenRefreshed,true);
        tokens.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(token);
    }

    private void sendNotification(RemoteMessage remoteMessage) {

        Map<String,String> data=remoteMessage.getData();
        String title=data.get("title");
        String message=data.get("message");

        Intent intent=new Intent(this,NewOrders.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent=PendingIntent.getActivity(this,0,intent,PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri=RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder builder=new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.grape)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager noti=(NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        noti.notify(0,builder.build());
    }

    private void sendNotificationAPI26(RemoteMessage remoteMessage) {

        Map<String,String> data=remoteMessage.getData();
        String title=data.get("title");
        String message=data.get("message");

        PendingIntent pendingIntent;
        Uri defaultSoundUri;
        android.app.Notification.Builder builder;

        if(FirebaseAuth.getInstance().getCurrentUser().getUid()!=null)
        {
            Intent intent=new Intent(this,NewOrders.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            pendingIntent=PendingIntent.getActivity(this,0,intent,PendingIntent.FLAG_ONE_SHOT);
            defaultSoundUri=RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

            NotificationHelper notificationHelper= new NotificationHelper(this);
            builder= notificationHelper.getOPGWebShopChannelNotification(title,message,pendingIntent,defaultSoundUri);
            notificationHelper.getManager().notify(new Random().nextInt(),builder.build());
        }

        else
        {
            defaultSoundUri=RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

            NotificationHelper notificationHelper= new NotificationHelper(this);
            builder= notificationHelper.getOPGWebShopChannelNotification(title,message,defaultSoundUri);
            notificationHelper.getManager().notify(new Random().nextInt(),builder.build());
        }
    }
}
