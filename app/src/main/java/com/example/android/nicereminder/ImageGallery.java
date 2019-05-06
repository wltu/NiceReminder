package com.example.android.nicereminder;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

public class ImageGallery extends AppCompatActivity {

    private LinearLayout table;
    private LinearLayout row;
    private static Context context;

    private boolean landscape;
    private int counter;
    private static ArrayList<Bitmap> imageGallery;
    private int numImages;
    private ImageView imageView;
    private int w;

    private ArrayList<String> fileNames;
    private static ImageView test;

    private int index = 0;

    private FirebaseAuth mAuth;
    private StorageReference mStorageRef;
    private File image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_gallery);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        table = (LinearLayout)findViewById(R.id.table);

        test = findViewById(R.id.testView);

        String files = getIntent().getStringExtra("fileNames");

        Log.d("Name", files);
        String name;

        int i = 0;
        index = 0;
        fileNames = new ArrayList<>();
        imageGallery = new ArrayList<>();

        for(int j = 1; j <= files.length(); j++){
            if(j == files.length()||files.charAt(j) == ','){
                name = files.substring(i, j);

                Log.d("Nameddd", name);

                fileNames.add(name);

                i = j + 1;
            }
        }

        context = getApplicationContext();

        mAuth = FirebaseAuth.getInstance();
        mStorageRef = FirebaseStorage.getInstance().getReference();

        downLoadFiles();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return false;
    }


    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        // Check if the current orientation is landscape.
        landscape = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;

        ScrollView layout = (ScrollView) findViewById(R.id.scroll);
        w = layout.getWidth();
    }

    private void setImages(){
        // Set up layout for the image gallery.


        Log.d("Test????", "" + imageGallery.size());


        if(imageGallery.size() == 0){
            return;
        }
        // Set up current row for the image gallery.
        row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        row.setGravity(Gravity.CENTER);



        // Number of image per row is 3 for portrait and 4 for landscape.
        int numImage = (3 + (landscape ? 1 : 0));
        counter = 0;

        numImages = imageGallery.size();

        // Set up all rows.
        for(int i = 0; i < numImages; i++){

            Log.d("sdasdasdas", "" + i);
            // Set up image view.
            imageView = new ImageView(this);

            imageView.setImageBitmap(imageGallery.get(i));

            imageView.setLayoutParams(new LayoutParams(w / numImage, w/ numImage));


            // Select Image
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d("Error", "" );
                }
            });

            row.addView(imageView);

            if(++counter == numImage){
                counter = 0;

                table.addView(row);

                row = new LinearLayout(this);
                row.setOrientation(LinearLayout.HORIZONTAL);
                row.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));
                row.setGravity(Gravity.CENTER);
            }
        }

        if(counter > 0){
            for(; counter < numImage; counter++){
                imageView = new ImageView(this);
                imageView.setLayoutParams(new LayoutParams(w / numImage, w/ numImage));
                row.addView(imageView);
            }
        }

        table.addView(row);
    }

    private void downLoadFiles() {
        StorageReference mref;

        try {
            mref = mStorageRef.child("User/" + mAuth.getCurrentUser().getEmail() + "/gallery/" + fileNames.get(index));


            Log.d("Name", fileNames.get(index));
            image = File.createTempFile(fileNames.get(index).substring(0, fileNames.get(index).indexOf('.')), "jpg");
            mref.getFile(image)
                    .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                            index++;
                            try {
                                imageGallery.add(BitmapFactory.decodeStream(new FileInputStream(image)));
                                test.setImageBitmap(BitmapFactory.decodeStream(new FileInputStream(image)));
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            }

                            Log.d("Download", imageGallery.size() + "");

                            if(imageGallery.size() != fileNames.size()) {
                                downLoadFiles();
                            }else{
                                setImages();
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
