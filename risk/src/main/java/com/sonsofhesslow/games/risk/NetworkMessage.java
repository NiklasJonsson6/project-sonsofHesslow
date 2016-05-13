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
        action = getAction(values[0]);
    }

    public static NetworkMessage territoryChangedMessageBuilder(Territory territory){
        int[] valueArray = {NetworkAction.regionTroupsChange.getValue(), territory.getId(), territory.getArmyCount()};
        return new NetworkMessage(valueArray);
    }

    public static NetworkMessage ownerChangedMessageBuilder(Territory territory){
        int[] valueArray = {NetworkAction.ownerChange.getValue(), };
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
        return values[1];
    }

    public void send(MainActivity activity) {
        int[] arr = new int[values.length + 1];
        arr[0] = action.getValue();
        for(int i = 1; i < values.length + 1; i++){
            arr[i] = values[i];
        }

        ByteBuffer dlb = ByteBuffer.allocateDirect(arr.length * 4);
        IntBuffer buffer = dlb.asIntBuffer();
        buffer.put(arr);
        buffer.position(0);
        byte[] message = dlb.array();
        activity.broadcast(message,true);
    }

    private static NetworkAction getAction(int value) {
        for(NetworkAction action: NetworkAction.values()) {
            if(action.value == value) {
                return action;
            }
        }
        return null;
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
