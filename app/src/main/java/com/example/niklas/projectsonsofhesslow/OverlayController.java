package com.example.niklas.projectsonsofhesslow;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.view.*;
import android.widget.SeekBar;
import android.widget.TextView;

/**
 * Created by fredr on 2016-05-04.
 */
public class OverlayController {
    ViewGroup parent;
    LayoutInflater factory;
    SeekBar bar;
    OverlayController(android.view.ViewGroup viewGroup, LayoutInflater factory){
        this.parent = viewGroup;
        this.factory = factory;
        this.bar = ((SeekBar)factory.inflate(R.layout.activity_placearmies,null).findViewById(R.id.seekBar));
        bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                replaceText(R.id.troopsSelected,"" + progress);
                replaceText(R.id.troopsLeft,"" + (seekBar.getMax()-progress));
                System.out.println("Hej du ändra mig");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
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
    public void replaceText(int value, String prefText){
        ((TextView)parent.findViewById(value)).setText(prefText);
    }
    public void changeTextColour(int value, int c){
        ((TextView)parent.findViewById(value)).setTextColor(c);
    }
    public void replaceTextBackgroundColour(int value, String prefText){
        ((TextView)parent.findViewById(value)).setText(prefText);
    }
    public int getBarValue(int value){
        return ((SeekBar)parent.findViewById(value)).getProgress();
    }
    public void setBarMaxValue(int value, int max){
        ((SeekBar)parent.findViewById(value)).setMax(max);
    }



}
