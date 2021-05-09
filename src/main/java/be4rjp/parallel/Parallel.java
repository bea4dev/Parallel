package be4rjp.parallel;

import be4rjp.parallel.command.parallelCommandExecutor;
import be4rjp.parallel.structure.ParallelStructure;
import be4rjp.parallel.structure.StructureData;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class Parallel extends JavaPlugin {
    
    private static Parallel plugin;
    
    @Override
    public void onEnable() {
        // Plugin startup logic
        plugin = this;
    
        //Register event listeners
        getLogger().info("Registering event listeners...");
        PluginManager pluginManager = getServer().getPluginManager();
        pluginManager.registerEvents(new EventListener(), this);
    
    
        //Register command executors
        getLogger().info("Registering command executors...");
        getCommand("parallel").setExecutor(new parallelCommandExecutor());
        getCommand("parallel").setTabCompleter(new parallelCommandExecutor());
    
        
        StructureData.loadAllStructureData();
        ParallelStructure.loadAllParallelStructure();
    }
    
    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
    
    
    public static Parallel getPlugin(){
        return plugin;
    }
}
