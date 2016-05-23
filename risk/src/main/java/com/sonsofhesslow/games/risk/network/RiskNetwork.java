package com.sonsofhesslow.games.risk.network;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesStatusCodes;
import com.google.android.gms.games.multiplayer.Invitation;
import com.google.android.gms.games.multiplayer.Multiplayer;
import com.google.android.gms.games.multiplayer.Participant;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessage;
import com.google.android.gms.games.multiplayer.realtime.Room;
import com.google.android.gms.games.multiplayer.realtime.RoomConfig;
import com.google.example.games.basegameutils.BaseGameUtils;
import com.sonsofhesslow.games.risk.Controller;
import com.sonsofhesslow.games.risk.MainActivity;
import com.sonsofhesslow.games.risk.R;
import com.sonsofhesslow.games.risk.graphics.Geometry.Vector2;
import com.sonsofhesslow.games.risk.graphics.GraphicsManager;
import com.sonsofhesslow.games.risk.model.Player;
import com.sonsofhesslow.games.risk.model.Territory;

import java.util.ArrayList;
import java.util.List;

public class RiskNetwork implements GooglePlayNetworkCompatible {
    public static Resources resources;

    final static String TAG = "Risk";

    // Request codes for the UIs that is shown with startActivityForResult:
    final static int RC_WAITING_ROOM = 10002;
    // Request code used to invoke sign in user interactions.
    private static final int RC_SIGN_IN = 9001;

    // Client used to interact with Google APIs.
    private GoogleApiClient mGoogleApiClient;

    // Currently resolving a connection failure?
    private boolean mResolvingConnectionFailure = false;

    // Has the user clicked the sign-in button?
    private boolean mSignInClicked = false;

    // Set to true to automatically start the sign in flow when the Activity starts.
    // Set to false to require the user to click the button in order to sign in.
    private boolean mAutoStartSignInFlow = false;

    // Room ID where the currently active game is taking place
    String mRoomId = null;

    // Playing in multiplayer mode?
    boolean mMultiplayer = false;

    // The participants in the currently active game
    ArrayList<Participant> mParticipants = null;

    // My participant ID in the currently active game
    String mMyId = null;

    // Id of the invitation player received via the invitation listener
    String mIncomingInvitationId = null;

    //google network in use
    private GooglePlayNetwork googlePlayNetwork = null;

    //activity in use
    private MainActivity activity;


    public RiskNetwork(MainActivity activity, GoogleApiClient mGoogleApiClient) {
        this.activity = activity;
        this.mGoogleApiClient = mGoogleApiClient;
    }

    private boolean selfModified = false;

    // Handle the result of the invitation inbox UI, where the player can pick an invitation to accept.
    private void handleInvitationInboxResult(int response, Intent data) {
        if (response != Activity.RESULT_OK) {
            Log.w(TAG, "*** invitation inbox UI cancelled, " + response);
            switchToMainScreen();
            return;
        }

        Log.d(TAG, "Invitation inbox UI succeeded.");
        Invitation inv = data.getExtras().getParcelable(Multiplayer.EXTRA_INVITATION);

        // accept invitation
        acceptInviteToRoom(inv.getInvitationId());
    }

    // Accept the given invitation.
    void acceptInviteToRoom(String invId) {
        Log.d(TAG, "Accepting invitation: " + invId);
        RoomConfig.Builder roomConfigBuilder = RoomConfig.builder(googlePlayNetwork);
        roomConfigBuilder.setInvitationIdToAccept(invId)
                .setMessageReceivedListener(googlePlayNetwork)
                .setRoomStatusUpdateListener(googlePlayNetwork);
        switchToScreen(R.id.screen_wait);
        //keepScreenOn();
        resetGameVars();
        Games.RealTimeMultiplayer.join(mGoogleApiClient, roomConfigBuilder.build());
    }

    // Activity is going to the background. Leave the current room.
   //onstop removed

    //onstart removed

    // Handle back key to make sure player cleanly leave a game if player are in the middle of one
    // onkeydown removed

    // Leave the room.
    //leaveroom removed

    // Show the waiting room UI
    void showWaitingRoom(Room room) {
        // minimum number of players required for our game
        // For simplicity, it's required for everyone to join the game before it's started
        final int MIN_PLAYERS = Integer.MAX_VALUE;
        Intent i = Games.RealTimeMultiplayer.getWaitingRoomIntent(mGoogleApiClient, room, MIN_PLAYERS);

        // show waiting room UI
        activity.startActivityForResult(i, RC_WAITING_ROOM);
    }

