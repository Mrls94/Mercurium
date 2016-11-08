package com.example.mbalza.mercurium;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.storage.StorageManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
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
    private FirebaseStorage storage;

    private ImageView imageView;
    private String downloadImageUrl;

    private DatabaseReference root;
    private String temp_key;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);

        username = getIntent().getStringExtra("user_name");
        roomname = getIntent().getStringExtra("room_name");
        imageView = (ImageView) findViewById(R.id.postImage);

        storage = FirebaseStorage.getInstance();
        progressDialog = new ProgressDialog(getApplicationContext());

        setTitle("Enviar Post a "+roomname);

        root = FirebaseDatabase.getInstance().getReference().child(roomname);

        SendPostBtn = (Button) findViewById(R.id.SendPostBtn);
        UploadFotoBtn = (Button) findViewById(R.id.UploadPictureBtn);
        textmsg = (EditText) findViewById(R.id.editTextMsg);

        SendPostBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                //Toast.makeText(getApplicationContext(),currentDateTimeString,Toast.LENGTH_SHORT).show();

                Map<String, Object> map = new HashMap<String, Object>();

                temp_key = root.push().getKey();
                root.updateChildren(map);

                StorageReference storageReference = storage.getReferenceFromUrl("gs://mercurium-a01ca.appspot.com");

                StorageReference postRef = storageReference.child(temp_key+".jpg");
                StorageReference postimageRef = storageReference.child("images/"+temp_key+".jpg");

                imageView.setDrawingCacheEnabled(true);
                imageView.buildDrawingCache();
                Bitmap bitmap = imageView.getDrawingCache();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] data = baos.toByteArray();

                UploadTask uploadTask = postRef.putBytes(data);
                progressDialog = new ProgressDialog(getApplicationContext());
                progressDialog.setTitle("Subiendo Post");
                progressDialog.setMessage("Subiendo Post");
                //progressDialog.show();
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        progressDialog.hide();
                        Toast.makeText(getApplicationContext(),"No se pudo completar el post",Toast.LENGTH_SHORT).show();

                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        progressDialog.hide();
                        Uri downloadurl = taskSnapshot.getDownloadUrl();

                        downloadImageUrl = downloadurl.toString();



                        String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());

                        DatabaseReference message_root = root.child(temp_key);
                        Map<String,Object> map2 = new HashMap<String, Object>();

                        map2.put("Name", username);
                        map2.put("Msg", textmsg.getText().toString());
                        map2.put("Date",currentDateTimeString);
                        map2.put("imageurl", downloadImageUrl);

                        message_root.updateChildren(map2);


                        System.out.println(" get path  "+downloadurl.getPath());
                        System.out.println(" get host  "+downloadurl.getHost());
                        System.out.println(" to string  "+downloadurl.toString());
                        //Toast.makeText(getApplicationContext(),downloadurl.getPath(),Toast.LENGTH_SHORT).show();

                    }
                });



                SendNotifications sendNotifications = new SendNotifications();
                sendNotifications.execute(roomname,textmsg.getText().toString(),username);

                finish();
            }
        });

        UploadFotoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new   Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, 2);

            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 2 && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = { MediaStore.Images.Media.DATA };

            Cursor cursor = getContentResolver().query(selectedImage,filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();
            // String picturePath contains the path of selected Image

            // Show the Selected Image on ImageView

            imageView.setImageBitmap(BitmapFactory.decodeFile(picturePath));

        }

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
