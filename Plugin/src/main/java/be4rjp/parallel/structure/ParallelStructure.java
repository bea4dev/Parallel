package be4rjp.parallel.structure;

import be4rjp.parallel.Parallel;
import be4rjp.parallel.ParallelUniverse;
import be4rjp.parallel.ParallelWorld;
import be4rjp.parallel.enums.UpdatePacketType;
import be4rjp.parallel.player.ParallelPlayer;
import be4rjp.parallel.util.BlockPosition3i;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
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
     * @param implStructureData 構造物データ
     */
    public void setStructureData(Player player, ImplStructureData implStructureData){
        ParallelPlayer parallelPlayer = ParallelPlayer.getParallelPlayer(player);
        if(parallelPlayer == null) return;
        
        this.setStructureData(parallelPlayer, implStructureData);
    }
    
    /**
     * この構造物を指定された構造物データを適応して特定のプレイヤーへ見せる
     * @param parallelPlayer 構造物を変化させて見せるプレイヤー
     * @param implStructureData 構造物データ
     */
    public void setStructureData(ParallelPlayer parallelPlayer, ImplStructureData implStructureData) {
        this.setStructureData(parallelPlayer, implStructureData, UpdatePacketType.MULTI_BLOCK_CHANGE);
    }
    
    /**
     * この構造物を指定された構造物データを適応して特定のプレイヤーへ見せる
     * @param parallelPlayer 構造物を変化させて見せるプレイヤー
     * @param implStructureData 構造物データ
     */
    public void setStructureData(ParallelPlayer parallelPlayer, ImplStructureData implStructureData, @Nullable UpdatePacketType type){
        clearStructureData(parallelPlayer, false);
    
        ParallelUniverse universe = parallelPlayer.getUniverse();
        if(universe == null) return;
    
        ParallelWorld parallelWorld = universe.getWorld(Objects.requireNonNull(baseLocation.getWorld()).getName());
        
        Set<Block> blocks = new HashSet<>();
        Set<BlockPosition3i> updateBlocks = new HashSet<>();
        for(Map.Entry<BlockPosition3i, BlockData> entry : implStructureData.getBlockDataMap().entrySet()){
            BlockPosition3i relative = entry.getKey();
            Block block = getBaseLocation().add(relative.getX(), relative.getY(), relative.getZ()).getBlock();
            parallelWorld.setBlockData(block.getX(), block.getY(), block.getZ(), entry.getValue());
            blocks.add(block);
            updateBlocks.add(new BlockPosition3i(block.getX(), block.getY(), block.getZ()));
        }
        
        for(Map.Entry<BlockPosition3i, Integer> entry : implStructureData.getBlockLightLevelMap().entrySet()){
            BlockPosition3i relative = entry.getKey();
            Block block = getBaseLocation().add(relative.getX(), relative.getY(), relative.getZ()).getBlock();
            parallelWorld.setBlockLightLevel(block.getX(), block.getY(), block.getZ(), entry.getValue());
            blocks.add(block);
            updateBlocks.add(new BlockPosition3i(block.getX(), block.getY(), block.getZ()));
        }
        
        dataMap.put(universe.getName(), blocks);
    
        parallelWorld.sendMultiBlockUpdate(updateBlocks);
    }
    
    
    /**
     * 適用されている構造物データを消去します
     * @param player 構造物を変化させて見せるプレイヤー
     * @param chunkUpdate チャンクアップデートのパケットを送信するかどうか
     */
    public void clearStructureData(Player player, boolean chunkUpdate){
        ParallelPlayer parallelPlayer = ParallelPlayer.getParallelPlayer(player);
        if(parallelPlayer == null) return;
    
        this.clearStructureData(parallelPlayer, chunkUpdate);
    }
    
    
    /**
     * 適用されている構造物データを消去します
     * @param parallelPlayer 構造物を変化させて見せるプレイヤー
     * @param chunkUpdate チャンクアップデートのパケットを送信するかどうか
     */
    public void clearStructureData(ParallelPlayer parallelPlayer, boolean chunkUpdate){
    
        ParallelUniverse universe = parallelPlayer.getUniverse();
        if(universe == null) return;
        
        Set<Block> blocks = dataMap.get(universe.getName());
        if(blocks == null) return;
    
        ParallelWorld parallelWorld = universe.getWorld(Objects.requireNonNull(baseLocation.getWorld()).getName());
        
        for(Block block : blocks){
            parallelWorld.removeBlockData(block.getX(), block.getY(), block.getZ());
        }
        Set<BlockPosition3i> blockPosition3iSet = new HashSet<>();
        blocks.forEach(block -> blockPosition3iSet.add(new BlockPosition3i(block.getX(), block.getY(), block.getZ())));
        
        parallelWorld.sendMultiBlockUpdate(blockPosition3iSet);
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
