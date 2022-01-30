package be4rjp.parallel.v1_15_R1;

import be4rjp.parallel.ParallelUniverse;
import be4rjp.parallel.ParallelWorld;
import be4rjp.parallel.nms.IPacketHandler;
import be4rjp.parallel.player.ParallelPlayer;
import net.minecraft.server.v1_15_R1.BlockPosition;
import net.minecraft.server.v1_15_R1.PacketPlayOutBlockChange;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.v1_15_R1.block.data.CraftBlockData;

import java.lang.reflect.Field;

public class BlockChangePacketHandler implements IPacketHandler {

    public static Field a;

    static {
        try {
            a = PacketPlayOutBlockChange.class.getDeclaredField("a");
            a.setAccessible(true);
        }catch (Exception e){e.printStackTrace();}
    }

    @Override
    public Object rewrite(Object packet, ParallelPlayer parallelPlayer, boolean cacheSetting) {

        ParallelUniverse universe = parallelPlayer.getUniverse();
        if(universe == null) return packet;

        String worldName = parallelPlayer.getBukkitPlayer().getWorld().getName();
        ParallelWorld parallelWorld = universe.getWorld(worldName);

        try {
            PacketPlayOutBlockChange blockChange = (PacketPlayOutBlockChange) packet;
            BlockPosition bp = (BlockPosition) a.get(blockChange);

            BlockData blockData = parallelWorld.getBlockData(bp.getX(), bp.getY(), bp.getZ());
            if(blockData == null) return packet;

            PacketPlayOutBlockChange newPacket = new PacketPlayOutBlockChange();
            a.set(newPacket, bp);
            newPacket.block = ((CraftBlockData) blockData).getState();

            return newPacket;
        }catch (Exception e){e.printStackTrace();}

        return packet;
    }
}
