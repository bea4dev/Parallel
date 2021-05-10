package be4rjp.parallel.structure;

import be4rjp.parallel.Parallel;
import be4rjp.parallel.ParallelWorld;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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
        Map<Block, BlockData> blockDataMap = new HashMap<>();
        for(Map.Entry<Vector, BlockData> entry : structureData.getBlockDataMap().entrySet()){
            Block block = getBaseLocation().add(entry.getKey()).getBlock();
            blockDataMap.put(block, entry.getValue());
        }
    
        ParallelWorld parallelWorld = ParallelWorld.getParallelWorld(uuid);
        parallelWorld.setBlocks(blockDataMap, true);
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
