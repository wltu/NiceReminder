package com.example.android.nicereminder;

import android.Manifest;
import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;

import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

// TODO: Profile Pictures
/*
    Allow user to access photo local photo gallery to set as profile
    Add sign up option with name...
 */

public class MainScreen extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static FragmentManager fragmentManager;
    private static Context context;
    private Activity activity = this;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private boolean SearchLocation;

    // Saved Data Information.
    public static final String SHARDED_PREFS = "sharedPref";
    public static final String LOCATION = "location";
    public static final String LOCATION_ON = "location_on";

    private String currentLocation;
    private static FirebaseAuth mAuth;
    private static FirebaseDatabase database;
    private DatabaseReference myRef;
    private static StorageReference mStorageRef;

    private static File profileFile = null;

    // Account Variables
    private NavigationView navigationView;
    private Menu navMenu;
    private static MenuItem signin;
    private static MenuItem signup_setting;
    private static MenuItem signup;
    private static MenuItem signin_setting;
    private static MenuItem signout;
    private static MenuItem signout_setting;
    private static ImageView user_image;
    private static TextView user_name;
    private static TextView user_email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fragmentManager = getFragmentManager();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        currentLocation = "";


        navMenu = navigationView.getMenu();
        signin = navMenu.findItem(R.id.nav_signin);
        signout = navMenu.findItem(R.id.nav_signout);
        signup = navMenu.findItem(R.id.nav_signup);


        LinearLayout layout = (LinearLayout) (navigationView).getHeaderView(0);
        user_image = (ImageView) layout.getChildAt(0);
        user_name = (TextView)layout.getChildAt(1);
        user_email = (TextView)layout.getChildAt(2);


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
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onProviderDisabled(String provider) {
            }
        };



        loadData();

        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();


        database = FirebaseDatabase.getInstance();

