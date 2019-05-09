package com.example.android.nicereminder;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
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

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class Gallery extends Fragment {
    private static HashMap<ImageView, Integer> map;

    public static boolean delete;


    private static Context context;


    public static ArrayList<Bitmap> imageGallery;
    public static ArrayList<Boolean> selectImage;


    private static boolean landscape;
    private static int w;

    private static ArrayList<String> fileNames;

    private int index = 0;

    private static FirebaseAuth mAuth;
    private StorageReference mStorageRef;
    private static StorageReference storageref;
    private File image;

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


        Log.d("Name", files);
        String name;

        int i = 0;
        index = 0;

        context = view.getContext();

        mAuth = FirebaseAuth.getInstance();
        mStorageRef = FirebaseStorage.getInstance().getReference();


        Log.d("Width", "" + w);
        Log.d("Landscape", "" + landscape);

        if(files != null && !files.isEmpty() && imageGallery != null){
            selectImage =new ArrayList<>(Collections.nCopies(imageGallery.size(), false));
            setImages();
            return;
        }

        fileNames = new ArrayList<>();
        imageGallery = new ArrayList<>();

        for(int j = 1; j <= files.length(); j++){
            if(j == files.length()||files.charAt(j) == ','){
                name = files.substring(i, j);

                fileNames.add(name);

                i = j + 1;
            }
        }

       selectImage = new ArrayList<>(Collections.nCopies(fileNames.size(), false));


        if(fileNames.size() > 0)
            downLoadFiles();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_gallery, container, false);
    }

    public static void setFileNames(String fileNames) {
        files = fileNames;
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
                            imageGallery.add(RotateBitmap(BitmapFactory.decodeFile(image.getPath()), 90));
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
            if(i != 0){
                files = files + ",";
            }

            if(selectImage.get(i)){
                storageref = FirebaseStorage.getInstance().getReference().child("User/" + email + "/gallery/" + fileNames.get(i));
                storageref.delete();


                selectImage.remove(i);
                imageGallery.remove(i);
                fileNames.remove(i);
                i--;
            }else{
                files = files + fileNames.get(i);
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
