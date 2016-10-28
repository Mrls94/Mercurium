package com.example.mbalza.mercurium;

import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Chat_Room extends AppCompatActivity {

    private Button sendmsg;
    private EditText textmsg;
    private TextView chatconvsersation;

    private String username, roomname;

    private DatabaseReference root;
    private String temp_key;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat__room);

        sendmsg = (Button) findViewById(R.id.buttonsend);
        textmsg = (EditText) findViewById(R.id.editTextMsg);
        chatconvsersation = (TextView) findViewById(R.id.textViewChat);

        username = getIntent().getStringExtra("user_name");
        roomname = getIntent().getStringExtra("room_name");

        setTitle("Room - "+roomname);

        root = FirebaseDatabase.getInstance().getReference().child(roomname);


        sendmsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Map<String, Object> map = new HashMap<String, Object>();

                temp_key = root.push().getKey();
                root.updateChildren(map);

                DatabaseReference message_root = root.child(temp_key);
                Map<String,Object> map2 = new HashMap<String, Object>();

                map2.put("Name", username);
                map2.put("Msg", textmsg.getText().toString());

                message_root.updateChildren(map2);


            }
        });

        root.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                append_chat(dataSnapshot);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                append_chat(dataSnapshot);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void append_chat(DataSnapshot dataSnapshot) {

        String uname, msg;

        Iterator i = dataSnapshot.getChildren().iterator();

        while (i.hasNext())
        {
            msg = (String)((DataSnapshot)i.next()).getValue();
            uname = (String)((DataSnapshot)i.next()).getValue();

            chatconvsersation.append(uname+" Dice:  "+msg+"\n \n");
        }

    }
}
