package com.sonsofhesslow.games.risk.network;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.games.multiplayer.Invitation;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessage;
import com.google.android.gms.games.multiplayer.realtime.Room;

import java.util.List;

public interface GooglePlayNetworkCompatible {
    void onRealTimeMessageReceived(RealTimeMessage rtm);
    void broadcast(byte[] messageArray);

    void onConnected(@Nullable Bundle bundle);
    void onConnectionSuspended(int i);
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult);
    public void onInvitationReceived(Invitation invitation);
    public void onInvitationRemoved(String s);
    public void onRoomConnecting(Room room);
    public void onRoomAutoMatching(Room room);
    public void onPeerInvitedToRoom(Room room, List<String> list);
    public void onPeerDeclined(Room room, List<String> list);
    public void onPeerJoined(Room room, List<String> list);
    public void onPeerLeft(Room room, List<String> list);
    public void onConnectedToRoom(Room room);
    public void onDisconnectedFromRoom(Room room);
    public void onPeersConnected(Room room, List<String> list);
    public void onPeersDisconnected(Room room, List<String> list);
    public void onP2PConnected(String s);
    public void onP2PDisconnected(String s);
    public void onRoomCreated(int i, Room room);
    public void onJoinedRoom(int i, Room room);
    public void onLeftRoom(int i, String s);
    public void onRoomConnected(int i, Room room);
}
