package com.sonsofhesslow.games.risk.network;

import android.app.Activity;
import android.content.Intent;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.games.multiplayer.Participant;
import com.google.android.gms.games.multiplayer.realtime.Room;

import java.util.ArrayList;

/**
 * Created by Daniel on 26/05/2016.
 */
public interface UIUpdate {
    void displayInvitation(String caller);
    void removeInvitation();
    void showWaitingRoom(Intent intent);
    void displayError();
    void startGame();
    Activity getActivity();
    void showMainScreen();
    void showSignInScreen();
    void showWaitScreen();
}
