package be4rjp.parallel.nms.manager;

import be4rjp.parallel.Config;
import be4rjp.parallel.Parallel;
import be4rjp.parallel.ParallelWorld;
import be4rjp.parallel.chunk.AsyncChunkCache;
import be4rjp.parallel.nms.NMSUtil;
import be4rjp.parallel.nms.PacketHandler;
import be4rjp.parallel.util.BlockLocation;
import be4rjp.parallel.util.ChunkLocation;
import be4rjp.parallel.util.ChunkPosition;
import be4rjp.parallel.util.PBlockData;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.nio.channels.ClosedChannelException;
import java.util.Map;
import java.util.Set;

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
    
            ChunkPosition chunkPosition = new ChunkPosition(chunkX << 4, chunkZ << 4);
            Map<BlockLocation, PBlockData> dataMap = parallelWorld.getChunkBlockMap().get(chunkPosition);
            if(dataMap == null){
                packetHandler.doWrite(channelHandlerContext, packet, channelPromise);
                return;
            }
    
            ChunkLocation chunkLocation = new ChunkLocation(player.getWorld(), chunkPosition.x << 4, chunkPosition.z << 4);
            Object editedPacket = parallelWorld.getEditedPacketForChunkMap().get(chunkLocation);
            if(editedPacket != null && Config.isPerformanceMode()){
                packetHandler.doWrite(channelHandlerContext, editedPacket, channelPromise);
                return;
            }
    
            World world = player.getWorld();
            Object nmsWorld = NMSUtil.getNMSWorld(world);
            
            boolean isSameWorld = false;
            for(BlockLocation blockLocation : dataMap.keySet()){
                if(blockLocation.getWorld() == world){
                    isSameWorld = true;
                    break;
                }
            }
            if(!isSameWorld){
                packetHandler.doWrite(channelHandlerContext, packet, channelPromise);
                return;
            }
            

            AsyncChunkCache asyncChunkCache = AsyncChunkCache.getWorldAsyncChunkCash(world.getName());
            if(asyncChunkCache == null){
                packetHandler.doWrite(channelHandlerContext, packet, channelPromise);
                sendChunkWarnMessage();
                return;
            }

            Object nmsChunk = asyncChunkCache.getCashedChunk(chunkX, chunkZ);
            if(nmsChunk == null){
                packetHandler.doWrite(channelHandlerContext, packet, channelPromise);
                sendChunkWarnMessage();
                return;
            }
        
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
                                Object iBlockData = NMSUtil.getTypeChunkSection(Array.get(sections, y), (x + ix) & 0xF, (y + iy) & 0xF, (z + iz) & 0xF);
                                BlockData blockData = NMSUtil.getBlockData(iBlockData);
                                Object iBlockData2 = NMSUtil.getIBlockData(blockData.clone());
                                NMSUtil.setTypeChunkSection(chunkSection, (x + ix) & 0xF, (y + iy) & 0xF, (z + iz) & 0xF, iBlockData2);
                            }
                        }
                    }
                
                    Object newSections = NMSUtil.getChunkSections(newChunk);
                    Array.set(newSections, y, chunkSection);
                }
            }
            
            int count = 0;
            for (Map.Entry<BlockLocation, PBlockData> entry : dataMap.entrySet()) {
                BlockLocation location = entry.getKey();
                BlockData blockData = entry.getValue().getBlockData();
                Chunk chunk = location.getChunk();

                if(blockData == null) continue;
                if(world != location.getWorld()) continue;
            
                if (chunk.getX() == chunkX && chunk.getZ() == chunkZ) {
                    Object iBlockData = NMSUtil.getIBlockData(blockData);
                
                    Object newSections = NMSUtil.getChunkSections(newChunk);
                    try{
                        Object cs = Array.get(newSections, location.getY() >> 4);
                        if (cs == null) {
                            cs = NMSUtil.createChunkSection(location.getY() >> 4 << 4);
                            Array.set(newSections, location.getY() >> 4, cs);
                        }
                        NMSUtil.setTypeChunkSection(cs, location.getX() & 0xF, location.getY() & 0xF, location.getZ() & 0xF, iBlockData);
                    }catch (ArrayIndexOutOfBoundsException e){/**/}
                    count++;
                }
            }
        
            if (count == 0) {
                packetHandler.doWrite(channelHandlerContext, packet, channelPromise);
                return;
            }
        
            Object newPacket = NMSUtil.createPacketPlayOutMapChunk(newChunk, 65535);
            parallelWorld.getEditedPacketForChunkMap().put(chunkLocation, newPacket);
            packetHandler.doWrite(channelHandlerContext, newPacket, channelPromise);
        }catch (ClosedChannelException e){
            //None
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void sendChunkWarnMessage(){
        if(Config.isShowChunkPacketWarning()) Parallel.getPlugin().getLogger().warning("Attempted to send a packet with an unloaded chunk.");
    }
}
