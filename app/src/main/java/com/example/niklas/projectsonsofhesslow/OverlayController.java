package com.example.niklas.projectsonsofhesslow;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.view.*;

/**
 * Created by fredr on 2016-05-04.
 */
public class OverlayController {
    ViewGroup parent;
    LayoutInflater factory;
    OverlayController(android.view.ViewGroup viewGroup, LayoutInflater factory){
        this.parent = viewGroup;
        this.factory = factory;
    }
    public void addView(int value){
        parent.addView(factory.inflate(value,null));
    }
    public void addView(android.view.View view){
        parent.addView(view);
    }

    public void addViewChange(android.view.View view){
        parent.removeViewAt(parent.getChildCount());
        parent.addView(view);
    }
    public void addViewChange(int value){
        if(parent.getChildCount()>0){
            parent.removeViewAt(parent.getChildCount()-1);
        }
        parent.addView(factory.inflate(value,null));
    }

    public void replaceIndex(android.view.View view, int index){
        parent.removeViewAt(index);
        parent.addView(view, index);
    }
    public void replaceIndex(int value, int index){
        parent.removeViewAt(index);
        parent.addView(factory.inflate(value,null), index);
    }

}
