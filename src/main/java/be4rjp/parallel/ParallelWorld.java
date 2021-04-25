package be4rjp.parallel;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;

import java.util.AbstractMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class ParallelWorld {
    
    private static Set<Map.Entry<String, ParallelWorld>> worldMap;
    
    static {
        initialize();
    }
    
    public static void initialize(){
        worldMap = new CopyOnWriteArraySet<>();
    }
    
    public static ParallelWorld getParallelWorld(Player player){
        return getParallelWorld(player.getUniqueId().toString());
    }
    
    public static ParallelWorld getParallelWorld(String uuid){
        for(Map.Entry<String, ParallelWorld> entry : worldMap){
            if(entry.getKey().equals(uuid)) return entry.getValue();
        }
        return new ParallelWorld(uuid);
    }
    
    public static void removeParallelWorld(String uuid){
        worldMap.removeIf(entry -> entry.getKey().equals(uuid));
    }
    
    
    
    
    private final String uuid;
    private final Set<Map.Entry<Location, BlockData>> blockMap;
    
    public ParallelWorld(String uuid){
        this.uuid = uuid;
        this.blockMap = new CopyOnWriteArraySet<>();
        
        removeParallelWorld(uuid);
        worldMap.add(new AbstractMap.SimpleEntry<>(uuid, this));
    }
    
    public void setBlock(Block block, Material material){
        setBlock(block.getLocation(), material.createBlockData());
    }
    
    public void setBlock(Block block, BlockData blockData){
        setBlock(block.getLocation(), blockData);
    }
    
    public void setBlock(Location location, BlockData blockData){
        blockMap.removeIf(entry -> entry.getKey().equals(location));
        blockMap.add(new AbstractMap.SimpleEntry<>(location, blockData));
    
        if(location.getChunk().isLoaded()) {
            for(Player player : Bukkit.getServer().getOnlinePlayers()) {
                if (player.getUniqueId().toString().equals(uuid)) {
                    player.sendBlockChange(location, blockData);
                    break;
                }
            }
        }
    }
    
    public void removeBlock(Block block){
        removeBlock(block.getLocation());
    }
    
    public void removeBlock(Location location){
        blockMap.removeIf(entry -> entry.getKey().equals(location));
    }
    
    public Set<Map.Entry<Location, BlockData>> getBlockMap() {return blockMap;}
}
