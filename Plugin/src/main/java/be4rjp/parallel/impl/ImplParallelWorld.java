package be4rjp.parallel.impl;

import be4rjp.parallel.ParallelChunk;
import be4rjp.parallel.ParallelUniverse;
import be4rjp.parallel.ParallelWorld;
import be4rjp.parallel.nms.INMSHandler;
import be4rjp.parallel.nms.NMSManager;
import be4rjp.parallel.util.BlockPosition3i;
import be4rjp.parallel.util.ChunkUtil;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ImplParallelWorld implements ParallelWorld {

    private final ParallelUniverse parallelUniverse;

    private final String worldName;
    
    public ImplParallelWorld(ParallelUniverse parallelUniverse, String worldName){
        this.parallelUniverse = parallelUniverse;
        this.worldName = worldName;
    }
    
    public String getWorldName() {return worldName;}
    
    
    
    private final Map<Long, ImplParallelChunk> chunkMap = new ConcurrentHashMap<>();
    
    @Override
    public String getName() {
        return worldName;
    }
    
    @Override
    public @NotNull ParallelUniverse getParallelUniverse() {
        return parallelUniverse;
    }

    @Override
    public void setType(int blockX, int blockY, int blockZ, Material material) {
        int chunkX = blockX >> 4;
        int chunkZ = blockZ >> 4;
        long coord = ChunkUtil.getCoordinateKey(chunkX, chunkZ);

        ParallelChunk parallelChunk = chunkMap.computeIfAbsent(coord, c -> new ImplParallelChunk(this, chunkX, chunkZ));
        parallelChunk.setType(blockX, blockY, blockZ, material);
    }

    @Override
    public @Nullable Material getType(int blockX, int blockY, int blockZ) {
        int chunkX = blockX >> 4;
        int chunkZ = blockZ >> 4;
        long coord = ChunkUtil.getCoordinateKey(chunkX, chunkZ);

        ParallelChunk parallelChunk = chunkMap.get(coord);
        if(parallelChunk == null) return null;

        return parallelChunk.getType(blockX, blockY, blockZ);
    }

    @Override
    public void setBlockData(int blockX, int blockY, int blockZ, BlockData blockData) {
        int chunkX = blockX >> 4;
        int chunkZ = blockZ >> 4;
        long coord = ChunkUtil.getCoordinateKey(chunkX, chunkZ);

        ParallelChunk parallelChunk = chunkMap.computeIfAbsent(coord, c -> new ImplParallelChunk(this, chunkX, chunkZ));
        parallelChunk.setBlockData(blockX, blockY, blockZ, blockData);
    }

    @Override
    public @Nullable BlockData getBlockData(int blockX, int blockY, int blockZ) {
        int chunkX = blockX >> 4;
        int chunkZ = blockZ >> 4;
        long coord = ChunkUtil.getCoordinateKey(chunkX, chunkZ);

        ParallelChunk parallelChunk = chunkMap.get(coord);
        if(parallelChunk == null) return null;

        return parallelChunk.getBlockData(blockX, blockY, blockZ);
    }
    
    @Override
    public @Nullable Object getNMSBlockData(int blockX, int blockY, int blockZ) {
        int chunkX = blockX >> 4;
        int chunkZ = blockZ >> 4;
        long coord = ChunkUtil.getCoordinateKey(chunkX, chunkZ);
    
        ParallelChunk parallelChunk = chunkMap.get(coord);
        if(parallelChunk == null) return null;
    
        return parallelChunk.getNMSBlockData(blockX, blockY, blockZ);
    }
    
    @Override
    public void removeBlockData(int blockX, int blockY, int blockZ) {
        int chunkX = blockX >> 4;
        int chunkZ = blockZ >> 4;
        long coord = ChunkUtil.getCoordinateKey(chunkX, chunkZ);
    
        ParallelChunk parallelChunk = chunkMap.get(coord);
        if(parallelChunk == null) return;
        
        parallelChunk.removeBlockData(blockX, blockY, blockZ);
    }
    
    @Override
    public void setBlockLightLevel(int blockX, int blockY, int blockZ, int level) {
        int chunkX = blockX >> 4;
        int chunkZ = blockZ >> 4;
        long coord = ChunkUtil.getCoordinateKey(chunkX, chunkZ);

        ParallelChunk parallelChunk = chunkMap.computeIfAbsent(coord, c -> new ImplParallelChunk(this, chunkX, chunkZ));
        parallelChunk.setBlockLightLevel(blockX, blockY, blockZ, level);
    }

    @Override
    public int getBlockLightLevel(int blockX, int blockY, int blockZ) {
        int chunkX = blockX >> 4;
        int chunkZ = blockZ >> 4;
        long coord = ChunkUtil.getCoordinateKey(chunkX, chunkZ);

        ParallelChunk parallelChunk = chunkMap.get(coord);
        if(parallelChunk == null) return 0;

        return parallelChunk.getBlockLightLevel(blockX, blockY, blockZ);
    }
    
    @Override
    public void removeBlockLight(int blockX, int blockY, int blockZ) {
        int chunkX = blockX >> 4;
        int chunkZ = blockZ >> 4;
        long coord = ChunkUtil.getCoordinateKey(chunkX, chunkZ);
    
        ParallelChunk parallelChunk = chunkMap.get(coord);
        if(parallelChunk == null) return;
        
        parallelChunk.removeBlockLight(blockX, blockY, blockZ);
    }
    
    @Override
    public void setSkyLightLevel(int blockX, int blockY, int blockZ, int level) {
        int chunkX = blockX >> 4;
        int chunkZ = blockZ >> 4;
        long coord = ChunkUtil.getCoordinateKey(chunkX, chunkZ);

        ParallelChunk parallelChunk = chunkMap.computeIfAbsent(coord, c -> new ImplParallelChunk(this, chunkX, chunkZ));
        parallelChunk.setSkyLightLevel(blockX, blockY, blockZ, level);
    }

    @Override
    public int getSkyLightLevel(int blockX, int blockY, int blockZ) {
        int chunkX = blockX >> 4;
        int chunkZ = blockZ >> 4;
        long coord = ChunkUtil.getCoordinateKey(chunkX, chunkZ);

        ParallelChunk parallelChunk = chunkMap.get(coord);
        if(parallelChunk == null) return 0;

        return parallelChunk.getSkyLightLevel(blockX, blockY, blockZ);
    }
    
    @Override
    public void removeSkyLight(int blockX, int blockY, int blockZ) {
        int chunkX = blockX >> 4;
        int chunkZ = blockZ >> 4;
        long coord = ChunkUtil.getCoordinateKey(chunkX, chunkZ);
    
        ParallelChunk parallelChunk = chunkMap.get(coord);
        if(parallelChunk == null) return;
        
        parallelChunk.removeSkyLight(blockX, blockY, blockZ);
    }
    
    @Override
    public boolean hasBlockData(int blockX, int blockY, int blockZ) {
        int chunkX = blockX >> 4;
        int chunkZ = blockZ >> 4;
        long coord = ChunkUtil.getCoordinateKey(chunkX, chunkZ);
    
        ParallelChunk parallelChunk = chunkMap.get(coord);
        if(parallelChunk == null) return false;
        
        return parallelChunk.hasBlockData(blockX, blockY, blockZ);
    }
    
    @Override
    public boolean hasBlockLight(int blockX, int blockY, int blockZ) {
        int chunkX = blockX >> 4;
        int chunkZ = blockZ >> 4;
        long coord = ChunkUtil.getCoordinateKey(chunkX, chunkZ);
    
        ParallelChunk parallelChunk = chunkMap.get(coord);
        if(parallelChunk == null) return false;
    
        return parallelChunk.hasBlockLight(blockX, blockY, blockZ);
    }
    
    @Override
    public boolean hasSkyLight(int blockX, int blockY, int blockZ) {
        int chunkX = blockX >> 4;
        int chunkZ = blockZ >> 4;
        long coord = ChunkUtil.getCoordinateKey(chunkX, chunkZ);
    
        ParallelChunk parallelChunk = chunkMap.get(coord);
        if(parallelChunk == null) return false;
    
        return parallelChunk.hasSkyLight(blockX, blockY, blockZ);
    }
    
    @Override
    public ImplParallelChunk getChunk(int chunkX, int chunkZ){return chunkMap.get(ChunkUtil.getCoordinateKey(chunkX, chunkZ));}
    
    @Override
    public void sendBlockUpdate(int blockX, int blockY, int blockZ) {
        INMSHandler nmsHandler = NMSManager.getNmsHandler();
        Object packet = nmsHandler.createBlockChangePacket(this, blockX, blockY, blockZ);
        if(packet != null){
            parallelUniverse.getResidents().forEach(player -> {
                if(worldName.equals(player.getBukkitPlayer().getWorld().getName())) nmsHandler.sendPacket(player.getBukkitPlayer(), packet);
            });
        }
    }
    
    @Override
    public void sendMultiBlockUpdate(Set<BlockPosition3i> blocks) {
        INMSHandler nmsHandler = NMSManager.getNmsHandler();
        Set<Object> packets = nmsHandler.createMultiBlockChangePacket(this, blocks);
        for(Object packet : packets){
            parallelUniverse.getResidents().forEach(player -> {
                if(worldName.equals(player.getBukkitPlayer().getWorld().getName())) nmsHandler.sendPacket(player.getBukkitPlayer(), packet);
            });
        }
    }
    
}
