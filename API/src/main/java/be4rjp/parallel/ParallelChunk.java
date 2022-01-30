package be4rjp.parallel;

import be4rjp.parallel.util.SectionLevelArray;
import be4rjp.parallel.util.SectionTypeArray;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ParallelChunk {

    /**
     * Get the ParallelWorld in which this chunk is stored.
     * @return ParallelWorld
     */
    @NotNull ParallelWorld getWorld();

    /**
     * Get coordinate(Chunk X)
     * @return chunkX
     */
    int getChunkX();

    /**
     * Get coordinate(Chunk Z)
     * @return chunkZ
     */
    int getChunkZ();

    /**
     * Set the material for a block in this chunk.
     * @param blockX Block coordinate X
     * @param blockY Block coordinate Y
     * @param blockZ Block coordinate Z
     * @param material Bukkit material
     */
    void setType(int blockX, int blockY, int blockZ, Material material);

    /**
     * Get the material for a block in this chunk.
     * @param blockX Block coordinate X
     * @param blockY Block coordinate Y
     * @param blockZ Block coordinate Z
     * @return Material for the block
     */
    @Nullable Material getType(int blockX, int blockY, int blockZ);

    /**
     * Set the data for a block in this chunk.
     * @param blockX Block coordinate X
     * @param blockY Block coordinate Y
     * @param blockZ Block coordinate Z
     * @param blockData Bukkit BlockData
     */
    void setBlockData(int blockX, int blockY, int blockZ, BlockData blockData);

    /**
     * Get the data for a block in this chunk.
     * @param blockX Block coordinate X
     * @param blockY Block coordinate Y
     * @param blockZ Block coordinate Z
     * @return BlockData for the block
     */
    @Nullable BlockData getBlockData(int blockX, int blockY, int blockZ);
    
    /**
     * Get the nms IBlockData for a block in this chunk.
     * @param blockX Block coordinate X
     * @param blockY Block coordinate Y
     * @param blockZ Block coordinate Z
     * @return BlockData for the block
     */
    @Nullable Object getNMSBlockData(int blockX, int blockY, int blockZ);
    
    /**
     * Remove the data for a block in this chunk.
     * @param blockX Block coordinate X
     * @param blockY Block coordinate Y
     * @param blockZ Block coordinate Z
     */
    void removeBlockData(int blockX, int blockY, int blockZ);

    /**
     * Set block light level for a block in this chunk.
     * @param blockX Block coordinate X
     * @param blockY Block coordinate Y
     * @param blockZ Block coordinate Z
     * @param level Light level
     */
    void setBlockLightLevel(int blockX, int blockY, int blockZ, int level);

    /**
     * Set block light level for a block in this chunk.
     * @param blockX Block coordinate X
     * @param blockY Block coordinate Y
     * @param blockZ Block coordinate Z
     * @return Block light
     */
    int getBlockLightLevel(int blockX, int blockY, int blockZ);
    
    /**
     * Remove block light level for a block in this chunk.
     * @param blockX Block coordinate X
     * @param blockY Block coordinate Y
     * @param blockZ Block coordinate Z
     */
    void removeBlockLight(int blockX, int blockY, int blockZ);

    /**
     * Set sky light level for a block in this chunk.
     * @param blockX Block coordinate X
     * @param blockY Block coordinate Y
     * @param blockZ Block coordinate Z
     * @param level Light level
     */
    void setSkyLightLevel(int blockX, int blockY, int blockZ, int level);

    /**
     * Set sky light level for a block in this chunk.
     * @param blockX Block coordinate X
     * @param blockY Block coordinate Y
     * @param blockZ Block coordinate Z
     * @return Sky light
     */
    int getSkyLightLevel(int blockX, int blockY, int blockZ);
    
    /**
     * Remove sky light level for a block in this chunk.
     * @param blockX Block coordinate X
     * @param blockY Block coordinate Y
     * @param blockZ Block coordinate Z
     */
    void removeSkyLight(int blockX, int blockY, int blockZ);

    /**
     * Get block light nibble array.
     * @param sectionY Chunk section index.
     * @return SectionLevelArray
     */
    @Nullable SectionLevelArray getBlockLightSectionLevelArray(int sectionY);

    /**
     * Get sky light nibble array.
     * @param sectionY Chunk section index.
     * @return SectionLevelArray
     */
    @Nullable SectionLevelArray getSkyLightSectionLevelArray(int sectionY);

    /**
     * Get all block data.
     * @param sectionY Chunk section index.
     * @return SectionTypeArray
     */
    @Nullable SectionTypeArray getSectionTypeArray(int sectionY);
    
    /**
     * Gets whether the specified block is set with data.
     * @param blockX Block coordinate X
     * @param blockY Block coordinate Y
     * @param blockZ Block coordinate Z
     * @return Whether the specified block is set with data.
     */
    boolean hasBlockData(int blockX, int blockY, int blockZ);
    
    /**
     * Gets whether the specified block is set with block light.
     * @param blockX Block coordinate X
     * @param blockY Block coordinate Y
     * @param blockZ Block coordinate Z
     * @return Whether the specified block is set with block light.
     */
    boolean hasBlockLight(int blockX, int blockY, int blockZ);
    
    /**
     * Gets whether the specified block is set with sky light.
     * @param blockX Block coordinate X
     * @param blockY Block coordinate Y
     * @param blockZ Block coordinate Z
     * @return Whether the specified block is set with sky light.
     */
    boolean hasSkyLight(int blockX, int blockY, int blockZ);
    
    /**
     * Sends the data of all blocks set in this chunk to the players.
     * @param player Player to sen
     */
    void sendUpdate(Player player);

    @Nullable Object getCachedMapChunkPacket();
    
    @Nullable Object getCachedLightUpdatePacket();
    
    void setMapChunkPacketCache(Object packet);
    
    void setLightUpdatePacketCache(Object packet);
}
