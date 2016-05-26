package com.sonsofhesslow.games.risk.network;

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
    void showWaitingRoom(Room room);
    void displayError();
    void startGame(ArrayList<Participant> participants);
    boolean resolveConnection(ConnectionResult result);
    void showMainScreen();
    void showSignInScreen();
    void showWaitScreen();
}
