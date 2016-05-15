package com.sonsofhesslow.games.risk;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

public class NetworkMessage {
    public static NetworkMessage deseirialize(byte[] bytebuffer)
    {
        ByteBuffer dlb = ByteBuffer.allocateDirect(bytebuffer.length);
        dlb.put(bytebuffer);
        dlb.position(0);
        IntBuffer intBuf = ByteBuffer.wrap(bytebuffer).order(ByteOrder.BIG_ENDIAN).asIntBuffer();
        int[] arr = new int[intBuf.remaining()];
        intBuf.get(arr);
        return new NetworkMessage(arr);
    }

    int[] values;
    public NetworkMessage(int[] values) {
        this.values = values;
    }

    public static NetworkMessage territoryChangedMessageBuilder(Territory territory, int newTroops){
        System.out.println("in territory change message builder");

        System.out.println("region id: " + territory.getId() + " value: " + territory.getArmyCount() + " occupier" + territory.getOccupier());
        int[] valueArray = {NetworkAction.regionTroupsChange.getValue(), territory.getId(), newTroops};
        System.out.println("region id: " + territory.getId() + " value: " + territory.getArmyCount() + " occupier" + territory.getOccupier());

        return new NetworkMessage(valueArray);
    }

    public static NetworkMessage ownerChangedMessageBuilder(Territory territory, Player newOccupier){
        System.out.println("in owner change message builder");

        System.out.println("region id: " + territory.getId() + " value: " + territory.getArmyCount() + " occupier" + territory.getOccupier());
        int[] valueArray = {NetworkAction.ownerChange.getValue(), territory.getId(), newOccupier.getParticipantId()};
        System.out.println("region id: " + territory.getId() + " value: " + territory.getArmyCount() + " occupier" + territory.getOccupier());

        return new NetworkMessage(valueArray);
    }

    public static NetworkMessage turnChangedMessageBuilder(Player currentPlayerDone){
        System.out.println("in turn change message builder");

        System.out.println("participant  id: " + currentPlayerDone.getParticipantId());
        int[] valueArray = {NetworkAction.turnChange.getValue(), currentPlayerDone.getParticipantId()};

        return new NetworkMessage(valueArray);
    }

    public int getRegionID() {
        return values[1];
    }
    public int getValue() {
        return values[2];
    }
    public int getOccupierId() {
        return values[2];
    }
    public int getNewPlayerId() {
        return values[1];
    }

    public byte[] serialize() {
        ByteBuffer dlb = ByteBuffer.allocateDirect(values.length * 4);
        IntBuffer buffer = dlb.asIntBuffer();
        buffer.put(values);
        buffer.position(0);
        dlb.order(ByteOrder.BIG_ENDIAN);
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
