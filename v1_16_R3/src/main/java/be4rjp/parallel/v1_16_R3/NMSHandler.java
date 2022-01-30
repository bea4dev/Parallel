package be4rjp.parallel.v1_16_R3;

import be4rjp.parallel.ParallelChunk;
import be4rjp.parallel.ParallelWorld;
import be4rjp.parallel.nms.INMSHandler;
import be4rjp.parallel.util.BlockPosition3i;
import be4rjp.parallel.util.SectionLevelArray;
import be4rjp.parallel.util.SectionTypeArray;
import io.netty.channel.Channel;
import net.minecraft.server.v1_16_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.v1_16_R3.CraftChunk;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R3.block.data.CraftBlockData;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class NMSHandler implements INMSHandler {

    @Override
    public Channel getChannel(Player player) {
        return ((CraftPlayer) player).getHandle().playerConnection.networkManager.channel;
    }
    
    @Override
    public void sendPacket(Player player, Object packet) {
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket((Packet<?>) packet);
    }
    
    @Override
    public Object getIBlockDataByCombinedId(int id) {return Block.getByCombinedId(id);}

    @Override
    public int getCombinedIdByIBlockData(Object iBlockData) {return Block.getCombinedId((IBlockData) iBlockData);}

    @Override
    public Object getIBlockData(BlockData blockData) {return ((CraftBlockData) blockData).getState();}

    @Override
    public BlockData getBukkitBlockData(Object iBlockData) {return CraftBlockData.fromData((IBlockData) iBlockData);}

    @Override
    public Object[] createIBlockDataArray(int length) {return new IBlockData[length];}

    @Override
    public boolean isMapChunkPacket(Object packet) {return packet instanceof PacketPlayOutMapChunk;}

    @Override
    public boolean isMultiBlockChangePacket(Object packet) {return packet instanceof PacketPlayOutMultiBlockChange;}

    @Override
    public boolean isBlockChangePacket(Object packet) {return packet instanceof PacketPlayOutBlockChange;}

    @Override
    public boolean isLightUpdatePacket(Object packet) {return packet instanceof PacketPlayOutLightUpdate;}
    
    @Override
    public boolean isFlyPacket(Object packet) {return packet instanceof PacketPlayInFlying;}
    
    @Override
    public @Nullable Object createBlockChangePacket(ParallelWorld parallelWorld, int blockX, int blockY, int blockZ) {
        PacketPlayOutBlockChange packet = new PacketPlayOutBlockChange();
        BlockData blockData = parallelWorld.getBlockData(blockX, blockY, blockZ);
        if(blockData == null) return null;
        
        try {
            BlockChangePacketHandler.a.set(packet, new BlockPosition(blockX, blockY, blockZ));
            packet.block = ((CraftBlockData) blockData).getState();
            return packet;
            
        }catch (Exception e){e.printStackTrace();}
        
        return null;
    }
    
    @Override
    public Set<Object> createMultiBlockChangePacket(ParallelWorld parallelWorld, Set<BlockPosition3i> blocks) {
        Map<BlockPosition3i, Set<BlockPosition3i>> chunkMap = new HashMap<>();
        
        for(BlockPosition3i bp : blocks){
            chunkMap.computeIfAbsent(new BlockPosition3i(bp.getX() >> 4, bp.getY() >> 4, bp.getZ() >> 4), cp -> new HashSet<>()).add(bp);
        }
    
        Set<Object> packets = new HashSet<>();
        
        for(Map.Entry<BlockPosition3i, Set<BlockPosition3i>> entry : chunkMap.entrySet()){
            BlockPosition3i sectionPosition = entry.getKey();
            Set<BlockPosition3i> bps = entry.getValue();
            
            ParallelChunk parallelChunk = parallelWorld.getChunk(sectionPosition.getX(), sectionPosition.getZ());
            if(parallelChunk == null) continue;
            
            SectionTypeArray sectionTypeArray = parallelChunk.getSectionTypeArray(sectionPosition.getY());
            if(sectionTypeArray == null) continue;
            
            List<Short> coordList = new ArrayList<>();
            List<IBlockData> dataList = new ArrayList<>();
            
            for(BlockPosition3i bp : bps){
                IBlockData iBlockData = (IBlockData) sectionTypeArray.getType(bp.getX() & 0xF, bp.getY() & 0xF, bp.getZ() & 0xF);
                if(iBlockData == null) continue;
                
                coordList.add((short) ((bp.getX() & 0xF) << 8 | (bp.getZ() & 0xF) << 4 | bp.getY() & 0xF));
                dataList.add(iBlockData);
            }
    
            short[] coordArray = new short[coordList.size()];
            IBlockData[] dataArray = new IBlockData[dataList.size()];
            for(int i = 0; i < coordList.size(); i++){
                coordArray[i] = coordList.get(i);
                dataArray[i] = dataList.get(i);
            }
            
            try {
                PacketPlayOutMultiBlockChange packet = new PacketPlayOutMultiBlockChange();
                MultiBlockChangePacketHandler.a.set(packet, SectionPosition.a(sectionPosition.getX(), sectionPosition.getY(), sectionPosition.getZ()));
                MultiBlockChangePacketHandler.b.set(packet, coordArray);
                MultiBlockChangePacketHandler.c.set(packet, dataArray);
                MultiBlockChangePacketHandler.d.setBoolean(packet, true);
                
                packets.add(packet);
            }catch (Exception e){e.printStackTrace();}
        }
        
        return packets;
    }
    
    @Override
    public void sendChunkMultiBlockChangeUpdatePacket(Player player, ParallelChunk parallelChunk) {
        
        for(int sectionIndex = 0; sectionIndex < 16; sectionIndex++){
            SectionTypeArray sectionTypeArray = parallelChunk.getSectionTypeArray(sectionIndex);
            if(sectionTypeArray == null) continue;
    
            List<Short> coordList = new ArrayList<>();
            List<IBlockData> dataList = new ArrayList<>();
            
            boolean notEmpty = sectionTypeArray.threadsafeIteration((x, y, z, iBlockData) -> {
                coordList.add((short) (x << 8 | z << 4 | y));
                dataList.add((IBlockData) iBlockData);
            });
            
            if(!notEmpty) continue;
    
            short[] coordArray = new short[coordList.size()];
            IBlockData[] dataArray = new IBlockData[dataList.size()];
            for(int i = 0; i < coordList.size(); i++){
                coordArray[i] = coordList.get(i);
                dataArray[i] = dataList.get(i);
            }
    
            try {
                PacketPlayOutMultiBlockChange packet = new PacketPlayOutMultiBlockChange();
                MultiBlockChangePacketHandler.a.set(packet, SectionPosition.a(parallelChunk.getChunkX(), sectionIndex, parallelChunk.getChunkZ()));
                MultiBlockChangePacketHandler.b.set(packet, coordArray);
                MultiBlockChangePacketHandler.c.set(packet, dataArray);
                MultiBlockChangePacketHandler.d.setBoolean(packet, true);
        
                sendPacket(player, packet);
            }catch (Exception e){e.printStackTrace();}
        }
    }
    
    @Override
    public @Nullable Object createLightUpdatePacketAtPrimaryThread(ParallelChunk parallelChunk) {
        if(!Bukkit.isPrimaryThread()) throw new IllegalStateException("DO NOT CALL FROM ASYNC THREAD!");
    
        World world = Bukkit.getWorld(parallelChunk.getWorld().getName());
        if(world == null) return null;
    
        boolean has = false;
        for(int sectionIndex = 0; sectionIndex < 16; sectionIndex++){
            SectionLevelArray blockLevelArray = parallelChunk.getBlockLightSectionLevelArray(sectionIndex);
            SectionLevelArray skyLevelArray = parallelChunk.getSkyLightSectionLevelArray(sectionIndex);
        
            if(blockLevelArray != null){
                if(blockLevelArray.getSize() != 0) has = true;
            }
            if(skyLevelArray != null){
                if(skyLevelArray.getSize() != 0) has = true;
            }
        }
        if(!has) return null;
    
        return new PacketPlayOutLightUpdate(
                new ChunkCoordIntPair(parallelChunk.getChunkX(), parallelChunk.getChunkZ()),
                ((CraftWorld) world).getHandle().getChunkProvider().getLightEngine(), true);
    }

    @Override
    public void sendClearChunkMultiBlockChangePacketAtPrimaryThread(Player player, ParallelChunk parallelChunk) {
        if (!Bukkit.isPrimaryThread()) throw new IllegalStateException("DO NOT CALL FROM ASYNC THREAD!");
    
        World world = Bukkit.getWorld(parallelChunk.getWorld().getName());
        if (world == null) return;
    
        if (player.getWorld() != world) return;
    
        Chunk chunk = world.getChunkAt(parallelChunk.getChunkX(), parallelChunk.getChunkZ());
        net.minecraft.server.v1_16_R3.Chunk nmsChunk = ((CraftChunk) chunk).getHandle();
    
        for (int sectionIndex = 0; sectionIndex < 16; sectionIndex++) {
            SectionTypeArray sectionTypeArray = parallelChunk.getSectionTypeArray(sectionIndex);
            if (sectionTypeArray == null) continue;
    
            ChunkSection chunkSection = nmsChunk.getSections()[sectionIndex];
            if (chunkSection == null) continue;
            
            List<Short> coordList = new ArrayList<>();
            List<IBlockData> dataList = new ArrayList<>();
            boolean notEmpty = sectionTypeArray.threadsafeIteration((x, y, z, iBlockData) -> {
                coordList.add((short) (x << 8 | z << 4 | y));
                
                IBlockData chunkData = chunkSection.getType(x, y, z);
                if(chunkData == null) chunkData = ((CraftBlockData) org.bukkit.Material.AIR.createBlockData()).getState();
                dataList.add(chunkData);
            });
            if (!notEmpty) continue;
    
            short[] coordArray = new short[coordList.size()];
            IBlockData[] dataArray = new IBlockData[dataList.size()];
            for(int i = 0; i < coordList.size(); i++){
                coordArray[i] = coordList.get(i);
                dataArray[i] = dataList.get(i);
            }
            
            try {
                PacketPlayOutMultiBlockChange packet = new PacketPlayOutMultiBlockChange();
                MultiBlockChangePacketHandler.a.set(packet, SectionPosition.a(parallelChunk.getChunkX(), sectionIndex, parallelChunk.getChunkZ()));
                MultiBlockChangePacketHandler.b.set(packet, coordArray);
                MultiBlockChangePacketHandler.c.set(packet, dataArray);
                MultiBlockChangePacketHandler.d.setBoolean(packet, true);
        
                sendPacket(player, packet);
            }catch (Exception e){e.printStackTrace();}
        }
    }
}
