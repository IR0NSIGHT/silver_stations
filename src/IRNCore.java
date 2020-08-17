import api.ModPlayground;
import api.common.GameServer;
import api.listener.Listener;
import api.listener.events.player.PlayerChatEvent;
import api.listener.events.systems.InterdictionCheckEvent;
import api.mod.StarLoader;
import api.mod.StarMod;
import api.utils.StarRunnable;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.data.PlayerControllable;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.data.player.ControllerStateUnit;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.player.faction.Faction;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.game.common.data.world.StellarSystem;
import org.schema.game.common.data.world.Universe;
import org.schema.game.server.controller.SectorSwitch;
import org.schema.game.server.data.GameServerState;
import org.schema.game.server.data.PlayerNotFountException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

//Mods must extend StarMod
public class IRNCore extends StarMod {
    public static IRNstationManager stations = null;
    //This blank main method is needed, it allows you to select this as your main class.
    public static void main(String[] args) {
    }

    //OnGameStart is called whenever the game starts, this is where you want to register your mods name, version, and other info.
    @Override
    public void onGameStart() {
        setModName("IRN stations");
        setModVersion("0.20");
        setModDescription("adds special purpose stations");
    }

    //onEnable, more on this later.
    @Override
    public void onEnable() {
       IRNstationManager stations = new IRNstationManager();
    }
}