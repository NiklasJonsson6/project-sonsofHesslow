package com.sonsofhesslow.games.risk;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.sonsofhesslow.games.risk.model.Card;
import com.sonsofhesslow.games.risk.model.Player;
import com.sonsofhesslow.games.risk.model.Risk;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by fredr on 2016-05-18.
 */
public class Overlay {
    ViewGroup parent;
    LayoutInflater factory;
    Context context;
    int movementBlue;
    int placeArmiesGreen;
    int pickTerritoriesOrange;
    int fightRed;
    boolean listPopulated;

    Overlay(Context context) {
        // TODO: 2016-05-26 r.id color instead? (better to take from res)  getResources().getColor(R.color.<id>);
        movementBlue = Color.parseColor("#ff0099cc");
        placeArmiesGreen = Color.parseColor("#66CDAA");
        pickTerritoriesOrange = Color.parseColor("#F0E68C");
        fightRed = Color.parseColor("#B22222");

        FrameLayout frameLayout = new FrameLayout(context);
        frameLayout.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, Gravity.BOTTOM));
        parent = frameLayout;
        this.context = context;
        this.factory = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        listPopulated = false;
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
        listPopulated = true;
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

    public void populateListView(Player[] players, Collection<float[]> colours){
        //Elements
        ArrayList<float[]> colour = new ArrayList<float[]>(colours);
        ArrayList<String> names = new ArrayList<String>();
        ArrayList<String> armyCount = new ArrayList<String>();
        ArrayList<Uri> images = new ArrayList<Uri>();
        for (Player player : players){
            names.add(player.getName());
            armyCount.add("Territory count: " + player.getTerritoriesOwned());
            images.add(player.getImageRefrence());
        }
        //Adapter
        ListView listView = (ListView) parent.findViewById(R.id.listView);
        listView.setAdapter(new CustomAdapter((MainActivity) context, names, images, armyCount, colour));
        ((FrameLayout) parent.findViewById(R.id.listFrame)).setLayoutParams(new FrameLayout.LayoutParams((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 280, context.getResources().getDisplayMetrics()),
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 57*names.size() + 50, context.getResources().getDisplayMetrics())));
    }

    public void populateGridView(ArrayList<Card> cardList){
        //Elements
        ArrayList<String> names = new ArrayList<String>();
        ArrayList<Integer> images = new ArrayList<Integer>();
        for (Card card : cardList){
            switch (card.getCardType()){
                case INFANTRY:
                    images.add(R.drawable.ic_account_box_black_48dp);
                    names.add("Infantery");
                    break;
                case CAVALRY:
                    images.add(R.drawable.ic_account_box_black_48dp);
                    names.add("Cavalry");
                    break;
                case ARTILLARY:
                    images.add(R.drawable.ic_account_box_black_48dp);
                    names.add("Artillary");
                    break;
            }
        }
        //Adapter
        GridView gridView = (GridView) parent.findViewById(R.id.gridView);
        gridView.setAdapter(new CardGridAdapter((MainActivity) context, names, images));
        //((FrameLayout) parent.findViewById(R.id.gridView)).setLayoutParams(new FrameLayout.LayoutParams((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 280, context.getResources().getDisplayMetrics()),
                //(int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 57*names.size() + 50, context.getResources().getDisplayMetrics())));
    }

    public void setCardVisibility(boolean state){
        if (state == true) {
            hideBottom();
            parent.findViewById(R.id.cardView).setVisibility(View.VISIBLE);
        } else {
            parent.findViewById(R.id.cardView).setVisibility(View.GONE);
        }
    }
}
