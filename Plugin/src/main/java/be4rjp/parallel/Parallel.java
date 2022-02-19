package be4rjp.parallel;

import be4rjp.artgui.ArtGUI;
import be4rjp.parallel.cinema4c.BridgeManager;
import be4rjp.parallel.command.parallelCommandExecutor;
import be4rjp.parallel.impl.ImplParallelAPI;
import be4rjp.parallel.nms.NMSManager;
import be4rjp.parallel.structure.ParallelStructure;
import be4rjp.parallel.structure.ImplStructureData;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;


public final class Parallel extends JavaPlugin {
    
    private static Parallel plugin;

    private static ArtGUI artGUI;
    
    @Override
    public void onEnable() {
        // Plugin startup logic
        plugin = this;
        
        createAPIInstance();

        artGUI = new ArtGUI(this);

        NMSManager.setup();

        //Load config
        Config.load();
    
        //Register event listeners
        getLogger().info("Registering event listeners...");
        PluginManager pluginManager = getServer().getPluginManager();
        pluginManager.registerEvents(new EventListener(), this);
    
    
        if(Bukkit.getPluginManager().getPlugin("WorldEdit") != null) {
            //Register command executors
            getLogger().info("Registering command executors...");
            getCommand("parallel").setExecutor(new parallelCommandExecutor());
            getCommand("parallel").setTabCompleter(new parallelCommandExecutor());
        }
    
        
        //For cinema4c extensions
        
        if(getServer().getPluginManager().getPlugin("Cinema4C") != null){
            getLogger().info("Registering cinema4c extensions...");
            BridgeManager.registerPluginBridge(this.getName());
        }
        
        ImplStructureData.loadAllStructureData();
        ParallelStructure.loadAllParallelStructure();
    }
    
    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
    
    public void createAPIInstance(){
        try {
            Field instance = ParallelAPI.class.getDeclaredField("instance");
            instance.setAccessible(true);
            instance.set(null, new ImplParallelAPI());
        }catch (Exception e){
            e.printStackTrace();
            throw new IllegalStateException("Failed to instantiate the API.");
        }
    }
    
    
    public static Parallel getPlugin(){
        return plugin;
    }

    public ArtGUI getArtGUI() {return artGUI;}
    
}
