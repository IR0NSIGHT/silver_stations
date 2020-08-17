import api.ModPlayground;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;

import java.io.IOException;
import java.io.Serializable;


public class IRNstationModule implements Serializable {
    /*
    => represents the ingame spacestation object. is not limited to segmentController or server restarts.
    A class that holds the required values for stationManager with special abilities
    contains anchor stationManager
    all instances of this class are stored in the IRNstationManagerManager classes list
    this class gets created, handled and deleted by IRNstationManagerManager
     */
    private transient IRNstationManager stationManager = null; //link to manager class (central piece)
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
    public int[] stationSectorArr;
    public int[] stationSystemArr;
    private static final long serialVersionUID = 964380174990140560L;  //cp from stackoverflow after lcoal calss imcompaltible error
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
        stationID = stationI.getId();
        factionID = stationI.getFactionId();
        stationName = stationI.getRealName();
        stationUIDfull = stationI.getUniqueIdentifierFull();
        stationUID = stationI.getUniqueIdentifier();
        stationSector = stationI.getSector(new Vector3i());
        stationSystem = stationI.getSystem(new Vector3i());

        stationSectorArr = V3itoArr(stationSector);
        stationSystemArr = V3itoArr(stationSystem);

        stationManager.StationaddStationToFile(this);

        //this.addChangeListener(new AnchorListener());
    }
    public void readObject(java.io.ObjectInputStream stream) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        stationManager = IRNCore.stations;
        //ModPlayground.broadcastMessage("read method accessed");
        stationSystem = ArrtoV3i(stationSectorArr);
        stationSector = ArrtoV3i(stationSectorArr);
        //ModPlayground.broadcastMessage("read sector arr " + stationSectorArr);
        //ModPlayground.broadcastMessage("read system arr " + stationSystemArr);
        //ModPlayground.broadcastMessage("read sector " + stationSector);
        //ModPlayground.broadcastMessage("read system " + stationSystem);
    }
    public void writeObject(java.io.ObjectOutputStream stream) throws IOException {
        stream.defaultWriteObject();
    }

}
