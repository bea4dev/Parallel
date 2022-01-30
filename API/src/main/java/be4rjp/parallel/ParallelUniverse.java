package be4rjp.parallel;

import be4rjp.parallel.player.ParallelPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Set;

/**
 * If you set an instance of a class that implements this interface as a player, the block changes will be applied.
 */
public interface ParallelUniverse {

    /**
     * Get name of this universe.
     * @return Universe name.
     */
    @NotNull String getName();

    /**
     * Get ParallelWorld from the world name.
     * @param worldName Name of the world.
     * @return ParallelWorld
     */
    @NotNull ParallelWorld getWorld(String worldName);

    /**
     * Add a player to this universe.
     * @param player Player to add
     */
    void addPlayer(@NotNull ParallelPlayer player);

    /**
     * Remove a player to this universe.
     * @param player A player to remove
     */
    void removePlayer(@NotNull ParallelPlayer player);

    /**
     * Get all players in this universe.
     * @return All players in this universe
     */
    Set<ParallelPlayer> getResidents();
    
    /**
     * Get all world in this universe.
     * @return All world.
     */
    Collection<ParallelWorld> getAllWorld();
    
    /**
     * Add a diff for the specified universe.
     * @param universe Universe
     */
    void addDiffs(ParallelUniverse universe);
}
