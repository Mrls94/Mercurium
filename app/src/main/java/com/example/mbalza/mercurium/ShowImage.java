package com.example.mbalza.mercurium;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

import java.io.InputStream;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class ShowImage extends AppCompatActivity {

    ImageView image;
    String url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_image);

        image = (ImageView) findViewById(R.id.imageShow);
        url = getIntent().getStringExtra("url");

        getImageasync imageasync = new getImageasync();
        imageasync.execute(url);

    }

    private class getImageasync extends AsyncTask<String,Void,Bitmap>
    {

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            image.setImageBitmap(bitmap);
        }

        @Override
        protected Bitmap doInBackground(String... params) {

            String stringurl = params[0];

            try
            {
                URL url = new URL(stringurl);
                HttpsURLConnection httpsURLConnection = (HttpsURLConnection) url.openConnection();

                InputStream inputStream = httpsURLConnection.getInputStream();
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

                return bitmap;

            }catch (Exception e)
            {e.printStackTrace();}

            return null;
        }
    }
}
