import api.ModPlayground;
import api.common.GameServer;
import api.listener.Listener;
import api.listener.events.SegmentControllerOverheatEvent;
import api.listener.events.ShipJumpEngageEvent;
import api.listener.events.player.PlayerChatEvent;
import api.listener.events.register.ElementRegisterEvent;
import api.listener.events.register.RegisterAddonsEvent;
import api.listener.events.systems.InterdictionCheckEvent;
import api.mod.StarLoader;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.shorts.ShortArrayList;
import it.unimi.dsi.fastutil.shorts.ShortList;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.data.PlayerControllable;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.elements.VoidElementManager;
import org.schema.game.common.controller.elements.jumpdrive.JumpAddOn;
import org.schema.game.common.controller.elements.jumpprohibiter.InterdictionAddOn;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.game.common.data.blockeffects.config.EffectModule;
import org.schema.game.common.data.blockeffects.config.StatusEffectType;
import org.schema.game.common.data.blockeffects.config.parameter.StatusEffectParameterType;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.game.common.data.world.StellarSystem;
import org.schema.game.common.data.world.Universe;
import org.schema.game.server.controller.SectorSwitch;
import org.schema.game.server.data.GameServerState;
import org.schema.game.server.data.PlayerNotFountException;

import java.io.*;
import java.util.*;

public class IRNstationManager implements Serializable {
    /*
    central manager that creates, stores and handles all station modules
     */
    public transient List<IRNstationModule> stations = null; //holds all station instances
    public String filePath;

    public IRNstationManager() {
        //chatDebug("station manager is running");
        stations = new ArrayList<IRNstationModule>();
        filePath = "IRN_stations.ser";
        // createFile(stations);   //create savefile
        createListeners(); //adds event listeners
    }

    private boolean debug = false;

    private void createListeners() {
        /*
            create listeners for playerchat, interdictionEvent, jumpEvent to detect the events needed
            listeneres will pass event to theri respective event methods
         */
        StarLoader.registerListener(PlayerChatEvent.class, new Listener<PlayerChatEvent>() {
            @Override
            public void onEvent(PlayerChatEvent playerChatEvent) {  //playerChat for setting up stations TODO automate through custom reactor addon
                chatEvent(playerChatEvent);
            }
        });
        StarLoader.registerListener(InterdictionCheckEvent.class, new Listener<InterdictionCheckEvent>() {
            @Override
            public void onEvent(InterdictionCheckEvent event) { //detect jumps
                interdictionEvent(event);
            }
        });
        StarLoader.registerListener(SegmentControllerOverheatEvent.class, new Listener<SegmentControllerOverheatEvent>() {
            @Override
            public void onEvent(SegmentControllerOverheatEvent segmentControllerOverheatEvent) {    //delete destroyed stations from list
                overheatEvent(segmentControllerOverheatEvent);
            }
        });
        StarLoader.registerListener(ShipJumpEngageEvent.class, new Listener<ShipJumpEngageEvent>() {
            @Override
            public void onEvent(ShipJumpEngageEvent event) {    //delete destroyed stations from list
                jumpEvent(event);
            }
        });
        /*
        StarLoader.registerListener(ElementRegisterEvent.class, new Listener<ElementRegisterEvent>() {
            @Override
            public void onEvent(ElementRegisterEvent event) {    //delete destroyed stations from list

                SegmentController ship = event.getSegmentController();
                chat("ELEMENT REGISTER EVENT FIRED");
                //get player
                ship.isConrolledByActivePlayer();
                PlayerControllable playerShip = (PlayerControllable)ship;
                if (!playerShip.getAttachedPlayers().isEmpty()) {   //ship is not empty
                    PlayerState player = playerShip.getAttachedPlayers().get(0);
                    boolean allowAnchor = false;
                    try {
                        allowAnchor = AllowAnchor(player,ship);
                    } catch (Exception e) {
                        chatDebug(e.toString());
                    }
                    if (allowAnchor) {
                        registerAnchor(player,ship);
                    }
                }


            }
        });
        */
        /*
        StarLoader.registerListener(RegisterAddonsEvent.class, new Listener<RegisterAddonsEvent>() {
            @Override
            public void onEvent(RegisterAddonsEvent event) {
                chat("REGISTER ADDONS EVENT FIRED");
                chat("entity " + event.getContainer().getSegmentController().getUniqueIdentifier() + " registered addons");

            }
        });
        */
    }

