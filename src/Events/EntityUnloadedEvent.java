package Events;

import api.ModPlayground;
import api.listener.events.Event;

public class EntityUnloadedEvent extends Event {
    private final String entityUID;
    public  EntityUnloadedEvent(String UID) {
        this.entityUID = UID;

       // ModPlayground.broadcastMessage("entity unloaded event fired for " + this.entityUID);
    }
    public String getEntityUID() {
        return this.entityUID;
    }
}
