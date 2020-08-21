import api.DebugFile;
import api.ModPlayground;
import api.common.GameServer;
import api.utils.StarRunnable;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.List;

public class stationFileHandler {
    final IRNstationManager manager;
    String filePath = "silverStations.ser";
    boolean hasReadFile = false;
     /**
     * handles all things station-
     * file writing
     * file reading
     */
    public stationFileHandler(IRNstationManager manager) {
        this.manager = manager;
        FileLoop();
    }
    private void FileLoop() {
        new StarRunnable() {
            @Override
            public void run() {
                if (GameServer.getServerState() != null) {
                    chatDebug("file handle loop running at ------------" + System.currentTimeMillis()/1000);
                    if (!hasReadFile) {
                        /**
                         * get all stations from the save file
                         * check if each station already exists in the runtime manager stationslist
                         * if not, add to manager stationslist
                         * only execute once
                         */
                        //TODO check reading for erorrs
                        List<IRNstationModule> fileList = manager.ReadListFromFile(filePath);
                        for (int i = 0; i < fileList.size(); i++) {
                            int idx = manager.FindStationInList(manager.stations,fileList.get(i).stationUID);
                            if (idx == -1) {
                                manager.stations.add(fileList.get(i));
                            }
                        }
                        hasReadFile = true;
                    }
                    //write current station list to file -> update file
                    WriteFile(manager.stations);
                }
            }
            public boolean WriteFile(List<IRNstationModule> stationsL) {  //create a savefile for the stations if it doesnt exist yet.
                chatDebug("file creator was called");
                FileOutputStream fos = null;
                ObjectOutputStream oos = null;
                chatDebug("creating file");
                if (stationsL == null) {
                    chatDebug("station list is null");
                    return false;
                }
                chatDebug("creating fos");
                //create file output stream
                try {
                    fos = new FileOutputStream(filePath);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    chatDebug(e.toString());
                    return false;
                }
                chatDebug("creating oos");
                //create Object output system
                try {
                    oos = new ObjectOutputStream(fos);
                } catch (IOException e) {
                    e.printStackTrace();
                    chatDebug(e.toString());
                    return false;
                }
                //write file
                if (fos != null && oos != null) {
                    try {
                        for (int i = 0; i < stationsL.size(); i++) {    //write each sation object by itself
                            oos.writeObject(stationsL.get(i));
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        chatDebug(e.toString());
                        return false;
                    }
                }
                //chatDebug("closing");
                try {
                    oos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    chatDebug(e.toString());
                    return false;
                }
                chatDebug("file created");
                return true;
            }
            private void chatDebug(String s) {
                if (true) {
                    DebugFile.log(System.currentTimeMillis()/1000 + "" + s, manager.modInstance);
                   // ModPlayground.broadcastMessage(s);
                }
            }
        }.runTimer(25 * 5);
    }
}
