package com.example.niklas.projectsonsofhesslow;

import android.app.Activity;
import android.content.res.Resources;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;

import Graphics.GL_TouchEvent;
import Graphics.GL_TouchListener;
import Graphics.GraphicsManager;
import Graphics.MyGLRenderer;
import Graphics.GraphicsObjects.Camera;
import Graphics.Geometry.Vector2;
import Graphics.Geometry.Vector3;
import Graphics.MyGLSurfaceView;

public class MainActivity extends AppCompatActivity implements GL_TouchListener {

    long lastTimestamp;
    public static Resources resources;
    MyGLSurfaceView graphicsView;
    FrameLayout frameLayout;
    Controller controller;
    OverlayController overlayController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        resources = this.getResources();
        graphicsView = new MyGLSurfaceView(this);
        graphicsView.addListener(this);
        frameLayout = new FrameLayout(this);
        frameLayout.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT,Gravity.BOTTOM));
        overlayController = new OverlayController(frameLayout, getLayoutInflater());
        setContentView(R.layout.activity_main);
    }


    Vector2 prevPos;

    //for visual click debugging.
    //Square sq = new Square(new Vector2(0,0), 0.2f, new float[]{0.1f,0.9f,0.3f,1f});

    @Override
    public void Handle(GL_TouchEvent event) {

        if(prevPos!=null)
        {
            //sq.setPos(event.worldPosition);
            Vector2 delta;
            if(!event.isZooming) {
                delta = Vector2.Sub(MyGLRenderer.ScreenToWorldCoords(prevPos, 0), event.worldPosition);
            } else {
                delta = new Vector2(0,0);
            }
            //System.out.println("delta:" + delta);
            switch (event.e.getAction()) {
                case MotionEvent.ACTION_MOVE:
                    Camera cam = Camera.getInstance();
                    cam.setPosRel(new Vector3(delta,event.scale));
                    break;
            }
        }
        //System.out.println("Screen grej" + event.screenPosition.y);
        graphicsView.requestRender();
        prevPos = event.screenPosition;
    }

    public void startGame(View v) {
        /*setContentView(R.layout.activity_overlay);
        View C = findViewById(R.id.Test);
        ViewGroup parent = (ViewGroup) C.getParent();
        int index = parent.indexOfChild(C);
        parent.removeView(C);
        C = graphicsView;
        parent.addView(C, index);*/
        overlayController.addView(graphicsView);
        //View overlay = factory.inflate(R.layout.activity_nextturn, null);
        overlayController.addView(R.layout.activity_playerturn);
        overlayController.addView(R.layout.activity_nextturn);
        controller = new Controller(overlayController,graphicsView);
        graphicsView.addListener(controller);
        setContentView(frameLayout);

    }

    public void nextTurnPressed(View v) {
        //controller has always been init since nextTurn button
        //is not visible before startGame has been pressed
        //TODO give territories continents, setArmiesToPlace gives nullpointerexception when pressed
        controller.nextTurn();
    }

    public void showCardsPressed(View v) {
        //TODO show new layout with cards and trade in button
    }
    public void fightPressed(View v){
        controller.fightButtonPressed();
    }
    public void placePressed(View v){
        controller.placeButtonPressed();
        System.out.println("Place button pressed");
    }
    public void donePressed(View v){
        controller.doneButtonPressed();
        System.out.println("Done button pressed");
    }
}