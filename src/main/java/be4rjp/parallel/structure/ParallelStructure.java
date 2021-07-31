package be4rjp.parallel.structure;

import be4rjp.parallel.Parallel;
import be4rjp.parallel.ParallelWorld;
import be4rjp.parallel.enums.UpdatePacketType;
import be4rjp.parallel.nms.NMSUtil;
import be4rjp.parallel.util.BlockPosition3i;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class ParallelStructure {
    
    private static Map<String, ParallelStructure> structureMap;
    
    static {
        initialize();
    }
    
    public static void initialize(){
        structureMap = new HashMap<>();
    }
    
    public static ParallelStructure getParallelStructure(String name){
        return structureMap.get(name);
    }
    
    public static Map<String, ParallelStructure> getStructureMap() {return structureMap;}
    
    /**
     * 全ての構造物を読み込む
     */
    public static void loadAllParallelStructure() {
        initialize();
    
        Parallel.getPlugin().getLogger().info("Loading structures...");
        File dir = new File("plugins/Parallel/structures");
    
        dir.getParentFile().mkdir();
        dir.mkdir();
        File[] files = dir.listFiles();
        if (files.length == 0) {
            //Parallel.getPlugin().saveResource("structures/sample-structure.yml", false);
            files = dir.listFiles();
        }
    
        if (files != null) {
            for (File file : files) {
                Parallel.getPlugin().getLogger().info(file.getName());
                String name = file.getName().replace(".yml", "");
                ParallelStructure parallelStructure = new ParallelStructure(name);
                parallelStructure.loadData();
            }
        }
    }
    
    
    
    private final String name;
    private Location baseLocation;
    private Map<String, Set<Block>> dataMap = new HashMap<>();
    
    public ParallelStructure(String name){
        this.name = name;
        structureMap.put(name, this);
    }
    
    public void setBaseLocation(Location baseLocation){this.baseLocation = baseLocation;}
    
    
    public Location getBaseLocation() {return this.baseLocation.clone();}
    
    
    /**
     * この構造物を指定された構造物データで上書きして特定のプレイヤーへ見せる
     * @param player 構造物を変化させて見せるプレイヤー
     * @param structureData 構造物データ
     */
    public void setStructureData(Player player, StructureData structureData){
        this.setStructureData(player.getUniqueId().toString(), structureData);
    }
    
    
    /**
     * この構造物を指定された構造物データを適応して特定のプレイヤーへ見せる
     * @param uuid 構造物を変化させて見せるプレイヤーのuuid
     * @param structureData 構造物データ
     */
    public void setStructureData(String uuid, StructureData structureData){
        clearStructureData(uuid, false);
        
        Map<Block, BlockData> blockDataMap = new HashMap<>();
        Set<Block> blocks = new HashSet<>();
        for(Map.Entry<BlockPosition3i, BlockData> entry : structureData.getBlockDataMap().entrySet()){
            BlockPosition3i relative = entry.getKey();
            Block block = getBaseLocation().add(relative.getX(), relative.getY(), relative.getZ()).getBlock();
            blockDataMap.put(block, entry.getValue());
            blocks.add(block);
        }
    
        Map<Block, Integer> blockLightLevelMap = new HashMap<>();
        for(Map.Entry<BlockPosition3i, Integer> entry : structureData.getBlockLightLevelMap().entrySet()){
            BlockPosition3i relative = entry.getKey();
            Block block = getBaseLocation().add(relative.getX(), relative.getY(), relative.getZ()).getBlock();
            blockLightLevelMap.put(block, entry.getValue());
            blocks.add(block);
        }
        
        dataMap.put(uuid, blocks);
    
        ParallelWorld parallelWorld = ParallelWorld.getParallelWorld(uuid);
        parallelWorld.setBlocks(blockDataMap, UpdatePacketType.MULTI_BLOCK_CHANGE);
        parallelWorld.setLightLevels(blockLightLevelMap);
    }
    
    
    /**
     * 適用されている構造物データを消去します
     * @param player 構造物を変化させて見せるプレイヤー
     * @param chunkUpdate チャンクアップデートのパケットを送信するかどうか
     */
    public void clearStructureData(Player player, boolean chunkUpdate){
        this.clearStructureData(player.getUniqueId().toString(), chunkUpdate);
    }
    
    
    /**
     * 適用されている構造物データを消去します
     * @param uuid 構造物を変化させて見せるプレイヤーのuuid
     * @param chunkUpdate チャンクアップデートのパケットを送信するかどうか
     */
    public void clearStructureData(String uuid, boolean chunkUpdate){
        Set<Block> blocks = dataMap.get(uuid);
        if(blocks == null) return;
        
        ParallelWorld parallelWorld = ParallelWorld.getParallelWorld(uuid);
        Set<Chunk> chunks = new HashSet<>();
        for(Block block : blocks){
            parallelWorld.removeBlock(block);
            chunks.add(block.getChunk());
        }
        dataMap.remove(uuid);
        
        if(chunkUpdate){
            Player player = Bukkit.getPlayer(UUID.fromString(uuid));
            if(player == null) return;
            
            try {
                for (Chunk chunk : chunks) {
                    Object nmsChunk = NMSUtil.getNMSChunk(chunk);
                    NMSUtil.sendChunkUpdatePacket(player, nmsChunk);
                }
            }catch (Exception e){e.printStackTrace();}
        }
    }
    
    
    /**
     * ymlファイルから読み込み
     */
    public void loadData() {
        File file = new File("plugins/Parallel/structures", name + ".yml");
        createFile(file);
        
        FileConfiguration yml = YamlConfiguration.loadConfiguration(file);
        this.baseLocation = yml.getLocation("base-location");
    }
    
    
    /**
     * ymlファイルへ書き込み
     */
    public void saveData() {
        File file = new File("plugins/Parallel/structures", name + ".yml");
        FileConfiguration yml = new YamlConfiguration();
        
        yml.set("base-location", baseLocation);
    
        try {
            yml.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    
    /**
     * ファイルが存在しなければ作成する
     * @param file
     */
    public void createFile(File file){
        file.getParentFile().mkdir();
        if(!file.exists()){
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
