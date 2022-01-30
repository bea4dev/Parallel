package be4rjp.parallel.impl;

import be4rjp.parallel.ParallelChunk;
import be4rjp.parallel.ParallelUniverse;
import be4rjp.parallel.ParallelWorld;
import be4rjp.parallel.nms.NMSManager;
import be4rjp.parallel.player.ParallelPlayer;
import be4rjp.parallel.util.SectionLevelArray;
import be4rjp.parallel.util.SectionTypeArray;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
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
    public @NotNull String getName() {return universeName;}


    private final Map<String, ParallelWorld> parallelWorldMap = new ConcurrentHashMap<>();

    @Override
    public @NotNull ParallelWorld getWorld(String worldName) {
        return parallelWorldMap.computeIfAbsent(worldName, name -> new ImplParallelWorld(this, worldName));
    }

    @Override
    public void addPlayer(@NotNull ParallelPlayer player) {player.setUniverse(this);}

    @Override
    public void removePlayer(@NotNull ParallelPlayer player) {player.setUniverse(null);}

    @Override
    public Set<ParallelPlayer> getResidents() {return new HashSet<>(players);}
    
    @Override
    public Collection<ParallelWorld> getAllWorld() {return parallelWorldMap.values();}
    
    @Override
    public void addDiffs(ParallelUniverse universe) {
        int indexStart = NMSManager.isHigher_v1_18_R1() ? -4 : 0;
        int indexEnd = NMSManager.isHigher_v1_18_R1() ? 20 : 16;
        
        for(ParallelWorld diffWorld : universe.getAllWorld()){
            for(ParallelChunk diffChunk : diffWorld.getAllChunk()){
                for(int i = indexStart; i < indexEnd; i++){
                    ParallelWorld thisWorld = null;
                    ParallelChunk thisChunk = null;
            
                    SectionTypeArray sectionTypeArray = diffChunk.getSectionTypeArray(i);
                    if(sectionTypeArray != null) {
                        thisWorld = this.getWorld(diffWorld.getName());
                        thisChunk = ((ImplParallelWorld) thisWorld).createChunkIfAbsent(diffChunk.getChunkX(), diffChunk.getChunkZ());
                        SectionTypeArray thisType = ((ImplParallelChunk) thisChunk).createSectionTypeArrayIfAbsent(i);
                        
                        sectionTypeArray.threadsafeIteration(thisType::setType);
                    }
    
                    SectionLevelArray blockLightLevelArray = diffChunk.getBlockLightSectionLevelArray(i);
                    if(blockLightLevelArray != null){
                        if(thisWorld == null) thisWorld = this.getWorld(diffWorld.getName());
                        if(thisChunk == null) thisChunk = ((ImplParallelWorld) thisWorld).createChunkIfAbsent(diffChunk.getChunkX(), diffChunk.getChunkZ());
                        SectionLevelArray thisLevel = ((ImplParallelChunk) thisChunk).createBlockLightSectionLevelArrayIfAbsent(i);
    
                        blockLightLevelArray.threadsafeIteration(thisLevel::setLevel);
                    }
    
                    SectionLevelArray skyLightLevelArray = diffChunk.getSkyLightSectionLevelArray(i);
                    if(skyLightLevelArray != null){
                        if(thisWorld == null) thisWorld = this.getWorld(diffWorld.getName());
                        if(thisChunk == null) thisChunk = ((ImplParallelWorld) thisWorld).createChunkIfAbsent(diffChunk.getChunkX(), diffChunk.getChunkZ());
                        SectionLevelArray thisLevel = ((ImplParallelChunk) thisChunk).createSkyLightSectionLevelArrayIfAbsent(i);
        
                        skyLightLevelArray.threadsafeIteration(thisLevel::setLevel);
                    }
                }
            }
        }
        
        
        for(ParallelPlayer parallelPlayer : this.getResidents()){
            ((ImplParallelPlayer) parallelPlayer).setUniverseRaw(null);
            parallelPlayer.setUniverse(this);
        }
    }
    
    public Set<ParallelPlayer> getPlayers() {return players;}
}
