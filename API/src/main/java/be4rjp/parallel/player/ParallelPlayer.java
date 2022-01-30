package be4rjp.parallel.player;

import be4rjp.parallel.ParallelUniverse;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class ParallelPlayer {

    protected static final Map<Player, ParallelPlayer> playerMap = new ConcurrentHashMap<>();

    public static @Nullable ParallelPlayer getParallelPlayer(Player player){return playerMap.get(player);}


    protected final Player player;

    protected ParallelUniverse currentUniverse = null;

    protected ParallelPlayer(Player player){
        this.player = player;
    }

    public Player getBukkitPlayer() {return player;}

    public abstract @Nullable ParallelUniverse getUniverse();

    public abstract void setUniverse(@Nullable ParallelUniverse parallelUniverse);
}
