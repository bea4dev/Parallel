package be4rjp.parallel.chiyogami;

import org.bukkit.World;
import world.chiyogami.chiyogamilib.ChiyogamiLib;
import world.chiyogami.chiyogamilib.ServerType;

import java.lang.reflect.Method;
import java.util.UUID;

public class ChiyogamiBridge {
    
    private static Method addEditedBlock;
    private static Method removeEditedBlock;
    
    static {
        if(ChiyogamiLib.getServerType() == ServerType.CHIYOGAMI){
            try{
                Class<?> wrappedParallelWorld = Class.forName("world.chiyogami.bridge.WrappedParallelWorld");
                addEditedBlock = wrappedParallelWorld.getMethod("addEditedBlock", World.class, int.class, int.class, int.class);
                removeEditedBlock = wrappedParallelWorld.getMethod("removeEditedBlock", World.class, int.class, int.class, int.class);
            }catch (Exception e){e.printStackTrace();}
        }
    }
    
    public static void addEditedBlock(World world, int x, int y, int z, Object wrappedParallelWorldObject){
        try {
            addEditedBlock.invoke(wrappedParallelWorldObject, world, x, y, z);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void removeEditedBlock(World world, int x, int y, int z, Object wrappedParallelWorldObject){
        try {
            removeEditedBlock.invoke(wrappedParallelWorldObject, world, x, y, z);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static Object getWrappedParallelWorld(UUID uuid){
        try {
            Class<?> ParallelWorldBridge = Class.forName("world.chiyogami.bridge.ParallelWorldBridge");
            Method method = ParallelWorldBridge.getMethod("getWrappedParallelWorld", UUID.class);
            return method.invoke(null, uuid);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return null;
    }
    
}
