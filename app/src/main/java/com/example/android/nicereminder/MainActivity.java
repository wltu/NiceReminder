package com.example.android.nicereminder;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {
    private Context context;
    private Activity activity = this;
    private Button update;
    private Button upload;
    private Button download;
    private TextView cloudText;
    private TextView text;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private boolean SearchLocation;

    // Saved Data Information.
    public static final String SHARDED_PREFS = "sharedPref";
    public static final String LOCATION = "location";
    public static final String LOCATION_ON = "location_on";

    private String currentLocation;
    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private DatabaseReference myRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        currentLocation = "";

        text = (TextView) findViewById(R.id.text);
        cloudText = (TextView) findViewById(R.id.cloud);
        update = (Button) findViewById(R.id.button);
        upload = (Button)findViewById(R.id.upload);
        download = (Button)findViewById(R.id.download);

        context = getApplicationContext();


        // Register the listener with the Location Manager to receive location updates
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }

        SearchLocation = false;

        // Acquire a reference to the system Location Manager
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        // Define a listener that responds to location updates
        locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                currentLocation = location.getLatitude() + " : " + location.getLongitude();
                text.setText(currentLocation);
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onProviderDisabled(String provider) {
            }
        };

        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SearchLocation = !SearchLocation;

                // Register the listener with the Location Manager to receive location updates
                if(SearchLocation){
                    requestLocation();
                }else{
                    locationManager.removeUpdates(locationListener);
                    text.setText("OK");
                }
            }
        });


        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myRef.setValue(currentLocation);
            }
        });

        download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


            }
        });

        loadData();

        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        if(user == null){
            startActivity(new Intent(MainActivity.this, DatabaseActivity.class));
        }else{
            // User is signed in
            if(user.getEmail().isEmpty()){
                startActivity(new Intent(MainActivity.this, DatabaseActivity.class));
            }
        }

        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("Test");

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                String value = dataSnapshot.getValue(String.class);
                cloudText.setText(value);
            }

            @Override
            public void onCancelled(DatabaseError error) {

            }
        });
    }


    @Override
    protected void onDestroy() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARDED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString(LOCATION, text.getText().toString());
        editor.putBoolean(LOCATION_ON, SearchLocation);

        editor.commit();

        mAuth.signOut();

        super.onDestroy();
    }

    private void requestLocation(){
        text.setText("Search...");
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }

        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
    }

    private void loadData(){
        SharedPreferences sharedPreferences = getSharedPreferences(SHARDED_PREFS, MODE_PRIVATE);

        SearchLocation = sharedPreferences.getBoolean(LOCATION_ON, false);

        if(SearchLocation){
            requestLocation();
        }

        text.setText(sharedPreferences.getString(LOCATION, "Hello World!"));
    }
}
