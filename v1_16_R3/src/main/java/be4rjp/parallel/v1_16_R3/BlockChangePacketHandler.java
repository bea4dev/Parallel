package be4rjp.parallel.v1_16_R3;

import be4rjp.parallel.ParallelUniverse;
import be4rjp.parallel.ParallelWorld;
import be4rjp.parallel.nms.IPacketHandler;
import be4rjp.parallel.player.ParallelPlayer;
import net.minecraft.server.v1_16_R3.BlockPosition;
import net.minecraft.server.v1_16_R3.IBlockData;
import net.minecraft.server.v1_16_R3.PacketPlayOutBlockBreak;
import net.minecraft.server.v1_16_R3.PacketPlayOutBlockChange;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.v1_16_R3.block.data.CraftBlockData;

import java.lang.reflect.Field;

public class BlockChangePacketHandler implements IPacketHandler {

    public static Field a;
    public static Field c;
    public static Field d;

    static {
        try {
            a = PacketPlayOutBlockChange.class.getDeclaredField("a");
            a.setAccessible(true);

            c = PacketPlayOutBlockBreak.class.getDeclaredField("c");
            d = PacketPlayOutBlockBreak.class.getDeclaredField("d");
            c.setAccessible(true);
            d.setAccessible(true);
        }catch (Exception e){e.printStackTrace();}
    }

    @Override
    public Object rewrite(Object packet, ParallelPlayer parallelPlayer, boolean cacheSetting) {

        ParallelUniverse universe = parallelPlayer.getUniverse();
        if(universe == null) return packet;

        String worldName = parallelPlayer.getBukkitPlayer().getWorld().getName();
        ParallelWorld parallelWorld = universe.getWorld(worldName);

        try {
            if (packet instanceof PacketPlayOutBlockChange) {
                PacketPlayOutBlockChange blockChange = (PacketPlayOutBlockChange) packet;
                BlockPosition bp = (BlockPosition) a.get(blockChange);

                BlockData blockData = parallelWorld.getBlockData(bp.getX(), bp.getY(), bp.getZ());
                if (blockData == null) return packet;
                //if(blockData.getMaterial() == Material.AIR) return packet;

                blockChange.block = ((CraftBlockData) blockData).getState();

                return blockChange;
            } else {
                PacketPlayOutBlockBreak blockBreak = (PacketPlayOutBlockBreak) packet;
                BlockPosition bp = (BlockPosition) c.get(blockBreak);

                BlockData blockData = parallelWorld.getBlockData(bp.getX(), bp.getY(), bp.getZ());
                if (blockData == null) return packet;

                IBlockData data = ((CraftBlockData) blockData).getState();
                d.set(blockBreak, data);

                return blockBreak;
            }
        }catch (Exception e){e.printStackTrace();}

        return packet;
    }
}