    public void onInvitationReceived(Invitation invitation) {
        //invitation to play a game, store it in mIncomingInvitationId and show popup on screen
        mIncomingInvitationId = invitation.getInvitationId();
        ((TextView) activity.findViewById(R.id.incoming_invitation_text)).setText(
                invitation.getInviter().getDisplayName() + " " +
                        activity.getString(R.string.is_inviting_you));
        switchToScreen(mCurScreen); // This will show the invitation popup
    }

    public void onInvitationRemoved(String invitationId) {
        if (mIncomingInvitationId.equals(invitationId)&&mIncomingInvitationId!=null) {
            mIncomingInvitationId = null;
            switchToScreen(mCurScreen); //hide the invitation popup
        }
    }

    //CALLBACKS SECTION. (API callbacks)

    public void onConnected(Bundle connectionHint) {
        Log.d(TAG, "onConnected() called. Sign in successful!");

        Log.d(TAG, "Sign-in succeeded.");

        //to be notified when invited to play
        System.out.println("mgapiclient: " + mGoogleApiClient + " googlePlayNetwork: " + googlePlayNetwork);
        Games.Invitations.registerInvitationListener(mGoogleApiClient, googlePlayNetwork);

        if (connectionHint != null) {
            Log.d(TAG, "onConnected: connection hint provided. Checking for invite.");
            Invitation inv = connectionHint
                    .getParcelable(Multiplayer.EXTRA_INVITATION);
            if (inv != null && inv.getInvitationId() != null) {
                // retrieve and cache the invitation ID
                Log.d(TAG,"onConnected: connection hint has a room invite!");
                acceptInviteToRoom(inv.getInvitationId());
                return;
            }
        }
        switchToMainScreen();
    }

    public void onConnectionSuspended(int i) {
        Log.d(TAG, "onConnectionSuspended() called. Trying to reconnect.");
        mGoogleApiClient.connect();
    }

    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed() called, result: " + connectionResult);

        if (mResolvingConnectionFailure) {
            Log.d(TAG, "onConnectionFailed() ignoring connection failure; already resolving.");
            return;
        }

        if (mSignInClicked || mAutoStartSignInFlow) {
            mAutoStartSignInFlow = false;
            mSignInClicked = false;
            mResolvingConnectionFailure = BaseGameUtils.resolveConnectionFailure(activity, mGoogleApiClient,
                    connectionResult, RC_SIGN_IN, activity.getString(R.string.signin_other_error));
        }

