import api.ModPlayground;
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

import java.util.List;

public class Debugger {
    private IRNstationManager stationManager;
    private AnchorManager anchorManager;
    private boolean debug = true;
    public Debugger(IRNstationManager sM, AnchorManager aM) {
        this.stationManager = sM;
        this.anchorManager = aM;
        StarLoader.registerListener(PlayerChatEvent.class, new Listener<PlayerChatEvent>() {
            @Override
            public void onEvent(PlayerChatEvent playerChatEvent) {  //playerChat for setting up stations TODO automate through custom reactor addon
                chatEvent(playerChatEvent);
            }
        });
    }

    private void chatEvent(PlayerChatEvent event) {
        if (event.isServer()) {
            //double event
        } else {    //call event handlers
            //create all relevant information
            ChatDebug("message registered");
            String text = event.getText();
            String playerName = event.getMessage().sender;
            PlayerState player = null;
            SimpleTransformableSendableObject ship = null;

            //get player
            try {
                player = GameServer.getServerState().getPlayerFromName(playerName);
            } catch (PlayerNotFountException e) {
                ChatDebug("error: " + e);
            }

            //get ship/station
            try {
                ship = player.getFirstControlledTransformableWOExc(); //players ship or station (astronaut if not in ship/station)
            } catch (NullPointerException e) {
                ChatDebug(e.toString());
                e.printStackTrace();
            }

            if (player == null || text == null || ship == null) {    //check if all needed information is available
                ChatDebug("couldnt find player by its name: ");
            } else {
                ChatDebug("player " + player.getName() + " send text, has name " + playerName);
                //--- evaluate and react to text typed
                if (text.contains("test")) {
                    ChatDebug("IRN testmod is active");
                }
                if (text.equals("cash")) {
                    ChatDebug("sending credit card information to IR0NSIGHT");
                    ChatDebug("ca ching");
                    int amount = 100000000;
                    player.setCredits(amount);
                }
                if (text.equals("!IRN read")) {
                    ChatDebug("reading stations file, updating list");
                   // stations = StationreadFile(filePath);  //read available stations from file, push to global list
                }
                if (text.equals("!IRN debug")) {
                    debug = !debug;
                    ModPlayground.broadcastMessage("set debug mode to " + debug);
                }
                if (text.toLowerCase().contains("help")) {
                    ChatDebug("Use the !IRN prefix to use the mod commands. Type -!IRN help- for more info");
                }
                if (text.equals("!IRN help")) {
                    ChatDebug("THIS MOD IS IN ALPHA STATE AND NOT READY TO BE USED FOR SURVIVAL" +
                            "The IRN stations mod adds special abilities to stations. Anchor stations will redirect any jump to or within their home system to their own sector." +
                            " To set up an anchor station, type -!IRN register anchor-" +
                            "to enable/disable debug mode, type -!IRN debug-" +
                            "to receive money, type -!IRN getcash-" +
                            "to delete all anchor stations (will not delete the station itself), delete -IRN_stations.ser- in your starmade folder");
                }
                if (text.equals("!IRN list")) {
                   DebugStations(stationManager.stations);
                }
                if (text.equals("!IRN remove anchor")) {
                   // StationRemoveFromFile(ship.getUniqueIdentifier(), filePath);
                }
                if (text.equals("!IRN clean")) {
                   // RemoveDeleteStations();
                }

            }
        }
    } 
    private void ChatDebug(String s) {
        if (debug) {
            ModPlayground.broadcastMessage(s);
        }

    }
    private void DebugStations(List<IRNstationModule> list) {
        ChatDebug("-----------Listing all stations:-----------------------");
        ChatDebug("List is null:" + (list == null));
        ChatDebug("List has " + list.size() + "entries");
        for (int i = 0; i < list.size(); i++) {
            IRNstationModule station = list.get(i);
            ChatDebug("---------------------------------------------------");
            ChatDebug("station UID: --------" + station.stationUID);
            ChatDebug("station sector: -----" + station.getStationSector());
            ChatDebug("station system: -----" + station.getStationSystem());
            ChatDebug("station type:--------" + station.type);
            String factionName = String.valueOf(station.factionID);
            try {
                if (station.factionID != 0) {
                    factionName = GameServer.getServerState().getFactionManager().getFaction(station.factionID).getName();
                }

            } catch (Exception e) {
                ChatDebug("faction name error");
                ChatDebug(e.toString());
            }
            ChatDebug("faction name:--------" + factionName);

        }
        ChatDebug("-------------------------------------------------------");
    };
}
