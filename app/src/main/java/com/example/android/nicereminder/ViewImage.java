package com.example.android.nicereminder;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.hardware.SensorListener;
import android.hardware.SensorManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;


public class ViewImage extends AppCompatActivity implements SensorListener {

    private static final int SHAKE_THRESHOLD = 1000;

    private ImageView image;
    private int index;
    private int size;

    private OnSwipeTouchListener swipe;

    private SensorManager sensorMgr;
    private long lastUpdate, startTime;
    private float x, y, z, last_x, last_y, last_z;
    private int count;
    private boolean shake;
    private boolean shakeZ;
    private boolean stall;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        setContentView(R.layout.activity_view_image);

        image = findViewById(R.id.preview);

        index = getIntent().getIntExtra("index", 0);

        image.setImageBitmap(Gallery.imageGallery.get(index));
        size = Gallery.imageGallery.size();

        swipe = new OnSwipeTouchListener(getApplication());

        sensorMgr = (SensorManager) getSystemService(SENSOR_SERVICE);

        lastUpdate = -1;
        last_x = 0;
        last_y = 0;
        last_z = 0;


        shake = false;
        shakeZ = false;
        stall = false;
        count = 0;

        sensorMgr.registerListener(this,
                SensorManager.SENSOR_ACCELEROMETER,
                SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return swipe.onTouch(image, event);
    }

    // Sensor Events
    @Override
    public void onSensorChanged(int sensor, float[] values) {
        if (sensor == SensorManager.SENSOR_ACCELEROMETER) {
            long curTime = System.currentTimeMillis();
            // only allow one update every 100ms.


            long diffTime = (curTime - lastUpdate);

            if(stall && curTime - startTime > 500){
                stall = false;
            }

            if (diffTime > 100) {
                lastUpdate = curTime;

                x = values[SensorManager.DATA_X];
                y = values[SensorManager.DATA_Y];
                z = values[SensorManager.DATA_Z];

                float speed = Math.abs(x + y - last_x - last_y) / diffTime * 10000;
                float speedZ = Math.abs(z - last_z) / diffTime * 10000;


                if(!stall){
                    if(speed < speedZ && speedZ > SHAKE_THRESHOLD){
                        if(shakeZ){
                            if (--index < 0) {
                                index = 0;
                            } else {
                                image.setImageBitmap(Gallery.imageGallery.get(index));
                            }
                            shakeZ = false;
                            shake = false;
                            stall = true;
                            startTime = curTime;
                        }else{
                            shake = false;
                            shakeZ = true;
                        }
                    }else if (speed > SHAKE_THRESHOLD) {
                        Log.d("Speed", "" + speed);

                        if(shake){
                            if (++index >= size) {
                                index = size - 1;
                            } else {
                                image.setImageBitmap(Gallery.imageGallery.get(index));
                            }
                            shake = false;
                            shakeZ = false;
                            stall = true;

                            startTime = curTime;
                        }else{
                            shake = true;
                            shakeZ = false;
                        }
                    }
                }


                last_x = x;
                last_y = y;
                last_z = z;
            }


        }
    }

    @Override
    public void onAccuracyChanged(int sensor, int accuracy) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sensorMgr.unregisterListener(this);
    }

    // Motion Detector for swiping images.
    private class OnSwipeTouchListener implements View.OnTouchListener {

        private final GestureDetector gestureDetector;

        public OnSwipeTouchListener (Context ctx){
            gestureDetector = new GestureDetector(ctx, new GestureListener());
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            return gestureDetector.onTouchEvent(event);
        }

        private final class GestureListener extends GestureDetector.SimpleOnGestureListener {

            private static final int SWIPE_THRESHOLD = 100;
            private static final int SWIPE_VELOCITY_THRESHOLD = 100;

            @Override
            public boolean onDown(MotionEvent e) {
                return true;
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                boolean result = false;
                try {
                    float diffY = e2.getY() - e1.getY();
                    float diffX = e2.getX() - e1.getX();
                    if (Math.abs(diffX) > Math.abs(diffY)) {
                        if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                            if (diffX > 0) {
                                onSwipeRight();
                            } else {
                                onSwipeLeft();
                            }
                            result = true;
                        }
                    }
                    else if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffY > 0) {
                            onSwipeBottom();
                        } else {
                            onSwipeTop();
                        }
                        result = true;
                    }
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
                return result;
            }
        }

        public void onSwipeRight() {
            if (--index < 0) {
                index = 0;
            } else {
                image.setImageBitmap(Gallery.imageGallery.get(index));
            }
        }
        public void onSwipeLeft() {
            if (++index >= size) {
                index = size - 1;
            } else {
                image.setImageBitmap(Gallery.imageGallery.get(index));
            }
        }
        public void onSwipeTop() {
        }

        public void onSwipeBottom() {
        }
    }
}
