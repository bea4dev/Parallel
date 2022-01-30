package be4rjp.parallel.chiyogami;

import org.bukkit.World;
import world.chiyogami.chiyogamilib.ChiyogamiLib;
import world.chiyogami.chiyogamilib.ServerType;

import java.util.UUID;

public class ChiyogamiManager {
    
    public static void addEditedBlock(World world, int x, int y, int z, Object wrappedParallelWorld){
        if(ChiyogamiLib.getServerType() == ServerType.CHIYOGAMI && wrappedParallelWorld != null){
            ChiyogamiBridge.addEditedBlock(world, x, y, z, wrappedParallelWorld);
        }
    }
    
    public static void removeEditedBlock(World world, int x, int y, int z, Object wrappedParallelWorld){
        if(ChiyogamiLib.getServerType() == ServerType.CHIYOGAMI && wrappedParallelWorld != null){
            ChiyogamiBridge.removeEditedBlock(world, x, y, z, wrappedParallelWorld);
        }
    }
    
    public static Object getWrappedParallelWorld(UUID uuid){
        if(ChiyogamiLib.getServerType() == ServerType.CHIYOGAMI){
            return ChiyogamiBridge.getWrappedParallelWorld(uuid);
        }
        return null;
    }
    
}
