package be4rjp.parallel.nms.manager;

import be4rjp.parallel.ParallelWorld;
import be4rjp.parallel.nms.NMSUtil;
import be4rjp.parallel.nms.PacketHandler;
import be4rjp.parallel.util.ChunkLocation;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.nio.channels.ClosedChannelException;
import java.util.Map;

public class ChunkPacketManager extends BukkitRunnable {
    
    private static Class<?> PacketPlayOutMapChunk;
    private static Field a;
    private static Field b;
    private static Field loc;
    private static Field d;
    
    static {
        try {
            PacketPlayOutMapChunk = NMSUtil.getNMSClass("PacketPlayOutMapChunk");
            a = PacketPlayOutMapChunk.getDeclaredField("a");
            b = PacketPlayOutMapChunk.getDeclaredField("b");
            a.setAccessible(true);
            b.setAccessible(true);
            
            Class<?> Chunk = NMSUtil.getNMSClass("Chunk");
            loc = Chunk.getDeclaredField("loc");
            d = Chunk.getDeclaredField("d");
            loc.setAccessible(true);
            d.setAccessible(true);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    
    
    
    private final ChannelHandlerContext channelHandlerContext;
    private final Object packet;
    private final ChannelPromise channelPromise;
    private final PacketHandler packetHandler;
    private final Player player;
    
    public ChunkPacketManager(ChannelHandlerContext channelHandlerContext, Object packet, ChannelPromise channelPromise, PacketHandler packetHandler, Player player){
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
        
            int chunkX = (int) a.get(packet);
            int chunkZ = (int) b.get(packet);
    
            ChunkLocation chunkLocation = new ChunkLocation(chunkX << 4, chunkZ << 4);
            Map<Location, BlockData> dataMap = parallelWorld.getChunkBlockMap().get(chunkLocation);
            if(dataMap == null){
                packetHandler.doWrite(channelHandlerContext, packet, channelPromise);
                return;
            }
        
            Object nmsWorld = NMSUtil.getNMSWorld(player.getWorld());
            Object nmsChunk = NMSUtil.getNMSChunk(player.getWorld().getChunkAt(chunkX, chunkZ));
        
            Object newChunk = NMSUtil.createChunk(nmsWorld, loc.get(nmsChunk), d.get(nmsChunk));
        
        
            //Copy block data
            Object sections = NMSUtil.getChunkSections(nmsChunk);
            for (int y = 0; y < 16; y++) {
                if (Array.get(sections, y) != null) {
                    Object chunkSection = NMSUtil.createChunkSection(y << 4);
                
                    int x = chunkX << 4;
                    int z = chunkZ << 4;
                
                    for (int ix = 0; ix < 16; ix++) {
                        for (int iy = 0; iy < 16; iy++) {
                            for (int iz = 0; iz < 16; iz++) {
                                Object iBlockData = NMSUtil.getTypeChunkSection(Array.get(sections, y), (x + ix) & 15, (y + iy) & 15, (z + iz) & 15);
                                BlockData blockData = NMSUtil.getBlockData(iBlockData);
                                Object iBlockData2 = NMSUtil.getIBlockData(blockData.clone());
                                NMSUtil.setTypeChunkSection(chunkSection, (x + ix) & 15, (y + iy) & 15, (z + iz) & 15, iBlockData2);
                            }
                        }
                    }
                
                    Object newSections = NMSUtil.getChunkSections(newChunk);
                    Array.set(newSections, y, chunkSection);
                }
            }
            
            int count = 0;
            for (Map.Entry<Location, BlockData> entry : dataMap.entrySet()) {
                Location location = entry.getKey();
                BlockData blockData = entry.getValue();
                Chunk chunk = location.getChunk();
            
                if (chunk.getX() == chunkX && chunk.getZ() == chunkZ) {
                    Object iBlockData = NMSUtil.getIBlockData(blockData);
                
                    Object newSections = NMSUtil.getChunkSections(newChunk);
                    try{
                        Object cs = Array.get(newSections, location.getBlockY() >> 4);
                        if (cs == null) {
                            cs = NMSUtil.createChunkSection(location.getBlockY() >> 4 << 4);
                            Array.set(newSections, location.getBlockY() >> 4, cs);
                        }
                        NMSUtil.setTypeChunkSection(cs, location.getBlockX() & 15, location.getBlockY() & 15, location.getBlockZ() & 15, iBlockData);
                    }catch (ArrayIndexOutOfBoundsException e){/**/}
                    count++;
                }
            }
        
            if (count == 0) {
                packetHandler.doWrite(channelHandlerContext, packet, channelPromise);
                return;
            }
        
            Object newPacket = NMSUtil.createPacketPlayOutMapChunk(newChunk, 65535);
            packetHandler.doWrite(channelHandlerContext, newPacket, channelPromise);
        }catch (ClosedChannelException e){
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
