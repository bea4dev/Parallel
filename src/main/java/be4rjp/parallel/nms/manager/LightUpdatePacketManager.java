package be4rjp.parallel.nms.manager;

import be4rjp.parallel.Config;
import be4rjp.parallel.ParallelWorld;
import be4rjp.parallel.chunk.AsyncChunkCache;
import be4rjp.parallel.nms.NMSClass;
import be4rjp.parallel.nms.NMSUtil;
import be4rjp.parallel.nms.PacketHandler;
import be4rjp.parallel.util.*;
import com.google.common.collect.Lists;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class LightUpdatePacketManager extends BukkitRunnable {
    
    private static Class<?> PacketPlayOutLightUpdate;
    private static Class<?> NibbleArray;
    
    private static Field a;
    private static Field b;
    private static Field c;
    private static Field d;
    private static Field e;
    private static Field f;
    private static Field g;
    private static Field h;
    private static Field i;
    
    private static Method methodA;
    private static Method methodC;
    private static Method asBytes;
    
    public static boolean VERSION_1_16_R1 = true;
    
    static {
        try {
            PacketPlayOutLightUpdate = NMSClass.PACKET_PLAY_OUT_LIGHT_UPDATE.getNMSClass();
            NibbleArray = NMSClass.NIBBLE_ARRAY.getNMSClass();
    
            a = PacketPlayOutLightUpdate.getDeclaredField("a");
            b = PacketPlayOutLightUpdate.getDeclaredField("b");
            c = PacketPlayOutLightUpdate.getDeclaredField("c");
            d = PacketPlayOutLightUpdate.getDeclaredField("d");
            e = PacketPlayOutLightUpdate.getDeclaredField("e");
            f = PacketPlayOutLightUpdate.getDeclaredField("f");
            g = PacketPlayOutLightUpdate.getDeclaredField("g");
            h = PacketPlayOutLightUpdate.getDeclaredField("h");
    
            a.setAccessible(true);
            b.setAccessible(true);
            c.setAccessible(true);
            d.setAccessible(true);
            e.setAccessible(true);
            f.setAccessible(true);
            g.setAccessible(true);
            h.setAccessible(true);
            
            methodA = NibbleArray.getMethod("a", int.class, int.class, int.class, int.class);
            methodC = NibbleArray.getMethod("c");
            asBytes = NibbleArray.getMethod("asBytes");
            
            try{
                i = PacketPlayOutLightUpdate.getDeclaredField("i");
                i.setAccessible(true);
            }catch (NoSuchFieldException e){
                VERSION_1_16_R1 = false;
            }
        }catch (Exception e){e.printStackTrace();}
    }
    
    
    private final ChannelHandlerContext channelHandlerContext;
    private final Object packet;
    private final ChannelPromise channelPromise;
    private final PacketHandler packetHandler;
    private final Player player;
    
    public LightUpdatePacketManager(ChannelHandlerContext channelHandlerContext, Object packet, ChannelPromise channelPromise, PacketHandler packetHandler, Player player){
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
            
            int chunkX = a.getInt(packet);
            int chunkZ = b.getInt(packet);
    
            World world = player.getWorld();
            Object nmsWorld = NMSUtil.getNMSWorld(world);
            /*
            AsyncChunkCache asyncChunkCache = AsyncChunkCache.getWorldAsyncChunkCash(player.getWorld().getName());
            if(asyncChunkCache == null){
                packetHandler.doWrite(channelHandlerContext, packet, channelPromise);
                ChunkPacketManager.sendChunkWarnMessage();
                return;
            }
    
            Object nmsChunk = asyncChunkCache.getCashedChunk(chunkX, chunkZ);
            if(nmsChunk == null){
                packetHandler.doWrite(channelHandlerContext, packet, channelPromise);
                ChunkPacketManager.sendChunkWarnMessage();
                return;
            }*/
    
            ChunkPosition chunkPosition = new ChunkPosition(chunkX << 4, chunkZ << 4);
            Map<BlockLocation, PBlockData> dataMap = parallelWorld.getChunkBlockMap().get(chunkPosition);
            if(dataMap == null){
                packetHandler.doWrite(channelHandlerContext, packet, channelPromise);
                return;
            }
    
            ChunkLocation chunkLocation = new ChunkLocation(player.getWorld(), chunkPosition.x << 4, chunkPosition.z << 4);
            Object editedPacket = parallelWorld.getEditedPacketForLightMap().get(chunkLocation);
            if(editedPacket != null && Config.isPerformanceMode()){
                packetHandler.doWrite(channelHandlerContext, editedPacket, channelPromise);
                return;
            }
            
    
            int varE = 0;
            int varC = 0;
            List<byte[]> varG = Lists.newArrayList();
            int varF = 0;
            int varD = 0;
            List<byte[]> varH = Lists.newArrayList();
    
            for (int var3 = 0; var3 < 18; ++var3) {
                Object var4 = NibbleArray.getConstructor(byte[].class).newInstance((Object) new byte[2048]);
                Object var5 = NibbleArray.getConstructor(byte[].class).newInstance((Object) new byte[2048]);
    
                Set<LightLevelInfo> lightLevelInfoSet = new HashSet<>();
                for (Map.Entry<BlockLocation, PBlockData> entry : dataMap.entrySet()) {
                    BlockLocation location = entry.getKey();
                    int level = entry.getValue().getBlockLightLevel();
    
                    if(level == -1) continue;
                    if(world != location.getWorld()) continue;
    
                    if(location.getY() >> 4 != -1 + var3) continue;
    
                    int x = location.getX() - (chunkX << 4);
                    int z = location.getZ() - (chunkZ << 4);
                    int y = location.getY() - ((location.getY() >> 4) << 4);
                    lightLevelInfoSet.add(new LightLevelInfo(x, y, z, level));
                }
    
                boolean writeSky = false;
                boolean writeBlock = false;
                for(int x = 0; x < 16; x++) {
                    for (int y = 0; y < 16; y++) {
                        for (int z = 0; z < 16; z++) {
                            Block block = world.getBlockAt(x + (chunkX << 4), y + ((var3 - 1) << 4), z + (chunkZ << 4));
                            int BLevel = block.getLightFromBlocks();
                            int SLevel = block.getLightFromSky();
                            if (BLevel != 0) writeBlock = true;
                            if (SLevel != 0) writeSky = true;
    
                            for (LightLevelInfo lightLevelInfo : lightLevelInfoSet) {
                                if (lightLevelInfo.x == x && lightLevelInfo.y == y && lightLevelInfo.z == z) {
                                    BLevel = lightLevelInfo.level;
                                    writeBlock = true;
                                    break;
                                }
                            }
                            methodA.invoke(var4, x, y, z, SLevel);
                            methodA.invoke(var5, x, y, z, BLevel);
                        }
                    }
                }
                
                if (writeSky) {
                    if ((boolean) methodC.invoke(var4)) {
                        varE |= 1 << var3;
                    }
                    else {
                        varC |= 1 << var3;
                        varG.add(((byte[]) asBytes.invoke(var4)).clone());
                    }
                }
                if (writeBlock) {
                    if ((boolean) methodC.invoke(var5)) {
                        varF |= 1 << var3;
                    }
                    else {
                        varD |= 1 << var3;
                        varH.add(((byte[]) asBytes.invoke(var5)).clone());
                    }
                }
            }
            
            Object newPacket = PacketPlayOutLightUpdate.newInstance();
            a.set(newPacket, chunkX);
            b.set(newPacket, chunkZ);
            g.set(newPacket, varG);
            h.set(newPacket, varH);
            e.set(newPacket, varE);
            c.set(newPacket, varC);
            f.set(newPacket, varF);
            d.set(newPacket, varD);
            
            if(VERSION_1_16_R1){
                i.set(newPacket, i.getBoolean(packet));
            }
    
            parallelWorld.getEditedPacketForLightMap().put(chunkLocation, newPacket);
            packetHandler.doWrite(channelHandlerContext, newPacket, channelPromise);
        }catch (Exception e){e.printStackTrace();}
    }
}
