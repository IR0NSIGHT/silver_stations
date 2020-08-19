import Events.EntityLoadEventLoop;
import api.mod.StarMod;

//Mods must extend StarMod
public class IRNCore extends StarMod {
    public static IRNstationManager stationManager;
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
        EntityLoadEventLoop loop = new EntityLoadEventLoop();
        stationManager = new IRNstationManager();
        Debugger debugger = new Debugger(stationManager,stationManager.anchorManager);
    }
}