import api.common.GameServer;
import api.listener.Listener;
import api.listener.events.player.PlayerChatEvent;
import api.mod.StarLoader;
import it.unimi.dsi.fastutil.shorts.ShortArrayList;
import org.schema.game.common.controller.elements.jumpprohibiter.InterdictionAddOn;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.game.common.data.blockeffects.config.StatusEffectType;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.game.server.data.PlayerNotFountException;

public class debugStuff {
    /*
    public debugStuff() {
        StarLoader.registerListener(PlayerChatEvent.class, new Listener<PlayerChatEvent>() {
            @Override
            public void onEvent(PlayerChatEvent playerChatEvent) {  //playerChat for setting up stations TODO automate through custom reactor addon
                chatEvent(playerChatEvent);
            }
        });
    }
    private void chatEvent(PlayerChatEvent event) {

        //check last chattime, has to be at least half a second later
        long timeNow = System.currentTimeMillis();
        if (timeNow - lastChatTime <= 500) {
            //double event
        } else {    //call event handlers
            //save time of event (server tends to call events twice at the same time
            lastChatTime = System.currentTimeMillis();
            //create all relevant information
            chatDebug("message registered");
            String text = event.getText();
            String playerName = event.getMessage().sender;
            PlayerState player = null;
            SimpleTransformableSendableObject ship = null;

            //get player
            try {
                player = GameServer.getServerState().getPlayerFromName(playerName);
            } catch (PlayerNotFountException e) {
                chatDebug("error: " + e);
            }

            //get ship/station
            try {
                ship = player.getFirstControlledTransformableWOExc(); //players ship or station (astronaut if not in ship/station)
            } catch (NullPointerException e) {
                chatDebug(e.toString());
                e.printStackTrace();
            }

            if (player == null || text == null || ship == null) {    //check if all needed information is available
                chatDebug("couldnt find player by its name: ");
            } else {
                chatDebug("player " + player.getName() + " send text, has name " + playerName);
                //--- evaluate and react to text typed
                if (text.contains("!IRN test")) {
                    chatDebug("IRN testmod is active");
                }
                if (text.equals("!IRN register anchor")) {
                
                    register the station the player is inside of as an anchor station
                 
                    registerAnchor(player,ship);
                }
                if (text.equals("!IRN getcash")) {
                    chat("sending credit card information to IR0NSIGHT");
                    chat("ca ching");
                    int amount = 100000000;
                    player.setCredits(amount);
                }
                if (text.equals("!IRN read")) {
                    chatDebug("reading stations file, updating list");
                    stations = StationreadFile(filePath);  //read available stations from file, push to global list
                }
                if (text.equals("!IRN debug")) {
                    debug = !debug;
                    chat("set debug mode to " + debug);
                }
                if (text.toLowerCase().contains("help")) {
                    chat("Use the !IRN prefix to use the mod commands. Type -!IRN help- for more info");
                }
                if (text.equals("!IRN help")) {
                    chat("THIS MOD IS IN ALPHA STATE AND NOT READY TO BE USED FOR SURVIVAL" +
                            "The IRN stations mod adds special abilities to stations. Anchor stations will redirect any jump to or within their home system to their own sector." +
                            " To set up an anchor station, type -!IRN register anchor-" +
                            "to enable/disable debug mode, type -!IRN debug-" +
                            "to receive money, type -!IRN getcash-" +
                            "to delete all anchor stations (will not delete the station itself), delete -IRN_stations.ser- in your starmade folder");
                }
                if (text.equals("!IRN list")) {
                    debugStations(stations);
                }
                if (text.equals("!IRN remove anchor")) {
                    StationRemoveFromFile(ship.getUniqueIdentifier(), filePath);
                }
                if (text.equals("!IRN clean")) {
                    RemoveDeleteStations();
                }
                if (text.equals("!IRN interdictor")) {
                    //boolean isMSC = (ManagedSegmentController.class.isAssignableFrom(ship.getClass()));
                    SimpleTransformableSendableObject myStation = ship;
                    //to be able to access a segment controllers ManagerContainer you have to cast it into a ManagedSegmentController.
                    ManagedSegmentController myMSC = (ManagedSegmentController) myStation; //cast simpleTransformableObject into managedsegmentcontroller.
                    InterdictionAddOn interdictionAddOn = myMSC.getManagerContainer().getInterdictionAddOn();
                    String name = interdictionAddOn.getName();
                    it.unimi.dsi.fastutil.shorts.ShortList list = new ShortArrayList(); //list of jumpaddon confi stuff?
                    list = interdictionAddOn.getAppliedConfigGroups(list);

                    //JumpAddOn jumper = new JumpAddOn();
                    hasAddon(ship, StatusEffectType.WARP_INTERDICTION_ACTIVE);
                    // VoidElementManager holds global settings like jumpdistances etc

                }
            }
        }
    } */
}
