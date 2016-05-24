package com.sonsofhesslow.games.risk;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesActivityResultCodes;
import com.google.android.gms.games.multiplayer.Invitation;
import com.google.android.gms.games.multiplayer.Multiplayer;
import com.google.android.gms.games.multiplayer.Participant;
import com.google.android.gms.games.multiplayer.realtime.RoomConfig;
import com.google.example.games.basegameutils.BaseGameUtils;
import com.sonsofhesslow.games.risk.graphics.GL_TouchEvent;
import com.sonsofhesslow.games.risk.graphics.GL_TouchListener;
import com.sonsofhesslow.games.risk.graphics.Geometry.Vector2;
import com.sonsofhesslow.games.risk.graphics.Geometry.Vector3;
import com.sonsofhesslow.games.risk.graphics.Camera;
import com.sonsofhesslow.games.risk.graphics.MyGLRenderer;
import com.sonsofhesslow.games.risk.graphics.MyGLSurfaceView;
import com.sonsofhesslow.games.risk.model.Player;
import com.sonsofhesslow.games.risk.model.Risk;
import com.sonsofhesslow.games.risk.network.GooglePlayNetwork;
import com.sonsofhesslow.games.risk.network.RiskNetwork;
import com.sonsofhesslow.games.risk.network.RiskNetworkManager;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
        implements GL_TouchListener, View.OnClickListener
        //, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        // RoomStatusUpdateListener, RoomUpdateListener, OnInvitationReceivedListener
        {

    public static Resources resources;
    public static Context context;
    static Overlay newOverlayController;
    MyGLSurfaceView graphicsView;
    private Controller controller;


    final static String TAG = "Risk";

    // Request codes for the UIs that is shown with startActivityForResult:
    final static int RC_SELECT_PLAYERS = 10000;
    final static int RC_INVITATION_INBOX = 10001;
    final static int RC_WAITING_ROOM = 10002;

    // Request code used to invoke sign in user interactions.
    private static final int RC_SIGN_IN = 9001;

    // Client used to interact with Google APIs.
    private GoogleApiClient mGoogleApiClient;

    // Room ID where the currently active game is taking place
    String mRoomId = null;

    // Playing in multiplayer mode?
    boolean mMultiplayer = false;
    public boolean mSignInClicked = false;
    // The participants in the currently active game
    ArrayList<Participant> mParticipants = null;

    // Id of the invitation player received via the invitation listener
    String mIncomingInvitationId = null;

    Vector2 prevPos;

    GooglePlayNetwork googlePlayNetwork = null;

    private RiskNetwork riskNetwork = null;

    RiskNetworkManager riskNetworkManager = null;

    private LinearLayout mainLayout;

    LayoutInflater inflater = null;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;
        newOverlayController = new Overlay(this);
        graphicsView = new MyGLSurfaceView(this,getResources());
        graphicsView.addListener(this);

        //this.riskNetwork = new RiskNetwork(this);

        googlePlayNetwork = new GooglePlayNetwork();

        // Create the Google Api Client with access to Games
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(googlePlayNetwork)
                .addOnConnectionFailedListener(googlePlayNetwork)
                .addApi(Games.API).addScope(Games.SCOPE_GAMES)
                .build();

        riskNetwork = new RiskNetwork(this, this.mGoogleApiClient);

        riskNetwork.setGooglePlayNetwork(googlePlayNetwork);
        googlePlayNetwork.setNetworkTarget(riskNetwork);

        // set up a click listener for everything in main menus
        for (int id : CLICKABLES) {
            findViewById(id).setOnClickListener(this);
        }

        this.mainLayout = new LinearLayout(this);

        inflater = (LayoutInflater)context.getSystemService
                (Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.activity_main, mainLayout);
    }

    public void handle(GL_TouchEvent event) {

        if (prevPos != null) {
            //sq.setPos(event.worldPosition);
            Vector2 delta;
            if (!event.isZooming) {
                delta = Vector2.Sub(MyGLRenderer.screenToWorldCoords(prevPos, 0), event.worldPosition);
            } else {
                delta = new Vector2(0, 0);
            }
            //System.out.println("delta:" + delta);
            switch (event.e.getAction()) {
                case MotionEvent.ACTION_MOVE:
                    Camera cam = Camera.getInstance();
                    cam.setPosRel(new Vector3(delta, event.scale));
                    break;
            }
        }

        //System.out.println("Screen grej" + event.screenPosition.y);
        graphicsView.requestRender();
        prevPos = event.screenPosition;
    }

    public void onClick(View v) {
        Intent intent;

        switch (v.getId()) {
            case R.id.button_single_player:
            case R.id.button_single_player_2:
                // play a single-player game
                System.out.println("online singleplayer");
                startGame(false, new int[2], null);
                break;
            case R.id.button_sign_in:
                Log.d(TAG, "Sign-in button clicked");
                mSignInClicked = true;
                mGoogleApiClient.connect();
                break;
            case R.id.button_sign_out:
                // user wants to sign out
                Log.d(TAG, "Sign-out button clicked");
                Games.signOut(mGoogleApiClient);
                mGoogleApiClient.disconnect();
                switchToScreen(R.id.screen_sign_in);
                break;
            case R.id.button_invite_players:
                // show list of invitable players
                intent = Games.RealTimeMultiplayer.getSelectOpponentsIntent(mGoogleApiClient, 1, 3);
                switchToScreen(R.id.screen_wait);
                startActivityForResult(intent, RC_SELECT_PLAYERS);
                break;
            case R.id.button_see_invitations:
                // show list of pending invitations
                intent = Games.Invitations.getInvitationInboxIntent(mGoogleApiClient);
                switchToScreen(R.id.screen_wait);
                startActivityForResult(intent, RC_INVITATION_INBOX);
                break;
            case R.id.button_accept_popup_invitation:
                // user wants to accept the invitation shown on the invitation popup (from OnInvitationReceivedListener).
                acceptInviteToRoom(mIncomingInvitationId);
                mIncomingInvitationId = null;
                break;
            case R.id.button_quick_game:
                // user wants to play against a random opponent right now
                startQuickGame();
                break;
        }
    }

    void startQuickGame() {
        //1-3 opponents
        final int MIN_OPPONENTS = 1, MAX_OPPONENTS = 3;
        Bundle autoMatchCriteria = RoomConfig.createAutoMatchCriteria(MIN_OPPONENTS,
                MAX_OPPONENTS, 0);
        //this.matchCriteria = autoMatchCriteria;
        RoomConfig.Builder rtmConfigBuilder = RoomConfig.builder(googlePlayNetwork);
        rtmConfigBuilder.setMessageReceivedListener(googlePlayNetwork);
        rtmConfigBuilder.setRoomStatusUpdateListener(googlePlayNetwork);
        rtmConfigBuilder.setAutoMatchCriteria(autoMatchCriteria);
        switchToScreen(R.id.screen_wait);
        keepScreenOn();
        resetGameVars();
        Games.RealTimeMultiplayer.create(mGoogleApiClient, rtmConfigBuilder.build());
    }

    @Override
    public void onActivityResult(int requestCode, int responseCode,
                                 Intent intent) {
        super.onActivityResult(requestCode, responseCode, intent);
        switch (requestCode) {
            case RC_SELECT_PLAYERS:
                // result from "select players" UI -- ready to create the room
                riskNetwork.handleSelectPlayersResult(responseCode, intent);
                break;
            case RC_INVITATION_INBOX:
                // result from the "select invitation" UI (invitation inbox).
                handleInvitationInboxResult(responseCode, intent);
                break;
            case RC_WAITING_ROOM:
                // result from the "waiting room" UI.
                if (responseCode == Activity.RESULT_OK) {
                    // ready to start playing
                    Log.d(TAG, "Starting game (waiting room returned OK).");

                    // TODO: 2016-05-13 implement start game
                    //startGame(true, new int[2]);
                } else if (responseCode == GamesActivityResultCodes.RESULT_LEFT_ROOM) {
                    // player indicated that they want to leave the room
                    System.out.println("234 left room");
                    leaveRoom();
                } else if (responseCode == Activity.RESULT_CANCELED) {
                    // Dialog was cancelled (user pressed back key, for instance). Leaving room
                    System.out.println("234 result canceled");
                    leaveRoom();
                }
                break;
            case RC_SIGN_IN:
                Log.d(TAG, "onActivityResult with requestCode == RC_SIGN_IN, responseCode="
                        + responseCode + ", intent=" + intent);
                if (responseCode == Activity.RESULT_OK) {
                    mGoogleApiClient.connect();
                } else {
                    BaseGameUtils.showActivityResultError(this, requestCode,responseCode, R.string.signin_other_error);
                }
                break;
        }
        super.onActivityResult(requestCode, responseCode, intent);
    }


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
        keepScreenOn();
        resetGameVars();
        Games.RealTimeMultiplayer.join(mGoogleApiClient, roomConfigBuilder.build());
    }

    // Activity is going to the background. Leave the current room.
    public void onStop() {
        Log.d(TAG, "**** got onStop");

        // if player in a room, leave it.
        //leaveRoom();

        // stop trying to keep the screen on
        stopKeepingScreenOn();

        if (mGoogleApiClient == null || !mGoogleApiClient.isConnected()){
            switchToScreen(R.id.screen_sign_in);
        }
        else {
            switchToScreen(R.id.screen_wait);
        }
        System.out.println("234 end of onstop function");
        super.onStop();
    }

    public void onStart() {
        switchToScreen(R.id.screen_wait);
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            Log.w(TAG,
                    "GameHelper: client was already connected on onStart()");
        } else {
            Log.d(TAG,"Connecting client.");
            mGoogleApiClient.connect();
        }
        super.onStart();
    }

    // Handle back key to make sure player cleanly leave a game if player are in the middle of one
    public boolean onKeyDown(int keyCode, KeyEvent e) {
        if (keyCode == KeyEvent.KEYCODE_BACK && mCurScreen == R.id.screen_game) {
            setContentView(R.layout.activity_main);
            mCurScreen = R.id.screen_main;
            System.out.println("onkey leave room");
            leaveRoom();
            return true;
        }
        return super.onKeyDown(keyCode, e);
    }

    // Leave the room.
    public void leaveRoom() {
        Log.d(TAG, "Leaving room.");
        stopKeepingScreenOn();
        if (mRoomId != null) {
            //switchToScreen(R.id.screen_wait);
            mRoomId = null;
            Games.RealTimeMultiplayer.leave(mGoogleApiClient, googlePlayNetwork, mRoomId);
        } else {
            switchToMainScreen();
        }
    }

    public void startGame(boolean online, int[] ids, ArrayList<Participant> participants) {

        if(online) {
            initOnlineGame(ids);
            for(Player player : controller.getRiskModel().getPlayers()){
                for(Participant participant : participants) {
                    if(participant.getParticipantId().hashCode() == player.getParticipantId()) {
                        player.setName(participant.getDisplayName());
                        player.setImageRefrence(participant.getIconImageUri());
                    }
                }
            }
        } else {
            initOfflineGame(ids);
        }

        /*for(Player p : controller.getRiskModel().getPlayers()){
            System.out.println("par id: " + p.getParticipantId());
        }*/

        /*setContentView(R.layout.activity_overlay);
        View C = findViewById(R.id.Test);
        ViewGroup parent = (ViewGroup) C.getParent();
        int index = parent.indexOfChild(C);
        parent.removeView(C);
        C = graphicsView;
        parent.addView(C, index);*/
        //overlayController.addView(graphicsView);
        newOverlayController.addView(graphicsView);
        newOverlayController.addView(R.layout.activity_mainoverlay);
        //View overlay = factory.inflate(R.layout.activity_nextturn, null);
        //overlayController.addView(R.layout.activity_playerturn);
        //overlayController.addView(R.layout.activity_chooseterritory);
        //overlayController.addView(R.layout.activity_mainoverlay);
        graphicsView.addListener(controller);
        //setContentView(overlayController.getOverlay());
        setContentView(newOverlayController.getOverlay());
        newOverlayController.setGamePhase(Risk.GamePhase.PICK_TERRITORIES);
        mCurScreen = R.id.screen_game;
    }


    //UI SECTION. Methods that implement the game's UI.


    // This array lists everything that's clickable, for installing click event handlers.
    final static int[] CLICKABLES = {
            R.id.button_accept_popup_invitation, R.id.button_invite_players,
            R.id.button_quick_game, R.id.button_see_invitations, R.id.button_sign_in,
            R.id.button_sign_out, R.id.button_single_player,
            R.id.button_single_player_2
    };

    // This array lists all the individual screens our game has.
    final static int[] SCREENS = {
            R.id.screen_game, R.id.screen_main, R.id.screen_sign_in,
            R.id.screen_wait
    };

    int mCurScreen = -1;
    public void switchToScreen(int screenId) {
        // make the requested screen visible; hide all others.
        for (int id : SCREENS) {
            // TODO: 2016-05-24 fix for real?, not just null check
           if(findViewById(id) != null) {
               findViewById(id).setVisibility(screenId == id ? View.VISIBLE : View.GONE);
           }
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
        if(findViewById(R.id.invitation_popup) != null) {
            findViewById(R.id.invitation_popup).setVisibility(showInvPopup ? View.VISIBLE : View.GONE);
        }
        System.out.println("end of sts function");
    }

    void switchToMainScreen() {
        System.out.println("234 in switch to main screen");
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            System.out.println("234 switching to main screen");
            switchToScreen(R.id.screen_main);
        }
        else {
            System.out.println("234 switching to sign-in screen");

            switchToScreen(R.id.screen_sign_in);
        }
    }

    //GAME BUTTONS

    public void nextTurnPressed(View v) {
        //controller has always been init since nextTurn button
        //is not visible before startGame has been pressed
        //TODO give territories continents, setArmiesToPlace gives nullpointerexception when pressed
        Log.d(TAG, "nextturn pressed");
        controller.nextTurn();
    }

    public void showCardsPressed(View v) {
        //TODO show new layout with cards and trade in button
    }

    public void fightPressed(View v){
        controller.fightButtonPressed();
    }
    public void placePressed(View v){
        controller.placeButtonPressed(newOverlayController.getBarValue());
        System.out.println("Place button pressed");
    }
    public void donePressed(View v){
        controller.doneButtonPressed();
        newOverlayController.setNextTurnVisible(true);
        System.out.println("Done button pressed");
    }
    //MISC SECTION. Miscellaneous methods

    // Sets the flag to keep this screen on
    void keepScreenOn() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    // Clears the flag that keeps the screen on.
    void stopKeepingScreenOn() {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private void initOnlineGame(int[] ids) {
        System.out.println("init online game");
        System.out.println("risknetowrk_: " + riskNetwork + " \nmmyid: " + riskNetwork.getmMyId());
        this.controller = new Controller(ids, riskNetwork.getmMyId().hashCode(), newOverlayController);
        //this.riskNetwork = new RiskNetwork(this);
        this.riskNetworkManager = new RiskNetworkManager(controller.getRiskModel(), this.controller, this.riskNetwork);
        //riskNetwork.setGooglePlayNetwork(googlePlayNetwork);
        //googlePlayNetwork.setNetworkTarget(riskNetwork);
    }

    private void initOfflineGame(int[] ids) {
        this.controller = new Controller(ids, 0, newOverlayController);
    }

    private void resetGameVars() {
        // TODO: 2016-05-13
    }

    public Controller getController() {
        return controller;
    }

    public void showList(View v){
        newOverlayController.setListVisible(true);

    }

    public void hideList(View v){
        newOverlayController.setListVisible(false);
    }
    public ArrayList<Participant> getmParticipants() {
        return mParticipants;
    }

    /*private void populateListView(){
>>>>>>> List awesome
        //Elements
        String array[] = {"Daniel", "Arvid", "Niklas", "Fredrik"};
        int [] image = {R.drawable.downarrow};
        String count[] = {"5", "6", "7", "1337"};
        //Adapter
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.activity_playerinfo, array);
        ListView listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(new CustomAdapter(this, array, image, count));
    }*/
}
