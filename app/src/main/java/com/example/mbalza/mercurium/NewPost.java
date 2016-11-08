package com.example.mbalza.mercurium;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

public class NewPost extends AppCompatActivity {

    Button SendPostBtn, UploadFotoBtn;
    EditText textmsg;

    private String username, roomname;

    private DatabaseReference root;
    private String temp_key;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);

        username = getIntent().getStringExtra("user_name");
        roomname = getIntent().getStringExtra("room_name");

        setTitle("Enviar Post a "+roomname);

        root = FirebaseDatabase.getInstance().getReference().child(roomname);

        SendPostBtn = (Button) findViewById(R.id.SendPostBtn);
        UploadFotoBtn = (Button) findViewById(R.id.UploadPictureBtn);
        textmsg = (EditText) findViewById(R.id.editTextMsg);

        SendPostBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Map<String, Object> map = new HashMap<String, Object>();

                temp_key = root.push().getKey();
                root.updateChildren(map);

                String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());

                //Toast.makeText(getApplicationContext(),currentDateTimeString,Toast.LENGTH_SHORT).show();

                DatabaseReference message_root = root.child(temp_key);
                Map<String,Object> map2 = new HashMap<String, Object>();

                map2.put("Name", username);
                map2.put("Msg", textmsg.getText().toString());
                map2.put("Date",currentDateTimeString);

                message_root.updateChildren(map2);

                SendNotifications sendNotifications = new SendNotifications();
                sendNotifications.execute(roomname,textmsg.getText().toString(),username);

                finish();
            }
        });

    }

    public class SendNotifications extends AsyncTask<String, Void, String>
    {
        //ProgressDialog progressDialog;

        @Override
        protected void onPostExecute(String aVoid) {
            super.onPostExecute(aVoid);
            //Toast.makeText(getApplicationContext(),aVoid,Toast.LENGTH_SHORT).show();

            //progressDialog.hide();

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //progressDialog = new ProgressDialog(getApplicationContext());
            //progressDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {

            String roomname = params[0];
            String message = params[1];
            String usersent = params[2];
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



                msg.put("body", roomname+ " - "+usersent+" dice: "+message);
                notification.put("notification",msg);
                roomname = roomname.replace(" ","_");
                to.put("to","/topics/"+roomname);
                parent.put("to", "/topics/"+roomname);
                parent.put("notification", msg);

                String post = "{ " +
                        "\"to\": \"/topics/news\", " +
                        "\"data\": " +
                        "{ \"message\": "+roomname+ " - "+usersent+" dice: "+message+"\",  " +
                        "}  " +
                        "}";

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
