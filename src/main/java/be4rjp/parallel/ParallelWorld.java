package be4rjp.parallel;

import be4rjp.parallel.enums.UpdatePacketType;
import be4rjp.parallel.nms.NMSUtil;
import be4rjp.parallel.nms.manager.MultiBlockChangePacketManager;
import be4rjp.parallel.util.*;
import org.bukkit.*;
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
    
    private static Map<UUID, ParallelWorld> worldMap;
    
    private static final ParallelWorld onlyOneWorld;
    
    static {
        initialize();
        onlyOneWorld = new ParallelWorld(UUID.randomUUID());
    }
    
    public static void initialize(){
        worldMap = new ConcurrentHashMap<>();
    }
    
    public static synchronized ParallelWorld getParallelWorld(Player player){
        return getParallelWorld(player.getUniqueId());
    }
    
    public static synchronized ParallelWorld getParallelWorld(String uuidString){
        return getParallelWorld(UUID.fromString(uuidString));
    }
    
    public static synchronized ParallelWorld getParallelWorld(UUID uuid){
        if(Config.getWorkType() == Config.WorkType.NORMAL) {
            if (worldMap.containsKey(uuid)) return worldMap.get(uuid);
            return new ParallelWorld(uuid);
        }else{
            return onlyOneWorld;
        }
    }
    
    public static synchronized void removeParallelWorld(String uuid){
        worldMap.remove(UUID.fromString(uuid));
    }
    
    public static synchronized void removeParallelWorld(UUID uuid){
        worldMap.remove(uuid);
    }
    
    
    
    
    private final UUID uuid;
    private final Map<ChunkPosition, Map<BlockLocation, PBlockData>> chunkBlockMap;
    private final Map<ChunkLocation, Object> editedPacketForChunkMap;
    private final Map<ChunkLocation, Object> editedPacketForLightMap;
    
    private ParallelWorld(UUID uuid){
        this.uuid = uuid;
        this.chunkBlockMap = new ConcurrentHashMap<>();
        this.editedPacketForChunkMap = new ConcurrentHashMap<>();
        this.editedPacketForLightMap = new ConcurrentHashMap<>();
        
        removeParallelWorld(uuid);
        worldMap.put(uuid, this);
    }
    
    
    private void addEditedChunk(ChunkPosition chunkPosition, World world){
        ChunkLocation chunkLocation = new ChunkLocation(world, chunkPosition.x << 4, chunkPosition.z << 4);
        this.editedPacketForChunkMap.remove(chunkLocation);
        this.editedPacketForLightMap.remove(chunkLocation);
    }
    
    public Map<ChunkLocation, Object> getEditedPacketForChunkMap() {return editedPacketForChunkMap;}
    
    public Map<ChunkLocation, Object> getEditedPacketForLightMap() {return editedPacketForLightMap;}
    
    /**
     * ブロックを設置します
     * @param block 設置したいブロック
     * @param material 設定したいブロックのマテリアル
     * @param blockUpdate ブロックの変更をプレイヤーに通知するかどうか
     */
    public void setBlock(Block block, Material material, boolean blockUpdate){
        setBlock(block, material.createBlockData(), blockUpdate);
    }


    /**
     * ブロックを設置します
     * @param block 設置したいブロック
     * @param blockData 設定したいブロックのデータ
     * @param blockUpdate ブロックの変更をプレイヤーに通知するかどうか
     */
    public void setBlock(Block block, BlockData blockData, boolean blockUpdate){
        ChunkPosition chunkPosition = new ChunkPosition(block.getX(), block.getZ());
        this.addEditedChunk(chunkPosition, block.getWorld());

        Map<BlockLocation, PBlockData> blockMap = chunkBlockMap.get(chunkPosition);
        if(blockMap == null){
            blockMap = new ConcurrentHashMap<>();
            chunkBlockMap.put(chunkPosition, blockMap);
        }
        PBlockData pBlockData = blockMap.computeIfAbsent(BlockLocation.createBlockLocation(block), k -> new PBlockData());
        pBlockData.setBlockData(blockData);

        if(block.getChunk().isLoaded() && blockUpdate) {
            Player player = Bukkit.getPlayer(uuid);
            if(player != null) player.sendBlockChange(block.getLocation(), blockData);
        }
    }
    
    
    /**
     * ブロックのライトレベルを設定します
     * @param block 設定したいブロック
     * @param level 設定したいライトレベル
     */
    public void setLightLevel(Block block, int level){
        ChunkPosition chunkPosition = new ChunkPosition(block.getX(), block.getZ());
        this.addEditedChunk(chunkPosition, block.getWorld());
        
        Map<BlockLocation, PBlockData> blockMap = chunkBlockMap.get(chunkPosition);
        if(blockMap == null){
            blockMap = new ConcurrentHashMap<>();
            chunkBlockMap.put(chunkPosition, blockMap);
        }
        PBlockData pBlockData = blockMap.computeIfAbsent(BlockLocation.createBlockLocation(block), k -> new PBlockData());
        level = Math.max(level, 0);
        level = Math.min(level, 15);
        pBlockData.setBlockLightLevel(level);
    }
    
    
    /**
     * 一気に大量のブロックを設置します。
     * @param blockDataMap 置き換えるブロックとブロックデータのマップ
     * @param type ブロックの変更をクライアントに適用させるためのパケットの種類。 MULTI_BLOCK_CHANGE推奨
     */
    public void setBlocks(Map<Block, BlockData> blockDataMap, @Nullable UpdatePacketType type){
        
        if(type == null) type = UpdatePacketType.NO_UPDATE;
    
        Map<Chunk, Set<Block>> updateMap = new HashMap<>();
        
        for(Map.Entry<Block, BlockData> entry : blockDataMap.entrySet()){
            Block block = entry.getKey();
            BlockData data = entry.getValue();
        
            Location location = block.getLocation();
            ChunkPosition chunkPosition = new ChunkPosition(location.getBlockX(), location.getBlockZ());
            this.addEditedChunk(chunkPosition, block.getWorld());
        
            Map<BlockLocation, PBlockData> blockMap = chunkBlockMap.get(chunkPosition);
            if(blockMap == null){
                blockMap = new ConcurrentHashMap<>();
                chunkBlockMap.put(chunkPosition, blockMap);
            }
            PBlockData pBlockData = blockMap.computeIfAbsent(BlockLocation.createBlockLocation(block), k -> new PBlockData());
            pBlockData.setBlockData(data);
    
            Set<Block> blocks = updateMap.computeIfAbsent(block.getChunk(), k -> new HashSet<>());
            blocks.add(block);
        }

        
        if(Config.getWorkType() == Config.WorkType.NORMAL) {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null) return;
            this.sendUpdatePacket(player, type, updateMap);
        }else{
            Set<Player> players = new HashSet<>(Bukkit.getOnlinePlayers());
            for(Player player : players){
                this.sendUpdatePacket(player, type, updateMap);
            }
        }
        
    }
    
    
    /**
     * ブロックのアップデートパケットを送信します
     * @param player 送信するプレイヤー
     * @param type 送信するパケットの種類
     * @param updateMap アップデートしたいチャンクとブロックのSetのマップ
     */
    public void sendUpdatePacket(Player player, UpdatePacketType type, Map<Chunk, Set<Block>> updateMap){
        switch (type){
            case NO_UPDATE:{
                break;
            }
        
            case CHUNK_MAP:{
                for(Chunk chunk : updateMap.keySet()) {
                    if (chunk.isLoaded()){
                        try {
                            Object nmsChunk = NMSUtil.getNMSChunk(chunk);
                            NMSUtil.sendChunkUpdatePacket(player, nmsChunk);
                        } catch (Exception e) {
                            e.printStackTrace();
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
                            short loc = (short) ((block.getX() & 0xF) << 12 | (block.getZ() & 0xF) << 8 | block.getY());
                            locations[index] = loc;
                            index++;
                        }
                    
                        if (chunk.isLoaded()){
                            try {
                                NMSUtil.sendLegacyMultiBlockChangePacket(player, index, locations, chunk);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }else{
                    for(Set<Block> blocks : updateMap.values()) {
                    
                        Map<BlockPosition3i, Set<Block>> sectionMap = new HashMap<>();
                        for(Block block : blocks){
                            BlockPosition3i sectionPosition = new BlockPosition3i(block.getX() >> 4, block.getY() >> 4, block.getZ() >> 4);
                            Set<Block> sectionBlocks = sectionMap.computeIfAbsent(sectionPosition, k -> new HashSet<>());
                            sectionBlocks.add(block);
                        }
                    
                        for(Map.Entry<BlockPosition3i, Set<Block>> sectionEntry : sectionMap.entrySet()){
                            BlockPosition3i sectionPosition = sectionEntry.getKey();
                            Set<Block> sectionBlocks = sectionEntry.getValue();
                        
                            Set<Short> locations = new HashSet<>();
                            for(Block block : sectionBlocks){
                                short location = (short) ((block.getX() & 0xF) << 8 | (block.getZ() & 0xF) << 4 | block.getY() & 0xF);
                                locations.add(location);
                            }
                        
                            short[] locationsArray = new short[locations.size()];
                            int index = 0;
                            for(short location : locations){
                                locationsArray[index] = location;
                                index++;
                            }
                        
                            try {
                                NMSUtil.sendMultiBlockChangePacket(player, locations.size(), locationsArray, sectionPosition);
                            }catch (Exception e){e.printStackTrace();}
                        }
                    }
                }
            
                break;
            }
        }
    }
    
    
    /**
     * 設置した全てのブロックデータを消去します
     */
    public void removeAll(){
        chunkBlockMap.clear();
    }
    
    
    /**
     * 一気に大量のブロックを設置します。
     * @param blockDataMap 置き換えるブロックとブロックデータのマップ
     * @param chunkUpdate チャンクアップデートのパケットをプレイヤーに送信するかどうか
     */
    @Deprecated
    public void setBlocks(Map<Block, BlockData> blockDataMap, boolean chunkUpdate){
        Set<Chunk> chunkSet = new HashSet<>();
        
        for(Map.Entry<Block, BlockData> entry : blockDataMap.entrySet()){
            Block block = entry.getKey();
            BlockData data = entry.getValue();
            
            BlockLocation location = BlockLocation.createBlockLocation(block);
            ChunkPosition chunkPosition = new ChunkPosition(location.getX(), location.getZ());
            this.addEditedChunk(chunkPosition, block.getWorld());

            Map<BlockLocation, PBlockData> blockMap = chunkBlockMap.get(chunkPosition);
            if(blockMap == null){
                blockMap = new ConcurrentHashMap<>();
                chunkBlockMap.put(chunkPosition, blockMap);
            }
            PBlockData pBlockData = blockMap.computeIfAbsent(BlockLocation.createBlockLocation(block), k -> new PBlockData());
            pBlockData.setBlockData(data);
            
            chunkSet.add(block.getChunk());
        }
    
        
        if(!chunkUpdate) return;
        Player player = Bukkit.getPlayer(uuid);
        if(player == null) return;
        
        for(Chunk chunk : chunkSet) {
            if (chunk.isLoaded()){
                try {
                    Object nmsChunk = NMSUtil.getNMSChunk(chunk);
                    NMSUtil.sendChunkUpdatePacket(player, nmsChunk);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    
    /**
     * 一気に大量のブロックのライトレベルを設定します。
     * @param lightLevelMap 置き換えるブロックとブロックデータのマップ
     */
    public void setLightLevels(Map<Block, Integer> lightLevelMap){
        Set<Chunk> chunkSet = new HashSet<>();
        
        for(Map.Entry<Block, Integer> entry : lightLevelMap.entrySet()){
            Block block = entry.getKey();
            int level = Math.min(entry.getValue(), 15);
            level = Math.max(level, 0);
            
            BlockLocation location = BlockLocation.createBlockLocation(block);
            ChunkPosition chunkPosition = new ChunkPosition(location.getX(), location.getZ());
            this.addEditedChunk(chunkPosition, block.getWorld());
            
            Map<BlockLocation, PBlockData> blockMap = chunkBlockMap.get(chunkPosition);
            if(blockMap == null){
                blockMap = new ConcurrentHashMap<>();
                chunkBlockMap.put(chunkPosition, blockMap);
            }
            PBlockData pBlockData = blockMap.computeIfAbsent(BlockLocation.createBlockLocation(block), k -> new PBlockData());
            pBlockData.setBlockLightLevel(level);
            
            chunkSet.add(block.getChunk());
        }
    }
    


    /**
     * 指定されたブロックに設定されているデータを削除します
     * @param block データを削除するブロック
     */
    public void removeBlock(Block block){
        BlockLocation location = BlockLocation.createBlockLocation(block);
        ChunkPosition chunkPosition = new ChunkPosition(location.getX(), location.getZ());
        this.addEditedChunk(chunkPosition, block.getWorld());

        Map<BlockLocation, PBlockData> blockMap = chunkBlockMap.get(chunkPosition);
        if(blockMap == null) return;
        blockMap.remove(location);
    }
    
    
    /**
     * 指定されたブロックに設定されているデータを削除します
     * @param block データを削除するブロック
     * @param update ブロックの変更をプレイヤーに通知するかどうか
     */
    public void removeBlock(Block block, boolean update){
        removeBlock(block);
        Player player = Bukkit.getPlayer(uuid);
        if(player != null) player.sendBlockChange(block.getLocation(), block.getBlockData());
    }
    
    /**
     * 一気に大量のブロックのデータを削除します。
     * @param blocks データを削除するブロック
     * @param type ブロックの変更をクライアントに適用させるためのパケットの種類。 MULTI_BLOCK_CHANGE推奨
     */
    public void removeBlocks(Set<Block> blocks, @Nullable UpdatePacketType type){
        if(type == null) type = UpdatePacketType.NO_UPDATE;
    
        Map<Chunk, Set<Block>> updateMap = new HashMap<>();
    
        for(Block block : blocks){
            Location location = block.getLocation();
            ChunkPosition chunkPosition = new ChunkPosition(location.getBlockX(), location.getBlockZ());
            this.addEditedChunk(chunkPosition, block.getWorld());
        
            Map<BlockLocation, PBlockData> blockMap = chunkBlockMap.get(chunkPosition);
            if(blockMap != null){
                blockMap.remove(BlockLocation.createBlockLocation(block));
            }
        
            Set<Block> updateBlocks = updateMap.computeIfAbsent(block.getChunk(), k -> new HashSet<>());
            updateBlocks.add(block);
        }
    
    
        if(Config.getWorkType() == Config.WorkType.NORMAL) {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null) return;
            this.sendUpdatePacket(player, type, updateMap);
        }else{
            Set<Player> players = new HashSet<>(Bukkit.getOnlinePlayers());
            for(Player player : players){
                this.sendUpdatePacket(player, type, updateMap);
            }
        }
    }
    
    
    /**
     * ブロックデータを取得します
     * @param block 取得したいブロック
     * @return BlockData 見つからない場合は null を返します
     */
    public @Nullable BlockData getBlockData(Block block){
        BlockLocation location = BlockLocation.createBlockLocation(block);
        ChunkPosition chunkPosition = new ChunkPosition(location.getX(), location.getZ());
    
        Map<BlockLocation, PBlockData> blockMap = chunkBlockMap.get(chunkPosition);
        if(blockMap == null) return null;
        
        PBlockData pBlockData = blockMap.get(location);
        if(pBlockData == null) return null;
        
        return pBlockData.getBlockData();
    }
    
    
    /**
     * ライトレベルを取得します
     * @param block ライトレベルを取得したいブロック
     * @return int(0 ~ 15) 見つからない場合は -1 を返します
     */
    public int getBlockLightLevel(Block block){
        BlockLocation location = BlockLocation.createBlockLocation(block);
        ChunkPosition chunkPosition = new ChunkPosition(location.getX(), location.getZ());
        
        Map<BlockLocation, PBlockData> blockMap = chunkBlockMap.get(chunkPosition);
        if(blockMap == null) return -1;
    
        PBlockData pBlockData = blockMap.get(location);
        if(pBlockData == null) return -1;
    
        return pBlockData.getBlockLightLevel();
    }


    /**
     * ブロックの編集を行いたい場合は必ずほかのメソッドを使用してください
     * @return Map<ChunkLocation, Map<BlockLocation, PBlockData>>
     */
    public Map<ChunkPosition, Map<BlockLocation, PBlockData>> getChunkBlockMap() {
        return new ConcurrentHashMap<>(chunkBlockMap);
    }
}
