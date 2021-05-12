package be4rjp.parallel.cinema4c;

import be4rjp.cinema4c.bridge.Cinema4CBridge;

public class BridgeManager {
    public static void registerPluginBridge(String name){
        Cinema4CBridge.registerPluginBridge(name, new C4CBridge());
    }
}
