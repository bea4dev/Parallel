package be4rjp.parallel.impl;

import be4rjp.parallel.ParallelAPI;
import be4rjp.parallel.ParallelUniverse;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ImplParallelAPI extends ParallelAPI {
    
    //All universes
    private static final Map<String, ParallelUniverse> universeMap = new ConcurrentHashMap<>();
    
    @Override
    public @NotNull ParallelUniverse createUniverse(String universeName) {
        return universeMap.computeIfAbsent(universeName, ImplParallelUniverse::new);
    }
    
    @Override
    public @Nullable ParallelUniverse getUniverse(String universeName) {return universeMap.get(universeName);}
    
    @Override
    public void removeUniverse(String universeName) {
        ParallelUniverse universe = getUniverse(universeName);
        if(universe != null) ((ImplParallelUniverse) universe).getPlayers().forEach(player -> player.setUniverse(null));
    
        universeMap.remove(universeName);
    }
    
    @Override
    public Set<String> getAllUniverseName() {return universeMap.keySet();}
    
    @Override
    public Collection<ParallelUniverse> getAllUniverse() {return universeMap.values();}
}
