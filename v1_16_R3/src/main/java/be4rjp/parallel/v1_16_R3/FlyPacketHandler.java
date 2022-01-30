package be4rjp.parallel.v1_16_R3;

import be4rjp.parallel.ParallelUniverse;
import be4rjp.parallel.ParallelWorld;
import be4rjp.parallel.nms.IPacketHandler;
import be4rjp.parallel.player.ParallelPlayer;
import net.minecraft.server.v1_16_R3.EntityPlayer;
import net.minecraft.server.v1_16_R3.PlayerConnection;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.util.NumberConversions;

import java.lang.reflect.Field;


public class FlyPacketHandler implements IPacketHandler {
    
    private static Field C;
    private static Field E;
    
    static {
        try {
            C = PlayerConnection.class.getDeclaredField("C");
            E = PlayerConnection.class.getDeclaredField("E");
            
            C.setAccessible(true);
            E.setAccessible(true);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    
    @Override
    public Object rewrite(Object packet, ParallelPlayer parallelPlayer, boolean cacheSetting) {
        ParallelUniverse universe = parallelPlayer.getUniverse();
        if(universe == null) return packet;
        
        World world = parallelPlayer.getBukkitPlayer().getWorld();
        String worldName = world.getName();
        ParallelWorld parallelWorld = universe.getWorld(worldName);
        
        EntityPlayer entityPlayer = ((CraftPlayer) parallelPlayer.getBukkitPlayer()).getHandle();
        
        int x = NumberConversions.floor(entityPlayer.locX());
        int y = NumberConversions.floor(entityPlayer.locY());
        int z = NumberConversions.floor(entityPlayer.locZ());
        
        int downY = y - 1;
        downY = Math.max(0, downY);
    
        if(parallelWorld.hasBlockData(x, y, z) || parallelWorld.hasBlockData(x, downY, z)){
            try {
                PlayerConnection playerConnection = entityPlayer.playerConnection;
                C.set(playerConnection, 0);
                E.set(playerConnection, 0);
            }catch (Exception e){e.printStackTrace();}
        }
        
        return packet;
    }
    
}

