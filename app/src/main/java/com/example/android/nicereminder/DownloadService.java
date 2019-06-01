package com.example.android.nicereminder;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
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

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class DownloadService extends IntentService {
    public DownloadService() {
        super("DownloadService");
    }

    private static FirebaseAuth mAuth;
    private StorageReference mStorageRef;
    private static StorageReference storageref;
    private File image;

    private int index ;
    private String files;
    private  String newPicFile;
    private String mCameraFileName;
    private String longitude;
    private String latitude;

    private DatabaseReference dataref;

    private Intent done;

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (action.equals("download")) {
                index = 0;

                mAuth = FirebaseAuth.getInstance();
                mStorageRef = FirebaseStorage.getInstance().getReference();

                //files = intent.getStringExtra("files");

                Log.e("WTF0", MainScreen.getLatitude());
                Log.e("WTF1", MainScreen.getLongitude());

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

                            Intent done = new Intent();
                            done.setAction("download");
                            sendBroadcast(done);
                        }else{
                            // This method is called once with the initial value and again
                            // whenever data at this location is updated.

                            files = dataSnapshot.getValue(String.class);

                            Log.e("HTF", files);
                            String name;
                            int i = 0;
                            Gallery.fileNames = new ArrayList<>();
                            Gallery.imageGallery = new ArrayList<>();

                            for (int j = 1; j <= files.length(); j++) {
                                if (j == files.length() || files.charAt(j) == ',') {
                                    name = files.substring(i, j);

                                    Gallery.fileNames.add(name);

                                    Log.d("Set Names", name);
                                    i = j + 1;
                                }
                            }

                            if(Gallery.fileNames.size() > 0)
                                downLoadFiles();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {

                    }
                });
            }else if(action.equals("upload")){
                mAuth = FirebaseAuth.getInstance();
                mStorageRef = FirebaseStorage.getInstance().getReference();

                newPicFile = intent.getStringExtra("name");
                mCameraFileName = intent.getStringExtra("image");
                files = intent.getStringExtra("files");
                longitude = intent.getStringExtra("longitude");
                latitude = intent.getStringExtra("latitude");

                Uri imageTaken = Uri.fromFile(new File(mCameraFileName));


                Gallery.imageGallery.add(Gallery.RotateBitmap(BitmapFactory.decodeFile(mCameraFileName), 90));

                String email = mAuth.getCurrentUser().getEmail();

                StorageReference storageref = mStorageRef.child("User/" + email + "/gallery/" + latitude + "/" + longitude + "/" + newPicFile);

                Log.d("Full Name", mCameraFileName);
                Log.d("Name",newPicFile);

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

                                Toast.makeText(getApplicationContext(), "Uploaded Image!", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                // Handle unsuccessful uploads
                                // ...
                            }
                        });
            }
        }
    }


    private void downLoadFiles() {
        File file = new File("/sdcard/" + Gallery.fileNames.get(index));
        if(file.exists()){
            index++;
            Gallery.imageGallery.add(Gallery.RotateBitmap(BitmapFactory.decodeFile(file.getPath()), 90));
            Log.d("File", "Exit");

            if(Gallery.fileNames.size() > 0 && Gallery.imageGallery.size() < Gallery.fileNames.size()) {
                downLoadFiles();
            }else{
                // Finish downloading all images
                done = new Intent();
                done.setAction("download");
                sendBroadcast(done);
            }
            return;
        }

        StorageReference mref;

        try {
            mref = mStorageRef.child("User/" + mAuth.getCurrentUser().getEmail() + "/gallery/" + latitude + "/" + longitude + "/" + Gallery.fileNames.get(index));

            Log.d("Size", Gallery.fileNames.size() + "");
            Log.d("Size?", Gallery.imageGallery.size() + "");
            Log.d("Name", Gallery.fileNames.get(index));

            int i = Gallery.fileNames.get(index).indexOf('.');

            Log.d("File", Gallery.fileNames.get(index).substring(0,i));
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
                                Intent done = new Intent();
                                done.setAction("download");
                                sendBroadcast(done);
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
