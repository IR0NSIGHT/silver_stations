import api.ModPlayground;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;

import java.io.IOException;
import java.io.Serializable;


public class IRNstationModule implements Serializable {
    /*
    => represents the ingame spacestation object. is not limited to segmentController or server restarts.
    A class that holds the required values for stationManager with special abilities
    contains anchor stationManager
    all instances of this class are stored in the IRNstationManager classes list
    this class gets created, handled and deleted by IRNstationManager
    is serialized and saved by IRN stationmanager
     */
    public enum StationTypes {
        ANCHOR,
        RADAR,
        MINING,
        FACTORY,
        DEFAULT
    }

    public StationTypes type;

    public int stationID;
    public String stationUIDfull;
    public String stationUID;
    public String stationName;
    public int factionID;

    private transient IRNstationManager stationManager; //link to manager class (central piece)
    private transient SegmentController stationSegmentController;
    private transient Vector3i stationSector;
    private transient Vector3i stationSystem;
    private int[] stationSectorArr;
    private int[] stationSystemArr;
    private static final long serialVersionUID = 964380174990140560L;  //cp from game error log, might need to update on version change.

    public IRNstationModule(IRNstationManager stationManagerManagerI, SimpleTransformableSendableObject stationI) { //constructor
        stationManager = stationManagerManagerI;
        stationSegmentController = (SegmentController) stationI;
        UpdateStats();

        stationSectorArr = V3itoArr(stationSector);
        stationSystemArr = V3itoArr(stationSystem);

        stationManager.stations.add(this);
    }

    public IRNstationModule(IRNstationManager stationManagerManagerI, SegmentController stationI) { //constructor
        ModPlayground.broadcastMessage("new station module created");
        stationManager = stationManagerManagerI;
        stationSegmentController = stationI;
        UpdateStats();
        stationSectorArr = V3itoArr(stationSector);
        stationSystemArr = V3itoArr(stationSystem);

        stationManager.stations.add(this);
    }

    public Vector3i getStationSector() {
        //check if sector has value
        if (stationSector == null || stationSegmentController == null) {
            //construct from arr
            stationSector = ArrtoV3i(stationSectorArr);
        }
        return stationSector;
    }

    public Vector3i getStationSystem() {
        //if system isNull cosntruct from arr
        if (stationSystem == null || stationSegmentController == null) {
            stationSystem = ArrtoV3i(stationSystemArr);
        }
        return stationSystem;
    }

    public void setStationSegmentController(SegmentController sc) {
        this.stationSegmentController = sc;
    }

    public SegmentController getstationSegmentController() {
        return this.stationSegmentController;
    }

    public void setType(StationTypes type) {
        this.type = type;
    }

    public void UpdateStats() {
        //ModPlayground.broadcastMessage("setting station stats");
        try {
            if (stationSegmentController != null) {
                stationID = stationSegmentController.getId(); //TODO why do i have that field?
                factionID = stationSegmentController.getFactionId();
                stationName = stationSegmentController.getRealName();
                stationUIDfull = stationSegmentController.getUniqueIdentifierFull();
                stationUID = stationSegmentController.getUniqueIdentifier();
                stationSector = stationSegmentController.getSector(new Vector3i());
                stationSystem = stationSegmentController.getSystem(new Vector3i());
                //ModPlayground.broadcastMessage("set stats for " + stationName);
            } else {

            }
        } catch (Exception e) {
            ModPlayground.broadcastMessage("update stats failed");
            ModPlayground.broadcastMessage(e.toString());
        }
    }

    private int[] V3itoArr(Vector3i thisV) {
        int[] arr = {thisV.x, thisV.y, thisV.z};
        return arr;
    }

    private Vector3i ArrtoV3i(int[] arr) {
        Vector3i thisV = new Vector3i(arr[0], arr[1], arr[2]);
        String s = arr.toString();
        //("converted arr" + s + " to v3i" +thisV);
        return thisV;
    }


}
