package com.example.niklas.projectsonsofhesslow;

import android.content.res.Resources;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import gl_own.Camera;

public class MainActivity extends AppCompatActivity {

    Timer t = new Timer(true);
    Updatable updatable = Camera.getInstance();
    long lastTimestamp;
    public static Resources resources;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(new copied_gl.MyGLSurfaceView(this));

        //setContentView(R.layout.activity_main);
        t.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                long timeStamp = Calendar.getInstance().getTimeInMillis();
                updatable.Update(timeStamp-lastTimestamp);
                lastTimestamp = timeStamp;
            }
        }, 17, 17);
        resources = this.getResources();
    }
}
