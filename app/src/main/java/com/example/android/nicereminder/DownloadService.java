package com.example.android.nicereminder;

import android.app.IntentService;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class DownloadService extends IntentService {
    public DownloadService() {
        super("DownloadService");
    }

    // Firebase Variables
    private static FirebaseAuth mAuth;
    private StorageReference mStorageRef;

    // Image Variables
    private File image;
    private int index ;
    private String files;
    private  String newPicFile;
    private String mCameraFileName;

    // Location
    private String longitude;
    private String latitude;

    private DatabaseReference dataref;
    private Bitmap bmap;

    private Intent done;

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();

            // Download Background Task.
            if (action.equals("download")) {
                index = 0;

                // Set Variables
                mAuth = FirebaseAuth.getInstance();
                mStorageRef = FirebaseStorage.getInstance().getReference();
                latitude = MainScreen.getLatitude();
                longitude = MainScreen.getLongitude();

                dataref = FirebaseDatabase.getInstance().getReference("user").child(mAuth.getCurrentUser().getEmail()
                                                        .replace('.', ' '))
                                                        .child("gallery")
                                                        .child(latitude)
                                                        .child(longitude);

                dataref.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.getValue() == null){
                            dataref.setValue("");

                            done("download");
                        }else{
                            files = dataSnapshot.getValue(String.class);

                            String name;
                            int i = 0;

                            // Clear ArrayList
                            Gallery.fileNames.clear();
                            Gallery.imageGallery.clear();

//                            Gallery.fileNames = new ArrayList<>();
//                            Gallery.imageGallery = new ArrayList<>();

                            // Get File Names
                            for (int j = 1; j <= files.length(); j++) {
                                if (j == files.length() || files.charAt(j) == ',') {
                                    name = files.substring(i, j);

                                    Gallery.fileNames.add(name);
                                    i = j + 1;
                                }
                            }

                            if(Gallery.fileNames.size() > 0)
                                downLoadFiles();
                        }

                        dataref.removeEventListener(this);
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {}
                });
            }
            // Upload Background Task
            else if(action.equals("upload")){
                mAuth = FirebaseAuth.getInstance();
                mStorageRef = FirebaseStorage.getInstance().getReference();

                newPicFile = intent.getStringExtra("name");
                mCameraFileName = intent.getStringExtra("image");
                files = intent.getStringExtra("files");
                longitude = intent.getStringExtra("longitude");
                latitude = intent.getStringExtra("latitude");

                Uri imageTaken = Uri.fromFile(new File(mCameraFileName));

                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 8;
                Gallery.imageGallery.add(Gallery.RotateBitmap(BitmapFactory.decodeFile(mCameraFileName, options), 90));

                String email = mAuth.getCurrentUser().getEmail();

                StorageReference storageref = mStorageRef.child("User/" + email + "/gallery/" + latitude + "/" + longitude + "/" + newPicFile);

                // Upload Image to cloud.
                dataref = FirebaseDatabase.getInstance().getReference("user").child(mAuth.getCurrentUser().getEmail().replace('.', ' ')).child("gallery").child(latitude).child(longitude);
                storageref.putFile(imageTaken)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                if(files.isEmpty()){
                                    files = newPicFile;
                                }else{
                                    files = files + "," + newPicFile;
                                }

                                dataref.setValue(files);

                                done("upload");
                                Toast.makeText(getApplicationContext(), "Uploaded Image!", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                Toast.makeText(getApplicationContext(), "Error in uploading image!", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        }
    }

    private void done(String action) {
        Intent done = new Intent();
        done.setAction(action);
        sendBroadcast(done);
    }


    // Download all gallery images.
    private void downLoadFiles() {
        File file = new File("/sdcard/" + Gallery.fileNames.get(index));

        // Image exist on current device.
        if(file.exists()){
            index++;

            // Rescale Image to reduce memory.
            bmap = Gallery.RotateBitmap(BitmapFactory.decodeFile(file.getPath()), 90);
            bmap = Bitmap.createScaledBitmap(bmap, bmap.getWidth() / 2, bmap.getHeight() / 2, false);

            Gallery.imageGallery.add(bmap);

            if(Gallery.fileNames.size() > 0 && index < Gallery.fileNames.size() && Gallery.imageGallery.size() < Gallery.fileNames.size()) {
                downLoadFiles();
            }else{
                // Finish downloading all images
                done("download");
            }
            return;
        }


        // Image doesn't exist on current device. Download from cloud.
        StorageReference mref;

        try {
            mref = mStorageRef.child("User/" + mAuth.getCurrentUser().getEmail() + "/gallery/" + latitude + "/" + longitude + "/" + Gallery.fileNames.get(index));

            int i = Gallery.fileNames.get(index).indexOf('.');

            image = File.createTempFile(Gallery.fileNames.get(index).substring(0,i), Gallery.fileNames.get(index).substring(i));
            mref.getFile(image)
                    .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                            index++;
                            Gallery.imageGallery.add(Gallery.RotateBitmap(BitmapFactory.decodeFile(image.getPath()), 90));
                            if(Gallery.imageGallery.size() != Gallery.fileNames.size()) {
                                downLoadFiles();
                            }else{
                                // Finish downloading all images
                                done("download");
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
