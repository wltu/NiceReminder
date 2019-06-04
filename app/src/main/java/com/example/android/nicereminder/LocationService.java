package com.example.android.nicereminder;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LocationService extends Service {
    private int id;
    private double latitude;
    private double longitude;
    private boolean newLocation;


    /*
    Each grid is 0.0002 by 0.0002 degree... Within the grid there is 0.0001 margin on each side before changing grid.
    Each grid is defined by its bottom left corner.
    */
    public static final double FAR_DISTANCE = 0.0003;  // Change Margin...
    public static final double NEAR_DISTANCE = 0.0001;  // Change Margin...


    private LocationListener listener;
    private LocationManager locationManager;
    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private DatabaseReference dataref;


    public LocationService() {
        id = 0;
        newLocation = false;
        latitude = -1;
        longitude = -1;

        mAuth = FirebaseAuth.getInstance();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        onTaskRemoved(intent);
        return START_STICKY;
    }

    @Override
    public void onCreate() {

        listener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Log.e("Change", "Location");
                double lat = location.getLatitude();
                double lon = location.getLongitude();

                if((latitude != -1 && longitude != -1)){
                    if(((latitude - lat > NEAR_DISTANCE) || ((longitude - lon) > NEAR_DISTANCE)) || (lat - latitude > FAR_DISTANCE) || ((lon - longitude) > FAR_DISTANCE)){

                        int a = (int)(lat * 10000);
                        int b = (int)(lon * 10000);
                        latitude = (a - Math.abs(a % 2)) / 10000.0;
                        longitude = (b - Math.abs(b % 2)) / 10000.0;

                        mAuth = FirebaseAuth.getInstance();
                        database = FirebaseDatabase.getInstance();

                        if(mAuth.getCurrentUser() != null) {
                            dataref = database.getReference("user").child(mAuth.getCurrentUser().getEmail().replace('.', ' '))
                                    .child("gallery").child(("" + latitude).replace('.', ' '))
                                    .child(("" + longitude).replace('.', ' '));
                            dataref.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.getValue() == null) {
                                        dataref.setValue("");
                                    } else {
                                        if (dataSnapshot.getValue(String.class).length() > 0)
                                            sendNotification();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        }
                    }
                }else if(latitude == -1 && longitude == -1){
                    int a = (int)(lat * 10000);
                    int b = (int)(lon * 10000);
                    latitude = (a - Math.abs(a % 2)) / 10000.0;
                    longitude = (b - Math.abs(b % 2)) / 10000.0;
                }

//                latitude = 34.4182;
//                longitude = -119.8632;
                Intent i = new Intent("location_update");
                i.putExtra("longitude", "" + longitude);
                i.putExtra("latitude", "" + latitude);
                sendBroadcast(i);
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {
                Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
            }
        };

        locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);

        //noinspection MissingPermission
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,2000,0,listener);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(locationManager != null){
            //noinspection MissingPermission
            locationManager.removeUpdates(listener);
        }
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Intent restart = new Intent(getApplicationContext(), this.getClass());
        restart.setPackage(getPackageName());
        startService(restart);
        super.onTaskRemoved(rootIntent);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private void sendNotification() {
        String CHANNEL_ID = "CHANNEL";
        CharSequence name = "NICE CHANNEL";
        String Description = "Very nice channel";


        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {

            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
            mChannel.setDescription(Description);
            mChannel.enableLights(true);
            mChannel.setLightColor(Color.RED);
            mChannel.enableVibration(true);
            mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            mChannel.setShowBadge(true);

            if (notificationManager != null) {

                notificationManager.createNotificationChannel(mChannel);
            }

        }


        Intent intent =  new Intent(getApplicationContext(), MainScreen.class);
        intent.setAction("restart");
        intent.putExtra("longitude", "" + longitude);
        intent.putExtra("latitude", "" + latitude);

        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);


        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);


        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.nicereminder)
                .setContentTitle("Location Gallery")
                .setContentText("You have been here before! Click here to view the gallery from this location!")
                .setDefaults(Notification.DEFAULT_ALL)
                .setPriority(Notification.PRIORITY_HIGH)
                .setContentIntent(pendingIntent);


        if (notificationManager != null) {
            Notification notification = builder.build();
            notification.flags |= Notification.FLAG_AUTO_CANCEL;
            notificationManager.notify(id, notification);

            if(++id < 0){
                id = 0;
            }
        }
    }
}


