package be4rjp.parallel;

import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class Parallel extends JavaPlugin {
    
    private static Parallel plugin;
    
    @Override
    public void onEnable() {
        // Plugin startup logic
        plugin = this;
    
        PluginManager pluginManager = getServer().getPluginManager();
        pluginManager.registerEvents(new EventListener(), this);
    }
    
    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
    
    
    public static Parallel getPlugin(){
        return plugin;
    }
}
