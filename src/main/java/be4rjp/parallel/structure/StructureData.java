package be4rjp.parallel.structure;

import be4rjp.parallel.Parallel;
import be4rjp.parallel.nms.NMSUtil;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.util.Vector;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StructureData {
    
    private static Map<String, StructureData> structureDataMap;
    
    static {
        initialize();
    }
    
    public static void initialize(){
        structureDataMap = new HashMap<>();
    }
    
    public static StructureData getStructureData(String name){
        return structureDataMap.get(name);
    }
    
    
    /**
     * 全ての構造物データを読み込む
     */
    public static void loadAllStructureData() {
        initialize();
    
        Parallel.getPlugin().getLogger().info("Loading structure data...");
        File dir = new File("plugins/Parallel/structure_data");
    
        dir.getParentFile().mkdir();
        dir.mkdir();
        File[] files = dir.listFiles();
        if (files.length == 0) {
            //Parallel.getPlugin().saveResource("structure_data/sample-data.yml", false);
            files = dir.listFiles();
        }
    
        if (files != null) {
            for (File file : files) {
                Parallel.getPlugin().getLogger().info(file.getName());
                String name = file.getName().replace(".yml", "");
                
                StructureData data = new StructureData(name);
                data.loadData();
            }
        }
    }
    
    
    
    
    private final String name;
    private final Map<Vector, BlockData> blockDataMap = new HashMap<>();
    
    public StructureData(String name){
        this.name = name;
        structureDataMap.put(name, this);
    }
    
    public Map<Vector, BlockData> getBlockDataMap() {return blockDataMap;}
    
    
    /**
     * ブロックの状態を記録
     * @param baseLocation
     * @param blocks
     */
    public void setBlockData(Location baseLocation, List<Block> blocks){
        for(Block block : blocks) {
            Vector relative = new Vector(block.getX() - baseLocation.getBlockX(), block.getY() - baseLocation.getBlockY(), block.getZ() - baseLocation.getBlockZ());
            BlockData blockData = block.getBlockData();
            
            this.blockDataMap.put(relative, blockData);
        }
    }
    
    /**
     * ymlファイルから読み込み
     */
    public void loadData(){
        this.blockDataMap.clear();
    
        File file = new File("plugins/Parallel/structure_data", name + ".yml");
        createFile(file);
    
        FileConfiguration yml = YamlConfiguration.loadConfiguration(file);
        List<String> lines = yml.getStringList("blocks");
        //x, y, z, CombinedId
        for(String line : lines){
            line = line.replace(" ", "");
            String[] args = line.split(",");
            
            int x = Integer.parseInt(args[0]);
            int y = Integer.parseInt(args[1]);
            int z = Integer.parseInt(args[2]);
            
            Vector relative = new Vector(x, y, z);
            int id = Integer.parseInt(args[3]);
            try {
                Object iBlockData = NMSUtil.getByCombinedId(id);
                BlockData blockData = NMSUtil.getBlockData(iBlockData);
                
                this.blockDataMap.put(relative, blockData);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    
    /**
     * ymlファイルへ書き込み
     */
    public void saveData(){
        File file = new File("plugins/Parallel/structure_data", name + ".yml");
        FileConfiguration yml = new YamlConfiguration();
        
        List<String> lines = new ArrayList<>();
        for(Map.Entry<Vector, BlockData> entry : this.blockDataMap.entrySet()){
            Vector relative = entry.getKey();
    
            try {
                Object iBlockData = NMSUtil.getIBlockData(entry.getValue());
                int id = NMSUtil.getCombinedId(iBlockData);
                
                String line = relative.getBlockX() + ", " + relative.getBlockY() + ", " + relative.getBlockZ() + ", " + id;
                lines.add(line);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        yml.set("blocks", lines);
    
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
