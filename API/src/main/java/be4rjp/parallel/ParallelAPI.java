package be4rjp.parallel;

import be4rjp.parallel.player.ParallelPlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Set;

public abstract class ParallelAPI {
    
    //API instance
    private static ParallelAPI instance;
    
    /**
     * Get api instance.
     * @return ParallelAPI
     */
    public static @NotNull ParallelAPI getInstance() {return instance;}
    
    /**
     * Create universe if absent.
     * @param universeName Name of a universe
     * @return ParallelUniverse
     */
    public abstract @NotNull ParallelUniverse createUniverse(String universeName);
    
    /**
     * Get universe.
     * @param universeName Name of a universe
     * @return If the Universe with the specified name does not exist, return null.
     */
    public abstract @Nullable ParallelUniverse getUniverse(String universeName);
    
    /**
     * Remove universe with the specified name.
     * @param universeName Name of a universe.
     */
    public abstract void removeUniverse(String universeName);
    
    /**
     * Get all universe name.
     * @return All universe name
     */
    public abstract Set<String> getAllUniverseName();
    
    /**
     * Get all universe.
     * @return All universe
     */
    public abstract Collection<ParallelUniverse> getAllUniverse();
    
    /**
     * Get ParallelPlayer
     * @return ParallelPlayer
     */
    public @Nullable ParallelPlayer getParallelPlayer(Player player){return ParallelPlayer.getParallelPlayer(player);}
    
}
