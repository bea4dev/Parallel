package be4rjp.parallel.cinema4c;

import be4rjp.cinema4c.bridge.PluginBridge;
import be4rjp.cinema4c.player.ScenePlayer;
import be4rjp.parallel.Parallel;
import be4rjp.parallel.structure.ParallelStructure;
import be4rjp.parallel.structure.StructureData;
import org.bukkit.ChatColor;

public class C4CBridge implements PluginBridge {
    
    @Override
    public void executeCommand(ScenePlayer scenePlayer, String command) {
        //set-data [structure-name] [data-name]
        String[] args = command.split(" ");
        if(args.length < 3) return;
        
        if(args[0].equals("set-data")){
            ParallelStructure parallelStructure = ParallelStructure.getParallelStructure(args[1]);
            if (parallelStructure == null) {
                Parallel.getPlugin().getLogger().warning(ChatColor.RED + "指定された名前の構造体は存在しません。");
                return;
            }
    
            StructureData structureData = StructureData.getStructureData(args[2]);
            if (structureData == null) {
                Parallel.getPlugin().getLogger().warning(ChatColor.RED + "指定された名前の構造データは存在しません。");
                return;
            }
            
            scenePlayer.getAudiences().forEach(audience -> parallelStructure.setStructureData(audience, structureData));
        }
    }
}
