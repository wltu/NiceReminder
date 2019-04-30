package com.example.android.nicereminder;

import android.Manifest;
import android.app.Activity;
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

    private Context context;
    private Activity activity = this;
    private Button update;
    private Button upload;
    private Button download;
    private TextView cloudText;
    private TextView text;
    private ImageView image;
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

    private static File localFile = null;

    // Account Variables
    private Menu menu;
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

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        currentLocation = "";

        text = (TextView) findViewById(R.id.text);
        cloudText = (TextView) findViewById(R.id.cloud);
        update = (Button) findViewById(R.id.button);
        upload = (Button)findViewById(R.id.upload);
        download = (Button)findViewById(R.id.download);
        image = (ImageView)findViewById(R.id.image);

        navMenu = ((NavigationView)findViewById(R.id.nav_view)).getMenu();
        signin = navMenu.findItem(R.id.nav_signin);
        signout = navMenu.findItem(R.id.nav_signout);
        signup = navMenu.findItem(R.id.nav_signup);

        LinearLayout layout = (LinearLayout) (((NavigationView)findViewById(R.id.nav_view)).getHeaderView(0));
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

                try {

                    StorageReference ref = mStorageRef.child("tzuyu.jpg");
                    localFile = File.createTempFile("tzuyu", "jpg");

                    ref.getFile(localFile)
                            .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                    // Successfully downloaded data to local file
                                    // ...
                                    try {
                                        image.setImageBitmap(BitmapFactory.decodeStream(new FileInputStream(localFile)));
                                    } catch (FileNotFoundException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            // Handle failed download
                            // ...

                            Log.e("ERROR", "Download Failed!");
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }


            }
        });

        loadData();

        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();


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

        this.menu = menu;

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

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_signin) {
            startActivity(new Intent(MainScreen.this, LoginActivity.class));
        } else if (id == R.id.nav_signout) {
            SignOut();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onDestroy() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARDED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString(LOCATION, text.getText().toString());
        editor.putBoolean(LOCATION_ON, SearchLocation);

        editor.commit();

        //mAuth.signOut();

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

    private void SignOut(){
        mAuth.signOut();

        UpdateAccountStatus(false);

        Toast.makeText(getApplicationContext(), "Signed Out", Toast.LENGTH_SHORT).show();
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


            try {

                StorageReference mref = mStorageRef.child("User/" + email +  "/profile.jpg");
                localFile = File.createTempFile("profile", "jpg");

                mref.getFile(localFile)
                        .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                // Successfully downloaded data to local file
                                // ...
                                try {
                                    user_image.setImageBitmap(BitmapFactory.decodeStream(new FileInputStream(localFile)));
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

        }
    }
}