//        myRef = database.getReference("Test");
//
//
//        myRef.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                // This method is called once with the initial value and again
//                // whenever data at this location is updated.
//                String value = dataSnapshot.getValue(String.class);
//
//            }
//
//            @Override
//            public void onCancelled(DatabaseError error) {
//
//            }
//        });

        mStorageRef = FirebaseStorage.getInstance().getReference();


    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.

        getMenuInflater().inflate(R.menu.main_screen, menu);

        signup_setting = menu.findItem(R.id.action_signup);
        signin_setting = menu.findItem(R.id.action_signin);
        signout_setting = menu.findItem(R.id.action_signout);


        FirebaseUser user = mAuth.getCurrentUser();   
        if(user == null){                                                                       
            //startActivity(new Intent(MainScreen.this, LoginActivity.class));
            UpdateAccountStatus(false);
        }else{                                                                                  
            // User is signed in                                                                
            if(user.getEmail().isEmpty()){                                                      
                //startActivity(new Intent(MainScreen.this, LoginActivity.class));
                UpdateAccountStatus(false);
            }else{                                                                              
                UpdateAccountStatus(true);                                                      
            }                                                                                   
        }                                                                                       

        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        Intent intent;
        //noinspection SimplifiableIfStatement
        switch(id) {
            case R.id.action_settings:
                if(mAuth.getCurrentUser() != null){
                    navigationView.setCheckedItem(R.id.nav_manage);
                    //getSupportFragmentManager().beginTransaction().replace(R.id.activity_mainscreen, new Settings()).commit();
                    getFragmentManager().beginTransaction().replace(R.id.activity_mainscreen, new Settings()).commit();
                }

                return true;
            case R.id.action_signout:
                SignOut();

                return true;
            case R.id.action_signin:
                intent = new Intent(MainScreen.this, LoginActivity.class);
                intent.putExtra("Signup", false);
                startActivity(intent);

                return true;
            case R.id.action_signup:
                intent = new Intent(MainScreen.this, LoginActivity.class);
                intent.putExtra("Signup", true);
                startActivity(intent);

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        Intent intent;

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {
            if(mAuth.getCurrentUser() != null){
                //getSupportFragmentManager().beginTransaction().replace(R.id.activity_mainscreen, new Settings()).commit();

                getFragmentManager().beginTransaction().replace(R.id.activity_mainscreen, new Settings()).commit();
            }
        } else if (id == R.id.nav_signin) {
            intent = new Intent(MainScreen.this, LoginActivity.class);
            intent.putExtra("Signup", false);
            startActivity(intent);

        } else if (id == R.id.nav_signout) {
            SignOut();
        } else if(id == R.id.nav_signup){
            intent = new Intent(MainScreen.this, LoginActivity.class);
            intent.putExtra("Signup", true);
            startActivity(intent);

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onDestroy() {
//        SharedPreferences sharedPreferences = getSharedPreferences(SHARDED_PREFS, MODE_PRIVATE);
//        SharedPreferences.Editor editor = sharedPreferences.edit();
//
//        editor.putString(LOCATION, text.getText().toString());
//        editor.putBoolean(LOCATION_ON, SearchLocation);

//        editor.commit();

        //mAuth.signOut();

        super.onDestroy();
    }

    private void requestLocation(){
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }

        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
    }

    private void loadData(){
//        SharedPreferences sharedPreferences = getSharedPreferences(SHARDED_PREFS, MODE_PRIVATE);
//
//        SearchLocation = sharedPreferences.getBoolean(LOCATION_ON, false);
//
//        if(SearchLocation){
//            requestLocation();
//        }
//
//        text.setText(sharedPreferences.getString(LOCATION, "Hello World!"));
    }

    public static void UpdateAccountStatus(boolean signined){
        signup.setVisible(!signined);
        signup_setting.setVisible(!signined);
        signin_setting.setVisible(!signined);
        signin.setVisible(!signined);
        signout_setting.setVisible(signined);
        signout.setVisible(signined);

        if(signined){
            String email = mAuth.getCurrentUser().getEmail();
            user_email.setText(email);

            String temp = email.replace('.', ' ');
            DatabaseReference ref = database.getReference(temp);

            ref.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    // This method is called once with the initial value and again
                    // whenever data at this location is updated.
                    user_name.setText(dataSnapshot.getValue(String.class));
                }

                @Override
                public void onCancelled(DatabaseError error) {

                }
            });


            if(profileFile == null){
                try {
                    StorageReference mref = mStorageRef.child("User/" + email +  "/profile.jpg");
                    profileFile = File.createTempFile("profile", "jpg");

                    mref.getFile(profileFile)
                            .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                    // Successfully downloaded data to local file
                                    // ...
                                    try {
                                        user_image.setImageBitmap(BitmapFactory.decodeStream(new FileInputStream(profileFile)));
                                    } catch (FileNotFoundException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            user_image.setImageResource(R.mipmap.ic_launcher_round);
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }else{
                try {
                    user_image.setImageBitmap(BitmapFactory.decodeStream(new FileInputStream(profileFile)));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }else{
            fragmentManager.beginTransaction().replace(R.id.activity_mainscreen, new SignedOut()).commit();

            user_image.setImageResource(R.mipmap.ic_launcher_round);
            user_name.setText(R.string.nav_header_title);
            user_email.setText(R.string.nav_header_subtitle);
        }
    }

    public static String getName(){
        return user_name.getText().toString();
    }

    public static void updateName(String name){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference(mAuth.getCurrentUser().getEmail().replace('.', ' '));

        myRef.setValue(name);

        user_name.setText(name);


        Toast.makeText(context, "Name Changed!", Toast.LENGTH_SHORT).show();
    }

    public static void SignOut(){

        mAuth.signOut();
        profileFile = null;
        UpdateAccountStatus(false);

        Toast.makeText(context, "Signed Out", Toast.LENGTH_SHORT).show();
    }

    public static void changePassword(String password){
        mAuth.getCurrentUser().updatePassword(password);

        Toast.makeText(context, "Password Changed!", Toast.LENGTH_SHORT).show();
    }

    public static void deleteAccount(){
        mAuth.getCurrentUser().delete();
        mAuth.signOut();
        profileFile = null;

        UpdateAccountStatus(false);
        Toast.makeText(context, "Deleted Account!", Toast.LENGTH_SHORT).show();
    }
}
