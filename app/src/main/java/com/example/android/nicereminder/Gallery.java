package com.example.android.nicereminder;

import android.app.Fragment;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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


public class Gallery extends Fragment {
    private LinearLayout table;
    private LinearLayout row;
    private static Context context;

    private static boolean landscape;
    private int counter;
    private static ArrayList<Bitmap> imageGallery;
    private int numImages;
    private ImageView imageView;
    private static int w;

    private ArrayList<String> fileNames;
    private static ImageView test;

    private int index = 0;

    private FirebaseAuth mAuth;
    private StorageReference mStorageRef;
    private File image;

    private static String files;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_gallery, container, false);

        table = (LinearLayout)view.findViewById(R.id.table);

        test = view.findViewById(R.id.testView);


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

        context = container.getContext();

        mAuth = FirebaseAuth.getInstance();
        mStorageRef = FirebaseStorage.getInstance().getReference();

        downLoadFiles();

        return view;
    }

    public static void setFileNames(String fileNames) {
        files = fileNames;
    }

    public static void setLandscape(boolean l, int ww){
        landscape = l;
        w = ww;
    }

    private void setImages(){
        // Set up layout for the image gallery.


        Log.d("Test????", "" + imageGallery.size());


        if(imageGallery.size() == 0){
            return;
        }
        // Set up current row for the image gallery.
        row = new LinearLayout(context);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        row.setGravity(Gravity.CENTER);



        // Number of image per row is 3 for portrait and 4 for landscape.
        int numImage = (3 + (landscape ? 1 : 0));
        counter = 0;

        numImages = imageGallery.size();

        // Set up all rows.
        for(int i = 0; i < numImages; i++){

            Log.d("sdasdasdas", "" + i);
            // Set up image view.
            imageView = new ImageView(context);

            imageView.setImageBitmap(imageGallery.get(i));

            imageView.setLayoutParams(new ViewGroup.LayoutParams(w / numImage, w/ numImage));


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

                row = new LinearLayout(context);
                row.setOrientation(LinearLayout.HORIZONTAL);
                row.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));
                row.setGravity(Gravity.CENTER);
            }
        }

        if(counter > 0){
            for(; counter < numImage; counter++){
                imageView = new ImageView(context);
                imageView.setLayoutParams(new ViewGroup.LayoutParams(w / numImage, w/ numImage));
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
