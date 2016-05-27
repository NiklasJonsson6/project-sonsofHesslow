package com.sonsofhesslow.games.risk;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesActivityResultCodes;
import com.google.android.gms.games.multiplayer.Invitation;
import com.google.android.gms.games.multiplayer.Multiplayer;
import com.google.android.gms.games.multiplayer.Participant;
import com.google.android.gms.games.multiplayer.realtime.Room;
import com.google.example.games.basegameutils.BaseGameUtils;
import com.sonsofhesslow.games.risk.graphics.GLTouchEvent;
import com.sonsofhesslow.games.risk.graphics.GLTouchListener;
import com.sonsofhesslow.games.risk.graphics.GraphicsManager;
import com.sonsofhesslow.games.risk.graphics.geometry.Vector2;
import com.sonsofhesslow.games.risk.graphics.geometry.Vector3;
import com.sonsofhesslow.games.risk.graphics.Camera;
import com.sonsofhesslow.games.risk.graphics.MyGLRenderer;
import com.sonsofhesslow.games.risk.graphics.MyGLSurfaceView;
import com.sonsofhesslow.games.risk.model.Player;
import com.sonsofhesslow.games.risk.model.Risk;
import com.sonsofhesslow.games.risk.model.Territory;
import com.sonsofhesslow.games.risk.network.RiskNetworkManager;
import com.sonsofhesslow.games.risk.network.UIUpdate;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
        implements GLTouchListener, View.OnClickListener, UIUpdate{

    public static Resources resources;
    public static Context context;
    static Overlay newOverlayController;
    MyGLSurfaceView graphicsView;

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


    // Id of the invitation player received via the invitation listener
    String mIncomingInvitationId = null;

    Vector2 prevPos;

    private Controller controller;

    private RiskNetworkManager riskNetworkManager = null;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;
        graphicsView = new MyGLSurfaceView(this,getResources());
        newOverlayController = new Overlay(this);
        graphicsView.addListener(this);

        riskNetworkManager = new RiskNetworkManager(this,this);
        this.mGoogleApiClient = riskNetworkManager.getGoogleApiClient();

        // set up a click listener for everything in main menus
        for (int id : CLICKABLES) {
            findViewById(id).setOnClickListener(this);
        }
    }

    public void onConfigurationChanged(Configuration newConfig){
        super.onConfigurationChanged(newConfig);
        if(newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE){
            newOverlayController.changeGridLayout(true);
        } else {
            newOverlayController.changeGridLayout(false);
        }
    }

    public void handle(GLTouchEvent event) {

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

    //handles the menu-button events
    public void onClick(View v) {
        System.out.println("on click");
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
                riskNetworkManager.getRiskNetwork().signInClicked = true;
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
                riskNetworkManager.acceptInviteToRoom(mIncomingInvitationId);
                mIncomingInvitationId = null;
                break;
            case R.id.button_quick_game:
                // user wants to play against a random opponent right now
                startQuickGame();
                break;
        }
    }

    //starts an online game with random players
    void startQuickGame() {
        switchToScreen(R.id.screen_wait);
        resetGameVars();
        riskNetworkManager.startQuickGame();
    }

    @Override
    public void onActivityResult(int requestCode, int responseCode,
                                 Intent intent) {
        super.onActivityResult(requestCode, responseCode, intent);
        switch (requestCode) {
            case RC_SELECT_PLAYERS:
                // result from "select players" UI -- ready to create the room
                handleSelectPlayersResult(responseCode, intent);
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
                    leaveRoom();
                } else if (responseCode == Activity.RESULT_CANCELED) {
                    // Dialog was cancelled (user pressed back key, for instance). Leaving room
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
        switchToScreen(R.id.screen_wait);
        resetGameVars();
        riskNetworkManager.acceptInviteToRoom(inv.getInvitationId());
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

        switchToScreen(R.id.screen_wait);
        resetGameVars();
        riskNetworkManager.startInviteGame(data);
        Log.d(TAG, "Room created, waiting for it to be ready...");
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
            //switchToScreen(mCurScreen);
            setContentView(R.layout.activity_main);
            mCurScreen = R.id.screen_main;
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
            //Games.RealTimeMultiplayer.leave(mGoogleApiClient, googlePlayNetwork, mRoomId);
            Games.RealTimeMultiplayer.leave(mGoogleApiClient, riskNetworkManager.getGooglePlayNetwork(), mRoomId);
        } else {
            switchToMainScreen();
        }
    }

    public void startGame(boolean isOnline, int[] ids, ArrayList<Participant> participants) {

        if(graphicsView.getParent() != null) {
            setContentView(R.layout.activity_main);
            context = this;
            newOverlayController = new Overlay(this);
            graphicsView = new MyGLSurfaceView(this,getResources());
            graphicsView.addListener(this);

        }

        //keeps screen turned on until game is finnished
        keepScreenOn();

        if(isOnline) {
            initOnlineGame(ids);
            for(Player player : controller.getRiskModel().getPlayers()){
                for(Participant participant : participants) {
                    if(participant.getParticipantId().hashCode() == player.getParticipantId()) {
                        //getting GooglePlay's account information, adding to player
                        player.setName(participant.getDisplayName());
                        player.setImageRefrence(participant.getIconImageUri());
                    }
                }
            }
        } else {
            initOfflineGame(ids);
        }

        //((ViewGroup)graphicsView.getParent()).removeView(newOverlayController);

        System.out.println("graphicsview: " + graphicsView + " getparent: " + graphicsView.getParent());
        System.out.println("viewgroup: " +  graphicsView.getParent());
        if (graphicsView.getParent() != null) {
            //((ViewGroup)graphicsView.getParent()).removeAllViews();
            //newOverlayController.removeView(graphicsView.getId());
            System.out.println("after remove views");
        }

        //reset if returning after a game
        //newOverlayController.removeView(graphicsView.getId());

        System.out.println("poverlay234: " +newOverlayController.getOverlay());
        System.out.println("graview getpa: " + graphicsView.getParent());

        if(graphicsView.getParent() == null ) {
            newOverlayController.addView(graphicsView);
            System.out.println("after add graphics");
            newOverlayController.addView(R.layout.activity_mainoverlay);
            System.out.println("after activity mainoverlay");
            newOverlayController.addView(R.layout.activity_cards);
            System.out.println("after activity activity cards");

            graphicsView.addListener(controller);
        }
        System.out.println("after 1");
        setContentView(newOverlayController.getOverlay());
        System.out.println("after 2");
        newOverlayController.setGamePhase(Risk.GamePhase.PICK_TERRITORIES);
        System.out.println("after 3");
        mCurScreen = R.id.screen_game;
        System.out.println("after 4");
        newOverlayController.changeGridLayout(this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE);
        System.out.println("after 5");
    }

    public void startGame(boolean isOnline, ArrayList<Participant> participants) {
        startGame(isOnline, getParticipantIds(), participants);
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
            //always show in singleplayer
            showInvPopup = true;
            //could change to for example (mCurScreen == R.id.screen_main && <ingame_condition>);
            //(maybe toggleable in settings)
        }
        if(findViewById(R.id.invitation_popup) != null) {
            findViewById(R.id.invitation_popup).setVisibility(showInvPopup ? View.VISIBLE : View.GONE);
        }
    }

    void switchToMainScreen() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            for (int id : CLICKABLES) {
                findViewById(id).setOnClickListener(this);
            }
            switchToScreen(R.id.screen_main);
        }
        else {
            switchToScreen(R.id.screen_sign_in);
        }
    }


    //GAME BUTTONS SECTION - handle buttonevents from the buttons in game (not menu)

    public void nextTurnPressed(View v) {
        //controller has always been init since nextTurn button
        //is not visible before startGame has been pressed
        //TODO give territories continents, setArmiesToPlace gives nullpointerexception when pressed
        Log.d(TAG, "nextturn pressed");
        controller.nextTurn();
    }

    public void showCardsPressed(View v) {
        //TODO show new layout with cards and trade in button
        newOverlayController.setCardVisibility(true);
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

    public void hideCards(View v){
        newOverlayController.setCardVisibility(false);
        newOverlayController.setNextTurnVisible(true);
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
        this.controller = new Controller(ids, newOverlayController);
        controller.setSelfId(riskNetworkManager.getRiskNetwork().getmMyId().hashCode());
        //add to observables
        Risk riskModel = controller.getRiskModel();
        riskModel.addObserver(riskNetworkManager);
        for(Territory territory: riskModel.getTerritories()) {
                territory.addObserver(riskNetworkManager);
        }

        riskNetworkManager.getRiskNetwork().addListener(controller);
    }

    private void initOfflineGame(int[] ids) {
        this.controller = new Controller(ids, newOverlayController);
    }

    private void resetGameVars() {
        // TODO: 2016-05-13 if needed
    }

    public Controller getController() {
        return controller;
    }

    public void showList(View v){
        if(newOverlayController.listPopulated) {
            newOverlayController.setListVisible(true);
        }
    }

    public void hideList(View v){
        newOverlayController.setListVisible(false);
    }

    public void getCardsPressed(View v){
        if(newOverlayController.getSelectedCards().size() == 3) {
            controller.turnInCards(newOverlayController.getSelectedCards());
        }
    }

    public void setmGoogleApiClient(GoogleApiClient mGoogleApiClient) {
        this.mGoogleApiClient = mGoogleApiClient;
    }

    public int[] getParticipantIds(){
        ArrayList<Participant> participants = riskNetworkManager.getRiskNetwork().getmParticipants();

        int c = 0;
        int[] ids = new int[participants.size()];
        for(Participant participant : participants){
            ids[c++] = participant.getParticipantId().hashCode(); //@hash collisions
        }
        return ids;
    }

//setting up the callbacks for the network.
    @Override
    public void displayInvitation(String caller ) {
        //invitation to play a game, store it in mIncomingInvitationId and show popup on screen
        ((TextView) findViewById(R.id.incoming_invitation_text)).setText(
                caller + " " +
                        getString(R.string.is_inviting_you));
        switchToScreen(getmCurScreen()); // This will show the invitation popup
    }

    @Override
    public void removeInvitation() {
        switchToScreen(getmCurScreen()); //hide the invitation popup
    }

    @Override
    public void showWaitingRoom(Room room) {
        final int MIN_PLAYERS = Integer.MAX_VALUE;
        Intent i = Games.RealTimeMultiplayer.getWaitingRoomIntent(mGoogleApiClient, room, MIN_PLAYERS);
        startActivityForResult(i, RC_WAITING_ROOM);
    }

    @Override
    public void displayError() {
        BaseGameUtils.makeSimpleDialog(this, getString(R.string.game_problem));
        switchToMainScreen();
    }

    @Override
    public void startGame(ArrayList<Participant> participants) {
        startGame(true, participants);
    }

    @Override
    public void showMainScreen() {
        switchToScreen(R.id.screen_main);
    }

    @Override
    public void showSignInScreen() {
        switchToScreen(R.id.screen_sign_in);
    }

    @Override
    public void showWaitScreen() {
        switchToScreen(R.id.screen_wait);
    }

    @Override
    public boolean resolveConnection(ConnectionResult result) {
        return BaseGameUtils.resolveConnectionFailure(this, mGoogleApiClient,
                result, RC_SIGN_IN, getString(R.string.signin_other_error));
    }

    public int getmCurScreen() {
        return mCurScreen;
    }
}
