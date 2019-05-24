package com.example.android.nicereminder;

import android.app.Fragment;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;


public class Camera extends Fragment {
    private int id;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.camera, container, false);

        id = 0;
        Button test = v.findViewById(R.id.test);
        test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String CHANNEL_ID = "CHANNEL";
                CharSequence name = "NICE CHANNEL";
                String Description = "Very nice channel";


                NotificationManager notificationManager = (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
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


                Intent intent =  new Intent(getContext(), DownloadService.class);
                intent.setAction("download");


                PendingIntent pendingIntent = PendingIntent.getService(getContext(), 0, intent, 0);


                NotificationCompat.Builder builder = new NotificationCompat.Builder(getContext())
                        .setSmallIcon(R.drawable.nicereminder)
                        .setContentTitle("Location Gallery")
                        .setContentText("You have been here before! CLick here to view the gallery from this location")
                        .setDefaults(Notification.DEFAULT_ALL)
                        .setPriority(Notification.PRIORITY_HIGH)
                        .setContentIntent(pendingIntent);


                if (notificationManager != null) {
                    notificationManager.notify(id++, builder.build());
                }
            }
        });
        return v;
    }
}
