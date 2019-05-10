package com.example.android.nicereminder;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

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

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (action.equals("download")) {
                index = 0;

                mAuth = FirebaseAuth.getInstance();
                mStorageRef = FirebaseStorage.getInstance().getReference();

                String files = intent.getStringExtra("files");
                String name;
                int i = 0;
                Gallery.fileNames = new ArrayList<>();
                Gallery.imageGallery = new ArrayList<>();

                for(int j = 1; j <= files.length(); j++){
                    if(j == files.length()||files.charAt(j) == ','){
                        name = files.substring(i, j);

                        Gallery.fileNames.add(name);

                        i = j + 1;
                    }
                }

                downLoadFiles();
            }
        }
    }


    private void downLoadFiles() {
        StorageReference mref;

        try {
            mref = mStorageRef.child("User/" + mAuth.getCurrentUser().getEmail() + "/gallery/" + Gallery.fileNames.get(index));

            Log.d("Size", Gallery.fileNames.size() + "");
            Log.d("Size?", Gallery.imageGallery.size() + "");
            Log.d("Name", Gallery.fileNames.get(index));

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
