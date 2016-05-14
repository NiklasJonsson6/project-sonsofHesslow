package com.sonsofhesslow.games.risk;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

/**
 * Created by Arvid on 2016-05-13.
 */
public class NetworkMessage {
    public static NetworkMessage deseirialize(byte[] bytebuffer)
    {
        ByteBuffer dlb = ByteBuffer.allocateDirect(bytebuffer.length * 4);
        dlb.put(bytebuffer);
        dlb.position(0);
        int[] arr = dlb.asIntBuffer().array();
        return new NetworkMessage(arr);
    }

    MainActivity activity;
    int[] values;
    NetworkAction action;
    public NetworkMessage(int[] values) {
        this.values = values;
    }

    public static NetworkMessage territoryChangedMessageBuilder(Territory territory){
        int[] valueArray = {NetworkAction.regionTroupsChange.getValue(), territory.getId(), territory.getArmyCount()};
        return new NetworkMessage(valueArray);
    }

    public static NetworkMessage ownerChangedMessageBuilder(Territory territory){
        int[] valueArray = {NetworkAction.ownerChange.getValue(), territory.getId(), territory.getOccupier().getParticipantId() };
        return new NetworkMessage(valueArray);
    }

    //NetworkAction actions;
    //private int regionID;
    //private int value;

    public int getRegionID() {
        return values[1];
    }
    public int getValue() {
        return values[2];
    }
    public int getOccupierId() {
        return values[2];
    }

    public byte[] serialize() {
        ByteBuffer dlb = ByteBuffer.allocateDirect(values.length * 4);
        IntBuffer buffer = dlb.asIntBuffer();
        buffer.put(values);
        buffer.position(0);
        return dlb.array();
    }

    public NetworkAction getAction() {
        return NetworkAction.values()[values[0]];
    }

    public enum NetworkAction {
        regionTroupsChange(0),
        ownerChange(1),
        turnChange(2);

        private final int value;
        NetworkAction(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }
}
