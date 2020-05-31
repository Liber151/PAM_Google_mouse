package com.example.pam_google_mouse;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;


public class MainActivity extends AppCompatActivity {

    double[] przesuniecie={0,0};
    double[] lokalizacja={0,0};
    boolean flaga=false;
    int PERMISSION_ID = 44,counter=0;
    double skala=1,wielkoscPola=1,weilkoscSkali=1,x,y;
    double r1, r2, B, Ra=0;

    FusedLocationProviderClient mFusedLocationClient;
    TextView latTextView, lonTextView, counterText, wielkoscEkranu, daneDebug;
    SeekBar wyborSkali;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        latTextView = findViewById(R.id.latTextView);
        lonTextView = findViewById(R.id.lonTextView);
        wielkoscEkranu=findViewById(R.id.wielkoscTextView);
        counterText = findViewById(R.id.counterTextView);
        wyborSkali = findViewById(R.id.ChooseRange);
        daneDebug=findViewById(R.id.daneDebug);

        wyborSkali.setProgress(99); //ustaw defaultowa wartosc slidera

        updateSkali();

        wyborSkali.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                updateSkali();  //przy kazdej zmianie stanu slidera zaktualizuj wartosci
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this); //zgarnij providera gps, wazne do ustalania polozenia
        getLastLocation();
    }

    @SuppressLint("MissingPermission")
    private void getLastLocation(){
        updateSkali();
        if (checkPermissions()) {
            if (isLocationEnabled()) {
                mFusedLocationClient.getLastLocation().addOnCompleteListener(
                        new OnCompleteListener<Location>() {
                            @Override
                            public void onComplete(@NonNull Task<Location> task) {
                                Location location = task.getResult();
                                if (location == null) {
                                    requestNewLocationData();
                                } else {
                                    if ((flaga==false) && (location.getLatitude()!=0 && location.getLongitude()!=0)){
                                        flaga=true;
                                        lokalizacja[0]=location.getLatitude();
                                        lokalizacja[1]=location.getLongitude();
                                        skala=obliczenie_skali(location.getLatitude());
                                    }
                                    przesuniecie[0]=location.getLatitude()-lokalizacja[0];
                                    przesuniecie[1]=location.getLongitude()-lokalizacja[1];
                                    latTextView.setText(przesuniecie[0]+"");
                                    lonTextView.setText(przesuniecie[1]+"");
                                    counter++;
                                    updateSkali();
                                }
                            }
                        }
                );
            } else {
                Toast.makeText(this, "Turn on location", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        } else {
            requestPermissions();
        }
    }

    public void updateSkali(){  //Generalnie, kiedy chcemy przekalkulowac dane, czyli np. pozycja myszki na ekranie w procentach, wywoulemy ta funckje
        //czyli co chwile
        weilkoscSkali=new Double(100-wyborSkali.getProgress())/100*skala;   //skala pomagajaca w konwersji rpzesuniecia ze stopni na procenty
        wielkoscPola=new Double(wyborSkali.getProgress()+1)/100*skala;      //skala okreslajaca jaki procent 1 poludnika stranowi nasze pole, 100 to 100%, czyli pole na 1 stopien geograficzny

        counterText.setText("1deg global="+String.format("%.3f",weilkoscSkali) +"km");  //debug pola
        wielkoscEkranu.setText("Wielkosc ekranu na osi poziomej: "+String.format("%.3f",wielkoscPola)+"km");

        x=przesuniecie[1]*Ra*weilkoscSkali;
        y=przesuniecie[0]*Ra*weilkoscSkali*-1;  //konwersja przesuniecia ze stopni na procenty
        daneDebug.setText(przesuniecie[1]+" "+przesuniecie[1]+" x:"+x+" y: "+y);
    }

    private double obliczenie_skali(double lat){
        r1=6378.137;        //w tej funkcji obliczam jaki obwod ma rownoleznik na ktorym stoimy
        r2=6356.752;        //nastepnie dziele na 360 zeby uzyskac wielkosc 1 stopnia na potrzeby apki
        B=lat;
        Ra=Math.sqrt(
                (Math.pow((Math.pow(r1,2)*Math.cos(B)),2)+Math.pow(Math.pow(r2,2)*Math.sin(B),2))
                /(Math.pow(r1*Math.cos(B),2)+Math.pow(r2*Math.sin(B),2))
        );//wynik w km
        Ra=Ra/360;
        return Ra;
    }

    public void podglad(View view) {
        Intent i = new Intent(getApplicationContext(), MapaActivity.class);
        i.putExtra("x",x);
        i.putExtra("y",y);
        startActivity(i);//odpal nowa aktywnosc, przeslij do niej x i y
    }

    @SuppressLint("MissingPermission")
    private void requestNewLocationData(){

        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        mLocationRequest.setInterval(5);
        mLocationRequest.setFastestInterval(200);
        mLocationRequest.setNumUpdates(5);


        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mFusedLocationClient.requestLocationUpdates(
                mLocationRequest, mLocationCallback,
                Looper.myLooper()
        );

    }

    private LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            Location mLastLocation = locationResult.getLastLocation();
        }
    };

    private boolean checkPermissions() { //sprawdz czy sa dodane pozwolenia do zdobycia loaklizacji dla fbi
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        return false;
    }

    private void requestPermissions() { //popros ladnie o pozwolenie na sledzenie
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
                PERMISSION_ID
        );
    }

    private boolean isLocationEnabled() {   //jezeli lokalizacja nie jest wlaczona, otworz menu do wlaczenia
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
                LocationManager.NETWORK_PROVIDER
        );
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_ID) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation();
            }
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        if (checkPermissions()) {
            getLastLocation();
        }

    }
    public void falseAnswer(View view) {
        getLastLocation();
    }
}