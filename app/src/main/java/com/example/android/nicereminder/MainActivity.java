package com.example.android.nicereminder;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private Context context;
    private Activity activity = this;
    private Button update;
    private TextView text;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private boolean SearchLocation;
    private boolean startApp;

    // Saved Data Information.
    public static final String SHARDED_PREFS = "sharedPref";
    public static final String LOCATION = "location";
    public static final String LOCATION_ON = "location_on";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        text = (TextView) findViewById(R.id.text);
        update = (Button) findViewById(R.id.button);
        context = getApplicationContext();


        // Register the listener with the Location Manager to receive location updates
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }

        SearchLocation = false;
        startApp = false;
        // Acquire a reference to the system Location Manager
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        // Define a listener that responds to location updates
        locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                text.setText(location.getLatitude() + " : " + location.getLongitude());
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

        loadData();
    }

    @Override
    protected void onDestroy() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARDED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString(LOCATION, text.getText().toString());
        editor.putBoolean(LOCATION_ON, SearchLocation);

        editor.commit();

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
