package be4rjp.parallel.v1_15_R1;

import be4rjp.parallel.ParallelChunk;
import be4rjp.parallel.ParallelWorld;
import be4rjp.parallel.nms.INMSHandler;
import be4rjp.parallel.util.BlockPosition3i;
import be4rjp.parallel.util.ChunkPosition;
import be4rjp.parallel.util.SectionLevelArray;
import be4rjp.parallel.util.SectionTypeArray;
import io.netty.channel.Channel;
import net.minecraft.server.v1_15_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.v1_15_R1.CraftChunk;
import org.bukkit.craftbukkit.v1_15_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_15_R1.block.data.CraftBlockData;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer;
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
        Map<ChunkPosition, Set<BlockPosition3i>> chunkMap = new HashMap<>();
        
        for(BlockPosition3i bp : blocks){
            chunkMap.computeIfAbsent(new ChunkPosition(bp.getX(), bp.getZ()), cp -> new HashSet<>()).add(bp);
        }
    
        Set<Object> packets = new HashSet<>();
        
        for(Map.Entry<ChunkPosition, Set<BlockPosition3i>> entry : chunkMap.entrySet()){
            ChunkPosition chunkPosition = entry.getKey();
            Set<BlockPosition3i> bps = entry.getValue();
            
            ParallelChunk parallelChunk = parallelWorld.getChunk(chunkPosition.x, chunkPosition.z);
            if(parallelChunk == null) continue;
    
            PacketPlayOutMultiBlockChange packet = new PacketPlayOutMultiBlockChange();
            List<PacketPlayOutMultiBlockChange.MultiBlockChangeInfo> infoList = new ArrayList<>();
            
            for(BlockPosition3i bp : bps){
                BlockData blockData = parallelChunk.getBlockData(bp.getX(), bp.getY(), bp.getZ());
                if(blockData == null) continue;
    
                short loc = (short) ((bp.getX() & 0xF) << 12 | (bp.getZ() & 0xF) << 8 | bp.getY());
                infoList.add(packet.new MultiBlockChangeInfo(loc, ((CraftBlockData) blockData).getState()));
            }
            
            try {
                MultiBlockChangePacketHandler.a.set(packet, new ChunkCoordIntPair(chunkPosition.x, chunkPosition.z));
                MultiBlockChangePacketHandler.b.set(packet, infoList.toArray());
                packets.add(packet);
            }catch (Exception e){e.printStackTrace();}
        }
        
        return packets;
    }
    
    @Override
    public void sendChunkMultiBlockChangeUpdatePacket(Player player, ParallelChunk parallelChunk) {
        PacketPlayOutMultiBlockChange packet = new PacketPlayOutMultiBlockChange();
        List<PacketPlayOutMultiBlockChange.MultiBlockChangeInfo> infoList = new ArrayList<>();
    
        boolean has = false;
        for(int sectionIndex = 0; sectionIndex < 16; sectionIndex++){
            SectionTypeArray sectionTypeArray = parallelChunk.getSectionTypeArray(sectionIndex);
            if(sectionTypeArray == null) continue;
    
            int finalSectionIndex = sectionIndex;
            boolean notEmpty = sectionTypeArray.threadsafeIteration((x, y, z, iBlockData) -> {
                short loc = (short) (x << 12 | z << 8 | (y + (finalSectionIndex << 4)));
                infoList.add(packet.new MultiBlockChangeInfo(loc, (IBlockData) iBlockData));
            });
            
            if(notEmpty) has = true;
        }
        
        if(!has) return;
        
        if(infoList.size() == 0) return;
    
        PacketPlayOutMultiBlockChange.MultiBlockChangeInfo[] array = infoList.toArray(new PacketPlayOutMultiBlockChange.MultiBlockChangeInfo[infoList.size()]);

        try {
            MultiBlockChangePacketHandler.a.set(packet, new ChunkCoordIntPair(parallelChunk.getChunkX(), parallelChunk.getChunkZ()));
            MultiBlockChangePacketHandler.b.set(packet, array);
            sendPacket(player, packet);
        }catch (Exception e){e.printStackTrace();}
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
                ((CraftWorld) world).getHandle().getChunkProvider().getLightEngine());
    }

    @Override
    public void sendClearChunkMultiBlockChangePacketAtPrimaryThread(Player player, ParallelChunk parallelChunk) {
        if(!Bukkit.isPrimaryThread()) throw new IllegalStateException("DO NOT CALL FROM ASYNC THREAD!");

        World world = Bukkit.getWorld(parallelChunk.getWorld().getName());
        if(world == null) return;

        if(player.getWorld() != world) return;
        
        List<Short> coordList = new ArrayList<>();
    
        boolean has = false;
        for(int sectionIndex = 0; sectionIndex < 16; sectionIndex++){
            SectionTypeArray sectionTypeArray = parallelChunk.getSectionTypeArray(sectionIndex);
            if(sectionTypeArray == null) continue;
        
            int finalSectionIndex = sectionIndex;
            boolean notEmpty = sectionTypeArray.threadsafeIteration((x, y, z, iBlockData) -> {
                short loc = (short) (x << 12 | z << 8 | (y + (finalSectionIndex << 4)));
                coordList.add(loc);
            });
        
            if(notEmpty) has = true;
        }
    
        if(!has) return;
    
        if(coordList.size() == 0) return;
        
        Chunk chunk = world.getChunkAt(parallelChunk.getChunkX(), parallelChunk.getChunkZ());
        net.minecraft.server.v1_15_R1.Chunk nmsChunk = ((CraftChunk) chunk).getHandle();
        
        short[] array = new short[coordList.size()];
        for(int i = 0; i < coordList.size(); i++){
            array[i] = coordList.get(i);
        }
        
        PacketPlayOutMultiBlockChange packet = new PacketPlayOutMultiBlockChange(coordList.size(), array, nmsChunk);
        sendPacket(player, packet);
    }
}
