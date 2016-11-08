package com.example.mbalza.mercurium;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener{

    private Button addButton;
    private EditText room_name;

    private ListView listView;
    private ArrayAdapter<String> arrayAdapter;
    private ArrayList<String> list_of_rooms = new ArrayList<>();

    private DatabaseReference root = FirebaseDatabase.getInstance().getReference().getRoot();
    private DatabaseReference currentUserRoot;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private String nonameerror = "Error_5678 No NAME";
    private String name = nonameerror;

    private GoogleApiClient mGoogleApiClient;
    private GoogleSignInOptions gso;
    private static final int RC_SIGN_IN = 5678;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        addButton = (Button) findViewById(R.id.AddChatButton);
        room_name = (EditText) findViewById(R.id.ChatName);
        listView = (ListView) findViewById(R.id.listview);

        arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,list_of_rooms);

        listView.setAdapter(arrayAdapter);

        mAuth = FirebaseAuth.getInstance();



        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestIdToken(getString(R.string.webClient_id))
                .build();


        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        if (name.compareTo(nonameerror)==0)
        {
            request_user_name();
        }


        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {



                Intent i = new Intent(getApplicationContext(),NewForum.class);
                if(room_name.getText().toString().length()>1)
                {
                    i.putExtra("chatname", room_name.getText().toString());
                    i.putExtra("username",name);
                    i.putExtra("uid",FirebaseAuth.getInstance().getCurrentUser().getUid());
                    startActivity(i);
                }
                else
                {
                    Toast.makeText(getApplicationContext(),"Foro sin nombre", Toast.LENGTH_SHORT).show();
                }

                //Map<String, Object> map = new HashMap<String, Object>();
                //map.put(room_name.getText().toString(),"" );
                //root.updateChildren(map);

            }
        });
        /*
        root.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                Set<String> set = new HashSet<String>();
                Iterator i = dataSnapshot.getChildren().iterator();

                while(i.hasNext())
                {
                    set.add(((DataSnapshot)i.next()).getKey());
                }

                list_of_rooms.clear();
                list_of_rooms.addAll(set);

                arrayAdapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        }); */


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {


                String roomname = ((TextView)view).getText().toString();

                roomname = roomname.replace(" ", "_");

                FirebaseMessaging.getInstance().subscribeToTopic(roomname);
                Intent i = new Intent(getApplicationContext(), Chat_Room.class);
                Intent y = new Intent(getApplicationContext(),newChatroom.class);
                i.putExtra("room_name", ((TextView)view).getText().toString());
                i.putExtra("user_name",name);
                y.putExtra("room_name", ((TextView)view).getText().toString());
                y.putExtra("user_name",name);
                startActivity(i);

            }
        });


        mAuthListener  = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user==null)
                {
                    request_user_name();
                }
            }
        };

    }

    private void request_user_name() {

        AlertDialog.Builder builder =  new AlertDialog.Builder(this);

        builder.setTitle("Sign In: ");

        final EditText input_field = new EditText(this);
        final SignInButton googlebutton = new SignInButton(this);
        googlebutton.setScopes(gso.getScopeArray());
        googlebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
                startActivityForResult(signInIntent, RC_SIGN_IN);
            }
        });

        builder.setView(googlebutton);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                if(name.compareTo(nonameerror)==0)
                {
                    dialog.cancel();
                    request_user_name();
                }

            }
        });

        builder.show();

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            //Toast.makeText(this,"Entered onActivityResult",Toast.LENGTH_SHORT).show();
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }

    private void handleSignInResult(GoogleSignInResult result) {

        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount acct = result.getSignInAccount();
            //mStatusTextView.setText(getString(R.string.signed_in_fmt, acct.getDisplayName()));
            //updateUI(true);
            name = acct.getDisplayName();
            Toast.makeText(this,"Welcome "+acct.getDisplayName(),Toast.LENGTH_SHORT).show();
            fireBaseGoogleSignin(acct);


        } else {

            Toast.makeText(this,"no success"+result.getStatus(),Toast.LENGTH_LONG).show();

            // Signed out, show unauthenticated UI.
            //updateUI(false);
        }

    }

    private void fireBaseGoogleSignin(GoogleSignInAccount acct) {

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if(!task.isSuccessful())
                {
                    Toast.makeText(getApplicationContext(),"Failed Auth Firebase"+task.getException(),Toast.LENGTH_SHORT).show();
                    System.out.println(task.getException());
                }
                else
                {
                    updateRoot();
                    FirebaseMessaging.getInstance().subscribeToTopic("news");
                    System.out.println("Subscribed");
                }

            }
        });

    }

    private void updateRoot() {

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        DatabaseReference userroot = FirebaseDatabase.getInstance().getReference().child("Users");
        userroot.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                checkUser(dataSnapshot);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    public void checkUser(DataSnapshot datasnapchot)
    {
        Iterator i = datasnapchot.getChildren().iterator();
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        Boolean flag = false;

        while (i.hasNext() && !flag)
        {
            if(uid.compareTo( ((DataSnapshot) i.next()).getKey() ) == 0 )
            {
                flag = true;

                currentUserRoot = root.child("Users").child(uid).child("Forums");

            }
        }

        if(!flag)
        {
            Map<String,Object> map = new HashMap<>();

            map.put(uid,"");

            root.child("Users").updateChildren(map);

            DatabaseReference userroot = root.child("Users").child(uid);


            Map<String,Object> map2 = new HashMap<>();
            map2.put("DisplayName",FirebaseAuth.getInstance().getCurrentUser().getDisplayName());

            Map<String,Object> map4 = new HashMap<>();
            map4.put("Forums","");

            userroot.updateChildren(map2);

            Map<String,Object> map3 = new HashMap<>();
            map3.put("General","");
            userroot.child("Forums").updateChildren(map3);

            currentUserRoot = userroot.child("Forums");


        }

        currentUserRoot.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                Set<String> set = new HashSet<String>();
                Iterator i = dataSnapshot.getChildren().iterator();

                while(i.hasNext())
                {
                    //set.add(((DataSnapshot)i.next()).getValue().toString());
                    set.add(((DataSnapshot)i.next()).getKey());
                }

                list_of_rooms.clear();
                list_of_rooms.addAll(set);

                arrayAdapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    @Override
    protected void onStart() {
        super.onStart();

        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }


}
