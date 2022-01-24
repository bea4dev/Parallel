package be4rjp.parallel.world;

import be4rjp.parallel.util.ChunkUtil;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ImplParallelWorld {
    
    private final String worldName;
    
    public ImplParallelWorld(String worldName){
        this.worldName = worldName;
    }
    
    public String getWorldName() {return worldName;}
    
    
    
    private final Map<Long, ImplParallelChunk> chunkMap = new ConcurrentHashMap<>();
    
    public ImplParallelChunk getChunk(int chunkX, int chunkZ){return chunkMap.get(ChunkUtil.getCoordinateKey(chunkX, chunkZ));}
    
    public void setChunk(ImplParallelChunk implParallelChunk){
        
    }
}
