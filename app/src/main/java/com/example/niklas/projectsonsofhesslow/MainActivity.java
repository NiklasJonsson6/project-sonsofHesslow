package com.example.niklas.projectsonsofhesslow;

import android.content.res.Resources;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import copied_gl.GL_TouchEvent;
import copied_gl.GL_TouchListener;
import copied_gl.GraphicsManager;
import copied_gl.MyGLRenderer;
import copied_gl.MyGLSurfaceView;
import gl_own.Camera;
import gl_own.Geometry.Vector2;
import gl_own.Geometry.Vector3;

public class MainActivity extends AppCompatActivity implements GL_TouchListener {

    long lastTimestamp;
    public static Resources resources;
    copied_gl.MyGLSurfaceView graphicsView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        graphicsView = new copied_gl.MyGLSurfaceView(this);

        graphicsView.addListener(this);

        setContentView(graphicsView);

        //setContentView(R.layout.activity_main);
        resources = this.getResources();
    }


    Vector2 prevPos;
    @Override
    public void Handle(GL_TouchEvent event) {

        if(event.touchedRegion)
        {
            float[] color = {(float)Math.random(),(float)Math.random(),(float)Math.random(),1f};
            GraphicsManager.setColor(event.regionIndex, color);
        }

        if(prevPos!=null)
        {
            Vector2 delta = Vector2.Sub(MyGLRenderer.ScreenToWorldCoords(prevPos,0),event.worldPosition);
            System.out.println("delta:" + delta);
            switch (event.e.getAction()) {
                case MotionEvent.ACTION_MOVE:
                    Camera cam = Camera.getInstance();
                    cam.setPosRel(new Vector3(delta));
                    break;
            }
        }

        graphicsView.requestRender();
        prevPos = event.screenPosition;
    }
}
