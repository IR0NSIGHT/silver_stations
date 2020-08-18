package Events;

import api.ModPlayground;
import api.entity.StarEntity;
import api.listener.events.Event;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.generator.AsteroidCreatorThread;
import org.schema.game.server.controller.RequestData;

public class EntityLoadedEvent extends Event{
        /*
            fired from EntityLoadEventload whenever a new segment controller is detected.
            loop runs once a second -> event is not instant.
        */
        private final String entityUID;
        private final SegmentController entitySegmentController;
        private final StarEntity starEntity;
        public EntityLoadedEvent(StarEntity starEntity) {
            this.entitySegmentController = starEntity.internalEntity;
            this.entityUID = starEntity.getUID();
            this.starEntity = starEntity;
            //ModPlayground.broadcastMessage("entity loaded event fired for " + this.entityUID);
        }
        public String getEntityUID() {
            return this.entityUID;
        }
        public StarEntity getStarEntity() {
            return this.starEntity;
        }
        public SegmentController getSegmentController() {
            return this.entitySegmentController;
        }

}
