package be4rjp.parallel.impl;

import be4rjp.parallel.ParallelChunk;
import be4rjp.parallel.ParallelWorld;
import be4rjp.parallel.nms.INMSHandler;
import be4rjp.parallel.nms.NMSManager;
import be4rjp.parallel.util.SectionLevelArray;
import be4rjp.parallel.util.SectionTypeArray;
import be4rjp.parallel.util.TaskHandler;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ImplParallelChunk implements ParallelChunk {

    private final ParallelWorld parallelWorld;
    
    private final int chunkX;
    
    private final int chunkZ;

    private final SectionLevelArray[] blockLightArrays;

    private final SectionLevelArray[] skyLightArrays;

    private final SectionTypeArray[] sectionTypeArrays;
    
    
    private Object mapChunkPacketCache;
    
    private Object lightUpdatePacketCache;
    
    
    public ImplParallelChunk(ParallelWorld parallelWorld, int chunkX, int chunkZ){
        this.parallelWorld = parallelWorld;
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;

        this.blockLightArrays = new SectionLevelArray[NMSManager.isHigher_v1_18_R1() ? 24 : 16];
        this.skyLightArrays = new SectionLevelArray[NMSManager.isHigher_v1_18_R1() ? 24 : 16];
        this.sectionTypeArrays = new SectionTypeArray[NMSManager.isHigher_v1_18_R1() ? 24 : 16];
    }

    @Override
    public @NotNull ParallelWorld getWorld() {
        return parallelWorld;
    }

    @Override
    public int getChunkX() {return chunkX;}

    @Override
    public int getChunkZ() {return chunkZ;}
    
    
    public SectionLevelArray createBlockLightSectionLevelArrayIfAbsent(int sectionY) {
        int sectionIndex = getSectionIndex(sectionY << 4);
        SectionLevelArray sectionLevelArray = blockLightArrays[sectionIndex];
        if(sectionLevelArray == null) {
            sectionLevelArray = new SectionLevelArray();
            blockLightArrays[sectionIndex] = sectionLevelArray;
        }
        
        return sectionLevelArray;
    }
    
    public SectionLevelArray createSkyLightSectionLevelArrayIfAbsent(int sectionY) {
        int sectionIndex = getSectionIndex(sectionY << 4);
        SectionLevelArray sectionLevelArray = skyLightArrays[sectionIndex];
        if(sectionLevelArray == null) {
            sectionLevelArray = new SectionLevelArray();
            skyLightArrays[sectionIndex] = sectionLevelArray;
        }
    
        return sectionLevelArray;
    }
    
    public SectionTypeArray createSectionTypeArrayIfAbsent(int sectionY) {
        int sectionIndex = getSectionIndex(sectionY << 4);
        SectionTypeArray sectionTypeArray = sectionTypeArrays[sectionIndex];
        if(sectionTypeArray == null) {
            sectionTypeArray = new SectionTypeArray();
            sectionTypeArrays[sectionIndex] = sectionTypeArray;
        }
        
        return sectionTypeArray;
    }


    private int getSectionIndex(int blockY){
        if(NMSManager.isHigher_v1_18_R1()){
            return (blockY + 64) >> 4;
        }else{
            return blockY >> 4;
        }
    }

    @Override
    public void setType(int blockX, int blockY, int blockZ, Material material) {
        int sectionIndex = getSectionIndex(blockY);

        SectionTypeArray sectionTypeArray = sectionTypeArrays[sectionIndex];
        if(sectionTypeArray == null){
            sectionTypeArray = new SectionTypeArray();
            sectionTypeArrays[sectionIndex] = sectionTypeArray;
        }

        Object iBlockData = NMSManager.getNmsHandler().getIBlockData(material.createBlockData());
        sectionTypeArray.setType(blockX & 0xF, blockY & 0xF, blockZ & 0xF, iBlockData);
        mapChunkPacketCache = null;
    }

    @Override
    public @Nullable Material getType(int blockX, int blockY, int blockZ) {
        int sectionIndex = getSectionIndex(blockY);

        SectionTypeArray sectionTypeArray = sectionTypeArrays[sectionIndex];
        if(sectionTypeArray == null) return null;

        Object iBlockData = sectionTypeArray.getType(blockX & 0xF, blockY & 0xF, blockZ & 0xF);
        if(iBlockData == null) return null;

        return NMSManager.getNmsHandler().getBukkitBlockData(iBlockData).getMaterial();
    }

    @Override
    public void setBlockData(int blockX, int blockY, int blockZ, BlockData blockData) {
        int sectionIndex = getSectionIndex(blockY);

        SectionTypeArray sectionTypeArray = sectionTypeArrays[sectionIndex];
        if(sectionTypeArray == null){
            sectionTypeArray = new SectionTypeArray();
            sectionTypeArrays[sectionIndex] = sectionTypeArray;
        }

        Object iBlockData = NMSManager.getNmsHandler().getIBlockData(blockData);
        sectionTypeArray.setType(blockX & 0xF, blockY & 0xF, blockZ & 0xF, iBlockData);
        mapChunkPacketCache = null;
    }

    @Override
    public @Nullable BlockData getBlockData(int blockX, int blockY, int blockZ) {
        int sectionIndex = getSectionIndex(blockY);

        SectionTypeArray sectionTypeArray = sectionTypeArrays[sectionIndex];
        if(sectionTypeArray == null) return null;

        Object iBlockData = sectionTypeArray.getType(blockX & 0xF, blockY & 0xF, blockZ & 0xF);
        if(iBlockData == null) return null;

        return NMSManager.getNmsHandler().getBukkitBlockData(iBlockData);
    }
    
    @Override
    public @Nullable Object getNMSBlockData(int blockX, int blockY, int blockZ) {
        int sectionIndex = getSectionIndex(blockY);
    
        SectionTypeArray sectionTypeArray = sectionTypeArrays[sectionIndex];
        if(sectionTypeArray == null) return null;
    
        return sectionTypeArray.getType(blockX & 0xF, blockY & 0xF, blockZ & 0xF);
    }
    
    @Override
    public void removeBlockData(int blockX, int blockY, int blockZ) {
        int sectionIndex = getSectionIndex(blockY);
    
        SectionTypeArray sectionTypeArray = sectionTypeArrays[sectionIndex];
        if(sectionTypeArray == null) return;
        
        sectionTypeArray.remove(blockX, blockY, blockZ);
        mapChunkPacketCache = null;
    }
    
    @Override
    public void setBlockLightLevel(int blockX, int blockY, int blockZ, int level) {
        int sectionIndex = getSectionIndex(blockY);

        SectionLevelArray sectionLevelArray = blockLightArrays[sectionIndex];
        if(sectionLevelArray == null){
            sectionLevelArray = new SectionLevelArray();
            blockLightArrays[sectionIndex] = sectionLevelArray;
        }

        sectionLevelArray.setLevel(blockX & 0xF, blockY & 0xF, blockZ & 0xF, (byte) level);
        lightUpdatePacketCache = null;
    }

    @Override
    public int getBlockLightLevel(int blockX, int blockY, int blockZ) {
        int sectionIndex = getSectionIndex(blockY);

        SectionLevelArray sectionLevelArray = blockLightArrays[sectionIndex];
        if(sectionLevelArray == null) return 0;

        return sectionLevelArray.getLevel(blockX & 0xF, blockY & 0xF, blockZ & 0xF);
    }
    
    @Override
    public void removeBlockLight(int blockX, int blockY, int blockZ) {
        int sectionIndex = getSectionIndex(blockY);
    
        SectionLevelArray sectionLevelArray = blockLightArrays[sectionIndex];
        if(sectionLevelArray == null) return;
        
        sectionLevelArray.remove(blockX, blockY, blockZ);
        lightUpdatePacketCache = null;
    }
    
    @Override
    public void setSkyLightLevel(int blockX, int blockY, int blockZ, int level) {
        int sectionIndex = getSectionIndex(blockY);
    
        SectionLevelArray sectionLevelArray = skyLightArrays[sectionIndex];
        if(sectionLevelArray == null){
            sectionLevelArray = new SectionLevelArray();
            skyLightArrays[sectionIndex] = sectionLevelArray;
        }
    
        sectionLevelArray.setLevel(blockX & 0xF, blockY & 0xF, blockZ & 0xF, (byte) level);
        lightUpdatePacketCache = null;
    }

    @Override
    public int getSkyLightLevel(int blockX, int blockY, int blockZ) {
        int sectionIndex = getSectionIndex(blockY);
    
        SectionLevelArray sectionLevelArray = skyLightArrays[sectionIndex];
        if(sectionLevelArray == null) return 0;
    
        return sectionLevelArray.getLevel(blockX & 0xF, blockY & 0xF, blockZ & 0xF);
    }
    
    @Override
    public void removeSkyLight(int blockX, int blockY, int blockZ) {
        int sectionIndex = getSectionIndex(blockY);
    
        SectionLevelArray sectionLevelArray = skyLightArrays[sectionIndex];
        if(sectionLevelArray == null) return;
        
        sectionLevelArray.remove(blockX, blockY, blockZ);
        lightUpdatePacketCache = null;
    }
    
    @Override
    public @Nullable SectionLevelArray getBlockLightSectionLevelArray(int sectionY) {
        int sectionIndex = getSectionIndex(sectionY << 4);
        return blockLightArrays[sectionIndex];
    }

    @Override
    public @Nullable SectionLevelArray getSkyLightSectionLevelArray(int sectionY) {
        int sectionIndex = getSectionIndex(sectionY << 4);
        return skyLightArrays[sectionIndex];
    }

    @Override
    public @Nullable SectionTypeArray getSectionTypeArray(int sectionY) {
        int sectionIndex = getSectionIndex(sectionY << 4);
        return sectionTypeArrays[sectionIndex];
    }
    
    @Override
    public boolean hasBlockData(int blockX, int blockY, int blockZ) {
        int sectionIndex = getSectionIndex(blockY);
    
        SectionTypeArray sectionTypeArray = sectionTypeArrays[sectionIndex];
        if(sectionTypeArray == null) return false;
    
        return sectionTypeArray.contains(blockX & 0xF, blockY & 0xF, blockZ & 0xF);
    }
    
    @Override
    public boolean hasBlockLight(int blockX, int blockY, int blockZ) {
        int sectionIndex = getSectionIndex(blockY);
    
        SectionLevelArray sectionLevelArray = blockLightArrays[sectionIndex];
        if(sectionLevelArray == null) return false;
    
        return sectionLevelArray.contains(blockX & 0xF, blockY & 0xF, blockZ & 0xF);
    }
    
    @Override
    public boolean hasSkyLight(int blockX, int blockY, int blockZ) {
        int sectionIndex = getSectionIndex(blockY);
    
        SectionLevelArray sectionLevelArray = skyLightArrays[sectionIndex];
        if(sectionLevelArray == null) return false;
    
        return sectionLevelArray.contains(blockX & 0xF, blockY & 0xF, blockZ & 0xF);
    }
    
    @Override
    public void sendUpdate(Player player) {
        INMSHandler nmsHandler = NMSManager.getNmsHandler();
    
        nmsHandler.sendChunkMultiBlockChangeUpdatePacket(player, this);
    
        TaskHandler.runSync(() -> {
            World world = Bukkit.getWorld(getWorld().getName());
            if(world == null) return;
            
            TaskHandler.runWorldSync(world, () -> {
                Object lightUpdatePacket = nmsHandler.createLightUpdatePacketAtPrimaryThread(this);
                if(lightUpdatePacket != null) nmsHandler.sendPacket(player, lightUpdatePacket);
            });
        });
    }

    public void sendClearPacket(Player player){
        INMSHandler nmsHandler = NMSManager.getNmsHandler();

        TaskHandler.runSync(() -> {
            World world = Bukkit.getWorld(getWorld().getName());
            if(world == null) return;

            TaskHandler.runWorldSync(world, () -> {
                nmsHandler.sendClearChunkMultiBlockChangePacketAtPrimaryThread(player, this);
            });
        });
    }
    
    @Override
    public @Nullable Object getCachedMapChunkPacket() {
        return mapChunkPacketCache;
    }
    
    @Override
    public @Nullable Object getCachedLightUpdatePacket() {
        return lightUpdatePacketCache;
    }
    
    @Override
    public void setMapChunkPacketCache(Object packet) {
        this.mapChunkPacketCache = packet;
    }
    
    @Override
    public void setLightUpdatePacketCache(Object packet) {
        this.lightUpdatePacketCache = packet;
    }
    
}
