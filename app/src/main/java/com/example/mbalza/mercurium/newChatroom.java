package com.example.mbalza.mercurium;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class newChatroom extends AppCompatActivity {

    private ListView listViewchat;
    private Button sendmsg;

    private String username, roomname;

    private ArrayAdapter<String> arrayAdapter;
    private ArrayList<String> list_of_chats = new ArrayList<>();
    private ArrayList<String> list_of_images = new ArrayList<>();

    private DatabaseReference root;
    private String temp_key;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_chatroom);


        list_of_chats.add("1 strubg");
        list_of_chats.add("2 string");

        listViewchat = (ListView) findViewById(R.id.Listviewnew);
        arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,list_of_chats);
        listViewchat.setAdapter(arrayAdapter);

        sendmsg = (Button) findViewById(R.id.buttonNewPost);

        username = getIntent().getStringExtra("user_name");
        roomname = getIntent().getStringExtra("room_name");

        setTitle("Room - "+roomname);

        root = FirebaseDatabase.getInstance().getReference().child(roomname);

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
        Set<String> imageset = new HashSet<>();

        String uname, msg, date, imageurl;

        Iterator i = dataSnapshot.getChildren().iterator();

        while (i.hasNext())
        {
            date = (String)((DataSnapshot)i.next()).getValue();

            msg = (String)((DataSnapshot)i.next()).getValue();
            uname = (String)((DataSnapshot)i.next()).getValue();

            imageurl = (String) ((DataSnapshot)i.next()).getValue();



            String onelie = "justoneline";
            String added = date+" \n"+uname+" Dice:  \n"+msg;

            //chatconvsersation.append(added);

            System.out.println(added);

            set.add(added);
            imageset.add(imageurl);

        }

        list_of_images.clear();
        list_of_images.addAll(imageset);

        list_of_chats.clear();
        list_of_chats.addAll(set);
        //System.out.println(set.toString());
        arrayAdapter.notifyDataSetChanged();
        //mAdapter.notifyDataSetChanged();

    }
}
