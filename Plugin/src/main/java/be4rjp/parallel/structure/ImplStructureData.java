package be4rjp.parallel.structure;

import be4rjp.parallel.Parallel;
import be4rjp.parallel.nms.NMSManager;
import be4rjp.parallel.util.BlockPosition3i;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ImplStructureData extends StructureData{
    
    public ImplStructureData(String name) {
        super(name);
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
                
                ImplStructureData data = new ImplStructureData(name);
                data.loadData();
            }
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
    
            BlockPosition3i relative = new BlockPosition3i(x, y, z);
            int id = Integer.parseInt(args[3]);
            Object iBlockData = NMSManager.getNmsHandler().getIBlockDataByCombinedId(id);
            this.blockDataMap.put(relative, NMSManager.getNmsHandler().getBukkitBlockData(iBlockData));
        }
    
        if(yml.contains("block-lights")) {
            lines = yml.getStringList("block-lights");
            //x, y, z, lightLevel
            for (String line : lines) {
                line = line.replace(" ", "");
                String[] args = line.split(",");
        
                int x = Integer.parseInt(args[0]);
                int y = Integer.parseInt(args[1]);
                int z = Integer.parseInt(args[2]);
    
                BlockPosition3i relative = new BlockPosition3i(x, y, z);
                int lightLevel = Integer.parseInt(args[3]);
                this.blockLightLevelMap.put(relative, lightLevel);
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
        for(Map.Entry<BlockPosition3i, BlockData> entry : this.blockDataMap.entrySet()){
            BlockPosition3i relative = entry.getKey();
    
            try {
                Object iBlockData = NMSManager.getNmsHandler().getIBlockData(entry.getValue());
                int id = NMSManager.getNmsHandler().getCombinedIdByIBlockData(iBlockData);
                
                String line = relative.getX() + ", " + relative.getY() + ", " + relative.getZ() + ", " + id;
                lines.add(line);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        yml.set("blocks", lines);
    
        List<String> lines2 = new ArrayList<>();
        for(Map.Entry<BlockPosition3i, Integer> entry : this.blockLightLevelMap.entrySet()){
            BlockPosition3i relative = entry.getKey();
            int lightLevel = entry.getValue();
            
            String line = relative.getX() + ", " + relative.getY() + ", " + relative.getZ() + ", " + lightLevel;
            lines2.add(line);
        }
        yml.set("block-lights", lines2);
        
    
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
