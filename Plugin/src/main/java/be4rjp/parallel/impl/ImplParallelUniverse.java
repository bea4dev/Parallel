package be4rjp.parallel.impl;

import be4rjp.parallel.ParallelUniverse;
import be4rjp.parallel.ParallelWorld;
import be4rjp.parallel.player.ParallelPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ImplParallelUniverse implements ParallelUniverse {

    private final String universeName;

    private final Set<ParallelPlayer> players = ConcurrentHashMap.newKeySet();

    public ImplParallelUniverse(String universeName){
        this.universeName = universeName;
    }

    @Override
    public @NotNull String getName() {
        return universeName;
    }


    private final Map<String, ParallelWorld> parallelWorldMap = new ConcurrentHashMap<>();

    @Override
    public @NotNull ParallelWorld getWorld(String worldName) {
        return parallelWorldMap.computeIfAbsent(worldName, name -> new ImplParallelWorld(this, worldName));
    }

    @Override
    public void addPlayer(@NotNull ParallelPlayer player) {
        player.setUniverse(this);
    }

    @Override
    public void removePlayer(@NotNull ParallelPlayer player) {
        player.setUniverse(null);
    }

    @Override
    public Set<ParallelPlayer> getResidents() {
        return new HashSet<>(players);
    }

    public Set<ParallelPlayer> getPlayers() {return players;}
}
