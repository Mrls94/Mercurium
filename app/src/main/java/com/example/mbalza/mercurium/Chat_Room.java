package com.example.mbalza.mercurium;

import android.content.Intent;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class Chat_Room extends AppCompatActivity {

    private Button sendmsg;
    private EditText textmsg;
    private TextView chatconvsersation;
    private RecyclerView recyclerView;


    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private String username, roomname;
    private ArrayList<String> data = new ArrayList<>();


    private DatabaseReference root;
    private String temp_key;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat__room);

        sendmsg = (Button) findViewById(R.id.buttonsend);
        textmsg = (EditText) findViewById(R.id.editTextMsg);
        chatconvsersation = (TextView) findViewById(R.id.textViewChat);
        recyclerView = (RecyclerView) findViewById(R.id.myRecyclerView);

        mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);

        data.add(" empty ");
        data.add(" empty 2");

        mAdapter = new MyAdapter(data);
        recyclerView.setAdapter(mAdapter);

        username = getIntent().getStringExtra("user_name");
        roomname = getIntent().getStringExtra("room_name");

        setTitle("Room - "+roomname);

        root = FirebaseDatabase.getInstance().getReference().child(roomname);




        sendmsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i = new Intent(getApplicationContext(),NewPost.class);
                i.putExtra("user_name",username);
                i.putExtra("room_name",roomname);
                startActivity(i);




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

        Set<String> set = new HashSet<String>();

        String uname, msg;

        Iterator i = dataSnapshot.getChildren().iterator();

        while (i.hasNext())
        {
            msg = (String)((DataSnapshot)i.next()).getValue();
            uname = (String)((DataSnapshot)i.next()).getValue();

            String added = uname+" Dice:  "+msg+"\n \n";

            chatconvsersation.append(added);

            System.out.println(added);

            //set.add(added);

        }

        data.clear();
        data.addAll(set);

        mAdapter.notifyDataSetChanged();

    }
}
