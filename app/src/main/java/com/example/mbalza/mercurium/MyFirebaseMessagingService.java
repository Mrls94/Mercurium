package com.example.mbalza.mercurium;

import android.app.Service;
import android.widget.Toast;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

/**
 * Created by mbalza on 11/2/16.
 */
public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        //Toast.makeText(getApplicationContext(),remoteMessage.getNotification().toString(),Toast.LENGTH_SHORT).show();
    }
}
