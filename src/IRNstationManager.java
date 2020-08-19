import Events.EntityLoadedEvent;
import api.ModPlayground;
import api.common.GameServer;
import api.listener.Listener;
import api.listener.events.SegmentControllerOverheatEvent;
import api.listener.events.ShipJumpEngageEvent;
import api.listener.events.systems.InterdictionCheckEvent;
import api.mod.StarLoader;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.elements.jumpprohibiter.InterdictionAddOn;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.game.common.data.blockeffects.config.EffectModule;
import org.schema.game.common.data.blockeffects.config.StatusEffectType;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.game.common.data.world.StellarSystem;
import org.schema.game.common.data.world.Universe;
import org.schema.game.server.controller.SectorSwitch;
import org.schema.game.server.data.GameServerState;

import java.io.*;
import java.util.*;

public class IRNstationManager implements Serializable {
    /*
        central manager that creates, stores and handles all station modules
     */
    public transient List<IRNstationModule> stations = null; //holds all station instances
    public transient AnchorManager anchorManager;
    public String filePath;

    public IRNstationManager() {
        anchorManager = new AnchorManager(this);
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
        StarLoader.registerListener(EntityLoadedEvent.class, new Listener<EntityLoadedEvent>() {
            @Override
            public void onEvent(EntityLoadedEvent event) {
                if (event.getSegmentController().getType() == SimpleTransformableSendableObject.EntityType.SPACE_STATION) {
                    ModPlayground.broadcastMessage("station was loaded");
                    //check for existing stationfile
                    try {
                        String stationUID = event.getSegmentController().getUniqueIdentifier();
                        int idx = FindStationInList(stations, stationUID);
                        //ModPlayground.broadcastMessage("idx of station is " + idx);
                        if (idx != -1) { //already registered
                            //add segment controller to station object
                            stations.get(idx).setStationSegmentController(event.getSegmentController());
                        } else { //not yet registered
                            //create station object
                            //ModPlayground.broadcastMessage("segmentcontroller is " + event.getSegmentController());

                            registerIRNstation(event.getSegmentController());
                        }
                    } catch (Exception e) {
                        ModPlayground.broadcastMessage("entity loaded event listener failed");
                        ModPlayground.broadcastMessage(e.toString());
                    }

                }
            }
        });
        StarLoader.registerListener(InterdictionCheckEvent.class, new Listener<InterdictionCheckEvent>() {
            @Override
            public void onEvent(InterdictionCheckEvent event) { //detect jumps
                //interdictionEvent(event);
            }
        });
        StarLoader.registerListener(SegmentControllerOverheatEvent.class, new Listener<SegmentControllerOverheatEvent>() {
            @Override
            public void onEvent(SegmentControllerOverheatEvent segmentControllerOverheatEvent) {    //delete destroyed stations from list
                //overheatEvent(segmentControllerOverheatEvent);
            }
        });
        StarLoader.registerListener(ShipJumpEngageEvent.class, new Listener<ShipJumpEngageEvent>() {
            @Override
            public void onEvent(ShipJumpEngageEvent event) {    //delete destroyed stations from list
              //  jumpEvent(event);
            }
        });
    }


    private void interdictionEvent(InterdictionCheckEvent event) {
        //event.getSegmentController().getSector();
    }


    private void overheatEvent(SegmentControllerOverheatEvent event) {
        chat("overheat detected");
        String entityUID = event.getEntity().getUniqueIdentifier();
        int idx = FindStationInList(stations, entityUID); //retrieve list entry
        if (idx != -1) {
            chat("anchor station " + event.getEntity().getName() + " was destroyed by " + event.getLastDamager());
          //  StationRemoveFromFile(entityUID, filePath); //pass entitys uid for removal
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

    private void registerIRNstation(SegmentController station) {
        IRNstationModule newStation = new IRNstationModule(this, station);
    }

    //methods used just for structuring
    public StellarSystem GetSystem(Vector3i sector) {
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

    public int FindStationInList(List<IRNstationModule> list, IRNstationModule element) {
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

    public int FindStationInList(List<IRNstationModule> list, String stationUID) {
        /*
            compare stations ID to all station IDs in list, return index of first found element, else return -1 if not found
         */
        //chatDebug("try to find stationUID " + stationUID + " in list");
        for (int i = 0; i < list.size(); i++) {
            IRNstationModule x = list.get(i);
            //chatDebug("x.UID " + x.stationUID + " vs UID" + stationUID);
            if (x.stationUID.equals(stationUID)) {
               // chatDebug("station already present by UID in list");
                chatDebug("ID found in list");
                //chatDebug(x.stationUID + " in sector " + x.getStationSector() + " system" + x.getStationSystem());
                return i;
            }
        }
        chatDebug("ID not found in list");
        return -1;
    }

    private List<IRNstationModule> GetFactionsStations(int factionID, List<IRNstationModule> allList) {
        List<IRNstationModule> list = new ArrayList<>();
        for (int i = 0; i < allList.size(); i++) {
            if (allList.get(i).factionID == factionID) {
                list.add(allList.get(i));
            }
        }
        return list;
    }

    public int StationInSystem(List<IRNstationModule> list, Vector3i system) {
        /*
            will search list for system, will return index of first match, returns -1 if nothing found
         */
       // debugStations(list);
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

    private void chat(String s) {
        ModPlayground.broadcastMessage(s);
    }

    private void chatDebug(String s) {
        if (false) {
            ModPlayground.broadcastMessage("SM" + s);
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

}
