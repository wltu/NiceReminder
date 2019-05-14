package com.example.android.nicereminder;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;


public class Camera extends Fragment {
    private static ImageView cameraView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.camera, container, false);

        cameraView = v.findViewById(R.id.cameraView);

        return v;
    }

    public static void setImage(Uri image){
        cameraView.setImageURI(image);
        cameraView.setRotation(90);
    }
}
