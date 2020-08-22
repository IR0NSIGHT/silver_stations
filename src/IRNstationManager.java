import Events.EntityLoadedEvent;
import api.DebugFile;
import api.ModPlayground;
import api.common.GameServer;
import api.listener.Listener;
import api.listener.events.SegmentControllerOverheatEvent;
import api.listener.events.ShipJumpEngageEvent;
import api.listener.events.systems.InterdictionCheckEvent;
import api.mod.StarLoader;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.game.common.data.world.StellarSystem;
import org.schema.game.common.data.world.Universe;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class IRNstationManager implements Serializable {
    /*
        central manager that creates, stores and handles all station modules
     */
    public transient List<IRNstationModule> stations = null; //holds all station instances
    public transient AnchorManager anchorManager;
    public String filePath;
    public IRNCore modInstance;
    public IRNstationManager(IRNCore modInstance) {
        this.modInstance = modInstance;
        anchorManager = new AnchorManager(this);
        stationFileHandler fileHandler = new stationFileHandler(this);
        filePath = fileHandler.filePath;;
        stations = new ArrayList<>();
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
                    chatDebug("station was loaded: " + event.getSegmentController().getUniqueIdentifier());
                    //check for existing stationfile
                    try {
                        String stationUID = event.getSegmentController().getUniqueIdentifier();
                        chatDebug("station UID is " + stationUID);
                        int idx = FindStationInList(stations, stationUID);
                        chatDebug("station find in list idx is " + idx);

                        if (idx != -1) { //already registered
                            //add segment controller to station object
                            stations.get(idx).setStationSegmentController(event.getSegmentController());
                            chatDebug("set stations segment controller");
                        } else { //not yet registered
                            //create station object
                            //chatDebug("segmentcontroller is " + event.getSegmentController());
                            chatDebug("station SC == null: " + (event.getSegmentController() == null));
                            registerIRNstation(event.getSegmentController());
                            chatDebug("registered new station");
                        }
                    } catch (Exception e) {
                        chatDebug("entity loaded event listener failed");
                        chatDebug(e.toString());
                    }
                    chatDebug("entity loaded event done -----------");
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




    public List<IRNstationModule> ReadListFromFile(String filePath) {
        /**
         * attempt to read the savefile containing the station modules.
         * if sth goes wrong, return the empty stationsL1 list.
         */
        List<IRNstationModule> stationsL1 = new ArrayList<IRNstationModule>();
        //check if file exists, else create it
        chat("trying to read station file");
        File f = new File(filePath);
        if (f.exists()) {
            chatDebug("file exists");

        } else {
            chatDebug("file does not exist, creating file");
             return stationsL1;
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
             return stationsL1;
        }
        //chatDebug("ois");
        //create object input stream
        try {
            ois = new ObjectInputStream(fis);
        } catch (IOException e) {
            e.printStackTrace();
            chatDebug(e.toString());
             return stationsL1;
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
                return stationsL1;
            } catch (InvalidClassException e) {
                e.printStackTrace();
                chatDebug(e.toString());
                 return stationsL1;
            } catch (StreamCorruptedException e) {
                e.printStackTrace();
                chatDebug(e.toString());
                 return stationsL1;
            } catch (OptionalDataException e) {
                e.printStackTrace();
                chatDebug(e.toString());
                 return stationsL1;
            } catch (EOFException e) {
                e.printStackTrace();
                chatDebug(e.toString());
                chatDebug("no more object in stream");
                break;  //break out of loop
            } catch (IOException e) {
                e.printStackTrace();
                chatDebug(e.toString());
                 return stationsL1;
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
             return stationsL1;
        }

    }

    private void registerIRNstation(SegmentController station) {
        chatDebug("registerIRNstation called");
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

    public void chatDebug(String s) {
        if (true) {

            SimpleDateFormat formatter = new SimpleDateFormat ("dd-MM-yyyy 'at' HH:mm:ss z");
            Date date = new Date(System.currentTimeMillis());
            String timeStamp = formatter.format(date);
            DebugFile.log((timeStamp + " -- sysMan -- " + s), modInstance);
            //ModPlayground.broadcastMessage("SM" + s);
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
