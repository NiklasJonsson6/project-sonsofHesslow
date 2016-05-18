package com.sonsofhesslow.games.risk;

import android.content.Context;
import android.os.Bundle;
import android.view.*;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by fredr on 2016-05-18.
 */
public class Overlay {
    ViewGroup parent;
    LayoutInflater factory;

    Overlay(Context context) {
        FrameLayout frameLayout = new FrameLayout(context);
        frameLayout.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, Gravity.BOTTOM));
        parent = frameLayout;
        this.factory = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void addView(int value) {
        parent.addView(factory.inflate(value, null));
    }

    public void addView(android.view.View view) {
        parent.addView(view);
    }

    public void setGamePhase(Risk.GamePhase phase){
        switch (phase){
            case PICK_TERRITORIES:
                ((TextView) parent.findViewById(R.id.currentPhase)).setText("Pick Territories");
                break;
            case PLACE_STARTING_ARMIES:
                ((TextView) parent.findViewById(R.id.currentPhase)).setText("Place starting armies");
                break;
            case PLACE_ARMIES:
                ((TextView) parent.findViewById(R.id.currentPhase)).setText("Place armies");
                break;
            case FIGHT:
                ((TextView) parent.findViewById(R.id.currentPhase)).setText("Fight");
                break;
            case MOVEMENT:
                ((TextView) parent.findViewById(R.id.currentPhase)).setText("Movement");
                break;
        }
    }

    public void setCurrentPlayer(Player player){
        ((TextView) parent.findViewById(R.id.currentPlayer)).setText(player.getName());
    }

    public void setListVisible(boolean state){
        if(state == true){
            parent.findViewById(R.id.listView).setVisibility(View.VISIBLE);
            parent.findViewById(R.id.showList).setVisibility(View.GONE);
            parent.findViewById(R.id.hideList).setVisibility(View.VISIBLE);
        } else {
            parent.findViewById(R.id.listView).setVisibility(View.GONE);
            parent.findViewById(R.id.showList).setVisibility(View.VISIBLE);
            parent.findViewById(R.id.hideList).setVisibility(View.GONE);
        }
    }
    public void setFightVisible(boolean state){
        if(state == true){
            parent.findViewById(R.id.fightButton).setVisibility(View.VISIBLE);
            parent.findViewById(R.id.nextTurn).setVisibility(View.GONE);
            parent.findViewById(R.id.cards).setVisibility(View.GONE);
        } else {
            parent.findViewById(R.id.fightButton).setVisibility(View.GONE);
            parent.findViewById(R.id.nextTurn).setVisibility(View.VISIBLE);
            parent.findViewById(R.id.cards).setVisibility(View.VISIBLE);
        }
    }

    public void setInformation(String Text){
        parent.findViewById(R.id.information).setVisibility(View.VISIBLE);
        ((TextView) parent.findViewById(R.id.information)).setText(Text);
        parent.findViewById(R.id.nextTurn).setVisibility(View.GONE);
        parent.findViewById(R.id.cards).setVisibility(View.GONE);
    }

    public void setNextTurnVisible(boolean state){
        if(state == true){
            parent.findViewById(R.id.nextTurn).setVisibility(View.VISIBLE);
            parent.findViewById(R.id.cards).setVisibility(View.VISIBLE);
        } else {
            parent.findViewById(R.id.nextTurn).setVisibility(View.GONE);
            parent.findViewById(R.id.cards).setVisibility(View.GONE);
        }
    }
    public ViewGroup getOverlay(){
        return parent;
    }
}