        switchToScreen(R.id.screen_sign_in);
    }

    //connected to the room, (not playing yet)
    public void onConnectedToRoom(Room room) {
        Log.d(TAG, "onConnectedToRoom.");
        //get participants and my ID:
        mParticipants = room.getParticipants();
        mMyId = room.getParticipantId(Games.Players.getCurrentPlayerId(mGoogleApiClient));

        // save room ID if its not initialized in onRoomCreated() so player can leave cleanly before the game starts.
        if(mRoomId==null)
            mRoomId = room.getRoomId();

        // print out the list of participants (for debug purposes)
        Log.d(TAG, "Room ID: " + mRoomId);
        Log.d(TAG, "My ID " + mMyId);
        Log.d(TAG, "<< CONNECTED TO ROOM>>");
    }

    // Called when player successfully left the room (this happens a result of voluntarily leaving via a call to leaveRoom().
    public void onLeftRoom(int statusCode, String roomId) {
        // player have left the room; return to main screen.
        Log.d(TAG, "onLeftRoom, code " + statusCode);
        switchToMainScreen();
    }

    // Called when player  get disconnected from the room. User returns to the main screen.
    public void onDisconnectedFromRoom(Room room) {
        mRoomId = null;
        showGameError();
    }

    // Show error message about game being cancelled and return to main screen.
    void showGameError() {
        BaseGameUtils.makeSimpleDialog(activity, activity.getString(R.string.game_problem));
        switchToMainScreen();
    }

    // room has been created
    public void onRoomCreated(int statusCode, Room room) {
        Log.d(TAG, "onRoomCreated(" + statusCode + ", " + room + ")");
        if (statusCode != GamesStatusCodes.STATUS_OK) {
            Log.e(TAG, "*** Error: onRoomCreated, status " + statusCode);
            showGameError();
            return;
        }

        // save room ID so player can leave cleanly before the game starts.
        mRoomId = room.getRoomId();

        // show the waiting room UI
        showWaitingRoom(room);
    }
    // room is fully connected.
    @Override
    public void onRoomConnected(int statusCode, Room room) {
        Log.d(TAG, "onRoomConnected(" + statusCode + ", " + room + ")");
        int c=0;
        int[] ids = new int[mParticipants.size()];
        for(Participant p : mParticipants){
            System.out.println("id: " + p.getParticipantId() + " hc: " + p.getParticipantId().hashCode());

            ids[c++] = p.getParticipantId().hashCode(); //@hash collisions
        }

        mMultiplayer = true;
        System.out.println("online online");
        activity.startGame(true, ids, mParticipants);
        if (statusCode != GamesStatusCodes.STATUS_OK) {

            Log.e(TAG, "*** Error: onRoomConnected, status " + statusCode);
            showGameError();
            return;
        }
        updateRoom(room);
    }

    @Override
    public void onJoinedRoom(int statusCode, Room room) {
        Log.d(TAG, "onJoinedRoom(" + statusCode + ", " + room + ")");
        if (statusCode != GamesStatusCodes.STATUS_OK) {
            Log.e(TAG, "*** Error: onRoomConnected, status " + statusCode);
            showGameError();
            return;
        }

        // show the waiting room UI
        showWaitingRoom(room);
    }

    // TODO: 2016-05-13 useful things, not just update
    @Override
    public void onPeerDeclined(Room room, List<String> arg1) {
        updateRoom(room);
    }

    @Override
    public void onPeerInvitedToRoom(Room room, List<String> arg1) {
        updateRoom(room);
    }

    @Override
    public void onP2PDisconnected(String participant) {
    }

    @Override
    public void onP2PConnected(String participant) {
    }

    @Override
    public void onPeerJoined(Room room, List<String> arg1) {
        updateRoom(room);
    }

    @Override
    public void onPeerLeft(Room room, List<String> peersWhoLeft) {
        updateRoom(room);
    }

    @Override
    public void onRoomAutoMatching(Room room) {
        updateRoom(room);
    }

    @Override
    public void onRoomConnecting(Room room) {
        updateRoom(room);
    }

    @Override
    public void onPeersConnected(Room room, List<String> peers) {
        updateRoom(room);
    }

    @Override
    public void onPeersDisconnected(Room room, List<String> peers) {
        updateRoom(room);
    }

    void updateRoom(Room room) {
        if (room != null) {
            mParticipants = room.getParticipants();
        }
        if (mParticipants != null) {
            // TODO: 2016-05-13 update variables from participants
        }
    }

    //removed startgame

    //COMMUNICATIONS SECTION. Methods that implement the game's network protocol

    // Called when received message from the network (updates from other players).
    @Override


    public void onRealTimeMessageReceived(RealTimeMessage rtm) {
        byte[] messageBuffer = rtm.getMessageData();
        String sender = rtm.getSenderParticipantId();

        selfModified = true;

        try {
            RiskNetworkMessage recievedNetworkData = RiskNetworkMessage.deSerialize(messageBuffer);


            activity.getController().refreshGamePhase();

            switch (recievedNetworkData.action) {
                case armyAmountChange: {
                    System.out.println("rtmr region changed");
                    Territory changedTerritory = Controller.getTerritoryById(recievedNetworkData.regionId);
                    if(changedTerritory!=null) {
                        changedTerritory.setArmyCount(recievedNetworkData.troups);
                    }
                    else {
                        System.out.println("illegal region index");
                    }
                }
                break;
                case occupierChange: {
                    System.out.println("rtmr in owner changed");
                    Territory changedTerritory = Controller.getTerritoryById(recievedNetworkData.regionId);
                    Player newOccupier = null;

                    for(Player p : Controller.getRiskModel().getPlayers()) {
                        if(p.getParticipantId() == recievedNetworkData.participantId) {
                            System.out.println("found owner");
                            newOccupier = p;
                            break;
                        }
                    }
                    if(changedTerritory!=null) {
                        changedTerritory.setOccupier(newOccupier);
                    }
                    else {
                        System.out.println("illegal region index");
                    }
                }
                break;
                case turnChange: {
                    System.out.println("rtmr turnchange");
                    activity.getController().nextPlayer();
                    /*int playerIndex = 0;
                    int amountOfPlayers = Controller.riskModel.getPlayers().length;
                    for(int i = 0; i < amountOfPlayers; i++) {
                        if(recievedNetworkData.participantId == Controller.riskModel.getPlayers()[i].getParticipantId()){
                            playerIndex = i;
                        }
                    }
                    if(playerIndex == amountOfPlayers - 1){
                        System.out.printf("new players turn (wrap turn)");
                        Controller.riskModel.setCurrentPlayer(Controller.riskModel.getPlayers()[0]);
                    } else {
                        System.out.println("new players turn");
                        Controller.riskModel.setCurrentPlayer(Controller.riskModel.getPlayers()[playerIndex + 1]);
                    }*/
                }
                break;
                default:{
                    System.out.println("network failure");
                    BaseGameUtils.makeSimpleDialog(activity, "Unknown network failure.\n(Please send an email to onetapchap@gmail.com and tell us how this happend, thank you!)");
                }
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }

        selfModified = false;

        GraphicsManager.requestRender();
    }

    // Broadcast to everybody else.
    public void broadcast(byte[] messageArray) {
        if(!selfModified && googlePlayNetwork != null) {
            System.out.println("broadbast risknetwork");
            ArrayList<Participant> targetParticipants = new ArrayList<Participant>();

            for(Participant participant : mParticipants) {
                if (participant.getParticipantId().equals(mMyId)) {
                    //should not be sending message to own phone
                    continue;
                }
                if (participant.getStatus() != Participant.STATUS_JOINED) {
                    //should not be sending message if player has not joined properly
                    continue;
                }

                targetParticipants.add(participant);
            }

            googlePlayNetwork.broadcast(messageArray, mGoogleApiClient, targetParticipants, mRoomId);
        }
    }


    //UI SECTION. Methods that implement the game's UI.

    // This array lists all the individual screens our game has.
    final static int[] SCREENS = {
            R.id.screen_game, R.id.screen_main, R.id.screen_sign_in,
            R.id.screen_wait
    };
    int mCurScreen = -1;

    void switchToScreen(int screenId) {
        // make the requested screen visible; hide all others.
        for (int id : SCREENS) {
            activity.findViewById(id).setVisibility(screenId == id ? View.VISIBLE : View.GONE);
        }
        mCurScreen = screenId;

        // show invitation popup?
        boolean showInvPopup;
        if (mIncomingInvitationId == null) {
            // no invitation, so no popup
            showInvPopup = false;
        } else if (mMultiplayer) {
            // if in multiplayer, only show invitation on main screen
            showInvPopup = (mCurScreen == R.id.screen_main);
        } else {
            showInvPopup = true;
            //could change to for example (mCurScreen == R.id.screen_main && <ingame_condition>);
            //(maybe toggleable in settings)
        }
        activity.findViewById(R.id.invitation_popup).setVisibility(showInvPopup ? View.VISIBLE : View.GONE);
    }

    void switchToMainScreen() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            switchToScreen(R.id.screen_main);
        }
        else {
            switchToScreen(R.id.screen_sign_in);
        }
    }

    //MISC SECTION. Miscellaneous methods

    private void resetGameVars() {
        // TODO: 2016-05-13
    }

    public void setGooglePlayNetwork(GooglePlayNetwork googlePlayNetwork) {
        this.googlePlayNetwork = googlePlayNetwork;
    }

    public String getmMyId() {
        return mMyId;
    }

    // Handle the result of the "Select players UI", launched when the user clicked the
    // "Invite friends" button. Creating a room with selected players.
    public void handleSelectPlayersResult(int response, Intent data) {
        if (response != Activity.RESULT_OK) {
            Log.w(TAG, "*** select players UI cancelled, " + response);
            switchToMainScreen();
            return;
        }

        Log.d(TAG, "Select players UI succeeded.");

        // get the invitee list
        final ArrayList<String> invitees = data.getStringArrayListExtra(Games.EXTRA_PLAYER_IDS);
        Log.d(TAG, "Invitee count: " + invitees.size());
        //this.playersToInvite = invitees;

        // get the automatch criteria
        Bundle autoMatchCriteria = null;
        int minAutoMatchPlayers = data.getIntExtra(Multiplayer.EXTRA_MIN_AUTOMATCH_PLAYERS, 0);
        int maxAutoMatchPlayers = data.getIntExtra(Multiplayer.EXTRA_MAX_AUTOMATCH_PLAYERS, 0);
        if (minAutoMatchPlayers > 0 || maxAutoMatchPlayers > 0) {
            autoMatchCriteria = RoomConfig.createAutoMatchCriteria(
                    minAutoMatchPlayers, maxAutoMatchPlayers, 0);
            Log.d(TAG, "Automatch criteria: " + autoMatchCriteria);
        }
        //this.matchCriteria = autoMatchCriteria;

        // create the room
        Log.d(TAG, "Creating room...");
        RoomConfig.Builder rtmConfigBuilder = RoomConfig.builder(googlePlayNetwork);
        rtmConfigBuilder.addPlayersToInvite(invitees);
        rtmConfigBuilder.setMessageReceivedListener(googlePlayNetwork);
        rtmConfigBuilder.setRoomStatusUpdateListener(googlePlayNetwork);
        if (autoMatchCriteria != null) {
            rtmConfigBuilder.setAutoMatchCriteria(autoMatchCriteria);
        }

        switchToScreen(R.id.screen_wait);
        resetGameVars();

        Games.RealTimeMultiplayer.create(mGoogleApiClient, rtmConfigBuilder.build());
        Log.d(TAG, "Room created, waiting for it to be ready...");
    }
}
