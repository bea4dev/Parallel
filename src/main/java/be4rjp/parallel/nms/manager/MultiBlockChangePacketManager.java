package be4rjp.parallel.nms.manager;

import be4rjp.parallel.ParallelWorld;
import be4rjp.parallel.nms.NMSUtil;
import be4rjp.parallel.nms.PacketHandler;
import be4rjp.parallel.util.BlockLocation;
import be4rjp.parallel.util.BlockPosition3i;
import be4rjp.parallel.util.ChunkPosition;
import be4rjp.parallel.util.PBlockData;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
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
    private static Field a;
    private static Field b;
    private static Field c;
    
    public static boolean VERSION_1_16_R3 = false;
    
    static {
        try {
            PacketPlayOutMultiBlockChange = NMSUtil.getNMSClass("PacketPlayOutMultiBlockChange");
            IBlockData = NMSUtil.getNMSClass("IBlockData");
            a = PacketPlayOutMultiBlockChange.getDeclaredField("a");
            b = PacketPlayOutMultiBlockChange.getDeclaredField("b");
            a.setAccessible(true);
            b.setAccessible(true);
            
            try{
                MultiBlockChangeInfo = NMSUtil.getNMSClass("PacketPlayOutMultiBlockChange$MultiBlockChangeInfo");
            }catch (ClassNotFoundException e){
                VERSION_1_16_R3 = true;
                c = PacketPlayOutMultiBlockChange.getDeclaredField("c");
                c.setAccessible(true);
            }
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
            
            ParallelWorld parallelWorld = ParallelWorld.getParallelWorld(player);
            
            if(VERSION_1_16_R3){
                short[] locArray = (short[]) b.get(packet);
                Object blockDataArray = c.get(packet);

                BlockPosition3i blockPosition3i = NMSUtil.getBlockPosition3i(a.get(packet));
                ChunkPosition chunkPosition = new ChunkPosition(blockPosition3i.getX() << 4, blockPosition3i.getZ() << 4);
                Map<BlockLocation, PBlockData> dataMap = parallelWorld.getChunkBlockMap().get(chunkPosition);
                if(dataMap == null){
                    packetHandler.doWrite(channelHandlerContext, packet, channelPromise);
                    return;
                }

                for (int index = 0; index < locArray.length; index++) {
                    for (Map.Entry<BlockLocation, PBlockData> entry : dataMap.entrySet()) {
                        BlockLocation location = entry.getKey();
                        BlockData blockData = entry.getValue().getBlockData();

                        if(blockData == null) continue;
                        if(player.getWorld() != location.getWorld()) continue;

                        if((location.getY() >> 4) != blockPosition3i.getY()) continue;
    
                        int x = location.getX() & 0xF;
                        int y = location.getY() & 0xF;
                        int z = location.getZ() & 0xF;
                        
                        short loc = (short) (x << 8 | z << 4 | y);
                        short bLoc = locArray[index];
                        
                        if(loc == bLoc){
                            Array.set(blockDataArray, index, NMSUtil.getIBlockData(blockData));
                        }
                    }
                }
            } else {
                Object multiBlockChangeInfoArray = b.get(packet);

                ChunkPosition chunkPosition = NMSUtil.getChunkLocation(a.get(packet));
                Map<BlockLocation, PBlockData> dataMap = parallelWorld.getChunkBlockMap().get(chunkPosition);
                if(dataMap == null){
                    packetHandler.doWrite(channelHandlerContext, packet, channelPromise);
                    return;
                }
                
                for (int index = 0; index < Array.getLength(multiBlockChangeInfoArray); index++) {
                    for (Map.Entry<BlockLocation, PBlockData> entry : dataMap.entrySet()) {
                        BlockLocation location = entry.getKey();
                        BlockData blockData = entry.getValue().getBlockData();

                        if(blockData == null) continue;
                        if(player.getWorld() != location.getWorld()) continue;
            
                        Object originalInfo = Array.get(multiBlockChangeInfoArray, index);
                        short loc = (short) ((location.getX() & 0xF) << 12 | (location.getZ() & 0xF) << 8 | location.getY());
                        short b = (short) MultiBlockChangeInfo.getMethod("b").invoke(originalInfo);
            
                        if (loc == b) {
                            Object info = MultiBlockChangeInfo.getConstructor(PacketPlayOutMultiBlockChange, short.class, IBlockData).newInstance(packet, loc, NMSUtil.getIBlockData(blockData));
                            Array.set(multiBlockChangeInfoArray, index, info);
                        }
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
