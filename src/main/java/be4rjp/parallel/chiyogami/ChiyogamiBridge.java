package be4rjp.parallel.chiyogami;

import org.bukkit.World;
import world.chiyogami.bridge.ParallelWorldBridge;
import world.chiyogami.bridge.WrappedParallelWorld;

import java.util.UUID;

public class ChiyogamiBridge {
    
    public static void addEditedBlock(World world, int x, int y, int z, Object wrappedParallelWorldObject){
        WrappedParallelWorld wrappedParallelWorld = (WrappedParallelWorld) wrappedParallelWorldObject;
        wrappedParallelWorld.addEditedBlock(world, x, y, z);
    }
    
    public static void removeEditedBlock(World world, int x, int y, int z, Object wrappedParallelWorldObject){
        WrappedParallelWorld wrappedParallelWorld = (WrappedParallelWorld) wrappedParallelWorldObject;
        wrappedParallelWorld.removeEditedBlock(world, x, y, z);
    }
    
    public static Object getWrappedParallelWorld(UUID uuid){
        return ParallelWorldBridge.getWrappedParallelWorld(uuid);
    }
    
}
