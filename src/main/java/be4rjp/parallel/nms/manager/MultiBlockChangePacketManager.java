package be4rjp.parallel.nms.manager;

import be4rjp.parallel.ParallelWorld;
import be4rjp.parallel.nms.NMSUtil;
import be4rjp.parallel.nms.PacketHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import org.bukkit.Location;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.nio.channels.ClosedChannelException;
import java.util.*;

public class MultiBlockChangePacketManager extends BukkitRunnable {
    
    private static Class<?> PacketPlayOutMultiBlockChange;
    private static Class<?> MultiBlockChangeInfo;
    private static Class<?> IBlockData;
    private static Field b;
    
    static {
        try {
            PacketPlayOutMultiBlockChange = NMSUtil.getNMSClass("PacketPlayOutMultiBlockChange");
            MultiBlockChangeInfo = NMSUtil.getNMSClass("PacketPlayOutMultiBlockChange$MultiBlockChangeInfo");
            IBlockData = NMSUtil.getNMSClass("IBlockData");
            b = PacketPlayOutMultiBlockChange.getDeclaredField("b");
            b.setAccessible(true);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    
    
    
    private final ChannelHandlerContext channelHandlerContext;
    private final Object packet;
    private final ChannelPromise channelPromise;
    private final PacketHandler packetHandler;
    private final Player player;
    
    public MultiBlockChangePacketManager(ChannelHandlerContext channelHandlerContext, Object packet, ChannelPromise channelPromise, PacketHandler packetHandler, Player player){
        this.channelHandlerContext = channelHandlerContext;
        this.packet = packet;
        this.channelPromise = channelPromise;
        this.packetHandler = packetHandler;
        this.player = player;
    }
    
    
    @Override
    public void run() {
        try {
            Object multiBlockChangeInfoArray = b.get(packet);
    
            ParallelWorld parallelWorld = ParallelWorld.getParallelWorld(player);
    
            for (int index = 0; index < Array.getLength(multiBlockChangeInfoArray); index++) {
                for (Map.Entry<Location, BlockData> entry : parallelWorld.getBlockMap()) {
                    Location location = entry.getKey();
                    BlockData blockData = entry.getValue();
    
                    Object originalInfo = Array.get(multiBlockChangeInfoArray, index);
                    short loc = (short) ((location.getBlockX() & 15) << 12 | (location.getBlockZ() & 15) << 8 | location.getBlockY());
                    short b = (short) MultiBlockChangeInfo.getMethod("b").invoke(originalInfo);
    
                    if (loc == b) {
                        Object info = MultiBlockChangeInfo.getConstructor(PacketPlayOutMultiBlockChange, short.class, IBlockData).newInstance(packet, loc, NMSUtil.getIBlockData(blockData));
                        Array.set(multiBlockChangeInfoArray, index, info);
                    }
                }
            }
    
            packetHandler.doWrite(channelHandlerContext, packet, channelPromise);
        }catch (ClosedChannelException e){
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
