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
    private transient IRNstationManager stationManager; //link to manager class (central piece)
    private transient SegmentController stationSegmentController;
    public int stationID; public String stationUIDfull; public String stationUID;
    public String stationName;
    public int factionID;
    private transient Vector3i stationSector;
    public Vector3i getStationSector() {
        //check if sector has value
        if (stationSector == null) {
            //construct from arr
            stationSector = ArrtoV3i(stationSectorArr);
        }
        return stationSector;
    }
    private transient Vector3i stationSystem;
    public  Vector3i getStationSystem() {
        //if system isNull cosntruct from arr
        if (stationSystem == null) {
            stationSystem = ArrtoV3i(stationSystemArr);
        }
        return stationSystem;
    }
    private int[] stationSectorArr;
    private int[] stationSystemArr;
    private static final long serialVersionUID = 964380174990140560L;  //cp from game error log, might need to update on version change.
    private int[] V3itoArr(Vector3i thisV) {
        int[] arr = {thisV.x, thisV.y, thisV.z};
        return arr;
    }
    private Vector3i ArrtoV3i (int[] arr) {
        Vector3i thisV = new Vector3i(arr[0],arr[1],arr[2]);
        String s = arr.toString();
        //("converted arr" + s + " to v3i" +thisV);
        return thisV;
    }
    public IRNstationModule(IRNstationManager stationManagerManagerI, SimpleTransformableSendableObject stationI) { //constructor
        stationManager = stationManagerManagerI;
        stationID = stationI.getId(); //TODO why do i have that field?
        factionID = stationI.getFactionId();
        stationName = stationI.getRealName();
        stationUIDfull = stationI.getUniqueIdentifierFull();
        stationUID = stationI.getUniqueIdentifier();
        stationSector = stationI.getSector(new Vector3i());
        stationSystem = stationI.getSystem(new Vector3i());
        stationSegmentController = (SegmentController) stationI;

        stationSectorArr = V3itoArr(stationSector);
        stationSystemArr = V3itoArr(stationSystem);

        stationManager.stations.add(this);
    }
    public IRNstationModule(IRNstationManager stationManagerManagerI, SegmentController stationI) { //constructor
        stationManager = stationManagerManagerI;
        stationID = stationI.getId(); //TODO why do i have that field?
        factionID = stationI.getFactionId();
        stationName = stationI.getRealName();
        stationUIDfull = stationI.getUniqueIdentifierFull();
        stationUID = stationI.getUniqueIdentifier();
        stationSector = stationI.getSector(new Vector3i());
        stationSystem = stationI.getSystem(new Vector3i());
        stationSegmentController = stationI;

        stationSectorArr = V3itoArr(stationSector);
        stationSystemArr = V3itoArr(stationSystem);

        stationManager.stations.add(this);
    }

    public void setStationSegmentController(SegmentController sc) {
        this.stationSegmentController = sc;
    }
    public SegmentController getstationSegmentController() {
        return this.stationSegmentController;
    }
}
