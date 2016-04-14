package com.example.niklas.projectsonsofhesslow;

import android.content.res.Resources;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        graphicsView = new MyGLSurfaceView(this);
        graphicsView.addListener(this);
        setContentView(R.layout.activity_main);
        resources = this.getResources();
    }


    Vector2 prevPos;

    //for visual click debugging.
    //Square sq = new Square(new Vector2(0,0), 0.2f, new float[]{0.1f,0.9f,0.3f,1f});

    @Override
    public void Handle(GL_TouchEvent event) {

        if(event.touchedRegion)
        {
            float[] color = {0.7f,0.9f,0.4f,1f};
            float[] neighbor_color = {0.6f,0.9f,0.3f,1f};
            float[] region_color = {0.5f,0.9f,0.2f,1f};
            Integer[] in_continent = GraphicsManager.getContinentRegions(GraphicsManager.getContinetId(event.regionId));
            Integer[] neighbours = GraphicsManager.getNeighbours(event.regionId);

            for(int i = 0; i<in_continent.length;i++)
            {
                GraphicsManager.setColor(in_continent[i],region_color);
            }

            for(int i = 0; i< neighbours.length; i++)
            {
                GraphicsManager.setColor(neighbours[i],neighbor_color);
            }

            GraphicsManager.setColor(event.regionId, color);
        }

        if(prevPos!=null)
        {
            //sq.setPos(event.worldPosition);
            Vector2 delta = Vector2.Sub(MyGLRenderer.ScreenToWorldCoords(prevPos,0),event.worldPosition);
            System.out.println("delta:" + delta);
            switch (event.e.getAction()) {
                case MotionEvent.ACTION_MOVE:
                    Camera cam = Camera.getInstance();
                    cam.setPosRel(new Vector3(delta));
                    break;
            }
        }
        System.out.println("Screen grej" + event.screenPosition.y);
        graphicsView.requestRender();
        prevPos = event.screenPosition;
    }

    public void startGame(View v) {
        setContentView(R.layout.activity_overlay);
        View C = findViewById(R.id.Test);
        ViewGroup parent = (ViewGroup) C.getParent();
        int index = parent.indexOfChild(C);
        parent.removeView(C);
        C = graphicsView;
        parent.addView(C, index);
    }

}
