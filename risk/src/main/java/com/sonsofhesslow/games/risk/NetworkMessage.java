package com.sonsofhesslow.games.risk;

import android.webkit.JavascriptInterface;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.StreamCorruptedException;

public class NetworkMessage implements Serializable{
    private static final long serialVersionUID = 1L;
    static NetworkMessage deSerialize(byte[] arr) throws StreamCorruptedException,IOException,ClassNotFoundException {
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(arr));
        NetworkMessage message = (NetworkMessage) ois.readObject();
        return message;
    }
    byte[] serialize() throws IOException
    {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos= new ObjectOutputStream(bos);
        oos.writeObject(this);
        return bos.toByteArray();
    }

    enum NetworkAction
    {
        turnChange,
        troupChange,
        ownerChange,
    }

    public NetworkMessage(NetworkAction action, int troups, int participantId, int regionId) {
        this.action = action;
        this.troups = troups;
        this.participantId = participantId;
        this.regionId = regionId;
    }

    public NetworkAction action;
    public int troups;
    public int participantId;
    public int regionId;

    public static NetworkMessage territoryChangedMessageBuilder(Territory territory, int newTroops){
        return new NetworkMessage(NetworkAction.troupChange,newTroops,-1,territory.getId());
    }

    public static NetworkMessage ownerChangedMessageBuilder(Territory territory, Player newOccupier){
        return new NetworkMessage(NetworkAction.ownerChange,-1,newOccupier.getParticipantId(),territory.getId());
    }

    public static NetworkMessage turnChangedMessageBuilder(Player currentPlayerDone){
        return new NetworkMessage(NetworkAction.turnChange,-1,currentPlayerDone.getParticipantId(),-1);
    }
}

