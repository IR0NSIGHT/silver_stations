package Events;

import api.ModPlayground;
import api.common.GameServer;
import api.entity.StarEntity;
import api.mod.StarLoader;
import api.server.Server;
import api.utils.StarRunnable;
import org.schema.game.common.controller.SegmentController;
import org.schema.schine.network.server.ServerState;

import java.util.ArrayList;

public class EntityLoadEventLoop{
    /*
       create a loop checking for newly loaded entities
       fires Events.EntityLoadedEvent
       TODO will fire Events.EntityUnloadedEvent
     */
    private ArrayList<StarEntity> oldLoadedEnts; //log all loaded entities.

    public EntityLoadEventLoop(){
        SegmentControllerCheckLoop();
    }

    private void SegmentControllerCheckLoop() {
        new StarRunnable() {
            private ArrayList<StarEntity> oldLoadedEnts;
            @Override
            public void run() {
                if (ServerState.isShutdown() || ServerState.isFlagShutdown()) {
                    this.cancel();
                }
                if (GameServer.getServerState() != null) { //only start running when server is loaded.
                    ArrayList<StarEntity> newEnts = new ArrayList<>();
                    ArrayList<StarEntity> unloadedEnts = new ArrayList<>();
                    if (oldLoadedEnts == null || oldLoadedEnts.size() == 0) { //first time init, all entites are new.
                        oldLoadedEnts = getoldLoadedEnts();

                    }

                    ArrayList<StarEntity> newLoadedEnts = getoldLoadedEnts();
                    //check currenty loaded entities against old newLoadedEnts of loaded entities to get newly loaded ones.
                    for (int i = 0; i < newLoadedEnts.size(); i++) {
                        //foreach currently loaded entity
                        StarEntity newEntity = newLoadedEnts.get(i);
                        //check if its in old newLoadedEnts
                        boolean alreadyListed = IsInList(oldLoadedEnts, newEntity.getUID());
                        if (!alreadyListed) {
                            newEnts.add(newEntity);
                        } else {
                        }
                    }

                    for (int i = 0; i < newEnts.size(); i++) {
                        EntityLoadedEvent event = new EntityLoadedEvent(newEnts.get(i));
                        StarLoader.fireEvent(EntityLoadedEvent.class, event, true);
                    }

                    for (int i = 0; i < oldLoadedEnts.size(); i++) {
                        //if old entity not in new newLoadedEnts -> was unloaded
                        StarEntity oldEnt = oldLoadedEnts.get(i);
                        boolean inNewList = IsInList(newLoadedEnts,oldEnt.getUID());
                        if (!inNewList) {
                            unloadedEnts.add(oldEnt);
                        }
                    }

                    for (int i = 0; i < unloadedEnts.size(); i++) {
                        //TODO get unloaded Entities UID -> star api merge request
                        //Events.EntityUnloadedEvent event = new Events.EntityUnloadedEvent(unloadedEnts.get(i).getUID());
                        //StarLoader.fireEvent(Events.EntityUnloadedEvent.class, event, true);
                    }
                    //ModPlayground.broadcastMessage("updating entities loaded. new: " + newEnts.size() + " unloaded:" + unloadedEnts.size() +" total:" + newLoadedEnts.size() +   "----------------------------" + System.currentTimeMillis()/1000);
                    oldLoadedEnts = newLoadedEnts; //replace old loaded entity newLoadedEnts with new one.
                }

            }
            private boolean IsInList(ArrayList<StarEntity> newLoadedEnts, String UID) {
            for (int j = 0; j < newLoadedEnts.size(); j++) {
                    //compare UIDs
                    //break out at first matching instance
                    if (newLoadedEnts.get(j).getUID().equals(UID)) {
                        //already in loaded Entities.
                        return true;
                    } else {
                        //found a match, entity is in newLoadedEnts
                    }
                }
                //looped through complete newLoadedEnts, didnt find a match, entity is not in newLoadedEnts
                return false;
            }
            private ArrayList<StarEntity> getoldLoadedEnts() {
                ArrayList<StarEntity> newLoadedEnts = new ArrayList<>();
                for (SegmentController e: Server.getServerState().getSegmentControllersByName().values()) {
                    newLoadedEnts.add(new StarEntity(e));
                }
                return newLoadedEnts;
            }
        }.runTimer(25);
    }


}
