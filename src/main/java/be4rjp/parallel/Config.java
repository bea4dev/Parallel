package be4rjp.parallel;

import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class Config {
    
    private static WorkType workType = WorkType.NORMAL;
    
    public static WorkType getWorkType() {return workType;}
    
    public static void load(){
        File file = new File("plugins/Parallel", "config.yml");
        file.getParentFile().mkdirs();
    
        if(!file.exists()){
            Parallel.getPlugin().saveResource("config.yml", false);
        }
    
        //ロードと値の保持
        YamlConfiguration yml = YamlConfiguration.loadConfiguration(file);
        
        if(yml.contains("work-type")) workType = WorkType.valueOf(yml.getString("work-type"));
    }
    
    public enum WorkType{
        NORMAL,
        ONLY_ONE
    }
}
