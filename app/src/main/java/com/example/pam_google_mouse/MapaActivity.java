package com.example.pam_google_mouse;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import androidx.appcompat.app.AppCompatActivity;
import java.io.InputStream;

public class MapaActivity extends AppCompatActivity {

    private String url_screen = "http://zalcizenie.000webhostapp.com/desktop.png";
    String operacja="brak";
    ImageView image;
    TextView test1,test2;
    Button button;
    double x=10,y=10;
    ProgressDialog mProgressDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapa);

        test1=findViewById(R.id.textView3);
        test2=findViewById(R.id.textView4);
        image = (ImageView) findViewById(R.id.image);
        button = findViewById(R.id.button);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DownloadImageTask(image,operacja).execute(url_screen);
                operacja="brak";
            }
        });
    }

    public void click(View view) {
        operacja="klik";
    }
    public void dclick(View view) {
        operacja="doubleklik";
    }
    public void tzyml(View view) {
        operacja="trzymajl";
    }
    public void klikp(View view) {
        operacja="klikp";
    }



    private static class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        String operacja;
        private void sendGET() throws IOException {
        int x=200;
        int y=300;
        URL obj = new URL("https://zalcizenie.000webhostapp.com/?request=grab&x="+x+"&y="+y+"&operacja="+operacja);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("GET");
        int responseCode = con.getResponseCode();
        System.out.println("GET Response Code :: " + responseCode);
    }

    protected Bitmap doInBackground(String... urls) {

        try {
            sendGET();
        } catch (IOException e) {
            e.printStackTrace();
        }

        String urldisplay = urls[0];
        Bitmap bmp = null;
        try {
            InputStream in = new java.net.URL(urldisplay).openStream();
            bmp = BitmapFactory.decodeStream(in);
        } catch (Exception e) {
            Log.e("Error", e.getMessage());
            e.printStackTrace();
        }
        return bmp;
    }
        public DownloadImageTask(ImageView bmImage,String operand) { //konstruktor
            this.bmImage = bmImage;
            this.operacja=operand;
        }
        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }

}
