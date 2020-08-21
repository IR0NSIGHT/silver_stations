import api.common.GameServer;
import api.listener.Listener;
import api.listener.events.ShipJumpEngageEvent;
import api.mod.StarLoader;
import api.utils.StarRunnable;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.elements.jumpprohibiter.InterdictionAddOn;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.game.common.data.blockeffects.config.EffectModule;
import org.schema.game.common.data.blockeffects.config.StatusEffectType;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.game.server.controller.SectorSwitch;
import org.schema.game.server.data.GameServerState;

import java.util.ArrayList;
import java.util.List;

public class AnchorManager {
    private final IRNstationManager manager;
    private List<IRNstationModule> anchors;

    public AnchorManager(IRNstationManager manager) {
        this.manager = manager;
        createJumpListener();
        UpdateLoop();
    }

    //create update loop
    private void UpdateLoop() {
        /**
         * checks stations from stationmanager for anchor properties, updates anchormanagers list of anchors
         * will add new stations that meet criterio
         * will remove old stations that have lost criteria
         */
        final AnchorManager anchorManager = this;
        final IRNstationManager stationManager = manager;
        anchors = new ArrayList<>();
        new StarRunnable() {

            @Override
            public void run() {
                try {
                    manager.chatDebug ("anchor loop at --------" + System.currentTimeMillis()/1000 + "-------------------");
                    for (int i = 0; i < stationManager.stations.size(); i++) {
                        //check if has segment controller
                        IRNstationModule station = stationManager.stations.get(i);
                        //check is station is still existing on server

                        boolean isExist = GameServer.getServerState().existsEntity(SimpleTransformableSendableObject.EntityType.SPACE_STATION,station.stationName);
                        if (!isExist) {
                            //TODO isExists gives false negatives.
                            manager.chatDebug("station " + station.stationName + "does not exist on server. purge from lists.");
                            //manager.chatDebug("station is entity type " + station.getstationSegmentController().getType());
                            //remove dead stations from anchors and stations list. -> garbage collector will kill instances
                           int idx = manager.FindStationInList(anchors,station.stationUID);
                           if (idx != -1) {
                               //remove dead station from list
                               anchors.remove(idx);
                           }
                            idx = manager.FindStationInList(manager.stations,station.stationUID);
                            if (idx != -1) {
                                //remove dead station from list
                                manager.stations.remove(idx);
                            }
                            continue;
                        }

                        //manager.chatDebug("station is: " + station.stationName);
                        if (station.getstationSegmentController() != null) {
                            //manager.chatDebug("anchor manager checking conditions");
                            //manager.chatDebug("station has segment controller");
                            station.UpdateStats();
                            //check for anchor properties
                            boolean allowAnchor = AllowAnchor(station);
                            if (allowAnchor) {
                                station.setType(IRNstationModule.StationTypes.ANCHOR);
                            } else {
                                if (station.type == IRNstationModule.StationTypes.ANCHOR) {
                                    station.setType(IRNstationModule.StationTypes.DEFAULT);
                                }
                            }
                            //manager.chatDebug("station allowed anchor: " + allowAnchor);
                            //manager.chatDebug("checking anchors list for station");
                            //check if in anchors
                            int idx = manager.FindStationInList(anchors, station.stationUID);
                            //manager.chatDebug("station at idx in anchors: " + idx);
                            if (allowAnchor && idx == -1) {
                                /** station has anchor capability and is not in anchors list
                                 *
                                 */
                                manager.chatDebug("adding station to anchors: " + station.stationName);
                                anchors.add(station);

                            }
                            if (!allowAnchor && idx != -1) {
                                /**
                                 * station is in list but has NO anchor capability
                                 */
                                manager.chatDebug("removing station from anchors: " + station.stationName);
                                anchors.remove(idx);

                            }
                        } else {
                            //no segment controller -> not loaded
                            int idx = manager.FindStationInList(anchors, station.stationUID);
                            if (station.type == IRNstationModule.StationTypes.ANCHOR) {
                                //manager.chatDebug("station at idx in anchors: " + idx);
                                if (idx == -1) {
                                    /** station has anchor capability and is not in anchors list
                                     *
                                     */
                                    manager.chatDebug("adding unloaded station to anchors: " + station.stationName);
                                    anchors.add(station);
                                }
                            } else {
                                if (idx != -1) {
                                    manager.chatDebug("removing unloaded station from anchors");
                                    anchors.remove(idx);
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    manager.chatDebug("anchor manager update loop failed");
                    manager.chatDebug(e.toString());
                }

                //get all stations with a segment controller


            }

            private boolean AllowAnchor (IRNstationModule stationModule){
                SegmentController station = stationModule.getstationSegmentController();
                //manager.chatDebug("AllowAnchor running");
                String stationUID = station.getUniqueIdentifier();
                Vector3i stationSystem = station.getSystem(new Vector3i());
                boolean systemIsFree = true;
                boolean ownsSystem = true;
                boolean stationHasInhibitor = true; //TODO check if station has inhibitor chambers
                boolean isNotHomebase = true; //TODO add check for homebase
                String failMessage = "";
                //TODO feedback messages directly to player, not on serverchat.
                //manager.chatDebug("AA: checking for other anchors in system");
                if (StationInSystem(anchors, stationSystem, stationUID) != -1) {
                    //TODO causes loop to fail, run without timer
                    failMessage += ("system already has an anchor station. ");
                    systemIsFree = false;
                }
                //manager.chatDebug("AA: checking for ownership of system");
                try {
                    Vector3i sector = stationModule.getStationSector();
                    if (manager.GetSystem(sector).getOwnerFaction() != station.getFactionId() || station.getFactionId() == 0 || manager.GetSystem(sector).getOwnerFaction() == 0) {
                        /**
                         *  compares system ownership to stations faction
                         *  factioID = 0 checks for unclaimed stuff -> otherwise if you shoot out a stations module in an unclaimed system, it gets anchored.
                         */
                        ownsSystem = false;
                        failMessage += "your faction does not own this system. ";
                    }
                } catch (Exception e) {
                    manager.chatDebug("failed to check system ownership");
                    manager.chatDebug(e.toString());
                }

                //manager.chatDebug("AA: checking for installed interdictor");
                if (!hasAddon(station, StatusEffectType.WARP_INTERDICTION_ACTIVE)) {

                    //checks if entity has a inhibitor chamber selected.(does not need to be running)

                    stationHasInhibitor = false;
                    failMessage += "station does not have a inhibitor installed. ";
                }
                //TODO add homebase check
                if (systemIsFree && ownsSystem && stationHasInhibitor && isNotHomebase) {
                    return true;
                }
                //manager.chatDebug(failMessage);
                return false;
            }
            private boolean hasAddon (SegmentController station, StatusEffectType addon){
                ManagedSegmentController myMSC = (ManagedSegmentController) station;
                InterdictionAddOn interdictionAddOn = myMSC.getManagerContainer().getInterdictionAddOn();
                List<EffectModule> moduleList = interdictionAddOn.getConfigManager().getModulesList();
                String val = "";
                for (int i = 0; i < moduleList.size(); i++) {
                    EffectModule module = moduleList.get(i);
                    try {
                        val = module.getValueString();
                    } catch (Exception e) {
                        val = "unreadable";
                    }
                    if (module.getType() == addon) {
                        return true;
                    }
                }
                return false;
            }
            private int StationInSystem(List < IRNstationModule > list, Vector3i system, String stationUID){
                /**
                 will search list for system, will return index of first match, returns -1 if nothing found
                 */
                //manager.chatDebug("StationInSystem running");
                //cycle through list and compare each registered anchor stations system to input system
                Vector3i xSys;
                for (int i = 0; i < list.size(); i++) {
                    try {
                        String xStationUID = list.get(i).stationUID;
                        if (xStationUID.equals(stationUID)) {
                            //skip this station so it doesnt block itself
                          continue;
                        }
                        try {
                            xSys = list.get(i).getStationSystem();
                            //manager.chatDebug("getting system for station " + stationUID);
                            //manager.chatDebug("passed system: " + system.toString());
                            //manager.chatDebug("xStation " + list.get(i).stationName + " sys" + xSys.toString());
                            // chatDebug(" comparing list system " + xSys + " to system " + system);
                            if ((xSys.x == system.x) && (xSys.y == system.y) && (system.z == xSys.z)) {
                                //both systems have the same Vector -> its the same system
                                return i;    //break from method
                            }
                        } catch (Exception e) {
                            manager.chatDebug("station has system field = null");
                            manager.chatDebug(e.toString());
                        }



                    } catch (Exception e) {
                        manager.chatDebug("get system failed");
                        manager.chatDebug(e.toString());
                    }
                }
                return -1;
            }
        }.runTimer(25 * 5);
    }
    private void createJumpListener () {
        StarLoader.registerListener(ShipJumpEngageEvent.class, new Listener<ShipJumpEngageEvent>() {
            @Override
            public void onEvent(ShipJumpEngageEvent shipJumpEngageEvent) {
                jumpEvent(shipJumpEngageEvent);
            }
        });
    }
    private void jumpEvent (ShipJumpEngageEvent event){
        /**
         * check old and new system if they have registered anchors
         *  if true, abort jump and queue a new jump to the anchors sector
         */
        SegmentController ship = event.getController();
        Vector3i newSector = event.getNewSector();
        Vector3i oldSector = event.getOriginalSectorPos();
        Vector3i newSystem = manager.GetSystem(newSector).getPos();
        Vector3i oldSystem = manager.GetSystem(oldSector).getPos();


        IRNstationModule anchor;
        int oldSystemAnchored = manager.StationInSystem(anchors, oldSystem);
        int newSystemAnchored = manager.StationInSystem(anchors, newSystem);

        if (oldSystemAnchored != -1 || newSystemAnchored != -1) {
            /**stop original jump
             * add jump to anchor stations homesector
             * check origin system first for anchor -> if origin and target systems are anchored, origin will win.
             */
            if (oldSystemAnchored != -1) {
                anchor = anchors.get(oldSystemAnchored);
            } else {
                anchor = anchors.get(newSystemAnchored);
            }

            event.setCanceled(true);
            //queue new jump to anchor station

            Vector3i jumpTo = anchor.getStationSector(); //target sector (no distance limit set)

            ship.getNetworkObject().graphicsEffectModifier.add((byte) 1); //TODO get graphic effect to work
            SectorSwitch sectorSwitch = ((GameServerState) ship.getState()).getController().queueSectorSwitch(ship, jumpTo, 1, false, true, true);
        }
    }
}