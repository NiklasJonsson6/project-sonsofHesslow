package com.example.niklas.projectsonsofhesslow;

import android.support.v7.app.AppCompatActivity;
import android.view.*;

/**
 * Created by fredr on 2016-05-04.
 */
public class OverlayController {
    ViewGroup parent;
    OverlayController(android.view.ViewGroup viewGroup){
        this.parent = viewGroup;
    }
    public void addView(android.view.View view){
        parent.addView(view);
    }
    public void addViewChange(android.view.View view){
        parent.removeViewAt(parent.getChildCount());
        parent.addView(view);
    }
    public void replaceIndex(android.view.View view, int index){
        parent.removeViewAt(index);
        parent.addView(view, index);
    }

}
