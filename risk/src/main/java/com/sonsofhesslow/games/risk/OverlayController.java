package com.sonsofhesslow.games.risk;

import android.content.Context;
import android.view.*;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

/**
 * Created by fredr on 2016-05-04.
 */
public class OverlayController {
    ViewGroup parent;
    LayoutInflater factory;
    SeekBar bar;
    OverlayController(Context context){
        FrameLayout frameLayout = new FrameLayout(context);
        frameLayout.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT,Gravity.BOTTOM));
        parent = frameLayout;
        this.factory = (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        bar = ((SeekBar)factory.inflate(R.layout.activity_placearmies, null).findViewById(R.id.seekBar));
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
                System.out.println("Hej du ändra mig");
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
        if(parent.findViewById(value) instanceof  TextView) {
            ((TextView) parent.findViewById(value)).setText(prefText);
        } else if(parent.findViewById(value) instanceof Button) {
            ((Button) parent.findViewById(value)).setText(prefText);
        }
    }
    public void changeTextColour(int value, int c){
        if(parent.findViewById(value) instanceof  TextView) {
            ((TextView) parent.findViewById(value)).setTextColor(c);
        } else if(parent.findViewById(value) instanceof Button) {
            ((Button) parent.findViewById(value)).setTextColor(c);
        }
    }
    public void setBackgroundColour(int value, int c){
        if(parent.findViewById(value) instanceof  TextView) {
            ((TextView) parent.findViewById(value)).setBackgroundColor(c);
        } else if(parent.findViewById(value) instanceof Button) {
            ((Button) parent.findViewById(value)).setBackgroundColor(c);
        } else if(parent.findViewById(value) instanceof RelativeLayout) {
            ((RelativeLayout) parent.findViewById(value)).setBackgroundColor(c);
        }
    }
    public int getBarValue(int value){
        return ((SeekBar)parent.findViewById(value)).getProgress();
    }
    public void setBarMaxValue(int value, int max){
        ((SeekBar)parent.findViewById(value)).setMax(max);
    }
    public ViewGroup getOverlay(){
        return parent;
    }

}
