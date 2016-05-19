package com.sonsofhesslow.games.risk;

import android.content.Context;
import android.graphics.Color;
import android.view.*;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.sonsofhesslow.games.risk.model.Player;
import com.sonsofhesslow.games.risk.model.Risk;

/**
 * Created by fredr on 2016-05-18.
 */
public class Overlay {
    ViewGroup parent;
    LayoutInflater factory;
    int movementBlue;
    int placeArmiesGreen;
    int pickTerritoriesOrange;
    int fightRed;

    Overlay(Context context) {
        movementBlue = Color.parseColor("#ff0099cc");
        placeArmiesGreen = Color.parseColor("#66CDAA");
        pickTerritoriesOrange = Color.parseColor("#F0E68C");
        fightRed = Color.parseColor("#B22222");
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

    public void removeView(int value) {parent.removeView(parent.findViewById(R.id.waitTurn));}

    public void setGamePhase(Risk.GamePhase phase){
        switch (phase){
            case PICK_TERRITORIES:
                ((TextView) parent.findViewById(R.id.currentPhase)).setText("Pick Territories");
                ((TextView) parent.findViewById(R.id.currentPhase)).setBackgroundColor(pickTerritoriesOrange);
                setInformation("Choose a country", true);
                setInformationColour(pickTerritoriesOrange);
                break;
            case PLACE_STARTING_ARMIES:
                ((TextView) parent.findViewById(R.id.currentPhase)).setText("Place starting armies");
                ((TextView) parent.findViewById(R.id.currentPhase)).setBackgroundColor(placeArmiesGreen);
                setInformation("Armies to place: ", true);
                setInformationColour(placeArmiesGreen);
                break;
            case PLACE_ARMIES:
                ((TextView) parent.findViewById(R.id.currentPhase)).setText("Place armies");
                ((TextView) parent.findViewById(R.id.currentPhase)).setBackgroundColor(placeArmiesGreen);
                setPlaceArmiesVisible(true);
                break;
            case FIGHT:
                ((TextView) parent.findViewById(R.id.currentPhase)).setText("Fight");
                setNextTurnVisible(true);
                ((TextView) parent.findViewById(R.id.currentPhase)).setBackgroundColor(fightRed);
                break;
            case MOVEMENT:
                ((TextView) parent.findViewById(R.id.currentPhase)).setText("Movement");
                ((TextView) parent.findViewById(R.id.currentPhase)).setBackgroundColor(movementBlue);
                break;
        }
    }

    public void setCurrentPlayer(Player player, int colour){
        ((TextView) parent.findViewById(R.id.currentPlayer)).setText(player.getName());
        ((TextView) parent.findViewById(R.id.currentPlayer)).setBackgroundColor(colour);
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
            hideBottom();
            parent.findViewById(R.id.fightButton).setVisibility(View.VISIBLE);
        } else {
            parent.findViewById(R.id.fightButton).setVisibility(View.GONE);
        }
    }

    public void setInformation(String Text, boolean state){
        if(state == true){
            hideBottom();
            parent.findViewById(R.id.information).setVisibility(View.VISIBLE);
            ((TextView) parent.findViewById(R.id.information)).setText(Text);
        } else {
            parent.findViewById(R.id.information).setVisibility(View.GONE);
        }
    }

    public void  setInformationColour(int colour){
        parent.findViewById(R.id.information).setBackgroundColor(colour);
    }

    public void setNextTurnVisible(boolean state){
        if(state == true){
            hideBottom();
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
    public int getBarValue(){
        return ((SeekBar)parent.findViewById(R.id.troopSeekBar)).getProgress();
    }
    public void setBarMaxValue(int max){
        ((SeekBar)parent.findViewById(R.id.troopSeekBar)).setMax(max);
    }
    public void setPlaceArmiesVisible(boolean state) {
        if (state == true) {
            hideBottom();
            parent.findViewById(R.id.placeArmies).setVisibility(View.VISIBLE);
        } else {
            parent.findViewById(R.id.placeArmies).setVisibility(View.GONE);
        }
    }

    public void hideBottom(){
        setNextTurnVisible(false);
        setPlaceArmiesVisible(false);
        setFightVisible(false);
        setInformation("", false);
    }
}
