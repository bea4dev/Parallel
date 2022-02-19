package be4rjp.parallel.structure;

import be4rjp.parallel.util.BlockPosition3i;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class StructureData {
    
    protected static Map<String, StructureData> structureDataMap;
    
    static {
        initialize();
    }
    
    public static void initialize(){
        structureDataMap = new HashMap<>();
    }
    
    public static StructureData getStructureData(String name){
        return structureDataMap.get(name);
    }
    
    public static Map<String, StructureData> getStructureDataMap() {return structureDataMap;}
    
    
    
    protected final String name;
    protected final Map<BlockPosition3i, BlockData> blockDataMap = new HashMap<>();
    protected final Map<BlockPosition3i, Integer> blockLightLevelMap = new HashMap<>();
    
    public StructureData(String name){
        this.name = name;
        structureDataMap.put(name, this);
    }
    
    public Map<BlockPosition3i, BlockData> getBlockDataMap() {return blockDataMap;}
    
    public Map<BlockPosition3i, Integer> getBlockLightLevelMap() {return blockLightLevelMap;}
    
    /**
     * ブロックの状態を記録
     * @param baseLocation
     * @param blocks
     */
    public void setBlockData(Location baseLocation, List<Block> blocks){
        for(Block block : blocks) {
            BlockPosition3i relative = new BlockPosition3i(block.getX() - baseLocation.getBlockX(), block.getY() - baseLocation.getBlockY(), block.getZ() - baseLocation.getBlockZ());
            BlockData blockData = block.getBlockData();
            int blockLightLevel = block.getLightFromBlocks();
            
            this.blockDataMap.put(relative, blockData);
            this.blockLightLevelMap.put(relative, blockLightLevel);
        }
    }
}
