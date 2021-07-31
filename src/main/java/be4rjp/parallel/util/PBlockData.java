package be4rjp.parallel.util;

import org.bukkit.block.data.BlockData;

public class PBlockData {
    
    private BlockData blockData = null;
    
    private int blockLightLevel = -1;
    
    public PBlockData(BlockData blockData, int blockLightLevel){
        this.blockData = blockData;
        this.blockLightLevel = blockLightLevel;
    }
    
    public PBlockData(BlockData blockData){
        this.blockData = blockData;
    }
    
    public PBlockData(int blockLightLevel){
        this.blockLightLevel = blockLightLevel;
    }
    
    public PBlockData(){}
    
    public BlockData getBlockData() {return blockData;}
    
    public void setBlockData(BlockData blockData) {this.blockData = blockData;}
    
    public int getBlockLightLevel() {return blockLightLevel;}
    
    public void setBlockLightLevel(int lightLevel) {this.blockLightLevel = lightLevel;}
}
