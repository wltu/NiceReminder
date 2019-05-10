package com.example.android.nicereminder;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class Gallery extends Fragment {
    private static HashMap<ImageView, Integer> map;

    public static boolean delete;


    private static Context context;


    public static ArrayList<Bitmap> imageGallery = new ArrayList<>();;
    public static ArrayList<Boolean> selectImage = new ArrayList<>();;
    public static ArrayList<String> fileNames = new ArrayList<>();;


    private static boolean landscape;
    private static int w;

    private static FirebaseAuth mAuth;
    private StorageReference mStorageRef;
    private static StorageReference storageref;

    private static String files = "";

    private View view;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        view = getView();

        delete = false;
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
        map = new HashMap<>();
        context = view.getContext();

        mAuth = FirebaseAuth.getInstance();
        mStorageRef = FirebaseStorage.getInstance().getReference();




        if(imageGallery.size() > 0){
            selectImage = new ArrayList<>(Collections.nCopies(imageGallery.size(), false));
            setImages();
            return;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_gallery, container, false);
    }

    private void setImages(){
        // Set up layout for the image gallery.
        LinearLayout table = (LinearLayout)view.findViewById(R.id.table);
        LinearLayout row;


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
        int counter = 0;
        ImageView imageView;
        int numImages = imageGallery.size();

        // Set up all rows.
        for(int i = 0; i < numImages; i++){
            // Set up image view.
            imageView = new ImageView(context);

            imageView.setImageBitmap(imageGallery.get(i));
            map.put(imageView, i);

            imageView.setLayoutParams(new ViewGroup.LayoutParams(w / numImage, w/ numImage));;

            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

            // Select Image
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(delete){
                        if(v.getForeground() == null){
                            v.setForeground(getResources().getDrawable(R.drawable.border));
                            selectImage.set(map.get(v), true);
                        }else{
                            v.setForeground(null);
                            selectImage.set(map.get(v), false);
                        }
                    }else {
                        Intent intent = new Intent(getActivity(), ViewImage.class);
                        intent.putExtra("index", map.get(v));
                        startActivity(intent);
                    }
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

    public static Bitmap RotateBitmap(Bitmap source, float angle)
    {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    public static String DeleteSelected(){
        String email = mAuth.getCurrentUser().getEmail();
        files = "";

        for(int i = 0; i < fileNames.size(); i++){
            if(selectImage.get(i)){
                storageref = FirebaseStorage.getInstance().getReference().child("User/" + email + "/gallery/" + fileNames.get(i));
                storageref.delete();


                selectImage.remove(i);
                imageGallery.remove(i);
                fileNames.remove(i);
                i--;
            }
        }

        for(int i = 0; i < fileNames.size(); i++) {
            files += fileNames.get(i);

            if(i != fileNames.size() - 1){
                files += ",";
            }
        }

        return files;
    }

    public static void CancelSelect(){
        for (ImageView v : map.keySet()) {
            v.setForeground(null);
        }
    }
}
