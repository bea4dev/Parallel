package be4rjp.parallel;

import be4rjp.parallel.nms.NMSUtil;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;

import java.util.AbstractMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * プレイヤーごとに違うブロックを設置するための機能をまとめたクラス
 */
public class ParallelWorld {
    
    private static Map<String, ParallelWorld> worldMap;
    
    static {
        initialize();
    }
    
    public static void initialize(){
        worldMap = new ConcurrentHashMap<>();
    }
    
    public static ParallelWorld getParallelWorld(Player player){
        return getParallelWorld(player.getUniqueId().toString());
    }
    
    public static ParallelWorld getParallelWorld(String uuid){
        if(worldMap.containsKey(uuid)) return worldMap.get(uuid);
        return new ParallelWorld(uuid);
    }
    
    public static void removeParallelWorld(String uuid){
        worldMap.remove(uuid);
    }
    
    
    
    
    private final String uuid;
    private final Map<Location, BlockData> blockMap;
    
    public ParallelWorld(String uuid){
        this.uuid = uuid;
        this.blockMap = new ConcurrentHashMap<>();
        
        removeParallelWorld(uuid);
        worldMap.put(uuid, this);
    }
    
    public void setBlock(Block block, Material material){
        setBlock(block.getLocation(), material.createBlockData());
    }
    
    public void setBlock(Block block, BlockData blockData){
        setBlock(block.getLocation(), blockData);
    }
    
    public void setBlock(Location location, BlockData blockData){
        blockMap.put(location, blockData);
    
        if(location.getChunk().isLoaded()) {
            for(Player player : Bukkit.getServer().getOnlinePlayers()) {
                if (player.getUniqueId().toString().equals(uuid)) {
                    player.sendBlockChange(location, blockData);
                    break;
                }
            }
        }
    }
    
    
    /**
     * 一気に大量のブロックを設置する
     * @param blockDataMap 置き換えるブロックとブロックデータのマップ
     * @param chunkUpdate チャンクアップデートのパケットをプレイヤーに送信するかどうか
     */
    public void setBlocks(Map<Block, BlockData> blockDataMap, boolean chunkUpdate){
        Set<Chunk> chunkSet = new HashSet<>();
        
        for(Map.Entry<Block, BlockData> entry : blockDataMap.entrySet()){
            Block block = entry.getKey();
            BlockData data = entry.getValue();
            
            Location location = block.getLocation();
            blockMap.put(location, data);
            
            chunkSet.add(block.getChunk());
        }
    
        
        if(!chunkUpdate) return;
        
        for(Chunk chunk : chunkSet) {
            if (chunk.isLoaded()){
                for (Player player : Bukkit.getServer().getOnlinePlayers()) {
                    if (player.getUniqueId().toString().equals(uuid)) {
                        try {
                            Object nmsChunk = NMSUtil.getNMSChunk(chunk);
                            NMSUtil.sendChunkUpdatePacket(player, nmsChunk);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                    }
                }
            }
        }
    }
    
    public void removeBlock(Block block){
        removeBlock(block.getLocation());
    }
    
    public void removeBlock(Location location){
        blockMap.remove(location);
    }
    
    public Map<Location, BlockData> getBlockMap() {return blockMap;}
}