    private long lastChatTime;

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
                /*
                    register the station the player is inside of as an anchor station
                 */
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
                    hasAddon(ship,StatusEffectType.WARP_INTERDICTION_ACTIVE);
                    // VoidElementManager holds global settings like jumpdistances etc

                }
            }
        }
    }

    private void interdictionEvent(InterdictionCheckEvent event) {
        //event.getSegmentController().getSector();
    }

    private void jumpEvent(ShipJumpEngageEvent event) {
        /*
            check old and new system if they have registered anchors
            if true, abort jump.
         */
        SegmentController ship = event.getController();
        Vector3i newSector = event.getNewSector();
        Vector3i oldSector = event.getOriginalSectorPos();
        Vector3i newSystem = GetSystem(newSector).getPos();
        Vector3i oldSystem = GetSystem(oldSector).getPos();
        chatDebug("jump detected. origin system: " + oldSystem + " to system " + newSystem);
        try {
            chatDebug("trying to delay jump");
            event.wait(10000);
            event.wait();
        } catch (InterruptedException e) {
            chatDebug(e.toString());
        }
        //check if stations list is existant, if not read from file
        if (stations == null || stations.size() == 0) {
            chatDebug("station list is empty/null, reading from file");
            stations = StationreadFile(filePath);
        }
        ;
        IRNstationModule anchor;
        int oldSystemAnchored = systemAlreadyTaken(stations, oldSystem);
        int newSystemAnchored = systemAlreadyTaken(stations, newSystem);
        if (oldSystemAnchored != -1) {
            anchor = stations.get(oldSystemAnchored);
            chat("origin system is anchored: " + oldSystem);
        } else {
            if (newSystemAnchored != -1) {

                chat("new system is anchored: " + newSystem);
            }
            anchor = stations.get(newSystemAnchored);
            if (anchor == null) {
                chatDebug("anchor is null");
            }
        }

        if (oldSystemAnchored != -1 || newSystemAnchored != -1) {
            chatDebug("Anchor detected, redirecting jump");
            event.setCanceled(true);
            chatDebug("redirecting jump to anchor");
            //queue new jump to anchor station

            Vector3i jumpTo = anchor.getStationSector(); //target sector (no distance limit set)
            SegmentController sc = ship;  //ships segment controller, using one from an interdiction check to redirect ships
            sc.getNetworkObject().graphicsEffectModifier.add((byte) 1); //dont know what that does.
            SectorSwitch sectorSwitch = ((GameServerState) sc.getState()).getController().queueSectorSwitch(sc, jumpTo, 1, false, true, true);

        }
    }

    private void overheatEvent(SegmentControllerOverheatEvent event) {
        chat("overheat detected");
        String entityUID = event.getEntity().getUniqueIdentifier();
        int idx = FindStationInList(stations, entityUID); //retrieve list entry
        if (idx != -1) {
            chat("anchor station " + event.getEntity().getName() + " was destroyed by " + event.getLastDamager());
            StationRemoveFromFile(entityUID, filePath); //pass entitys uid for removal
        }

    }

    //Debug file handlers storing station and not list
    public boolean StationcreateFile(List<IRNstationModule> stationsL) {  //create a savefile for the stations if it doesnt exist yet.
        chatDebug("file creator was called");
        FileOutputStream fos = null;
        ObjectOutputStream oos = null;
        //List<IRNstationModule> stationsL = new ArrayList<IRNstationModule>();
        //stationsL.add(station);
        chatDebug("creating file");
        if (stationsL == null) {
            chatDebug("station list is null");
            return false;
        }
        //chatDebug("creating fos");
        //create file output stream
        try {
            fos = new FileOutputStream(filePath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            chatDebug(e.toString());
            return false;
        }
        //chatDebug("creating oos");
        //create Object output system
        try {
            oos = new ObjectOutputStream(fos);
        } catch (IOException e) {
            e.printStackTrace();
            chatDebug(e.toString());
            return false;
        }
        //write file
        if (fos != null && oos != null) {
            try {
                for (int i = 0; i < stationsL.size(); i++) {    //write each sation object by itself
                    oos.writeObject(stationsL.get(i));
                }
            } catch (IOException e) {
                e.printStackTrace();
                chatDebug(e.toString());
                return false;
            }
        }
        //chatDebug("closing");
        try {
            oos.close();
        } catch (IOException e) {
            e.printStackTrace();
            chatDebug(e.toString());
            return false;
        }
        stations = stationsL;   //update global list to newest file.
        debugStations(stations);
        chatDebug("file created");
        return true;
    }

    public boolean StationaddStationToFile(IRNstationModule station) {    //read file and write the added station to it

        chatDebug("station add to file was called");
        //read the file

        List<IRNstationModule> stationsL = StationreadFile(filePath);
        if (stationsL != null) {
            chatDebug("file successfully read");
        } else {
            chatDebug("error: station list read from file is null");
        }
        ;
        //chatDebug("adding station to local list");
        debugStations(stationsL);
        //check if station is in list
        if (FindStationInList(stationsL, station) != -1) {
            chatDebug("station is already registered in file!");
            return true;
        } else {
            //add station to list
            stationsL.add(station);
        }
        ;

        chatDebug("rewriting file");
        //rewrite file
        if (StationcreateFile(stationsL)) {
            chatDebug("file successfully written");
            return true;
        }
        chatDebug("station not successfully added to file");
        return false;
    }

    public List<IRNstationModule> StationreadFile(String filePath) {
        List<IRNstationModule> stationsL1 = new ArrayList<IRNstationModule>();
        //check if file exists, else create it
        chat("trying to read station file");
        File f = new File(filePath);
        if (f.exists()) {
            chatDebug("file exists");
        } else {
            chatDebug("file does not exist, creating file");
            StationcreateFile(stationsL1);   //create savefile
        }

        chatDebug("accessing data");
        //chatDebug("create vars");

        //read list from file
        FileInputStream fis = null;
        ObjectInputStream ois = null;
        //chatDebug("create fis");
        //create file input stream
        try {
            fis = new FileInputStream(filePath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            chatDebug(e.toString());
            return null;
        }
        //chatDebug("ois");
        //create object input stream
        try {
            ois = new ObjectInputStream(fis);
        } catch (IOException e) {
            e.printStackTrace();
            chatDebug(e.toString());
            return null;
        }
        //loop over input stream reading one station module at a time and add it to list, until eof Exc. is caught -> stream empty
        for (int i = 0; i < 10; i++) {
            //chatDebug(" iterator i = " + i);
            try {
                //ois.available()
                IRNstationModule station = (IRNstationModule) ois.readObject();
                stationsL1.add(station);

            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                chatDebug(e.toString());
                return null;
            } catch (InvalidClassException e) {
                e.printStackTrace();
                chatDebug(e.toString());
                return null;
            } catch (StreamCorruptedException e) {
                e.printStackTrace();
                chatDebug(e.toString());
                return null;
            } catch (OptionalDataException e) {
                e.printStackTrace();
                chatDebug(e.toString());
                return null;
            } catch (EOFException e) {
                e.printStackTrace();
                chatDebug(e.toString());
                chatDebug("no more object in stream");
                break;  //break out of loop
            } catch (IOException e) {
                e.printStackTrace();
                chatDebug(e.toString());
                return null;
            }
        }
        debugStations(stationsL1);
        //chatDebug("closing reader");
        try {
            ois.close();
            chatDebug("read file complete");
            debugStations(stations);
            return stationsL1;
        } catch (IOException e) {
            e.printStackTrace();
            chatDebug(e.toString());
            return null;
        }

    }

    public boolean StationRemoveFromFile(String stationUID, String filePath) {
        List<IRNstationModule> list = StationreadFile(filePath);
        if (list == null || list.size() == 0) {
            chatDebug("list read from file is null or size zero");
            return false;
        } else {
            chatDebug("got station list from file");
            int idx = FindStationInList(list, stationUID);
            if (idx == -1) {
                chatDebug("station is not in list");
                return false;
            } else {
                chatDebug("station is at ind " + idx + " of stations list");
                IRNstationModule removed = list.remove(idx);
                chatDebug("removed station " + removed.stationName + " (ID " + removed.stationUID + ") sector " + removed.getStationSector() + " from list.");

                boolean wroteFile = StationcreateFile(list);
                if (wroteFile) {
                    chatDebug("rewrote file with new list");
                    StationreadFile(filePath);  //re read to update global list
                    debugStations(list);
                    return true;
                } else {
                    chatDebug("fatal error trying to write file with new list");
                    return false;
                }
            }
        }
    }

    //station types
    private boolean registerAnchor(PlayerState player, SimpleTransformableSendableObject station) {
        boolean allowAnchor = AllowAnchor(player, station);
        if (allowAnchor) { //all conditions are met, register new anchor
            IRNstationModule newStation = new IRNstationModule(this, station); //create a new station module to hold vital info
            return true;
        }
        return false;
    }


    //methods used just for structuring
    private StellarSystem GetSystem(Vector3i sector) {
        //get universe
        Universe universe = GameServer.getUniverse();
        //get system from sector pos
        // ! ATTENTION system is its own data type and not the Vector3i you get returned from f.e. segmentcontroller.getSystem
        StellarSystem system = null;
        try {
            system = universe.getStellarSystemFromSecPos(sector);
        } catch (IOException e) {
            chatDebug("tried getting system from sector " + sector + " failed with " + e);
        }

        return system;
    }

    private List<IRNstationModule> RemoveDeleteStations() {
        List<IRNstationModule> deadStations = new ArrayList<>();
        List<IRNstationModule> list = StationreadFile(filePath);
        for (int i = 0; i < list.size(); i++) {
            IRNstationModule station = list.get(i);
            //
            boolean isExist = GameServer.getServerState().existsEntity(SimpleTransformableSendableObject.EntityType.SPACE_STATION, station.stationUID);
            if (!isExist) {
                chatDebug(" found " + station.stationUID + " to be not existant on gameserver. marking for delete from savefile");
                deadStations.add(station);  //mark for delete
                list.remove(i);
            }
        }
        chatDebug("found " + deadStations.size() + " dead stations in list");
        chatDebug("rewriting cleaned list to file");
        StationcreateFile(list);
        return deadStations;
    }

    private int FindStationInList(List<IRNstationModule> list, IRNstationModule element) {
        /*
            compare stations ID to all station IDs in list, return index of first found element, else return -1 if not found
         */
        for (int i = 0; i < list.size(); i++) {
            IRNstationModule x = list.get(i);
            if (x.stationUID == element.stationUID) {
                chatDebug("station already present by UID in list");
                chatDebug(x.stationUID + " in sector " + x.getStationSector() + " system" + x.getStationSystem());
                return i;
            }
        }
        ;
        chatDebug("ID not found in list");
        return -1;
    }

    private int FindStationInList(List<IRNstationModule> list, String stationUID) {
        /*
            compare stations ID to all station IDs in list, return index of first found element, else return -1 if not found
         */
        chatDebug("try to find stationUID " + stationUID + " in list");
        for (int i = 0; i < list.size(); i++) {
            IRNstationModule x = list.get(i);
            chatDebug("x.UID " + x.stationUID + " vs UID" + stationUID);
            if (x.stationUID.equals(stationUID)) {
                chatDebug("station already present by UID in list");
                chatDebug(x.stationUID + " in sector " + x.getStationSector() + " system" + x.getStationSystem());
                return i;
            }
        }
        ;
        chatDebug("ID not found in list");
        return -1;
    }

    private int systemAlreadyTaken(List<IRNstationModule> list, Vector3i system) {
        /*
            will search list for system, will return index of first match, returns -1 if nothing found
         */
        debugStations(list);
        //cycle through list and compare each registered anchor stations system to input system
        for (int i = 0; i < list.size(); i++) {
            Vector3i xSys = list.get(i).getStationSystem();
            chatDebug(" comparing list system " + xSys + " to system " + system);
            if ((xSys.x == system.x) && (xSys.y == system.y) && (system.z == xSys.z)) {
                //both systems have the same Vector -> its the same system
                chatDebug("same system");
                return i;    //break from method
            }
        }
        chatDebug("new system");
        return -1;
    }

    private boolean AllowAnchor(PlayerState player, SimpleTransformableSendableObject station) {
        String stationUID = station.getUniqueIdentifier();
        Vector3i stationSector = station.getSector(new Vector3i());
        Vector3i stationSystem = station.getSystem(new Vector3i());
        boolean isStation = true;
        boolean systemIsFree = true;
        boolean stationNotRegistered = true;
        boolean ownsStation = true;
        boolean ownsSystem = true;
        boolean stationHasInhibitor = true; //TODO check if station has inhibitor chambers
        boolean isNotHomebase = true; //TODO add check for homebase
        String failMessage = "";
        //TODO boolean for debug messages
        //TODO feedback messages directly to player, not on serverchat.
        if (station.getType() != SimpleTransformableSendableObject.EntityType.SPACE_STATION) {
            isStation = false;
           failMessage += ("only stations can be registered. "); //TODO add into central feedback system instead of breaking out here.

        }
        if (isStation) {
            if (systemAlreadyTaken(stations, stationSystem) != -1) {
                failMessage += ("system already has an anchor station. ");
                systemIsFree = true;
            }
            if (FindStationInList(stations, stationUID) != -1) {
                failMessage += ("station is already registered. ");
                stationNotRegistered = true;
            }
            if (GetSystem(station.getSector(new Vector3i())).getOwnerFaction() != station.getFactionId()) {
            /*
                compares system ownership to ships faction
             */
                ownsSystem = false;
                failMessage += "your faction does not own this system. ";
            }
            if (player.getFactionId() != station.getFactionId()) {
            /*
                checks for player faction and station faction
             */
                ownsStation = false;
                failMessage += "your faction does not own this station.";
            }
            if (!hasAddon(station,StatusEffectType.WARP_INTERDICTION_ACTIVE)) {
                /*
                    checks if entity has a inhibitor chamber selected. (does not need to be running)
                 */
                stationHasInhibitor = false;
                failMessage += "station does not have a inhibitor installed. ";
            }

        }
        if (isStation && systemIsFree && stationNotRegistered && ownsStation && ownsSystem && stationHasInhibitor && isNotHomebase) {
            return true;
        }
        chat(failMessage);
        return false;
    }

    private void chat(String s) {
        ModPlayground.broadcastMessage(s);
    }

    private void chatDebug(String s) {
        if (debug) {
            ModPlayground.broadcastMessage(s);
        }
    }

    private void debugStations(List<IRNstationModule> stations) {
        chatDebug("old list is size " + stations.size());
        for (int i = 0; i < stations.size(); i++) {
            IRNstationModule thisStat = stations.get(i);
            chatDebug("station i: ID " + thisStat.stationID + " in sector " + thisStat.getStationSector() + " in system " + thisStat.getStationSystem());
            chatDebug("has UIDfull " + thisStat.stationUIDfull + "& UID " + thisStat.stationUID);
        }
        ;
    }
    private boolean hasAddon (SimpleTransformableSendableObject ship, StatusEffectType addon) {
        chatDebug("listing all modules of interdiction addon for entity " + ship.getUniqueIdentifier());
        chatDebug("searching for " + addon);
        ManagedSegmentController myMSC = (ManagedSegmentController) ship;
        chatDebug("MSC is " + myMSC.toString());
        //GameServer.getServerState().
        InterdictionAddOn interdictionAddOn = myMSC.getManagerContainer().getInterdictionAddOn();
        chatDebug("interdiction addon is " + interdictionAddOn.getName());
        List<EffectModule> moduleList = interdictionAddOn.getConfigManager().getModulesList();
        chatDebug("modules list has size: " + moduleList.size());
        String val = "";
        for (int i = 0; i < moduleList.size(); i++) {
            EffectModule module = moduleList.get(i);

            try {
                val = module.getValueString();

            } catch (Exception e) {
                val = "unreadable";
                chatDebug(e.toString());
            }
            chatDebug("" + module.getType() + ": " + val);
            if (module.getType() == addon) {

                return  true;
            }
        }
        chatDebug("done with list. no inhibitor found.");
        return false;
    }
}
