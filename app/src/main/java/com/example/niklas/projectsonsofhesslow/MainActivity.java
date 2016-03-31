package com.example.niklas.projectsonsofhesslow;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(new copied_gl.MyGLSurfaceView(this));
        //setContentView(R.layout.activity_main);
    }
}
