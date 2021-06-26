package be4rjp.parallel;

import be4rjp.parallel.enums.UpdatePacketType;
import be4rjp.parallel.nms.NMSUtil;
import be4rjp.parallel.nms.manager.MultiBlockChangePacketManager;
import be4rjp.parallel.util.ChunkLocation;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.util.*;
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
    private final Map<ChunkLocation, Map<Location, BlockData>> chunkBlockMap;
    
    private ParallelWorld(String uuid){
        this.uuid = uuid;
        this.chunkBlockMap = new ConcurrentHashMap<>();
        
        removeParallelWorld(uuid);
        worldMap.put(uuid, this);
    }


    /**
     * ブロックを設置します
     * @param block 設置したいブロック
     * @param material 設定したいブロックのマテリアル
     * @param blockUpdate ブロックの変更をプレイヤーに通知するかどうか
     */
    public void setBlock(Block block, Material material, boolean blockUpdate){
        setBlock(block.getLocation(), material.createBlockData(), blockUpdate);
    }


    /**
     * ブロックを設置します
     * @param block 設置したいブロック
     * @param blockData 設定したいブロックのデータ
     * @param blockUpdate ブロックの変更をプレイヤーに通知するかどうか
     */
    public void setBlock(Block block, BlockData blockData, boolean blockUpdate){
        setBlock(block.getLocation(), blockData, blockUpdate);
    }


    private void setBlock(Location location, BlockData blockData, boolean blockUpdate){
        ChunkLocation chunkLocation = new ChunkLocation(location.getBlockX(), location.getBlockZ());

        Map<Location, BlockData> blockMap = chunkBlockMap.get(chunkLocation);
        if(blockMap == null){
            blockMap = new ConcurrentHashMap<>();
            chunkBlockMap.put(chunkLocation, blockMap);
        }
        blockMap.put(location, blockData);
    
        if(location.getChunk().isLoaded() && blockUpdate) {
            for(Player player : Bukkit.getServer().getOnlinePlayers()) {
                if (player.getUniqueId().toString().equals(uuid)) {
                    player.sendBlockChange(location, blockData);
                    break;
                }
            }
        }
    }
    
    
    /**
     * 一気に大量のブロックを設置します。1.16.4以降未対応
     * @param blockDataMap 置き換えるブロックとブロックデータのマップ
     * @param type ブロックの変更をクライアントに適用させるためのパケットの種類。
     *             MULTI_BLOCK_CHANGE推奨
     */
    @Deprecated
    public void setBlocks(Map<Block, BlockData> blockDataMap, @Nullable UpdatePacketType type){
        
        if(type == null) type = UpdatePacketType.NO_UPDATE;
    
        Map<Chunk, Set<Block>> updateMap = new HashMap<>();
        
        for(Map.Entry<Block, BlockData> entry : blockDataMap.entrySet()){
            Block block = entry.getKey();
            BlockData data = entry.getValue();
        
            Location location = block.getLocation();
            ChunkLocation chunkLocation = new ChunkLocation(location.getBlockX(), location.getBlockZ());
        
            Map<Location, BlockData> blockMap = chunkBlockMap.get(chunkLocation);
            if(blockMap == null){
                blockMap = new ConcurrentHashMap<>();
                chunkBlockMap.put(chunkLocation, blockMap);
            }
            blockMap.put(location, data);
    
            Set<Block> blocks = updateMap.computeIfAbsent(block.getChunk(), k -> new HashSet<>());
            blocks.add(block);
        }
        
        
        switch (type){
            case NO_UPDATE:{
                break;
            }
            
            case CHUNK_MAP:{
                for(Chunk chunk : updateMap.keySet()) {
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
                break;
            }
            
            case MULTI_BLOCK_CHANGE:{
                if(!MultiBlockChangePacketManager.VERSION_1_16_R3){
                    for(Map.Entry<Chunk, Set<Block>> entry : updateMap.entrySet()) {
                        Chunk chunk = entry.getKey();
                        Set<Block> blocks = entry.getValue();
                        
                        short[] locations = new short[65535];
                        int index = 0;
                        for(Block block : blocks){
                            short loc = (short) ((block.getX() & 15) << 12 | (block.getZ() & 15) << 8 | block.getY());
                            locations[index] = loc;
                            index++;
                        }
    
                        if (chunk.isLoaded()){
                            for (Player player : Bukkit.getServer().getOnlinePlayers()) {
                                if (player.getUniqueId().toString().equals(uuid)) {
                                    try {
                                        NMSUtil.sendLegacyMultiBlockChangePacket(player, index, locations, chunk);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    break;
                                }
                            }
                        }
                    }
                }
                
                break;
            }
        }
    }
    
    
    /**
     * 一気に大量のブロックを設置します。
     * @param blockDataMap 置き換えるブロックとブロックデータのマップ
     * @param chunkUpdate チャンクアップデートのパケットをプレイヤーに送信するかどうか
     */
    public void setBlocks(Map<Block, BlockData> blockDataMap, boolean chunkUpdate){
        Set<Chunk> chunkSet = new HashSet<>();
        
        for(Map.Entry<Block, BlockData> entry : blockDataMap.entrySet()){
            Block block = entry.getKey();
            BlockData data = entry.getValue();
            
            Location location = block.getLocation();
            ChunkLocation chunkLocation = new ChunkLocation(location.getBlockX(), location.getBlockZ());

            Map<Location, BlockData> blockMap = chunkBlockMap.get(chunkLocation);
            if(blockMap == null){
                blockMap = new ConcurrentHashMap<>();
                chunkBlockMap.put(chunkLocation, blockMap);
            }
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
    
    
    


    /**
     * 指定されたブロックに設定されているデータを削除します
     * @param block
     */
    public void removeBlock(Block block){
        removeBlock(block.getLocation());
    }


    private void removeBlock(Location location){
        ChunkLocation chunkLocation = new ChunkLocation(location.getBlockX(), location.getBlockZ());

        Map<Location, BlockData> blockMap = chunkBlockMap.get(chunkLocation);
        if(blockMap == null){
            blockMap = new ConcurrentHashMap<>();
            chunkBlockMap.put(chunkLocation, blockMap);
        }
        blockMap.remove(location);
    }


    /**
     * ブロックの編集を行いたい場合は必ずほかのメソッドを使用してください
     * @return Map<ChunkLocation, Map<Location, BlockData>>
     */
    public Map<ChunkLocation, Map<Location, BlockData>> getChunkBlockMap() {
        return new ConcurrentHashMap<>(chunkBlockMap);
    }
}
