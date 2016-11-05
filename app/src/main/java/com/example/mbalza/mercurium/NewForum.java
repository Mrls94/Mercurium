package com.example.mbalza.mercurium;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.ButtonBarLayout;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

public class NewForum extends AppCompatActivity {

    private DatabaseReference root;
    private ArrayList<UsernameUIDpair> list_of_users;
    private ArrayList<CheckBox> list_of_checkbox;
    private String currentUID;
    private String currentUserName;

    private EditText topicname;
    private Button addTopicbtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_forum);

        root = FirebaseDatabase.getInstance().getReference().getRoot().child("Users");
        list_of_users = new ArrayList<>();
        list_of_checkbox = new ArrayList<>();

        currentUID = getIntent().getStringExtra("uid");
        currentUserName = getIntent().getStringExtra("username");

        topicname = (EditText) findViewById(R.id.ForumNameEditText);
        addTopicbtn = (Button) findViewById(R.id.CreateForumBtn);

        addTopicbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String forumname = topicname.getText().toString();

                UsernameUIDpair currentuser = new UsernameUIDpair(currentUserName,currentUID);
                list_of_users.add(currentuser);

                for (int i = 0; i<list_of_checkbox.size();i++)
                {
                    CheckBox chbox = list_of_checkbox.get(i);
                    if(chbox.isChecked())
                    {
                        String name = chbox.getText().toString();
                        String id =  chbox.getTag().toString();

                        UsernameUIDpair us = new UsernameUIDpair(name,id);
                        list_of_users.add(us);

                    }
                }

                DatabaseReference root = FirebaseDatabase.getInstance().getReference().getRoot();
                //Map<String,Object> map = new HashMap<String, Object>();
                //map.put(forumname,"");
                //root.updateChildren(map);

                //String tempkey = root.push().getKey();
                //Map<String,Object> map = new HashMap<String, Object>();
                //root.updateChildren(map);

                for(int i = 0; i<list_of_users.size(); i++)
                {
                    UsernameUIDpair user = list_of_users.get(i);
                    //Toast.makeText(getApplicationContext(), user.DisplayName+" - "+user.Uid,Toast.LENGTH_SHORT).show();
                    DatabaseReference rootuser = FirebaseDatabase.getInstance()
                            .getReference().getRoot()
                            .child("Users").child(user.Uid).child("Forums");

                    Map<String,Object> map2 = new HashMap<String, Object>();
                    map2.put(forumname,"");

                    rootuser.updateChildren(map2);

                }

                Map<String,Object> map = new HashMap<String, Object>();
                map.put(forumname," ");
                root.updateChildren(map);

                NotifyforumAsync asyc = new NotifyforumAsync();
                asyc.execute(forumname);
                Toast.makeText(getApplicationContext(),"Foro Creado",Toast.LENGTH_SHORT).show();
                finish();

            }
        });

        root.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                list_of_checkbox.clear();
                ((ViewGroup) findViewById(R.id.ScrollViewLinear)).removeAllViews();
                listUsersValue(dataSnapshot);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        root.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                //listUsers(dataSnapshot);

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                //listUsers(dataSnapshot);

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

    private void listUsersValue(DataSnapshot dataSnapshot) {

        Iterator i = dataSnapshot.getChildren().iterator();

        while (i.hasNext())
        {
            DataSnapshot value = (DataSnapshot)i.next();

            Iterator j = value.getChildren().iterator();

            while (j.hasNext())
            {
                DataSnapshot innervalue = (DataSnapshot)j.next();
                if (innervalue.getKey().compareTo("DisplayName")==0)
                {
                    //Toast.makeText(getApplicationContext(),innervalue.getValue().toString(),Toast.LENGTH_SHORT).show();
                    String id = value.getKey();
                    String name = innervalue.getValue().toString();
                    UsernameUIDpair user = new UsernameUIDpair(name,id);

                    if(currentUID.compareTo(id)!=0)
                    {
                        CheckBox checkbox = new CheckBox(this);
                        checkbox.setTag(id);
                        checkbox.setText(name);

                        list_of_checkbox.add(checkbox);
                        ((ViewGroup) findViewById(R.id.ScrollViewLinear)).addView(checkbox);
                    }



                    //list_of_users.add(user);

                }
            }

            //Toast.makeText(getApplicationContext(), value.getKey(), Toast.LENGTH_SHORT).show();
        }

    }

    public void listUsers(DataSnapshot dataSnapshot)
    {
        Iterator i = dataSnapshot.getChildren().iterator();

        while(i.hasNext())
        {
            DataSnapshot value = ((DataSnapshot)i.next());



            Toast.makeText(getApplicationContext(),value.getKey(),Toast.LENGTH_SHORT).show();

            if (value.getKey().compareTo("DisplayName")==0)
            {
                Toast.makeText(getApplicationContext(), value.getValue().toString(), Toast.LENGTH_SHORT).show();
            }

        }
    }

    public class NotifyforumAsync extends AsyncTask<String,Void,String>
    {

        @Override
        protected String doInBackground(String... params) {

            String forumname = params[0];

            String url = "https://fcm.googleapis.com/fcm/send";

            try {
                URL urls = new URL(url);
                HttpsURLConnection httpsURLConnection = (HttpsURLConnection) urls.openConnection();
                httpsURLConnection.setRequestMethod("POST");
                httpsURLConnection.setRequestProperty("Content-Type", "application/json");
                httpsURLConnection.setRequestProperty("Authorization", "key=AIzaSyBjwxgBRcnvbcXjIYDsbnujhphhY5AUwc4");

                JSONObject parent = new JSONObject();
                JSONObject to = new JSONObject();
                JSONObject notification = new JSONObject();
                JSONObject msg = new JSONObject();

                msg.put("body", "New Forum Created: "+forumname);
                notification.put("notification",msg);
                to.put("to","/topics/news");
                parent.put("to", "/topics/news");
                parent.put("notification", msg);



                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(httpsURLConnection.getOutputStream()));

                System.out.println(parent.toString());

                writer.write(parent.toString());
                writer.flush();
                System.out.println("Antes de reader");
                //BufferedReader reader = new BufferedReader(new InputStreamReader(httpsURLConnection.getInputStream()));

                int responsecode = httpsURLConnection.getResponseCode();

                String response = " - "+responsecode;

                //String line = "";

                //while((line=reader.readLine())!=null)
                //{
                //    response+=line;
                //}

                //reader.close();
                writer.close();

                return response;


            } catch (Exception e) {
                e.printStackTrace();
            }


            return null;
        }
    }

}
