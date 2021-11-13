package be4rjp.parallel;

import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class Config {
    
    private static WorkType workType = WorkType.NORMAL;
    
    private static boolean performanceMode = false;
    
    private static boolean rewriteLightPacket = true;

    private static boolean showChunkPacketWarning = true;
    
    public static WorkType getWorkType() {return workType;}
    
    public static boolean isPerformanceMode() {return performanceMode;}
    
    public static boolean isRewriteLightPacket() {return rewriteLightPacket;}

    public static boolean isShowChunkPacketWarning() {return showChunkPacketWarning;}

    public static void load(){
        File file = new File("plugins/Parallel", "config.yml");
        file.getParentFile().mkdirs();
    
        if(!file.exists()){
            Parallel.getPlugin().saveResource("config.yml", false);
        }
    
        //ロードと値の保持
        YamlConfiguration yml = YamlConfiguration.loadConfiguration(file);
        
        if(yml.contains("work-type")) workType = WorkType.valueOf(yml.getString("work-type"));
        if(yml.contains("performance-mode")) performanceMode = yml.getBoolean("performance-mode");
        if(yml.contains("rewrite-light-packet")) rewriteLightPacket = yml.getBoolean("rewrite-light-packet");
        if(yml.contains("show-chunk-packet-warning")) showChunkPacketWarning = yml.getBoolean("show-chunk-packet-warning");
    }
    
    public enum WorkType{
        NORMAL,
        ONLY_ONE
    }
}
