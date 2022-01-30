package be4rjp.parallel.chiyogami;

import be4rjp.parallel.player.ParallelPlayer;
import org.bukkit.entity.Player;
import world.chiyogami.chiyogamilib.ChiyogamiLib;
import world.chiyogami.chiyogamilib.ServerType;

public class ChiyogamiManager {
    
    public static void setCheckFunction(ParallelPlayer parallelPlayer, Object wrappedParallelPlayer){
        if(ChiyogamiLib.getServerType() == ServerType.CHIYOGAMI && wrappedParallelPlayer != null){
            ChiyogamiBridge.setCheckFunction(parallelPlayer, wrappedParallelPlayer);
        }
    }
    
    
    public static void removeWrappedParallelPlayer(Player player){
        if(ChiyogamiLib.getServerType() == ServerType.CHIYOGAMI){
            ChiyogamiBridge.removeWrappedParallelPlayer(player);
        }
    }
    
    public static Object getWrappedParallelPlayer(Player player){
        if(ChiyogamiLib.getServerType() == ServerType.CHIYOGAMI){
            return ChiyogamiBridge.getWrappedParallelPlayer(player);
        }
        return null;
    }
    
}
