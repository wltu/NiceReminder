package com.example.android.nicereminder;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;

import android.support.v4.app.NotificationCompat;
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
import com.google.firebase.storage.UploadTask;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainScreen extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {


    private String TAG = "Permission";

    private Bitmap image;
    private Uri imageTaken;
    private String mCameraFileName;
    private String newPicFile;

    private static String files = null;

    private static FragmentManager fragmentManager;
    private static Context context;
    private Activity activity = this;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private boolean newLocation;

    // Saved Data Information.
    public static final String SHARDED_PREFS = "sharedPref";
    public static final String LATITUDE = "latitude";
    public static final String LONGITUDE = "longitude";
    public static final double FAR_DISTANCE = 0.0003;  // Change Margin...
    public static final double NEAR_DISTANCE = 0.0001;  // Change Margin...
    /*
        Each grid is 0.0002 by 0.0002 degree... Within the grid there is 0.0001 margin on each side before changing grid.
        Each grid is defined by its bottom left corner.
     */

    private String currentLocation;
    private static double latitude = -1;
    private static double longitude  = -1;
    private TextView locationTest;

    private Intent locationIntent = null;
    private Intent uploadIntent = null;
    private Intent downloadIntent = null;

    // Database Variables
    private static FirebaseAuth mAuth;
    private static FirebaseDatabase database;
    private static DatabaseReference dataref = null;
    private static StorageReference mStorageRef;

    private static File profileFile = null;


    private boolean inGallery;
    private String name;


    // Account Variables
    private NavigationView navigationView;
    private Menu navMenu;

    private MenuItem delete_setting;
    private MenuItem delete_setting_button;


    private static MenuItem signin;
    private static MenuItem signup_setting;
    private static MenuItem signup;
    private static MenuItem signin_setting;
    private static MenuItem signout;
    private static MenuItem signout_setting;
    private static ImageView user_image;
    private static TextView user_name;
    private static TextView user_email;


    private int id;
    private boolean restart;

    private BackgroundTask backgroundTask;
    private BackgroundTask uploadTask;
    private BackgroundTask locationTask;

    private class BackgroundTask extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent != null){
                if(intent.getAction().equals("upload")){
                    Log.e("Restart Location", "Nice");
                    stopService(uploadIntent);
                }else if(intent.getAction().equals("download")){
                    Log.d("Download", "OK");

                    if(downloadIntent != null) {
                        stopService(downloadIntent);
                        if (mAuth.getCurrentUser() != null && (restart | inGallery)) {
                            try {
                                delete_setting.setVisible(true);

                                inGallery = true;
                                Bundle bundle = new Bundle();
                                bundle.putString("files", files);
                                Gallery fragmet = new Gallery();
                                fragmet.setArguments(bundle);
                                fragmentManager.beginTransaction().replace(R.id.activity_mainscreen, fragmet).commit();
                            } catch (IllegalStateException e) {
                                delete_setting.setVisible(false);

                                Log.e("Error", "ok");
                            }
                        }
                    }

                    restart = false;
                }else if(intent.getAction().equals("location_update")){
                    double lat = Double.parseDouble(intent.getStringExtra("latitude"));
                    double lon = Double.parseDouble(intent.getStringExtra("longitude"));
                    currentLocation = lat + " : " + lon;

                    if((latitude != -1 && longitude != -1)){
                        if(lat != latitude | lon != longitude){
                            latitude = lat;
                            longitude = lon;

//                            newLocation = true;
                            Log.d("Change Location", "0");
                            newLocation = true;
                            getLocationGallery();
                        }
                    }else if(latitude == -1 && longitude == -1){
                        latitude = lat;
                        longitude = lon;

                        Log.d("Change Location", "1");
                        newLocation = true;
                        getLocationGallery();
                     }

                    locationTest.setText(currentLocation + "\n" + latitude + ", " + longitude);

                }
            }
        }
    }

    private void getLocationGallery() {
        if(mAuth.getCurrentUser() != null) {
            dataref = database.getReference("user").child(mAuth.getCurrentUser().getEmail().replace('.', ' '))
                    .child("gallery").child(("" + latitude).replace('.', ' '))
                    .child(("" + longitude).replace('.', ' '));
            dataref.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getValue() == null) {
                        dataref.setValue("");
                        newLocation = false;
                    } else {
                        if(newLocation && (files == null || !files.equals(dataSnapshot.getValue(String.class)))) {
                            Log.e("Download", "NO");

                            files = dataSnapshot.getValue(String.class);

                            downloadIntent = new Intent(context, DownloadService.class);
                            downloadIntent.setAction("download");
                            downloadIntent.putExtra("files", dataSnapshot.getValue(String.class));
                            downloadIntent.putExtra("latitude", ("" + latitude).replace('.', ' '));
                            downloadIntent.putExtra("longitude", ("" + longitude).replace('.', ' '));
                            startService(downloadIntent);
                        }

                        newLocation = false;
                    }

                    dataref.removeEventListener(this);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);

        id = 0;
        newLocation = true;
        restart = false;
        inGallery = false;

        setContentView(R.layout.activity_main_screen);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        locationTest = findViewById(R.id.location);
        fragmentManager = getFragmentManager();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mAuth.getCurrentUser() != null) {
                    fragmentManager.beginTransaction().replace(R.id.activity_mainscreen, new Camera()).commit();
                    takePicture();
                }
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);




        // Menu Items
        navMenu = navigationView.getMenu();
        signin = navMenu.findItem(R.id.nav_signin);
        signout = navMenu.findItem(R.id.nav_signout);
        signup = navMenu.findItem(R.id.nav_signup);


        // Profile Information
        LinearLayout layout = (LinearLayout) (navigationView).getHeaderView(0);
        user_image = (CircleImageView) layout.getChildAt(0);
        user_name = (TextView)layout.getChildAt(1);
        user_email = (TextView)layout.getChildAt(2);


        context = getApplicationContext();

        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();

        database = FirebaseDatabase.getInstance();

        mStorageRef = FirebaseStorage.getInstance().getReference();

        FirebaseUser user = mAuth.getCurrentUser();
        if(user != null && profileFile == null){
            try {
                StorageReference mref = mStorageRef.child("User/" + user.getEmail() +  "/profile.jpg");
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
        }

        // Reset to New Location...
        if(getIntent().getAction() != null && getIntent().getAction().equals("restart")){
            latitude = Double.parseDouble(getIntent().getStringExtra(LATITUDE));
            longitude = Double.parseDouble(getIntent().getStringExtra(LONGITUDE));

            restart = true;
            locationTest.setText(latitude + ": " + longitude);
            Log.d("Change Location", "2");
            newLocation = true;
            getLocationGallery();
        }else {
            // Load Last Known Location...
            loadData();
        }

        IsPermissionGranted();
    }

    @Override
    protected void onStart() {
        super.onStart();

        sendNotification();
        // Register the listener with the Location Manager to receive location updates

        if(mAuth.getCurrentUser() != null){
            Log.d("latitude", "" + latitude);
            Log.d("longitude", "" + longitude);
            String temp = mAuth.getCurrentUser().getEmail().replace('.', ' ');
            dataref = database.getReference("user").child(temp).child("gallery").child(("" + latitude).replace('.', ' ')).child(("" + longitude).replace('.', ' '));

            dataref.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.getValue() == null){
                        dataref.setValue("");
                    }else{
                        // This method is called once with the initial value and again
                        // whenever data at this location is updated.
                        if(files == null){
//                            Intent intent =  new Intent(context, DownloadService.class);
//                            intent.setAction("download");
//                            intent.putExtra("files", dataSnapshot.getValue(String.class));
//                            intent.putExtra("latitude", ("" + latitude).replace('.', ' '));
//                            intent.putExtra("longitude",("" + longitude).replace('.', ' '));
//                            startService(intent);
                        }

                        files = dataSnapshot.getValue(String.class);
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {

                }
            });
        }

        IntentFilter filter = new IntentFilter();
        filter.addAction("download");


        backgroundTask = new BackgroundTask();

        registerReceiver(backgroundTask, filter);

        uploadTask = new BackgroundTask();
        filter = new IntentFilter();
        filter.addAction("upload");

        registerReceiver(uploadTask, filter);


        filter = new IntentFilter();
        filter.addAction("location_update");
        locationTask = new BackgroundTask();
        registerReceiver(locationTask, filter);
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

        delete_setting = menu.findItem(R.id.action_delete);
        delete_setting_button = menu.findItem(R.id.action_delete_button);
        delete_setting.setVisible(false);
        delete_setting_button.setVisible(false);

        signup_setting = menu.findItem(R.id.action_signup);
        signin_setting = menu.findItem(R.id.action_signin);
        signout_setting = menu.findItem(R.id.action_signout);

        fragmentManager = getFragmentManager();

        FirebaseUser user = mAuth.getCurrentUser();
        if(user == null){
            UpdateAccountStatus(false);
        }else{
            // User is signed in
            if(user.getEmail().isEmpty()){
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

        delete_setting.setVisible(false);
        inGallery = false;

        switch(id) {
            case R.id.action_delete_button:
                delete_setting.setVisible(true);

                AlertDialog.Builder alert = new AlertDialog.Builder(this);

                alert.setTitle("Delete Images?");


                alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                });

                alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        files = Gallery.DeleteSelected();
                        dataref.setValue(files);

                        delete_setting.setVisible(true);

                        Bundle bundle = new Bundle();
                        bundle.putString("files", files);
                        Gallery fragmet = new Gallery();
                        fragmet.setArguments(bundle);

                        fragmentManager.beginTransaction().replace(R.id.activity_mainscreen, fragmet).commit();

                        delete_setting.setTitle(R.string.action_delete);
                        delete_setting_button.setVisible(false);
                    }
                });

                alert.show();
                return true;
            case R.id.action_delete:
                delete_setting.setVisible(true);

                if(Gallery.delete){
                    setDeleteOption();
                }else{
                    delete_setting.setTitle(R.string.action_delete_cancel);
                    delete_setting_button.setVisible(true);
                    Gallery.delete = true;
                }


                return true;
            case R.id.action_settings:
                setDeleteOption();

                if(mAuth.getCurrentUser() != null){
                    navigationView.setCheckedItem(R.id.nav_manage);
                    //getSupportFragmentManager().beginTransaction().replace(R.id.activity_mainscreen, new Settings()).commit();
                    getFragmentManager().beginTransaction().replace(R.id.activity_mainscreen, new Settings()).commit();
                }

                return true;
            case R.id.action_signout:
                setDeleteOption();
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

    private void setDeleteOption() {
        Gallery.CancelSelect();
        delete_setting.setTitle(R.string.action_delete);
        delete_setting_button.setVisible(false);
        Gallery.delete = false;
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        Intent intent;

        delete_setting.setVisible(false);

        inGallery = false;

        if (id == R.id.nav_camera) {
            setDeleteOption();
            if(mAuth.getCurrentUser() != null) {
                fragmentManager.beginTransaction().replace(R.id.activity_mainscreen, new Camera()).commit();
                takePicture();
            }
        } else if (id == R.id.nav_gallery) {
            if(mAuth.getCurrentUser() != null) {
                inGallery = true;
                delete_setting.setVisible(true);

                Bundle bundle = new Bundle();
                bundle.putString("files", files);
                Gallery fragmet = new Gallery();
                fragmet.setArguments(bundle);

                fragmentManager.beginTransaction().replace(R.id.activity_mainscreen, fragmet).commit();
            }
        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {
            setDeleteOption();
            if(mAuth.getCurrentUser() != null){
                getFragmentManager().beginTransaction().replace(R.id.activity_mainscreen, new Settings()).commit();
            }
        } else if (id == R.id.nav_signin) {
            intent = new Intent(MainScreen.this, LoginActivity.class);
            intent.putExtra("Signup", false);
            startActivity(intent);

        } else if (id == R.id.nav_signout) {
            setDeleteOption();
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

    private void takePicture() {
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        Intent intent = new Intent();
        intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);


        newPicFile = Calendar.getInstance().getTimeInMillis()+ ".jpg";
        String outPath = "/sdcard/" + newPicFile;
        File outFile = new File(outPath);

        mCameraFileName = outFile.toString();
        Uri outuri = Uri.fromFile(outFile);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, outuri);
        startActivityForResult(intent, 1);

        imageTaken = null;
    }

    @Override
    protected void onPause() {
        super.onPause();

        unregisterReceiver(backgroundTask);
        unregisterReceiver(uploadTask);
        unregisterReceiver(locationTask);
    }

    @Override
    protected void onDestroy() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARDED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        Log.d("Store", "" + latitude);
        Log.d("Store", "" + longitude);
        editor.putString(LATITUDE, "" + latitude);
        editor.putString(LONGITUDE, "" + longitude);

        editor.commit();

        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 1 && resultCode == RESULT_OK){
            Log.d("Picture", "Taken");
            //takePicture();

            uploadIntent =  new Intent(context, DownloadService.class);
            uploadIntent.setAction("upload");
            uploadIntent.putExtra("files", files);
            uploadIntent.putExtra("image", mCameraFileName);
            uploadIntent.putExtra("name", newPicFile);
            uploadIntent.putExtra("latitude", ("" + latitude).replace('.', ' '));
            uploadIntent.putExtra("longitude",("" + longitude).replace('.', ' '));

            stopService(downloadIntent);
            startService(uploadIntent);
        }else if(requestCode == 2 && resultCode == RESULT_OK){
            Uri image = data.getData();

            if(image != null){
                try {
                    user_image.setImageBitmap(MediaStore.Images.Media.getBitmap(this.getContentResolver(), image));

                    String email = mAuth.getCurrentUser().getEmail();
                    StorageReference storageref = mStorageRef.child("User/" + email + "/profile.jpg");

                    storageref.putFile(image)
                            .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    Toast.makeText(context, "Changed Profile!", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception exception) {
                                    // Handle unsuccessful uploads
                                    // ...
                                }
                            });
                } catch (IOException e) {
                    e.printStackTrace();
                }


            }
        }
    }

    private void loadData(){
        SharedPreferences sharedPreferences = getSharedPreferences(SHARDED_PREFS, MODE_PRIVATE);

        latitude = Double.parseDouble(sharedPreferences.getString(LATITUDE, "-1"));
        longitude = Double.parseDouble(sharedPreferences.getString(LONGITUDE, "-1"));

        locationTest.setText(sharedPreferences.getString(LATITUDE, "-1") + ", " + sharedPreferences.getString(LONGITUDE, "-1"));

        if(latitude != -1 && longitude != -1) {
            Log.d("Change Location", "3");
            newLocation = true;
            getLocationGallery();
        }
    }

    public static void UpdateAccountStatus(boolean signined){
        signup.setVisible(!signined);
        signup_setting.setVisible(!signined);
        signin_setting.setVisible(!signined);
        signin.setVisible(!signined);
        signout_setting.setVisible(signined);
        signout.setVisible(signined);

        if(signined){
            fragmentManager.beginTransaction().replace(R.id.activity_mainscreen, new HomePage()).commit();
            String email = mAuth.getCurrentUser().getEmail();
            user_email.setText(email);

            String temp = email.replace('.', ' ');
            DatabaseReference ref = database.getReference("user").child(temp).child("name");

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

            temp = mAuth.getCurrentUser().getEmail().replace('.', ' ');
            dataref = database.getReference("user").child(temp).child("gallery").child(("" + latitude).replace('.', ' ')).child(("" + longitude).replace('.', ' '));

            dataref.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.getValue() == null){
                        dataref.setValue("");
                    }else{
                        // This method is called once with the initial value and again
                        // whenever data at this location is updated.
                        files = dataSnapshot.getValue(String.class);
                    }
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
        DatabaseReference myRef = database.getReference("user").child(mAuth.getCurrentUser().getEmail().replace('.', ' ')).child("name");

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
        dataref = database.getReference("user").child(mAuth.getCurrentUser().getEmail().replace('.', ' '));
        dataref.removeValue();

        StorageReference storageref = mStorageRef.child("User/" + mAuth.getCurrentUser().getEmail());
        storageref.delete();

        mAuth.getCurrentUser().delete();
        mAuth.signOut();
        profileFile = null;

        UpdateAccountStatus(false);
        Toast.makeText(context, "Deleted Account!", Toast.LENGTH_SHORT).show();
    }



    // Check Permission

    private boolean IsPermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED &&
                    checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                            == PackageManager.PERMISSION_GRANTED &&
                    checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG,"Permission is granted4");

                locationIntent = new Intent(context, LocationService.class);
                startService(locationIntent);
                return true;
            } else {

                Log.v(TAG,"Permission is revoked4");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.ACCESS_FINE_LOCATION}, 0);

                Log.e("Granded", "Permission");
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG,"Location Permission is granted4");
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 0: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    Intent locationIntent = new Intent(context, LocationService.class);
                    startService(locationIntent);
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
        }
    }

    public static String getLatitude(){
        return ("" + latitude).replace('.', ' ');
    }

    public static String getLongitude(){
        return ("" + longitude).replace('.', ' ');
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
        intent.putExtra("longitude", "-119.8634");
        intent.putExtra("latitude", "34.418");

        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);


        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);


        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.nicereminder)
                .setContentTitle("Location Gallery")
                .setContentText("You have been here before! CLick here to view the gallery from this location")
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
