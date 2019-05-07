package com.example.android.nicereminder;

import android.app.Fragment;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import android.support.constraint.ConstraintLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;

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


    private int counter;
    private static ArrayList<Bitmap> imageGallery;
    private int numImages;
    private ImageView imageView;

    private static boolean landscape;
    private static int w;

    private static ArrayList<String> fileNames;

    private int index = 0;

    private FirebaseAuth mAuth;
    private StorageReference mStorageRef;
    private File image;

    private static String prevous_files = "";
    private static String files = "";

    private View view;


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        view = getView();

        landscape = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
        view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {

                w = getView().getWidth();

                if(w > 0)
                {
                    getView().getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    setUp();
                }
            }
        });
    }

    private void setUp() {
        table = (LinearLayout)view.findViewById(R.id.table);

        Log.d("Name", files);
        String name;

        int i = 0;
        index = 0;

        context = view.getContext();

        mAuth = FirebaseAuth.getInstance();
        mStorageRef = FirebaseStorage.getInstance().getReference();


        Log.d("Width", "" + w);
        Log.d("Landscape", "" + landscape);
        if(prevous_files.compareTo(files) == 0){
            setImages();
            return;
        }

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

        downLoadFiles();

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_gallery, container, false);
    }

    public static void setFileNames(String fileNames) {
        prevous_files = files;
        files = fileNames;
    }

    public static void setUpPhone(boolean l, int width){
//        landscape = l;
//        w = width;
//
//        Log.d("Width", "" + w);
//        Log.d("Landscape", "" + landscape);
    }
    private void setImages(){
        // Set up layout for the image gallery.

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

                            if(imageGallery.size() != fileNames.size()) {
                                downLoadFiles();
                            }else{
                                prevous_files = files;
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
