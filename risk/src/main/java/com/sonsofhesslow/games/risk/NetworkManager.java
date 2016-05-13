package com.sonsofhesslow.games.risk;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

public class NetworkManager {
    MainActivity activity;
    boolean selfModified;
    public NetworkManager(Risk risk, final MainActivity activity) {
        this.activity = activity;
        for(final Territory territory : risk.getTerritories())
        {
            territory.addTroupListeners(new Territory.TroupChangeListener() {
                @Override
                public void handle(Territory.TroupChangeEvent troupChangeEvent) {
                    if (!selfModified) {
                        NetworkMessage message = NetworkMessage.territoryChangedMessageBuilder(territory);
                        message.send(activity);
                    }
                }
            });
            territory.addOwnerListeners(new Territory.OwnerChangeListener() {
                @Override
                public void handle(Territory.OwnerChangeEvent ownerChangeEvent) {
                    if (!selfModified) {
                        NetworkMessage message = NetworkMessage.ownerChangedMessageBuilder(territory);
                        message.send(activity);
                    }
                }
            });
        }
    }
}
